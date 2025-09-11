package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HechoRepository extends JpaRepository<Hecho, Integer> {
    List<Hecho> findAll();
    Hecho save(Hecho col);

    List<Hecho> findByNombreColeccionAndEstado(String nombreColeccion, ar.edu.utn.dds.k3003.model.EstadoHecho estado);

}
