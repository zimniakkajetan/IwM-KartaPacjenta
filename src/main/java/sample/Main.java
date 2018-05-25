package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Application {
    static List<SplitPane> splitpane = new ArrayList<SplitPane>();
    static AnchorPane root;
    private static String currentView;
    private static HashMap<String, Pane> screenMap = new HashMap<>();
    private static ChartsController chartsController;
    private static PatientController patientController;
    private static Controller controller;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);


        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        FXMLLoader patientLoader = new FXMLLoader(getClass().getClassLoader().getResource("patient.fxml"));
        FXMLLoader chartsLoader = new FXMLLoader(getClass().getClassLoader().getResource("charts.fxml"));

        loader.setController(controller=new Controller(client));
        patientLoader.setController(patientController=new PatientController(client));
        chartsLoader.setController(chartsController=new ChartsController());

        screenMap.put("main", loader.load());
        screenMap.put("patient", patientLoader.load());
        screenMap.put("charts",chartsLoader.load());
        screenMap.get("main").getStylesheets().add("styles.css");
        screenMap.get("patient").getStylesheets().add("styles.css");
        screenMap.get("charts").getStylesheets().add("styles.css");


        primaryStage.setTitle("Karta Pacjenta");
        controller.init();
        primaryStage.setScene(new Scene(screenMap.get("main")));


        primaryStage.show();

    }
    public static void changeView (String name, Object... params){
        Pane pane = screenMap.get(name);
        if(pane!=null) {
            if(name=="patient"){
                patientController.initData(params);
            }else if(name=="main"){
                controller.clearSelection();
            }
            primaryStage.getScene().setRoot(pane);
            currentView = name;
        }
    }

    public static void showCharts(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){
        Stage stage = new Stage();
        stage.setTitle("My New Stage Title");
        Scene scene=new Scene(screenMap.get("charts"));
        chartsController=new ChartsController(patient,observations,dateBegin,dateEnd);
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
