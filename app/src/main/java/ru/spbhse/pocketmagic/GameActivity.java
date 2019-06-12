package ru.spbhse.pocketmagic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import ru.spbhse.pocketmagic.R;

public class GameActivity extends AppCompatActivity {

    private Controller controller;

    private int maxHP;
    private int maxMP;

    private ProgressBar playerHP;
    private TextView valueHP;
    private ProgressBar playerMP;
    private TextView valueMP;

    private TextView opponentName;
    private ProgressBar opponentHP;
    private TextView valueOHP;
    private ImageView opponentAvatar;
    private ImageView opponentSpell;

    private ImageView playerBuffA;
    private ImageView playerBuffB;
    private ImageView opponentBuffA;
    private ImageView opponentBuffB;

    private ImageView fog;
    private GifImageView breeze;
    private GifImageView ices;
    private ImageView playerEffect;

    private GifImageView playerCast;
    private GifImageView playerSun;
    private GifImageView opponentCast;
    private GifImageView opponentSun;

    private GestureOverlayView gestureOverlayView;

    private TextView timer;

    public interface Caster {

        void cast(String spell);
    }

    private class Painter implements ru.spbhse.pocketmagic.Painter, Caster {

        private GifDrawable breezeBackAnim;
        private GifDrawable breezeFrontAnim;
        private GifDrawable icesAnim;
        private GifDrawable fireBallBackAnim;
        private GifDrawable fireBallFrontAnim;
        private GifDrawable lightningBackAnim;
        private GifDrawable lightningFrontAnim;
        private GifDrawable exhaustingSunBackAnim;
        private GifDrawable exhaustingSunFrontAnim;

        public void setOpponentName(@NonNull String name) {
            opponentName.setText(name);
        }

        public void setMaxHP(int value) {
            maxHP = value;
        }

        public void setMaxMP(int value) {
            maxMP = value;
        }

        public void setPlayerHP(int value) {
            playerHP.setProgress(value);
            String text = value + "/" + maxHP;
            valueHP.setText(text);
        }

        public void setPlayerMP(int value) {
            playerMP.setProgress(value);
            String text = value + "/" + maxMP;
            valueMP.setText(text);
        }

        public void setOpponentHP(int value) {
            opponentHP.setProgress(value);
            String text = value + "/" + maxHP;
            valueOHP.setText(text);
        }

        public void endGame(GameResult result) {
            finishGame(result);
        }

        public void showOpponentSpell(String spell) {
            if (spell.equals("Heal")) {
                opponentSpell.setImageResource(R.drawable.heal);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("SunShield")) {
                opponentSpell.setImageResource(R.drawable.sunshield);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("Freeze")) {
                opponentSpell.setImageResource(R.drawable.freeze);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("Fog")) {
                opponentSpell.setImageResource(R.drawable.fog);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("Breeze")) {
                opponentSpell.setImageResource(R.drawable.breeze);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("FireBall")) {
                opponentSpell.setImageResource(R.drawable.fireball);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("Lightning")) {
                opponentSpell.setImageResource(R.drawable.lightning);
                opponentSpell.setVisibility(View.VISIBLE);
            } else if (spell.equals("ExhaustingSun")) {
                opponentSpell.setImageResource(R.drawable.exhausting_sun);
                opponentSpell.setVisibility(View.VISIBLE);
            }
        }

        public void hideOpponentSpell() {
            opponentSpell.setVisibility(View.INVISIBLE);
        }

