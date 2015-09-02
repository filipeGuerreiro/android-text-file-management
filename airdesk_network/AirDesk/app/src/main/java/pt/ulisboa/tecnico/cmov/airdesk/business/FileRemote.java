package pt.ulisboa.tecnico.cmov.airdesk.business;

import org.json.JSONObject;

/**
 * Created by alex on 17-03-2015.
 */
public class FileRemote implements IFile {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean write(String text) {
        return false;
    }
    @Override
    public String read() {
        return null;
    }
    @Override
    public boolean del() {
        return true;
    }

    public void setText(String text) {}

    @Override
    public int getSize() {
        return 0;
    }
    public void setSize(int newSize) { }

    public boolean isShared() { return false; }
    public void setSharedStatus(boolean b) { }

    public int getTimestamp() { return 0; }
    public void setTimestamp(int t) { }

    public JSONObject toJSON() {return null; }
}
