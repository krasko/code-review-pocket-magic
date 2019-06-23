package ru.spbhse.pocketmagic;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static java.lang.Math.min;

/** Controller -- carries out communication between UI, Network and Game Logic */
public class Controller {
    /* inner logic of the game */
    private Logic logic;

    /* Parent painter */
    private Painter painter;
    /* Special AsyncTask for increasing playerMP every x seconds */
    private ManaGenerator generation;
    /* there are 3 cases when the game should be stopped
    isStopped == true iff one of them happened */
    private boolean isStopped = false;

    /* Bot ot Multiplayer */
    private GameType type;
    private Bot bot;

    /** Creates Controller and starts the game with given type of game and Painter -- UI entity */
    public Controller(Painter painter, GameType type) {
        this.painter = painter;
        this.type = type;
        logic = new Logic();
        startGame();
    }

    private void startGame() {
        painter.setMaxHP(logic.getMaxHp());
        painter.setMaxMP(logic.getMaxMp());
        painter.setPlayerHP(logic.getPlayerHP());
        painter.setPlayerMP(logic.getPlayerMP());
        painter.setOpponentHP(logic.getOpponentHP());
        if (type == GameType.BOT) {
            painter.setOpponentName("Bot");
            bot = new Bot();
            bot.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            painter.setOpponentName(NetworkController.getOpponentName());
        }
        generation = new ManaGenerator();
        generation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void endGame() {
        isStopped = true;
        if (bot != null) {
            bot.stop();
        }
        generation.stop();
    }

    /** Method activating spell from the player. */
    public void playerSpell(String spell) {
        String ability = logic.ableToThrowTheSpell(spell);
        if (ability != "ok") {
            painter.sendNotification(ability);
            return;
        } else {
            logic.initializeCast(spell);
        }
        painter.lockInput();
        painter.timerCast(logic.getCastByName(spell));
        if (type == GameType.MULTIPLAYER) {
            NetworkController.sendSpell(logic.getIDByName(spell));
        }
        ThrowPlayerSpell throwPlayerSpell = new ThrowPlayerSpell();
        throwPlayerSpell.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, spell);
    }

