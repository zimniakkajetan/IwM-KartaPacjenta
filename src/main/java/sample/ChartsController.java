package sample;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import com.jfoenix.controls.JFXTabPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;

import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class ChartsController {
    private List<Observation> observations = new ArrayList<Observation>();
    private List<Observation> filteredObservations = new ArrayList<Observation>();
    private Map<String,ArrayList<Observation>> dataMap=new HashMap<>();

    private Patient patient;
    @FXML
    JFXTabPane tabPane;


    public ChartsController(){
    }

    public ChartsController(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){
        this.observations = observations;
        this.patient = patient;
    }

    public void drawCharts(){
        retrieveData();
        createCharts();
    }

    private void retrieveData(){
        observations.forEach(observation -> {
            String key=observation.getCode().getText();
            if(dataMap.get(key)==null){
                dataMap.put(key,new ArrayList<>(Arrays.asList(observation)));
            }else{
                dataMap.get(key).add(observation);
            }
        });
    }

    private void createCharts(){
        for(Tab tab : tabPane.getTabs()){
            ArrayList<Observation> dataArray = dataMap.get(tab.getText());
            if(dataArray!=null){
                ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

                ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

                for(Observation observation : dataArray){
                    Date date = ((DateTimeDt)observation.getEffective()).getValue();
                    QuantityDt quantity = (QuantityDt)observation.getValue();
                    Number value = quantity.getValueElement().getValueAsNumber();
                    seriesData.add(new XYChart.Data<Date,Number>(date,value));
                }

                series.add(new XYChart.Series<>("Series", seriesData));

                NumberAxis numberAxis = new NumberAxis();
                nl.itopia.corendon.components.DateAxis dateAxis = new nl.itopia.corendon.components.DateAxis();
                ScatterChart<Date,Number> sc = new ScatterChart<Date,Number>(dateAxis,numberAxis);
                sc.getData().setAll(series);

                tab.setContent(sc);
            }
        }
    }

}
