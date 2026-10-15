package cl.dsy1102.vivero.dao;

import cl.dsy1102.vivero.model.Planta;
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
 * Guarda las plantas de un tipo en un archivo JSON con Gson.
 *
 * Es la unica clase, junto a AppFX, que conoce el formato y la ubicacion de
 * los archivos. Un objeto de esta clase por tipo de planta: como cada archivo
 * contiene una sola clase, Gson no necesita informacion de subtipos.
 */
public class GsonPlantaDao<T extends Planta> implements PlantaDao<T> {

    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GsonPlantaDao(Path archivo, Class<T> clase) {
        this.archivo = archivo;
        // List<T> pierde el tipo generico al compilar (type erasure): TypeToken lo reconstruye.
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
    }

    @Override
    public List<T> cargar() throws PersistenciaException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();   // primera ejecucion: no es un error
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> plantas = gson.fromJson(lector, tipoLista);
            if (plantas == null) {
                return new ArrayList<>();   // fromJson retorna null con un archivo vacio
            }
            for (T planta : plantas) {
                if (planta == null) {
                    throw new IllegalArgumentException("el archivo contiene un elemento nulo");
                }
                planta.validar();   // Gson no pasa por los setters del modelo
            }
            return new ArrayList<>(plantas);
        } catch (IOException | JsonParseException | IllegalArgumentException e) {
            throw new PersistenciaException("No se pudo leer " + archivo.getFileName()
                    + ": el archivo está dañado o tiene datos inválidos.", e);
        }
    }

    @Override
    public void guardar(List<T> plantas) throws PersistenciaException {
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);   // no falla si ya existe
            }
            try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                gson.toJson(plantas, tipoLista, escritor);
            }
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("No se pudo guardar " + archivo.getFileName()
                    + ". Revisa los permisos y el espacio en disco.", e);
        }
    }
}
