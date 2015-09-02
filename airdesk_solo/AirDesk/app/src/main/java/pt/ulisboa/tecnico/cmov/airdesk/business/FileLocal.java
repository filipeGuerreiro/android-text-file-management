package pt.ulisboa.tecnico.cmov.airdesk.business;

import android.util.Log;

import java.io.File;

/**
 * Created by alex on 12-03-2015.
 */
public class FileLocal implements IFile {

    private final File file;
    private boolean write_lock;
    private int size;

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
        String ret = FileUtil.readFileAsString(file);

        if (ret == null)
            return "";
        else
            return ret;
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
}
