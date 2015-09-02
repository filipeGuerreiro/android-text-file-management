package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.business.IFile;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;

/**
 * Created by alex on 23-03-2015.
 *
 * sniped from :
 * developer.android.com/training/notepad/index.html
 */
public class FileEditActivity extends Activity {

    public static final String EXTRA_FILE_NAME = "pt.ulisboa.tecnico.cmov.airdesk.gui#EXTRA_FILE_NAME";
    public static final String EXTRA_FILE_POS = "pt.ulisboa.tecnico.cmov.airdesk.gui#EXTRA_FILE_POS";
    public static final String EXTRA_WORKSPACE_TYPE = "pt.ulisboa.tecnico.cmov.airdesk.gui#EXTRA_WORKSPACE_TYPE";

    public enum Workspace_Type { OWNED, FOREIGN }
    //public int WORKSPACETYPE_OWNED = 0;
    //public int WORKSPACETYPE_FOREEIGN = 1;

    private EditText mTitleText;
    private EditText mBodyText;
    private IFile file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalContext globalContext = GlobalContext.getGC();
        User user = globalContext.loggedInUser;
        String fName = this.getIntent().getExtras().getString(EXTRA_FILE_NAME);
        final Workspace_Type workspaceType = (Workspace_Type)this.getIntent().getExtras().get(EXTRA_WORKSPACE_TYPE);

        final Workspace activeWorkspace;
        if(workspaceType.equals(Workspace_Type.OWNED)) {
            activeWorkspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());
        } else {
            activeWorkspace = user.getForeignWorkspace(globalContext.getWorkspaceIndex());
        }

        file = activeWorkspace.getFile(fName);
        final int initFileSize = file.getSize();
        final int initTimestamp = file.getTimestamp();

        setContentView(R.layout.activity_file_edit);
        setTitle(R.string.editor);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // Check if new size exceeds max quota
                float newTextSizeDiff = mBodyText.getText().toString().length() - initFileSize;
                int currentTotalSize = activeWorkspace.getUsedSpace();
                int maximumQuota = activeWorkspace.getMaxSpace();

                if(file.getTimestamp() > initTimestamp) {
                    Toast.makeText(getApplicationContext(),
                            "Someone else edited this file in the meantime. Please exit and retry.",
                            Toast.LENGTH_SHORT).show();
                }

                else if(currentTotalSize + newTextSizeDiff <= maximumQuota) {
                    setResult(RESULT_OK);
                    saveState(workspaceType);

                    // Send update to other clients
                    WifiAPI.shareFile(activeWorkspace, file);

                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Changes exceed maximum quota.",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void populateFields() {
        mTitleText.setText(file.getName());
        mBodyText.setText(file.read());

        Log.d(Constants.LOG_TAG, "populateFields:" + file.read());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState(Workspace_Type workspaceType) {

        // If it's a foreign workspace, don't write to disk!
        if(workspaceType.equals(Workspace_Type.FOREIGN)) {

            file.setText(mBodyText.getText().toString());

        } else {

            boolean ret = file.write(mBodyText.getText().toString());
            //file.setTimestamp(file.getTimestamp()+1);
            Log.i(Constants.LOG_TAG, String.format("FileEditActivity.saveState(), file: %s, success: %b", file.getName(), ret));
        }
    }
}