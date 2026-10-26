# Guía de resolución · 11 Persistencia con Gson

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/11-persistencia-gson`

## Idea central

```
Main ──usa──► InstrumentoDao<T> (interfaz) ◄──implementa── GsonInstrumentoDao<T> ──► Gson ──► data/cuerdas.json
                    │                                                                      └──► data/vientos.json
                    └── lanza PersistenciaException (nunca IOException ni excepciones de Gson)
```

- Hay **un DAO por subtipo**: `GsonInstrumentoDao<InstrumentoCuerda>` y `GsonInstrumentoDao<InstrumentoViento>`. La misma clase sirve para ambos porque es genérica.
- Como cada archivo guarda una sola clase, Gson **no necesita saber de qué subtipo es cada objeto**: lo sabe por el tipo de la lista. (Con un único archivo mezclado, Gson no resuelve la herencia por sí solo y habría que escribir un adaptador propio.)

## Paso 1 — Módulo (R4)

```java
module cl.dsy1102.ejemplos.instrumentos {
    requires com.google.gson;
    opens cl.dsy1102.ejemplos.instrumentos.model to com.google.gson;
}
```

- `requires`: el proyecto usa clases de Gson.
- `opens ... to`: Gson no usa los *getters*, lee y escribe los **atributos privados** por reflexión. Un paquete que no está abierto no permite esa reflexión.

**Pregunta 4:** sin el `opens`, el proyecto compila igual, pero al guardar Gson lanza

```
JsonIOException: Failed making field '...InstrumentoCuerda#numeroCuerdas' accessible;
either increase its visibility or write a custom TypeAdapter for its declaring type.
```

`JsonIOException` es una `JsonParseException`, así que el DAO la traduce a `PersistenciaException` con el mensaje *"Revisa permisos y espacio en disco"*. El mensaje del DAO engaña: **siempre se debe mirar la causa** (`getCause()`).

## Paso 2 — Construcción (R1)

```java
this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
```

**Pregunta 1:** por el *type erasure*, `List<InstrumentoCuerda>` y `List<InstrumentoViento>` son la misma clase en tiempo de ejecución. Con `List.class` Gson no sabría qué contiene la lista y crearía mapas (`LinkedTreeMap`) en lugar de instrumentos. `TypeToken` reconstruye el tipo completo a partir de la clase que recibe el constructor.

## Paso 3 — `cargar()` (R2)

```java
if (!Files.exists(archivo)) return new ArrayList<>();
try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
    List<T> instrumentos = gson.fromJson(lector, tipoLista);
    if (instrumentos == null) return new ArrayList<>();   // archivo vacío
    for (T instrumento : instrumentos) { ...; instrumento.validar(); }
    return new ArrayList<>(instrumentos);
} catch (IOException | JsonParseException | IllegalArgumentException e) {
    throw new PersistenciaException("...", e);
}
```

- **Pregunta 2:** con un archivo vacío (o con el texto `null`) `fromJson` **retorna `null`**; no lanza excepción. Sin la comprobación, el `for` lanzaría `NullPointerException` y la aplicación se caería en la primera ejecución.
- **Pregunta 3:** Gson crea el objeto sin llamar al constructor (usa `Unsafe`) y asigna los atributos directamente. Por eso `numeroCuerdas = 99` entra sin que ningún *setter* lo revise. La solución es un método `validar()` en el modelo, que el DAO llama después de leer. Compara con Jackson (caso 10): ahí los *setters* sí se ejecutan y validan solos.
- Los errores de Gson (`JsonSyntaxException`, `JsonIOException`) son `RuntimeException`: no obligan a `try/catch`, pero **hay que capturarlos** (`JsonParseException` es su clase padre).
- Un `[ null ]` también es JSON válido: se revisa cada elemento antes de validarlo.
- Se retorna `new ArrayList<>(...)` (modificable) para que quien llama pueda agregar elementos.

## Paso 4 — `guardar()` (R3)

```java
Files.createDirectories(carpeta);
try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
    gson.toJson(instrumentos, tipoLista, escritor);
}
```

- Se indica el `Type` también al escribir, para que Gson use el tipo `List<T>` completo.
- El orden de los atributos en el JSON es primero los de la subclase y luego los de la clase base (`numeroCuerdas`, `electrico`, `nombre`, `precio`). Es normal en Gson.

## Pregunta 5 — Un archivo por subtipo

| Ventaja | Costo |
|---|---|
| Gson no necesita información de subtipos ni adaptadores. | Hay dos DAO (o uno genérico, como aquí) y dos archivos que mantener coherentes. |
| Un archivo dañado afecta solo a un tipo. | Para mostrar todo junto hay que cargar ambos y combinar las listas. |
| El JSON es simple y legible. | Una regla que abarque ambos tipos (por ejemplo, nombres no repetidos) se verifica al juntar las listas. |

## Verificación

1. `mvn test` → 7 pruebas en verde.
2. `mvn compile exec:java` → salida igual a la del enunciado.
3. Quita el `opens` y ejecuta `mvn test`: fallan las pruebas que guardan y cargan. Devuélvelo.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `NullPointerException` al cargar un archivo vacío | No se consideró el `null` de `fromJson`. |
| Cargar `List.class` en vez de `TypeToken` y recibir `ClassCastException` al usar los elementos | Gson creó `LinkedTreeMap` en vez del modelo. |
| El programa acepta `numeroCuerdas = 99` desde el archivo | Falta llamar a `validar()` después de leer. |
| `JsonIOException: Failed making field ... accessible` | Falta el `opens ... to com.google.gson`. |
| `Unable to create instance of class ...` / `Cannot allocate class ...` al cargar (en IntelliJ o en module path; con `mvn exec:java` no aparece) | Falta `requires jdk.unsupported;`: el modelo no tiene constructor sin argumentos y Gson necesita `sun.misc.Unsafe` para crear los objetos. |
| `module com.google.gson not found` | Falta la dependencia en el `pom.xml` o `requires` mal escrito. |
| Tildes rotas en el archivo | Se usó `FileReader`/`FileWriter` sin indicar UTF-8. |
