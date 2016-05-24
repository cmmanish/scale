package com.android.scale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.scale.Backend.DatabaseHandler;
import com.android.scale.Backend.Instagram4J;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String TAG = "Log-MainActivity";
    private ImageView imageView1, imageView2;
    private TextView textView1, textView2;
    private SQLiteDatabase db;
    private String str = "VIRAT";
    Instagram4J instagram4J = new Instagram4J();
    ArrayList<Bitmap> imageList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        db = this.openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        //Log.i(TAG, "Database Dropped " + databaseHandler.dropTable(db));

        Log.i(TAG, "Database " + databaseHandler.checkDataBase());

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        textView1 = (TextView) findViewById(R.id.textView1);

        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView2 = (TextView) findViewById(R.id.textView2);
    }

    public void downloadSaveAndDisplayImage(View view) {
        try {
            imageList = instagram4J.getBitmapsFromTagSearch(str);
            Log.i(TAG, "Row Number " + insertImage(imageList.get(2)));
            Toast.makeText(this, "Insert Success", Toast.LENGTH_SHORT).show();
            imageView1.setImageBitmap(imageList.get(2));
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchAndDisplayImage(View view) {
        try {
            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
            Cursor c = db.rawQuery("select * from image_table ORDER BY _id DESC", null);
            if (c.moveToNext()) {
                byte[] image = c.getBlob(1);
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                imageView2.setImageBitmap(bmp);
                Toast.makeText(this, "DISPLAY Success", Toast.LENGTH_SHORT).show();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long insertImage(Bitmap img) {

        byte[] data = getBitmapAsByteArray(img);
        ContentValues values = new ContentValues();
        values.put("image", data);
        try {
            db = openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
            long rowid = db.insert("image_table", null, values);
            if (rowid == -1) {
                Log.i(TAG, "ERROR");
            } else {
                Log.i(TAG, "IMAGE INSERTED IN DB");
            }
            return rowid;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.close();
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}