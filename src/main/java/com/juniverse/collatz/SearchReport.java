package com.juniverse.collatz;

import com.juniverse.collatz.dto.CycleResult;
import com.juniverse.collatz.dto.SearchResult;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Presenta los resultados de la búsqueda de contraejemplos: lista, estadísticas y una interpretación sencilla.
 */
public class SearchReport {

    private static final Logger LOGGER = AppLogger.get(SearchReport.class);

    /** Imprime los resultados y las estadísticas de la búsqueda. */
    public void display(SearchResult result) {
        LOGGER.info("\nSEARCH RESULTS");
        LOGGER.info("=".repeat(60));

        // Tiempo de ejecución
        LOGGER.info(String.format("Total time: %d seconds", result.executionTime().toSeconds()));

        // Verificar si encontramos contraejemplos
        if (result.hasCounterexamples()) {
            LOGGER.info("POSSIBLE COUNTEREXAMPLES FOUND");

            if (!result.cyclesFound().isEmpty()) {
                LOGGER.info("\nCYCLES DETECTED:");
                for (CycleResult cycle : result.cyclesFound()) {
                    LOGGER.info(String.format("Number %d of cycle form: %s",
                            cycle.startNumber(), cycle.cycle()));
                }
            }

            if (!result.missingFromTree().isEmpty()) {
                LOGGER.info("\nMISSING NUMBERS IN INVERSE TREE:");
                result.missingFromTree().stream()
                        .limit(20) // Mostrar solo los primeros 20
                        .forEach(n -> LOGGER.info(String.format("%d", n)));

                if (result.missingFromTree().size() > 20) {
                    LOGGER.info(String.format("... and %d more",
                            result.missingFromTree().size() - 20));
                }
            }
        } else {
            LOGGER.info("No counterexamples were found in the explored range.");
        }

        // Estadísticas detalladas
        LOGGER.info("\nDETAILED STATISTICS:");
        Map<String, Object> stats = result.statistics();

        LOGGER.info(String.format("Special candidates generated: %,d",
                (Integer) stats.get("candidatos_especiales")));
        LOGGER.info(String.format("Numbers in reverse tree: %,d",
                (Integer) stats.get("numeros_en_arbol")));
        LOGGER.info(String.format("Missing numbers found: %,d",
                (Integer) stats.get("numeros_faltantes")));
        LOGGER.info(String.format("Tree coverage: %.2f%%",
                (Double) stats.get("cobertura_arbol_porcentaje")));
        LOGGER.info(String.format("Maximum observed value: %,d",
                (Long) stats.get("valor_maximo_visto")));

        // Análisis de resultados
        LOGGER.info("\nANALYSIS:");
        analyze(result);
    }

    /** Interpreta los resultados: contraejemplos, cobertura del árbol y valores máximos. */
    private void analyze(SearchResult result) {
        Map<String, Object> stats = result.statistics();

        if (result.hasCounterexamples()) {
            LOGGER.info("Manual verification of the counterexamples found is required");
        } else {
            LOGGER.info("The results support the validity of the Collatz conjecture");
        }

        // Análisis de cobertura
        double coverage = (Double) stats.get("cobertura_arbol_porcentaje");
        LOGGER.info(assessCoverage(coverage));

        // Análisis de valores máximos
        long maxValue = (Long) stats.get("valor_maximo_visto");
        if (maxValue > 1_000_000) {
            LOGGER.info("Some numbers reach very high values - interesting candidates");
        }

    }

    /**
     * Valora la cobertura del árbol inverso.
     * @param coverage porcentaje de números del rango que aparecen en el árbol
     * @return mensaje con la valoración
     */
    public static String assessCoverage(double coverage) {
        if (coverage > 95.0) {
            return "Excellent reverse tree coverage";
        }
        if (coverage > 80.0) {
            return "Good reverse tree coverage";
        }
        return "Consider increasing the depth of the tree";
    }
}
