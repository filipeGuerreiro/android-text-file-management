package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 08/05/2015.
 */
public class UnshareFileTask extends AsyncTask<String, Void, Void> {

    private Workspace workspace;
    private String fileName;

    public UnshareFileTask(Workspace workspace, String fileName) {
        this.workspace = workspace;
        this.fileName = fileName;
    }

    protected Void doInBackground(String... operation) {

        Log.d(WifiAPI.TAG, "UnshareFileTask started (" + this.hashCode() + ").");

        for(Node node : WifiAPI.connectedNodes.values()) {
            if(node.getWorkspace(workspace) != null) {

                Log.d(WifiAPI.TAG, operation[0] + fileName + " to: " + node.getUserMail());
                String msgToSend = operation[0] + workspace.toJSON(fileName).toString();

                try {
                    node.getSocket().getOutputStream().write((msgToSend + "\n").getBytes());
                } catch (IOException e) {
                    Log.d(WifiAPI.TAG, "Error sending file to user");
                }
            }
        }
        return null;
    }

    protected void onProgressUpdate() {

    }

    protected void onPostExecute() {
        // end quietly
    }
}
