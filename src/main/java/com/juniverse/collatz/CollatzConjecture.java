package com.juniverse.collatz;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Logger;
/**
 * Conjetura de Collatz: partiendo de cualquier entero positivo, si n es par se divide entre 2 y si es impar
 * se calcula 3n + 1; la conjetura afirma que siempre se llega a 1.
 *
 * @author linux
 */
public final class CollatzConjecture {
    private static final Logger LOGGER = AppLogger.get(CollatzConjecture.class);


    private static final ConcurrentHashMap<BigInteger, Boolean> memoizationMap = new ConcurrentHashMap<>();

    /** Clase de utilidades: no se instancia. */
    private CollatzConjecture() {
    }

     /**
      * Aplica un paso de Collatz.
      * @param n entero positivo
      * @return n/2 si es par, 3n + 1 si es impar
      * @throws IllegalArgumentException si {@code n <= 0}
      * @throws ArithmeticException si 3n + 1 desborda un long
      */
     public static long collatzStep(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Value  " + n + " is not valid");
        }
        if (n % 2 == 0) {
            return n / 2;
        } else {
            // Verificar overflow antes de la operación
            if (n > (Long.MAX_VALUE - 1) / 3) {
                throw new ArithmeticException("Overflow detected in: " + n);
            }
            return 3 * n + 1;
        }
    }
    
    /**
     * Genera la secuencia completa hasta llegar a 1, sin límite de tamaño.
     * @param n entero positivo
     * @return secuencia desde {@code n} hasta 1
     * @throws IllegalArgumentException si {@code n <= 0}
     */
    public static List<BigInteger> generateCollatzSequence(BigInteger n) {
        if (n.signum() <= 0) {
            throw new IllegalArgumentException("Value  " + n + " is not valid");
        }
        List<BigInteger> sequence = new ArrayList<>();
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.valueOf(2);
        BigInteger three = BigInteger.valueOf(3);

        while (!n.equals(one)) {
            sequence.add(n);
            if (n.mod(two).equals(BigInteger.ZERO)) {
                n = n.divide(two);
            } else {
                n = n.multiply(three).add(one);
            }
        }
        sequence.add(one);
        return sequence;
    }

    /**
     * Imprime la secuencia hasta 1.
     * @param n entero positivo
     * @return cantidad de pasos que se necesitaron
     * @throws ArithmeticException si algún valor desborda un long
     */
    public static int showCollatzSequence(long n) {
        int count = 0;
        if (n <= 0) {
            throw new IllegalArgumentException("Value  " + n + " is not valid");
        }
        // El logger siempre cierra la línea: la secuencia se arma completa y se registra de una vez
        StringBuilder sequence = new StringBuilder("Sequence for " + n + " => ");
        while (n != 1) {
            sequence.append(n).append(' ');
            n = collatzStep(n); // lanza ArithmeticException si 3n+1 desborda un long
            count++;
        }
        // Agregar 1 al final de la secuencia
        sequence.append(1);
        LOGGER.info(sequence.toString());
        return count;
    }

    /**
     * Comprueba si la secuencia de {@code n} llega a 1 (con memoria de los números ya analizados).
     * @param n entero positivo
     * @return {@code true} si converge a 1; {@code false} si entra en un ciclo
     */
    public static boolean isCollatzSequence(BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Value " + n + " is not valid");
        }

        Set<BigInteger> seen = new HashSet<>();
        while (!n.equals(BigInteger.ONE) && !seen.contains(n)) {
            seen.add(n);

            if (memoizationMap.containsKey(n)) {
                return memoizationMap.get(n);
            }

            if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                n = n.divide(BigInteger.TWO);
            } else {
                n = n.multiply(BigInteger.valueOf(3)).add(BigInteger.ONE);
            }
        }

        boolean converges = n.equals(BigInteger.ONE);
        for (BigInteger num : seen) {
            memoizationMap.put(num, converges);
        }

        return converges;
    }
}
