package cl.dsy1102.vivero;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion grafica.
 *
 * Revisa el enunciado en README.md. Ejecuta con: mvn javafx:run
 */
public class AppFX extends Application {

    @Override
    public void init() {
        // TODO R1: trazar el ciclo de vida imprimiendo por consola.
    }

    @Override
    public void start(Stage stage) {
        // TODO R5/R6: crear un DAO JSON por tipo de planta (interiores.json y exteriores.json),
        //             el repositorio, y cargar los datos. Si la carga falla, informar en la
        //             ventana principal y continuar con la tabla vacia.
        // TODO R2: cargar la vista principal (FXML), entregarle el repositorio a su
        //          controlador y mostrarla en el Stage recibido.
        stage.setTitle("Vivero Hojas del Sur");
        stage.show();
    }

    @Override
    public void stop() {
        // TODO R1: trazar el cierre de la aplicacion.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
