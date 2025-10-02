package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PdiProxy implements FachadaProcesadorPdI {

    private final PdiRetrofitClient service;
    private final Counter llamadasPdi;
    private final Counter erroresPdi;

    @Autowired
    public PdiProxy(ObjectMapper mapper,
                    MeterRegistry registry,
                    @Value("${URL_PDI:https://tpdds2025-procesadorpdi-2.onrender.com/}") String baseUrlEnv) {

        mapper.findAndRegisterModules();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final String baseUrl = ensureEndsWithSlash(baseUrlEnv);
        log.info("[PDI] Base URL: {}", baseUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        this.service = retrofit.create(PdiRetrofitClient.class);

        // Métricas mínimas
        this.llamadasPdi = Counter.builder("fuentes.pdi.llamadas").register(registry);
        this.erroresPdi  = Counter.builder("fuentes.pdi.errores").register(registry);
    }

    private static String ensureEndsWithSlash(String base) {
        if (base == null || base.isBlank()) return "https://tpdds2025-procesadorpdi-2.onrender.com/";
        return base.endsWith("/") ? base : base + "/";
    }

    @Override
    public PdIDTO procesar(PdIDTO dto) {
        Objects.requireNonNull(dto, "PdIDTO requerido");
        if (dto.hechoId() == null || dto.hechoId().isBlank())
            throw new IllegalStateException("hecho_id requerido");

        try {
            llamadasPdi.increment();
            Response<PdIDTO> resp = service.crear(dto).execute();

            if (resp.isSuccessful() && resp.body() != null) {
                return resp.body();
            }

            erroresPdi.increment();
            String errorBody = safeReadBody(resp);
            int code = resp.code();
            log.warn("[PDI] POST /pdis -> {} {} body={}", code, resp.message(), errorBody);

            if (code == 400) throw new IllegalStateException("PdI inválido: " + errorBody);
            if (code == 422) throw new IllegalStateException("PdI rechazado por Procesador: " + errorBody);
            throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + code + "): " + errorBody);

        } catch (Exception e) {
            erroresPdi.increment();
            throw new RuntimeException("No se pudo invocar ProcesadorPdI", e);
        }
    }

    @Override public PdIDTO buscarPdIPorId(String id) { throw new UnsupportedOperationException("no implementado en proxy"); }
    @Override public List<PdIDTO> buscarPorHecho(String hechoId) { throw new UnsupportedOperationException("no implementado en proxy"); }
    @Override public void setFachadaSolicitudes(FachadaSolicitudes f) {  }

    private static String safeReadBody(Response<?> resp) {
        try {
            @Nullable okhttp3.ResponseBody eb = resp.errorBody();
            return (eb == null) ? "" : eb.string();
        } catch (Exception e) {
            return "<unreadable>";
        }
    }

}