package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 26/04/2015.
 */
public class IncomingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(WifiAPI.TAG, "IncomingCommTask started (" + this.hashCode() + ").");

        try {
            WifiAPI.mSrvSocket = new SimWifiP2pSocketServer(
                    Integer.parseInt(WifiAPI.mainActivity.getString(R.string.port)));

        } catch (IOException e) {
            e.printStackTrace();
        }

            try {

                while (!Thread.currentThread().isInterrupted()) {
                    if (WifiAPI.mSrvSocket != null) {

                        SimWifiP2pSocket sock = WifiAPI.mSrvSocket.accept();

                        publishProgress(sock);
                    }
                }
            } catch (IOException e) {
                Log.d("Error accepting socket:", e.getMessage());
            }
        return null;
    }


    @Override
    protected void onProgressUpdate(SimWifiP2pSocket... values) {

        Node newNode = new Node(values[0]);
        WifiAPI.connectedNodes.put(newNode.getUserMail(), newNode);

        Log.d(WifiAPI.TAG, "IncomingTask - New socket accepted!");

        WifiAPI.mComm = new ReceiveCommTask();
        WifiAPI.mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, newNode);
    }
}