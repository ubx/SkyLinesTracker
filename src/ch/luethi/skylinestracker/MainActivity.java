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
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;

public class MainActivity extends Activity {

    public static final String BROADCAST_STATUS = "SKYLINESTRACKER_BROADCAST_STATUS";
    public static final String MESSAGE_POS_STATUS = "MESSAGE_POS_STATUS";
    public static final String MESSAGE_POS_WAIT_STATUS = "MESSAGE_POS_WAIT_STATUS";
    public static final String MESSAGE_CON_STATUS = "MESSAGE_CON_STATUS";
    public static final String IPADDRESS = "ipaddress";

    private static Intent positionService;
    private TextView statusText;
    private CheckBox checkLiveTracking;
    private final IntentFilter brFilter = new IntentFilter(BROADCAST_STATUS);
    private String msgPosSent;
    private String msgNoInet;
    private String msgWaitGps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "a9b9af2d");
        positionService = new Intent(this, PositionService.class);
        if (getIntent().hasExtra(IPADDRESS)) {
            positionService.putExtras(getIntent().getExtras());
        }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStatusChange);
    }

    @Override
    protected void onResume() {
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
            if (intent.hasExtra(MESSAGE_POS_STATUS)) {
                int cnt = intent.getIntExtra(MESSAGE_POS_STATUS, 0);
                statusText.setText(cnt + msgPosSent);
            } else if (intent.hasExtra(MESSAGE_CON_STATUS)) {
                statusText.setText(msgNoInet);
            } else if (intent.hasExtra(MESSAGE_POS_WAIT_STATUS)) {
                statusText.setText(msgWaitGps);
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
