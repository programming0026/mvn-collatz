package com.juniverse.collatz.dto;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Resultado de la búsqueda de contraejemplos.
 *
 * @param cyclesFound ciclos encontrados
 * @param missingFromTree números fuera del árbol inverso que además no se pudieron verificar
 * @param specialCandidates candidatos especiales generados
 * @param statistics estadísticas de la búsqueda
 * @param executionTime tiempo total
 *
 * @author ajuar
 */
public record SearchResult(
        List<CycleResult> cyclesFound,
        List<Long> missingFromTree,
        List<Long> specialCandidates,
        Map<String, Object> statistics,
        Duration executionTime) {

    /** Indica si hay posibles contraejemplos (ciclos o números no verificados). */
    public boolean hasCounterexamples() {
        return !cyclesFound.isEmpty() || !missingFromTree.isEmpty();
    }
}
