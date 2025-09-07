package ar.edu.utn.dds.k3003.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class Coleccion {

    public Coleccion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Coleccion() {
        // Constructor vacío requerido por JPA
    }
    @Id
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaModificacion;

}
