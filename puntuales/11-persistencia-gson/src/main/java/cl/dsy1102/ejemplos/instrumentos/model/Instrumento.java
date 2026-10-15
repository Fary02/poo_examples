package cl.dsy1102.ejemplos.instrumentos.model;

/**
 * Instrumento del taller. Clase abstracta: se guarda siempre como uno de sus
 * subtipos (cada subtipo tiene su propio archivo JSON).
 *
 * Reglas comunes: nombre no vacio y precio entre $1.000 y $5.000.000.
 */
public abstract class Instrumento {

    public static final int PRECIO_MINIMO = 1_000;
    public static final int PRECIO_MAXIMO = 5_000_000;

    private String nombre;
    private int precio;

    protected Instrumento(String nombre, int precio) {
        setNombre(nombre);
        setPrecio(precio);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarNombre(nombre);
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = validarPrecio(precio);
    }

    /**
     * Vuelve a comprobar todas las reglas sobre los datos actuales.
     * Gson llena los atributos directamente y NO pasa por los constructores ni
     * por los setters, asi que el DAO llama a este metodo despues de leer.
     *
     * @throws IllegalArgumentException si algun dato incumple una regla
     */
    public void validar() {
        validarNombre(nombre);
        validarPrecio(precio);
    }

    public abstract String obtenerTipo();

    public abstract String obtenerDetalle();

    protected static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        return nombre.trim();
    }

    protected static int validarPrecio(int precio) {
        if (precio < PRECIO_MINIMO || precio > PRECIO_MAXIMO) {
            throw new IllegalArgumentException("El precio debe estar entre " + pesos(PRECIO_MINIMO)
                    + " y " + pesos(PRECIO_MAXIMO) + ".");
        }
        return precio;
    }

    public static String pesos(int monto) {
        return String.format("$%,d", monto).replace(',', '.');
    }

    @Override
    public String toString() {
        return obtenerDetalle();
    }
}
