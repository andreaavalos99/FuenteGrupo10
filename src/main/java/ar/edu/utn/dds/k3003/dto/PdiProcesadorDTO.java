package ar.edu.utn.dds.k3003.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdiProcesadorDTO {
    private String id;
    private String hechoId;
    private String descripcion;
    private String lugar;
    private LocalDateTime momento;
    private String contenido;
    private List<String> etiquetas;
    private String resultadoOcr;
    private String urlImagen;
}
