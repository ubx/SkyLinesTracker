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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.geeksville.location.SkyLinesTrackingWriter;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

import static androidx.core.app.NotificationCompat.Builder;


public class PositionService extends Service implements LocationListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private SkyLinesTrackingWriter skyLinesTrackingWriter = null;
    private LocationManager locationManager;
    private SkyLinesPrefs prefs;
    private HandlerThread senderThread;
    private String ipAddress;

    private static SkyLinesApp app;
    private static Intent intentPosStatus, intentWaitStatus, intentConStatus;

    private Handler delayHandler = new Handler();
    private Runnable timerRunnable;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private int currentTrackingInterval = 1000;
    private NetworkStateReceiver networkStateReceiver;
    private static final int ONGOING_NOTIFICATION_ID = PositionService.class.hashCode();


    public PositionService() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("SkyLines", "timerRunnable, isOnline()=" + isOnline());
                if (isOnline()) {
                    new DequeueTask().execute();
                    sendPositionWaitStatus();
                }
                startTimer();
            }
        };


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences xprefs, String key) {
                if (key.equals(SkyLinesPrefs.TRACKING_INTERVAL)) {
                    if (SkyLinesApp.fixStack != null) {
                        SkyLinesApp.fixStack.setCapacity(calculateFixQueueSize());
                    }
                    startLocationUpdates();
                } else if (key.equals(SkyLinesPrefs.TRACKING_KEY)) {
                    if (skyLinesTrackingWriter != null) {
                        skyLinesTrackingWriter.setKey(prefs.getTrackingKey());
                    }
                }
            }
        };
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(ONGOING_NOTIFICATION_ID, createNotification());
        } else {
            startForeground(ONGOING_NOTIFICATION_ID, new Notification());
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        prefs = new SkyLinesPrefs(this);
        app = ((SkyLinesApp) getApplicationContext());
        app.positionService = this;
        intentPosStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentPosStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_POS_STATUS);
        intentWaitStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentWaitStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_POS_WAIT_STATUS);
        intentConStatus = new Intent(MainActivity.BROADCAST_STATUS);
        intentConStatus.putExtra(MainActivity.MESSAGE_STATUS_TYPE, MainActivity.MESSAGE_CON_STATUS);

        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(this);
        sprefs.registerOnSharedPreferenceChangeListener(listener);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }



    @TargetApi(26)
    private Notification createNotification() {
        final NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setDescription(getString(R.string.app_name));

        final NotificationManager manager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(notificationChannel);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Builder notificationBuilder = new Builder(this, getString(R.string.app_name));
        return notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(getString(R.string.run_in_background))
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setUsesChronometer(true)
                .setContentIntent(contentIntent)
                .build();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean init = intent != null && intent.getBooleanExtra("init", false);
        if (prefs.isQueueFixes()) {
            SkyLinesApp.fixStack = new FixQueue(getApplicationContext(), calculateFixQueueSize(), init);
        } else {
            SkyLinesApp.fixStack = new FixQueueNop(getApplicationContext());
        }
        Log.d("SkyLines", "SkyLinesApp, onStartCommand(), fixStack.size()=" + SkyLinesApp.fixStack.size() + ", init=" + init + ", prefs.getQueueFixesMax()=" + prefs.getQueueFixesMaxSeconds());

        skyLinesTrackingWriter = null;
        ipAddress = prefs.getIpAddress();
        startLocationUpdates();
        return START_STICKY;
    }

    private int calculateFixQueueSize() {
        currentTrackingInterval = Math.min(currentTrackingInterval, prefs.getTrackingInterval()); // never increase tracking interval !
        return prefs.getQueueFixesMaxSeconds() / currentTrackingInterval;
    }

    private void startLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (senderThread != null) {
            senderThread.quit();
        }
        senderThread = new HandlerThread("SenderThread");
        senderThread.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permissions are handled in the MainActivity class and should be granted here !.
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, prefs.getTrackingInterval() * 1000, 0, this, senderThread.getLooper());
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(networkStateReceiver);
        locationManager.removeUpdates(this);
        skyLinesTrackingWriter = null;
        app.positionService = null;
        Looper looper = senderThread == null ? null : senderThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        stopTimer();
        Log.d("SkyLines", "PositionService, onDestroy()");
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        stopTimer();
        skyLinesTrackingWriter = getOrCreateSkyLinesTrackingWriter();
        if (skyLinesTrackingWriter != null) { // fix NPE #11
            if (location.getLatitude() != 0.0) {
                app.lastLat = location.getLatitude();
                app.lastLon = location.getLongitude();
                // convert m/sec to km/hr
                float kmPerHr = location.hasSpeed() ? location.getSpeed() * 3.6F : Float.NaN;
                float[] accelVals = null;
                float vspd = Float.NaN;
                skyLinesTrackingWriter.emitPosition(location.getTime(), app.lastLat, app.lastLon,
                        location.hasAltitude() ? (float) location.getAltitude() : Float.NaN,
                        (int) location.getBearing(), kmPerHr, accelVals, vspd);
                if (app.guiActive) {
                    if (isOnline()) {
                        sendPositionStatus();
                    } else {
                        sendConnectionStatus();
                    }
                }
            } else {
                if (isOnline())
                    skyLinesTrackingWriter.dequeAndSendFix();
            }
        } else {
            if (app.guiActive) {
                sendConnectionStatus();
            }
        }

        Log.d("SkyLines", "onLocationChanged, isOnline()=" + isOnline());
        startTimer();
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

    public static boolean isOnline() {
        NetworkInfo networkInfo = ((ConnectivityManager) Objects.requireNonNull(app.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private SkyLinesTrackingWriter getOrCreateSkyLinesTrackingWriter() {
        if (skyLinesTrackingWriter == null) {
            try {
                skyLinesTrackingWriter = new SkyLinesTrackingWriter(prefs.getTrackingKey(), ipAddress);
            } catch (SocketException | UnknownHostException e) {
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

    private void startTimer() {
        delayHandler.postDelayed(timerRunnable, 2 * (prefs.getTrackingInterval() * 1000));
    }

    private void stopTimer() {
        delayHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void networkAvailable() {
        new DequeueTask().execute();
        sendPositionWaitStatus();
    }

    @Override
    public void networkUnavailable() {
    }

    private class DequeueTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            if (skyLinesTrackingWriter != null) {
                skyLinesTrackingWriter.dequeAndSendFix();
            }
            return null;
        }
    }

}
