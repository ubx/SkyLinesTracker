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

public class MainActivity extends Activity {

    public static final String BROADCAST_STATUS = "SKYLINESTRACKER_BROADCAST_STATUS";
    public static final String MESSAGE_POS_STATUS = "MESSAGE_POS_STATUS";

    private static final String TAG = "MAIN";
    private static Intent positionService;
    private TextView statusText;
    private CheckBox checkLiveTracking;
    private IntentFilter brFilter = new IntentFilter(BROADCAST_STATUS);
    private String msgPosSent;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        positionService = new Intent(this, PositionService.class);
        setContentView(R.layout.activity_main);
        statusText = (TextView) findViewById(R.id.statusText);
        checkLiveTracking = (CheckBox) findViewById(R.id.checkLiveTracking);
        msgPosSent = " " + getResources().getString(R.string.msg_pos_sent);
        LocalBroadcastManager.getInstance(this).registerReceiver(onPositionStatusChange, brFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPositionStatusChange);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPositionServiceRunning()) {
            checkLiveTracking.setChecked(true);
            statusText.setText(R.string.resume);
            LocalBroadcastManager.getInstance(this).registerReceiver(onPositionStatusChange, brFilter);

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
            LocalBroadcastManager.getInstance(this).registerReceiver(onPositionStatusChange, brFilter);
            startService(positionService);
            statusText.setText(R.string.on);
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onPositionStatusChange);
            stopService(positionService);
            statusText.setText(R.string.off);
        }
    }

    private BroadcastReceiver onPositionStatusChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int cnt = intent.getIntExtra(MESSAGE_POS_STATUS, 0);
            statusText.setText(cnt + msgPosSent);
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
