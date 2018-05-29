package sample;

import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTabPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import nl.itopia.corendon.components.DateAxis;

public class ChartsController {
    private List<Observation> observations = new ArrayList<Observation>();
    private Map<String,ArrayList<Observation>> dataMap=new HashMap<>();

    private Patient patient;
    LocalDate dateBegin;
    LocalDate dateEnd;
    @FXML
    JFXTabPane tabPane;


    public ChartsController(){
    }

    public ChartsController(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){
        this.observations = observations;
        this.patient = patient;
        this.dateBegin=dateBegin;
        this.dateEnd=dateEnd;
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
                ScatterChart<Date,Number> sc=createChart(dataArray,null,null);
                VBox vbox = new VBox();
                HBox hbox = new HBox();
                JFXDatePicker datePickerBegin=new JFXDatePicker();
                JFXDatePicker datePickerEnd = new JFXDatePicker();
                DateConverter converter = new DateConverter();
                datePickerBegin.setConverter(converter);
                datePickerEnd.setConverter(converter);
                datePickerBegin.setPromptText("Begin date");
                datePickerEnd.setPromptText("End date");
                datePickerBegin.setValue(dateBegin);
                datePickerEnd.setValue(dateEnd);
                hbox.getChildren().addAll(datePickerBegin,datePickerEnd);
                hbox.setPadding(new Insets(10));
                hbox.setAlignment(Pos.BOTTOM_RIGHT);
                hbox.setSpacing(8);
                hbox.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY,Insets.EMPTY)));
                vbox.getChildren().addAll(hbox,sc);
                tab.setContent(vbox);

                datePickerBegin.setOnAction(event -> {
                    filterByDate(event);
                });
                datePickerEnd.setOnAction(event->{
                   filterByDate(event);
                });
            }
        }
    }

    private ScatterChart<Date,Number> createChart(ArrayList<Observation> dataArray,Date dateBegin, Date dateEnd){
        ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

        ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

        if (dateBegin == null) {
            dateBegin = new Date(Long.MIN_VALUE);
        }
        if (dateEnd == null) {
            dateEnd = new Date(Long.MAX_VALUE);
        }

        for(Observation observation : dataArray){
            Date date = ((DateTimeDt)observation.getEffective()).getValue();
            if(date.getTime()<dateBegin.getTime() || date.getTime()>dateEnd.getTime())continue;
            QuantityDt quantity = (QuantityDt)observation.getValue();
            Number value = quantity.getValueElement().getValueAsNumber();
            seriesData.add(new XYChart.Data<Date,Number>(date,value));
        }

        series.add(new XYChart.Series<>("Series", seriesData));

        NumberAxis numberAxis = new NumberAxis();
        DateAxis dateAxis = new DateAxis();

        ScatterChart<Date,Number> sc = new ScatterChart<Date,Number>(dateAxis,numberAxis);
        sc.setLegendVisible(false);
        sc.getData().setAll(series);
        sc.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        sc.setPadding(new Insets(24,24,24,24));
        return sc;
    }

    private void filterByDate(ActionEvent event){
        HBox hbox = (HBox)((Node)event.getSource()).getParent();
        VBox vbox = (VBox)hbox.getParent();
        ScatterChart<Date,Number> sc=(ScatterChart<Date,Number>)vbox.getChildren().get(1);
        DateAxis dateAxis = (DateAxis) sc.getXAxis();

        JFXDatePicker datePickerBegin = (JFXDatePicker)hbox.getChildren().get(0);
        JFXDatePicker datePickerEnd = (JFXDatePicker)hbox.getChildren().get(1);

        LocalDate localDateBegin = datePickerBegin.getValue();
        LocalDate localDateEnd = datePickerEnd.getValue();
        Date dateBegin = localDateBegin==null? new Date(Long.MIN_VALUE) : Date.from(localDateBegin.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date dateEnd = localDateEnd==null? new Date(Long.MAX_VALUE) : Date.from(localDateEnd.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        String key = tab.getText();
        if(key!=null && dataMap.get(key)!=null){
            vbox.getChildren().set(1,createChart(dataMap.get(key),dateBegin,dateEnd));
        }

    }
}
