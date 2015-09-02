package pt.ulisboa.tecnico.cmov.airdesk.exception;

/**
 * Created by alex on 06-04-2015.
 */
public class InvalidMaxSpace extends AirDeskException {
    public InvalidMaxSpace (int invalidValue, int occupied) {
        super(String.format("Invalid new Maximum space: %d bytes.\n Current occupied space: %d bytes",invalidValue, occupied));
    }


}
