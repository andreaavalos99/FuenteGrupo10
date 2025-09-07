package ar.edu.utn.dds.k3003.model.exceptions;

import ar.edu.utn.dds.k3003.app.Fachada;

import lombok.Getter;

@Getter
public class Fuente extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Fachada anAttribute;

    public Fuente(String message, Fachada anAttribute) {
        super(message);
        this.anAttribute = anAttribute;
    }
}
