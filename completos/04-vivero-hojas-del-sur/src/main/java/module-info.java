/**
 * Modulo de la aplicacion Vivero Hojas del Sur.
 *
 * requires: modulos externos que usa el proyecto.
 * opens:    paquetes que otras bibliotecas leen por reflexion
 *           (FXML inyecta los @FXML de los controladores; Gson lee y escribe
 *           los atributos del modelo; la tabla lee sus getters con
 *           PropertyValueFactory).
 * exports:  paquete de la clase Application, que JavaFX debe poder instanciar.
 *
 * jdk.unsupported: el modelo no tiene constructor sin argumentos, asi que Gson crea
 *           los objetos con sun.misc.Unsafe. Hoy llega igual a traves de JavaFX
 *           (javafx.graphics lo requiere), pero se declara para no depender de eso.
 */
module cl.dsy1102.vivero {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires jdk.unsupported;

    opens cl.dsy1102.vivero.controller to javafx.fxml;
    opens cl.dsy1102.vivero.model to com.google.gson, javafx.base;

    exports cl.dsy1102.vivero;
}
