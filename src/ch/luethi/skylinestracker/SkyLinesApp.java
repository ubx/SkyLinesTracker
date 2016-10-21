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

import android.app.Application;
import com.splunk.mint.Mint;

public class SkyLinesApp extends Application {

    public boolean guiActive;
    public double lastLat, lastLon;
    public PositionService positionService = null;
    public static FixQueueIF fixStack;

    @Override
    public void onCreate() {
        super.onCreate();
        Mint.setFlushOnlyOverWiFi(true);
        Mint.initAndStartSession(this, "a9b9af2d");
    }
}