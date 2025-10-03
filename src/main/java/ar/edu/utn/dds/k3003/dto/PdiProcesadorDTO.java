package ar.edu.utn.dds.k3003.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PdiProcesadorDTO(
        String id,
        String hechoId,
        String descripcion,
        String lugar,
        LocalDateTime momento,
        String contenido,
        List<String> etiquetas,
        String resultadoOcr,
        String urlImagen
) {
    public PdiProcesadorDTO(String id, String hechoId) {
        this(id, hechoId, null, null, null, null, List.of(), null, null);
    }
}