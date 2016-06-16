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
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    private static final String[] CAMERA_PERMS = {Manifest.permission.CAMERA};
    private static final String[] LOCATION_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int INITIAL_REQUEST = 1337;
    private static final int CAMERA_REQUEST = INITIAL_REQUEST + 1;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private ImageView imgLogo;
    private TextView cityText;
    private TextView text;
    private DataBaseHelper dataBaseHelper = null;
    private SQLiteDatabase sqlDb;
    private String city, state, postalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_home);

        imgLogo = (ImageView) findViewById(R.id.imgLogo);
        cityText = (TextView) findViewById(R.id.cityTextView);
        cityText.setText("ABC");

        setCityStateZipcode();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        sqlDb = dataBaseHelper.getWritableDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        setContentView(R.layout.activity_home);
        //setCityStateZipcode();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.checkDataBase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        dataBaseHelper = new DataBaseHelper(this);
        dataBaseHelper.close();
    }

    public void setCityStateZipcode() {

        try {
//            if (!canAccessLocation() || !canAccessContacts()) {
//                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
//            }

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "ACCESS_FINE_LOCATION &ACCESS_COARSE_LOCATION NOT SET", Toast.LENGTH_SHORT).show();
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            List<Address> addresses;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            postalCode = addresses.get(0).getPostalCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        cityText.setTextColor(Color.RED);
        cityText.setText(city + ", " + state);
        Log.i(TAG, ("Location: " + city + ", " + state));
        Toast.makeText(getApplicationContext(), city + ", " + state, Toast.LENGTH_SHORT).show();
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

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessCamera() {
        return (hasPermission(Manifest.permission.CAMERA));
    }

    private boolean canAccessContacts() {
        return (hasPermission(Manifest.permission.READ_CONTACTS));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }
}