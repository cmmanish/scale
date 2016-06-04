package com.android.scale;

/**
 * Created by mmadhusoodan on 5/30/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.scale.Backend.DataBaseHelper;

import java.util.ArrayList;

public class SplashScreen extends Activity {

    private static final String TAG = "Log-SplashScreen";
    private static final int CAMERA_REQUEST = 1888;
    private SQLiteDatabase db;
    private ImageView imgLogo;
    private DataBaseHelper dataBaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        if (dataBaseHelper.checkDataBase()) {
            Bitmap dbBitmap = dataBaseHelper.getLatestImage();
            imgLogo.setImageBitmap(null);
            imgLogo.setImageBitmap(dbBitmap);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.close();
    }

    public void captureSourceImage(View view) {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void captureDestination(View view) {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                Bitmap myBitmap = (Bitmap) data.getExtras().get("data");
                DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
                dataBaseHelper.dbInsertImage(myBitmap);
                Bitmap dbBitmap = dataBaseHelper.getLatestImage();

                imgLogo.setMaxHeight(1);
                imgLogo.setMaxWidth(1);
                imgLogo.setImageBitmap(dbBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                byte[] data = dataBaseHelper.getBitmapAsByteArray(myImage);
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
}