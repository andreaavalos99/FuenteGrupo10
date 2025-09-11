package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
public class Coleccion {

    @Id
    private String nombre;

    private String descripcion;

    private LocalDateTime fechaModificacion;

    @PrePersist
    @PreUpdate
    void touch() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public Coleccion() {
        // Constructor vacío requerido por JPA
    }

}
