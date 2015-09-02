package pt.ulisboa.tecnico.cmov.airdesk.business;

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

    @Override
    public int getSize() {
        return 0;
    }

    public void setSize(int newSize) { }
}
