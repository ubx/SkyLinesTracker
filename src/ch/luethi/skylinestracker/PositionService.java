/*
 * SkyLines Tracker is a location tracking client for the SkyLines platform <www.skylines-project.org>.
 * Copyright (C) 2013  Andreas Lüthi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.luethi.skylinestracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.geeksville.location.SkyLinesTrackingWriter;

import java.net.SocketException;
import java.net.UnknownHostException;


public class PositionService extends Service implements LocationListener {

    private SkyLinesTrackingWriter skyLinesTrackingWriter = null;
    private LocationManager locationManager;
    private SkyLinesPrefs prefs;
    private int posCount = 0;
    private HandlerThread senderThread;
    private ConnectivityManager connectivityManager;
    private String ipAddress;

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefs = new SkyLinesPrefs(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // service restarted by os
            posCount = prefs.getPosCount();
            Log.d("XXXX", "OS-restart PositionService, posCount=" + posCount);
        }
        skyLinesTrackingWriter = null;
        ipAddress = prefs.getIpAddress();
        senderThread = new HandlerThread("SenderThread");
        senderThread.start();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.getTrackingInterval() * 1000, 0, this, senderThread.getLooper());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("XXXX", "onDestroy, posCount=" + posCount);
        locationManager.removeUpdates(this);
        skyLinesTrackingWriter = null;
        senderThread.getLooper().quit();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isOnline()) {
            if (location.getLatitude() != 0.0) {

                double lat = location.getLatitude();
                double longitude = location.getLongitude();
                // convert m/sec to km/hr
                float kmPerHr = location.hasSpeed() ? location.getSpeed() * 3.6F : Float.NaN;
                float[] accelVals = null;
                float vspd = Float.NaN;
                getOrCreateSkyLinesTrackingWriter().emitPosition(location.getTime(), lat, longitude,
                        location.hasAltitude() ? (float) location.getAltitude() : Float.NaN,
                        (int) location.getBearing(), kmPerHr, accelVals, vspd);
                sendPositionStatus();
            }
        } else {
            sendConnectionStatus();
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (i != LocationProvider.AVAILABLE) {
            sendPositionWaitStatus();
        }
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
        sendPositionWaitStatus();
    }


    private SkyLinesTrackingWriter getOrCreateSkyLinesTrackingWriter() {
        if (skyLinesTrackingWriter == null) {
            try {
                skyLinesTrackingWriter = new SkyLinesTrackingWriter(prefs.getTrackingKey(), ipAddress);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return skyLinesTrackingWriter;
    }

    private void sendPositionStatus() {
        Intent intent = new Intent(MainActivity.BROADCAST_STATUS);
        intent.putExtra(MainActivity.MESSAGE_POS_STATUS, ++posCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        prefs.setPosCount(posCount); // ToDo -- how much does this cost?
    }

    private void sendPositionWaitStatus() {
        Intent intent = new Intent(MainActivity.BROADCAST_STATUS);
        intent.putExtra(MainActivity.MESSAGE_POS_WAIT_STATUS, 0);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendConnectionStatus() {
        Intent intent = new Intent(MainActivity.BROADCAST_STATUS);
        intent.putExtra(MainActivity.MESSAGE_CON_STATUS, 0);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private boolean isOnline() {
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
