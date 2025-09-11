package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PdiProxy implements FachadaProcesadorPdI {
    private final PdiRetrofitClient service;

    private final Counter llamadasPdi;
    private final Counter erroresPdi;
    private final Timer   tiempoPdi;

    public PdiProxy(ObjectMapper mapper, MeterRegistry registry) {
        String base = System.getenv().getOrDefault("URL_PDI", "http://localhost:8082/");
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        Retrofit r = new Retrofit.Builder()
                .baseUrl(base)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        this.service = r.create(PdiRetrofitClient.class);

        this.llamadasPdi = Counter.builder("fuentes.pdi.llamadas")
                .description("Llamadas salientes a ProcesadorPdI")
                .register(registry);

        this.erroresPdi = Counter.builder("fuentes.pdi.errores")
                .description("Errores en integración con ProcesadorPdI")
                .register(registry);

        this.tiempoPdi = Timer.builder("fuentes.pdi.tiempo")
                .description("Tiempo de llamadas a ProcesadorPdI")
                .register(registry);
    }

    public PdiProxy(PdiRetrofitClient service, Counter llamadasPdi, Counter erroresPdi, Timer tiempoPdi) {
        this.service = service;
        this.llamadasPdi = llamadasPdi;
        this.erroresPdi = erroresPdi;
        this.tiempoPdi = tiempoPdi;
    }

    @Override
    public PdIDTO procesar(PdIDTO dto) throws IllegalStateException {
        return tiempoPdi.record(() -> {
            try {
                llamadasPdi.increment();
                Response<PdIDTO> resp = exec(service.crear(dto));
                if (resp.isSuccessful()) return resp.body();
                erroresPdi.increment();
                if (resp.code() == HttpStatus.BAD_REQUEST.getCode()) {
                    throw new IllegalStateException("PdI inválido");
                }
                throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + resp.code() + ")");
            } catch (RuntimeException e) {
                erroresPdi.increment();
                throw e;
            }
        });
    }

    @Override
    public PdIDTO buscarPdIPorId(String id) throws NoSuchElementException {
        return tiempoPdi.record(() -> {
            llamadasPdi.increment();
            Response<PdIDTO> resp = exec(service.get(id));
            if (resp.isSuccessful()) return resp.body();
            erroresPdi.increment();
            if (resp.code() == HttpStatus.NOT_FOUND.getCode()) {
                throw new NoSuchElementException("PdI no encontrado");
            }
            throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + resp.code() + ")");
        });
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
        return tiempoPdi.record(() -> {
            llamadasPdi.increment();
            Response<List<PdIDTO>> resp = exec(service.porHecho(hechoId));
            if (resp.isSuccessful()) return Optional.ofNullable(resp.body()).orElseGet(List::of);
            erroresPdi.increment();
            throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + resp.code() + ")");
        });
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
    }

    private static <T> Response<T> exec(Call<T> c) {
        try { return c.execute(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
