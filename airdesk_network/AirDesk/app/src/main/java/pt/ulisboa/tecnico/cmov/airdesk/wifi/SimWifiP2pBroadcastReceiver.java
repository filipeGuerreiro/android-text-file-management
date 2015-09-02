package pt.ulisboa.tecnico.cmov.airdesk.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.airdesk.business.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.gui.MainActivity;

/**
 * Created by Filipe on 24/04/2015.
 */
public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private MainActivity mActivity;
    private WifiAPI mAPI;

    public SimWifiP2pBroadcastReceiver(MainActivity activity, WifiAPI api) {
        super();
        this.mActivity = activity;
        this.mAPI = api;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the WDSim service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Log.i(Constants.LOG_TAG, "WiFi Direct enabled");
                Toast.makeText(mActivity, "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.i(Constants.LOG_TAG, "WiFi Direct disabled");
                Toast.makeText(mActivity, "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            Log.d(Constants.LOG_TAG, "Peer list changed");
            Toast.makeText(mActivity, "Peer list changed",
                    Toast.LENGTH_SHORT).show();

            SimWifiP2pDeviceList devList = (SimWifiP2pDeviceList) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);

            String devsStr = "";
            for (SimWifiP2pDevice dev : devList.getDeviceList()) {
                devsStr += dev.deviceName + " ";
            }
            Log.d(Constants.LOG_TAG, "Peer list: " + devsStr);


        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mActivity, "Network membership changed",
                    Toast.LENGTH_SHORT).show();

            //mAPI.updateGroup(ginfo);
            while(true) {
                if (WifiAPI.mChannel != null && WifiAPI.mManager != null) {
                    WifiAPI.mManager.requestGroupInfo(WifiAPI.mChannel,
                            (SimWifiP2pManager.GroupInfoListener) MainActivity.wifiAPI);
                    break;
                }
            }


        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mActivity, "Group ownership changed",
                    Toast.LENGTH_SHORT).show();

        }
    }
}