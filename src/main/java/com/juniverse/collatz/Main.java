package com.juniverse.collatz;

/**
 * Punto de entrada: lanza las demostraciones de la conjetura de Collatz.
 *
 * @author linux
 */
public class Main {

    private Main() {
    }

    /**
     * Ejecuta las cuatro demostraciones.
     * @param args no se usan
     */
    public static void main(String[] args) {
        CollatzDemos.runAll();
    }
}
