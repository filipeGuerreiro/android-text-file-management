package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;

/**
 * Created by Filipe on 25/03/2015.
 */
public class VisibilityActivity extends Activity {

    GlobalContext globalContext;

    private ListView tagsListView;
    private ArrayAdapter<String> tagsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visibility);
        globalContext = GlobalContext.getGC();

        final User user = globalContext.loggedInUser;
        final LocalWorkspace workspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());
        List<String> tags = workspace.getTags();
        tagsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                tags);

        tagsListView = (ListView) findViewById(R.id.tagsListView);
        tagsListView.setAdapter(tagsAdapter);
        tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                workspace.removeTag(position);
                tagsAdapter.notifyDataSetChanged();
            }
        });

        // Set initial visibility_button text
        Button visibilityButton = (Button) findViewById(R.id.setVisibilityButton);
        if(workspace.isPublic()) {
            visibilityButton.setText("Make Private");
        }
        else {
            visibilityButton.setText("Make Public");
        }
    }

    public void addTag(View view) {
        final User user = globalContext.loggedInUser;
        final LocalWorkspace workspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());

        EditText tagEditText = (EditText) findViewById(R.id.tagEditText);
        String newTag = tagEditText.getText().toString();

        if(newTag.length() < 1){
            Toast.makeText(this, "Please choose a tag first.", Toast.LENGTH_SHORT).show();
            return;

        }
        if(workspace.getTags().contains(newTag)){
            Toast.makeText(this, "Tag already in list.", Toast.LENGTH_SHORT).show();
            return;
        }



        workspace.addTag(newTag);
        tagsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tagsAdapter.notifyDataSetChanged();
    }

    public void changeVisibilityOnClick(View view) {
        final User user = globalContext.loggedInUser;
        final LocalWorkspace workspace = user.getOwnedWorkspace(globalContext.getWorkspaceIndex());

        Button visibilityButton = (Button) findViewById(R.id.setVisibilityButton);
        if(workspace.isPublic()) {
            visibilityButton.setText("Make Public");
            Toast.makeText(globalContext, "Workspace is now private.", Toast.LENGTH_SHORT).show();
        }
        else {
            visibilityButton.setText("Make Private");
            Toast.makeText(globalContext, "Workspace is now public.", Toast.LENGTH_SHORT).show();
        }
        workspace.setVisibility(!workspace.isPublic());
    }
}
