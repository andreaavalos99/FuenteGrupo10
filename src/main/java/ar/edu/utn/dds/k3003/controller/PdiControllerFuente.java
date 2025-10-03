package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dto.PdiProcesadorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdis")
public class PdiControllerFuente {

    private final Fachada fachada;

    public PdiControllerFuente(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<?> recibirYProcesar(@RequestBody PdiProcesadorDTO body) {
        var result = fachada.recibirYEnviarPdi(body);
        if (result == null) {
            return ResponseEntity.status(502).body("ProcesadorPdI no disponible o error al procesar");
        }
        return ResponseEntity.ok(result);
    }
}