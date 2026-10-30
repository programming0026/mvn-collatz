package com.juniverse.collatz.dto;

import java.util.List;

/**
 * Resultado de analizar un número inicial en busca de ciclos.
 *
 * @param startNumber número inicial
 * @param cycle elementos del ciclo (vacío si no hay)
 * @param foundCycle {@code true} si se encontró un ciclo distinto del 1-4-2
 * @param iterations iteraciones realizadas
 * @param maxValue mayor valor alcanzado
 *
 * @author ajuar
 */
public record CycleResult(long startNumber,
        List<Long> cycle,
        boolean foundCycle,
        long iterations,
        long maxValue) {

    /** Resultado de un número sin ciclo. */
    public static CycleResult noCycle(long start, long iterations, long maxValue) {
        return new CycleResult(start, List.of(), false, iterations, maxValue);
    }

    /** Resultado de un número que entra en un ciclo; el valor máximo es el mayor del ciclo. */
    public static CycleResult withCycle(long start, List<Long> cycle, long iterations) {
        long maxValue = cycle.stream().mapToLong(Long::longValue).max().orElse(start);
        return new CycleResult(start, cycle, true, iterations, maxValue);
    }
}
