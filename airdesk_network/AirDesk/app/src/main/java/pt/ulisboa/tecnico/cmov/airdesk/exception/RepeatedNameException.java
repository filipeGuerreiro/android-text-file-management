package pt.ulisboa.tecnico.cmov.airdesk.exception;

/**
 * Created by alex on 23-03-2015.
 */
public class RepeatedNameException extends AirDeskException {

    public RepeatedNameException(String repeatedName) {
        super(String.format("Duplicated name %s", repeatedName));
    }
}
