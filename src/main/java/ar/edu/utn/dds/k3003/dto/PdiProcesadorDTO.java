package ar.edu.utn.dds.k3003.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PdiProcesadorDTO {
    private String id;

    @JsonProperty("hecho_id")
    private String hechoId;

    private String descripcion;
    private String lugar;
    private LocalDateTime momento;
    private String contenido;
    private List<String> etiquetas;

    @JsonProperty("resultado_ocr")
    private String resultadoOcr;

    @JsonProperty("url_imagen")
    private String urlImagen;

}