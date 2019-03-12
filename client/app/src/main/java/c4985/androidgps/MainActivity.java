/*---------------------------------------------------------------------------------------
--	Source File:	MainActivity.java -
--
--	Classes:
--
--	Methods:
--
--	Date:			March 7, 2019
--
--	Revisions:		(Date and Description)
--
--	Designer:		Viktor Alvar
--
--	Programmer:		Viktor Alvar
--
--	Notes:
--
---------------------------------------------------------------------------------------*/

package c4985.androidgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.net.*;
import java.io.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @SuppressLint("MissingPermission")
    public void initLocationHandler() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationListener locationListener = new MyLocationListener(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 500, locationListener);
    }

    public void promptLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationHandler();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission is needed to show location.", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promptLocationPermissions();
        findViewById(R.id.getLocationButton).setOnClickListener(this);
        findViewById(R.id.connectButton).setOnClickListener(this);
        findViewById(R.id.updateButton).setOnClickListener(this);
        findViewById(R.id.disconnectButton).setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationHandler();
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.getLocationButton:
                promptLocationPermissions();
                break;

            case R.id.connectButton:
                break;

            case R.id.updateButton:
                break;

            case R.id.disconnectButton:
                break;
        }
    }
}
