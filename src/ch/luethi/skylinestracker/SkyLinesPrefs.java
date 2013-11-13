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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigInteger;


class SkyLinesPrefs {

    private static final String TRACKING_KEY = "tracking_key";
    private static final String TRACKING_INTERVAL = "tracking_interval";
    private static final String AUTOSTART_TRACKING = "autostart_tracking";
    private static final String SMS_CONFIG = "sms_config";
    private final SharedPreferences prefs;

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
