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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=21)

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
        ShadowLocationManager shadowLocationManager = Shadows.shadowOf(locationManager);
        Location expectedLocation = location(LocationManager.GPS_PROVIDER, 12.0, 20.0);

        shadowLocationManager.simulateLocation(expectedLocation);
        // -- todo
        assertEquals(expectedLocation.getLatitude(), skyLinesApp.lastLat);
        assertEquals(expectedLocation.getLongitude(), skyLinesApp.lastLon);

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