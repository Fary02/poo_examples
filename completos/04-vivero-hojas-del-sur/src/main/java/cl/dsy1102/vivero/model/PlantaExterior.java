package cl.dsy1102.vivero.model;

/**
 * Planta de exterior. Precio base de $5.500; si resiste las heladas, el
 * precio sube un 20 %. La especie es obligatoria.
 */
public class PlantaExterior extends Planta {

    public static final int PRECIO_BASE = 5_500;
    public static final int RECARGO_HELADAS = 20;

    private String especie;
    private boolean resistenteHeladas;

    public PlantaExterior(String nombre, int alturaCm, int cantidad, String especie, boolean resistenteHeladas) {
        super(nombre, alturaCm, cantidad);
        setEspecie(especie);
        this.resistenteHeladas = resistenteHeladas;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = validarTexto(especie, "La especie no puede estar vacía.");
    }

    public boolean isResistenteHeladas() {
        return resistenteHeladas;
    }

    public void setResistenteHeladas(boolean resistenteHeladas) {
        this.resistenteHeladas = resistenteHeladas;
    }

    @Override
    public void validar() {
        super.validar();
        validarTexto(especie, "La especie no puede estar vacía.");
    }

    @Override
    public int calcularPrecio() {
        return resistenteHeladas ? PRECIO_BASE + PRECIO_BASE * RECARGO_HELADAS / 100 : PRECIO_BASE;
    }

    @Override
    public String obtenerTipo() {
        return "Exterior";
    }
}
