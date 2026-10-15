/**
 * Modulo del taller de instrumentos.
 *
 * requires: Gson es un modulo con nombre (com.google.gson).
 * opens:    Gson lee y escribe los atributos privados del modelo por reflexion.
 *           Sin esta linea, el modelo compila pero Gson falla al guardar o cargar.
 */
module cl.dsy1102.ejemplos.instrumentos {
    requires com.google.gson;

    opens cl.dsy1102.ejemplos.instrumentos.model to com.google.gson;

    exports cl.dsy1102.ejemplos.instrumentos;
}
