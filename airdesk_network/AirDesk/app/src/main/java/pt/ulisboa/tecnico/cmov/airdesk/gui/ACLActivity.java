package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.wifi.WifiAPI;

/**
 * Created by petrucci on 07/04/15.
 */
public class ACLActivity extends Activity {

    GlobalContext globalContext;

    private ListView listView;
    private ArrayAdapter<String> aclAdapter;

    private LocalWorkspace currentWorkspace;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_acl);
        globalContext = GlobalContext.getGC();

        final User user = globalContext.loggedInUser;
        currentWorkspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());
        final List<String> allowedUsers = currentWorkspace.getUserACL();
        aclAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                allowedUsers);

        listView = (ListView) findViewById(R.id.aclListView);
        listView.setAdapter(aclAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userToBeRemoved = allowedUsers.get(position);
                currentWorkspace.rmUserFromACL(userToBeRemoved);

                WifiAPI.uninviteUser(userToBeRemoved, currentWorkspace);

                aclAdapter.notifyDataSetChanged();
            }
        });

    }

    public void addUser(View view) {
        final User user = globalContext.loggedInUser;

        EditText editText = (EditText) findViewById(R.id.aclEditText);
        String email = editText.getText().toString();

        if(currentWorkspace.getUserACL().contains(email) ) {
            Toast.makeText(this, "Already in list.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(email.length() < 1 || ! GlobalContext.rfc2822.matcher(email).matches() ) {
            Toast.makeText(this, "Email must be valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentWorkspace.addUserToACL(email);

        WifiAPI.inviteUser(email, currentWorkspace);

        aclAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aclAdapter.notifyDataSetChanged();
    }

}
