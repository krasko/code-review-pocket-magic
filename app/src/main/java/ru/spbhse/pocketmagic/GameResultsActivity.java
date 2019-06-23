package ru.spbhse.pocketmagic;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ru.spbhse.pocketmagic.R;

public class GameResultsActivity extends AppCompatActivity {

    private TextView textResult;
    private ImageView pictureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game_results);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        textResult = findViewById(R.id.textResult);
        pictureResult = findViewById(R.id.pictureResult);

        int result = getIntent().getExtras().getInt("RESULT");
        switch (result) {
            case 1:
                textResult.setText("VICTORY");
                pictureResult.setImageResource(R.drawable.victory_image);
                break;
            case 0:
                textResult.setText("DRAW");
                pictureResult.setImageResource(R.drawable.draw_image);
                break;
            case -1:
                textResult.setText("DEFEAT");
                pictureResult.setImageResource(R.drawable.defeat_image);
                break;
        }

        Button goToMenuButton = findViewById(R.id.goToMenuButton);
        goToMenuButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameResultsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() { /* Do nothing. */}
}
