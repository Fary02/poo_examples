package cl.dsy1102.vivero.repository;

import cl.dsy1102.vivero.dao.PersistenciaException;
import javafx.collections.ObservableList;

/**
 * Operaciones genericas que usan los controladores. La lista que retorna
 * listar() se enlaza a la vista y refleja cada cambio.
 */
public interface Repository<T> {

    void cargar() throws PersistenciaException;

    ObservableList<T> listar();

    void agregar(T elemento) throws PersistenciaException;

    void eliminar(T elemento) throws PersistenciaException;
}
