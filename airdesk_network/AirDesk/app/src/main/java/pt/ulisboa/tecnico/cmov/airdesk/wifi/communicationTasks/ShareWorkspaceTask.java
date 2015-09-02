package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 30/04/2015.
 */
public class ShareWorkspaceTask extends AsyncTask<Void, Void, Void> {

    private static final int WAIT_TIME = 10000;

    final private String email;
    final private Workspace workspace;
    final private Node node;

    public ShareWorkspaceTask(String email, Workspace workspace) {
        this.email = email;
        this.workspace = workspace;
        this.node = null;
    }

    public ShareWorkspaceTask(Node node, Workspace workspace) {
        this.email = node.getUserMail();
        this.node = node;
        this.workspace = workspace;
    }

    protected Void doInBackground(Void... params) {

        Log.d(WifiAPI.TAG, "ShareWorkspaceTask started (" + this.hashCode() + ").");

        // Find node in the list with whom we wish to communicate
        Node foundNode = null;
        if(this.node != null) {
            foundNode = this.node;
        }
        else {
            for (Node n : WifiAPI.connectedNodes.values()) {
                Log.d(WifiAPI.TAG, "ShareWorkspace - Comparing email " + email + " with " + n.getUserMail());
                if (n.getUserMail() != null && n.getUserMail().equals(email)) {
                    foundNode = n;
                    Log.d(WifiAPI.TAG, "ShareWorkspaceTask: Found node with email '" + email + "'");
                    break;
                }
            }
        }

        // This loop will keep indefinitely in case the user leaves but comes back later
        //while(true) {

            // Only send if the node is in range and doesn't have the workspace yet
            if(foundNode != null && foundNode.getSocket() != null /*&& node.getWorkspace(workspace) == null*/) {

                Log.d(WifiAPI.TAG, "Sending workspace "+workspace.getName()+" to: "+foundNode.getUserMail());

                String msgToSend = WifiAPI.MSG_PREFIX_WORKSPACE + workspace.toJSON().toString();

                try {
                    foundNode.getSocket().getOutputStream().write((msgToSend + "\n").getBytes());
                } catch (IOException e) {
                    Log.d(WifiAPI.TAG, "Error sending workspace to user");
                }

                foundNode.addWorkspace(workspace);

                // add the reference to know that the user already has the workspace
                //node.addWorkspace(workspace);

            } /*else { // if it is null, the user left so remove the reference
                if(node != null) { //TODO:
                    //node.removeWorkspace(workspace);
                }
            }

            // Sleep for a few seconds before announcing again
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        return null;
    }

    protected void onProgressUpdate() {

    }

    protected void onPostExecute() {
        // end quietly
    }
}