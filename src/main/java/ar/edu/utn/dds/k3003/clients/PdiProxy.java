package ar.edu.utn.dds.k3003.clients;
import ar.edu.utn.dds.k3003.model.PdiDTO;
import io.javalin.http.HttpStatus;

import java.util.*;


import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class FuentesProxy implements FachadaFuente {
    private final String endpoint;
    private final FuentesRetrofitClient service;

    // Sugerencia: usá SNAKE_CASE para alinear con Entrega 2
    public FuentesProxy(ObjectMapper mapper) {
        var env = System.getenv();
        this.endpoint = env.getOrDefault("URL_FUENTES", "http://localhost:8080/");

        // Asegurá snake_case si tu API lo expone así (Entrega 2)
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.endpoint)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        this.service = retrofit.create(FuentesRetrofitClient.class);
    }

    // ---------- Métodos de FachadaFuente (consumen por HTTP a "Fuente") ----------
    @Override
    public ColeccionDTO agregar(ColeccionDTO dto) {
        Response<ColeccionDTO> r = exec(service.crearColeccion(dto));
        if (r.isSuccessful()) return r.body();
        throw errorFrom(r);
    }

    @Override
    public ColeccionDTO buscarColeccionXId(String nombre) {
        Response<ColeccionDTO> r = exec(service.coleccion(nombre));
        if (r.isSuccessful()) return r.body();
        throw errorFrom(r);
    }

    @Override
    public HechoDTO agregar(HechoDTO dto) {
        // Tu API de Fuente expone POST /hecho (según tu controller)
        // Si tu API remota lo tiene, agregá en el client:
        // Call<HechoDTO> crearHecho(@Body HechoDTO body);
        // y llamalo aquí. Si aún no está expuesto, lanzar excepción clara:
        throw new UnsupportedOperationException("El endpoint remoto de alta de Hecho no está definido en FuentesRetrofitClient.");
    }

    @Override
    public HechoDTO buscarHechoXId(String id) {
        Response<HechoDTO> r = exec(service.hechoPorId(id));
        if (r.isSuccessful()) return r.body();
        throw errorFrom(r);
    }

    @Override
    public List<HechoDTO> buscarHechosXColeccion(String nombre) {
        Response<List<HechoDTO>> r = exec(service.hechosDeColeccion(nombre));
        if (r.isSuccessful()) return Optional.ofNullable(r.body()).orElseGet(List::of);
        throw errorFrom(r);
    }

    @Override
    public void setProcesadorPdI(ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI fachadaProcesadorPdI) {
        // No aplica en el cliente (lo maneja el servicio Fuente del lado servidor)
    }

    @Override
    public ar.edu.utn.dds.k3003.facades.dtos.PdIDTO agregar(ar.edu.utn.dds.k3003.facades.dtos.PdIDTO pdIDTO) throws IllegalStateException {
        // Este método pertenece a la interfaz de Fuente de tu evaluador anterior.
        // En un proxy remoto, solo tendría sentido si Fuente expone /pdis para alta (no es parte de Fuentes).
        throw new UnsupportedOperationException("Operación no soportada en el proxy de Fuente.");
    }

    @Override
    public List<ColeccionDTO> colecciones() {
        Response<List<ColeccionDTO>> r = exec(service.colecciones());
        if (r.isSuccessful()) return Optional.ofNullable(r.body()).orElseGet(List::of);
        throw errorFrom(r);
    }

    // --------- Helpers ----------

    private static <T> Response<T> exec(retrofit2.Call<T> c) {
        try { return c.execute(); }
        catch (Exception e) { throw new RuntimeException("No se pudo conectar con FUENTES", e); }
    }

    private RuntimeException errorFrom(Response<?> r) {
        int code = r.code();
        if (code == HttpStatus.NOT_FOUND.getCode()) return new NoSuchElementException("Recurso no encontrado en FUENTES");
        if (code == HttpStatus.BAD_REQUEST.getCode()) return new InvalidParameterException("Parámetros inválidos para FUENTES");
        return new RuntimeException("Error FUENTES HTTP " + code);
    }
}