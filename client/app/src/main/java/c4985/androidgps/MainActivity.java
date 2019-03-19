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
--	Designer:		Simon Wu, Viktor Alvar
--
--	Programmer:		Simon Wu, Viktor Alvar
--
--	Notes:
--
---------------------------------------------------------------------------------------*/

package c4985.androidgps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final int REFRESH_TIME = 5000;
    private static final int REFRESH_DISTANCE = 50;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private String hostName;
    private String portNumber;
    private String deviceName;

    private Activity myActivity = this;
    private ConnectorTask connectorTask;

    private boolean isConnected;
    private boolean canSend;
    private Location myLocation;

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public void getLocation(View view) {
        if (initLocationServices()) {
            view.setEnabled(false);
            findViewById(R.id.disconnectButton).setEnabled(true);
            setStatus(R.id.gpsValue, R.string.gpsValueRunning, R.color.statusGreen);
        }
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public void connectServer(View view) {
        if (validateEditText()) {
            if (initLocationServices()) {
                findViewById(R.id.getLocationButton).setEnabled(false);
                findViewById(R.id.disconnectButton).setEnabled(true);
                setStatus(R.id.gpsValue, R.string.gpsValueRunning, R.color.statusGreen);
                connectorTask = new ConnectorTask();
                connectorTask.start();
                Toast.makeText(this, "Attempting Connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Text Fields Cannot Be Empty", Toast.LENGTH_LONG).show();
        }
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public void disconnectServer(View view) {
        if (isConnected) {
            isConnected = false;
            connectorTask = null;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            locationListener = null;
        }

        view.setEnabled(false);
        findViewById(R.id.connectButton).setEnabled(true);
        findViewById(R.id.getLocationButton).setEnabled(true);
        setStatus(R.id.statusValue, R.string.statusValueDisconnected, R.color.statusRed);
        setStatus(R.id.gpsValue, R.string.gpsValueStopped, R.color.statusRed);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public boolean validateEditText() {
        hostName = ((EditText) findViewById(R.id.ipEditText)).getText().toString();
        portNumber = ((EditText) findViewById(R.id.portEditText)).getText().toString();
        deviceName = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        return !hostName.isEmpty() && !portNumber.isEmpty() && !deviceName.isEmpty();
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public void updateUI(Location location) {
        TextView latTextView = findViewById(R.id.latValue);
        TextView lngTextView = findViewById(R.id.lngValue);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        latTextView.setText(String.valueOf(new DecimalFormat("###0.0000").format(latitude)));
        lngTextView.setText(String.valueOf(new DecimalFormat("###0.0000").format(longitude)));
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public void setStatus(int statusViewId, int statusMessageId, int statusColorId) {
        TextView statusTextView = findViewById(statusViewId);
        statusTextView.setText(statusMessageId);
        statusTextView.setTextColor(ContextCompat.getColor(this, statusColorId));
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public String getDeviceIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService (WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return String.valueOf(ipAddress);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public String getCurrentTime() {
        return DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:
    --
    --	DATE:
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:
    --
    --	INTERFACE:
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --
    ---------------------------------------------------------------------------------------*/
    public boolean initLocationServices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            String[] permissionsList = new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
            ActivityCompat.requestPermissions(this, permissionsList, REQUEST_FINE_LOCATION);
            return false;
        }
        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REFRESH_TIME, REFRESH_DISTANCE, locationListener);
        return true;
    }

    private class MyLocationListener implements LocationListener {

        /*---------------------------------------------------------------------------------------
        --	FUNCTION:
        --
        --	DATE:
        --
        --	REVISIONS:	    (Date and Description)
        --
        --	DESIGNER:		Simon Wu, Viktor Alvar
        --
        --	PROGRAMMER:
        --
        --	INTERFACE:
        --
        --	RETURNS:		void.
        --
        --	NOTES:
        --
        ---------------------------------------------------------------------------------------*/
        @Override
        public void onLocationChanged(Location location) {
            updateUI(location);
            myLocation = location;
            canSend = true;
        }

        // Not Implemented
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        // Not Implemented
        @Override
        public void onProviderEnabled(String provider) { }

        // Not Implemented
        @Override
        public void onProviderDisabled(String provider) { }
    }

    private class ConnectorTask extends Thread {

        /*---------------------------------------------------------------------------------------
        --	FUNCTION:
        --
        --	DATE:
        --
        --	REVISIONS:	    (Date and Description)
        --
        --	DESIGNER:		Simon Wu, Viktor Alvar
        --
        --	PROGRAMMER:
        --
        --	INTERFACE:
        --
        --	RETURNS:		void.
        --
        --	NOTES:
        --
        ---------------------------------------------------------------------------------------*/
        @Override
        public void run() {
            try {
                Socket socket = new Socket(hostName, Integer.parseInt(portNumber));
                isConnected = true;
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connectButton).setEnabled(false);
                        findViewById(R.id.disconnectButton).setEnabled(true);
                        setStatus(R.id.statusValue, R.string.statusValueConnected, R.color.statusGreen);
                    }
                });

                while (isConnected) {
                    if (canSend) {
                        canSend = false;
                        try {
                            OutputStream outToServer = socket.getOutputStream();
                            DataOutputStream out = new DataOutputStream(outToServer);
                            double latitude = myLocation.getLatitude();
                            double longitude = myLocation.getLongitude();
                            String data = new JSONObject().put("lat", latitude).put("lng", longitude)
                                    .put("name", deviceName).put("ip", getDeviceIpAddress())
                                    .put("time", getCurrentTime()).toString();
                            out.write((data + "\n").getBytes());

                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView recentUpdate = findViewById(R.id.lastUpdateValue);
                                    recentUpdate.setText(getCurrentTime());
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                socket.close();
            } catch  (IOException e) {
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(myActivity, "Socket Operation Failed", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
        }
    }
}
