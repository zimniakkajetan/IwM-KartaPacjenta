package sample;

import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import com.jfoenix.controls.JFXRippler;
import javafx.scene.layout.HBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChartsController {
    private List<Observation> observations = new ArrayList<Observation>();
    private Patient patient;
    public ChartsController(){

    }

    public ChartsController(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){
        this.observations = observations;
        this.patient = patient;

        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "", // Chart
                "", // X-Axis Label
                "", // Y-Axis Label
                dataset);

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(128, 191, 255));

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout(new BorderLayout());
        jPanel4.add(chartPanel, BorderLayout.NORTH);

        JFrame frame = new JFrame();
        frame.add(jPanel4);
        frame.pack();
        frame.setVisible(true);

        //ChartViewer viewer = new ChartViewer(chart);
        //System.out.println(viewer+"\n" + chartbox1+"\n");
        //JFXRippler rippler = new JFXRippler(viewer);
        //textFirstNamee.setText("aaaaaaaaaaaa");
        //chartbox1.getChildren().add(rippler);
    }

    private XYDataset createDataset() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        TimeSeries series1 = new TimeSeries("Series1");
        for(int i=0;i<observations.size();i++){
            if(getObservationDescription(observations.get(i)).contains("Heart")) {
                int a = observations.get(i).getMeta().getLastUpdated().getMinutes() + i;
                int b = observations.get(i).getMeta().getLastUpdated().getHours();
                int c = observations.get(i).getMeta().getLastUpdated().getDay();
                int d = observations.get(i).getMeta().getLastUpdated().getMonth();
                int e = observations.get(i).getMeta().getLastUpdated().getYear();
                series1.add(new Minute(a, b, c, d, 2018), 1.2);
            }
        }

        dataset.addSeries(series1);

        return dataset;
    }
    private String getObservationDescription(Observation observation) {
        String description = observation.getText().getDivAsString();
        if (description != null && description.contains("'>") && description.contains("</div")) {
            description = description.substring(description.lastIndexOf("'>") + 2, description.lastIndexOf("</div"));
        }
        return description == null ? "" : description;
    }

}