    /** Method activating spell from the opponent. */
    public void opponentSpell(int spellID) {
        if (isStopped) {
            return;
        }
        painter.showOpponentSpell(logic.getNameById(spellID));
        ThrowOpponentSpell throwOpponentSpell = new ThrowOpponentSpell();
        throwOpponentSpell.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, logic.getNameById(spellID));
    }

    private void generateMana(int mana) {
        logic.generateMana(mana);
        painter.setPlayerMP(logic.getPlayerMP());
    }

    /** Part of the opponent spell after cast. */
    private void throwOpponentSpell(String spell) {
        if (isStopped) {
            return;
        }
        painter.hideOpponentSpell();
        showOpponentCast(spell);
        logic.opponentSpell(spell);
        painter.setPlayerHP(logic.getPlayerHP());
        if (logic.getPlayerHP() == 0) {
            endGame();
            painter.endGame(GameResult.LOSE);
        }
    }
    /** Part of the player spell after cast. */
    private void throwPlayerSpell(String spell) {
        painter.unlockInput();
        if (isStopped) {
            return;
        }
        showPlayerCast(spell);
        logic.playerSpell(spell);
        painter.setPlayerMP(logic.getPlayerMP());
        painter.setPlayerHP(logic.playerHP);
        painter.setOpponentHP(logic.getOpponentHP());
        if (logic.getOpponentHP() == 0) {
            endGame();
            painter.endGame(GameResult.WIN);
        }
    }

    /** Part of the player spell after cast and throwing. */
    private  void stopPlayerSpell(String spell) {
        if (logic.getTypeByName(spell).equals("buff")) {
            painter.hidePlayerBuff(spell);
            logic.updatePlayerState("hide");
        } else  {
            logic.updateOpponentState("after spell");
            painter.hidePlayerCast(spell);
        }
        painter.setPlayerState(logic.playerState);
        painter.setOpponentState(logic.opponentState);
    }

    /** Makes a decision how to show player spell depending on type of the spell. */
    private void showPlayerCast(String spell) {
        painter.setOpponentState(logic.opponentState);
        String spellType = logic.getTypeByName(spell);
        if (spellType.equals("spell")) {
            painter.showPlayerCast(spell);
        }
        if (spellType.equals("buff")) {
            painter.showPlayerCast(spell);
            painter.setPlayerBuff(spell);
        }
        if (spellType.equals("effect")) {
            painter.showPlayerCast(spell);
        }
    }

    /** Makes a decision how to show opponent spell depending on type of the spell. */
    private void showOpponentCast(String spell) {
        if (logic.getTypeByName(spell).equals("buff")) {
            painter.showOpponentBuff(spell);
        }
        painter.showOpponentCast(spell);
    }

    /** Part of the opponent spell after cast and throwing. */
    private  void stopOpponentSpell(String spell) {
        if (logic.getTypeByName(spell).equals("buff")) {
            painter.hideOpponentBuff(spell);
            logic.updateOpponentState("hide");
        } else {
            painter.hideOpponentCast(spell);
            logic.updatePlayerState("after spell");
        }
        painter.hidePlayerState();
        painter.setPlayerState(logic.playerState);
        painter.setOpponentState(logic.opponentState);
    }

    /** Class extending AsyncTask for Throwing opponent spell in other thread. */
    private class ThrowOpponentSpell extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... spells) {
            try {
                TimeUnit.MILLISECONDS.sleep((long) (logic.getCastByName(spells[0]) * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < logic.getDurationByName(spells[0]); i++) {
                publishProgress(spells);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return spells[0];
        }

        @Override
        protected void onProgressUpdate(String... spells) {
            throwOpponentSpell(spells[0]);
        }

        @Override
        protected void onPostExecute(String spell) {
            stopOpponentSpell(spell);
        }
    }

    /** Class extending AsyncTask for Throwing player spell in other thread. */
    private class ThrowPlayerSpell extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... spells) {
            try {
                TimeUnit.MILLISECONDS.sleep((long) (logic.getCastByName(spells[0]) * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < logic.getDurationByName(spells[0]); i++) {
                publishProgress(spells);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return spells[0];
        }

        @Override
        protected void onProgressUpdate(String... spells) {
            throwPlayerSpell(spells[0]);
        }

        @Override
        protected void onPostExecute(String spell) {
            stopPlayerSpell(spell);
        }
    }

    /** Bot -- opponent imitation in other thread. */
    private class Bot extends AsyncTask<Void, Void, Void> {
        private boolean isAlive = true;
        private Random random = new Random();

        public void stop(){
            isAlive = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (isAlive) {
                try {
                    TimeUnit.SECONDS.sleep(random.nextInt(5) + 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            opponentSpell(random.nextInt(7) + 1);
        }
    }

    /** Class extending AsyncTask for generating mana in other thread. */
    private class ManaGenerator extends AsyncTask<Void, Void, Void> {
        private boolean isAlive = true;

        public void stop() {
            isAlive = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (isAlive) {
                publishProgress();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            generateMana(1);
        }
    }

    /** Class with logic of the game. */
    private class Logic {
        private static final int MAX_HP = 20;
        private static final int MAX_MP = 20;
        private static final int SPELL_LENS = 3;

        volatile private int playerHP = MAX_HP ;
        volatile private int opponentHP = MAX_HP;
        volatile private int playerMP = MAX_MP;

        private DatabaseHelper mDBHelper;
        private SQLiteDatabase mDb;

        private PlayerState playerState = PlayerState.NORMAL;
        private PlayerState opponentState = PlayerState.NORMAL;

        private Logic() {
            mDBHelper = new DatabaseHelper(painter.getContext());
            try {
                mDBHelper.updateDataBase();
            } catch (Error e) {
                throw new Error("UnableToUpdateDatabase");
            }
            try {
                mDb = mDBHelper.getWritableDatabase();
            } catch (SQLException mSQLException) {
                throw mSQLException;
            }
        }

        private int getPlayerHP() {
            return playerHP;
        }

        private int getOpponentHP() {
            return opponentHP;
        }

        private int getPlayerMP() {
            return playerMP;
        }

        private int getMaxHp() {
            return MAX_HP;
        }

        private int getMaxMp() {
            return MAX_MP;
        }

        synchronized private void initializeCast(String spell) {
            playerMP -= getCostByName(spell);
            playerMP = max(0, playerMP);
        }

        synchronized private void playerSpell(String spell) {
            playerHP += getHealingByName(spell);
            playerHP = min(playerHP, MAX_HP);
            opponentHP -= calcOpponentDamage(spell);
            opponentHP = max(opponentHP, 0);
            updateOpponentState(spell);
        }

        private int calcPlayerDamage(String spell) {
            int result = getDamageByName(spell);
            if (playerState == PlayerState.WET && spell.equals("Lightning")) {
                result = result * SPELL_LENS;
            }
            if ((playerState == PlayerState.FREEZING || playerState == PlayerState.FROZEN) && (spell.equals("Breeze"))) {
                result = SPELL_LENS;
            }
            return result;
        }

        private int calcOpponentDamage(String spell) {
            int result = getDamageByName(spell);
            if (opponentState == PlayerState.WET && spell.equals("Lightning")) {
                result = result * SPELL_LENS;
            }
            if ((opponentState == PlayerState.FREEZING || opponentState == PlayerState.FROZEN) && (spell.equals("Breeze"))) {
                result = SPELL_LENS;
            }
            return result;
        }

        synchronized private void opponentSpell(String spell) {
            playerHP -= calcPlayerDamage(spell);
            playerHP = max(playerHP, 0);
            opponentHP += getHealingByName(spell);
            opponentHP = min(opponentHP, MAX_HP);
            updatePlayerState(spell);
        }

        synchronized private void updatePlayerState(String spell) {
            if (spell.equals("hide") || spell.equals("after spell")) {
                playerState = PlayerState.NORMAL;
            }
            if (playerState == PlayerState.SUNNY) {
                return;
            }
            if (spell.equals("SunShield")) {
                playerState = PlayerState.SUNNY;
            }
            if (spell.equals("Heal")) {
                playerState = PlayerState.NORMAL;
            }
            if (playerState == PlayerState.NORMAL && spell.equals("Fog")) {
                playerState = PlayerState.FOG;
            }
            if (playerState == PlayerState.FOG && spell.equals("Breeze")) {
                playerState = PlayerState.WET;
            }
            if (playerState == PlayerState.FROZEN && spell.equals("FireBall")) {
                playerState = PlayerState.FOG;
            }
            if (playerState == PlayerState.WET && spell.equals("Freeze")) {
                playerState = PlayerState.FROZEN;
            }
            if (spell.equals("Freeze")) {
                playerState = PlayerState.FREEZING;
            }
        }

        synchronized private void updateOpponentState(String spell) {
            if (spell.equals("hide") || spell.equals("after spell")) {
                opponentState = PlayerState.NORMAL;
            }
            if (opponentState == PlayerState.SUNNY) {
                return;
            }
            if (spell.equals("SunShield")) {
                opponentState = PlayerState.SUNNY;
            }
            if (spell.equals("Heal")) {
                opponentState = PlayerState.NORMAL;
            }
            if (opponentState == PlayerState.NORMAL && spell.equals("Fog")) {
                opponentState = PlayerState.FOG;
            }
            if (opponentState == PlayerState.FOG && spell.equals("Breeze")) {
                opponentState = PlayerState.WET;
            }
            if (opponentState == PlayerState.FROZEN && spell.equals("FireBall")) {
                opponentState = PlayerState.FOG;
            }
            if (opponentState == PlayerState.WET && spell.equals("Freeze")) {
                opponentState = PlayerState.FROZEN;
            }
            if (spell.equals("Freeze")) {
                opponentState = PlayerState.FREEZING;
            }
        }

        private String ableToThrowTheSpell(String spell) {
            if (spell.equals("ExhaustingSun") && playerState.equals(PlayerState.FOG)) {
                return "You can't cast ExhaustingSun through the fog";
            }
            if (playerState == PlayerState.FROZEN || playerState == PlayerState.FREEZING) {
                return "You are frozen and not able to cast spells";
            }
            if (playerMP < getCostByName(spell) ) {
                return ("Not enough mana for the spell " + spell);
            }
            return "ok";
        }

        synchronized private void generateMana(int mana) {
            playerMP += mana;
            playerMP = min(playerMP, MAX_MP);
        }

        private String getNameById(int spellID) {
            Cursor cursor = mDb.rawQuery("SELECT name FROM spells WHERE _id=" + String.valueOf(spellID), null);
            cursor.moveToFirst();
            return cursor.getString(0);
        }

        private int getIDByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT _id FROM spells WHERE name='" + spell + "'", null);
            cursor.moveToFirst();
            return cursor.getInt(0);
        }

        private int getCostByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT cost FROM spells WHERE name='" + spell + "'", null);
            cursor.moveToFirst();
            return cursor.getInt(0);
        }

        private int getDamageByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT damage FROM spells WHERE name=?", new String[] {spell});
            cursor.moveToFirst();
            return cursor.getInt(0);
        }

        private double getCastByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT \"cast\" FROM spells WHERE name='" + spell + "'", null);
            cursor.moveToFirst();
            return cursor.getDouble(0);
        }

        private double getDurationByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT duration FROM spells WHERE name=?", new String[] {spell});
            cursor.moveToFirst();
            return cursor.getDouble(0);
        }

        private String getTypeByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT type FROM spells WHERE name='" + spell + "'", null);
            cursor.moveToFirst();
            return cursor.getString(0);
        }

        private int getHealingByName(String spell) {
            Cursor cursor = mDb.rawQuery("SELECT healing FROM spells WHERE name='" + spell + "'", null);
            cursor.moveToFirst();
            return cursor.getInt(0);
        }

        private List<Spell> getAllSpells() {
            List<Spell> result = new ArrayList<Spell>();
            Cursor cursor = mDb.rawQuery("SELECT * FROM spells", null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add( new Spell( cursor.getString(1), cursor.getInt(2),
                        cursor.getInt(3), cursor.getDouble(4), cursor.getDouble(5),
                        cursor.getString(6), cursor.getInt(7), cursor.getString(8)) );
                cursor.moveToNext();
            }
            return result;
        }

        private List<String> getAllSpellNames() {
            List<String> result = new ArrayList<String>();
            Cursor cursor = mDb.rawQuery("SELECT name FROM spells", null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add(cursor.getString(0));
                cursor.moveToNext();
            }
            return result;
        }
    }
}