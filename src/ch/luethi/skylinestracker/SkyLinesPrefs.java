package ch.luethi.skylinestracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;


public class SkyLinesPrefs {

    public static final String TRACKING_KEY = "tracking_key";
    public static final String TRACKING_INTERVAL = "tracking_interval";
    public static final String AUTOSTART_TRACKING = "autostart_tracking";
    public static final String SMS_CONFIG = "sms_config";
    SharedPreferences prefs;

    public SkyLinesPrefs(Context c) {

        prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public int getTrackingInterval() {
        String val = prefs.getString(TRACKING_INTERVAL, "5");
        return Integer.parseInt(val);
    }

    public void setTrackingInterval(String interval) {
        prefs.edit().putString(TRACKING_INTERVAL, interval).commit();
    }

    public long getTrackingKey() {
        String val = prefs.getString(TRACKING_KEY, "0");
        return new BigInteger(val, 16).longValue();
    }

    public void setTrackingKey(String key) {
        prefs.edit().putString(TRACKING_KEY, key).commit();
    }

    public boolean isAutostartTracking() {
        return prefs.getBoolean(AUTOSTART_TRACKING, false);
    }

    public void setAutostartTracking(boolean val) {
        prefs.edit().putBoolean(AUTOSTART_TRACKING, val).commit();
    }

    public boolean isSmsConfig() {
        return prefs.getBoolean(SMS_CONFIG, false);
    }

    public void setSmsConfig(boolean val) {
        prefs.edit().putBoolean(SMS_CONFIG, val).commit();
    }

}
