package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;

/**
 * Created by Filipe on 23/03/2015.
 */
public class SearchActivity extends Activity {

    GlobalContext globalContext;

    private ListView tagsListView;
    private ArrayAdapter<String> tagsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        globalContext = GlobalContext.getGC();
        
        final User user = globalContext.loggedInUser;
        List<String> tags = user.getTags();
        tagsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                tags);

        tagsListView = (ListView) findViewById(R.id.tagsListView);
        tagsListView.setAdapter(tagsAdapter);
        tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                user.removeTag(position);
                tagsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void addTag(View view) {
        final User user = globalContext.loggedInUser;

        EditText tagEditText = (EditText) findViewById(R.id.tagEditText);
        String newTag = tagEditText.getText().toString();

        if(newTag.length() < 1) {
            return;
        }
        user.addTag(newTag);
        tagsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tagsAdapter.notifyDataSetChanged();
    }
}
