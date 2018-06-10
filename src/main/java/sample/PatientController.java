package sample;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.XhtmlDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.jfoenix.controls.*;
import com.jfoenix.svg.SVGGlyph;
import com.sun.org.apache.bcel.internal.classfile.Code;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.text.*;
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
    Integer version_no = 0;

    @FXML
    TextArea textAreaPatientMedications;
    @FXML
    JFXTabPane tabPane;
    @FXML
    JFXButton backButton;
    @FXML
    Button editinfobtn;
    @FXML
    Button canceleditbtn;
    @FXML
    Text textPatientName;
    @FXML
    JFXDatePicker datePickerBegin;
    @FXML
    JFXDatePicker datePickerEnd;
    @FXML
    Text textFirstName, textLastName, textGender, textBirthdate;
    @FXML
    TextField textFirstNameE, textLastNameE, textGenderE, textBirthdateE;

    @FXML
    VBox VBoxObservations;
    @FXML
    VBox VBoxMedStatements;
    @FXML
    StackPane stackPaneDialogContainter;


    public PatientController(IGenericClient client) {
        this.client = client;
    }

    public void initData(Object... params) {
        DateConverter converter=new DateConverter();
        datePickerEnd.setConverter(converter);
        datePickerBegin.setConverter(converter);
        resetValues();
        backButton.setPickOnBounds(true);

        Region icon = new Region();
        icon.getStyleClass().add("icon");
        backButton.setGraphic(icon);

        patient = (Patient) params[0];
        version_no = (Integer) params[1];
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textPatientName.setText(patient.getName().get(version_no).getGivenAsSingleString() + " " + patient.getName().get(version_no).getFamilyAsSingleString());
                textFirstName.setText(patient.getName().get(version_no).getGivenAsSingleString());
                textLastName.setText(patient.getName().get(version_no).getFamilyAsSingleString());
                textGender.setText(patient.getGender());
                System.out.println(patient.getBirthDate()+"\n");
                if (patient.getBirthDate() != null) {
                    Format formatter = new SimpleDateFormat("dd.MM.yyyy");
                    textBirthdate.setText(formatter.format(patient.getBirthDate()));
                } else {
                    textBirthdate.setText("Unknown");
                }
            }
        });
        getPatientData();
    }

    public void showCharts(){
        Main.showCharts(patient,allObservations,datePickerBegin.getValue(),datePickerEnd.getValue());
        tabPane.requestFocus();
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
                if(o1.getEffective()==null && o2.getEffective()==null)return 0;
                if(o1.getEffective()==null)return 1;
                if(o2.getEffective()==null)return -1;
                return (((DateTimeDt)o2.getEffective()).getValue()).compareTo(((DateTimeDt)o1.getEffective()).getValue());
            }
        });
    }

    private void displayData() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                VBoxObservations.getChildren().clear();

                System.out.println("Observations: " + observations.size());
                System.out.println("Medications: " + medications.size());
                System.out.println("MedicationStatements: " + medicationStatements.size() + "\n");
                for(int i=0;i<observations.size();i++){
                    VBoxObservations.getChildren().add(createObservationItem(observations.get(i),i));
                }
                for (Medication medication : medications) {
                    textAreaPatientMedications.appendText(medication.getText() + "\n");
                }
                int id = 0;
                for (MedicationStatement mStatement : medicationStatements) {
                    CodeableConceptDt medication = null;
                    if (mStatement.getMedication() instanceof CodeableConceptDt) {
                        medication = ((CodeableConceptDt) mStatement.getMedication());
                    }
                    if(medication != null) {
                        VBoxMedStatements.getChildren().add(createMedicationStatementItem(medication.getText(),mStatement.getDosage().get(0).getText(),mStatement,id));
                        id ++;
                    }
                    //textAreaPatientMedicationStatements.appendText((medication != null ? medication.getText() : "") + " -> " + mStatement.getDosage().get(0).getText() + "\n");
                }
            }
        });
    }

    private String getObservationDescription(Observation observation) {
        System.out.println(observation.getText().getDivAsString()+"\n");
        String description = observation.getText().getDivAsString();
        if (description != null && description.contains("'>") && description.contains("</div")) {
            description = description.substring(description.lastIndexOf("'>") + 2, description.lastIndexOf("</div"));
        }
        if (description != null && description.contains("\">") && description.contains("</div")) {
            description = description.substring(description.lastIndexOf("\">") + 2, description.lastIndexOf("</div"));
        }
        return description == null ? "" : description;
    }

    @FXML
    private void goBack() {
        VBoxObservations.getChildren().clear();
        Main.changeView("main", null);
    }

    @FXML
    private void filterByDate() {
        LocalDate localDateBegin = datePickerBegin.getValue();
        if(localDateBegin != null) {
            localDateBegin = localDateBegin.plusDays(-1);
        }
        LocalDate localDateEnd = datePickerEnd.getValue();
        if(localDateEnd != null){
            localDateEnd = localDateEnd.plusDays(1);
        }

        Date dateBegin = localDateBegin == null ? new Date(Long.MIN_VALUE) : Date.from(localDateBegin.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateEnd = localDateEnd == null ? new Date(Long.MAX_VALUE) : Date.from(localDateEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());
        observations.clear();
        for (Observation observation : allObservations) {
            if (((DateTimeDt)observation.getEffective()).getValue().after(dateBegin)
                    && ((DateTimeDt)observation.getEffective()).getValue().before(dateEnd)) {
                observations.add(observation);
            }
        }
        displayData();
    }

    private void resetValues() {
        Platform.runLater(() -> {
            tabPane.getSelectionModel().select(0);
            tabPane.requestFocus();
            datePickerBegin.setValue(null);
            datePickerEnd.setValue(null);
            VBoxObservations.getChildren().clear();
            VBoxMedStatements.getChildren().clear();
            textAreaPatientMedications.clear();
            //textAreaPatientMedicationStatements.clear();
        });
    }

    private Node createObservationItem(Observation observation, int id) {
        HBox hbox = new HBox();
        hbox.setId("observationBox"+id);
        hbox.setPadding(new Insets(10, 16, 10, 16));
        hbox.setSpacing(16);
        hbox.getStyleClass().add("HBoxObservation");
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        Format formatter = new SimpleDateFormat("dd.MM.yyyy");
        vbox.getChildren().add(new Text(formatter.format(((DateTimeDt)observation.getEffective()).getValue())));
        vbox.getChildren().add(new Text(getObservationDescription(observation)));
        String svgPath = "M17,9H7V7H17M17,13H7V11H17M14,17H7V15H14M12,3A1,1 0 0,1 13,4A1,1 0 0,1 12,5A1,1 0 0,1 11,4A1,1 0 0,1 12,3M19,3H14.82C14.4,1.84 13.3,1 12,1C10.7,1 9.6,1.84 9.18,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5A2,2 0 0,0 19,3Z";
        if (getObservationDescription(observation).contains("Heart")) {
            svgPath = "M7.5,4A5.5,5.5 0 0,0 2,9.5C2,10 2.09,10.5 2.22,11H6.3L7.57,7.63C7.87,6.83 9.05,6.75 9.43,7.63L11.5,13L12.09,11.58C12.22,11.25 12.57,11 13,11H21.78C21.91,10.5 22,10 22,9.5A5.5,5.5 0 0,0 16.5,4C14.64,4 13,4.93 12,6.34C11,4.93 9.36,4 7.5,4V4M3,12.5A1,1 0 0,0 2,13.5A1,1 0 0,0 3,14.5H5.44L11,20C12,20.9 12,20.9 13,20L18.56,14.5H21A1,1 0 0,0 22,13.5A1,1 0 0,0 21,12.5H13.4L12.47,14.8C12.07,15.81 10.92,15.67 10.55,14.83L8.5,9.5L7.54,11.83C7.39,12.21 7.05,12.5 6.6,12.5H3Z";
        } else if (getObservationDescription(observation).contains("Height")) {
            svgPath = "M1.39,18.36L3.16,16.6L4.58,18L5.64,16.95L4.22,15.54L5.64,14.12L8.11,16.6L9.17,15.54L6.7,13.06L8.11,11.65L9.53,13.06L10.59,12L9.17,10.59L10.59,9.17L13.06,11.65L14.12,10.59L11.65,8.11L13.06,6.7L14.47,8.11L15.54,7.05L14.12,5.64L15.54,4.22L18,6.7L19.07,5.64L16.6,3.16L18.36,1.39L22.61,5.64L5.64,22.61L1.39,18.36Z";
        } else if (getObservationDescription(observation).contains("Weight")) {
            svgPath = "M12,3A4,4 0 0,1 16,7C16,7.73 15.81,8.41 15.46,9H18C18.95,9 19.75,9.67 19.95,10.56C21.96,18.57 22,18.78 22,19A2,2 0 0,1 20,21H4A2,2 0 0,1 2,19C2,18.78 2.04,18.57 4.05,10.56C4.25,9.67 5.05,9 6,9H8.54C8.19,8.41 8,7.73 8,7A4,4 0 0,1 12,3M12,5A2,2 0 0,0 10,7A2,2 0 0,0 12,9A2,2 0 0,0 14,7A2,2 0 0,0 12,5Z";
        } else if (getObservationDescription(observation).contains("Temperature")) {
            svgPath = "M17,17A5,5 0 0,1 12,22A5,5 0 0,1 7,17C7,15.36 7.79,13.91 9,13V5A3,3 0 0,1 12,2A3,3 0 0,1 15,5V13C16.21,13.91 17,15.36 17,17M11,8V14.17C9.83,14.58 9,15.69 9,17A3,3 0 0,0 12,20A3,3 0 0,0 15,17C15,15.69 14.17,14.58 13,14.17V8H11Z";
        }else if(getObservationDescription(observation).contains("Respiratory")){
            svgPath="M31.852,20.446c0-5.712-6.538-15.006-10.555-15.006c-1.917,0-2.927,2.128-3.445,4.963c-0.312-0.355-0.495-0.629-0.66-0.818\n" +
                    "\t\tV2.302c0-0.706-0.534-1.278-1.239-1.278s-1.239,0.572-1.239,1.278v7.446c-0.165,0.332-0.373,0.636-0.628,0.926\n" +
                    "\t\tc-0.509-2.922-1.512-5.135-3.467-5.135c-4.018,0-10.56,9.294-10.56,15.004c0,5.713-0.763,10.342,3.254,10.342\n" +
                    "\t\tc4.016,0,11.288-4.629,11.288-10.342c0-2.015-0.001-4.475-0.173-6.815c0.617-0.429,1.184-0.919,1.661-1.486\n" +
                    "\t\tc0.373,0.413,0.836,0.871,1.395,1.339c-0.175,2.357-0.175,4.835-0.175,6.865c0,5.712,7.272,10.344,11.29,10.344\n" +
                    "\t\tC32.61,30.79,31.852,26.158,31.852,20.446z";
        }
        SVGGlyph icon = new SVGGlyph(svgPath);
        icon.setSize(25);
        icon.setFill(Paint.valueOf("white"));
        StackPane pane = new StackPane();
        pane.setPrefWidth(50);
        pane.setPrefHeight(50);
        pane.getChildren().add(icon);
        pane.setStyle("-fx-background-color: #1976D2; -fx-border-radius: 25 25 25 25; -fx-background-radius: 25 25 25 25;");
        pane.setAlignment(Pos.CENTER);
        hbox.getChildren().add(pane);
        hbox.getChildren().add(vbox);

        hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                openObservationDialog(id);
                System.out.println(event.getSource());
            }
        });
        JFXRippler rippler = new JFXRippler(hbox);
        return rippler;
    }
    private Node createMedicationStatementItem(String medName, String description, MedicationStatement mStatement, int id) {
        HBox hbox = new HBox();
        //hbox.setId("observationBox"+id);
        hbox.setPadding(new Insets(10, 16, 10, 16));
        hbox.setSpacing(16);
        hbox.getStyleClass().add("HBoxObservation");
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        Format formatter = new SimpleDateFormat("dd.MM.yyyy");
        vbox.getChildren().add(new Text(medName));

        SVGGlyph icon = new SVGGlyph("M4.22,11.29L11.29,4.22C13.64,1.88 17.43,1.88 19.78,4.22C22.12,6.56 22.12,10.36 19.78,12.71L12.71,19.78C10.36,22.12 6.56,22.12 4.22,19.78C1.88,17.43 1.88,13.64 4.22,11.29M5.64,12.71C4.59,13.75 4.24,15.24 4.6,16.57L10.59,10.59L14.83,14.83L18.36,11.29C19.93,9.73 19.93,7.2 18.36,5.64C16.8,4.07 14.27,4.07 12.71,5.64L5.64,12.71Z");
        icon.setSize(25);
        icon.setFill(Paint.valueOf("white"));
        StackPane pane = new StackPane();
        pane.setPrefWidth(50);
        pane.setPrefHeight(50);
        pane.getChildren().add(icon);
        pane.setStyle("-fx-background-color: #1976D2; -fx-border-radius: 25 25 25 25; -fx-background-radius: 25 25 25 25;");
        pane.setAlignment(Pos.CENTER);
        hbox.getChildren().add(pane);
        hbox.getChildren().add(vbox);

        hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                openMedStatementsDialog(medName, description, mStatement,id);
                System.out.println(event.getSource());
            }
        });
        JFXRippler rippler = new JFXRippler(hbox);
        return rippler;
    }

    private void openObservationDialog(int id){
        Observation observation = observations.get(id);
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("Observation details"));
        String observationString = getObservationDescription(observation)+"\n"+observation.getMeta().getLastUpdated();
        if(observation.getComments() != null) {
            observationString += ("\n" + observation.getComments());
        }
        content.setBody(new Text(observationString));
        JFXDialog dialog = new JFXDialog(stackPaneDialogContainter,content,JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Close");
        button.setOnAction(actionEvent ->{
            dialog.close();
        });
        final TextField editField = new TextField();
        editField.setVisible(false);
        JFXButton button2 = new JFXButton("Edit");
        button2.setOnAction(actionEvent ->{
            if(!editField.isVisible()) {
                editField.setVisible(true);
                editField.setText(getObservationDescription(observation));
                content.setBody(editField);
                button2.setText("Save");
            }
            else{
                NarrativeDt observationText = new NarrativeDt();
                observationText.setDivAsString(editField.getText());
                observation.setText(observationText);
                updateObservation(observation);
                System.out.println("Zmieniono dane\n");
                dialog.close();
            }
        });
        dialog.setOnDialogClosed(event->{
            stackPaneDialogContainter.setMouseTransparent(true);
        });
        content.setActions(button,button2);
        stackPaneDialogContainter.setMouseTransparent(false);
        dialog.show();

    }
    private void openMedStatementsDialog(String title, String message, MedicationStatement mStatement,int id){
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text(title));
        content.setBody(new Text(message));
        JFXDialog dialog = new JFXDialog(stackPaneDialogContainter,content,JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Close");
        button.setOnAction(actionEvent ->{
            dialog.close();
        });
        final TextField editField = new TextField();
        JFXButton button2 = new JFXButton("Edit");
        editField.setVisible(false);
        button2.setOnAction(actionEvent ->{
            if(!editField.isVisible()) {
                editField.setVisible(true);
                editField.setText(message);
                content.setBody(editField);
                button2.setText("Save");
            }
            else{
                MedicationStatement.Dosage dosage = new MedicationStatement.Dosage();
                dosage.setText(editField.getText());
                mStatement.getDosage().set(id,dosage);
                updateMedStatements(mStatement);
                System.out.println("Zmieniono dane\n");
                dialog.close();
            }
        });
        dialog.setOnDialogClosed(event->{
            stackPaneDialogContainter.setMouseTransparent(true);
        });
        content.setActions(button, button2);
        stackPaneDialogContainter.setMouseTransparent(false);
        dialog.show();
    }
    @FXML
    private void editInfo(){

        if(editinfobtn.getText().equals("Edit")) {
            editinfobtn.setText("Save");
            canceleditbtn.setVisible(true);
            textFirstName.setVisible(false);
            textLastName.setVisible(false);
            textGender.setVisible(false);
            textBirthdate.setVisible(false);

            textFirstNameE.setText(textFirstName.getText());
            textLastNameE.setText(textLastName.getText());
            textGenderE.setText(textGender.getText());
            textBirthdateE.setText(textBirthdate.getText());

            textFirstNameE.setVisible(true);
            textLastNameE.setVisible(true);
            textGenderE.setVisible(true);
            textBirthdateE.setVisible(true);
        }
        else{
            patient.addName().addFamily(textLastNameE.getText()).addGiven(textFirstNameE.getText());
            patient.setGender(AdministrativeGenderEnum.forCode(textGenderE.getText()));
            Date date = null;
            Boolean correctDate = true;
            DateFormat dateFormat = new SimpleDateFormat(
                    "dd.MM.yyyy", Locale.US);
            try {
                date = dateFormat.parse(textBirthdateE.getText());
                patient.setBirthDateWithDayPrecision(date);
            } catch (ParseException e) {
                Alert alert = new Alert(Alert.AlertType.NONE, "Correct date format: dd.MM.yyyy", ButtonType.OK);
                alert.showAndWait();
                correctDate = false;
                e.printStackTrace();
            }
            if(correctDate){
                updateDB(patient);
            }
            canceledit();
        }
    }
    public void updateDB(Patient pat){
        MethodOutcome outcome = client.update()
                .resource(pat)
                .execute();
        IdDt id = (IdDt) outcome.getId();
        System.out.println("Got ID: " + id.getValue());
        System.out.println("Zmieniono dane\n");
    }
    public void updateMedStatements(MedicationStatement medStat){
        MethodOutcome outcome = client.update()
                .resource(medStat)
                .execute();
        IdDt id = (IdDt) outcome.getId();
        System.out.println("Got ID: " + id.getValue());
        System.out.println("Zmieniono dane\n");
    }
    public void updateObservation(Observation obs){
        MethodOutcome outcome = client.update()
                .resource(obs)
                .execute();
        IdDt id = (IdDt) outcome.getId();
        System.out.println("Got ID: " + id.getValue());
        System.out.println("Zmieniono dane\n");
    }
    @FXML
    private void canceledit(){
        canceleditbtn.setVisible(false);
        editinfobtn.setText("Edit");
        textFirstName.setVisible(true);
        textLastName.setVisible(true);
        textGender.setVisible(true);
        textBirthdate.setVisible(true);

        textFirstName.setText(textFirstNameE.getText());
        textLastName.setText(textLastNameE.getText());
        textGender.setText(textGenderE.getText());
        textBirthdate.setText(textBirthdateE.getText());

        textFirstNameE.setVisible(false);
        textLastNameE.setVisible(false);
        textGenderE.setVisible(false);
        textBirthdateE.setVisible(false);
    }
}
