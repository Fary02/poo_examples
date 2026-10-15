package cl.dsy1102.vivero.dao;

/**
 * Error al leer o escribir los datos. Los controladores solo conocen esta
 * excepcion: nunca la IOException del archivo ni las excepciones de Gson.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
