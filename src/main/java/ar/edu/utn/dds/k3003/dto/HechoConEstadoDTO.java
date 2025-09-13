package ar.edu.utn.dds.k3003.dto;

import ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum;
import ar.edu.utn.dds.k3003.model.EstadoHecho;

import java.time.LocalDateTime;
import java.util.List;

public record HechoConEstadoDTO(
        String id,
        String nombreColeccion,
        String titulo,
        List<String> etiquetas,
        CategoriaHechoEnum categoria,
        String ubicacion,
        LocalDateTime fecha,
        String origen,
        EstadoHecho estado
) {
    public HechoConEstadoDTO(String id, String nombreColeccion, String titulo) {
        this(id, nombreColeccion, titulo, null, null, null, null, null, EstadoHecho.ACTIVO);
    }

    public HechoConEstadoDTO(String id, String nombreColeccion, String titulo,
                             List<String> etiquetas,
                             CategoriaHechoEnum categoria,
                             String ubicacion,
                             LocalDateTime fecha,
                             String origen) {
        this(id, nombreColeccion, titulo, etiquetas, categoria, ubicacion, fecha, origen, EstadoHecho.ACTIVO);
    }
    public String id() {
        return this.id;
    }

    public String nombreColeccion() {
        return this.nombreColeccion;
    }

    public String titulo() {
        return this.titulo;
    }

    public List<String> etiquetas() {
        return this.etiquetas;
    }

    public CategoriaHechoEnum categoria() {
        return this.categoria;
    }

    public String ubicacion() {
        return this.ubicacion;
    }

    public LocalDateTime fecha() {
        return this.fecha;
    }

    public String origen() {
        return this.origen;
    }
}
