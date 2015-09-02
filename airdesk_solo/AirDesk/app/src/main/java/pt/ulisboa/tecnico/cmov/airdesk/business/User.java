package pt.ulisboa.tecnico.cmov.airdesk.business;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alex on 12-03-2015.
 */
public class User {

    private static final String NICK_KEY = "nick";
    private static final String MAIL_KEY = "mail";
    private static final String TAGS_KEY = "tags";
    private static final String HAS_PREFS_KEY = "exists";

    private String nickName;
    private String mail;
    private List<String> tags;
    private List<LocalWorkspace> ownedWorkspaces;
    private List<Workspace> foreignWorkspaces;
    private File userDir;

    protected User(String userNickName, String userMail) {
        nickName = userNickName;
        mail = userMail;
        tags = new ArrayList<>();
        ownedWorkspaces = new ArrayList<>();
        foreignWorkspaces = new ArrayList<>();
        userDir = new File(GlobalContext.getGC().getFilesDir(), getMailSanatized());
    }

    final public String getMail() {
        return mail;
    }

    final public String getMailSanatized() { return mail.replace("@", ""); }

    final public String getNickName() {
        return nickName;
    }

    final public void setNickName(String newNickname) { nickName = newNickname; }

    final public List<String> getTags() { return tags; }

    final public void addTag(String tag) {
        tags.add(tag);
    }

    final public void removeTag(int position) {
        tags.remove(position);
    }

    final public List<LocalWorkspace> getOwnedWorkspaces() { return ownedWorkspaces; }

    final public LocalWorkspace getOwnedWorkspace(int position) {
        return ownedWorkspaces.get(position);
    }


    final public void createWorkspace(String name, int size) {
        assert name != null;

        if(size < 1) {
            //TODO: meter isto com um toast e/ou excepcao
            assert false;
        }

        LocalWorkspace newWorkspace = new LocalWorkspace(userDir, name, size,this.getMail());
        addOwnedWorkspace(newWorkspace);
        //addForeignWorkspace(newWorkspace);
    }

    final public Workspace getOwnedWorkspace(String name) {
        for(Workspace workspace : ownedWorkspaces) {
            if(workspace.getName().equals(name))
                return workspace;
        }
        return null;
    }

    final protected void addOwnedWorkspace(LocalWorkspace newWorkspace) {
        ownedWorkspaces.add(newWorkspace);
        newWorkspace.mkdir();
    }

    final public void removeOwnedWorkspace(int oldWorkspacePosition) {
        LocalWorkspace workspace = ownedWorkspaces.get(oldWorkspacePosition);
        ownedWorkspaces.remove(workspace);
        foreignWorkspaces.remove(workspace);
        workspace.del();
    }

    final public List<Workspace> getForeignWorkspaces() { return foreignWorkspaces; }

    final public Workspace getForeignWorkspace(int position) {
        return foreignWorkspaces.get(position);
    }

    final public void addForeignWorkspace(Workspace newWorkspace) {
        foreignWorkspaces.add(newWorkspace);
    }

    final public void removeForeignWorkspace(int oldWorkspacePosition) {
        foreignWorkspaces.remove(oldWorkspacePosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!mail.equals(user.mail)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mail.hashCode();
    }

    /**
     * Saves the users settings persistently.
     */
    protected void savePreferences() {
        SharedPreferences settings = GlobalContext.getGC().getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(genPrefKey(User.HAS_PREFS_KEY), true);
        
        editor.putString(genPrefKey(User.NICK_KEY), getNickName());
        editor.putString(genPrefKey(User.MAIL_KEY), getMail());
        editor.putStringSet(genPrefKey(User.TAGS_KEY), new HashSet<>(getTags()));

        editor.apply();

        for(LocalWorkspace wk: this.ownedWorkspaces) {
            wk.savePreferences();
        }

        Log.i(Constants.LOG_TAG, String.format("User.savePreferences, mail: %s", getMail()));
    }

    /**
     * If the user has saved preferences, this method loads them.
     *
     * Returns true if preferences exist, false otherwise
     */
    protected boolean loadPreferences() {
        SharedPreferences settings = GlobalContext.getGC().getPreferences(0);

        //Settigns exist for this user
        if (settings.getBoolean(genPrefKey(User.HAS_PREFS_KEY), false) ) {
            setNickName(settings.getString(genPrefKey(User.NICK_KEY), null));

            tags = new ArrayList<>(settings.getStringSet(genPrefKey(User.TAGS_KEY), null));

            Log.i(Constants.LOG_TAG, String.format("User.loadPreferences, mail: %s, sucess", getMail()));
            return true;
        }

        Log.w(Constants.LOG_TAG, String.format("User.loadPreferences, mail: %s, failure", getMail()));
        return false;
    }

    /**
     * Loads all existings workspaces from the file system.
     * This method must only be called at the instantiation moment.
     */
    public void loadOwnedWorkspaces() {
        if (userDir.exists() && userDir.isDirectory()) {
            for (String wpSpaceName : userDir.list()) {
                LocalWorkspace wkSpace = new LocalWorkspace(userDir, wpSpaceName, 1,this.getMail());
                wkSpace.loadPreferences();
                wkSpace.sync();

                ownedWorkspaces.add(wkSpace);
            }

            Log.d(Constants.LOG_TAG, "User.loadOwnedWorkspaces");
        }
    }

    /**
     * Generates a key unique to this user
     * @param key the specific value key
     * @return unique user pair key
     */
    private String genPrefKey(String key) {
        return getMailSanatized() + key;
    }
}
