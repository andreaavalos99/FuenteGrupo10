package ar.edu.utn.dds.k3003.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
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