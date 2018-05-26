package sample;

import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXRippler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class ChartsController {
    private List<Observation> observations = new ArrayList<Observation>();
    private List<Observation> filteredObservations = new ArrayList<Observation>();

    private Patient patient;
    @FXML
    JFXDatePicker datePickerBegin;
    @FXML
    JFXDatePicker datePickerEnd;

    public ChartsController(){
    }

    public ChartsController(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){
        this.observations = observations;
        this.patient = patient;
        this.filteredObservations = observations;
        createChartHeart();

    }
    @FXML
    private void filterByDate() {
        LocalDate dateBegin = datePickerBegin.getValue();
        if(dateBegin != null) {
            dateBegin = dateBegin.plusDays(-1);
        }
        LocalDate dateEnd = datePickerEnd.getValue();
        if(dateEnd != null){
            dateEnd = dateEnd.plusDays(1);
        }
        if (dateBegin == null) {
            dateBegin = new Date(Long.MIN_VALUE).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (dateEnd == null) {
            dateEnd = new Date(Long.MAX_VALUE).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        filteredObservations.clear();
        for (Observation observation : observations) {
            if (observation.getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(dateBegin)
                    && observation.getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(dateEnd)) {
                filteredObservations.add(observation);
            }
        }
        createChartHeart();
    }
    private void createChartHeart(){
        XYDataset dataset = createDatasetHeart();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "", // Chart
                "", // X-Axis Label
                "Heart rate", // Y-Axis Label
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
    private XYDataset createDatasetHeart() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        TimeSeries series1 = new TimeSeries("Heart rate");
        for(int i=0;i<filteredObservations.size();i++){
            String descr = getObservationDescription(filteredObservations.get(i)).toString();

            if(descr.contains("Heart")) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filteredObservations.get(i).getMeta().getLastUpdated());
                int a = cal.get(Calendar.MINUTE) + i; //TU TRZEBA ZMIENIC W PRZYPADKU POBIERANIA ODPOWIEDNICH DANYCH
                int b = cal.get(Calendar.HOUR);
                int c = cal.get(Calendar.DAY_OF_MONTH);
                int d = cal.get(Calendar.MONTH);
                int e = cal.get(Calendar.YEAR);
                String chartValue = descr.replaceAll("[^-?0-9]+", " ");
                //System.out.println(Arrays.asList(chartValue.trim().split(" ")) + "\n");
                Float value = Float.valueOf(Arrays.asList(chartValue.trim().split(" ")).get(0));

                series1.add(new Minute(a,b,c,d,e), value);
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
