package pt.ulisboa.tecnico.cmov.airdesk.business;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidUserCreation;

/**
 * Created by Filipe on 11/03/2015.
 */
public class GlobalContext extends Application {

    private static GlobalContext gc;

    private List<User> users = new ArrayList<>();
    public User loggedInUser = null;

    // Regex that only matches a valid email address.
    public static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    private int workspaceIndex = 0;

    public User getUser(String email) {
        for(User user : users) {
            if(user.getMail().equals(email)) {
                loggedInUser = user;
                return user;
            }
        }
        return null;
    }

    public void addUser(String nickname, String email) throws InvalidUserCreation {

        if( nickname.trim().length() == 0 || ! rfc2822.matcher(email).matches() )
            throw new InvalidUserCreation(nickname, email);

        //TODO: move the logicic fo creating users to the User class.
        User user = new User(nickname, email);
        users.add(user);
        loggedInUser = user;

        if(user.loadPreferences()) {
            Log.i(Constants.LOG_TAG, String.format("GlobalContext.addUser, Loaded user (from savedPreferences), nick: %s, mail: %s", nickname, email));
            user.loadOwnedWorkspaces();
        } else {
            Log.i(Constants.LOG_TAG, String.format("GlobalContext.addUser, Created user, nick: %s, mail: %s", nickname, email));
        }
    }

    public int getWorkspaceIndex() { return workspaceIndex; }

    public void setWorkspaceIndex(int newIndex) {
        workspaceIndex  = newIndex;
    }

    public static final GlobalContext getGC() { return gc ; }

    public void savePreferences() {
        loggedInUser.savePreferences();
    }

    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences("AirDeskConfigs", mode);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalContext.gc = this;
    }
}
