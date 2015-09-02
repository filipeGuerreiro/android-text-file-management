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


    final public LocalWorkspace createWorkspace(String name, int size) {
        assert name != null;

        if(size < 1) {
            //TODO: meter isto com um toast e/ou excepcao
            assert false;
        }

        LocalWorkspace newWorkspace = new LocalWorkspace(userDir, name, size, this.getMail());
        addOwnedWorkspace(newWorkspace);
        //addForeignWorkspace(newWorkspace);

        return newWorkspace;
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

    final public Workspace addForeignWorkspace(String workspaceName, int size, String ownerMail) {
        assert workspaceName != null;

        if(size < 1) {
            //TODO: meter isto com um toast e/ou excepcao
            assert false;
        }

        LocalWorkspace newWorkspace = new LocalWorkspace(userDir, workspaceName, size, ownerMail);
        addForeignWorkspace(newWorkspace);
        return newWorkspace;
    }

    final public void addForeignWorkspace(Workspace newWorkspace) {
        foreignWorkspaces.add(newWorkspace);
    }

    final public void removeForeignWorkspace(int oldWorkspacePosition) {
        foreignWorkspaces.remove(oldWorkspacePosition);
    }

    /**
     * Remove all foreign workspaces from user "userMail"
     * @param userMail
     */
    final public void removeForeignWorkspace(String userMail) {
        for (int i = 0; i<foreignWorkspaces.size(); i++) {
            if (foreignWorkspaces.get(i).getOwnerMail().equals(userMail))
                foreignWorkspaces.remove(i);
        }
    }

    final public boolean doesForeignWorkspaceExist(String workspaceName, String ownerMail) {
        for(Workspace workspace : this.getForeignWorkspaces()) {
            if(workspace.getName().equals(workspaceName)
                    && workspace.getOwnerMail().equals(ownerMail)) {
                return true;
            }
        }
        return false;
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

        //Settings exist for this user
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
     * Loads all existing workspaces from the file system.
     * This method must only be called at the instantiation moment.
     */
    public void loadOwnedWorkspaces() {
        if (userDir.exists() && userDir.isDirectory()) {
            for (String wpSpaceName : userDir.list()) {
                LocalWorkspace wkSpace = new LocalWorkspace(userDir, wpSpaceName, 50, this.getMail());
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

    public void removeForeignWorkspace(String workspaceName, String user) {

        int i = 0;
        for(Workspace workspace : this.getForeignWorkspaces()) {

            // Removes the workspace that has a matching name and owner
            if(workspace.getName().equals(workspaceName) && workspace.getOwnerMail().equals(user)) {
                this.removeForeignWorkspace(i);
            }
            i++;
        }
    }

    public Workspace getForeignWorkspace(String workspaceOwner, String workspaceName) {
        for(Workspace workspace : this.getForeignWorkspaces()) {
            if(workspace.getName().equals(workspaceName) && workspace.getOwnerMail().equals(workspaceOwner)) {
                return workspace;
            }
        }
        return null;
    }

    public void testPopulate() {
        LocalWorkspace wrkSpc;

        try {
            wrkSpc = createWorkspace("testPrivateWorkspace", 555);
            wrkSpc.createFile("emptyFile");
            wrkSpc.createFile("cenasFile").write("cenas");

            wrkSpc = createWorkspace("testPrivateACL_a@a.a", 555);
            wrkSpc.createFile("emptyFile");
            wrkSpc.createFile("cenasFile").write("cenas");
            wrkSpc.addUserToACL("a@a.a");

            wrkSpc = createWorkspace("testPublic_tag_testTag", 555);
            wrkSpc.addTag("testTag");
            wrkSpc.setVisibility(true);
            wrkSpc.createFile("emptyFile");
            wrkSpc.createFile("cenasFile").write("cenas");

            wrkSpc = createWorkspace("testSize_maxSize_1", 1);
            wrkSpc.addTag("testTag");

        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
}
