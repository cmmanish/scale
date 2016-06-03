package com.android.scale;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.scale.Backend.DataBaseHelper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.Random;

public class MainActivity extends Activity {

    private static final String TAG = "Log-MainActivity";
    private ImageView imageView1, imageView2;
    private TextView textView1, textView2;
    private SQLiteDatabase db;
    private int maxRowCount = 500;
    private volatile int imageRow = 500;
    private int rowCount = 0;
    private DataBaseHelper dataBaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        textView1 = (TextView) findViewById(R.id.textView1);

        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView2 = (TextView) findViewById(R.id.textView2);

        dataBaseHelper = new DataBaseHelper(getApplicationContext());
        rowCount = dataBaseHelper.getDbRowCount();
    }

    public void fetchAndDisplayImage(View view) {
        try {
            dataBaseHelper = new DataBaseHelper(getApplicationContext());
            db = this.openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);

            Random rn = new Random();
            rowCount = dataBaseHelper.getDbRowCount();
            int n = (rowCount < maxRowCount) ? rowCount : maxRowCount;

            imageRow = rn.nextInt(n) + 1;

            Bitmap myBitmap = dataBaseHelper.getImageN(db, imageRow);
            imageView1.setImageBitmap(myBitmap);
            Toast.makeText(this, "DISPLAY Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void detectFaceInTheImage(View view) {
        try {
            dataBaseHelper = new DataBaseHelper(getApplicationContext());
            db = this.openOrCreateDatabase("image.db", Context.MODE_PRIVATE, null);
            Bitmap myBitmap = dataBaseHelper.getImageN(db, imageRow);

            imageView2.setImageBitmap(myBitmap);

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
            imageView2.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
            Toast.makeText(this, faces.size() + " faces detected", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}