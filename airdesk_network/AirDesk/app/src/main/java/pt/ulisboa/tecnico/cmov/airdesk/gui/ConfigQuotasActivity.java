package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidMaxSpace;

/**
 * Created by Filipe on 29/03/2015.
 */
public class ConfigQuotasActivity extends Activity {

    private LocalWorkspace activeWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_quotas);

        GlobalContext globalContext = GlobalContext.getGC();

        User user = globalContext.loggedInUser;
        activeWorkspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());

        // Setup the Current_size and Max_size text
        int currentSize = activeWorkspace.getUsedSpace();
        TextView currentSizeTextView = (TextView) findViewById(R.id.totalFileSizeTextView);
        currentSizeTextView.setText(Integer.toString(currentSize));

        int maxSize = activeWorkspace.getMaxSpace();
        EditText maxSizeEditText = (EditText) findViewById(R.id.maxQuotaEditText);
        maxSizeEditText.setText(Integer.toString(maxSize));
    }

    public void changeMaxQuota(View view) {

        EditText maxSizeEditText = (EditText) findViewById(R.id.maxQuotaEditText);
        try {
            int maxSize = Integer.parseInt(maxSizeEditText.getText().toString());
            activeWorkspace.setMaxSpace(maxSize);
            Toast.makeText(getApplicationContext(),
                    "Maximum quota changed.",
                    Toast.LENGTH_SHORT).show();

        } catch (InvalidMaxSpace e) {
            Toast.makeText(getApplicationContext(),
                    "The maximum size must be higher than the currently occupied.",
                    Toast.LENGTH_SHORT).show();

        } catch(NumberFormatException e){
            Toast.makeText(this, "Please choose a valid quota.", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
