package ch.luethi.skylinesclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 25.10.13
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class SkyLinesPrefs {

    SharedPreferences prefs;

    public SkyLinesPrefs(Context c) {

        prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public int getTrackingInterval() {
        String val = prefs.getString("tracking_interval", "5");
        return Integer.parseInt(val);
    }

    public long getTrackingKey() {
        String val = prefs.getString("tracking_key", "0");
        return new BigInteger(val, 16).longValue();
    }


}
