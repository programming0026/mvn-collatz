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

import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Demostraciones: muestra una secuencia, comprueba un rango, busca ciclos y busca contraejemplos.
 *
 * @author linux
 */
public class CollatzDemos {
    private static final Logger LOGGER = AppLogger.get(CollatzDemos.class);


    /** Ejecuta las cuatro demostraciones. */
    public static void runAll() {
        try {
            testCollatz();
            testIsCollatz(1000_000, 1000_100);
            testCollatzCycles(1, 100_000);
            testSearcher();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /** Busca ciclos en un rango y muestra estadísticas. */
    private static void testCollatzCycles(long init, long end) {

        LOGGER.info("=== Cycle Search in the Collatz Conjecture ===");

        // Buscar en el rango 1 a 100,000
        long startTime = System.currentTimeMillis();
        List<CycleResult> results = CollatzCycleDetector.searchCyclesInRange(init, end);
        long endTime = System.currentTimeMillis();

        LOGGER.info(String.format("%nSearch completed in %,d ms", endTime - startTime));
        LOGGER.info(String.format("Interesting results found: %d", results.size()));

        // Mostrar estadísticas
        if (!results.isEmpty()) {
            long maxValueSeen = results.stream()
                    .mapToLong(CycleResult::maxValue)
                    .max()
                    .orElse(0);

            LOGGER.info(String.format("Maximum value reached: %,d", maxValueSeen));

            // Mostrar cualquier ciclo encontrado
            results.stream()
                    .filter(CycleResult::foundCycle)
                    .forEach(result -> {
                        LOGGER.info("¡CYCLE FOUND!");
                        LOGGER.info("Initial number: " + result.startNumber());
                        LOGGER.info("Cycle: " + result.cycle());
                    });
        }

    }

    /** Muestra la secuencia de un número aleatorio. */
    private static void testCollatz() {
        LOGGER.info("==Show Collatz==");
        Random random = new Random();
        int value = random.nextInt(1, 1000);
        int n = CollatzConjecture.showCollatzSequence(value);
        LOGGER.info("Number of movements: " + n);
    }

    /** Comprueba en paralelo que los números del rango convergen a 1. */
    private static void testIsCollatz(long init, long end) throws InterruptedException {
        LOGGER.info("==IsCollatz==");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        for (long n = init; n <= end; n++) {
            BigInteger num = BigInteger.valueOf(n);
            executor.submit(() -> {
                if (!CollatzConjecture.isCollatzSequence(num)) {
                    LOGGER.severe("Number " + num + " does not meet Collatz's conjecture");
                } else {
                    LOGGER.info("Number " + num + " meets Collatz's conjecture");
                }
            });
        }

        executor.shutdown();
        int count = 0;
        while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            LOGGER.info("Waiting " + count + " ....");
            count++;
        }
    }

    /** Ejecuta la búsqueda de contraejemplos con sus parámetros por defecto. */
    private static void testSearcher() {
        LOGGER.info("Search for Collatz Counterexamples");
        LOGGER.info("=".repeat(60));

        // Configuración de búsqueda
        long maxSearchValue = 1_000_0000L;
        int maxTreeDepth = 1_00000;

        try (CollatzCounterExampleSearcher searcher
                = new CollatzCounterExampleSearcher(maxSearchValue, maxTreeDepth)) {

            // Ejecutar búsqueda
            SearchResult result = searcher.searchForCounterexamples();

            // Mostrar resultados
            new SearchReport().display(result);

        } catch (Exception e) {
            LOGGER.severe("Error during search: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
