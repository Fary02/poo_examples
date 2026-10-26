# Guía de resolución · Caso completo 04 Vivero Hojas del Sur

> Para comparar tu trabajo con la solución: `git diff main solucion -- completos/04-vivero-hojas-del-sur`

## 0. Mapa de requisitos

| Requisito | Dónde se resuelve |
|---|---|
| R1 Proyecto y ciclo de vida | `module-info.java`, `AppFX` |
| R2 Ventana principal | `principal-view.fxml`, `PrincipalController` |
| R3 Ventana de registro (modal) | `formulario-view.fxml`, `FormularioController`, `PrincipalController.pedirPlanta()` |
| R4 Validaciones del formulario | `FormularioController.onConfirmar()` |
| R5 Persistencia con Gson | `GsonPlantaDao` |
| R6 Repository + DAO | `PlantaDao`, `Repository`, `PlantaRepository` |
| R7 Estabilidad | todas las capas |

Flujo de una acción de principio a fin:

```
Agregar ─► PrincipalController ─► ventana modal (FormularioController valida y crea la Planta)
                │                                   │ Confirmar
                │◄──────────── getResultado() ──────┘
                ▼
        PlantaRepository.agregar(planta)
                │  1. copia de la lista + la planta nueva
                │  2. dao.guardar(solo las del tipo afectado)  ──► data/interiores.json | exteriores.json
                │  3. si el guardado resultó: lista observable.add(...)  ──► la tabla se actualiza sola
                ▼
        mostrarMensaje("Helecho (Interior) registrada correctamente.")
```

## Paso 1 — Módulos y ciclo de vida (R1)

```java
module cl.dsy1102.vivero {
    requires javafx.controls;  requires javafx.fxml;  requires com.google.gson;
    requires jdk.unsupported;
    opens cl.dsy1102.vivero.controller to javafx.fxml;
    opens cl.dsy1102.vivero.model to com.google.gson, javafx.base;
    exports cl.dsy1102.vivero;
}
```

- `controller → javafx.fxml`: FXML inyecta los atributos `@FXML` privados.
- `model → com.google.gson`: Gson lee y escribe los atributos privados.
- `model → javafx.base`: `PropertyValueFactory("nombre")` busca `getNombre()` por reflexión.
- `jdk.unsupported`: el modelo no tiene constructor sin argumentos, así que Gson crea los objetos con `sun.misc.Unsafe`. En esta app llega igual por JavaFX, pero sin JavaFX (caso puntual 11) cargar falla con `Unable to create instance of class ...`.

Si falta cualquiera, el proyecto **compila** pero falla al ejecutar. Los síntomas están al final de esta guía.

## Paso 2 — DAO con Gson (R5)

Es el mismo patrón del caso puntual 11 (`GsonInstrumentoDao`): una clase genérica, un objeto por tipo de planta.

```java
this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
List<T> plantas = gson.fromJson(lector, tipoLista);
if (plantas == null) return new ArrayList<>();     // archivo vacío
plantas.forEach(Planta::validar);                    // Gson no usa los setters
```

- Dos objetos, dos archivos: `new GsonPlantaDao<>(ARCHIVO_INTERIORES, PlantaInterior.class)` y el equivalente para exteriores. Como cada archivo contiene una sola clase, Gson no necesita información de subtipos.
- `JsonParseException` (padre de `JsonSyntaxException` y `JsonIOException`) es una `RuntimeException`: hay que capturarla para traducirla a `PersistenciaException`.

## Paso 3 — Repository (R6)

`PlantaRepository` recibe los dos DAO y una `ObservableList<Planta>` con todas las plantas.

- `cargar()` junta los resultados de ambos DAO y los carga con `setAll`.
- `agregar` y `eliminar` trabajan **primero sobre una copia**, guardan y solo después modifican la lista observable. Si el guardado falla, la tabla queda como estaba.
- Se reescribe **solo el archivo del tipo afectado** (`instanceof`), así un cambio nunca deja a medias el archivo del otro tipo.

## Paso 4 — Ventana principal (R2)

```java
FilteredList<Planta> filtradas = new FilteredList<>(repositorio.listar(), p -> true);
txtBuscar.textProperty().addListener((obs, antes, texto) -> filtradas.setPredicate(...));
SortedList<Planta> ordenadas = new SortedList<>(filtradas);
ordenadas.comparatorProperty().bind(tblPlantas.comparatorProperty());
tblPlantas.setItems(ordenadas);
```

