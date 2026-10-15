package cl.dsy1102.ejemplos.instrumentos;

import cl.dsy1102.ejemplos.instrumentos.dao.GsonInstrumentoDao;
import cl.dsy1102.ejemplos.instrumentos.dao.InstrumentoDao;
import cl.dsy1102.ejemplos.instrumentos.dao.PersistenciaException;
import cl.dsy1102.ejemplos.instrumentos.model.Instrumento;
import cl.dsy1102.ejemplos.instrumentos.model.InstrumentoCuerda;
import cl.dsy1102.ejemplos.instrumentos.model.InstrumentoViento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Demostracion de la persistencia. Ejecuta con: mvn compile exec:java
 * Ya esta resuelta: el trabajo esta en GsonInstrumentoDao (R1 a R4).
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path archivoCuerdas = Path.of("data", "cuerdas.json");
        Path archivoVientos = Path.of("data", "vientos.json");
        Files.deleteIfExists(archivoCuerdas);   // la demo siempre parte de cero
        Files.deleteIfExists(archivoVientos);

        InstrumentoDao<InstrumentoCuerda> cuerdas = new GsonInstrumentoDao<>(archivoCuerdas, InstrumentoCuerda.class);
        InstrumentoDao<InstrumentoViento> vientos = new GsonInstrumentoDao<>(archivoVientos, InstrumentoViento.class);

        System.out.println("== 1. Primera ejecucion (los archivos no existen)");
        mostrar(cuerdas);
        mostrar(vientos);

        System.out.println("\n== 2. Guardar dos cuerdas y un viento (un archivo por subtipo)");
        try {
            cuerdas.guardar(List.of(
                    new InstrumentoCuerda("Guitarra clasica", 120_000, 6, false),
                    new InstrumentoCuerda("Bajo Fender", 450_000, 4, true)));
            vientos.guardar(List.of(new InstrumentoViento("Flauta traversa", 280_000, "Metal", "Do")));
            System.out.println("Guardado en " + archivoCuerdas.toAbsolutePath());
            System.out.println(Files.readString(archivoCuerdas));
        } catch (PersistenciaException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        System.out.println("\n== 3. Cargar con DAO nuevos (como si se reiniciara el programa)");
        mostrar(new GsonInstrumentoDao<>(archivoCuerdas, InstrumentoCuerda.class));
        mostrar(new GsonInstrumentoDao<>(archivoVientos, InstrumentoViento.class));

        System.out.println("\n== 4. Archivo vacio");
        Path vacio = Path.of("data", "vacio.json");
        Files.writeString(vacio, "");
        mostrar(new GsonInstrumentoDao<>(vacio, InstrumentoCuerda.class));

        System.out.println("\n== 5. Archivo danado");
        Path danado = Path.of("data", "danado.json");
        Files.writeString(danado, "[ { \"numeroCuerdas\" : 6, \"nombre\" : ");
        mostrar(new GsonInstrumentoDao<>(danado, InstrumentoCuerda.class));

        System.out.println("\n== 6. Archivo con un dato que el modelo rechaza");
        Path invalido = Path.of("data", "invalido.json");
        Files.writeString(invalido, """
                [ { "numeroCuerdas" : 99, "electrico" : false, "nombre" : "Arpa gigante", "precio" : 900000 } ]""");
        mostrar(new GsonInstrumentoDao<>(invalido, InstrumentoCuerda.class));
    }

    private static <T extends Instrumento> void mostrar(InstrumentoDao<T> dao) {
        try {
            List<T> instrumentos = dao.cargar();
            System.out.println(instrumentos.size() + " instrumento(s)");
            for (T instrumento : instrumentos) {
                System.out.println("  " + instrumento.obtenerDetalle());
            }
        } catch (PersistenciaException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("  causa: " + e.getCause().getClass().getSimpleName());
        }
    }
}
