package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.security.InvalidParameterException;
import java.util.Map;

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
        try { Integer.parseInt(id); }
        catch (NumberFormatException e) {
            throw new InvalidParameterException("id inválido: debe ser numérico");
        }
        return ResponseEntity.ok(fachadaFuente.buscarHechoXId(id));
    }

    @PostMapping("/hecho")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO hecho) {
        if (hecho == null) throw new InvalidParameterException("body requerido");
        if (hecho.nombreColeccion() == null || hecho.nombreColeccion().isBlank())
            throw new InvalidParameterException("nombreColeccion es requerido");

        HechoDTO guardado = fachada.altaHecho(hecho);
        return ResponseEntity.ok(guardado);
    }

    static record EstadoRequest(String estado) {}

    @PatchMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> actualizarEstado(@PathVariable String id, @RequestBody EstadoRequest req) {
        try { Integer.parseInt(id); }
        catch (NumberFormatException e) { throw new InvalidParameterException("id inválido: debe ser numérico"); }

        if (req == null || req.estado() == null || req.estado().isBlank())
            throw new InvalidParameterException("estado es requerido");

        return ResponseEntity.ok(fachada.actualizarEstadoHecho(id, req.estado()));
    }

    @PatchMapping("/hecho/{id}/censurar")
    public ResponseEntity<Void> censurarHecho(@PathVariable String id) {
        try { Integer.parseInt(id); }
        catch (NumberFormatException e) { throw new InvalidParameterException("id inválido: debe ser numérico"); }

        fachada.actualizarEstadoHecho(id, "CENSURADO");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> eliminarTodo() {
        fachada.vaciarHechosYColecciones();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hechos/sin-solicitudes")
    public ResponseEntity<List<HechoDTO>> listarHechosSinSolicitudes(
            @RequestParam(defaultValue = "ACTIVO") String estado,
            @RequestParam(required = false) String nombre) {

        String est = estado == null ? "" : estado.trim().toUpperCase();
        if (!est.equals("ACTIVO") && !est.equals("CENSURADO"))
            throw new InvalidParameterException("estado inválido: use ACTIVO o CENSURADO");

        return ResponseEntity.ok(fachada.listarHechosSinSolicitudes(est, nombre));
    }

    @PostMapping("/pdi")
    public ResponseEntity<PdIDTO> crearPdi(@RequestBody PdIDTO dto) {
        if (dto == null) throw new InvalidParameterException("body requerido");
        if (dto.hechoId() == null || dto.hechoId().isBlank())
            throw new InvalidParameterException("hechoId es requerido");

        fachadaFuente.buscarHechoXId(dto.hechoId());

        PdIDTO procesado = fachada.agregar(dto);
        return ResponseEntity.ok(procesado);
    }

    @PostMapping("/busqueda")
    public ResponseEntity<List<HechoDTO>> reindexBusqueda() {
        List<HechoDTO> enviados = fachada.reindexarTodosEnBusqueda();
        return ResponseEntity.ok(enviados);
    }

}
