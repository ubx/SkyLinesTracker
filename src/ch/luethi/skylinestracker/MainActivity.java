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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.splunk.mint.Mint;
import java.text.DecimalFormat;

public class MainActivity extends Activity {

    public static final String BROADCAST_STATUS = "SKYLINESTRACKER_BROADCAST_STATUS";
    public static final String MESSAGE_STATUS_TYPE = "MESSAGE_STATUS_TYPE";
    public static final String ISTESTING = "ISTESTING";
    public static final int MESSAGE_POS_STATUS = 0;
    public static final int MESSAGE_POS_WAIT_STATUS = 1;
    public static final int MESSAGE_CON_STATUS = 2;

    private static final DecimalFormat dfLat = new DecimalFormat("##.00000");
    private static final DecimalFormat dfLon = new DecimalFormat("###.00000");


    private static Intent positionService;
    private TextView statusText;
    private CheckBox checkLiveTracking;
    private final IntentFilter brFilter = new IntentFilter(BROADCAST_STATUS);
    private String msgPosSent;
    private String msgNoInet;
    private String msgWaitGps;
    private SkyLinesApp app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().hasExtra(ISTESTING)) {
            Mint.setFlushOnlyOverWiFi(true);
            Mint.initAndStartSession(this, "a9b9af2d");
        }
        app = ((SkyLinesApp) getApplicationContext());
        positionService = new Intent(this, PositionService.class);

        SkyLinesPrefs prefs = new SkyLinesPrefs(this);
        prefs.setIpAddress(prefs.getIpAddress(getResources().getString(R.string.ip_address_dns)));

        setContentView(R.layout.activity_main);
        statusText = (TextView) findViewById(R.id.statusText);
        checkLiveTracking = (CheckBox) findViewById(R.id.checkLiveTracking);
        msgPosSent = " " + getResources().getString(R.string.msg_pos_sent);
        msgNoInet = getResources().getString(R.string.msg_no_inet);
        msgWaitGps = getResources().getString(R.string.resume);
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
        app.guiActive = true;
        super.onResume();
        if (isPositionServiceRunning()) {
            checkLiveTracking.setChecked(true);
            statusText.setText(R.string.resume);
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

    public void startStopTracking(View view) {
        CheckBox cb = (CheckBox) view;
        if (cb.isChecked()) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (!provider.contains("gps")) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
            LocalBroadcastManager.getInstance(this).registerReceiver(onStatusChange, brFilter);
            startService(positionService);
            statusText.setText(R.string.on);
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onStatusChange);
            stopService(positionService);
            statusText.setText(R.string.off);
        }
    }

    private final BroadcastReceiver onStatusChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int statusType = intent.getIntExtra(MESSAGE_STATUS_TYPE, 99);
            switch (statusType) {
                case MESSAGE_POS_STATUS:
                    statusText.setText(msgPosSent + " " + dfLon.format(app.lastLon) + " " + dfLat.format(app.lastLat));
                    break;
                case MESSAGE_CON_STATUS:
                    statusText.setText(msgNoInet);
                    break;
                case MESSAGE_POS_WAIT_STATUS:
                    statusText.setText(msgWaitGps);
                    break;
            }
        }
    };


    private boolean isPositionServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PositionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
