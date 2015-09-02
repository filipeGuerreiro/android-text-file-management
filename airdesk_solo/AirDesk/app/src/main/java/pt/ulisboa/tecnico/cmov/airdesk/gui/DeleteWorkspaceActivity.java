package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;

/**
 * Created by Filipe on 19/03/2015.
 */
public class DeleteWorkspaceActivity extends Activity {

    private enum TYPE_WORKSPACE {OWNED, FOREIGN};

    GlobalContext globalContext;

    private ListView ownedWorkspacesListView;
    private ListView foreignWorkspacesListView;

    private ArrayAdapter<LocalWorkspace> ownedWorkspacesAdapter;
    private ArrayAdapter<Workspace> foreignWorkspacesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_workspace);

        ownedWorkspacesListView = (ListView) findViewById(R.id.ownedWorkspacesListView);
        foreignWorkspacesListView = (ListView) findViewById(R.id.foreignWorkspacesListView);

        globalContext = GlobalContext.getGC();
        final User user = globalContext.loggedInUser;
        ownedWorkspacesAdapter = MainActivity.ownedWorkspacesAdapter;
        foreignWorkspacesAdapter = MainActivity.foreignWorkspacesAdapter;

        ownedWorkspacesListView.setAdapter(ownedWorkspacesAdapter);
        ownedWorkspacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showConfirmWindow(user, position, TYPE_WORKSPACE.OWNED);
            }
        });

        foreignWorkspacesListView.setAdapter(foreignWorkspacesAdapter);
        foreignWorkspacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showConfirmWindow(user, position, TYPE_WORKSPACE.FOREIGN);
            }
        });
    }

    public void finishActivity(View view) {
        this.finish();
    }

    public void showConfirmWindow(final User user, final int position, final TYPE_WORKSPACE type) {
        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(type == TYPE_WORKSPACE.OWNED) {
                            user.removeOwnedWorkspace(position);
                        } else {
                            user.removeForeignWorkspace(position);
                        }
                        finishActivity(null);
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
