package pt.ulisboa.tecnico.cmov.airdesk.business;

/**
 * Created by Filipe on 05/05/2015.
 */
public class DummyFile {

    private String name;
    private int size;
    private String text;
    private int timestamp;

    public DummyFile(String name, int size, String text, int timestamp) {
        this.name = name;
        this.size = size;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getName() { return name; }

    public int getSize() { return size; }

    public String getText() { return text; }

    public int getTimestamp() { return timestamp; }
}
