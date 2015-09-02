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

/**
 * Created by alex on 23-03-2015.
 *
 * sniped from :
 * developer.android.com/training/notepad/index.html
 */
public class FileEditActivity extends Activity {

    public static final String EXTRA_FILE_NAME = "pt.ulisboa.tecnico.cmov.airdesk.gui#EXTRA_FILE_NAME";
    public static final String EXTRA_WORKSPACE_POS = "pt.ulisboa.tecnico.cmov.airdesk.gui#EXTRA_WORKSPACE_POS";

    private EditText mTitleText;
    private EditText mBodyText;
    private IFile file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalContext globalContext = GlobalContext.getGC();
        User user = globalContext.loggedInUser;
        String fName = this.getIntent().getExtras().getString(EXTRA_FILE_NAME);
        final Workspace activeWorkspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());
        file = activeWorkspace.getFile(fName);
        final int initFileSize = file.getSize();

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

                if(currentTotalSize + newTextSizeDiff <= maximumQuota) {
                    setResult(RESULT_OK);
                    saveState();
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

    private void saveState() {
        boolean ret = file.write(mBodyText.getText().toString());
        Log.i(Constants.LOG_TAG, String.format("FileEditActivity.saveState(), file: %s, success: %b", file.getName(), ret));
    }
}