package sample;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.awt.*;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class Controller {
    IGenericClient client;

    public Controller(IGenericClient client) {
        this.client = client;
    }

    public void init() {
        comboBox.getItems().add(new Label("All"));
        comboBox.getItems().add(new Label("First Name"));
        comboBox.getItems().add(new Label("Last Name"));
        comboBox.getSelectionModel().select(0);

        JFXTreeTableColumn<TablePatient, String> nameColumn = new JFXTreeTableColumn<>("First Name");
        nameColumn.setPrefWidth(260);
        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<TablePatient, String> param) -> {
            if (nameColumn.validateValue(param)) return param.getValue().getValue().name;
            else return nameColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<TablePatient, String> lastNameColumn = new JFXTreeTableColumn<>("Last Name");
        lastNameColumn.setPrefWidth(260);
        lastNameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<TablePatient, String> param) -> {
            if (lastNameColumn.validateValue(param)) return param.getValue().getValue().lastName;
            else return lastNameColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<TablePatient, String> ageColumn = new JFXTreeTableColumn<>("Age");
        ageColumn.setPrefWidth(260);
        ageColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<TablePatient, String> param) -> {
            if (ageColumn.validateValue(param)) return param.getValue().getValue().age;
            else return ageColumn.getComputedValue(param);
        });

        patients = FXCollections.observableArrayList();
// build tree
        final TreeItem<TablePatient> root = new RecursiveTreeItem<TablePatient>(patients, RecursiveTreeObject::getChildren);
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.getColumns().setAll(nameColumn, lastNameColumn, ageColumn);
        treeView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    JFXTextField searchfield;
    @FXML
    JFXComboBox comboBox;
    @FXML
    JFXTreeTableView<TablePatient> treeView;

    ObservableList<TablePatient> patients;

    @FXML
    protected void search() {
        if (!searchfield.getText().isEmpty()) {
            // Perform a search
            new Thread(() -> {
                Bundle results = client
                        .search()
                        .forResource(Patient.class)
                        .where(Patient.FAMILY.matches().value(searchfield.getText().toString()))
                        .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                        .execute();

                System.out.println("Found " + results.getEntry().size() + " patients named " + searchfield.getText().toString());
                Platform.runLater(() -> {
                    fillNameListView(results, searchfield.getText().toString());
                });
            }).start();

        }
    }

    public void fillNameListView(final Bundle results, String name) {
        patients.clear();
        for (Bundle.Entry element : results.getEntry()) {
            Patient patient = (Patient) element.getResource();
            if (!patient.getName().get(0).getGiven().isEmpty()) {
                patients.add(new TablePatient(patient));
            }
            System.out.println(patient.getName().get(0).getGiven());
        }

        treeView.setOnMouseClicked(event -> {
            if (treeView.getSelectionModel().getSelectedItem() == null) return;
            Patient clickedPatient = treeView.getSelectionModel().getSelectedItem().getValue().patient;
            if (clickedPatient != null)
                Main.changeView("patient", clickedPatient);
        });
    }

    class TablePatient extends RecursiveTreeObject<TablePatient> {
        StringProperty name;
        StringProperty lastName;
        StringProperty age;
        Patient patient;

        public TablePatient(Patient patient) {
            this.patient = patient;
            name = new SimpleStringProperty(patient.getName().get(0).getGivenAsSingleString());
            lastName = new SimpleStringProperty(patient.getName().get(0).getFamilyAsSingleString() + " id:" + patient.getId().getIdPart());
            String ageString = "Unknown";
            if (patient.getBirthDate() != null) {
                ageString = String.valueOf(Period.between(patient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).getYears());
            }
            age = new SimpleStringProperty(ageString);
        }
    }

}
