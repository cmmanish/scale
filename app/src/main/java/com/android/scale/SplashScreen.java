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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.scale.Backend.DataBaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SplashScreen extends Activity {

    private static final String TAG = "Log-SplashScreen";
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int CAMERA_REQUEST = 1888;
    private SQLiteDatabase db;
    private File sourcePhotoFile = new File("");
    private File destinationPhotoFile = new File("");
    private ImageView myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        myImage = (ImageView) findViewById(R.id.imgLogo);
        myImage.setBackgroundColor(Color.RED);

    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_splash);
    }

    public void captureSource(View view) {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                Bitmap myBitmap = (Bitmap) data.getExtras().get("data");
                myImage.setMaxWidth(20);
                myImage.setBackgroundColor(Color.RED);
                myImage.setImageBitmap(myBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void captureDestination(View view) {
        try {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
            dataBaseHelper.checkDataBase();
            int rowCount = dataBaseHelper.getDbRowCount();
            dataBaseHelper.deleteRows();
            Log.i(TAG, "Moving to MainActivity");

        } catch (Exception e) {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            e.printStackTrace();
        }
    }

    private File dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        //     File sourcePhotoFile = null;
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                sourcePhotoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "Exception thrown in dispatchTakePictureIntent()" + ex.toString());
            }
            // Continue only if the File was successfully created
            if (sourcePhotoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(sourcePhotoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        return sourcePhotoFile;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File file = new File("/storage/emulated/legacy/Pictures");
        String imageFileName = "FromAndroid_";
        File image = File.createTempFile(imageFileName, ".jpg", file);
        return image;
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