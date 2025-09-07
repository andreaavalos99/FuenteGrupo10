package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HechoController {

    private final FachadaFuente fachadaFuente;

    @Autowired
    public HechoController(FachadaFuente fachadaFuente) {
        this.fachadaFuente = fachadaFuente;
    }

    //    @GetMapping
    //    public ResponseEntity<List<HechoDTO>> listarHechos() {
    //        return ResponseEntity.ok(fachadaFuente.Hechos());
    //    }

    @GetMapping("/hecho/{nombre}")
    public ResponseEntity<HechoDTO> obtenerHecho(@PathVariable String nombre) {
        return ResponseEntity.ok(fachadaFuente.buscarHechoXId(nombre));
    }

    @PostMapping("/hecho")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO Hecho) {
        return ResponseEntity.ok(fachadaFuente.agregar(Hecho));
    }
}
