package com.android.scale;

/**
 * Created by mmadhusoodan on 5/30/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.scale.Backend.DataBaseHelper;

public class CaptureActivity extends Activity {

    private static final String TAG = "Log-CaptureActivity";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imgLogo = null;
    DataBaseHelper dataBaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.checkDataBase();
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_capture);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.checkDataBase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.close();
    }

    public void captureImage(View view) {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearDb(View view) {
        try {
            dataBaseHelper = new DataBaseHelper(this);
            if (dataBaseHelper.deleteRows()) {
                Toast.makeText(this, "Database CLEAN", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            startActivity(new Intent(CaptureActivity.this, DisplayActivity.class));
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                Bitmap myBitmap = (Bitmap) data.getExtras().get("data");
                dataBaseHelper = new DataBaseHelper(this);
                dataBaseHelper.dbInsertImage(myBitmap);
                startActivity(new Intent(CaptureActivity.this, DisplayActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}