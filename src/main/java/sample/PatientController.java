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
import com.sun.org.apache.bcel.internal.classfile.Code;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PatientController {
    IGenericClient client;
    private Patient patient;
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
    HBox topHBox;
    @FXML
    JFXButton backButton;


    @FXML
    Text textPatientName;

    public PatientController(IGenericClient client) {
        this.client = client;
    }

    public void initData(Object... params) {
        backButton.setPickOnBounds(true);
        textAreaPatientInfo.requestFocus();
        
        textAreaPatientInfo.clear();
        textAreaPatientMedications.clear();
        textAreaPatientObservations.clear();
        textAreaPatientMedicationStatements.clear();

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
                        observations.add((Observation) resource);
                    } else if (resource instanceof Medication) {
                        medications.add((Medication) resource);
                    } else if (resource instanceof MedicationStatement) {
                        medicationStatements.add((MedicationStatement) resource);
                    }
                }

                sortArrays();
                displayData();

            }
        }).start();
    }

    private void sortArrays() {
        Collections.sort(observations, new Comparator<Observation>() {
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
                textAreaPatientInfo.appendText("Observations: " + observations.size() + "\n");
                textAreaPatientInfo.appendText("Medications: " + medications.size() + "\n");
                textAreaPatientInfo.appendText("MedicationStatements: " + medicationStatements.size() + "\n");
                for (Observation observation : observations) {
                    textAreaPatientObservations.appendText(observation.getMeta().getLastUpdated() + " " + getObservationDescription(observation) + "\n");
                }
                for(Medication medication : medications){
                    textAreaPatientMedications.appendText(medication.getText()+"\n");
                }
                for(MedicationStatement mStatement : medicationStatements){
                    CodeableConceptDt medication =null;
                    if(mStatement.getMedication() instanceof CodeableConceptDt){
                        medication= ((CodeableConceptDt)mStatement.getMedication());
                    }

                    textAreaPatientMedicationStatements.appendText((medication!=null? medication.getText() : "") + " -> " +mStatement.getDosage().get(0).getText()+"\n");
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
    private void goBack(){
        Main.changeView("main",null);
    }

}
