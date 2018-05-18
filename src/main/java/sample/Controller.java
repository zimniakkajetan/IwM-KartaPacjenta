package sample;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Controller {
    IGenericClient client;

    public Controller (IGenericClient client){
        this.client = client;
    }

    @FXML
    TextField searchfield;
    @FXML
    ListView name_listview;

    @FXML
    protected void search() {
        if(!searchfield.getText().isEmpty()){
            //System.out.println(searchfield.getText());
            // Perform a search
            Bundle results = client
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.FAMILY.matches().value(searchfield.getText().toString()))
                    .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                    .execute();

            System.out.println("Found " + results.getEntry().size() + " patients named " + searchfield.getText().toString());
            fillNameListView(results, searchfield.getText().toString());
        }
    }
    public void fillNameListView(final Bundle results, String name){
        ObservableList observableList = FXCollections.observableArrayList();
        for (Bundle.Entry element : results.getEntry()) {
            Patient patient = (Patient) element.getResource();
            if(!patient.getName().get(0).getGiven().isEmpty()) {
                observableList.add(patient.getName().get(0).getGiven() + " " + name);
            }
            System.out.println(patient.getName().get(0).getGiven());
        }
        name_listview.setItems(observableList);
        Main.changeView(2);
        name_listview.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                System.out.println("clicked on " + name_listview.getSelectionModel().getSelectedItem());
                //Patient patient = (Patient) results.getEntry().get(name_listview.).getResource();
                Main.changeView(1);
            }
        });
    }
}
