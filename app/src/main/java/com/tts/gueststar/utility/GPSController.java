package com.tts.gueststar.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.util.Log;

public class GPSController {
    private LocationManager lmGPS;
    private LocationListener locationListener;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private static Context myCtx;
    private boolean mbUpdatesStopped = true;//PP

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    private static GPSController mGetLatLongFromGPS;

    public static GPSController getinstance(Context ctx) {
        myCtx = ctx;
        if (mGetLatLongFromGPS == null)
            mGetLatLongFromGPS = new GPSController(ctx);
        return mGetLatLongFromGPS;
    }

    private GPSController(Context ctx) {
        myCtx = ctx;
        lmGPS = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startGPS() {
        Log.d("START GPS RUNNING", "RUNNING");

        if (lmGPS == null && myCtx == null)
            lmGPS = (LocationManager) myCtx.getSystemService(Context.LOCATION_SERVICE);

        if (locationListener == null)
            locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(myCtx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myCtx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lmGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        Log.e("Request Updates", "GPS UPDATES");

        lmGPS.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, locationListener);
        Log.e("Request Updates", "NETWORK UPDATES");
        Location loc = lmGPS.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLocation(loc);
        if (loc == null)
            loc = lmGPS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        updateLocation(loc);
        mbUpdatesStopped = false;
    }

    public void stopLocationListening() {
        Log.e("stopLocationListening", "stopLocationListening");
        if (lmGPS != null) {
            lmGPS.removeUpdates(locationListener);
            mbUpdatesStopped = true;
        }
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            Location curLoc = loc;
            Log.e("MyLocationListener", loc.toString());
            if (curLoc.hasAccuracy()) {
                updateLocation(curLoc);
                Log.e("myCtx 1 ", myCtx + "");

            } else {
                loc = null;
            }
        }

        public void onProviderDisabled(String provider) {
            Log.e("ProviderDisable", provider + "   *?*");
        }

        public void onProviderEnabled(String provider) {
            Log.e("ProviderEnable", provider + "   *?*");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e("StatusChange",
                    provider + " : " + status + " : " + extras.toString());
        }
    }

    private void updateLocation(Location loc) {
        if (loc != null) {
            mLatitude = loc.getLatitude();
            mLongitude = loc.getLongitude();
            Log.e("Lat  &  Lon", mLatitude + "  :  " + mLongitude + "  in updateLocation  ");
        } else {
            Log.e("Sorry", "LOC not found");
            mLatitude = 0.0;
            mLongitude = 0.0;
        }
    }
}
