package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data @Entity @NoArgsConstructor @AllArgsConstructor
public class Hecho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombreColeccion;
    private String titulo;

    @ElementCollection
    @CollectionTable(name = "hecho_etiquetas", joinColumns = @JoinColumn(name = "hecho_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas;

    @Enumerated(EnumType.STRING)
    private CategoriaHechoEnum categoria;

    private String ubicacion;
    private LocalDateTime fecha;
    private String origen;

    @Enumerated(EnumType.STRING)
    private EstadoHecho estado = EstadoHecho.ACTIVO;
}
