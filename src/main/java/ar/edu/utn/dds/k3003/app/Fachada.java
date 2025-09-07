package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.Coleccion;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.*;

import lombok.Data;
import lombok.val;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Fachada implements FachadaFuente {
    private ColeccionRepository coleccionRepo;
    private HechoRepository hechoRepo;
    private FachadaProcesadorPdI procesadorPdI;


    @Autowired
    public Fachada(ColeccionRepository coleccionRepository, HechoRepository hechoRepository) {
        this.coleccionRepo = coleccionRepository;
        this.hechoRepo = hechoRepository;
    }
    public Fachada(){
        this.coleccionRepo = new InMemoryColeccionRepo();
        this.hechoRepo = new InMemoryHechoRepo();
    }

    @Override
    public ColeccionDTO agregar(ColeccionDTO coleccionDTO) {
        if (this.coleccionRepo.findById(coleccionDTO.nombre()).isPresent()) {
            throw new IllegalArgumentException(coleccionDTO.nombre() + " ya existe");
        }
        Coleccion coleccion = new Coleccion(coleccionDTO.nombre(), coleccionDTO.descripcion());
        this.coleccionRepo.save(coleccion);
        return new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion());
    }

    @Override
    public ColeccionDTO buscarColeccionXId(String coleccionId) throws NoSuchElementException {
        val coleccionOptional = this.coleccionRepo.findById(coleccionId);
        if (coleccionOptional.isEmpty()) {
            throw new NoSuchElementException(coleccionId + " no existe");
        }
        val coleccion = coleccionOptional.get();
        return new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion());
    }

    @Override
    public HechoDTO agregar(HechoDTO hechoDTO) {
        if (Objects.equals(hechoDTO.nombreColeccion().trim(), "")) {
            throw new IllegalArgumentException(hechoDTO.id() + " no se paso nombre de coleccion");
        }
        /*
        //al añadir un hecho valido que exista una coleccion con el nombre de coleccion que tiene el hecho
        if (coleccionRepo.findById(hechoDTO.nombreColeccion()).isEmpty()) {
            throw new IllegalArgumentException(hechoDTO.nombreColeccion() + " no existe coleccion con ese nombre");
        }
        */
        if (this.coleccionRepo.findById(hechoDTO.id()).isPresent()) {
            throw new IllegalArgumentException(hechoDTO.id() + " ya existe");
        }
        Hecho hecho =
                new Hecho(
                        null,
                        hechoDTO.nombreColeccion(),
                        hechoDTO.titulo(),
                        hechoDTO.etiquetas(),
                        hechoDTO.categoria(),
                        hechoDTO.ubicacion(),
                        hechoDTO.fecha(),
                        hechoDTO.origen());
        // guardo en el repo y devuelvo el dto
        val resultadoHecho = hechoRepo.save(hecho);
        return new HechoDTO(
                resultadoHecho.getId().toString(),
                resultadoHecho.getNombreColeccion(),
                resultadoHecho.getTitulo(),
                resultadoHecho.getEtiquetas(),
                resultadoHecho.getCategoria(),
                resultadoHecho.getUbicacion(),
                resultadoHecho.getFecha(),
                resultadoHecho.getOrigen());
    }

    @Override
    public HechoDTO buscarHechoXId(String hechoId) throws NoSuchElementException {
        val hechoOptional = this.hechoRepo.findById(hechoId);
        if (hechoOptional.isEmpty()) {
            throw new NoSuchElementException(hechoId + " no existe");
        }
        val resultadoHecho = hechoOptional.get();
        return new HechoDTO(
                resultadoHecho.getId().toString(),
                resultadoHecho.getNombreColeccion(),
                resultadoHecho.getTitulo(),
                resultadoHecho.getEtiquetas(),
                resultadoHecho.getCategoria(),
                resultadoHecho.getUbicacion(),
                resultadoHecho.getFecha(),
                resultadoHecho.getOrigen());
    }

    @Override
    public List<HechoDTO> buscarHechosXColeccion(String s) throws NoSuchElementException {
        val coleccionOptional = this.coleccionRepo.findById(s);
        if (coleccionOptional.isEmpty()) {
            throw new NoSuchElementException(s + " no existe coleccion con ese nombre");
        }
        val hechos = this.hechoRepo.findAll();
        return hechos.stream()
                .filter(hecho -> hecho.getNombreColeccion().equals(s))
                .map(hecho -> new HechoDTO(
                        hecho.getId().toString(),
                        hecho.getNombreColeccion(),
                        hecho.getTitulo(),
                        hecho.getEtiquetas(),
                        hecho.getCategoria(),
                        hecho.getUbicacion(),
                        hecho.getFecha(),
                        hecho.getOrigen()))
                .toList();
    }

    @Override
    public void setProcesadorPdI(FachadaProcesadorPdI fachadaProcesadorPdI) {
        this.procesadorPdI = fachadaProcesadorPdI;
    }

    @Override
    public PdIDTO agregar(PdIDTO pdIDTO) throws IllegalStateException {
        return procesadorPdI.procesar(pdIDTO);
    }
    @Override
    public List<ColeccionDTO> colecciones(){
        return this.coleccionRepo.findAll().stream().map(coleccion -> new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion()) ).toList();
    }
}
