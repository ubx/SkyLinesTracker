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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SkyLinesPrefs prefs = new SkyLinesPrefs(context);
        if (prefs.isSmsConfig()) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    parsSms(context, prefs, msgs[i].getMessageBody());
                }
            }
        }
    }

    private void parsSms(Context context, SkyLinesPrefs prefs, String smsBody) {
        String params[] = smsBody.toUpperCase().split(" ");
        if (params.length > 1 && params[0].equals("SLT")) {
            for (int i = 1; i < params.length; i++) {
                String kv[] = params[i].split("=");
                if (kv.length == 2) {
                    if (kv[0].equals("KEY")) {
                        prefs.setTrackingKey(kv[1]);
                    } else if (kv[0].equals("INT")) {
                        prefs.setTrackingInterval(kv[1]);
                    } else if (kv[0].equals("AUTO")) {
                        prefs.setAutostartTracking(kv[1].equals("ON"));
                    } else if (kv[0].equals("SMS")) {
                        prefs.setSmsConfig(kv[1].equals("ON"));
                    } else if (kv[0].equals("LIVE")) {
                        Intent positionService = new Intent(context, PositionService.class);
                        if (kv[1].equals("ON")) {
                            context.startService(positionService);
                        } else if (kv[1].equals("OFF")) {
                            context.stopService(positionService);
                        }
                    }
                }
            }
        }
    }
}