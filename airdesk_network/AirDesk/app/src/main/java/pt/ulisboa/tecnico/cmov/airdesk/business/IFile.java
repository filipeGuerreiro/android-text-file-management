package pt.ulisboa.tecnico.cmov.airdesk.business;

import org.json.JSONObject;

/**
 * Created by alex on 17-03-2015.
 */
public interface IFile {

    public String getName();

    public boolean write(String text);
    public String read();
    public boolean del();

    public void setText(String text);

    public int getSize();
    public void setSize(int newSize);

    public boolean isShared();
    public void setSharedStatus(boolean b);

    public int getTimestamp();
    public void setTimestamp(int t);

    public JSONObject toJSON();
}
