package sample;

import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import java.time.LocalDate;
import java.util.List;

public class ChartsController {

    public ChartsController(){

    }

    public ChartsController(Patient patient, List<Observation> observations, LocalDate dateBegin, LocalDate dateEnd){

    }
}
