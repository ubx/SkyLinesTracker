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

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.widget.CompoundButton;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Robolectric.buildActivity;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)

public class SmsReceiverTest {

    private MainActivity mainActivity;
    private SkyLinesPrefs pref;
    private Context context;
    private SmsReceiver smsReceiver;
    private ActivityManager activityManager;

    @Before
    public void setUp() {
        activityManager = (ActivityManager) RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
        Intent intent = new Intent(application, MainActivity.class);
        intent.putExtra(MainActivity.ISTESTING, true);
        mainActivity = buildActivity(MainActivity.class).withIntent(intent).create().get();
        pref = new SkyLinesPrefs(RuntimeEnvironment.application.getApplicationContext());
        context = RuntimeEnvironment.application.getApplicationContext();
        smsReceiver = new SmsReceiver();
    }

    @Test
    public void testSendSmsConfigurationKey() {
        setPreferences(123, 2, false, false);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=AB7411"));
        assertTrue("Config changed although smsReceiver configuration not allowed", comparePreferences(123, 2, false, false));
        setPreferences(3456, 2, false, true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=AB7411"));
        assertTrue("Wrong Key after smsReceiver setting", comparePreferences(0xAB7411, 2, false, true));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=F6518"));
        assertTrue("Wrong Key after smsReceiver setting", comparePreferences(0xF6518, 2, false, true));
    }

    @Test
    public void testSendSmsConfigurationKeyNoHex() {
        setPreferences(234375, 2, false, true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=123W"));
        assertTrue("Wrong Key after no hexadecimal Kex  smsReceiver setting", comparePreferences(0, 2, false, true));
    }

    @Test
    public void testSendSmsConfigurationAuto() {
        setPreferences(54326, 2, true, true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT sms=Off"));
        assertTrue("Wrong Sms state after smsReceiver setting", comparePreferences(54326, 2, true, false));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Auto=On"));
        assertTrue("Wrong Sms state after smsReceiver setting", comparePreferences(54326, 2, true, false));
    }


    @Test
    public void testSendSmsConfigurationLiveTracking() {
        CompoundButton cb = (CompoundButton) mainActivity.findViewById(R.id.checkLiveTracking);
        setPreferences(12345, 2, true, true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Live=On"));
        assertThat("Live checkbox should be checked", smsReceiver.getPositionServiceRunning(), equalTo(true));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Live=Off"));
        assertThat("Live checkbox should be unchecked", smsReceiver.getPositionServiceRunning(), equalTo(false));
    }

    private void setPreferences(long key, int interval, boolean autoStart, boolean smsConfig) {
        pref.setTrackingKey(Long.toHexString(key));
        pref.setTrackingInterval(String.valueOf(interval));
        pref.setAutoStartTracking(autoStart);
        pref.setSmsConfig(smsConfig);
    }

    private boolean comparePreferences(long key, int interval, boolean autoStart, boolean smsConfig) {
        return (pref.getTrackingKey() == key & pref.getTrackingInterval() == interval & pref.isAutoStartTracking() == autoStart & pref.isSmsConfig() == smsConfig);
    }

    private static Intent createSms(String sender, String body) {
        byte[] pdu = null;
        byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
        byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte[] dateBytes = new byte[7];
        Calendar calendar = new GregorianCalendar();
        dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
        dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
        dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
        dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
        dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);
            bo.write(scBytes);
            bo.write(0x04);
            bo.write((byte) sender.length());
            bo.write(senderBytes);
            bo.write(0x00);
            try {
                byte[] bodyBytes = GsmAlphabet.stringToGsm7BitPacked(body);
                bo.write(0x00);
                bo.write(dateBytes);
                bo.write(bodyBytes);
            } catch (EncodeException e) {
                System.out.println("try UCS2");
                try {
                    byte[] textPart = body.getBytes("utf-16be");
                    bo.write(0x0b);
                    bo.write(dateBytes);
                    bo.write(textPart.length);
                    bo.write(textPart);
                } catch (UnsupportedEncodingException e1) {
                }
            }
            pdu = bo.toByteArray();
        } catch (IOException e) {
        }

        Intent intent = new Intent();
        intent.setClassName("com.android.mms", "com.android.mms.transaction.SmsReceiverService");
        intent.setAction("android.provider.Telephony.SMS_RECEIVED");
        intent.putExtra("pdus", new Object[]{pdu});
        intent.putExtra("format", "3gpp");
        return intent;
    }

    private static byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }
}
