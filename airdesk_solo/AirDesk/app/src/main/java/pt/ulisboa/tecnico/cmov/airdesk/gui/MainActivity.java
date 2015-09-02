package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.business.FileUtil;
import pt.ulisboa.tecnico.cmov.airdesk.business.LocalWorkspace;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.business.Workspace;

public class MainActivity extends ActionBarActivity {

    private ListView ownedWorkspacesListView;
    private ListView foreignWorkspacesListView;

    static public ArrayAdapter<LocalWorkspace> ownedWorkspacesAdapter;
    static public ArrayAdapter<Workspace> foreignWorkspacesAdapter;

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }

    public void populate(View view) {
        if (Constants.CREATE_STUBS) {
            FileUtil.deleteChildreenRecursive(getFilesDir());

            Log.d(Constants.LOG_TAG, "MainActivity.onCreate()");

            User user = GlobalContext.getGC().loggedInUser;
            user.createWorkspace("Snow Trip", 50);
            user.createWorkspace("Metal Concert", 50);
        }

        ownedWorkspacesAdapter.notifyDataSetChanged();
        foreignWorkspacesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeListViews();
    }

    private void initializeListViews() {
        final GlobalContext globalContext = GlobalContext.getGC();

        User user = globalContext.loggedInUser;

        ownedWorkspacesListView = (ListView) findViewById(R.id.ownedWorkspacesListView);
        foreignWorkspacesListView = (ListView) findViewById(R.id.foreignWorkspacesListView);

        List<LocalWorkspace> ownedWorkspaces = user.getOwnedWorkspaces();
        ownedWorkspacesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ownedWorkspaces);

        ownedWorkspacesListView.setAdapter(ownedWorkspacesAdapter);

        ownedWorkspacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                globalContext.setWorkspaceIndex(position);
                startViewOwnedWorkspaceActivity(view);
            }
        });

        List<Workspace> foreignWorkspaces = user.getForeignWorkspaces();
        foreignWorkspacesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                foreignWorkspaces);

        foreignWorkspacesListView.setAdapter(foreignWorkspacesAdapter);

        foreignWorkspacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                globalContext.setWorkspaceIndex(position);
                startViewForeignWorkspaceActivity(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ownedWorkspacesAdapter.notifyDataSetChanged();
        foreignWorkspacesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalContext.getGC().savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startCreateWorkspaceActivity(View v) {
        Intent intent = new Intent(this, CreateWorkspaceActivity.class);
        startActivity(intent);
    }

    public void startDeleteWorkspaceActivity(View view) {
        Intent intent = new Intent(this, DeleteWorkspaceActivity.class);
        startActivity(intent);
    }

    public void startViewOwnedWorkspaceActivity(View view) {
        Intent intent = new Intent(this, ViewOwnedWorkspaceActivity.class);
        startActivity(intent);
    }

    public void startViewForeignWorkspaceActivity(View view) {
        Intent intent = new Intent(this, ViewForeignWorkspaceActivity.class);
        startActivity(intent);
    }

    public void startSearchActivity(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void logoutOnClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
