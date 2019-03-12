package c4985.androidgps;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MyLocationListener implements LocationListener {

    private Activity activity;

    public MyLocationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView latTextView = this.activity.findViewById(R.id.latValue);
        TextView lngTextView = this.activity.findViewById(R.id.lngValue);
        latTextView.setText(String.valueOf(new DecimalFormat("###0.00").format(location.getLatitude())));
        lngTextView.setText(String.valueOf(new DecimalFormat("###0.00").format(location.getLongitude())));


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
}
