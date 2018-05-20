package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Application {
    static List<SplitPane> splitpane = new ArrayList<SplitPane>();
    static AnchorPane root;
    private static String currentView;
    private static HashMap<String, Pane> screenMap = new HashMap<>();
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
        FXMLLoader loader2 = new FXMLLoader(getClass().getClassLoader().getResource("patient.fxml"));

        loader.setController(controller=new Controller(client));
        loader2.setController(patientController=new PatientController(client));

        screenMap.put("main", loader.load());
        screenMap.put("patient", loader2.load());
        screenMap.get("main").getStylesheets().add("styles.css");
        screenMap.get("patient").getStylesheets().add("styles.css");


        primaryStage.setTitle("Karta Pacjenta");
        controller.init();
        primaryStage.setScene(new Scene(screenMap.get("main")));


        primaryStage.show();

    }
    public static void changeView (String name, Object... params){
        Pane pane = screenMap.get(name);
        if(pane!=null) {
            primaryStage.getScene().setRoot(pane);
            currentView = name;
        }
        if(name=="patient"){
            patientController.initData(params);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
