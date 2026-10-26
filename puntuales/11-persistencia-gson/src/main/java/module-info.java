/**
 * Modulo del taller de instrumentos.
 *
 * requires: Gson es un modulo con nombre (com.google.gson).
 * opens:    Gson lee y escribe los atributos privados del modelo por reflexion.
 *           Sin esta linea, el modelo compila pero Gson falla al guardar o cargar.
 * jdk.unsupported: el modelo no tiene constructor sin argumentos, asi que Gson crea
 *           los objetos con sun.misc.Unsafe. Gson lo declara como "requires static"
 *           (opcional): en module path nadie lo agrega si no se pide aqui, y cargar
 *           falla con "Unable to create instance of class ...".
 */
module cl.dsy1102.ejemplos.instrumentos {
    requires com.google.gson;
    requires jdk.unsupported;

    opens cl.dsy1102.ejemplos.instrumentos.model to com.google.gson;

    exports cl.dsy1102.ejemplos.instrumentos;
}
