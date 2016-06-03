package com.android.scale;

/**
 * Created by mmadhusoodan on 5/30/16.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.android.scale.Backend.DataBaseHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SplashScreen extends Activity {

    private static final String TAG = "Log-SplashScreen";
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
            dataBaseHelper.checkDataBase();
            int rowCount = dataBaseHelper.getDbRowCount();


            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            Log.i(TAG, "Moving to MainActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // put your code here...

    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long dbInsertImage(Bitmap myImage) {
        try {
            byte[] data = getBitmapAsByteArray(myImage);
            ContentValues values = new ContentValues();
            values.put("image", data);
            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
            long rowid = db.insert("image_table", null, values);
            if (rowid == -1) {
                Log.i(TAG, "ERROR");
            } else {
                Log.i(TAG, "IMAGE INSERTED IN DB : rowid " + rowid);
            }
            return rowid;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.close();
        }
    }

    public boolean dbInsertMultipleImages(ArrayList<Bitmap> imageList) {
        try {
            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
            String sql = "INSERT INTO image_table ('image')" + "VALUES (?);";
            SQLiteStatement statement = db.compileStatement(sql);
            db.beginTransaction();
            for (int i = 0; i < imageList.size(); i++) {
                Bitmap myImage = imageList.get(i);
                byte[] data = getBitmapAsByteArray(myImage);
                statement.clearBindings();
                statement.bindBlob(1, data);
                statement.execute();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            Cursor c = db.rawQuery("select count(*) from image_table; ", null);

            String strCount = "";
            if (c.moveToFirst()) {
                strCount = c.getString(c.getColumnIndex("count(*)"));
            }
            Log.i(TAG, "After Insert total rows " + strCount + " Rows");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}