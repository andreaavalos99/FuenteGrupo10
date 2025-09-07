package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ColeccionController {

    private final FachadaFuente fachadaFuente;

    @Autowired
    public ColeccionController(FachadaFuente fachadaFuente) {
        this.fachadaFuente = fachadaFuente;
    }

    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionDTO>> listarColecciones() {
        return ResponseEntity.ok(fachadaFuente.colecciones());
    }

    @GetMapping("/coleccion/{nombre}")
    public ResponseEntity<ColeccionDTO> obtenerColeccion(@PathVariable String nombre) {
        return ResponseEntity.ok(fachadaFuente.buscarColeccionXId(nombre));
    }
    @GetMapping("/coleccion/{nombre}/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechosXColeccion(@PathVariable String nombre) {
        return ResponseEntity.ok(fachadaFuente.buscarHechosXColeccion(nombre));
    }

    @PostMapping("/coleccion")
    public ResponseEntity<ColeccionDTO> crearColeccion(@RequestBody ColeccionDTO coleccion) {
        return ResponseEntity.ok(fachadaFuente.agregar(coleccion));
    }
} 