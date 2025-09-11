package ar.edi.itn.dds.k3003.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.Coleccion;

import lombok.val;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
/*
@ExtendWith({MockitoExtension.class})
public class FuenteTest {

    public static final String UNA_COL = "unaCol";
    public static final String DESCRIPCION = "1234556";
    Coleccion someDomainObject1;
    Fachada fuente;
    @Mock private FachadaProcesadorPdI fachadaProcesadorPdI;

    @BeforeEach
    void setUp() {
        fuente = new Fachada();
        fuente.setProcesadorPdI(this.fachadaProcesadorPdI);
    }

    @Test
    @DisplayName("Agregar coleccion")
    void testAgregarColeccion() {
        fuente.agregar(new ColeccionDTO(UNA_COL, DESCRIPCION));
        ColeccionDTO col = fuente.buscarColeccionXId(UNA_COL);

        assertEquals(UNA_COL, col.nombre());
    }

    @Test
    @DisplayName("Agregar coleccion repetida")
    void testRepatedColeccion() {
        fuente.agregar(new ColeccionDTO(UNA_COL, DESCRIPCION));
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    fuente.agregar(new ColeccionDTO(UNA_COL, "321"));
                });
    }

    @Test
    @DisplayName("Agregar hecho")
    void testAgregarHecho() {
        fuente.agregar(new ColeccionDTO("test1", "test1 primera entrega"));
        HechoDTO hechoDTO =
                new HechoDTO(
                        "",
                        "test1",
                        "unHecho",
                        List.of("etiqueta1"),
                        CategoriaHechoEnum.ENTRETENIMIENTO,
                        "bsas",
                        LocalDateTime.now(),
                        "celular");
        HechoDTO hecho1 = fuente.agregar(hechoDTO);
        Assertions.assertNotNull(hecho1.id(), "No se asigno un identificador al hecho agregado");
        HechoDTO hecho2 = fuente.buscarHechoXId(hecho1.id());
        Assertions.assertEquals(
                "unHecho", hecho2.titulo(), "Al buscar por id de hecho no se retorna el correcto.");
    }

    @Test
    @DisplayName("Agregar un pdi a un hecho")
    void testAgregarPdiAHecho() {
        Mockito.when(
                        this.fachadaProcesadorPdI.procesar(
                                (PdIDTO) ArgumentMatchers.any(PdIDTO.class)))
                .thenReturn(new PdIDTO("1", "unHecho"));
        fuente.agregar(new ColeccionDTO("unaColeccion", "coleccion"));
        HechoDTO hechoDTO =
                new HechoDTO(
                        "",
                        "unHecho",
                        "unaColeccion",
                        List.of(),
                        CategoriaHechoEnum.ENTRETENIMIENTO,
                        "bsas",
                        LocalDateTime.now(),
                        "celular");
        HechoDTO hecho1 = fuente.agregar(hechoDTO);
        fuente.agregar(new PdIDTO("unPdIId", hecho1.id()));
    }

    @Test
    @DisplayName("Agregar un pdi a un hecho")
    void testTratarDeAgregarHecho() {
        Mockito.when(
                        this.fachadaProcesadorPdI.procesar(
                                (PdIDTO) ArgumentMatchers.any(PdIDTO.class)))
                .thenThrow(IllegalStateException.class);
        fuente.agregar(new ColeccionDTO("unaColeccion2", "coleccion"));
        HechoDTO hechoDTO =
                new HechoDTO(
                        "",
                        "unHecho",
                        "unaColeccion2",
                        List.of(),
                        CategoriaHechoEnum.ENTRETENIMIENTO,
                        "bsas",
                        LocalDateTime.now(),
                        "celular");
        HechoDTO hecho1 = fuente.agregar(hechoDTO);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> fuente.agregar(new PdIDTO("unPdIId", hecho1.id())));
    }
}


 */