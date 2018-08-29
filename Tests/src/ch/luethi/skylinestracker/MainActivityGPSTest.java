package ch.luethi.skylinestracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.RuntimeEnvironment.application;

@Config(constants = BuildConfig.class, sdk = 21)
@RunWith(RobolectricTestRunner.class)

public class MainActivityGPSTest {

    MainActivity mainActivity;
    SkyLinesApp skyLinesApp;

    @Before
    public void setUp() {
        Intent intent = new Intent(application, MainActivity.class);
        intent.putExtra(MainActivity.ISTESTING, true);
        mainActivity = buildActivity(MainActivity.class).withIntent(intent).create().get();
        skyLinesApp = new SkyLinesApp();
    }

    @Ignore
    @Test
    public void shouldReturnTheLatestLocation() {
        LocationManager locationManager = (LocationManager)
                RuntimeEnvironment.application.getSystemService(Context.LOCATION_SERVICE);

        /* todo -- fix:
        Error:(42, 62) java: cannot access android.net.http.AndroidHttpClient
            class file for android.net.http.AndroidHttpClient not found


        ShadowLocationManager shadowLocationManager = Shadows.shadowOf(locationManager);
        Location expectedLocation = location(LocationManager.GPS_PROVIDER, 12.0, 20.0);

        shadowLocationManager.simulateLocation(expectedLocation);
        assertEquals(expectedLocation.getLatitude(), skyLinesApp.lastLat, 0);
        assertEquals(expectedLocation.getLongitude(), skyLinesApp.lastLon, 0);
        */

        //assertEquals(expectedLocation, actualLocation);
    }

    private Location location(String provider, double latitude, double longitude) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(System.currentTimeMillis());
        return location;
    }
}