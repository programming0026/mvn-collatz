package com.juniverse.collatz;

import com.juniverse.collatz.dto.CycleResult;
import com.juniverse.collatz.dto.SearchResult;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demostraciones: muestra una secuencia, comprueba un rango, busca ciclos y busca contraejemplos.
 *
 * @author linux
 */
public class Main {

    /**
     * Ejecuta las cuatro demostraciones.
     * @param args no se usan
     */
    public static void main(String[] args) {
        try {
            testCollatz();
            testIsCollatz(1000_000, 1000_100);
            testCollatzCycles(1, 100_000);
            testSearcher();
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /** Busca ciclos en un rango y muestra estadísticas. */
    private static void testCollatzCycles(long init, long end) {

        System.out.println("=== Cycle Search in the Collatz Conjecture ===");

        // Buscar en el rango 1 a 100,000
        long startTime = System.currentTimeMillis();
        List<CycleResult> results = CollatzCycleDetector.searchCyclesInRange(init, end);
        long endTime = System.currentTimeMillis();

        System.out.printf("%nSearch completed in %,d ms%n", endTime - startTime);
        System.out.printf("Interesting results found: %d%n", results.size());

        // Mostrar estadísticas
        if (!results.isEmpty()) {
            long maxValueSeen = results.stream()
                    .mapToLong(CycleResult::maxValue)
                    .max()
                    .orElse(0);

            System.out.printf("Maximum value reached: %,d%n", maxValueSeen);

            // Mostrar cualquier ciclo encontrado
            results.stream()
                    .filter(CycleResult::foundCycle)
                    .forEach(result -> {
                        System.out.println("¡CYCLE FOUND!");
                        System.out.println("Initial number: " + result.startNumber());
                        System.out.println("Cycle: " + result.cycle());
                    });
        }

    }

    /** Muestra la secuencia de un número aleatorio. */
    private static void testCollatz() {
        System.out.println("==Show Collatz==");
        Random random = new Random();
        int value = random.nextInt(1, 1000);
        int n = CollatzConjecture.showCollatzSequence(value);
        System.out.println("Number of movements: " + n);
    }

    /** Comprueba en paralelo que los números del rango convergen a 1. */
    private static void testIsCollatz(long init, long end) throws InterruptedException {
        System.out.println("==IsCollatz==");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        for (long n = init; n <= end; n++) {
            BigInteger num = BigInteger.valueOf(n);
            executor.submit(() -> {
                if (!CollatzConjecture.isCollatzSequence(num)) {
                    System.err.println("Number " + num + " does not meet Collatz's conjecture");
                } else {
                    System.out.println("Number " + num + " meets Collatz's conjecture");
                }
            });
        }

        executor.shutdown();
        int count = 0;
        while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("Waiting " + count + " ....");
            count++;
        }
    }

    /** Ejecuta la búsqueda de contraejemplos con sus parámetros por defecto. */
    private static void testSearcher() {
        System.out.println("Search for Collatz Counterexamples");
        System.out.println("=".repeat(60));

        // Configuración de búsqueda
        long maxSearchValue = 1_000_0000L;
        int maxTreeDepth = 1_00000;

        try (CollatzCounterExampleSearcher searcher
                = new CollatzCounterExampleSearcher(maxSearchValue, maxTreeDepth)) {

            // Ejecutar búsqueda
            SearchResult result = searcher.searchForCounterexamples();

            // Mostrar resultados
            displayResults(result);

        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /** Imprime los resultados y las estadísticas de la búsqueda. */
    private static void displayResults(SearchResult result) {
        System.out.println("\nSEARCH RESULTS");
        System.out.println("=".repeat(60));

        // Tiempo de ejecución
        System.out.printf("Total time: %d seconds%n", result.executionTime().toSeconds());

        // Verificar si encontramos contraejemplos
        if (result.hasCounterexamples()) {
            System.out.println("POSSIBLE COUNTEREXAMPLES FOUND");

            if (!result.cyclesFound().isEmpty()) {
                System.out.println("\nCYCLES DETECTED:");
                for (CycleResult cycle : result.cyclesFound()) {
                    System.out.printf("Number %d of cycle form: %s%n",
                            cycle.startNumber(), cycle.cycle());
                }
            }

            if (!result.missingFromTree().isEmpty()) {
                System.out.println("\nMISSING NUMBERS IN INVERSE TREE:");
                result.missingFromTree().stream()
                        .limit(20) // Mostrar solo los primeros 20
                        .forEach(n -> System.out.printf("%d%n", n));

                if (result.missingFromTree().size() > 20) {
                    System.out.printf("... and %d more%n",
                            result.missingFromTree().size() - 20);
                }
            }
        } else {
            System.out.println("No counterexamples were found in the explored range.");
        }

        // Estadísticas detalladas
        System.out.println("\nDETAILED STATISTICS:");
        Map<String, Object> stats = result.statistics();

        System.out.printf("Special candidates generated: %,d%n",
                (Integer) stats.get("candidatos_especiales"));
        System.out.printf("Numbers in reverse tree: %,d%n",
                (Integer) stats.get("numeros_en_arbol"));
        System.out.printf("Missing numbers found: %,d%n",
                (Integer) stats.get("numeros_faltantes"));
        System.out.printf("Tree coverage: %.2f%%%n",
                (Double) stats.get("cobertura_arbol_porcentaje"));
        System.out.printf("Maximum observed value: %,d%n",
                (Long) stats.get("valor_maximo_visto"));

        // Análisis de resultados
        System.out.println("\nANALYSIS:");
        analyzeResults(result);
    }

    /** Interpreta los resultados: contraejemplos, cobertura del árbol y valores máximos. */
    private static void analyzeResults(SearchResult result) {
        Map<String, Object> stats = result.statistics();

        if (result.hasCounterexamples()) {
            System.out.println("Manual verification of the counterexamples found is required");
        } else {
            System.out.println("The results support the validity of the Collatz conjecture");
        }

        // Análisis de cobertura
        double coverage = (Double) stats.get("cobertura_arbol_porcentaje");
        if (coverage > 95.0) {
            System.out.println("Excellent reverse tree coverage");
        } else if (coverage > 80.0) {
            System.out.println("Good reverse tree coverage");
        } else {
            System.out.println("Consider increasing the depth of the tree");
        }

        // Análisis de valores máximos
        long maxValue = (Long) stats.get("valor_maximo_visto");
        if (maxValue > 1_000_000) {
            System.out.println("Some numbers reach very high values - interesting candidates");
        }

    }
}
