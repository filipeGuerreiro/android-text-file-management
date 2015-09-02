package pt.ulisboa.tecnico.cmov.airdesk.wifi.communicationTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.airdesk.business.DummyFile;
import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.business.IFile;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.exception.RepeatedNameException;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.network.Node;

/**
 * Created by Filipe on 26/04/2015.
 */
public class ReceiveCommTask extends AsyncTask<Node, String, Void> {
    //private Node node;
    //private SimWifiP2pSocket socket;

    @Override
    protected Void doInBackground(Node... params) {
        BufferedReader sockIn;
        String st;
        int nConfirmMsgs = 0;

        Log.d(WifiAPI.TAG, "ReceiveCommTask started (" + this.hashCode() + ").");

        //socket = params[0];
        Node node = params[0];
        SimWifiP2pSocket s = node.getSocket();

        try {
            sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

            while (true) {

                if ((st = sockIn.readLine()) != null) {
                    Log.d(WifiAPI.TAG, "Message received: " + st);

                    int separator = st.indexOf(WifiAPI.SEPARATOR);
                    String msgPrefix = st.substring(0, separator + 1);
                    String msgContent = st.substring(separator + 1);

                    switch (msgPrefix) {
                        case WifiAPI.MSG_PREFIX_USER:
                            nConfirmMsgs = handleNewUserInput(node, msgContent, nConfirmMsgs);
                            break;

                        case WifiAPI.MSG_PREFIX_WORKSPACE:
                            handleWorkspaceInput(msgContent, node);
                            break;

                        case WifiAPI.MSG_PREFIX_REM_WORKSPACE:
                            handleRemoveWorkspaceInput(msgContent, node);
                            break;

                        case WifiAPI.MSG_PREFIX_TAG:
                            handleTagInput(msgContent, node);
                            break;

                        case WifiAPI.MSG_PREFIX_ASK_WORKSPACE:
                            handleAskWorkspaceInput(msgContent, node);
                            break;

                        case WifiAPI.MSG_PREFIX_FILE_EDIT:
                            handleFileEditInput(msgContent);
                            break;

                        case WifiAPI.MSG_PREFIX_NEW_FILE:
                            handleFileEditInput(msgContent);
                            break;

                        case WifiAPI.MSG_PREFIX_DELETE_FILE:
                            handleFileRemoveInput(msgContent);
                            break;

                        default:
                            Log.d(WifiAPI.TAG, "Prefix received '" + msgPrefix + "' does not have a match.");
                    }

                    publishProgress(st);

                }
            }
        } catch (IOException e) {
            Log.d("Error reading socket:", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        /*mTextOutput.setText("");
        findViewById(R.id.idSendButton).setEnabled(true);
        findViewById(R.id.idDisconnectButton).setEnabled(true);
        findViewById(R.id.idConnectButton).setEnabled(false);
        mTextInput.setHint("");
        mTextInput.setText("");*/

    }

    @Override
    protected void onProgressUpdate(String... values) {

        Toast.makeText(WifiAPI.mainActivity, values[0], Toast.LENGTH_SHORT).show();

        WifiAPI.mainActivity.redrawUI();
    }

    /**
     * Function that handles the input received when a user comes
     * into the network.
     * @param node
     * @param msgContent
     */
    private int handleNewUserInput(Node node, String msgContent, int nConfirmMsgs) {
        node.setUserMail(msgContent);
        // Send back an answer with my info in case I'm GO
        if(nConfirmMsgs < 1) {
            String userID = GlobalContext.getGC().loggedInUser.getMail();
            String message = WifiAPI.MSG_PREFIX_USER + userID;
            try {
                node.getSocket().getOutputStream().write((message + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            nConfirmMsgs++;
        }
        return nConfirmMsgs;
    }

    /**
     * Function used when receiving a workspace message.
     * This parses the message content for workspaces and their
     * respective files in JSON format.
     * @param msgContent
     */
    private void handleWorkspaceInput(String msgContent, Node node) {
        JSONObject jsonObject = null;
        String name = null;
        String owner = null;
        int size = -1;
        List<DummyFile> listFiles = new ArrayList<>();

        // Get all the workspace info and list of files
        try {
            jsonObject = new JSONObject(msgContent);
            name = jsonObject.getString("name");
            owner = jsonObject.getString("owner");
            size = jsonObject.getInt("size");
            JSONArray arrayJSONFiles = jsonObject.getJSONArray("listFiles");
            for(int i = 0; i < arrayJSONFiles.length(); ++i) {
                String fileName = arrayJSONFiles.getJSONObject(i).getString("name");
                int fileSize = arrayJSONFiles.getJSONObject(i).getInt("size");
                String fileText = arrayJSONFiles.getJSONObject(i).getString("text");
                int timestamp = arrayJSONFiles.getJSONObject(i).getInt("timestamp");
                listFiles.add(new DummyFile(fileName, fileSize, fileText, timestamp));
            }

        } catch(JSONException e) {
            Log.d(WifiAPI.TAG, "Error obtaining object from workspace JSON: " + e.getMessage());
            return;
        }

        Log.d(WifiAPI.TAG, "Created foreign workspace: " + msgContent);
        //for(Node n : WifiAPI.connectedNodes) Log.d(WifiAPI.TAG, "Node email is...: "+n.getNodeID());
        Workspace foreignWorkspace = GlobalContext.getGC().loggedInUser.addForeignWorkspace(name, size, owner);
        foreignWorkspace.setMaxSize(size);
        node.addWorkspace(foreignWorkspace);

        // Add all the files to the workspace
        for(DummyFile file : listFiles) {
            IFile newFile = null;
            try {
                newFile = foreignWorkspace.createFile(file.getName());
                newFile.write(file.getText());
                newFile.setTimestamp(file.getTimestamp());
                Log.d(WifiAPI.TAG, "Created foreign file: " + newFile.getName());
            } catch(RepeatedNameException e) {
                //
                e.printStackTrace();
                assert false;
            }
        }
    }

    /**
     * Function that handles a remove workspace message.
     * Simply removes the workspace from the user specified
     * in the msgContent.
     * @param msgContent
     */
    private void handleRemoveWorkspaceInput(String msgContent, Node node) {

        int separator = msgContent.indexOf(' ');
        String workspaceName = msgContent.substring(0, separator);
        String user = msgContent.substring(separator+1);

        Log.d(WifiAPI.TAG, "Removing foreign workspace: " + workspaceName + " owner: " + user);
        //for(Node n : WifiAPI.connectedNodes) Log.d(WifiAPI.TAG, "Node email is...: "+n.getNodeID());
        GlobalContext.getGC().loggedInUser.removeForeignWorkspace(workspaceName, user);
        node.removeWorkspace(user, workspaceName);
    }

    /**
     * Function used to handle a tag message
     * searches all the PUBLIC workspaces this machine has available
     * and sends the ones that have a matching tag.
     * @param msgContent
     */
    private void handleTagInput(String msgContent, Node node) {

        int separator = msgContent.indexOf(' ');
        String workspaceName = msgContent.substring(0, separator);

        // Check if I already have the workspace
        boolean hasWorkspace = GlobalContext.getGC().loggedInUser.
                doesForeignWorkspaceExist(workspaceName, node.getUserMail());

        // Ignore message if I already have the workspace, else ask for the workspace
        if(!hasWorkspace) {

            // Check if the workspace is wanted by this user! -- Matching tags
            String tagString = msgContent.substring(separator + 2, msgContent.length());
            String[] tagArray = tagString.split(" ");

            ArrayList<String> workspaceTags = new ArrayList<>();
            workspaceTags.addAll(Arrays.asList(tagArray));
            Log.d(WifiAPI.TAG, workspaceName+" - Tags found: "+workspaceTags.toString());

            ArrayList<String> userTags = (ArrayList)GlobalContext.getGC().loggedInUser.getTags();

            // If the user has any tag matching the workspace tags, send a request to the owner
            workspaceTags.retainAll(userTags);
            Log.d(WifiAPI.TAG, workspaceName+" - Tags matching: "+workspaceTags.toString());
            if (workspaceTags.size() > 0) {
                String askWorkspaceMessage = WifiAPI.MSG_PREFIX_ASK_WORKSPACE + workspaceName + "\n";

                SimWifiP2pSocket socket = node.getSocket();
                try {
                    socket.getOutputStream().write(askWorkspaceMessage.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * This message is received when the client asks explicitly for a workspace,
     * usually when she has found a matching tag in the function above.
     * Sends back the workspace that was requested in a JSON format.
     * @param workspaceName
     * @param node
     */
    private void handleAskWorkspaceInput(String workspaceName, Node node) {

        Workspace workspace = GlobalContext.getGC().loggedInUser.getOwnedWorkspace(workspaceName);

        if(workspace == null) {
            Log.d(WifiAPI.TAG, "Received request for non-existent workspace: " + workspaceName);
            return;
        }

        workspace.getTags().add(node.getUserMail());
        new ShareWorkspaceTask(node, workspace).executeOnExecutor(WifiAPI.myExecutor);
    }

    /**
     * This message is received when another client that shares a workspace with
     * this client makes a change to one of the files.
     * This updates the timestamp of the local file
     * if the foreign file was up to date.
     * @param msgContent the file comes along with the workspace in a JSON format
     */
    private void handleFileEditInput(String msgContent) {

        JSONObject jsonObject = null;
        String workspaceName = null;
        String workspaceOwner = null;
        int workspaceSize = -1;

        String fileName = null;
        int fileSize = -1;
        String fileText = null;
        int timeStamp = -1;

        // Get the workspace info and edited file
        try {
            jsonObject = new JSONObject(msgContent);
            workspaceName = jsonObject.getString("name");
            workspaceOwner = jsonObject.getString("owner");
            workspaceSize = jsonObject.getInt("size");

            JSONObject jsonFile = jsonObject.getJSONObject("editedFile");
            fileName = jsonFile.getString("name");
            fileText = jsonFile.getString("text");
            timeStamp = jsonFile.getInt("timestamp");

        } catch(JSONException e) {
            Log.d(WifiAPI.TAG, "Error obtaining object from workspace JSON: " + e.getMessage());
            return;
        }

        Log.d(WifiAPI.TAG, "Edit request for file: " + fileName + " with text: "+fileText);
        // Get the file reference
        User user = GlobalContext.getGC().loggedInUser;
        Workspace workspace = null;
        if(workspaceOwner.equals(user.getMail())) {
            workspace = user.getOwnedWorkspace(workspaceName);
        } else {
            workspace = user.getForeignWorkspace(workspaceOwner, workspaceName);
            workspace.setMaxSize(workspaceSize);
        }
        IFile file = (IFile)workspace.getFile(fileName);

        // If it's a new file
        if(file == null) {
            try {
                file = workspace.createFile(fileName);
                file.write(fileText);
                file.setTimestamp(timeStamp);
                Log.d(WifiAPI.TAG, "Created foreign file: " + file.getName());
            } catch(RepeatedNameException e) {
                // Ignore
            }
        }
        else {
            Log.d(WifiAPI.TAG, "Clock received: " + timeStamp + " and mine: " + file.getTimestamp());
            // Check the timestamps
            if (timeStamp > file.getTimestamp()) {

                file.write(fileText);
                file.setTimestamp(timeStamp);
            } else if (timeStamp == file.getTimestamp()) {

                // TODO: use a clock timestamp
            }
        }
    }

    /**
     * Handles the input for removing the file from the workspace.
     * @param msgContent the workspace with the file to be removed
     */
    private void handleFileRemoveInput(String msgContent) {

        JSONObject jsonObject = null;
        String workspaceName = null;
        String workspaceOwner = null;
        int workspaceSize = -1;

        String fileName = null;

        // Get the workspace info and file
        try {
            jsonObject = new JSONObject(msgContent);
            workspaceName = jsonObject.getString("name");
            workspaceOwner = jsonObject.getString("owner");
            workspaceSize = jsonObject.getInt("size");

            fileName = jsonObject.getString("file");

        } catch(JSONException e) {
            Log.d(WifiAPI.TAG, "Error obtaining object from workspace JSON: " + e.getMessage());
            return;
        }

        Log.d(WifiAPI.TAG, "Removing file: " + fileName);
        // Get the file reference
        User user = GlobalContext.getGC().loggedInUser;
        Workspace workspace = null;
        if(workspaceOwner.equals(user.getMail())) {
            workspace = user.getOwnedWorkspace(workspaceName);
        } else {
            workspace = user.getForeignWorkspace(workspaceOwner, workspaceName);
            workspace.setMaxSize(workspaceSize);
        }
        workspace.removeFile(fileName);
    }

    @Override
    protected void onPostExecute(Void result) {
        /*if (!s.isClosed()) {
            try {
                s.close();
            }
            catch (Exception e) {
                Log.d("Error closing socket:", e.getMessage());
            }
        }
        s = null;*/
        /*if (WifiAPI.mBound) {
            //guiUpdateDisconnectedState();
        } else {
            //guiUpdateInitState();
        }*/
    }
}
