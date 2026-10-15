package cl.dsy1102.ejemplos.instrumentos.model;

/** Instrumento de cuerda: tiene entre 4 y 12 cuerdas y puede ser electrico. */
public class InstrumentoCuerda extends Instrumento {

    private int numeroCuerdas;
    private boolean electrico;

    public InstrumentoCuerda(String nombre, int precio, int numeroCuerdas, boolean electrico) {
        super(nombre, precio);
        setNumeroCuerdas(numeroCuerdas);
        this.electrico = electrico;
    }

    public int getNumeroCuerdas() {
        return numeroCuerdas;
    }

    public void setNumeroCuerdas(int numeroCuerdas) {
        this.numeroCuerdas = validarCuerdas(numeroCuerdas);
    }

    public boolean isElectrico() {
        return electrico;
    }

    public void setElectrico(boolean electrico) {
        this.electrico = electrico;
    }

    @Override
    public void validar() {
        super.validar();
        validarCuerdas(numeroCuerdas);
    }

    @Override
    public String obtenerTipo() {
        return "Cuerda";
    }

    @Override
    public String obtenerDetalle() {
        return "[Cuerda] " + getNombre() + " | " + pesos(getPrecio()) + " | " + numeroCuerdas + " cuerdas"
                + (electrico ? " | electrico" : "");
    }

    private static int validarCuerdas(int numeroCuerdas) {
        if (numeroCuerdas < 4 || numeroCuerdas > 12) {
            throw new IllegalArgumentException("El numero de cuerdas debe estar entre 4 y 12.");
        }
        return numeroCuerdas;
    }
}
