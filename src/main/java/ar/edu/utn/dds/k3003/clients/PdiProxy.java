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
        mapper.findAndRegisterModules();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(new OkHttpClient())
                .build();
        this.api = retrofit.create(PdiRetrofitClient.class);
        log.info("[PDI] Base URL: {}", baseUrl);
    }

    public PdiProcesadorDTO crear(PdiProcesadorDTO dto) {
        try {
            Response<PdiProcesadorDTO> r = api.crear(dto).execute();
            if (!r.isSuccessful() || r.body() == null) {
                log.warn("[PDI] error {} al crear PDI", r.code());
                return null;
            }
            return r.body();
        } catch (Exception e) {
            log.warn("[PDI] no disponible: {}", e.toString());
            return null;
        }
    }
}