package pt.ulisboa.tecnico.cmov.airdesk.business;

import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.airdesk.exception.RepeatedNameException;

public interface Workspace{

    public String getName();

    public int getMaxSpace();

    public int getUsedSpace();

    Map<String, IFile> getFileMap();

    /**
     * Returns a list of all file names that belong to this LocalWorkspace
     * @return List<String>
     */
    public List<String> ls();

    public boolean del();

    /**
     * Creates a new file in this LocalWorkspace
     * @param fName
     * @throws RepeatedNameException
     */
    public IFile createFile(String fName) throws RepeatedNameException;

    void addFile(IFile newFile);

    public void removeFile(String fName);

    public IFile getFile(String name);

    public List<String> getTags();

    public String toString();

    public String getID();

}