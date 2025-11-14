package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dto.HechoIndexDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BusquedaRetrofitClient {

    @POST("hechos")
    Call<Void> indexHecho(@Body HechoIndexDTO body);
}
