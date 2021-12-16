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
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.splunk.mint.Mint;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private static final String ISTESTING = "ISTESTING";

    static final String BROADCAST_STATUS = "SKYLINESTRACKER_BROADCAST_STATUS";
    static final String MESSAGE_STATUS_TYPE = "MESSAGE_STATUS_TYPE";
    private static final String TESTING_IP = "TESTING_IP";
    static final int MESSAGE_POS_STATUS = 0;
    static final int MESSAGE_POS_WAIT_STATUS = 1;
    static final int MESSAGE_CON_STATUS = 2;

    private static final DecimalFormat dfLat = new DecimalFormat("##.00000");
    private static final DecimalFormat dfLon = new DecimalFormat("###.00000");

    private static Intent positionService;
    private TextView statusText;
    private TextView positionText;
    private TextView queueValueText;
    private CompoundButton checkLiveTracking;
    private final IntentFilter brFilter = new IntentFilter(BROADCAST_STATUS);
    private String msgPosSent;
    private String msgNoInet;
    private String msgWaitGps;
    private SkyLinesApp app;
    private SkyLinesPrefs prefs;
    private boolean doFixQueueing;
    private BroadcastReceiver onStatusChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAllPermissions();
        prefs = new SkyLinesPrefs(this);
        app = ((SkyLinesApp) getApplicationContext());
        Log.d("SkyLines", "MainActivity, ISTESTING=" + getIntent().hasExtra(ISTESTING));
        if (getIntent().hasExtra(ISTESTING)) {
            Mint.closeSession(this);
        }
        doFixQueueing = prefs.isQueueFixes();
        positionService = new Intent(this, PositionService.class);
        positionService.putExtra("init", true);

        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        positionText = findViewById(R.id.positionValueText);
        queueValueText = findViewById(R.id.queueValueText);
        TextView queueLabel = findViewById(R.id.queueLabel);
        queueLabel.setVisibility(doFixQueueing ? View.VISIBLE : View.GONE);

        checkLiveTracking = findViewById(R.id.checkLiveTracking);
        checkLiveTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                onOff(on);
            }
        });
        msgPosSent = " " + getResources().getString(R.string.msg_pos_sent);
        msgNoInet = getResources().getString(R.string.msg_no_inet);
        msgWaitGps = getResources().getString(R.string.resume);

        onStatusChange = new myBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(onStatusChange, brFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.guiActive = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStatusChange);
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.guiActive = true;
        if (isPositionServiceRunning()) {
            checkLiveTracking.setChecked(true);
            statusText.setText(R.string.resume);
            positionText.setText("");
            LocalBroadcastManager.getInstance(this).registerReceiver(onStatusChange, brFilter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                return false;

            case R.id.action_about:
                Intent aboutActivity = new Intent(this, AboutActivity.class);
                startActivity(aboutActivity);
                return false;

            case R.id.action_exit:
                stopService(positionService);
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("deprecation")
    private Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return provider.contains("gps");
        }
    }

    private void onOff(boolean on) {
        if (on) {
            if (!isLocationEnabled(this)) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
            LocalBroadcastManager.getInstance(this).registerReceiver(onStatusChange, brFilter);
            if (!isPositionServiceRunning()) {
                positionService.putExtra("init", true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(positionService);
                } else {
                    startService(positionService);
                }
            }
            statusText.setText(R.string.on);
            positionText.setText("");
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onStatusChange);
            stopService(positionService);
            statusText.setText(R.string.off);
            positionText.setText("");
            queueValueText.setText("");
        }
    }


    private class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int statusType = intent.getIntExtra(MESSAGE_STATUS_TYPE, 99);
            switch (statusType) {
                case MESSAGE_POS_STATUS:
                    statusText.setText(msgPosSent);
                    positionText.setText(String.format("%s - %s° %s°", getCurrentTimeStamp(), dfLon.format(app.lastLon), dfLat.format(app.lastLat)));
                    break;
                case MESSAGE_CON_STATUS:
                    statusText.setText(msgNoInet);
                    positionText.setText(String.format("%s - %s° %s°", getCurrentTimeStamp(), dfLon.format(app.lastLon), dfLat.format(app.lastLat)));
                    break;
                case MESSAGE_POS_WAIT_STATUS:
                    statusText.setText(msgWaitGps);
                    //positionText.setText("");
                    break;
            }
            if (doFixQueueing) {
                int trackingInterval = prefs.getTrackingInterval();
                //queueValueText.setText(String.format("%d / %d sec", app.fixStack.size() * trackingInterval, app.fixStack.getCapacity() * trackingInterval));
                queueValueText.setText(String.format("%d sec", SkyLinesApp.fixStack.size() * trackingInterval));
            }
        }
    }


    private boolean isPositionServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PositionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // permission related code below was copied from https://github.com/XCSoar/XCSoar/blob/master/android/src/XCSoar.java
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasAllPermissions() {
        for (String p : NEEDED_PERMISSIONS) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void requestAllPermissions() {
        if (android.os.Build.VERSION.SDK_INT <  Build.VERSION_CODES.M) {
            /* we don't need to request permissions on this old Android version */
            return;
        }

        /* starting with Android 6.0, we need to explicitly request all permissions before using them; mentioning them in the manifest
        is not enough
        */
        if (!hasAllPermissions()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.needs_to_collect_location_data)
                    .setPositiveButton(R.string.continue_dialog, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                MainActivity.this.requestPermissions(NEEDED_PERMISSIONS, 0);
                            } catch (IllegalArgumentException e) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Log.e(TAG, "could not request permissions: " + String.join(", ", NEEDED_PERMISSIONS), e);
                                }
                            }
                        }
                    }).show();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    private String getCurrentTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

}
