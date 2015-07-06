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
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ConnectionUpdateReceiver extends BroadcastReceiver {

    private PositionService posServer;

    @Override
    public void onReceive(Context context, Intent intent) {
        SkyLinesApp app = ((SkyLinesApp) context.getApplicationContext());
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        app.online = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.i("SkyLines", "onReceive " + intent.getAction());
        Toast.makeText(context, "onReceive " + intent.getAction(), Toast.LENGTH_LONG).show();

        if (app.positionService != null)     {
            app.positionService.broadcastReceiver();
            Log.i("SkyLines", "onReceive -> broadcastReceiver");
            Toast.makeText(context, "onReceive -> broadcastReceiver", Toast.LENGTH_LONG).show();
        }

    }

}


