package ch.luethi.skylinesclient;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends Activity {

    private static final String TAG = "Activity";
    private static Intent positionService;
    private TextView statusText;
    private CheckBox checkLiveTracking;
    private SkyLinesPrefs prefs;
    private int oldTrackingInterval;
    private long oldTrackingKey;


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
        prefs = new SkyLinesPrefs(this);
        oldTrackingInterval = prefs.getTrackingInterval();
        oldTrackingKey = prefs.getTrackingKey();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPositionServiceRunning()) {
            if (oldTrackingInterval != prefs.getTrackingInterval() || oldTrackingKey != prefs.getTrackingKey()) {
                oldTrackingInterval = prefs.getTrackingInterval();
                oldTrackingKey = prefs.getTrackingKey();
                stopService(positionService);
                startService(positionService);
            }
            checkLiveTracking.setChecked(true);
            statusText.setText(R.string.resume);
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
                oldTrackingInterval = prefs.getTrackingInterval();
                oldTrackingKey = prefs.getTrackingKey();
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
            startService(positionService);
            statusText.setText(R.string.on);
        } else {
            stopService(positionService);
            statusText.setText(R.string.off);
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

}
