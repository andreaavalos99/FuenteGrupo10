package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dto.HechoConEstadoDTO;
import ar.edu.utn.dds.k3003.dto.HechoMapper;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.EstadoHecho;
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

    @PatchMapping("/hecho/{id}/censurar")
    public ResponseEntity<HechoDTO> censurarHecho(@PathVariable String id) {
        try {
            HechoDTO dto = fachada.actualizarEstadoHecho(id, "CENSURADO");
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error de datos: " + e.getMostSpecificCause().getMessage(), e);
        }
    }


    @DeleteMapping("/reset")
    public ResponseEntity<Void> eliminarTodo() {
        fachada.vaciarHechosYColecciones();
        return ResponseEntity.noContent().build();
    }


}
