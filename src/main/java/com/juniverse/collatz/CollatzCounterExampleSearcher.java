package com.juniverse.collatz;

import com.juniverse.collatz.dto.CycleResult;
import com.juniverse.collatz.dto.SearchResult;
import java.io.Closeable;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.logging.Logger;
/**
 * Busca contraejemplos de la conjetura de Collatz combinando candidatos especiales, el árbol inverso
 * y la detección de ciclos con hilos virtuales.
 *
 * @author ajuar
 */
public class CollatzCounterExampleSearcher implements Closeable{
    private static final Logger LOGGER = AppLogger.get(CollatzCounterExampleSearcher.class);

    private final ExecutorService executor;
    private final long maxSearchValue;
    private final int maxTreeDepth;
    
    /**
     * Crea el buscador.
     * @param maxSearchValue valor máximo que se explora
     * @param maxTreeDepth profundidad máxima del árbol inverso
     */
    public CollatzCounterExampleSearcher(long maxSearchValue, int maxTreeDepth) {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.maxSearchValue = maxSearchValue;
        this.maxTreeDepth = maxTreeDepth;
    }
    
    /**
     * Ejecuta la búsqueda: genera candidatos, construye el árbol inverso, verifica los números ausentes
     * y detecta ciclos en los candidatos prioritarios.
     * @return resultado con ciclos, números sin verificar y estadísticas
     */
    public SearchResult searchForCounterexamples() {
        Instant startTime = Instant.now();
        LOGGER.info("Starting a comprehensive search for counterexamples...");
        
      
        LOGGER.info("Generating special candidates...");
        List<Long> specialCandidates = CandidateGenerator.generateAllCandidates(maxSearchValue);
        LOGGER.info(String.format("Generated %,d special candidates", specialCandidates.size()));
      
        
        LOGGER.info("Building inverse Collatz tree...");
        InverseCollatzTree tree = new InverseCollatzTree(maxTreeDepth, maxSearchValue);
        Set<Long> treeNumbers = tree.buildTreeParallel();
        LOGGER.info(String.format("Tree contains %,d numbers", treeNumbers.size()));
       
      
        LOGGER.info("Searching for missing numbers in the tree...");
        List<Long> notInTree = tree.findMissingNumbers(1, Math.min(maxSearchValue, 100_000));
        LOGGER.info(String.format("Found %,d numbers outside the tree", notInTree.size()));

        // Estar fuera del árbol NO es un contraejemplo: el árbol solo contiene valores <= maxSearchValue y muchas
        // secuencias suben más que eso antes de bajar a 1. Cada número se comprueba directamente (BigInteger).
        List<Long> missingNumbers = notInTree.stream()
                .filter(n -> !CollatzConjecture.isCollatzSequence(BigInteger.valueOf(n)))
                .toList();
        LOGGER.info(String.format("Of those, %,d could not be verified as converging", missingNumbers.size()));
        
        
        LOGGER.info("Checking cycles in priority candidates...");
        Set<Long> priorityCandidates = new HashSet<>();
        priorityCandidates.addAll(specialCandidates);
priorityCandidates.addAll(notInTree.subList(0, Math.min(notInTree.size(), 1000)));
        
        List<CycleResult> cycleResults = new ArrayList<>();
        List<Future<CycleResult>> futures = new ArrayList<>();
        
        for (Long candidate : priorityCandidates) {
            futures.add(executor.submit(() -> CollatzCycleDetector.detectCycle(candidate)));
        }
        
        for (Future<CycleResult> future : futures) {
            try {
                CycleResult result = future.get();
                if (result.foundCycle() || result.maxValue() > result.startNumber() * 50) {
                    cycleResults.add(result);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.severe("Error verifying cycle: " + e.getMessage());
            }
        }
        
       
        Map<String, Object> statistics = compileStatistics(
specialCandidates, treeNumbers, notInTree, cycleResults
        );
        
        Duration executionTime = Duration.between(startTime, Instant.now());
        
        return new SearchResult(
            cycleResults.stream().filter(CycleResult::foundCycle).toList(),
            missingNumbers,
            specialCandidates,
            statistics,
            executionTime
        );
    }
    
    /** Reúne las estadísticas de la búsqueda. */
    private Map<String, Object> compileStatistics(
            List<Long> specialCandidates,
            Set<Long> treeNumbers,
            List<Long> missingNumbers,
            List<CycleResult> cycleResults) {
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("candidatos_especiales", specialCandidates.size());
        stats.put("numeros_en_arbol", treeNumbers.size());
        stats.put("numeros_faltantes", missingNumbers.size());
        stats.put("ciclos_encontrados", cycleResults.stream().filter(CycleResult::foundCycle).count());
        
        // Estadísticas de valores máximos
        OptionalLong maxValueSeen = cycleResults.stream()
            .mapToLong(CycleResult::maxValue)
            .max();
        stats.put("valor_maximo_visto", maxValueSeen.orElse(0));
        
        // Cobertura del árbol
        double coverage = treeNumbers.size() / (double) maxSearchValue * 100;
        stats.put("cobertura_arbol_porcentaje", coverage);
        
        return stats;
    }
    
    /** Cierra el ejecutor de hilos. */
    @Override
    public void close() {
        executor.shutdown();
    }
}
