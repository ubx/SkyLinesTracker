package ch.luethi.skylinesclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
			
		case R.id.action_exit:
			finish();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void startStopTracking(View view) {
		CheckBox cb = (CheckBox) view;
		TextView st = (TextView) findViewById(R.id.statusText);
		if (cb.isChecked()) {
			st.setText(R.string.on);
		} else {
			st.setText(R.string.off);
		}
	}

}
