package pt.ulisboa.tecnico.cmov.airdesk.exception;

/**
 * Created by alex on 23-03-2015.
 */
public class InvalidUserCreation extends AirDeskException {

    public InvalidUserCreation(String nickname, String email) {
        super(String.format("Cannot create a user with:\nnickname="+nickname+"\nemail="+email));
    }
}
