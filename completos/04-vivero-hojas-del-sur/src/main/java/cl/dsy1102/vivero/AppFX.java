package cl.dsy1102.vivero;

import cl.dsy1102.vivero.controller.PrincipalController;
import cl.dsy1102.vivero.dao.GsonPlantaDao;
import cl.dsy1102.vivero.dao.PersistenciaException;
import cl.dsy1102.vivero.model.Planta;
import cl.dsy1102.vivero.model.PlantaExterior;
import cl.dsy1102.vivero.model.PlantaInterior;
import cl.dsy1102.vivero.repository.PlantaRepository;
import cl.dsy1102.vivero.repository.Repository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicacion grafica. Ejecuta con: mvn javafx:run
 *
 * Es el unico lugar, junto a los DAO, que conoce la ubicacion de los archivos.
 */
public class AppFX extends Application {

    private static final Path ARCHIVO_INTERIORES = Path.of("data", "interiores.json");
    private static final Path ARCHIVO_EXTERIORES = Path.of("data", "exteriores.json");

    @Override
    public void init() {
        System.out.println("[Ciclo de vida] init()  - hilo: " + Thread.currentThread().getName());
    }

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("[Ciclo de vida] start() - hilo: " + Thread.currentThread().getName());

        // Se arman las capas: el controlador recibira solo la interfaz Repository.
        Repository<Planta> repositorio = new PlantaRepository(
                new GsonPlantaDao<>(ARCHIVO_INTERIORES, PlantaInterior.class),
                new GsonPlantaDao<>(ARCHIVO_EXTERIORES, PlantaExterior.class));

        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/cl/dsy1102/vivero/view/principal-view.fxml"));
        Parent raiz = cargador.load();
        PrincipalController principal = cargador.getController();
        principal.inicializar(repositorio);

        stage.setTitle("Vivero Hojas del Sur");
        stage.setScene(new Scene(raiz, 900, 560));
        stage.setMinWidth(720);
        stage.setMinHeight(420);
        stage.show();

        // Si los archivos no existen la lista queda vacia sin error; si estan danados se avisa en la ventana.
        try {
            repositorio.cargar();
        } catch (PersistenciaException e) {
            principal.mostrarMensaje(e.getMessage() + " La aplicación inicia sin plantas.", false);
        }
    }

    @Override
    public void stop() {
        System.out.println("[Ciclo de vida] stop()  - hilo: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
