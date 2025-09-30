package ar.edu.utn.dds.k3003.clients;


import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SolicitudesProxy {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SolicitudesProxy.class);

    private final SolicitudesRetrofitClient api;

    public SolicitudesProxy(ObjectMapper mapper,
                            @Value("${URL_SOLICITUDES:https://tpdds2025-solicitudes.onrender.com/}") String baseUrl) {

        mapper.findAndRegisterModules();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .client(new OkHttpClient())
                .build();

        this.api = retrofit.create(SolicitudesRetrofitClient.class);
        log.info("[Solicitudes] Base URL: {}", baseUrl);
    }

    public Set<Integer> obtenerIdsConSolicitudSeguros() {
        try {
            Response<Set<String>> r = api.idsConSolicitud().execute();
            if (!r.isSuccessful() || r.body() == null) return Set.of();
            return r.body().stream().map(s -> {
                try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("[Solicitudes] no disponible: {}", e.toString());
            return Set.of();
        }
    }
}
