package pt.ulisboa.tecnico.cmov.airdesk.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.business.GlobalContext;
import pt.ulisboa.tecnico.cmov.airdesk.business.User;
import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidUserCreation;

/**
 * Created by Filipe on 23/03/2015.
 */
public class LoginActivity extends Activity {

    GlobalContext globalContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        globalContext = (GlobalContext) getApplicationContext();
    }


    public void login(View view) {
        EditText nicknameEditText = (EditText) findViewById(R.id.nicknameEditText);
        EditText emailEditText = (EditText) findViewById(R.id.emailEditText);
        String nickname = nicknameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        doLogin(nickname, email);
    }

     private void doLogin(String nickname, String email){

        // Check if user exists
        User user = globalContext.getUser(email);
        if(user == null) {
            // if he doesn't exist, create a new user
            try {
                globalContext.addUser(nickname, email);
            } catch (InvalidUserCreation e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            // if he does exist, update his username
            user.setNickName(nickname);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void devLogin(View view) {

        doLogin("developer", "dev@ist.utl.pt");
    }
}
