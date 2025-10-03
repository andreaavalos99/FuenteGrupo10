package ar.edu.utn.dds.k3003.controller;


import ar.edu.utn.dds.k3003.mensajeria.HechosPublisher;
import ar.edu.utn.dds.k3003.mensajeria.SubscriptionManager;
import ar.edu.utn.dds.k3003.model.Suscripcion;
import ar.edu.utn.dds.k3003.repository.SuscripcionRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SuscripcionesController {

    private final SuscripcionRepository repo;
    private final SubscriptionManager manager;
    private final HechosPublisher publisher;

    @Value("${MSG_EXCHANGE:eventos}") private String exchange;
    @Value("${MSG_RK_HECHO:hecho.creado}") private String rkHecho;

    public SuscripcionesController(SuscripcionRepository repo,
                                   SubscriptionManager manager,
                                   HechosPublisher publisher) {
        this.repo = repo; this.manager = manager; this.publisher = publisher;
    }

    @PostMapping("/suscripciones")
    public ResponseEntity<Suscripcion> suscribir(@RequestBody AltaSuscripcion req) throws Exception {
        Suscripcion s = manager.subscribeTopic(req.topic);
        return ResponseEntity.ok(s);
    }

    @GetMapping("/suscripciones")
    public List<Suscripcion> listar() { return repo.findAll(); }

    @DeleteMapping("/suscripciones/{id}")
    public ResponseEntity<Void> desuscribir(@PathVariable String id) throws Exception {
        manager.unsubscribe(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/_admin/publish")
    public ResponseEntity<Void> publish(@RequestBody PublishReq req) {
        String rk = (req.routingKey == null || req.routingKey.isBlank()) ? rkHecho : req.routingKey;
        publisher.publicarRaw(rk, req.payload);
        return ResponseEntity.accepted().build();
    }

    @Data public static class AltaSuscripcion { String topic; }
    @Data public static class PublishReq { String routingKey; String payload; }
}
