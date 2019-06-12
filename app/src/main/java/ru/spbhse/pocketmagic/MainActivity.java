package ru.spbhse.pocketmagic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import ru.spbhse.pocketmagic.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        final Animation animSize = AnimationUtils.loadAnimation(this, R.anim.size);

        Button flameButton = findViewById(R.id.flameButton);
        flameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animAlpha);
                Intent intent = new Intent(MainActivity.this, OpponentSearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button botButton = findViewById(R.id.botButton);
        botButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animSize);
                Intent intent = new Intent(MainActivity.this, LoadActivity.class);
                intent.putExtra("GameType", GameType.BOT);
                startActivity(intent);
                finish();
            }
        });

        Button spellsButton = findViewById(R.id.spellsButton);
        spellsButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fiflyc/Pocket-Magic/wiki/%D0%97%D0%B0%D0%BA%D0%BB%D0%B8%D0%BD%D0%B0%D0%BD%D0%B8%D1%8F")));
            }
        });

        try {
            if (getIntent().getExtras().getInt("Error") == 1) {
                new AlertDialog.Builder(this).setMessage("Connection error")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* Do nothing */
                            }
                        }).show();
            }
        } catch (NullPointerException e) {
            /* It's okay, do nothing. */
        }
    }
}
