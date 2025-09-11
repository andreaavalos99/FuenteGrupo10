package ar.edu.utn.dds.k3003.clients;


import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface PdiRetrofitClient {

    @POST("pdis")
    Call<PdIDTO> crear(@Body PdIDTO body);

    @GET("pdis/{id}")
    Call<PdIDTO> get(@Path("id") String id);

    @GET("pdis")
    Call<List<PdIDTO>> porHecho(@Query("hecho") String hechoId);
}

