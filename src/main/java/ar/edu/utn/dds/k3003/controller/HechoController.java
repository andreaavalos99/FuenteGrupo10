package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
public class HechoController {

    private final FachadaFuente fachadaFuente;
    private final Fachada fachada;

    @Autowired
    public HechoController(FachadaFuente fachadaFuente, Fachada fachada) {
        this.fachadaFuente = fachadaFuente;
        this.fachada = fachada;
    }

    @GetMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> obtenerHecho(@PathVariable String id) {
        try {
            return ResponseEntity.ok(fachadaFuente.buscarHechoXId(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hecho no encontrado: " + id);
        }
    }


    @PostMapping("/hecho")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO hecho) {
        return ResponseEntity.ok(fachadaFuente.agregar(hecho));
    }

    static record EstadoRequest(String estado) {}

    @PatchMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> actualizarEstado(@PathVariable String id, @RequestBody EstadoRequest req) {
        return ResponseEntity.ok(fachada.actualizarEstadoHecho(id, req.estado()));
    }
}
