package com.android.scale.Backend;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

/**
 * Created by mmadhusoodan on 5/19/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "Log-DataBaseHelper";
    private static String DB_PATH = "/data/data/com.android.scale/databases/";
    private static String DB_NAME = "image.db";
    private final Context myContext;
    private String SQL_CREATE_ENTRIES = "CREATE TABLE image_table (_id INTEGER PRIMARY KEY AUTOINCREMENT,image BLOB)";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            File database = myContext.getDatabasePath(DB_NAME);
            if (database.exists()) {
                Log.i("Database", "Found");
                String myPath = database.getAbsolutePath();
                Log.i("Database Path", myPath);
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
                if (doesTableExist(checkDB, "image_table")) {
                    Cursor c = checkDB.rawQuery("SELECT * FROM image_table ", null);
                    Log.i(TAG, "DB has " + c.getCount() + " rows ");
                } else {
                    Log.i("TABLE", "Not Found");
                    Cursor c = checkDB.rawQuery(SQL_CREATE_ENTRIES, null);
                    Log.i(TAG, "DB has " + c.getCount() + " rows ");
                }
            } else {
                Log.i("Database", "Not Found");
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.i("Database", "Not Found");
        }
        return checkDB != null ? true : false;
    }

    public int getDbRowCount() throws SQLException {

        SQLiteDatabase db = null;
        try {
            File database = myContext.getDatabasePath(DB_NAME);
            String myPath = database.getAbsolutePath();
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            Cursor c = db.rawQuery("select count(*) from image_table; ", null);

            String strCount = "";
            if (c.moveToFirst()) {
                strCount = c.getString(c.getColumnIndex("count(*)"));
            }
            Log.i(TAG, "After Insert total rows " + strCount + " Rows");
            int count = Integer.parseInt(strCount);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.close();
        }
    }

    public boolean dropTable(SQLiteDatabase db) {
        try {
            String DB_FULL_PATH = DB_PATH + DB_NAME;
            File file = new File(DB_FULL_PATH);
            if (file.exists()) {
                Log.i(TAG, "DB found ");
                Cursor c = db.rawQuery("DROP TABLE image_table ", null);
                Log.i(TAG, "TABLE DRPOPPED ");
            } else {
                Log.i(TAG, "No DB found ");
                Cursor c = db.rawQuery("DROP TABLE image_table ", null);
            }
            db.close();
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap getLatestImage(SQLiteDatabase db) throws SQLException {
        Bitmap bmp = null;
        try {
            Cursor c = db.rawQuery("select * from image_table ORDER BY _id DESC", null);
            if (c.moveToNext()) {
                byte[] image = c.getBlob(1);
                bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
        }
    }

    public Bitmap getImageN(SQLiteDatabase db, int rowId) throws SQLException {
        Bitmap bmp = null;
        try {
            Cursor c = db.rawQuery("select * from image_table where _id == " + rowId, null);
            if (c.moveToNext()) {
                byte[] image = c.getBlob(1);
                bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //    public long insertImage(Bitmap img) {
    //        try {
    //            byte[] data = getBitmapAsByteArray(img);
    //            ContentValues values = new ContentValues();
    //            values.put("image", data);
    //            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
    //            long rowid = db.insert("image_table", null, values);
    //            if (rowid == -1) {
    //                Log.i(TAG, "ERROR");
    //            } else {
    //                Log.i(TAG, "IMAGE INSERTED IN DB");
    //            }
    //            return rowid;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            return -1;
    //        } finally {
    //            db.close();
    //        }
    //    }
}