package sample;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTabPane;
import com.sun.org.apache.bcel.internal.classfile.Code;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class PatientController {
    IGenericClient client;
    private Patient patient;
    private ArrayList<Observation> allObservations = new ArrayList<Observation>();
    private ArrayList<Observation> observations = new ArrayList<Observation>();
    private ArrayList<Medication> medications = new ArrayList<Medication>();
    private ArrayList<MedicationStatement> medicationStatements = new ArrayList<MedicationStatement>();

    @FXML
    TextArea textAreaPatientInfo;
    @FXML
    TextArea textAreaPatientObservations;
    @FXML
    TextArea textAreaPatientMedicationStatements;
    @FXML
    TextArea textAreaPatientMedications;
    @FXML
    JFXTabPane tabPane;
    @FXML
    JFXButton backButton;
    @FXML
    Text textPatientName;
    @FXML
    JFXDatePicker datePickerBegin;
    @FXML
    JFXDatePicker datePickerEnd;


    public PatientController(IGenericClient client) {
        this.client = client;
    }

    public void initData(Object... params) {
        resetValues();
        backButton.setPickOnBounds(true);

        Region icon = new Region();
        icon.getStyleClass().add("icon");
        backButton.setGraphic(icon);

        patient = (Patient) params[0];
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textPatientName.setText(patient.getName().get(0).getGivenAsSingleString() + " " + patient.getName().get(0).getFamilyAsSingleString());
            }
        });
        getPatientData();
    }

    private void getPatientData() {
        final String id = patient.getId().getIdPart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle results = client
                        .search()
                        .forResource(Patient.class)
                        .where(new StringClientParam("_id").matchesExactly().value(id))
                        .revInclude(new Include("*"))
                        .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                        .execute();
                for (final Bundle.Entry element : results.getEntry()) {
                    IResource resource = element.getResource();
                    if (resource instanceof Observation) {
                        allObservations.add((Observation) resource);
                    } else if (resource instanceof Medication) {
                        medications.add((Medication) resource);
                    } else if (resource instanceof MedicationStatement) {
                        medicationStatements.add((MedicationStatement) resource);
                    }
                }

                sortArrays();
                observations = new ArrayList<>(allObservations);
                displayData();

            }
        }).start();
    }

    private void sortArrays() {
        Collections.sort(allObservations, new Comparator<Observation>() {
            @Override
            public int compare(Observation o1, Observation o2) {
                return o1.getMeta().getLastUpdated().compareTo(o2.getMeta().getLastUpdated());
            }
        });
    }

    private void displayData() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textAreaPatientObservations.clear();


                textAreaPatientInfo.appendText("Observations: " + observations.size() + "\n");
                textAreaPatientInfo.appendText("Medications: " + medications.size() + "\n");
                textAreaPatientInfo.appendText("MedicationStatements: " + medicationStatements.size() + "\n");
                for (Observation observation : observations) {
                    textAreaPatientObservations.appendText(observation.getMeta().getLastUpdated() + " " + getObservationDescription(observation) + "\n");
                }
                for (Medication medication : medications) {
                    textAreaPatientMedications.appendText(medication.getText() + "\n");
                }
                for (MedicationStatement mStatement : medicationStatements) {
                    CodeableConceptDt medication = null;
                    if (mStatement.getMedication() instanceof CodeableConceptDt) {
                        medication = ((CodeableConceptDt) mStatement.getMedication());
                    }

                    textAreaPatientMedicationStatements.appendText((medication != null ? medication.getText() : "") + " -> " + mStatement.getDosage().get(0).getText() + "\n");
                }
            }
        });
    }

    private String getObservationDescription(Observation observation) {
        String description = observation.getText().getDivAsString();
        if (description != null && description.contains("'>") && description.contains("</div")) {
            description = description.substring(description.lastIndexOf("'>") + 2, description.lastIndexOf("</div"));
        }
        return description == null ? "" : description;
    }

    @FXML
    private void goBack() {
        Main.changeView("main", null);
    }

    @FXML
    private void filterByDate() {
        LocalDate dateBegin = datePickerBegin.getValue();
        LocalDate dateEnd = datePickerEnd.getValue();
        observations.clear();
        for (Observation observation : allObservations) {
            if (dateBegin == null) {
                dateBegin = new Date(Long.MIN_VALUE).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (dateEnd == null) {
                dateEnd = new Date(Long.MAX_VALUE).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (observation.getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(dateBegin)
                    && observation.getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(dateEnd)) {
                observations.add(observation);
            }
        }
        displayData();
    }

    private void resetValues(){
        tabPane.getSelectionModel().select(0);
        textAreaPatientInfo.requestFocus();
        datePickerBegin.setValue(null);
        datePickerEnd.setValue(null);
        textAreaPatientInfo.clear();
        textAreaPatientMedications.clear();
        textAreaPatientObservations.clear();
        textAreaPatientMedicationStatements.clear();
    }

    }
