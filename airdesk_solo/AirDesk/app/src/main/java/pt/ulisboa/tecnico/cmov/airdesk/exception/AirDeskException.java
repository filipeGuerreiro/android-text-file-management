package pt.ulisboa.tecnico.cmov.airdesk.exception;

/**
 * Created by alex on 23-03-2015.
 */
public class AirDeskException extends Exception {

    public AirDeskException() {
        super();
    }


    public AirDeskException (String detailMessage) {
        super(detailMessage);
    }
}
