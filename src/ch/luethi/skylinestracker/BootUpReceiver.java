package ch.luethi.skylinestracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {

    private static final String TAG = "BOOT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        SkyLinesPrefs prefs = new SkyLinesPrefs(context);
        if (prefs.isAutostartTracking()) {
            Log.d(TAG, "onReceive isAutostartTracking 0");
            Intent positionService = new Intent(context, PositionService.class);
            context.startService(positionService);
            Log.d(TAG, "onReceive isAutostartTracking 1");
        }

    }
}