package ru.spbhse.pocketmagic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.nio.ByteBuffer;

public class NetworkController {

    private static AppCompatActivity currentActivity;
    private static Controller currentController;
    private static Network network;

    public static Network createNetwork(GoogleSignInAccount account) {
        network = new Network(account);
        return network;
    }

    public static void startGame() {
        Intent intent = new Intent(currentActivity, LoadActivity.class);
        intent.putExtra("GameType", GameType.MULTIPLAYER);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }

    public static void finishGame() {
        if (currentActivity.getClass() == GameActivity.class) {
            ((GameActivity) currentActivity).finishGame(GameResult.ERROR);
        }
    }

    synchronized public static void setUI(AppCompatActivity activity) {
        currentActivity = activity;
        if (currentActivity != null && currentActivity.getClass() == GameActivity.class) {
            currentController = ((GameActivity) currentActivity).getController();
        } else {
            currentController = null;
        }
    }

    synchronized public static Context getContext() {
        return currentActivity.getApplicationContext();
    }

    public static void receiveSpell(int spellID) {
        currentController.opponentSpell(spellID);
    }

    public static String getOpponentName() {
        return network.getOpponentName();
    }

    public static void sendSpell(int spellID) {
        network.sendMessage(ByteBuffer.allocate(4).putInt(spellID).array());
    }

    public static void showAlert(String message) {
        new AlertDialog.Builder(currentActivity)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(currentActivity, MainActivity.class);
                        currentActivity.startActivity(intent);
                        currentActivity.finish();
                    }
                }).show();
    }

    public static void showMessage(String message) {
        if (currentActivity.getClass() == OpponentSearchActivity.class) {
            ((OpponentSearchActivity) currentActivity).showMessage(message);
        }
    }
}
