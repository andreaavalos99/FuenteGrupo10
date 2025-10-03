package ar.edu.utn.dds.k3003.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record PdiProcesadorDTO(
        @JsonProperty("id")
        String id,

        @JsonProperty("hechoId") @JsonAlias("hecho_id")
        String hechoId,

        @JsonProperty("descripcion")
        String descripcion,

        @JsonProperty("lugar")
        String lugar,

        @JsonProperty("momento")
        LocalDateTime momento,

        @JsonProperty("contenido")
        String contenido,

        @JsonProperty("etiquetas")
        List<String> etiquetas,

        @JsonProperty("resultadoOcr") @JsonAlias("resultado_ocr")
        String resultadoOcr,

        @JsonProperty("urlImagen") @JsonAlias("url_imagen")
        String urlImagen
) {
    public PdiProcesadorDTO(String id, String hechoId) {
        this(id, hechoId, null, null, null, null, List.of(), null, null);
    }
}