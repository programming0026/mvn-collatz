package com.juniverse.collatz;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Ventana con una gráfica (JFreeChart) de la secuencia de Collatz de varios números iniciales.
 *
 * @author ajuar
 */
public class CollatzGraphic extends JFrame {

    /**
     * Crea la ventana con una serie por cada número inicial.
     * @param starts números iniciales (enteros positivos)
     */
    public CollatzGraphic(List<BigInteger> starts) {
        setTitle("Conjetura de Collatz");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JFreeChart chart = ChartFactory.createLineChart(
                "Conjetura de Collatz",
                "Paso",
                "Valor",
                dataset
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE};
        for (int s = 0; s < starts.size(); s++) {
            BigInteger start = starts.get(s);
            List<BigInteger> sequence =  CollatzConjecture.generateCollatzSequence(start);
            // una serie por número inicial: color y grosor por serie, no por punto
            renderer.setSeriesPaint(s, colors[s % colors.length]);
            renderer.setSeriesStroke(s, new BasicStroke(2.0f));
            for (int i = 0; i < sequence.size(); i++) {
                dataset.addValue(sequence.get(i).intValue(), "Inicio en " + start,
                        Integer.toString(i));
            }
        }
        renderer.setDefaultToolTipGenerator((CategoryDataset categoryDataset, int row, int column) -> {
            Number value = categoryDataset.getValue(row, column);
            String paso = (String) categoryDataset.getColumnKey(column);
            return "Valor: " + value.intValue() + " - Paso: " + paso;
        });

        plot.setRenderer(renderer);
        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

   

    /**
     * Muestra la gráfica de dos números aleatorios entre 10 y 99.
     * @param args no se usan
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<BigInteger> numbers = new ArrayList<>();
            Random random = new Random();
            for (int i = 1; i <= 2; i++) {
                int value = random.nextInt(10, 100);
                System.out.println("Value: " + value);
                numbers.add(new BigInteger(String.valueOf(value)));
            }
            CollatzGraphic frame = new CollatzGraphic(numbers);
            frame.setVisible(true);
        });
    }

}
