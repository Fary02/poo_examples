package cl.dsy1102.ejemplos.instrumentos.dao;

import cl.dsy1102.ejemplos.instrumentos.model.Instrumento;

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

    // TODO R1: guardar tambien el Type de List<T> (TypeToken.getParameterized(List.class, clase).getType())
    //          y un Gson con formato legible (GsonBuilder().setPrettyPrinting().create()).
    public GsonInstrumentoDao(Path archivo, Class<T> clase) {
        this.archivo = archivo;
    }

    /**
     * TODO R2:
     *  - Si el archivo no existe o esta vacio: lista vacia (OJO: fromJson retorna null con un archivo vacio).
     *  - Si no: gson.fromJson(lector, tipoLista), leyendo el archivo como UTF-8.
     *  - Llamar a validar() en cada instrumento leido (Gson no usa los setters del modelo).
     *  - IOException, JsonParseException o IllegalArgumentException
     *    -> PersistenciaException con un mensaje comprensible y la excepcion original como causa.
     */
    @Override
    public List<T> cargar() throws PersistenciaException {
        return new ArrayList<>();
    }

    /**
     * TODO R3:
     *  - Crea la carpeta del archivo si no existe.
     *  - Escribe en UTF-8 con gson.toJson(instrumentos, tipoLista, escritor).
     *  - IOException o JsonParseException -> PersistenciaException.
     */
    @Override
    public void guardar(List<T> instrumentos) throws PersistenciaException {
    }
}
