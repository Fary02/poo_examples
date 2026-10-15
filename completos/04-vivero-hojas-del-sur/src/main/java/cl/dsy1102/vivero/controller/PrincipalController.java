package cl.dsy1102.vivero.controller;

import cl.dsy1102.vivero.dao.PersistenciaException;
import cl.dsy1102.vivero.model.Planta;
import cl.dsy1102.vivero.repository.Repository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Ventana principal: tabla de plantas con busqueda en tiempo real, acciones
 * Agregar y Eliminar y un area de mensajes dentro de la misma ventana.
 *
 * Ningun mensaje se muestra por consola ni en un Alert: cada resultado
 * reemplaza al anterior en lblMensaje.
 */
public class PrincipalController {

    private static final String VISTA_FORMULARIO = "/cl/dsy1102/vivero/view/formulario-view.fxml";

    @FXML private TextField txtBuscar;
    @FXML private TableView<Planta> tblPlantas;
    @FXML private TableColumn<Planta, String> colTipo;
    @FXML private TableColumn<Planta, String> colNombre;
    @FXML private TableColumn<Planta, Integer> colAltura;
    @FXML private TableColumn<Planta, Integer> colCantidad;
    @FXML private TableColumn<Planta, Integer> colPrecio;
    @FXML private Label lblMensaje;

    private Repository<Planta> repositorio;

    /** Lo llama FXMLLoader al terminar de inyectar los @FXML. */
    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAltura.setCellValueFactory(new PropertyValueFactory<>("alturaCm"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        // Valores que no son atributos del modelo: se calculan con un lambda.
        colTipo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerTipo()));
        // El precio se guarda como numero (para ordenar bien) y solo se formatea al dibujar la celda.
        colPrecio.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().calcularPrecio()));
        colPrecio.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer precio, boolean vacia) {
                super.updateItem(precio, vacia);
                setText(vacia || precio == null ? null : Planta.pesos(precio));
            }
        });
        lblMensaje.setText("");
    }

    /** Recibe el repositorio desde AppFX y enlaza la tabla a su lista observable. */
    public void inicializar(Repository<Planta> repositorio) {
        this.repositorio = repositorio;

        // El filtro solo cambia lo que se VE: la lista del repositorio no se modifica.
        FilteredList<Planta> filtradas = new FilteredList<>(repositorio.listar(), planta -> true);
        txtBuscar.textProperty().addListener((obs, antes, texto) -> {
            String buscado = texto == null ? "" : texto.strip().toLowerCase();
            filtradas.setPredicate(planta -> buscado.isEmpty()
                    || planta.getNombre().toLowerCase().contains(buscado));
        });

        // SortedList mantiene el orden por columna sobre los datos ya filtrados.
        SortedList<Planta> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tblPlantas.comparatorProperty());
        tblPlantas.setItems(ordenadas);
    }

    @FXML
    private void onAgregar() {
        Planta nueva = pedirPlanta();
        if (nueva == null) {
            return;   // Cancelar: no se registra nada
        }
        try {
            repositorio.agregar(nueva);
            mostrarMensaje(nueva.getNombre() + " (" + nueva.obtenerTipo() + ") registrada correctamente.", true);
        } catch (PersistenciaException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    @FXML
    private void onEliminar() {
        Planta seleccionada = tblPlantas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje("Selecciona una planta de la tabla para eliminarla.", false);
            return;
        }
        try {
            repositorio.eliminar(seleccionada);
            mostrarMensaje(seleccionada.getNombre() + " (" + seleccionada.obtenerTipo() + ") eliminada.", true);
        } catch (PersistenciaException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    /**
     * Muestra un mensaje en la ventana principal; reemplaza al anterior.
     *
     * @param correcto true para el estilo de exito, false para el de error o aviso
     */
    public void mostrarMensaje(String texto, boolean correcto) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("resultado-ok", "resultado-error");
        lblMensaje.getStyleClass().add(correcto ? "resultado-ok" : "resultado-error");
    }

    /**
     * Abre el formulario en una ventana modal: mientras este abierta no se
     * puede usar la principal. showAndWait() detiene este metodo hasta que
     * la ventana se cierra.
     *
     * @return la planta confirmada, o null si el usuario cancelo
     */
    private Planta pedirPlanta() {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource(VISTA_FORMULARIO));
            Parent raiz = cargador.load();
            FormularioController formulario = cargador.getController();

            Stage ventana = new Stage();
            ventana.initOwner(tblPlantas.getScene().getWindow());
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setTitle("Registrar planta");
            ventana.setScene(new Scene(raiz));
            ventana.showAndWait();

            return formulario.getResultado();
        } catch (IOException e) {
            mostrarMensaje("No se pudo abrir el formulario de registro.", false);
            return null;
        }
    }
}
