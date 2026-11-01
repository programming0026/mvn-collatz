# Conjetura de Collatz

Explora la conjetura de Collatz (3n + 1): visualiza secuencias, detecta ciclos y busca posibles contraejemplos usando hilos virtuales de Java 21.

## Características

- Secuencias de Collatz con `long` (con detección de desbordamiento) y con `BigInteger`.
- Gráfica interactiva de la secuencia (JFreeChart).
- Detección de ciclos con el algoritmo de Floyd, en paralelo con hilos virtuales.
- Árbol inverso de Collatz y generador de candidatos especiales.
- Comprobación con memoria (memoización) de los números ya verificados.

## Requisitos

- JDK 21 y Maven 3.9 o superior.

## Compilar y ejecutar

```bash
mvn compile exec:java
```

Para ver la gráfica:

```bash
mvn compile exec:java -Dexec.mainClass=com.juniverse.collatz.CollatzGraphic
```

## Notas

- La conjetura está verificada por ordenador hasta 2⁶⁸ y sigue sin demostrarse; este proyecto es una herramienta de exploración.
- Un número fuera del árbol inverso no es un contraejemplo: el programa lo comprueba directamente antes de informarlo.
- La búsqueda por defecto de `Main` es intensiva; baja `maxSearchValue` en `testSearcher()` si tarda demasiado.

## JAR ejecutable (fat jar)

```bash
mvn clean package
java -jar target/mvn-collatz-1.0.0-SNAPSHOT-launcher.jar
```

El archivo `-launcher.jar` incluye todas las dependencias.

## Estructura

```text
src/main/java/com/juniverse/collatz/
├── Main.java                  # arranque
├── CollatzDemos.java          # las cuatro demostraciones
├── CollatzConjecture.java     # reglas y secuencias
├── CollatzCycleDetector.java, CollatzCounterExampleSearcher.java, InverseCollatzTree.java
├── SearchReport.java          # presenta e interpreta los resultados de la búsqueda
└── dto/                       # CycleResult, SearchResult, TreeNode
```

## Pruebas

```bash
mvn test
```

## Licencia

Código de ejemplo de uso libre con fines educativos.
