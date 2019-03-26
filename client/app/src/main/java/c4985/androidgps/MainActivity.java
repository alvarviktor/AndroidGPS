/*---------------------------------------------------------------------------------------
--	Source File:	MainActivity.java - Starting point of the android application.
--
--	Classes:        MainActivity        - public class
--                  MyLocationListener  - private class
--                  ConnectorTask       - private class
--
--	Methods:
--                  onCreate            (MainActivity Class)
--                  onLocationChanged   (MyLocationListener Class)
--                  onStatusChanged     (MyLocationListener Class)
--                  onProviderEnabled   (MyLocationListener Class)
--                  onProviderDisabled  (MyLocationListener Class)
--                  run                 (ConnectorTask Class)
--
--                  void getLocation(View view)
--                  void connectServer(View view)
--                  void disconnectServer(View view)
--                  boolean validateEditText()
--                  void updateUI(Location location)
--                  void setStatus(int statusViewId, int statusMessageId, int statusColorId)
--                  String getDeviceIpAddress()
--                  String getCurrentTime()
--                  boolean initLocationServices()
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
--  Starting point of the android application. Renders the app layout and contains
--  inner classes that enables gps and socket functionality. The user connects via a
--  tcp socket and continuously sends the current location.
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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final int REFRESH_TIME = 5000;    // 5 seconds
    private static final int REFRESH_DISTANCE = 50;  // 50 meters

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
    --	FUNCTION:       onCreate
    --
    --	DATE:           March 7, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Simon Wu, Viktor Alvar
    --
    --	INTERFACE:      onCreate(Bundle savedInstanceState)
    --                      Bundle savedInstanceState: Previous state data
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Creates and renders the android application activity. This function is called when
    --  the android application is opened.
    ---------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       getLocation
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      getLocation(View view)
    --                      View view: Button
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Gets the location. This function is called when the get location button is clicked.
    --  Initializes location services and updates ui elements on success. If the function
    --  is called for the first time the user may need to click the button twice to enable
    --  the location services.
    ---------------------------------------------------------------------------------------*/
    public void getLocation(View view) {
        if (initLocationServices()) {
            // Update ui elements
            view.setEnabled(false);
            findViewById(R.id.disconnectButton).setEnabled(true);
            setStatus(R.id.gpsValue, R.string.gpsValueRunning, R.color.statusGreen);
        }
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       connectServer
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Simon Wu
    --
    --	INTERFACE:      connectServer(View view)
    --                      View view: Button
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Connects to the server. This function is called when the connect button is clicked.
    --  Ensures that the text fields are valid before connecting. If a connection is
    --  established a new thread is created to process the send operation.
    ---------------------------------------------------------------------------------------*/
    public void connectServer(View view) {
        if (validateEditText()) {
            if (initLocationServices()) {
                // Update ui elements
                findViewById(R.id.getLocationButton).setEnabled(false);
                findViewById(R.id.disconnectButton).setEnabled(true);
                setStatus(R.id.gpsValue, R.string.gpsValueRunning, R.color.statusGreen);

                // Create send thread
                connectorTask = new ConnectorTask();
                connectorTask.start();
                Toast.makeText(this, "Attempting Connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Text Fields Cannot Be Empty", Toast.LENGTH_LONG).show();
        }
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       disconnectServer
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Simon Wu
    --
    --	INTERFACE:      disconnectServer(View view)
    --                      View view: Button
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Disconnects the connection. This function is called when the disconnect button is
    --  clicked. Signals the send thread to exit and close the socket. Removes the location
    --  updates and deallocate the listener object.
    ---------------------------------------------------------------------------------------*/
    public void disconnectServer(View view) {
        if (isConnected) {
            // Signal to exit thread
            isConnected = false;
            connectorTask = null;
        }

        // Remove location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            locationListener = null;
        }

        // Update ui elements
        view.setEnabled(false);
        findViewById(R.id.connectButton).setEnabled(true);
        findViewById(R.id.getLocationButton).setEnabled(true);
        setStatus(R.id.statusValue, R.string.statusValueDisconnected, R.color.statusRed);
        setStatus(R.id.gpsValue, R.string.gpsValueStopped, R.color.statusRed);

        TextView latitudeVal = findViewById(R.id.latValue);
        TextView longitudeVal = findViewById(R.id.lngValue);
        TextView recentUpdate = findViewById(R.id.lastUpdateValue);
        latitudeVal.setText(R.string.latPlaceholder);
        longitudeVal.setText(R.string.lngPlaceholder);
        recentUpdate.setText(R.string.lastUpdateValue);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       validateEditText
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      validateEditText()
    --
    --	RETURNS:		boolean.
    --
    --	NOTES:
    --  Validates the text fields. This function is called when the user attempts to
    --  connect to a server. Returns true if the text fields are not empty, false otherwise.
    ---------------------------------------------------------------------------------------*/
    public boolean validateEditText() {
        hostName = ((EditText) findViewById(R.id.ipEditText)).getText().toString();
        portNumber = ((EditText) findViewById(R.id.portEditText)).getText().toString();
        deviceName = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        return !hostName.isEmpty() && !portNumber.isEmpty() && !deviceName.isEmpty();
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       updateUI
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      updateUI(Location location)
    --                      Location location: Location object
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Updates the user interface with the most recent location. This function is called
    --  when the location listener generates a new location.
    ---------------------------------------------------------------------------------------*/
    public void updateUI(Location location) {
        // Get location info
        TextView latTextView = findViewById(R.id.latValue);
        TextView lngTextView = findViewById(R.id.lngValue);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Display location
        latTextView.setText(String.valueOf(new DecimalFormat("###0.0000").format(latitude)));
        lngTextView.setText(String.valueOf(new DecimalFormat("###0.0000").format(longitude)));
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       setStatus
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      setStatus(int statusViewId, int statusMessageId, int statusColorId)
    --                      int statusViewId: Text view
    --                      int statusMessageId: String resource
    --                      int statusColorId: Color resource
    --
    --	RETURNS:		void.
    --
    --	NOTES:
    --  Sets the status of a given text view. This function is called when the gps or
    --  socket connection is changed to show the user the current status.
    ---------------------------------------------------------------------------------------*/
    public void setStatus(int statusViewId, int statusMessageId, int statusColorId) {
        TextView statusTextView = findViewById(statusViewId);
        statusTextView.setText(statusMessageId);
        statusTextView.setTextColor(ContextCompat.getColor(this, statusColorId));
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       getDeviceIpAddress
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      getDeviceIpAddress()
    --
    --	RETURNS:		String.
    --
    --	NOTES:
    --  Gets the device's ip address. This function is called when the socket send operation
    --  is being executed. Returns the ip address as a String.
    ---------------------------------------------------------------------------------------*/
    public String getDeviceIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService (WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return String.valueOf(ipAddress);
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       getCurrentTime
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      getCurrentTime()
    --
    --	RETURNS:		String.
    --
    --	NOTES:
    --  Gets the current time of the android device. This function is called when the socket
    --  send operation is being executed. Return the current time as a String.
    ---------------------------------------------------------------------------------------*/
    public String getCurrentTime() {
        return DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }

    /*---------------------------------------------------------------------------------------
    --	FUNCTION:       initLocationServices
    --
    --	DATE:           March 13, 2019
    --
    --	REVISIONS:	    (Date and Description)
    --
    --	DESIGNER:		Simon Wu, Viktor Alvar
    --
    --	PROGRAMMER:     Viktor Alvar
    --
    --	INTERFACE:      initLocationServices()
    --
    --	RETURNS:		boolean.
    --
    --	NOTES:
    --  Initializes location service functionality on the android device. This function
    --  is called when the user wishes to get location or connect. Requests for location
    --  permissions and returns true if permission is granted, false otherwise.
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

    // GPS Inner Class
    private class MyLocationListener implements LocationListener {

        /*---------------------------------------------------------------------------------------
        --	FUNCTION:       onLocationChanged
        --
        --	DATE:           March 13, 2019
        --
        --	REVISIONS:	    (Date and Description)
        --
        --	DESIGNER:		Simon Wu, Viktor Alvar
        --
        --	PROGRAMMER:     Simon Wu, Viktor Alvar
        --
        --	INTERFACE:      onLocationChanged(Location location)
        --                      Location location: Location object
        --
        --	RETURNS:		void.
        --
        --	NOTES:
        --  Callback function when the location listener generates a new location. The new
        --  location is displayed and sent to the server.
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

    // Socket Send Inner Class
    private class ConnectorTask extends Thread {

        /*---------------------------------------------------------------------------------------
        --	FUNCTION:       run
        --
        --	DATE:           March 13, 2019
        --
        --	REVISIONS:	    (Date and Description)
        --
        --	DESIGNER:		Simon Wu, Viktor Alvar
        --
        --	PROGRAMMER:     Simon Wu
        --
        --	INTERFACE:      run()
        --
        --	RETURNS:		void.
        --
        --	NOTES:
        --  This function is called when the connect thread is executed. Creates a socket
        --  and sends a json object to the server. Sending only occurs when the location
        --  listener generates a new location.
        ---------------------------------------------------------------------------------------*/
        @Override
        public void run() {
            try {
                // Create socket
                Log.d("CONNECT:", "BEFORE");
                Socket socket = new Socket(hostName, Integer.parseInt(portNumber));
                Log.d("CONNECT:", "AFTER");
                isConnected = true;

                // Update ui
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.connectButton).setEnabled(false);
                        findViewById(R.id.disconnectButton).setEnabled(true);
                        setStatus(R.id.statusValue, R.string.statusValueConnected, R.color.statusGreen);
                    }
                });

                // Poll
                while (isConnected) {
                    if (canSend) {
                        canSend = false;
                        try {
                            OutputStream outToServer = socket.getOutputStream();
                            DataOutputStream out = new DataOutputStream(outToServer);
                            double latitude = myLocation.getLatitude();
                            double longitude = myLocation.getLongitude();

                            // Create json data
                            String data = new JSONObject().put("lat", latitude).put("lng", longitude)
                                    .put("device_id", deviceName).put("time", getCurrentTime())
                                    .put("device_ip", getDeviceIpAddress()).toString();
                            out.write((data + "\n").getBytes());

                            // Update ui
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
            } catch  (ConnectException e) {
                // Update ui
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(myActivity, "Connect Failed", Toast.LENGTH_LONG).show();
                    }
                });
                Log.d("CONNECT EXCEPTION:", "OCCURED");
                e.printStackTrace();
            } catch  (IOException e) {
                // Update ui
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
