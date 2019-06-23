package ru.spbhse.pocketmagic;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Class for database works. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "spells.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 15;

    private SQLiteDatabase database;
    private final Context context;
    private boolean needUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.context = context;
        copyDataBase();
        this.getReadableDatabase();
    }

    public void updateDataBase() {
        if (needUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists()) {
                dbFile.delete();
            }
            copyDataBase();
            needUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            copyDBFile();
        }
    }

    private void copyDBFile() {
        try (InputStream input = context.getAssets().open(DB_NAME); OutputStream output = new FileOutputStream(DB_PATH + DB_NAME)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public boolean openDataBase() throws SQLException {
        database = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return database != null;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            needUpdate = true;
    }
}