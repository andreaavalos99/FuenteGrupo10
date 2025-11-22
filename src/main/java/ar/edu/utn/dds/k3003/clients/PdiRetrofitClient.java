package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dto.PdiProcesadorDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PdiRetrofitClient {

    @POST("pdis")
    Call<PdiProcesadorDTO> crear(@Body PdiProcesadorDTO body, @Query("asinc") boolean asinc);
}
