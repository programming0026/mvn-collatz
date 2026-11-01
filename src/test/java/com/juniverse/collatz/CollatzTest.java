package com.juniverse.collatz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Pruebas de la lógica de Collatz y de la valoración del informe. */
class CollatzTest {

    @ParameterizedTest
    @CsvSource({"6, 3", "7, 22", "1, 4", "16, 8"})
    void collatzStepAplicaLaRegla(long n, long expected) {
        assertEquals(expected, CollatzConjecture.collatzStep(n));
    }

    @Test
    void laSecuenciaDeSeisTerminaEnUno() {
        List<BigInteger> sequence = CollatzConjecture.generateCollatzSequence(BigInteger.valueOf(6));
        assertEquals(BigInteger.valueOf(6), sequence.get(0));
        assertEquals(BigInteger.ONE, sequence.get(sequence.size() - 1));
        assertEquals(List.of(6, 3, 10, 5, 16, 8, 4, 2, 1), sequence.stream().map(BigInteger::intValue).toList());
    }

    @Test
    void losNumerosPequenosConvergenAUno() {
        for (int n = 1; n <= 200; n++) {
            assertTrue(CollatzConjecture.isCollatzSequence(BigInteger.valueOf(n)), "n = " + n);
        }
    }

    @ParameterizedTest
    @CsvSource({"99.0, Excellent reverse tree coverage",
        "90.0, Good reverse tree coverage",
        "50.0, Consider increasing the depth of the tree"})
    void valoraLaCoberturaDelArbol(double coverage, String expected) {
        assertEquals(expected, SearchReport.assessCoverage(coverage));
    }
}
