package ar.edu.utn.dds.k3003.dto;

import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.EstadoHecho;

public class HechoMapper {

    public static HechoConEstadoDTO toHechoConEstadoDTO(HechoDTO dto, EstadoHecho estado) {
        return new HechoConEstadoDTO(
                dto.id(),
                dto.nombreColeccion(),
                dto.titulo(),
                dto.etiquetas(),
                dto.categoria(),
                dto.ubicacion(),
                dto.fecha(),
                dto.origen(),
                estado
        );
    }
}