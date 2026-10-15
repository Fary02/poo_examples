package cl.dsy1102.ejemplos.instrumentos.dao;

/** Error al leer o escribir los datos. Quien usa el DAO nunca ve la IOException ni las excepciones de Gson. */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
