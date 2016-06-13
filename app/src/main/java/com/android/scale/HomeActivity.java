package com.android.scale;

/**
 * Created by mmadhusoodan on 5/30/16.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.scale.Backend.DataBaseHelper;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends Activity {
    private static final String TAG = "Log-HomeActivity";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imgLogo = null;
    private TextView locationTextView = null;
    DataBaseHelper dataBaseHelper = null;
    private SQLiteDatabase sqlDb;
    String city, state, postalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);
        locationTextView = (TextView) findViewById(R.id.cityState);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "ACCESS_FINE_LOCATION &ACCESS_COARSE_LOCATION NOT SET", Toast.LENGTH_SHORT).show();
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        List<Address> addresses;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            postalCode = addresses.get(0).getPostalCode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationTextView.setBackgroundColor(Color.RED);
        locationTextView.setText(city + ", " + state);
        Log.i(TAG, ("(" + city + ", " + state + ")"));
        Toast.makeText(getApplicationContext(), city + ", " + state, Toast.LENGTH_SHORT).show();

        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqlDb = dataBaseHelper.getWritableDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_home);
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
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                Bitmap myBitmap = (Bitmap) data.getExtras().get("data");
                dataBaseHelper = new DataBaseHelper(this);
                dataBaseHelper.dbInsertImage(sqlDb, myBitmap);
                startActivity(new Intent(HomeActivity.this, DisplayActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}