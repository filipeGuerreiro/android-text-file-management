package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 02/05/2015.
 */
public class UnshareWorkspaceTask extends AsyncTask<Void, Void, Void> {

    final private String email;
    final private Workspace workspace;

    public UnshareWorkspaceTask(String email, Workspace workspace) {
        this.email = email;
        this.workspace = workspace;
    }

    protected Void doInBackground(Void... params) {

        Log.d(WifiAPI.TAG, "UnshareWorkspaceTask started (" + this.hashCode() + ").");

        // Find node in the list with whom we wish to communicate
        Node node = null;
        for(Node n : WifiAPI.connectedNodes.values()) {
            Log.d(WifiAPI.TAG, "Comparing email "+email+" with "+n.getUserMail());
            if(n.getUserMail() != null && n.getUserMail().equals(email)) {
                node = n;
                Log.d(WifiAPI.TAG, "UnshareWorkspaceTask: Found node with email '"+ email+"'");
                break;
            }
        }

        // This loop will keep indefinitely in case the user leaves but comes back later
        //while(true) {

            // Only send if the node is in range and doesn't have the workspace yet
            if(node != null && node.getSocket() != null /*&& node.getWorkspace(workspace) == null*/) {

                Log.d(WifiAPI.TAG, "Removing workspace "+workspace.getName()+" from: "+node.getUserMail());

                String msgToSend = WifiAPI.MSG_PREFIX_REM_WORKSPACE + workspace.getName() + " " + workspace.getOwnerMail();

                try {
                    node.getSocket().getOutputStream().write((msgToSend + "\n").getBytes());
                } catch (IOException e) {
                    Log.d(WifiAPI.TAG, "Error sending workspace to user");
                }

                node.removeWorkspace(workspace);

                // add the reference to know that the user already has the workspace
                //node.addWorkspace(workspace); // TODO: probably not needed

            } /*else { // if it is null, the user left so remove the reference
                if(node != null) { //TODO:
                    //node.removeWorkspace(workspace);
                }
            }*/

            // Sleep for a few seconds before announcing again
            /*try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        return null;
    }

    protected void onProgressUpdate() {

    }

    protected void onPostExecute() {
        // end quietly
    }
}