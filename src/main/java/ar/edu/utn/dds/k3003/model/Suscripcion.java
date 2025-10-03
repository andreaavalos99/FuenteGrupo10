package ar.edu.utn.dds.k3003.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
public class Suscripcion {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String queueName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();


}