- `FilteredList` solo cambia lo que se **ve**; la lista del repositorio (y los archivos) no se tocan.
- `SortedList` conserva el ordenamiento por columna sobre los datos filtrados.
- La columna de precio es de `Integer` con un `cellFactory` que lo formatea: si fuera texto (`"$10.800"`), `$8.000` quedaría después de `$10.800` al ordenar.
- `colTipo` y `colPrecio` no son atributos: se calculan con `setCellValueFactory(celda -> ...)`.
- El área de mensajes es un `Label`. Cada llamada a `setText` reemplaza el mensaje anterior; el estilo (`resultado-ok` / `resultado-error`) se cambia con `getStyleClass()`.

## Paso 5 — Ventana modal (R3)

```java
Stage ventana = new Stage();
ventana.initOwner(tblPlantas.getScene().getWindow());
ventana.initModality(Modality.APPLICATION_MODAL);
ventana.setScene(new Scene(raiz));
ventana.showAndWait();
return formulario.getResultado();
```

- `initModality(APPLICATION_MODAL)` bloquea la ventana principal mientras esta esté abierta. `initOwner` hace que quede por encima de ella.
- `showAndWait()` **detiene** el método hasta que la ventana se cierra; por eso la línea siguiente ya puede leer el resultado.
- La comunicación es por **retorno**: el formulario guarda la planta en `resultado` y el controlador principal la lee con `getResultado()`. `Cancelar` deja `resultado` en `null`.
- Cada ventana tiene su propio controlador; quien abre la ventana es quien crea el `FXMLLoader` y recupera el controlador con `getController()`.

## Paso 6 — Validación dentro del formulario (R4)

1. `onConfirmar()` limpia los errores anteriores (`lblError` y el borde rojo de los campos).
2. Revisa cada campo y **acumula** los errores en una lista, con el formato *"Campo: motivo"*.
3. Si hay errores: `lblError.setText(String.join("\n", errores))` y `return`. No se cierra la ventana ni se borra ningún campo, así que los valores se mantienen.
4. Si no hay errores: crea la planta (el modelo vuelve a validar como red de seguridad) y cierra con `((Stage) lblError.getScene().getWindow()).close()`.

`lblError` solo ocupa espacio cuando tiene texto: `visibleProperty` se enlaza a `textProperty().isNotEmpty()` y `managedProperty` a `visibleProperty`.

## Verificación con los datos de prueba

| Prueba | Resultado esperado |
|---|---|
| Iniciar sin `data/` | Tabla vacía, sin mensaje. |
| **Eliminar** sin selección | *"Selecciona una planta de la tabla para eliminarla."* en la ventana principal. |
| Registrar las 4 plantas | Costos `$10.800`, `$8.000`, `$6.600`, `$5.500`; mensaje *"Rosal (Exterior) registrada correctamente."* |
| Buscar `LAV` | Solo Lavanda. Al borrar, las 4. |
| Formulario con altura `abc` y cantidad `0` | Dos líneas de error en el formulario, valores conservados, ventana abierta. |
| **Cancelar** | Se cierra y la tabla no cambia. |
| Eliminar Rosal, cerrar y abrir | Quedan 3; `exteriores.json` contiene solo Lavanda. |
| `interiores.json` con texto inválido | Mensaje en la ventana principal; tabla vacía; sin cierre. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `Failed making field ... accessible` al guardar o cargar | Falta `opens ...model to com.google.gson`. |
| La tabla muestra las filas pero las columnas quedan vacías | Falta `javafx.base` en el `opens` del modelo, o el nombre de `PropertyValueFactory` no coincide con un getter. |
| `LoadException ... Can't set property` / el botón no responde | Falta `opens ...controller to javafx.fxml`, o el `onAction="#metodo"` no coincide con el método. |
| `NullPointerException` al iniciar con un archivo vacío | No se consideró el `null` que retorna `fromJson`. |
| El formulario acepta altura 999 al cargar desde el archivo | Falta llamar a `validar()` después de leer. |
| La ventana principal sigue respondiendo con el formulario abierto | Falta `initModality` o se usó `show()` en vez de `showAndWait()`. |
| La tabla muestra una planta que no está en el archivo | Se modificó la lista observable antes de guardar. |
| `mvn javafx:run` no encuentra la clase | En un proyecto con módulos se indica `modulo/clase`, ver `pom.xml`. |
