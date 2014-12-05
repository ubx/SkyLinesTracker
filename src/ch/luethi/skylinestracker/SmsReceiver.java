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
        positionServiceRunning = null;
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
                        prefs.setAutoStartTracking(kv[1].equals("ON"));
                    } else if (kv[0].equals("SMS")) {
                        prefs.setSmsConfig(kv[1].equals("ON"));
                    } else if (kv[0].equals("LIVE")) {
                        Intent positionService = new Intent(context, PositionService.class);
                        if (kv[1].equals("ON")) {
                            context.startService(positionService);
                            positionServiceRunning = true;
                        } else if (kv[1].equals("OFF")) {
                            context.stopService(positionService);
                            positionServiceRunning = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Todo -- this code is for unit tests only, fins a better solution!
     */
    private Boolean positionServiceRunning = null;

    public Boolean getPositionServiceRunning() {
        return positionServiceRunning;
    }
}