package pt.ulisboa.tecnico.cmov.airdesk.business;

/**
 * Created by alex on 17-03-2015.
 */
public interface IFile {

    public String getName();

    public boolean write(String text);
    public String read();
    public boolean del();

    public int getSize();
    public void setSize(int newSize);
}
