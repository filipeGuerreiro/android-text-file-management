package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.IFile;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.exception.RepeatedNameException;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;

/**
 * Created by Filipe on 25/03/2015.
 */
public class ViewForeignWorkspaceActivity extends Activity {

    private ListView filesListView;
    public static ArrayAdapter<String> filesAdapter;

    private static Workspace activeWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_foreignworkspace);

        GlobalContext globalContext = GlobalContext.getGC();

        User user = globalContext.loggedInUser;
        activeWorkspace = user.getForeignWorkspace(globalContext.getWorkspaceIndex());
        final TextView workspaceName = (TextView) findViewById(R.id.workspaceName);
        workspaceName.setText(activeWorkspace.getName());

        filesListView = (ListView) findViewById(R.id.foreignFilesListView);
        filesAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1);
        loadFiles();

        filesListView.setAdapter(filesAdapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ViewForeignWorkspaceActivity.this, FileEditActivity.class);

                String fName = filesAdapter.getItem(position);
                intent.putExtra(FileEditActivity.EXTRA_FILE_NAME, fName);
                intent.putExtra(FileEditActivity.EXTRA_WORKSPACE_TYPE, FileEditActivity.Workspace_Type.FOREIGN);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFiles();
    }

    public void finishActivity(View view) {
        this.finish();
    }

    public void createFileOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_file_name);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.enter_file_name);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    IFile newFile = activeWorkspace.createFile(input.getText().toString());
                    dialog.dismiss();
                    loadFiles();

                    WifiAPI.shareNewFile(activeWorkspace, newFile);

                } catch (RepeatedNameException e) {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder RepeatedNamebuilder = new AlertDialog.Builder(ViewForeignWorkspaceActivity.this);

                    //RepeatedNamebuilder.setMessage(R.string.dialog_repeated_file_name_msg)
                    //        .setTitle(R.string.dialog_repeated_file_name_title);

                    AlertDialog RepeatedNameDialog = RepeatedNamebuilder.create();
                    RepeatedNameDialog.show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void deleteFileOnClick(View view) {
        Intent intent = new Intent(ViewForeignWorkspaceActivity.this, DeleteFileActivity.class);
        intent.putExtra(DeleteFileActivity.WORKSPACE_ISOWNED, false);
        startActivity(intent);
    }

    public static void loadFiles() {

        filesAdapter.clear();
        filesAdapter.addAll(activeWorkspace.ls());
    }
}
