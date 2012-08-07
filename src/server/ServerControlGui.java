package server;

import common.Configuration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Second;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ServerControlGui {
    public static final int MAXIMUM_ITEM_AGE = 5 * 60;
    public static final int MAX_CLIENTS = 500;
    private JFrame mainFrame;
    private boolean noGui = false;
    private JLabel numPlayersLabel;
    private JLabel messageQueueLabel;
    private JLabel outMessageQueueLabel;
    private DecimalFormat decimalformat;
    private JLabel netOutMessageQueueLabel;
    private JComponent numPlayersSliderPanel;

    /**
     * Dummy main for testing purposes.
     */
    public static void main(String[] args) {
        ServerControlGui serverControlGui = new ServerControlGui();
        serverControlGui.mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        serverControlGui.open();
    }

    public ServerControlGui() {
        noGui = !Configuration.getInstance().showGui();
        if (noGui)
            return;
        try {
            mainFrame = new JFrame("Server control panel");
            mainFrame.setSize(700, 600);
        } catch (HeadlessException e) {
            System.out.println("No graphical display available. ");
            noGui = true;
        }
        numPlayersSliderPanel = createSliderPanel("Number of simulated clients:", 0, MAX_CLIENTS, 0, new DelayedSliderListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ServerControlGui.this.setNumPlayers();
            }
        }));
    }

    public void open() {
        if (noGui) {
            return;
        }
        JPanel mainPanel = new JPanel();
        mainFrame.getContentPane().add(mainPanel);
        mainPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(createHeadline(), BorderLayout.NORTH);
        mainPanel.add(createPerformancePanel(), BorderLayout.CENTER);
        mainPanel.add(createSouthPanel(), BorderLayout.SOUTH);

        mainFrame.setVisible(true);
    }

    private Component createSouthPanel() {
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(createControlPanel());
        southPanel.add(createStatisticPanel());
        return southPanel;

    }

    private Component createStatisticPanel() {
        JPanel statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new BoxLayout(statisticsPanel, BoxLayout.X_AXIS));
        numPlayersLabel = createLabelAndNumber(statisticsPanel, "Number of players:");
        statisticsPanel.add(Box.createHorizontalStrut(20));
        messageQueueLabel = createLabelAndNumber(statisticsPanel, "Character in message queue size:");
        statisticsPanel.add(Box.createHorizontalStrut(20));
        outMessageQueueLabel = createLabelAndNumber(statisticsPanel, "Character out message queue size::");
        statisticsPanel.add(Box.createHorizontalStrut(20));
        netOutMessageQueueLabel = createLabelAndNumber(statisticsPanel, "Network out message queue size::");
        statisticsPanel.add(Box.createHorizontalStrut(Integer.MAX_VALUE));
        return statisticsPanel;
    }

    private JLabel createLabelAndNumber(JPanel statisticsPanel, String text) {
        JPanel labelAndNumberPanel = new JPanel();
        labelAndNumberPanel.setLayout(new BoxLayout(labelAndNumberPanel, BoxLayout.Y_AXIS));
        labelAndNumberPanel.add(new JLabel(text));
        JLabel number = new JLabel("0");
        labelAndNumberPanel.add(number);
        statisticsPanel.add(labelAndNumberPanel);
        return number;
    }

    private JComponent createHeadline() {
        JLabel headline = new JLabel("<html><h1>LEARS Server Control Panel</h1></html>");
        headline.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return headline;
    }

    private JComponent createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(createSliderPanel("Synthetic load iterations:", 0, 1000000, Configuration.getInstance().getLoadIntensity(), new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ServerMain.setSynthLoad(((JSlider) e.getSource()).getValue());
            }
        }));
        controlPanel.add(createSliderPanel("Number of threads:", 1, 128, Configuration.getInstance().getNumThreads(), new DelayedSliderListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ServerMain.setNumthreads(Integer.parseInt(e.getActionCommand()));
            }
        })));
        controlPanel.add(numPlayersSliderPanel);
        controlPanel.add(createSliderPanel("Number of NPCs:", 0, 500, 0, new DelayedSliderListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ServerMain.setNumNPCs(Integer.parseInt(e.getActionCommand()));
            }
        })));
        return controlPanel;
    }

    private void setNumPlayers() {
        final JSlider slider = (JSlider) numPlayersSliderPanel.getComponent(1);
        if (ServerMain.getSimulatorSize() == 0) {
            slider.setValue(0);
            return;
        }
        int prevSize = GameWorld.getInstance().getPlayerCharacters().size();
        ServerMain.setNumClients(slider.getValue());
        slider.setEnabled(false);
        Timer timer = new Timer((Configuration.getInstance().getLogonDelay() * (slider.getValue() - prevSize) / ServerMain.getSimulatorSize()), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slider.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }


    private JComponent createSliderPanel(String headline, int min, int max, int value, ChangeListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new Label(headline));
        final JSlider slider = new JSlider(min, max, value);
        panel.add(slider);
        final JLabel valueLabel = new JLabel("" + slider.getValue());
        panel.add(valueLabel);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                valueLabel.setText("" + slider.getValue());
            }
        });
        slider.addChangeListener(listener);
