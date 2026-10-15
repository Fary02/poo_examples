package cl.dsy1102.vivero.controller;

import cl.dsy1102.vivero.model.Planta;
import cl.dsy1102.vivero.model.PlantaExterior;
import cl.dsy1102.vivero.model.PlantaInterior;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de registro, abierto como ventana modal desde la principal.
 *
 * Valida los datos antes de crear la planta. Si hay errores, los muestra en
 * el propio formulario (campo y motivo), marca los campos y CONSERVA los
 * valores ingresados para que se puedan corregir. No usa Alert ni consola.
 */
public class FormularioController {

    private static final String INTERIOR = "Interior";
    private static final String EXTERIOR = "Exterior";
    private static final String CLASE_ERROR = "campo-error";

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtAltura;
    @FXML private TextField txtCantidad;
    @FXML private VBox boxInterior;
    @FXML private CheckBox chkMaceta;
    @FXML private CheckBox chkLuzIndirecta;
    @FXML private VBox boxExterior;
    @FXML private TextField txtEspecie;
    @FXML private CheckBox chkHeladas;
    @FXML private Label lblError;

    private Planta resultado;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(INTERIOR, EXTERIOR);
        // Solo se muestran los campos del tipo elegido; managed evita que ocupen espacio ocultos.
        boxInterior.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(INTERIOR));
        boxInterior.managedProperty().bind(boxInterior.visibleProperty());
        boxExterior.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(EXTERIOR));
        boxExterior.managedProperty().bind(boxExterior.visibleProperty());
        // El area de errores solo ocupa espacio cuando tiene un mensaje.
        lblError.setText("");
        lblError.visibleProperty().bind(lblError.textProperty().isNotEmpty());
        lblError.managedProperty().bind(lblError.visibleProperty());
    }

    /** @return la planta confirmada, o null si se cancelo (o aun no se confirma) */
    public Planta getResultado() {
        return resultado;
    }

    @FXML
    private void onConfirmar() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            marcar(cmbTipo, "Tipo: selecciona Interior o Exterior.", errores);
        }
        String nombre = leerTexto(txtNombre, "Nombre", errores);
        Integer altura = leerEntero(txtAltura, "Altura", Planta.ALTURA_MINIMA, Planta.ALTURA_MAXIMA, errores);
        Integer cantidad = leerEntero(txtCantidad, "Cantidad", Planta.CANTIDAD_MINIMA, Planta.CANTIDAD_MAXIMA, errores);
        String especie = EXTERIOR.equals(tipo) ? leerTexto(txtEspecie, "Especie", errores) : null;

        if (!errores.isEmpty()) {
            // No se cierra el formulario ni se borran los campos: el usuario corrige y reintenta.
            lblError.setText(String.join("\n", errores));
            return;
        }

        try {
            resultado = INTERIOR.equals(tipo)
                    ? new PlantaInterior(nombre, altura, cantidad, chkMaceta.isSelected(), chkLuzIndirecta.isSelected())
                    : new PlantaExterior(nombre, altura, cantidad, especie, chkHeladas.isSelected());
            cerrar();
        } catch (IllegalArgumentException e) {
            // Red de seguridad: el modelo tiene la ultima palabra sobre las reglas.
            resultado = null;
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        resultado = null;
        cerrar();
    }

    private void cerrar() {
        ((Stage) lblError.getScene().getWindow()).close();
    }

    private String leerTexto(TextField campo, String nombreCampo, List<String> errores) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            marcar(campo, nombreCampo + ": no puede estar vacío.", errores);
            return null;
        }
        return texto;
    }

    private Integer leerEntero(TextField campo, String nombreCampo, int minimo, int maximo, List<String> errores) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        String regla = nombreCampo + ": debe ser un número entero entre " + minimo + " y " + maximo + ".";
        if (texto.isEmpty()) {
            marcar(campo, nombreCampo + ": no puede estar vacío.", errores);
            return null;
        }
        try {
            int valor = Integer.parseInt(texto);
            if (valor < minimo || valor > maximo) {
                marcar(campo, regla, errores);
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            marcar(campo, regla, errores);
            return null;
        }
    }

    private void marcar(Control campo, String mensaje, List<String> errores) {
        errores.add(mensaje);
        campo.getStyleClass().add(CLASE_ERROR);
    }

    private void limpiarErrores() {
        lblError.setText("");
        List.of(cmbTipo, txtNombre, txtAltura, txtCantidad, txtEspecie)
                .forEach(control -> control.getStyleClass().remove(CLASE_ERROR));
    }
}
