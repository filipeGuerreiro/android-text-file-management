package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;

/**
 * Created by Filipe on 11/03/2015.
 */
public class CreateWorkspaceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workspace);
    }

    public void finishActivity(View view) {
        this.finish();
    }

    public void createWorkspace(View view) {

        EditText newWorkspaceEditText = (EditText) findViewById(R.id.nameEditText);
        String newWorkspaceName = newWorkspaceEditText.getText().toString();

        EditText newCapacityEditText = (EditText) findViewById(R.id.capacityEditText);

        int newCapacity = 0;
        try {
            newCapacity = Integer.parseInt(newCapacityEditText.getText().toString());
        } catch(NumberFormatException e) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Capacity is not a valid number.",
                    Toast.LENGTH_SHORT);
            t.show();
        }
        if(newCapacity < 1 || newWorkspaceName.trim().length() < 1) {
            return;
        }

        GlobalContext globalContext = (GlobalContext) getApplicationContext();
        User user = globalContext.loggedInUser;

        AlertDialog alertDialog = null;
        // Check if workspace already exists
        if(user.getOwnedWorkspace(newWorkspaceName) != null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Create workspace")
                    .setMessage("The workspace '"+newWorkspaceName+"' already exists. Please try again.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            user.createWorkspace(newWorkspaceName, newCapacity);
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Create workspace")
                    .setMessage("Workspace '"+newWorkspaceName+"' created.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        alertDialog.dismiss();
        this.finishActivity(view);
    }
}
