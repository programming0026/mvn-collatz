package com.juniverse.collatz;

import com.juniverse.collatz.dto.TreeNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Árbol inverso de Collatz: parte de 1 y genera los predecesores de cada número
 * (2n y, si es posible, (n − 1)/3 cuando es impar) hasta una profundidad y un valor máximos.
 *
 * @author ajuar
 */
public class InverseCollatzTree {

    private final Set<Long> exploredNumbers;
    private final Queue<TreeNode> searchQueue;
    private final int maxDepth;
    private final long maxValue;

    /**
     * Crea el árbol con la raíz en 1.
     * @param maxDepth profundidad máxima
     * @param maxValue valor máximo permitido en el árbol
     */
    public InverseCollatzTree(int maxDepth, long maxValue) {
        this.exploredNumbers = ConcurrentHashMap.newKeySet();
        this.searchQueue = new ConcurrentLinkedQueue<>();
        this.maxDepth = maxDepth;
        this.maxValue = maxValue;

        searchQueue.offer(new TreeNode(1, 0, null, "root"));
        exploredNumbers.add(1L);
    }

    /** Predecesores de un número: 2n y, si (n − 1) es divisible entre 3 y el resultado es impar, (n − 1)/3. */
    private List<TreeNode> generatePredecessors(TreeNode node) {
        List<TreeNode> predecessors = new ArrayList<>();
        long value = node.value();

        // Predecesor par: 2 * value
        long evenPredecessor = 2 * value;
        if (evenPredecessor <= maxValue && !exploredNumbers.contains(evenPredecessor)) {
            predecessors.add(new TreeNode(evenPredecessor, node.depth() + 1, node, "even"));
        }

        // Predecesor impar: (value - 1) / 3, solo si (value - 1) es divisible por 3
        if ((value - 1) % 3 == 0) {
            long oddPredecessor = (value - 1) / 3;
            if (oddPredecessor > 0 && oddPredecessor % 2 == 1
                    && oddPredecessor <= maxValue && !exploredNumbers.contains(oddPredecessor)) {
                predecessors.add(new TreeNode(oddPredecessor, node.depth() + 1, node, "odd"));
            }
        }

        return predecessors;
    }

    /**
     * Construye el árbol nivel por nivel, generando en paralelo los predecesores de cada nivel.
     * @return todos los números alcanzados
     */
    public Set<Long> buildTreeParallel() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            while (!searchQueue.isEmpty()) {
                List<Future<List<TreeNode>>> futures = new ArrayList<>();
                List<TreeNode> currentLevel = new ArrayList<>();

                
                while (!searchQueue.isEmpty()) {
                    TreeNode node = searchQueue.poll();
                    if (node.depth() < maxDepth) {
                        currentLevel.add(node);
                    }
                }

                if (currentLevel.isEmpty()) {
                    break;
                }

               
                for (TreeNode node : currentLevel) {
                    futures.add(executor.submit(() -> generatePredecessors(node)));
                }

                
                for (Future<List<TreeNode>> future : futures) {
                    try {
                        List<TreeNode> newNodes = future.get();
                        for (TreeNode newNode : newNodes) {
                            exploredNumbers.add(newNode.value());
                            searchQueue.offer(newNode);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println("Error generating predecessors: " + e.getMessage());
                    }
                }
                
                System.out.printf("Depth %d completed. Numbers explored: %,d%n",
                        currentLevel.get(0).depth(), exploredNumbers.size());
            }
        }

        return new HashSet<>(exploredNumbers);
    }

    /**
     * Números del rango que no están en el árbol.
     * @param rangeStart inicio (inclusive)
     * @param rangeEnd fin (inclusive)
     */
    public List<Long> findMissingNumbers(long rangeStart, long rangeEnd) {
        List<Long> missingNumbers = new ArrayList<>();

        for (long i = rangeStart; i <= rangeEnd; i++) {
            if (!exploredNumbers.contains(i)) {
                missingNumbers.add(i);
            }
        }

        return missingNumbers;
    }
}
