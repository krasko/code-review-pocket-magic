package ru.spbhse.pocketmagic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class OpponentSearchActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;

    private GoogleSignInOptions signInOptions;
    private GoogleSignInAccount googleAccount;
    private GoogleSignInClient googleClient;
    private RealTimeMultiplayerClient multiplayerClient;

    private String playerId;
    private TextView textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_opponent_search);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpponentSearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        textMessage = findViewById(R.id.textMessage);

        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestScopes(Games.SCOPE_GAMES_LITE)
                .requestScopes(Games.SCOPE_GAMES)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, signInOptions);
    }

    @Override
    public void onStart() {
        super.onStart();

        googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount == null) {
            Log.d("Pocket Magic", "Need sign in");
            startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_IN);
        } else {
            onConnected();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                googleAccount = task.getResult(ApiException.class);
                onConnected();
            } catch (ApiException e) {
                String message = e.getMessage();
                if (message == null) {
                    showError("Error while sign in Google Play");
                } else {
                    showError("Google API error " + CommonStatusCodes.getStatusCodeString(e.getStatusCode()));
                }
            }
        }
    }

    private void onConnected() {
        NetworkController.setUI(this);
        Network network = NetworkController.createNetwork(googleAccount);
        network.findAndStartGame();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(OpponentSearchActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).show();
    }

    public void showMessage(String message) {
        textMessage.setText(message);
    }
}
