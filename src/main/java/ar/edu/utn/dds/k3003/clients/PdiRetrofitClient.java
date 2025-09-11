package ar.edu.utn.dds.k3003.clients;


import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;


public interface FuentesRetrofitClient {

    @GET("colecciones")
    Call<List<ColeccionDTO>> colecciones();

    @GET("coleccion/{nombre}")
    Call<ColeccionDTO> coleccion(@Path("nombre") String nombre);

    @POST("coleccion")
    Call<ColeccionDTO> crearColeccion(@Body ColeccionDTO body);

    @GET("coleccion/{nombre}/hechos")
    Call<List<HechoDTO>> hechosDeColeccion(@Path("nombre") String nombre);

    @GET("hecho/{id}")
    Call<HechoDTO> hechoPorId(@Path("id") String id);

    // PATCH /hecho/{id}  { "estado": "borrado" }
    @PATCH("hecho/{id}")
    Call<HechoDTO> patchEstadoHecho(@Path("id") String id, @Body Map<String, String> body);
}