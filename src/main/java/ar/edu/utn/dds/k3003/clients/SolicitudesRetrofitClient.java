package ar.edu.utn.dds.k3003.clients;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.Set;

public interface SolicitudesRetrofitClient {
    @GET("solicitudes/hechos")
    Call<Set<String>> idsConSolicitud();
}
