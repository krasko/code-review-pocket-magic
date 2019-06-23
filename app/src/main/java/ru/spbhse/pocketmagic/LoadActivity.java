package ru.spbhse.pocketmagic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import ru.spbhse.pocketmagic.R;

public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_load);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Wait 2 seconds before starting other activity to avoid freezes. */
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                LoadActivity.this.moveToOtherActivity();
            }
        }, 2000);

    }

    private void moveToOtherActivity() {
        try {
            GameType type = (GameType) getIntent().getSerializableExtra("GameType");
            Intent intent = new Intent(LoadActivity.this, GameActivity.class);
            intent.putExtra("GameType", type);
            startActivity(intent);
            finish();
        } catch (NullPointerException e) {
            Log.wtf("Pocket Magic", "Stopped on LoadActivity");
            e.printStackTrace();
        }
    }
}
