package ar.edu.utn.dds.k3003.clients;


import ar.edu.utn.dds.k3003.dto.HechoIndexDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Component
public class BusquedaProxy {

    private static final Logger log = LoggerFactory.getLogger(BusquedaProxy.class);

    private final BusquedaRetrofitClient api;

    public BusquedaProxy(ObjectMapper mapper,
                         @Value("${URL_BUSQUEDA:https://busquedaservice.onrender.com/}") String baseUrl) {

        ObjectMapper clientMapper = mapper.copy();
        clientMapper.findAndRegisterModules();
        clientMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::info);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient http = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(JacksonConverterFactory.create(clientMapper))
                .client(http)
                .build();

        this.api = retrofit.create(BusquedaRetrofitClient.class);
        log.info("[BUSQUEDA] Base URL: {}", baseUrl);
    }

    public void indexarHecho(HechoDTO hecho) {
        HechoIndexDTO dto = new HechoIndexDTO(
                hecho.id(),
                hecho.nombreColeccion(),
                hecho.titulo()
        );

        try {
            Response<Void> r = api.upsertHecho(dto).execute();
            if (!r.isSuccessful()) {
                String body = null;
                try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignore) {}
                log.warn("[BUSQUEDA] error {} al indexar Hecho {}. errorBody={}",
                        r.code(), hecho.id(), body);
            } else {
                log.info("[BUSQUEDA] indexado Hecho {} correctamente", hecho.id());
            }
        } catch (Exception e) {
            log.warn("[BUSQUEDA] no disponible al indexar Hecho {}: {}", hecho.id(), e.toString());
        }
    }
}
