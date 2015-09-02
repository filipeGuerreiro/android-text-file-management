package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 26/04/2015.
 */
public class OutgoingCommTask extends AsyncTask<String, Void, String> {

    private SimWifiP2pSocket mCliSocket = null;
    private Node mCliNode = null;

    @Override
    protected void onPreExecute() {

    }

    protected String doInBackground(String... IP) {

        Log.d(WifiAPI.TAG, "OutgoingCommTask started (" + this.hashCode() + ").");

        try {

            SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(IP[0], Integer.parseInt(WifiAPI.mainActivity.getString(R.string.port)));

            // When connecting, send the user ID
            String userID = GlobalContext.getGC().loggedInUser.getMail();
            String message = WifiAPI.MSG_PREFIX_USER + userID;
            mCliSocket.getOutputStream().write((message + "\n").getBytes());

            mCliNode = new Node(mCliSocket);
            mCliNode.setIpAddress(IP[0]);
            WifiAPI.connectedNodes.put(mCliNode.getUserMail(), mCliNode);
            //WifiAPI.mCliSockets.add(mCliSocket);
            Log.d(WifiAPI.TAG, "OutgoingCommTask - Created new node: " + IP[0]);

        } catch (UnknownHostException e) {
            return "Unknown Host:" + e.getMessage();
        } catch (IOException e) {
            return "IO error:" + e.getMessage();
        }
        return null;
    }

    protected void onProgressUpdate() {

    }

    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(WifiAPI.mainActivity, result, Toast.LENGTH_SHORT).show();
            //findViewById(R.id.idConnectButton).setEnabled(true);
        }
        else {
            WifiAPI.mComm = new ReceiveCommTask();
            WifiAPI.mComm.executeOnExecutor(WifiAPI.myExecutor, mCliNode);
        }
    }

}
