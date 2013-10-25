package ch.luethi.skylinesclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import com.geeksville.location.SkyLinesTrackingWriter;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PositionService extends Service implements LocationListener {

    private SkyLinesTrackingWriter skyLinesTrackingWriter = null;
    private LocationManager locationManager;
    private SendThread sendThread;


    @Override
    public void onCreate() {
        super.onCreate();    //To change body of overridden methods use File | Settings | File Templates.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sendThread = new SendThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        return super.onStartCommand(intent, flags, startId);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        locationManager.removeUpdates(this);
        skyLinesTrackingWriter = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "PositionService onLocationChanged, lat=" + location.getLatitude() + " log=" + location.getLongitude(), Toast.LENGTH_LONG).show();


        // The emulator will falsely claim 0 for the first point reported -
        // skip it
        if (location.getLatitude() != 0.0) {
            sendThread.setLocation(location);
            new Thread(sendThread).start();
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProviderEnabled(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProviderDisabled(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private class SendThread implements Runnable {

        private Location location;

        public void setLocation(Location _location) {
            location = _location;
        }

        @Override
        public void run() {
            try {
                double lat = location.getLatitude();
                double longitude = location.getLongitude();
                float kmPerHr = location.hasSpeed() ? (float) (location.getSpeed() * 3.6) : Float.NaN;
                // convert m/sec to km/hr

                float[] accelVals = null;
                float vspd = Float.NaN;
                getOrCreateSkyLinesTrackingWriter().emitPosition(location.getTime(), lat, longitude,
                        location.hasAltitude() ? (float) location.getAltitude() : Float.NaN,
                        (int) location.getBearing(), kmPerHr, accelVals, vspd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isEmulator() {
        return Build.MANUFACTURER.equals("unknown");
    }

    private SkyLinesTrackingWriter getOrCreateSkyLinesTrackingWriter() {
        if (skyLinesTrackingWriter == null) {
            String ip_address;
            if (isEmulator()) {
                ip_address = "10.20.11.27";
            } else {
                ip_address = "78.47.50.46";
            }
            try {
                skyLinesTrackingWriter = new SkyLinesTrackingWriter(1, 1, ip_address); // ToDo -- get params
                Toast.makeText(this, "PositionService Started with  ip address " + ip_address, Toast.LENGTH_LONG).show();
            } catch (SocketException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnknownHostException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return skyLinesTrackingWriter;
    }
}