//        panel.setBorder(new EmptyBorder(10, 20, 30, 20));
        return panel;
    }

    private JComponent createPerformancePanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

        final YIntervalSeries cpuTimeSeries = createYIntervalSeries("CPU Usage");
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        ChartPanel cpuChartPanel = creatLineChart(cpuTimeSeries, "CPU Usage", "CPU Usage fraction 100%=one core", "##0%", new Range(0, nrOfProcessors));
        centerPanel.add(cpuChartPanel);


        final YIntervalSeries delaySeries = createYIntervalSeries("Average delay");
        ChartPanel delayChartPanel = creatLineChart(delaySeries, "Delay", "ms", "### ##0", new Range(0, 3000));
        centerPanel.add(delayChartPanel);

        ServerMain.getStatisticsSystem().addServerStatListener(new ServerStatListener() {
            @Override
            public void newCPUDataEvent(double cpuLoadValue) {
                cpuTimeSeries.add(new Second().getFirstMillisecond(), cpuLoadValue, cpuLoadValue, cpuLoadValue);
            }

            @Override
            public void newDelayDataEvent(double delayAvg, double delayMax, double delayMin, double messageQueueSizeAvg, double networkQueueSizeSum, int numPlayers, int queueLengthAcc) {
                delaySeries.add(new Second().getFirstMillisecond(), delayAvg, delayMin, delayMax);
                if (messageQueueLabel == null) {
                    return;//We got a message before everything was ready
                }
                messageQueueLabel.setText(decimalformat.format(messageQueueSizeAvg));
                outMessageQueueLabel.setText(decimalformat.format(networkQueueSizeSum));
                numPlayersLabel.setText("" + numPlayers);
                netOutMessageQueueLabel.setText("" + queueLengthAcc);

            }

        });
        return centerPanel;
    }

    private YIntervalSeries createYIntervalSeries(String name) {
        final YIntervalSeries series = new YIntervalSeries(name);
        //Fill with dummy data
        long now = System.currentTimeMillis();
        series.setMaximumItemCount(MAXIMUM_ITEM_AGE);
        for (int i = MAXIMUM_ITEM_AGE; i > 0; i--) {
            series.add(new Second(new Date(now - i * 1000)).getFirstMillisecond(), 0, 0, 0);
        }
        return series;
    }

    private ChartPanel creatLineChart(YIntervalSeries timeseries, String title, String valueAxisLabel, String decimalFormat, Range range) {
        YIntervalSeriesCollection timeseriescollection = new YIntervalSeriesCollection();
        timeseriescollection.addSeries(timeseries);
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, "Time", valueAxisLabel, timeseriescollection, true, true, false);
        jfreechart.setBackgroundPaint(mainFrame.getBackground());
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.lightGray);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);
        DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
        deviationrenderer.setSeriesStroke(0, new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        deviationrenderer.setSeriesFillPaint(0, new Color(255, 200, 200));
        deviationrenderer.setBaseToolTipGenerator(null);
        xyplot.setRenderer(deviationrenderer);
        NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        decimalformat = new DecimalFormat(decimalFormat);
        numberaxis.setNumberFormatOverride(decimalformat);
        numberaxis.setRange(range);
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
        dateaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        return new ChartPanel(jfreechart, false);
    }

    private static class DelayedSliderListener implements ChangeListener {
        private Timer timer;

        private DelayedSliderListener(ActionListener listener) {
            timer = new Timer((int) TimeUnit.SECONDS.toMillis(3), listener);
            timer.setRepeats(false);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            timer.restart();
            int lastSliderValue = ((JSlider) e.getSource()).getValue();
            timer.setActionCommand("" + lastSliderValue);
        }
    }
}
