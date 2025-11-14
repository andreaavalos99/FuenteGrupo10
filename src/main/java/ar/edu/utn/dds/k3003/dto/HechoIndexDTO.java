package ar.edu.utn.dds.k3003.dto;


import java.util.List;

public record HechoIndexDTO(
        String idHecho,
        String coleccion,
        String titulo,
        ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum categoria,
        String ubicacion,
        java.time.LocalDateTime fecha,
        String origen,
        List<String> etiquetas
) {}
