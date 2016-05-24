package com.android.scale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.scale.Backend.DatabaseHandler;
import com.android.scale.Backend.Instagram4J;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String TAG = "Log-MainActivity";
    private ImageView imageView1, imageView2;
    private TextView textView1, textView2;
    private SQLiteDatabase db;
    private String str = "SachinTendulkar";
    private Instagram4J instagram4J = new Instagram4J();
    private ArrayList<Bitmap> imageList = null;

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

        textView1 = (TextView) findViewById(R.id.textView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView2 = (TextView) findViewById(R.id.textView2);
    }

    public void detectFaceInTheImage(View view) {
        try {
            imageList = instagram4J.getBitmapsFromTagSearch(str);
            Log.i(TAG, "Row Number " + insertImage(imageList.get(2)));

            insertMultipleImages(imageList);

            Bitmap myBitmap = imageList.get(2);
            imageView1 = (ImageView) findViewById(R.id.imageView1);
            imageView1.setImageBitmap(myBitmap);

            Paint myRectPaint = new Paint();
            myRectPaint.setStrokeWidth(5);
            myRectPaint.setColor(Color.RED);
            myRectPaint.setStyle(Paint.Style.STROKE);

            Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(myBitmap, 0, 0, null);

            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();
            if (!faceDetector.isOperational()) {
                new AlertDialog.Builder(getApplicationContext()).setMessage("Could not set up the face detector!").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            for (int i = 0; i < faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                float x1 = thisFace.getPosition().x;
                float y1 = thisFace.getPosition().y;
                float x2 = x1 + thisFace.getWidth();
                float y2 = y1 + thisFace.getHeight();
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
            }
            imageView1.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public long insertImage(Bitmap img) {
        try {
            byte[] data = getBitmapAsByteArray(img);
            ContentValues values = new ContentValues();
            values.put("image", data);
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

    public boolean insertMultipleImages(ArrayList<Bitmap> imageList) {
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