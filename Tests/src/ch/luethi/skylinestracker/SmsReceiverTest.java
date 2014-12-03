package ch.luethi.skylinestracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)

public class SmsReceiverTest {

    SkyLinesPrefs pref;
    Context context;
    SmsReceiver smsReceiver;

    @Before
    public void setUp() {
        pref = new SkyLinesPrefs(Robolectric.application.getApplicationContext());
        context = Robolectric.application.getApplicationContext();
        smsReceiver = new SmsReceiver();
    }

    @Test
    public void testSendSmsConfigurationKey() {
        assertThat("Wrong initial Key after smsReceiver configuration not allowed", pref.isAutoStartTracking(), equalTo(false));
        pref.setTrackingKey("1234");
        pref.setSmsConfig(false);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=AB7411"));
        assertThat("Wrong Key after smsReceiver configuration not allowed", Long.toHexString(pref.getTrackingKey()).toUpperCase(), equalTo("1234"));
        pref.setSmsConfig(true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=AB7411"));
        assertThat("Wrong Key after smsReceiver setting", Long.toHexString(pref.getTrackingKey()).toUpperCase(), equalTo("AB7411"));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=F6518"));
        assertThat("Wrong Key after smsReceiver setting", Long.toHexString(pref.getTrackingKey()).toUpperCase(), equalTo("F6518"));
    }

    @Test
    public void testSendSmsConfigurationKeyNoHex() {
        pref.setSmsConfig(true);
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=ABD3"));
        assertThat("Wrong Key after smsReceiver setting", Long.toHexString(pref.getTrackingKey()).toUpperCase(), equalTo("ABD3"));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Key=123W"));
        assertThat("Wrong Key after smsReceiver setting", Long.toHexString(pref.getTrackingKey()).toUpperCase(), equalTo("0"));
    }

    @Test
    public void testSendSmsConfigurationAuto() {
        assertThat("Wrong initial Auto Start Tracking", pref.isAutoStartTracking(), equalTo(false));
        Intent intent = createSms("09000000000", "SLT Auto=On");
        pref.setSmsConfig(false);
        smsReceiver.onReceive(context, intent);
        assertThat("Wrong Auto Start Tracking after smsReceiver configuration not allowed", pref.isAutoStartTracking(), equalTo(false));
        pref.setSmsConfig(true);
        smsReceiver.onReceive(context, intent);
        assertThat("Wrong Auto Start Tracking after smsReceiver setting", pref.isAutoStartTracking(), equalTo(true));
        smsReceiver.onReceive(context, createSms("09000000000", "SLT Auto=Off"));
        assertThat("Wrong Auto Start Tracking after smsReceiver setting", pref.isAutoStartTracking(), equalTo(false));
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
