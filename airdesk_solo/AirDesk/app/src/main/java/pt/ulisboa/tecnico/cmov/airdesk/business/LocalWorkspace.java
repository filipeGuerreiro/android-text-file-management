package pt.ulisboa.tecnico.cmov.airdesk.business;

        import android.content.SharedPreferences;
        import android.util.Log;

        import java.io.File;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.HashSet;
        import java.util.List;
        import java.util.Map;
        import java.util.TreeMap;

        import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidMaxSpace;
        import pt.ulisboa.tecnico.cmov.airdesk.exception.RepeatedNameException;

/**
 * Created by petrucci on 07/04/15.
 */
public class LocalWorkspace implements Workspace {

        private static final String HAS_PREFS_KEY = "exists";
        private static final String TAGS_KEY = "tags";
        private static final String USERACL_KEY= "userACL";
        private static final String IS_PUBLIC_KEY = "isPublic";
        private static final String SIZE_KEY = "size";

        private File thisDir;
        private int size;
        private TreeMap<String, IFile> fileMap;
        private List<String> userACL;
        private List<String> tags;
        private boolean isPublic;

    private String ID;
        private String ownerEmail;

        protected LocalWorkspace(File workspaceDir, int size,String ownerEmail) {
            thisDir = workspaceDir;
            this.size = size;
            this.fileMap = new TreeMap<>();
            this.tags = new ArrayList<>();
            this.userACL = new ArrayList<>();
            this.isPublic = false;
            this.ID=this.getName()+this.ownerEmail;


            if (Constants.CREATE_STUBS) {
                try {
                    createFile("emptyFile");
                    createFile("File").write("cenas");
                } catch (RepeatedNameException e) {
                    //TODO: meter um toast nisto
                    assert false;
                }

            }
        }

        protected LocalWorkspace(File userDir, String name, int size,String ownerEmail) {
            this(new File(userDir, name), size,ownerEmail);
        }

        public String getName() {
            return thisDir.getName();
        }

        public void setName(String newName) {
            thisDir.renameTo(new File(thisDir.getParentFile(), newName));
        }

        public int getMaxSpace() {
            return size;
        }

        public int getUsedSpace() {
            int minSpace = 0;
            for(Map.Entry<String, IFile> entry : fileMap.entrySet()) {
                IFile file = entry.getValue();
                minSpace += file.getSize();
            }
            return minSpace;
        }

        public void setMaxSpace(int newSize) throws InvalidMaxSpace{
            Log.d(Constants.LOG_TAG, "" + newSize + "  " + getUsedSpace());
            if (newSize < getUsedSpace())
                throw new InvalidMaxSpace(newSize, getUsedSpace());

            size = newSize;
        }

        public Map<String, IFile> getFileMap() {
            return fileMap;
        }

        /**
         * Returns a list of all file names that belong to this LocalWorkspace
         * @return List<String>
         */
        public List<String> ls() {
        /*
            1 gets the set of keys, the names of all files
            2 converts to String[]
            3 converts to List , Arrays.asList(...)
         */
            return Arrays.asList(fileMap.keySet().toArray(new String[fileMap.size()]));
        }

    @Override
    public boolean del() {
        for (IFile file : fileMap.values()) {
            file.del();
        }
        fileMap.clear();
        return thisDir.delete();
    }

    /**
         * Creates a new file in this LocalWorkspace
         * @param fName
         * @throws RepeatedNameException
         */
        public IFile createFile(String fName) throws RepeatedNameException{
            if (!thisDir.exists())
                thisDir.mkdirs();

            if (fileMap.containsKey(fName)) {
                throw new RepeatedNameException(fName);
            } else {
                //TODO: dependendo se é privado ou nao meter isto como deve de ser
                //TODO: isto do global contezt ta muita martelado

                IFile file = new FileLocal(thisDir, fName);
                file.write("");
                addFile(file);
                return file;
            }
        }

        public void addFile(IFile newFile) {
            fileMap.put(newFile.getName(), newFile);
        }

        public void removeFile(String fName) {
            IFile file = fileMap.get(fName);
            if ( file != null ) {
                file.del();
                fileMap.remove(fName);
            }
            //TODO: meter isto a fazer throw de uma exepcao
        }

        public IFile getFile(String name) { return fileMap.get(name); }

