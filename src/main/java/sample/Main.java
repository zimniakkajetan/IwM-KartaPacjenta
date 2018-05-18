package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.IGenericClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    static List<SplitPane> splitpane = new ArrayList<SplitPane>();
    static AnchorPane root;
    static int curentView = 0;
    @Override
    public void start(Stage primaryStage) throws Exception{
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
        FXMLLoader loader2 = new FXMLLoader(getClass().getClassLoader().getResource("view2.fxml"));

        Controller controller = new Controller(client);

        loader.setController(controller);
        loader2.setController(controller);

        splitpane.add((SplitPane)loader.load());
        splitpane.add((SplitPane)loader2.load());

        root = (AnchorPane)FXMLLoader.load(getClass().getClassLoader().getResource("basic_annchor.fxml"));
        root.getChildren().add(splitpane.get(0));

        primaryStage.setTitle("Karta Pacjenta");
        primaryStage.setScene(new Scene(root, 300, 275));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());

        primaryStage.show();

    }
    public static void changeView (int i){

        if(i == 0){
            root.getChildren().remove(splitpane.get(curentView));
            root.getChildren().add(splitpane.get(0));
            curentView = 0;
        }
        if(i == 1){
            root.getChildren().remove(splitpane.get(curentView));
            root.getChildren().add(splitpane.get(1));
            curentView = 1;
            //Bedzie trzeba dodac przekazywanie parametrow,
            //np id wybranej osoby
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
