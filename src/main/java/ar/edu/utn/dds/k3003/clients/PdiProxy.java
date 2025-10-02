package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.clients.PdiRetrofitClient;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.extern.slf4j.Slf4j;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Component
@Slf4j
public class PdiProxy implements FachadaProcesadorPdI {

    private final PdiRetrofitClient service;

    private final Counter llamadasPdi;
    private final Counter erroresPdi;
    private final Timer tiempoPdi;



    public PdiProxy(ObjectMapper mapper,
                    MeterRegistry registry,
                    @Value("${URL_PDI:https://tpdds2025-procesadorpdi-2.onrender.com/}") String baseUrlEnv) {

        mapper.findAndRegisterModules();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final String baseUrl = ensureEndsWithSlash(baseUrlEnv);
        log.info("[PDI] Base URL: {}", baseUrl);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                msg -> log.info("[PDI http] {}", msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor requestIdInterceptor = chain -> {
            Request req = chain.request().newBuilder()
                    .header("X-Request-Id", UUID.randomUUID().toString())
                    .build();
            return chain.proceed(req);
        };

        Interceptor retryInterceptor = chain -> {
            Request req = chain.request();
            int attempts = 0;
            int max = 2;
            long backoff = 300L;

            while (true) {
                attempts++;
                okhttp3.Response resp = null;
                try {
                    resp = chain.proceed(req);
                    if (resp.code() >= 500 && attempts < max) {
                        resp.close();
                        sleep(backoff);
                        backoff *= 2;
                        continue;
                    }
                    return resp;
                } catch (IOException io) {
                    if (resp != null) resp.close();
                    if (attempts < max) {
                        sleep(backoff);
                        backoff *= 2;
                        continue;
                    }
                    throw io;
                }
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(requestIdInterceptor)
                .addInterceptor(retryInterceptor)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        this.service = retrofit.create(PdiRetrofitClient.class);

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

    private static void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido durante backoff", ie);
        }
    }

    private static String ensureEndsWithSlash(String base) {
        if (base == null || base.isBlank()) return "http://localhost:8082/";
        return base.endsWith("/") ? base : base + "/";
    }

    @Override
    public PdIDTO procesar(PdIDTO dto) throws IllegalStateException {
        Objects.requireNonNull(dto, "PdIDTO requerido");
        if (dto.hechoId() == null || dto.hechoId().isBlank()) {   // ← record accessor
            throw new IllegalStateException("hecho_id requerido");
        }

        return tiempoPdi.record(() -> {
            try {
                llamadasPdi.increment();
                Response<PdIDTO> resp = exec(service.crear(dto));
                if (resp.isSuccessful()) {
                    PdIDTO body = resp.body();
                    if (body == null) throw new IllegalStateException("Respuesta vacía de ProcesadorPdI");
                    return body;
                }
                erroresPdi.increment();
                String errorBody = safeReadBody(resp);
                int code = resp.code();
                log.warn("[PDI] POST /pdis -> {} {} body={}", code, resp.message(), errorBody);

                if (code == 400) throw new IllegalStateException("PdI inválido");
                if (code == 422) throw new IllegalStateException("ProcesadorPdI rechazó la PdI (unprocessable)");
                throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + code + ")");
            } catch (RuntimeException e) {
                erroresPdi.increment();
                throw e;
            }
        });
    }


    @Override
    public PdIDTO buscarPdIPorId(String id) throws NoSuchElementException {
        Objects.requireNonNull(id, "id requerido");
        return tiempoPdi.record(() -> {
            llamadasPdi.increment();
            Response<PdIDTO> resp = exec(service.get(id));
            if (resp.isSuccessful()) {
                PdIDTO body = resp.body();
                if (body == null) throw new NoSuchElementException("PdI no encontrado");
                return body;
            }
            erroresPdi.increment();
            int code = resp.code();
            if (code == 404) throw new NoSuchElementException("PdI no encontrado");
            throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + code + ")");
        });
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
        Objects.requireNonNull(hechoId, "hechoId requerido");
        return tiempoPdi.record(() -> {
            llamadasPdi.increment();
            Response<List<PdIDTO>> resp = exec(service.porHecho(hechoId));
            if (resp.isSuccessful()) {
                return Optional.ofNullable(resp.body()).orElseGet(List::of);
            }
            erroresPdi.increment();
            throw new RuntimeException("Error conectando ProcesadorPdI (HTTP " + resp.code() + ")");
        });
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
    }

    private static <T> Response<T> exec(Call<T> call) {
        try { return call.execute(); }
        catch (Exception e) {
            throw new RuntimeException("Fallo al invocar ProcesadorPdI", e);
        }
    }

    private static String safeReadBody(Response<?> resp) {
        try {
            @Nullable okhttp3.ResponseBody eb = resp.errorBody();
            return (eb == null) ? "" : eb.string();
        } catch (Exception e) {
            return "<unreadable>";
        }
    }
}