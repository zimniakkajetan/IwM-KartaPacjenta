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
    private static HashMap<String, Control> screenMap = new HashMap<String,Control>();
    private static PatientController patientController;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        //Connecting to server
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        /*Controller controller = new Controller(client);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("basic_annchor.fxml"));
        loader.setController(controller);
        Parent root = loader.load();*/

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        FXMLLoader loader2 = new FXMLLoader(getClass().getClassLoader().getResource("patient.fxml"));

        loader.setController(new Controller(client));
        loader2.setController(patientController=new PatientController(client));

        screenMap.put("main", (SplitPane)loader.load());
        screenMap.put("patient", (SplitPane)loader2.load());


        primaryStage.setTitle("Karta Pacjenta");
        primaryStage.setScene(new Scene(screenMap.get("main")));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());

        primaryStage.show();

    }
    public static void changeView (String name, Object... params){
        Control pane = screenMap.get(name);
        if(pane!=null) {
            primaryStage.setScene(new Scene(pane));
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
