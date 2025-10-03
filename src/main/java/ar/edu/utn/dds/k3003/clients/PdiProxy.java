package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dto.PdiProcesadorDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import okhttp3.OkHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Component
public class PdiProxy {
    private static final Logger log = LoggerFactory.getLogger(PdiProxy.class);
    private final PdiRetrofitClient api;

    public PdiProxy(ObjectMapper mapper,
                    @Value("${URL_PDI:https://tpdds2025-procesadorpdi-2.onrender.com/}") String baseUrl) {

        ObjectMapper clientMapper = mapper.copy();
        clientMapper.findAndRegisterModules();
        clientMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(JacksonConverterFactory.create(clientMapper))
                .client(new OkHttpClient())
                .build();

        this.api = retrofit.create(PdiRetrofitClient.class);
        log.info("[PDI] Base URL: {}", baseUrl);
        log.info("[PDI] mapper del cliente en SNAKE_CASE (solo para llamada al procesador)");
    }

    public PdiProcesadorDTO crear(PdiProcesadorDTO dto) {
        try {
            Response<PdiProcesadorDTO> r = api.crear(dto).execute();
            if (!r.isSuccessful() || r.body() == null) {
                String body = null;
                try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignore) {}
                log.warn("[PDI] error {} al crear PDI. errorBody={}", r.code(), body);
                return null;
            }
            return r.body();
        } catch (Exception e) {
            log.warn("[PDI] no disponible: {}", e.toString());
            return null;
        }
    }
}