        @Override
        public void showOpponentBuff(String buff) {
            if (buff.equals("Heal")) {
                opponentBuffB.setImageResource(R.drawable.buff_heal);
                opponentBuffB.setVisibility(View.VISIBLE);
            } else if (buff.equals("SunShield")) {
                opponentBuffA.setImageResource(R.drawable.buff_sunshield_a);
                opponentBuffA.setVisibility(View.VISIBLE);
            } else if (buff.equals("SunShieldB")) {
                opponentBuffA.setImageResource(R.drawable.buff_sunshield_b);
                opponentBuffA.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void hideOpponentBuff(String buff) {
            if (buff.equals("Heal")) {
                opponentBuffB.setVisibility(View.INVISIBLE);
            } else if (buff.equals("SunShield")) {
                opponentBuffA.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void setPlayerBuff(String buff) {
            if (buff.equals("Heal")) {
                playerBuffB.setImageResource(R.drawable.buff_heal);
                playerBuffB.setVisibility(View.VISIBLE);
            } else if (buff.equals("SunShield")) {
                playerBuffA.setImageResource(R.drawable.buff_sunshield_a);
                playerBuffA.setVisibility(View.VISIBLE);
            } else if (buff.equals("SunShieldB")) {
                playerBuffA.setImageResource(R.drawable.buff_sunshield_b);
                playerBuffA.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void hidePlayerBuff(String buff) {
            if (buff.equals("Heal")) {
                playerBuffB.setVisibility(View.INVISIBLE);
            } else if (buff.equals("SunShield")) {
                playerBuffA.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void setPlayerState(PlayerState state) {
            if (state == PlayerState.FROZEN || state == PlayerState.FREEZING) {
                playerEffect.setImageResource(R.drawable.effect_frozen_player);
                playerEffect.setVisibility(View.VISIBLE);
            } else if (state == PlayerState.WET) {
                playerEffect.setImageResource(R.drawable.effect_wet_player);
                playerEffect.setVisibility(View.VISIBLE);
            } else if (state == PlayerState.NORMAL) {
                playerEffect.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void hidePlayerState() {
            playerEffect.setVisibility(View.INVISIBLE);
        }

        @Override
        public void setOpponentState(PlayerState state) {
            switch (state) {
                case NORMAL:
                    opponentAvatar.setImageResource(R.drawable.opponent_normal);
                    break;
                case WET:
                    opponentAvatar.setImageResource(R.drawable.opponent_wet);
                    break;
                case FROZEN:
                    opponentAvatar.setImageResource(R.drawable.opponent_cold);
                    break;
                case FREEZING:
                    opponentAvatar.setImageResource(R.drawable.opponent_cold);
                    break;
            }
        }

        @Override
        public void showPlayerCast(String spell) {
            try {
                switch (spell) {
                    case "FireBall":
                        fireBallBackAnim = new GifDrawable(getContext().getResources(), R.drawable.fireball_back);
                        playerCast.setImageDrawable(fireBallBackAnim);

                        fireBallBackAnim.seekTo(0);
                        fireBallBackAnim.start();
                        playerCast.setVisibility(View.VISIBLE);
                        break;
                    case "Lightning":
                        lightningBackAnim = new GifDrawable(getContext().getResources(), R.drawable.lightning_back);
                        playerCast.setImageDrawable(lightningBackAnim);

                        lightningBackAnim.seekTo(0);
                        lightningBackAnim.start();
                        playerCast.setVisibility(View.VISIBLE);
                        break;
                    case "Fog":
                        fog.setVisibility(View.VISIBLE);
                        break;
                    case "Breeze":
                        breezeBackAnim = new GifDrawable(getContext().getResources(), R.drawable.breeze_back);
                        breeze.setImageDrawable(breezeBackAnim);

                        breezeBackAnim.seekTo(0);
                        breezeBackAnim.setSpeed(2.0f);
                        breezeBackAnim.start();
                        breeze.setVisibility(View.VISIBLE);
                        break;
                    case "Ices":
                        icesAnim = new GifDrawable(getContext().getResources(), R.drawable.ices);
                        ices.setImageDrawable(icesAnim);

                        icesAnim.seekTo(0);
                        icesAnim.stop();
                        ices.setVisibility(View.VISIBLE);
                        break;
                    case "ExhaustingSun":
                        exhaustingSunBackAnim = new GifDrawable(getContext().getResources(), R.drawable.exhausting_sun_back);
                        playerSun.setImageDrawable(exhaustingSunBackAnim);

                        exhaustingSunBackAnim.seekTo(0);
                        exhaustingSunBackAnim.setSpeed(2.0f);
                        exhaustingSunBackAnim.start();
                        playerSun.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (IOException e) {
                Log.wtf("Pocket Magic", "Gif exception");
                e.printStackTrace();
            }
        }

        @Override
        public void hidePlayerCast(String spell) {
            switch (spell) {
                case "FireBall":
                    playerCast.setVisibility(View.INVISIBLE);
                    fireBallBackAnim.stop();
                    fireBallBackAnim.recycle();
                    fireBallBackAnim = null;
                    break;
                case "Lightning":
                    playerCast.setVisibility(View.INVISIBLE);
                    lightningBackAnim.stop();
                    lightningBackAnim.recycle();
                    lightningBackAnim = null;
                case "Fog":
                    fog.setVisibility(View.INVISIBLE);
                    break;
                case "Breeze":
                    breeze.setVisibility(View.INVISIBLE);
                    breezeBackAnim.stop();
                    breezeBackAnim.recycle();
                    breezeBackAnim = null;
                    break;
                case "Ices":
                    icesAnim.seekTo(0);
                    icesAnim.start();
                    break;
                case "ExhaustingSun":
                    playerSun.setVisibility(View.INVISIBLE);
                    exhaustingSunBackAnim.stop();
                    exhaustingSunBackAnim.recycle();
                    exhaustingSunFrontAnim = null;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void showOpponentCast(String spell) {
            try {
                switch (spell) {
                    case "FireBall":
                        fireBallFrontAnim = new GifDrawable(getContext().getResources(), R.drawable.fireball_front);
                        opponentCast.setImageDrawable(fireBallFrontAnim);

                        fireBallFrontAnim.seekTo(0);
                        fireBallFrontAnim.start();
                        opponentCast.setVisibility(View.VISIBLE);
                        break;
                    case "Lightning":
                        lightningFrontAnim = new GifDrawable(getContext().getResources(), R.drawable.lightning_front);
                        opponentCast.setImageDrawable(lightningFrontAnim);

                        lightningFrontAnim.seekTo(0);
                        lightningFrontAnim.start();
                        opponentCast.setVisibility(View.VISIBLE);
                        break;
                    case "Fog":
                        fog.setVisibility(View.VISIBLE);
                        break;
                    case "Breeze":
                        breezeFrontAnim = new GifDrawable(getContext().getResources(), R.drawable.breeze_front);
                        breeze.setImageDrawable(breezeFrontAnim);

                        breezeFrontAnim.seekTo(0);
                        breezeFrontAnim.setSpeed(2.0f);
                        breezeFrontAnim.start();
                        breeze.setVisibility(View.VISIBLE);
                        break;
                    case "Ices":
                        icesAnim = new GifDrawable(getContext().getResources(), R.drawable.ices);
                        ices.setImageDrawable(icesAnim);

                        icesAnim.seekTo(0);
                        icesAnim.stop();
                        ices.setVisibility(View.VISIBLE);
                        break;
                    case "ExhaustingSun":
                        exhaustingSunFrontAnim = new GifDrawable(getContext().getResources(), R.drawable.exhausting_sun_front);
                        opponentSun.setImageDrawable(exhaustingSunFrontAnim);

                        exhaustingSunFrontAnim.seekTo(0);
                        exhaustingSunFrontAnim.setSpeed(2.0f);
                        exhaustingSunFrontAnim.start();
                        opponentSun.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (IOException e) {
                Log.wtf("Pocket Magic", "Gif exception");
                e.printStackTrace();
            }
        }

        @Override
        public void hideOpponentCast(String spell) {
            switch (spell) {
                case "FireBall":
                    opponentCast.setVisibility(View.INVISIBLE);
                    fireBallFrontAnim.stop();
                    fireBallFrontAnim.recycle();
                    fireBallFrontAnim = null;
                    break;
                case "Lightning":
                    opponentCast.setVisibility(View.INVISIBLE);
                    lightningFrontAnim.stop();
                    lightningFrontAnim.recycle();
                    lightningFrontAnim = null;
                    break;
                case "Fog":
                    fog.setVisibility(View.INVISIBLE);
                    break;
                case "Breeze":
                    breeze.setVisibility(View.INVISIBLE);
                    breezeFrontAnim.stop();
                    breezeFrontAnim.recycle();
                    breezeFrontAnim = null;
                    break;
                case "Ices":
                    icesAnim.seekTo(0);
                    icesAnim.start();
                    break;
                case "ExhaustingSun":
                    opponentSun.setVisibility(View.INVISIBLE);
                    exhaustingSunFrontAnim.stop();
                    exhaustingSunFrontAnim.recycle();
                    exhaustingSunFrontAnim = null;
                    break;
                default:
                    break;
            }
        }

        synchronized public void sendNotification(String notification) {
            Toast.makeText(getApplicationContext(), notification, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void lockInput() {
            gestureOverlayView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void unlockInput() {
            gestureOverlayView.setVisibility(View.VISIBLE);
        }

        @Override
        public void timerCast(double time) {
            new CountDownTimer((long) (time * 1000), 100) {
                public void onTick(long millisUntilFinished) {
                    timer.setText(millisUntilFinished / 1000 + "." + millisUntilFinished / 100 + "s");
                }
                public void onFinish() {
                    timer.setText("");
                    unlockInput();
                }
            }.start();
        }

        public Context getContext() {
            return GameActivity.this;
        }

        @Override
        public void cast(String spell) {
            controller.playerSpell(spell);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(getApplicationContext(), R.raw.gestures);
        gestureOverlayView = findViewById(R.id.gestureListener);
        if (!gestureLibrary.load()) {
            finish();
        }

        playerHP = findViewById(R.id.playerHP);
        valueHP = findViewById(R.id.valueHP);
        playerMP = findViewById(R.id.playerMP);
        valueMP = findViewById(R.id.valueMP);
        opponentHP = findViewById(R.id.opponentHP);
        valueOHP = findViewById(R.id.valueOHP);

        opponentName = findViewById(R.id.opponentName);
        opponentSpell = findViewById(R.id.opponentSpell);
        opponentAvatar = findViewById(R.id.opponentAvatar);

        timer = findViewById(R.id.timer);

        playerBuffA = findViewById(R.id.playerBuffA);
        playerBuffB = findViewById(R.id.playerBuffB);
        opponentBuffA = findViewById(R.id.opponentBuffA);
        opponentBuffB = findViewById(R.id.opponentBuffB);

        breeze = findViewById(R.id.breeze);
        fog = findViewById(R.id.fog);
        ices = findViewById(R.id.ices);
        playerEffect = findViewById(R.id.playerEffect);

        playerCast = findViewById(R.id.playerCast);
        opponentCast = findViewById(R.id.opponentCast);
        playerSun = findViewById(R.id.playerSun);
        opponentSun = findViewById(R.id.opponentSun);

        Painter painter = this.new Painter();
        if (getIntent().getSerializableExtra("GameType") == GameType.MULTIPLAYER) {
            NetworkController.setUI(this);
            controller = new Controller(painter, GameType.MULTIPLAYER);
            NetworkController.setUI(this);
        } else {
            controller = new Controller(painter, GameType.BOT);
        }

        opponentAvatar.setEnabled(false);
        gestureOverlayView.addOnGesturePerformedListener(new GestureListener(gestureLibrary, painter));
    }

    @Override
    public void onBackPressed() { /* Do nothing. */}

    public void finishGame(GameResult result) {
        if (result == GameResult.ERROR) {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.putExtra("ERROR", 1);
            startActivity(intent);
            finish();
        }

        Intent intent = new Intent(GameActivity.this, GameResultsActivity.class);;
        switch (result) {
            case WIN:
                intent.putExtra("RESULT", 1);
                break;
            case LOSE:
                intent.putExtra("RESULT", -1);
                break;
            case DRAW:
                intent.putExtra("RESULT", 0);
                break;
        }

        NetworkController.setUI(null);

        startActivity(intent);
        finish();
    }

    public Controller getController() {
        return controller;
    }
}