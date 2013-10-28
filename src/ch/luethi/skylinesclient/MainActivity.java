package ch.luethi.skylinesclient;

import android.app.Activity;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        positionService = new Intent(this, PositionService.class);
        setContentView(R.layout.activity_main);
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
        TextView st = (TextView) findViewById(R.id.statusText);
        if (cb.isChecked()) {
            Log.d(TAG, "ON");
            startService(positionService);
            st.setText(R.string.on);
        } else {
            Log.d(TAG, "OFF");
            stopService(positionService);
            st.setText(R.string.off);
        }
    }

}