        public void addTag(String newTag) { tags.add(newTag); }

        public void removeTag(int position) { tags.remove(position); }

        public List<String> getTags() { return tags; }

        public boolean isPublic() { return isPublic; }

        public void setVisibility(boolean visibility) { isPublic = visibility; }

        public String toString() {
            return getName();
        }

        public List<String> getUserACL(){ return this.userACL; }

        public void addUserToACL(String email) { this.userACL.add(email); }

        public boolean rmUserFromACL(String email) { return this.userACL.remove(email); }

        @Override
        public String getID() {
            return ID;
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }
        /**
         * Loads all the files in the file system dir, witch belong to this LocalWorkspace, but
         * are not being tracked by this LocalWorkspace
         * @return the number of new  files loaded
         */
        protected int sync() {
            int ret = 0;

            for (String fName : thisDir.list()) {
                if (fileMap.containsKey(fName))
                    continue;

                addFile(new FileLocal(thisDir, fName));
                ret++;
            }

            Log.i(Constants.LOG_TAG, String.format("LocalWorkspace.sync(), name: %s synced %d files", getName(), ret));
            return ret;
        }

        protected void mkdir() { thisDir.mkdirs(); }

        /**
         * Saves the users settings persistently.
         */
        protected void savePreferences() {
            SharedPreferences settings = GlobalContext.getGC().getPreferences(0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putBoolean(genPrefKey(LocalWorkspace.HAS_PREFS_KEY), true);

            editor.putBoolean(genPrefKey(LocalWorkspace.IS_PUBLIC_KEY), isPublic());
            editor.putStringSet(genPrefKey(LocalWorkspace.TAGS_KEY), new HashSet<>(getTags()));
            editor.putStringSet(genPrefKey(LocalWorkspace.USERACL_KEY), new HashSet<>(getUserACL()));
            editor.putInt(genPrefKey(LocalWorkspace.SIZE_KEY), getMaxSpace());

            editor.apply();

            Log.i(Constants.LOG_TAG, String.format("LocalWorkspace.savePreferences, name: %s", getName()));
        }

        /**
         * If the LocalWorkspace has saved preferences, this method loads them.
         *
         * Returns true if preferences exist, false otherwise
         */
        protected boolean loadPreferences() {
            SharedPreferences settings = GlobalContext.getGC().getPreferences(0);

            //Settigns exist for this LocalWorkspace
            if (settings.getBoolean(genPrefKey(LocalWorkspace.HAS_PREFS_KEY), false) ) {

                setVisibility(settings.getBoolean(genPrefKey(LocalWorkspace.IS_PUBLIC_KEY), isPublic()));

                int storedMaxSize = settings.getInt(genPrefKey(LocalWorkspace.SIZE_KEY), getMaxSpace());
                if (storedMaxSize < getUsedSpace()) {
                    try {
                        setMaxSpace(getUsedSpace());
                        Log.w(Constants.LOG_TAG,
                                String.format("LocalWorkspace.loadPreferences, name: %s, " +
                                                "Invalid stored MaxSize: %d. \n" +
                                                "Files must have been altered externally. New maxSize : %d.",
                                        getName(), storedMaxSize, getUsedSpace()));
                    } catch (InvalidMaxSpace e) {
                        Log.e(Constants.LOG_TAG,
                                "LocalWorkspace.loadPreferences, setMaxSpace(getMaxSpace()); throwed InvalidMaxSpace");
                        System.exit(-1);
                    }
                }

                tags = new ArrayList<>(settings.getStringSet(genPrefKey(LocalWorkspace.TAGS_KEY), null));
                userACL = new ArrayList<>(settings.getStringSet(genPrefKey(LocalWorkspace.USERACL_KEY), null));

                Log.i(Constants.LOG_TAG, String.format("LocalWorkspace.loadPreferences, name: %s, sucess", getName()));
                return true;
            }

            Log.i(Constants.LOG_TAG, String.format("LocalWorkspace.loadPreferences, name: %s, failure", getName()));
            return false;
        }

        /**
         * Generates a key unique to this LocalWorkspace/user pair
         * @param key the specific value key
         * @return unique LocalWorkspace/user pair key
         */
        private String genPrefKey(String key) {
            return thisDir.getPath() + key;
        }

    }
