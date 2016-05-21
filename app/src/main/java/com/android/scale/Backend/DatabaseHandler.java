package com.android.scale.Backend;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mmadhusoodan on 5/19/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "Log-Messages";
    private static String DB_PATH = "/data/data/com.android.scale/databases/";

    private static String DB_NAME = "image.db";
    private final Context myContext;
    String SQL_CREATE_ENTRIES = "CREATE TABLE image_table (_id INTEGER PRIMARY KEY AUTOINCREMENT,image BLOB)";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public boolean checkDataBase(SQLiteDatabase db) {
        try {
            String DB_FULL_PATH = DB_PATH + DB_NAME;
            File file = new File(DB_FULL_PATH);
            if (file.exists()) {
                Log.d(TAG, "DB found ");
                Cursor c = db.rawQuery("SELECT * FROM image_table ", null);
                Log.d(TAG, "DB has " + c.getCount() + " rows ");
            } else {
                Log.d(TAG, "No DB found ");
                db.rawQuery(SQL_CREATE_ENTRIES, null);
            }
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getDbRowCount() throws SQLException {
        try {
            ArrayList arrayList = new ArrayList();

            SQLiteDatabase db = this.getReadableDatabase();
            Log.d(TAG, "Database Location :" + db.getPath());
            String countQuery = "SELECT count(*) FROM image_table ";
            Log.d(TAG, "QUERY:" + countQuery);

            Cursor cursor = db.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    String trend = cursor.getString(1);
                    arrayList.add(trend);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            Log.d(TAG, "Row Count: " + arrayList.size());
            return arrayList.size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}