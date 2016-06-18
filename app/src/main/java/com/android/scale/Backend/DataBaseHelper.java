package com.android.scale.Backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by mmadhusoodan on 5/19/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "Log-DataBaseHelper";
    private static String DB_NAME = "image.db";
    private final Context myContext;
    private String SQL_CREATE_ENTRIES = "CREATE TABLE image_table (_id INTEGER PRIMARY KEY AUTOINCREMENT,image BLOB)";

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DataBaseHelper.class.getName(), "Upgrading database from version" + oldVersion + "to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT _id, image,  FROM image_table ORDER BY name", null));
    }

    public long dbInsertImage(SQLiteDatabase db, Bitmap myImage) {
        try {
            byte[] data = getBitmapAsByteArray(myImage);
            ContentValues values = new ContentValues();
            values.put("image", data);
            long rowid = db.insert("image_table", null, values);
            if (rowid != -1) {
                Log.i(TAG, "IMAGE INSERTED IN DB : rowid " + rowid);
            } else {
                Log.i(TAG, "ERROR");
            }
            return rowid;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public int getRowCount(SQLiteDatabase db, String tableName) throws SQLException {

        int count = 0;
        try {
            Cursor c = db.rawQuery("SELECT * FROM image_table; ", null);
            Log.i(TAG, "DB has " + c.getCount() + " rows ");
            count = c.getCount();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteRows() {
        SQLiteDatabase db = null;
        try {
            File database = myContext.getDatabasePath(DB_NAME);
            String myPath = database.getAbsolutePath();
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            Cursor c = db.rawQuery("DROP TABLE image_table ", null);
            Log.i(TAG, c.getCount() + " Rows in Db now");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap getLatestImage(SQLiteDatabase db) throws SQLException {
        Bitmap bmp = null;
        try {
            File database = myContext.getDatabasePath(DB_NAME);
            String myPath = database.getAbsolutePath();
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            Cursor c = db.rawQuery("select * from image_table ORDER BY _id DESC", null);
            if (c.moveToNext()) {
                byte[] image = c.getBlob(1);
                bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        }
    }

//    public boolean dbInsertMultipleImages(ArrayList<Bitmap> imageList) {
//        try {
//            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
//            String sql = "INSERT INTO image_table ('image')" + "VALUES (?);";
//            SQLiteStatement statement = db.compileStatement(sql);
//            db.beginTransaction();
//            for (int i = 0; i < imageList.size(); i++) {
//                Bitmap myImage = imageList.get(i);
//                byte[] data = dataBaseHelper.getBitmapAsByteArray(myImage);
//                statement.clearBindings();
//                statement.bindBlob(1, data);
//                statement.execute();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//            Cursor c = db.rawQuery("select count(*) from image_table; ", null);
//
//            String strCount = "";
//            if (c.moveToFirst()) {
//                strCount = c.getString(c.getColumnIndex("count(*)"));
//            }
//            Log.i(TAG, "After Insert total rows " + strCount + " Rows");
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            db.close();
//        }
//    }
}