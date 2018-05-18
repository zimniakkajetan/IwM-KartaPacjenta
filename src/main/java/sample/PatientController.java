package sample;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class PatientController {
    IGenericClient client;
    private Patient patient;

    @FXML
    TextArea textAreaPatientInfo;

    public PatientController(IGenericClient client) {
        this.client = client;
    }

    public void initData(Object... params){
        patient = (Patient) params[0];
        getPatientData();
    }

    private void getPatientData(){
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textAreaPatientInfo.appendText(element.getResource().getResourceName()+"\n\r");
                        }
                    });
                }
            }
        }).start();
    }

}
