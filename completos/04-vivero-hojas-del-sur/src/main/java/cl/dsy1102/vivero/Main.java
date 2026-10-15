package cl.dsy1102.vivero;

import cl.dsy1102.vivero.model.Planta;
import cl.dsy1102.vivero.model.PlantaExterior;
import cl.dsy1102.vivero.model.PlantaInterior;

import java.util.List;

/**
 * Demostracion del modelo por consola (ya resuelto).
 * Ejecuta con: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        List<Planta> plantas = List.of(
                new PlantaInterior("Helecho", 40, 12, true, true),
                new PlantaInterior("Cactus", 15, 30, false, false),
                new PlantaExterior("Lavanda", 60, 25, "Lavandula angustifolia", true),
                new PlantaExterior("Rosal", 90, 8, "Rosa x hybrida", false));

        for (Planta planta : plantas) {
            System.out.println(planta);
        }

        System.out.println();
        intentar("Altura 0", () -> new PlantaInterior("Fittonia", 0, 5, false, true));
        intentar("Nombre vacío", () -> new PlantaInterior("  ", 20, 5, false, true));
        intentar("Cantidad 501", () -> new PlantaExterior("Boldo", 120, 501, "Peumus boldus", true));
        intentar("Especie vacía", () -> new PlantaExterior("Boldo", 120, 5, "", true));
    }

    private static void intentar(String caso, Runnable creacion) {
        try {
            creacion.run();
            System.out.println(caso + ": aceptado");
        } catch (IllegalArgumentException e) {
            System.out.println(caso + ": rechazado -> " + e.getMessage());
        }
    }
}
