package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repository.ColeccionRepository;
import ar.edu.utn.dds.k3003.repository.HechoRepository;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
public class Fachada implements FachadaFuente {

    private final ColeccionRepository coleccionRepo;
    private final HechoRepository hechoRepo;

    private FachadaProcesadorPdI procesadorPdI;

    private final Counter coleccionesCreadas;
    private final Counter hechosCreados;
    private final Counter erroresDominio;
    private final Timer   tiempoAltaHecho;

    @Autowired
    public Fachada(ColeccionRepository coleccionRepository,
                   HechoRepository hechoRepository,
                   MeterRegistry meterRegistry) {
        this.coleccionRepo = coleccionRepository;
        this.hechoRepo = hechoRepository;

        this.coleccionesCreadas = Counter.builder("fuentes.colecciones.creadas")
                .description("Cantidad de colecciones creadas").register(meterRegistry);

        this.hechosCreados = Counter.builder("fuentes.hechos.creados")
                .description("Cantidad de hechos creados").register(meterRegistry);

        this.erroresDominio = Counter.builder("fuentes.errores")
                .description("Errores de negocio en Fuentes").register(meterRegistry);

        this.tiempoAltaHecho = Timer.builder("fuentes.hechos.alta.tiempo")
                .description("Tiempo de alta de hecho").register(meterRegistry);
    }


    @Override
    public ColeccionDTO agregar(ColeccionDTO dto) {
        if (dto == null || dto.nombre() == null || dto.nombre().isBlank()) {
            erroresDominio.increment();
            throw new IllegalArgumentException("Nombre de colección inválido");
        }
        if (coleccionRepo.findById(dto.nombre()).isPresent()) {
            erroresDominio.increment();
            throw new IllegalArgumentException(dto.nombre() + " ya existe");
        }
        var c = new Coleccion(dto.nombre(), dto.descripcion(), null);
        coleccionRepo.save(c);
        coleccionesCreadas.increment();
        return new ColeccionDTO(c.getNombre(), c.getDescripcion());
    }

    @Override @Transactional(readOnly = true)
    public ColeccionDTO buscarColeccionXId(String id) {
        var c = coleccionRepo.findById(id).orElseThrow(() -> new NoSuchElementException(id + " no existe"));
        return new ColeccionDTO(c.getNombre(), c.getDescripcion());
    }

    @Override @Transactional(readOnly = true)
    public List<ColeccionDTO> colecciones() {
        return coleccionRepo.findAll().stream()
                .map(c -> new ColeccionDTO(c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Override
    public HechoDTO agregar(HechoDTO dto) {
        return tiempoAltaHecho.record(() -> {
            if (dto == null || dto.nombreColeccion() == null || dto.nombreColeccion().isBlank()) {
                erroresDominio.increment();
                throw new IllegalArgumentException("No se pasó nombre de colección");
            }
            coleccionRepo.findById(dto.nombreColeccion()).orElseThrow(
                    () -> new IllegalArgumentException(dto.nombreColeccion() + " no existe colección con ese nombre")
            );

            try {
                var h = new Hecho(
                        null,
                        dto.nombreColeccion(),
                        dto.titulo(),
                        dto.etiquetas(),
                        dto.categoria(),
                        dto.ubicacion(),
                        dto.fecha(),
                        dto.origen(),
                        EstadoHecho.ACTIVO
                );
                var g = hechoRepo.save(h);
                hechosCreados.increment();
                return new HechoDTO(
                        g.getId().toString(), g.getNombreColeccion(), g.getTitulo(), g.getEtiquetas(),
                        g.getCategoria(), g.getUbicacion(), g.getFecha(), g.getOrigen()
                );
            } catch (RuntimeException e) {
                erroresDominio.increment();
                log.error("Error al dar de alta Hecho", e);
                throw e;
            }
        });
    }

    @Override @Transactional(readOnly = true)
    public HechoDTO buscarHechoXId(String hechoId) {
        final int id;
        try {
            id = Integer.parseInt(hechoId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id inválido: debe ser numérico");
        }

        var h = hechoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException(hechoId + " no existe"));

        return new HechoDTO(
                h.getId().toString(), h.getNombreColeccion(), h.getTitulo(), h.getEtiquetas(),
                h.getCategoria(), h.getUbicacion(), h.getFecha(), h.getOrigen()
        );
    }

    @Override @Transactional(readOnly = true)
    public List<HechoDTO> buscarHechosXColeccion(String nombreColeccion) {
        coleccionRepo.findById(nombreColeccion).orElseThrow(
                () -> new NoSuchElementException(nombreColeccion + " no existe coleccion con ese nombre")
        );
        return hechoRepo.findByNombreColeccionAndEstado(nombreColeccion, EstadoHecho.ACTIVO)
                .stream().map(h -> new HechoDTO(
                        h.getId().toString(), h.getNombreColeccion(), h.getTitulo(), h.getEtiquetas(),
                        h.getCategoria(), h.getUbicacion(), h.getFecha(), h.getOrigen()
                )).toList();
    }

    public HechoDTO actualizarEstadoHecho(String hechoId, String estadoTexto) {
        final int id;
        try {
            id = Integer.parseInt(hechoId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id inválido: debe ser numérico");
        }

        var hecho = hechoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hecho " + hechoId + " no existe"));

        var nuevo = switch (estadoTexto == null ? "" : estadoTexto.trim().toLowerCase()) {
            case "borrado", "censurado" -> EstadoHecho.CENSURADO;
            case "activo"               -> EstadoHecho.ACTIVO;
            default -> {
                erroresDominio.increment();
                throw new IllegalArgumentException("estado inválido: " + estadoTexto);
            }
        };

        hecho.setEstado(nuevo);
        var g = hechoRepo.save(hecho);
        return new HechoDTO(
                g.getId().toString(), g.getNombreColeccion(), g.getTitulo(), g.getEtiquetas(),
                g.getCategoria(), g.getUbicacion(), g.getFecha(), g.getOrigen()
        );
    }
    @Transactional
    public void vaciarHechosYColecciones() {
        hechoRepo.deleteAllInBatch();
        coleccionRepo.deleteAllInBatch();
    }
    // ====== PdI ======
    @Override public void setProcesadorPdI(FachadaProcesadorPdI f) { this.procesadorPdI = f; }
    @Override public PdIDTO agregar(PdIDTO p) { return procesadorPdI.procesar(p); }
}
