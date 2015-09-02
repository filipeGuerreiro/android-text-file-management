package pt.ulisboa.tecnico.cmov.airdesk.business;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by alex on 12-03-2015.
 */
public class FileLocal implements IFile {

    private final File file;
    private boolean write_lock;
    private int size;
    private String text;
    private boolean isShared = false;
    private int timestamp = 0;

    private void constructorCommon() {
        write_lock = true;
        Log.d(Constants.LOG_TAG, "new FileLocal instance, path: " + file.getPath());
    }

    protected FileLocal(File file) {
        this.file = file;
        constructorCommon();
    }

    protected FileLocal(File workspaceDir, String fileName) {
        this.file = new File(workspaceDir, fileName);
        constructorCommon();
    }

    /**
     * Writes the string to this file.
     * The user must have the writeLock
     *
     * @param text to write
     * @return true if successful, false otherwise
     */
    public boolean write(String text) {
        boolean ret = false;
        if (hasWriteLock()) {
            ret = FileUtil.writeStringAsFile(text, file);
            if (ret) {
                setSize(text.length());
                setTimestamp(getTimestamp()+1);
                Log.i(Constants.LOG_TAG, "FileLocal.write, hasWriteLock is true, write successful");
            }
        } else {
            Log.e(Constants.LOG_TAG, "FileLocal.write, hasWriteLock is false, write failed");
        }
        return ret;
    }

    /**
     * Returns the contents of this file as a string
     * @return the file content
     */
    public String read() {

        if(this.text != null)
            return text;

        String ret = FileUtil.readFileAsString(file);

        if (ret == null)
            return "";
        else
            return ret;
    }

    public void setText(String text) {

        this.text = text;
        setSize(text.length());
        setTimestamp(getTimestamp()+1);
    }

    /**
     * This method is very special.
     * It summons the almighty Cthulhu.
     */
    @Override
    public boolean del() {
        return file.delete();
    }

    synchronized private boolean hasWriteLock() { return write_lock; }

    @Override
    public String getName() { return file.getName(); }

    @Override
    public int getSize() {
        // TODO: calculate size of file
        return size;
    }

    public void setSize(int newSize) {
        size = newSize;
    }

    public boolean isShared() { return isShared; }

    public void setSharedStatus(boolean newStatus) { isShared = newStatus; }

    public int getTimestamp() { return timestamp; }

    public void setTimestamp(int newTimestamp) { timestamp = newTimestamp; }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put("name", this.getName());
            json.put("size", new Integer(this.size));
            json.put("text", this.read());
            json.put("timestamp", this.getTimestamp());
        } catch (JSONException e) { e.printStackTrace(); }

        return json;
    }
}
