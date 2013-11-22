package ch.luethi.skylinestracker;


import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class SkyLinesApp extends Application {

    public int posCount;

    @Override
    public boolean stopService(Intent name) {
        SkyLinesPrefs prefs = new SkyLinesPrefs(this);
        prefs.setPosCount(posCount);
        Log.d("XXXX", "SkyLinesApp, stopService, posCount=" + posCount);
        return super.stopService(name);
    }

    @Override
    public void onCreate() {
        SkyLinesPrefs prefs = new SkyLinesPrefs(this);
        posCount = prefs.getPosCount();
        Log.d("XXXX", "SkyLinesApp, onCreate, posCount=" + posCount);

    }

    @Override
    public void onTerminate() {
        SkyLinesPrefs prefs = new SkyLinesPrefs(this);
        prefs.setPosCount(posCount);
        Log.d("XXXX", "SkyLinesApp, onTerminate, posCount=" + posCount);
    }
}
