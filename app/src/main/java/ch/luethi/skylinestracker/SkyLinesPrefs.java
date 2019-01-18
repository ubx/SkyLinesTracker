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

    public static final String TRACKING_KEY = "tracking_key";
    public static final String TRACKING_INTERVAL = "tracking_interval";
    public static final String IP_ADDRESS = "ip_address";

    private static final String AUTOSTART_TRACKING = "autostart_tracking";
    private static final String QUEUE_FIXES = "queue_fixes";
    private static final String QUEUE_FIXES_MAX_SECONDS = "queue_fixes_max_seconds";

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
        long ret = 0;
        try {
            ret = new BigInteger(val, 16).longValue();
        } catch (NumberFormatException e) {
        }
        return ret;
    }

    public void setTrackingKey(String key) {
        prefs.edit().putString(TRACKING_KEY, key).commit();
    }

    public boolean isAutoStartTracking() {
        return prefs.getBoolean(AUTOSTART_TRACKING, false);
    }

    public void setAutoStartTracking(boolean val) {
        prefs.edit().putBoolean(AUTOSTART_TRACKING, val).commit();
    }


    public String getIpAddress() {
        return prefs.getString(IP_ADDRESS, "");
    }

    public String getIpAddress(String def) {
        return prefs.getString(IP_ADDRESS, def);
    }

    public void  setIpAddress(String ipAddress) {
        prefs.edit().putString(IP_ADDRESS, ipAddress).commit();
    }

    public boolean isQueueFixes() {
        return prefs.getBoolean(QUEUE_FIXES, true);
    }

    public void setQueueFixes(boolean val) {
        prefs.edit().putBoolean(QUEUE_FIXES, val).commit();
    }

    public int getQueueFixesMaxSeconds() {
        return prefs.getInt(QUEUE_FIXES_MAX_SECONDS, 1800);
    }

    public void setQueueFixesMaxSeconds(int val) {
        prefs.edit().putInt(QUEUE_FIXES_MAX_SECONDS, val).commit();
    }
}
