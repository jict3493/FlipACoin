package com.simu.flipacoin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.AttributedString;
import java.util.List;
import java.util.Random;

public class FlipACoin extends JFrame {
    private DefaultPieDataset dataset;
    private int caras = 0;
    private int cruces = 0;
    private int lanzamientos = 0;
    private final JLabel caraLabel = new JLabel("Cara");
    private final JLabel cruzLabel = new JLabel("Cruz");
    private JTextField vecesALanzar;
    private TextTitle contadorTitle;
    private JButton[] botones;

    public FlipACoin() {
        setTitle("Flip-A-Coin");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        dataset = new DefaultPieDataset();
        dataset.setValue("Cara", 1);
        dataset.setValue("Cruz", 1);

        JFreeChart chart = ChartFactory.createPieChart("Resultados", dataset, true, false, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new PieSectionLabelGenerator() {
            @Override
            public String generateSectionLabel(PieDataset dataset, Comparable key) {
                int count = key.equals("Cara") ? caras : cruces;
                double porcentaje = ((double) count / (caras + cruces)) * 100.0;

                if (Double.isNaN(porcentaje)) {
                    porcentaje = 50;
                }

                return String.format("%d veces (%.2f%%)", count, porcentaje);
            }

            @Override
            public AttributedString generateAttributedSectionLabel(PieDataset pieDataset, Comparable comparable) {
                return null;
            }
        });

        contadorTitle = new TextTitle("Lanzamientos: 0");
        contadorTitle.setPosition(RectangleEdge.TOP);
        contadorTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chart.addSubtitle(contadorTitle);

        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton lanzar10Button = new JButton("10");
        JButton lanzar100Button = new JButton("100");
        JButton lanzar1000Button = new JButton("1000");
        vecesALanzar = new JTextField("1", 5);
        JButton lanzarButton = new JButton("Lanzar");

        controlPanel.add(lanzar10Button);
        controlPanel.add(lanzar100Button);
        controlPanel.add(lanzar1000Button);
        controlPanel.add(vecesALanzar);
        controlPanel.add(lanzarButton);
        controlPanel.add(caraLabel);
        controlPanel.add(cruzLabel);
        add(controlPanel, BorderLayout.SOUTH);

        botones = new JButton[]{lanzarButton,lanzar10Button,lanzar100Button,lanzar1000Button};

        ActionListener vecesButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton clickedButton = (JButton) e.getSource();
                vecesALanzar.setText(clickedButton.getText());
            }
        };
        lanzar10Button.addActionListener(vecesButtonListener);
        lanzar100Button.addActionListener(vecesButtonListener);
        lanzar1000Button.addActionListener(vecesButtonListener);

        lanzarButton.addActionListener(e -> lanzarMoneda(Integer.parseInt(vecesALanzar.getText())));
        setVisible(true);
    }

    private void lanzarMoneda(int veces) {
        setBotonesHabilitados(false);

        SwingWorker<Void,Integer> worker = new SwingWorker<Void, Integer>() {
            Random random = new Random();

            @Override
            protected Void doInBackground() throws Exception {
                long sleepTime = (long) (1000 * (Math.sqrt(Math.sin(Math.PI * (Math.log10(veces) / 4)))) / (5 * Math.log10(veces)));
                for (int i = 0; i < veces; i++) {
                    boolean esCara = random.nextBoolean();

                    lanzamientos++;

                    if(esCara) {
                        caras++;
                        caraLabel.setEnabled(true);
                    } else {
                        cruces++;
                        cruzLabel.setEnabled(true);
                    }

                    publish(lanzamientos);

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ignored) {}
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                caraLabel.setEnabled(false);
                cruzLabel.setEnabled(false);
                actualizarGrafico();
            }

            @Override
            protected void done() {
                setBotonesHabilitados(true);
            }
        };

        worker.execute();
    }

    private void actualizarGrafico() {
        int total = caras + cruces;
        double porcentajeCaras = (double) (caras * 100) / total;
        double porcentajeCruces = (double) (cruces * 100) / total;

        dataset.setValue("Cara", porcentajeCaras);
        dataset.setValue("Cruz", porcentajeCruces);
        contadorTitle.setText("Lanzamientos: " + lanzamientos);
    }

    public void setBotonesHabilitados(boolean habilitados) {
        for(JButton boton : botones) {
            boton.setEnabled(habilitados);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FlipACoin::new);
    }
}