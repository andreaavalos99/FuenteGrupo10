package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Hecho;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/*
public class InMemoryHechoRepo implements HechoRepository {
    private List<Hecho> hechos;
    private Integer countId;

    public InMemoryHechoRepo() {
        this.hechos = new ArrayList<>();
        this.countId = 1;
    }

    @Override
    public Optional<Hecho> findById(String id) {
        return this.hechos.stream().filter(x -> (x.getId().toString().equals(id))).findFirst();
    }

    @Override
    public Hecho save(Hecho hecho) {
        hecho.setId(this.countId);
        this.hechos.add(hecho);
        this.countId++;
        return hecho;
    }
    @Override
    public List<Hecho> findAll() {
        return this.hechos;
    }
}


 */