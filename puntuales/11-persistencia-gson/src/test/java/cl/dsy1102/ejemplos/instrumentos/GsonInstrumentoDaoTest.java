package cl.dsy1102.ejemplos.instrumentos;

import cl.dsy1102.ejemplos.instrumentos.dao.GsonInstrumentoDao;
import cl.dsy1102.ejemplos.instrumentos.dao.PersistenciaException;
import cl.dsy1102.ejemplos.instrumentos.model.InstrumentoCuerda;
import cl.dsy1102.ejemplos.instrumentos.model.InstrumentoViento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de autoevaluacion. Compilan desde el inicio, pero fallan hasta
 * completar GsonInstrumentoDao. Ejecuta con: mvn test
 *
 * @TempDir entrega una carpeta temporal nueva para cada prueba: nunca se toca data/.
 */
class GsonInstrumentoDaoTest {

    @TempDir
    Path carpeta;

    private GsonInstrumentoDao<InstrumentoCuerda> daoCuerdas(String nombreArchivo) {
        return new GsonInstrumentoDao<>(carpeta.resolve(nombreArchivo), InstrumentoCuerda.class);
    }

    @Test
    void archivoInexistenteRetornaListaVacia() throws PersistenciaException {
        assertTrue(daoCuerdas("no-existe.json").cargar().isEmpty());
    }

    @Test
    void archivoVacioRetornaListaVacia() throws Exception {
        Files.writeString(carpeta.resolve("vacio.json"), "");
        assertTrue(daoCuerdas("vacio.json").cargar().isEmpty());
    }

    @Test
    void guardarYCargarConservaLosDatos() throws PersistenciaException {
        daoCuerdas("cuerdas.json").guardar(List.of(
                new InstrumentoCuerda("Guitarra clasica", 120_000, 6, false),
                new InstrumentoCuerda("Bajo Fender", 450_000, 4, true)));

        List<InstrumentoCuerda> cargadas = daoCuerdas("cuerdas.json").cargar();
        assertEquals(2, cargadas.size());
        assertEquals("Guitarra clasica", cargadas.get(0).getNombre());
        assertEquals(6, cargadas.get(0).getNumeroCuerdas());
        assertTrue(cargadas.get(1).isElectrico());
    }

    @Test
    void guardarCreaLaCarpetaYUsaFormatoLegible() throws Exception {
        Path archivo = carpeta.resolve("data").resolve("vientos.json");
        new GsonInstrumentoDao<>(archivo, InstrumentoViento.class)
                .guardar(List.of(new InstrumentoViento("Flauta traversa", 280_000, "Metal", "Do")));

        assertTrue(Files.exists(archivo));
        String contenido = Files.readString(archivo);
        assertTrue(contenido.contains("\n"), "pretty print: el JSON debe tener saltos de linea");
        assertTrue(contenido.contains("\"material\""));
        assertFalse(contenido.contains("numeroCuerdas"), "un archivo solo guarda su subtipo");
    }

    @Test
    void jsonDanadoLanzaPersistenciaExceptionConCausa() throws Exception {
        Files.writeString(carpeta.resolve("danado.json"), "[ { \"nombre\" : ");
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> daoCuerdas("danado.json").cargar());
        assertNotNull(e.getCause());
    }

    @Test
    void datoQueElModeloRechazaLanzaPersistenciaException() throws Exception {
        Files.writeString(carpeta.resolve("invalido.json"),
                "[ { \"numeroCuerdas\" : 99, \"electrico\" : false, \"nombre\" : \"Arpa\", \"precio\" : 900000 } ]");
        assertThrows(PersistenciaException.class, () -> daoCuerdas("invalido.json").cargar());
    }

    @Test
    void elementoNuloEnLaListaLanzaPersistenciaException() throws Exception {
        Files.writeString(carpeta.resolve("nulo.json"), "[ null ]");
        assertThrows(PersistenciaException.class, () -> daoCuerdas("nulo.json").cargar());
    }
}
