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
import android.net.NetworkInfo;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import com.geeksville.location.SkyLinesTrackingWriter;

import java.net.SocketException;
import java.net.UnknownHostException;


public class PositionService extends Service implements LocationListener {

    private SkyLinesTrackingWriter skyLinesTrackingWriter = null;
    private LocationManager locationManager;
    private SkyLinesPrefs prefs;
    private HandlerThread senderThread;
    private String ipAddress;

    private static SkyLinesApp app;
    private static Intent intentPosStatus, intentWaitStatus, intentConStatus;


    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        prefs = new SkyLinesPrefs(this);
        app = ((SkyLinesApp) getApplicationContext());
        intentPosStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentPosStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_POS_STATUS);
        intentWaitStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentWaitStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_POS_WAIT_STATUS);
        intentConStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentConStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_CON_STATUS);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        skyLinesTrackingWriter = null;
        initConnectionStatus();
        ipAddress = prefs.getIpAddress();
        senderThread = new HandlerThread("SenderThread");
        senderThread.start();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.getTrackingInterval() * 1000, 0, this, senderThread.getLooper());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        skyLinesTrackingWriter = null;
        Looper looper = senderThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() != 0.0) {
            if (isOnline()) {
                app.lastLat = location.getLatitude();
                app.lastLon = location.getLongitude();
                // convert m/sec to km/hr
                float kmPerHr = location.hasSpeed() ? location.getSpeed() * 3.6F : Float.NaN;
                float[] accelVals = null;
                float vspd = Float.NaN;
                getOrCreateSkyLinesTrackingWriter().emitPosition(location.getTime(), app.lastLat, app.lastLon,
                        location.hasAltitude() ? (float) location.getAltitude() : Float.NaN,
                        (int) location.getBearing(), kmPerHr, accelVals, vspd);
                if (app.guiActive) {
                    sendPositionStatus();
                }
            } else {
                if (app.guiActive)
                    sendConnectionStatus();
            }
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
        if (app.guiActive)
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
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPosStatus);
    }

    private void sendPositionWaitStatus() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentWaitStatus);
    }

    private void sendConnectionStatus() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentConStatus);
    }


    private boolean isOnline() {
        return app.online;
    }

    private void initConnectionStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        app.online = activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
