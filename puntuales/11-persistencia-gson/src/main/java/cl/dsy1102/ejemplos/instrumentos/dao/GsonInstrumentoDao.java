package cl.dsy1102.ejemplos.instrumentos.dao;

import cl.dsy1102.ejemplos.instrumentos.model.Instrumento;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los instrumentos de un subtipo en un archivo JSON con Gson.
 *
 * Un DAO por subtipo: como el archivo solo contiene instrumentos de una clase,
 * Gson no necesita saber "de que clase" es cada objeto.
 */
public class GsonInstrumentoDao<T extends Instrumento> implements InstrumentoDao<T> {

    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GsonInstrumentoDao(Path archivo, Class<T> clase) {
        this.archivo = archivo;
        // List<T> pierde el tipo generico al compilar (type erasure): TypeToken lo reconstruye.
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
    }

    @Override
    public List<T> cargar() throws PersistenciaException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> instrumentos = gson.fromJson(lector, tipoLista);
            if (instrumentos == null) {
                // Gson retorna null (no una excepcion) con un archivo vacio o con el texto "null".
                return new ArrayList<>();
            }
            for (T instrumento : instrumentos) {
                if (instrumento == null) {
                    throw new IllegalArgumentException("el archivo contiene un elemento nulo");
                }
                instrumento.validar();   // Gson no pasa por los setters: se valida a mano
            }
            return new ArrayList<>(instrumentos);
        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            // JsonParseException: JSON mal formado o con tipos distintos a los del modelo (RuntimeException).
            // IllegalArgumentException: validar() rechazo un valor del archivo.
            throw new PersistenciaException("No se pudieron leer los instrumentos de " + archivo.getFileName()
                    + ": el archivo esta danado o tiene datos invalidos.", e);
        }
    }

    @Override
    public void guardar(List<T> instrumentos) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);   // no falla si ya existe
            }
            try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                gson.toJson(instrumentos, tipoLista, escritor);
            }
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("No se pudieron guardar los instrumentos en " + archivo.getFileName()
                    + ". Revisa permisos y espacio en disco.", e);
        }
    }
}
