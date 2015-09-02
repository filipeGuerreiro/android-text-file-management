package pt.ulisboa.tecnico.cmov.airdesk.wifi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.business.IFile;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.gui.MainActivity;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.AnnounceTagsTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.IncomingCommTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.OutgoingCommTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.ReceiveCommTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.ShareFileTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.ShareWorkspaceTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.UnshareFileTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks.UnshareWorkspaceTask;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 24/04/2015.
 */
public class WifiAPI implements
        SimWifiP2pManager.PeerListListener,
        SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "airdesk";

    public static MainActivity mainActivity;

    public static final int SEPARATOR = ':';
    public static final String MSG_PREFIX_USER = "USER:";
    public static final String MSG_PREFIX_WORKSPACE = "WORKSPACE:";
    public static final String MSG_PREFIX_REM_WORKSPACE = "REM_WORKSPACE:";
    public static final String MSG_PREFIX_ASK_WORKSPACE = "ASK_WORKSPACE:";
    public static final String MSG_PREFIX_TAG = "TAG:";
    public static final String MSG_PREFIX_FILE_EDIT = "FILE_EDIT:";
    public static final String MSG_PREFIX_NEW_FILE = "NEW_FILE:";
    public static final String MSG_PREFIX_DELETE_FILE = "DELETE_FILE:";

    public static SimWifiP2pManager mManager = null;
    public static SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    public static boolean mBound = false;

    private TextView mTextInput;
    private TextView mTextOutput;
    private ServiceConnection mConnection = defServiceConnection();

    public static ReceiveCommTask mComm = null;
    public static SimWifiP2pSocketServer mSrvSocket = null;
    public static List<SimWifiP2pSocket> mCliSockets = new ArrayList<>();
    private List<String> groupIPs = new ArrayList<>();

    public static SimWifiP2pDeviceList inRangeDevices = new SimWifiP2pDeviceList();
    public static SimWifiP2pInfo mGInfo = new SimWifiP2pInfo();

    public static TreeMap<String, Node> connectedNodes = new TreeMap<String, Node>();

    public static final Executor myExecutor =
            new ThreadPoolExecutor(10 /*core*/, 50 /*max*/, 60 /*timeout*/,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public WifiAPI(MainActivity mainActivity) {

        WifiAPI.mainActivity = mainActivity;

        // Initialize the Termite API
        SimWifiP2pSocketManager.Init(mainActivity.getApplicationContext());

        // Register in interesting events
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(mainActivity, this);
        mainActivity.registerReceiver(receiver, filter);
    }

    /**
     * Functions for starting/stopping Wifi.
     */
    public void initWifi() {

        // Binds the Termite service
        Intent intent = new Intent(mainActivity, SimWifiP2pService.class);
        mainActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //Log.d(WifiAPI.TAG, "initWifi mManager : "+ mManager);
        mBound = true;
    }

    public void stopWifi() {
        if (mBound) {
            mainActivity.unbindService(mConnection);
            mBound = false;
            //guiUpdateInitState();

            // TODO: stop tasks?

            //closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (WifiAPI.mSrvSocket != null)
                WifiAPI.mSrvSocket.close();
            WifiAPI.mSrvSocket = null;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Function that initializes the Termite service connection.
     * Defines two callbacks that are called when the connection is successful or otherwise.
     */
    private ServiceConnection defServiceConnection() {
        return new ServiceConnection() {
            // callbacks for service binding, passed to bindService()

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = new Messenger(service);
                mManager = new SimWifiP2pManager(mService);
                mChannel = mManager.initialize(mainActivity.getApplication(), mainActivity.getMainLooper(), null);
                mBound = true;
                Log.d(WifiAPI.TAG, "onServiceConnected mManager : "+ mManager);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mManager = null;
                mChannel = null;
                mBound = false;
                Log.d(WifiAPI.TAG, "onServiceDisconnected mManager : "+ mManager);
            }
        };
    }

    /**
     * Method called from the Visibility Activity when the user
     * clicks the 'Make public' button
     * Starts a new Async task to send tags to every machine in the network.
     *
     * @param workspace the workspace that the owner wishes to make public
     */
    public static void startSendingTags(LocalWorkspace workspace) {

        new AnnounceTagsTask().executeOnExecutor(myExecutor, workspace);
    }

    /**
     * Method called from the ACL Activity when the owner
     * adds a new user to the list.
     * Starts a new Async task to send and manage the workspace activities.
     *
     * @param email            the email of the user that the owner wishes to invite
     * @param currentWorkspace the workspace that the owner wishes to share
     */
    public static void inviteUser(String email, Workspace currentWorkspace) {

        new ShareWorkspaceTask(email, currentWorkspace).executeOnExecutor(myExecutor);
    }

    /**
     * The dual to the inviteUser method.
     * Starts a new AsyncTask to send a message to the user so she can remove
     * the workspace from her application.
     *
     * @param userToBeRemoved  the email of the user to be removed
     * @param currentWorkspace the workspace that needs to be removed from the user
     */
    public static void uninviteUser(String userToBeRemoved, Workspace currentWorkspace) {

        new UnshareWorkspaceTask(userToBeRemoved, currentWorkspace).executeOnExecutor(myExecutor);
    }

    /**
     * Method called from the FileEdit activity when the user
     * confirms the file changes.
     * Starts a new AsyncTask to update the other clients' file replicas.
     *
     * @param file the file to be shared
     */
    public static void shareFile(Workspace workspace, IFile file) {

        new ShareFileTask(workspace, file).executeOnExecutor(myExecutor, WifiAPI.MSG_PREFIX_FILE_EDIT);
    }

    /**
     * Method called from the DeleteFile activity when the user
     * wants to delete a file.
     * Starts a new AsyncTask to delete the file from the other clients' workspaces.
     *
     * @param workspace the workspace to which the file belongs
     * @param fName     the file name of the file to be removed
     */
    public static void unshareFile(Workspace workspace, String fName) {

        new UnshareFileTask(workspace, fName).executeOnExecutor(myExecutor, WifiAPI.MSG_PREFIX_DELETE_FILE);
    }

    /**
     * Method called from the ViewOwnedWorkspace and ViewForeignWorkspace activities.
     * Starts a new AsyncTask to create a new file in other clients' workspaces.
     *
     * @param workspace the workspace to which the file belongs
     * @param newFile   the file that has been edited
     */
    public static void shareNewFile(Workspace workspace, IFile newFile) {

        new ShareFileTask(workspace, newFile).executeOnExecutor(myExecutor, WifiAPI.MSG_PREFIX_NEW_FILE);
    }

    /*
    * Listeners associated to WDSim
    */
    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        // Not used
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // If it's the GO, wait for connections, else send connection requests
        if (groupInfo.askIsGO()) {
            // Start waiting for connections
            if (WifiAPI.mSrvSocket == null) {
                Log.d(WifiAPI.TAG, "I'm the GO, starting server socket.");
                new IncomingCommTask().executeOnExecutor(myExecutor);
            }

        } else {
            Log.d(WifiAPI.TAG, "I'm a client, starting connections.");

            // Start a task to receive incoming connections in case another client wants to communicate
            if(mSrvSocket == null) {
                new IncomingCommTask().executeOnExecutor(myExecutor);
            }

            // Compile list of network members
            List<String> gottenIPs = new ArrayList<>();
            for (String deviceName : groupInfo.getDevicesInNetwork()) {
                SimWifiP2pDevice device = devices.getByName(deviceName);
                gottenIPs.add(device.getVirtIp());
            }

            // Remove IPs that left
            groupIPs.retainAll(gottenIPs);

            // Keep only new devices
            gottenIPs.removeAll(groupIPs);

            // Start outgoing communication tasks with NEW devices
            for (String deviceIP : gottenIPs) {

                //if (!groupIPs.contains(deviceIP)) {

                Log.d(WifiAPI.TAG, "Starting outgoing communication with " + deviceIP);
                //mainActivity.runOnUiThread(
                new OutgoingCommTask().executeOnExecutor(myExecutor, deviceIP);

                groupIPs.add(deviceIP);
                //}
            }
        }
    }

    /**
     * Removes/Adds the InRangeDevices var with the latest changes in devList.
     *
     * @param devList
     */
    protected void updateInRangeDevices(SimWifiP2pDeviceList devList) {
        WifiAPI.inRangeDevices.mergeUpdate(devList);
    }


    protected void updateGroup(SimWifiP2pInfo gInfoUpdate) {

        if (mGInfo.askHasGroupMembershipChanged(gInfoUpdate)) {
            User user = GlobalContext.getGC().loggedInUser;
            Set<String> oldDevs = mGInfo.getDevicesInNetwork();
            Set<String> newDevs = gInfoUpdate.getDevicesInNetwork();

            TreeSet<String> disconnectedDevs = new TreeSet<String>(oldDevs);
            disconnectedDevs.removeAll(newDevs);

            for (String dev : disconnectedDevs) {
                Node node = connectedNodes.remove(dev);
                user.removeForeignWorkspace(node.getUserMail());
                Log.d(WifiAPI.TAG, String.format("Disconnected from: %s", dev));
            }

            TreeSet<String> connectedDevs = new TreeSet<String>(newDevs);
            disconnectedDevs.removeAll(oldDevs);

            for (String dev : connectedDevs) {
                if (gInfoUpdate.askIsConnectionPossible(dev)) {
                    Log.d(WifiAPI.TAG, String.format("New dev: %s (connected)", dev));
                } else {
                    Log.d(WifiAPI.TAG, String.format("New dev: %s (not connected)", dev));
                }
            }

            mGInfo.mergeUpdate(gInfoUpdate);
        }
    }
}
