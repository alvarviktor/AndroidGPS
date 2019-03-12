package c4985.androidgps;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyLocationListener implements LocationListener {

    private Activity activity;

    private Connector destination;

    private boolean isConnected;

    public static String locationMsg;
    public static boolean canSend;

    public MyLocationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView latTextView = this.activity.findViewById(R.id.latValue);
        TextView lngTextView = this.activity.findViewById(R.id.lngValue);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        latTextView.setText(String.valueOf(new DecimalFormat("###0.00").format(latitude)));
        lngTextView.setText(String.valueOf(new DecimalFormat("###0.00").format(longitude)));

        locationMsg = Double.toString(latitude) + "&" + Double.toString(longitude);
//        if (isConnected) {
//            destination.unlock();
//        }
        canSend = true;
//  if (isConnected) {
//            String message = Double.toString(latitude) + "&" + Double.toString(longitude);
//            destination.sendData(message);
//        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setConnector(Connector destination) {
        this.destination = destination;
        destination.connect();
    }

    public void setActive() {
        isConnected = true;
    }

    public void setInactive() {
        isConnected = false;
    }


    public String getMessage() {
        return locationMsg;
    }
}
