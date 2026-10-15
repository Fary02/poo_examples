package cl.dsy1102.ejemplos.instrumentos.dao;

import cl.dsy1102.ejemplos.instrumentos.model.Instrumento;

import java.util.List;

/** Acceso a los datos de UN subtipo de instrumento (un archivo por subtipo). */
public interface InstrumentoDao<T extends Instrumento> {

    /** @return los instrumentos guardados; lista vacia si el archivo no existe o esta vacio */
    List<T> cargar() throws PersistenciaException;

    /** Reemplaza el contenido del archivo por la lista recibida. */
    void guardar(List<T> instrumentos) throws PersistenciaException;
}
