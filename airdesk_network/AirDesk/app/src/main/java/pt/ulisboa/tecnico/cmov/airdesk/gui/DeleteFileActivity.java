package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;

/**
 * Created by Filipe on 28/03/2015.
 */
public class DeleteFileActivity extends Activity {

    public static String WORKSPACE_ISOWNED = "pt.ulisboa.tecnico.cmov.airdesk.gui#WORKSPACE_ISOWNED";

    private ListView filesListView;
    private ArrayAdapter<String> filesAdapter;

    private Workspace activeWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_file);

        Bundle extras = getIntent().getExtras();
        boolean isOwned = extras.getBoolean(WORKSPACE_ISOWNED);

        GlobalContext globalContext = GlobalContext.getGC();

        User user = globalContext.loggedInUser;
        if(isOwned) {
            activeWorkspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());
        } else {
            activeWorkspace = user.getForeignWorkspace(globalContext.getWorkspaceIndex());
        }

        filesListView = (ListView) findViewById(R.id.ownedFilesListView);
        filesAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1);
        loadFiles();

        filesListView.setAdapter(filesAdapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fName = filesAdapter.getItem(position);

                showConfirmWindow(fName);
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

    private void loadFiles() {

        filesAdapter.clear();
        filesAdapter.addAll(activeWorkspace.ls());
    }

    public void showConfirmWindow(final String fName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        WifiAPI.unshareFile(activeWorkspace, fName);
                        activeWorkspace.removeFile(fName);
                        loadFiles();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
