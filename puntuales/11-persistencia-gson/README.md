# 11 · Persistencia con Gson — Taller de instrumentos

**Tema:** Gson (`Gson`, `GsonBuilder`, `TypeToken`), un archivo JSON por subtipo, módulos (`requires` y `opens` hacia `com.google.gson`), validación del modelo después de leer, archivos vacíos y dañados, traducción de excepciones propias, pruebas con `@TempDir`.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Un taller de instrumentos musicales registra instrumentos de **cuerda** (número de cuerdas, si es eléctrico) y de **viento** (material, afinación). Los datos se pierden al cerrar el programa. Se necesita guardarlos en archivos JSON, **un archivo por tipo de instrumento**:

- `data/cuerdas.json` contiene solo instrumentos de cuerda.
- `data/vientos.json` contiene solo instrumentos de viento.

Al volver a abrir el programa:

- cada archivo recupera sus instrumentos con todos sus datos;
- la primera ejecución (sin archivos) y un archivo vacío no son un error;
- un archivo dañado, o con datos que el modelo no acepta, se informa con un mensaje comprensible y sin detener el programa.

El resto del programa trabaja con la interfaz `InstrumentoDao<T>`, sin saber que los datos están en JSON.

## Código inicial

| Archivo | Estado |
|---|---|
| `module-info.java` | Resuelto. Revisa para qué sirven `requires` y `opens`. |
| `model/Instrumento` (abstracta), `InstrumentoCuerda`, `InstrumentoViento` | Reglas y validaciones resueltas, más un método `validar()`. |
| `dao/InstrumentoDao.java`, `PersistenciaException.java` | Resueltos. |
| `dao/GsonInstrumentoDao.java` | **Por completar.** |
| `Main.java` | Resuelto. Recorre los seis escenarios del problema. |
| `src/test/.../GsonInstrumentoDaoTest.java` | Compila desde el inicio. **5 de 7 pruebas fallan** hasta terminar. |

## Requerimientos

**R1. Construcción.** El constructor recibe el `Path` del archivo y la `Class<T>` del subtipo. Con ella arma el tipo `List<T>` usando `TypeToken` y crea un `Gson` con formato legible (*pretty print*).

**R2. `cargar()`**
- Archivo inexistente → lista vacía.
- Archivo vacío → lista vacía. Atención: Gson no lanza una excepción en este caso, retorna `null`.
- Después de leer, cada instrumento debe pasar por `validar()`. Gson llena los atributos directamente: **no usa los constructores ni los *setters***, por lo que las reglas del modelo no se aplican solas.
- Cualquier problema de lectura, de formato o de datos → `PersistenciaException` con un mensaje comprensible y la excepción original como **causa**.

**R3. `guardar(List<T>)`**
- Crea la carpeta `data/` si no existe.
- Escribe en UTF-8 y con formato legible.
- Cualquier problema de escritura → `PersistenciaException`.

**R4. Módulos.** Revisa `module-info.java`: ¿qué ocurre si se elimina la línea `opens ... to com.google.gson`?

## Resultado esperado (`mvn compile exec:java`)

```
== 1. Primera ejecucion (los archivos no existen)
0 instrumento(s)
0 instrumento(s)

== 2. Guardar dos cuerdas y un viento (un archivo por subtipo)
Guardado en .../data/cuerdas.json
[
  {
    "numeroCuerdas": 6,
    "electrico": false,
    "nombre": "Guitarra clasica",
    "precio": 120000
  },
  ...

== 3. Cargar con DAO nuevos (como si se reiniciara el programa)
2 instrumento(s)
  [Cuerda] Guitarra clasica | $120.000 | 6 cuerdas
  [Cuerda] Bajo Fender | $450.000 | 4 cuerdas | electrico
1 instrumento(s)
  [Viento] Flauta traversa | $280.000 | Metal | afinacion Do

== 4. Archivo vacio
0 instrumento(s)

== 5. Archivo danado
ERROR: No se pudieron leer los instrumentos de danado.json: el archivo esta danado o tiene datos invalidos.
  causa: JsonSyntaxException

== 6. Archivo con un dato que el modelo rechaza
ERROR: No se pudieron leer los instrumentos de invalido.json: el archivo esta danado o tiene datos invalidos.
  causa: IllegalArgumentException
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 7 pruebas en verde
mvn compile exec:java
```

Las pruebas se ejecutan dentro del módulo: si falta el `opens`, fallan.

## Preguntas para pensar

1. ¿Por qué se usa `TypeToken.getParameterized(List.class, clase)` y no `List.class`?
2. Gson retorna `null` con un archivo vacío. ¿Qué pasaría en `cargar()` si no se considerara ese caso?
3. Un archivo editado a mano trae `"numeroCuerdas": 99`. ¿Por qué el modelo no lo rechaza solo, y quién lo hace?
4. Si se elimina el `opens`, ¿qué excepción lanza Gson y por qué el mensaje del DAO puede confundir?
5. ¿Qué ventaja tiene guardar un archivo por subtipo respecto de un único archivo con todos los instrumentos? ¿Qué se pierde?
