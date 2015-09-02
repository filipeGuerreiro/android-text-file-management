package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 27/04/2015.
 */
public class AnnounceTagsTask extends AsyncTask<LocalWorkspace, Void, Void> {

    private static final int ANNOUNCE_WAIT_TIME = 30000;

    /**
     * This task is executed from the Visibility Activity when the workspace is
     * set to public.
     * @param workspace that is meant to be shared.
     * @return
     */
    protected Void doInBackground(LocalWorkspace... workspace) {

        Log.d(WifiAPI.TAG, "AnnounceTagsTask started (" + this.hashCode() + ").");

        // Send every few seconds while the workspace is still public
        while(workspace[0].isPublic()) {

            // Concatenate the list of tags
            String tags = "";
            for(String tag : workspace[0].getTags()) {
                tags += " "+tag;
            }

            // Send message like: 'TAG:SnowTrip CHEESE SNOW BUTTERFLY'
            String message = WifiAPI.MSG_PREFIX_TAG + workspace[0].getName() + " " + tags + "\n";

            // Send it to all nodes in the network
            for(Node node : WifiAPI.connectedNodes.values()) {
                SimWifiP2pSocket socket = node.getSocket();

                try {
                    socket.getOutputStream().write((message).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Sleep for a few seconds before announcing again
            try {
                Thread.sleep(ANNOUNCE_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
