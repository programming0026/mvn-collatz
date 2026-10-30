package com.juniverse.collatz.dto;

/**
 * Nodo del árbol inverso de Collatz.
 *
 * @param value número del nodo
 * @param depth profundidad (pasos hasta 1)
 * @param parent nodo sucesor (hacia el 1)
 * @param operationType "even" si viene de 2n, "odd" si viene de (n − 1)/3, "root" para el 1
 *
 * @author ajuar
 */
public record TreeNode(long value,
        int depth,
        TreeNode parent,
        String operationType) {

    /** Representación legible del nodo. */
    @Override
    public String toString() {
        return String.format("TreeNode{value=%d, depth=%d, op=%s}",
                value, depth, operationType);
    }
}
