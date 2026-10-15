package cl.dsy1102.vivero.dao;

import cl.dsy1102.vivero.model.Planta;

import java.util.List;

/** Acceso a los datos de UN tipo de planta (un archivo por tipo). */
public interface PlantaDao<T extends Planta> {

    /** @return las plantas guardadas; lista vacia si el archivo no existe o esta vacio */
    List<T> cargar() throws PersistenciaException;

    /** Reemplaza el contenido del archivo por la lista recibida. */
    void guardar(List<T> plantas) throws PersistenciaException;
}
