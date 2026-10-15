package cl.dsy1102.vivero.model;

/**
 * Planta de interior. Precio base de $8.000; si se vende con maceta
 * decorativa, el precio sube un 35 %.
 */
public class PlantaInterior extends Planta {

    public static final int PRECIO_BASE = 8_000;
    public static final int RECARGO_MACETA = 35;

    private boolean conMaceta;
    private boolean luzIndirecta;

    public PlantaInterior(String nombre, int alturaCm, int cantidad, boolean conMaceta, boolean luzIndirecta) {
        super(nombre, alturaCm, cantidad);
        this.conMaceta = conMaceta;
        this.luzIndirecta = luzIndirecta;
    }

    public boolean isConMaceta() {
        return conMaceta;
    }

    public void setConMaceta(boolean conMaceta) {
        this.conMaceta = conMaceta;
    }

    public boolean isLuzIndirecta() {
        return luzIndirecta;
    }

    public void setLuzIndirecta(boolean luzIndirecta) {
        this.luzIndirecta = luzIndirecta;
    }

    @Override
    public int calcularPrecio() {
        return conMaceta ? PRECIO_BASE + PRECIO_BASE * RECARGO_MACETA / 100 : PRECIO_BASE;
    }

    @Override
    public String obtenerTipo() {
        return "Interior";
    }
}
