package pt.ulisboa.tecnico.cmov.airdesk.wifi.network;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;

/**
 * Created by alex on 12-03-2015.
 */
public class Node {

    private String userMail;
    private SimWifiP2pSocket socket;
    private String ipAddress;
    private List<Workspace> sharedWorkspaces = new ArrayList<>();

    public Node(SimWifiP2pSocket socket) {
        this.socket = socket;
    }

    public Node(String userMail, SimWifiP2pSocket sock) {
        this.userMail = userMail;
        socket = sock;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public SimWifiP2pSocket getSocket() {
        return socket;
    }

    public void setNodeSocket(SimWifiP2pSocket newSocket) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            Log.d("Error closing socket:", e.getMessage());
        }

        socket = newSocket;
    }

    public Workspace getWorkspace(Workspace workspace) {
        for (Workspace w : sharedWorkspaces) {
            if (w.getName().equals(workspace.getName())) {
                return w;
            }
        }
        return null;
    }

    public void addWorkspace(Workspace newWorkspace) {
        sharedWorkspaces.add(newWorkspace);
    }

    public void removeWorkspace(Workspace workspace) {
        sharedWorkspaces.remove(workspace);
    }

    public void removeWorkspace(String user, String workspaceName) {
        int i = 0;
        for (Workspace w : sharedWorkspaces) {
            if (w.getOwnerMail().equals(user) && w.getName().equals(workspaceName)) {
                sharedWorkspaces.remove(i);
                return;
            }
            i++;
        }
    }

    public void setIpAddress(String IP) {
        this.ipAddress = IP;
    }
}
