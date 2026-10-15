package cl.dsy1102.ejemplos.instrumentos.model;

/** Instrumento de viento: material y afinacion obligatorios (por ejemplo "Madera" y "Sib"). */
public class InstrumentoViento extends Instrumento {

    private String material;
    private String afinacion;

    public InstrumentoViento(String nombre, int precio, String material, String afinacion) {
        super(nombre, precio);
        setMaterial(material);
        setAfinacion(afinacion);
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = validarTexto(material, "El material no puede estar vacio.");
    }

    public String getAfinacion() {
        return afinacion;
    }

    public void setAfinacion(String afinacion) {
        this.afinacion = validarTexto(afinacion, "La afinacion no puede estar vacia.");
    }

    @Override
    public void validar() {
        super.validar();
        validarTexto(material, "El material no puede estar vacio.");
        validarTexto(afinacion, "La afinacion no puede estar vacia.");
    }

    @Override
    public String obtenerTipo() {
        return "Viento";
    }

    @Override
    public String obtenerDetalle() {
        return "[Viento] " + getNombre() + " | " + pesos(getPrecio()) + " | " + material + " | afinacion " + afinacion;
    }

    private static String validarTexto(String texto, String mensajeError) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return texto.trim();
    }
}
