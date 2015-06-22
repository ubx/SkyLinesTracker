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

import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

public class ConnectionUpdateReceiver extends BroadcastReceiver {

    private PositionService posServer;
    private boolean init = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        SkyLinesApp app = ((SkyLinesApp) context.getApplicationContext());
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        app.online = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.i("SkyLines", "onReceive");

        if (!init) {
            Intent mIntent = new Intent(context, PositionService.class);
            try {
                context.bindService(mIntent, mConnection, context.BIND_AUTO_CREATE );
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (posServer != null) {
            posServer.broadcastReceiver();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            posServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PositionService.LocalBinder mLocalBinder = (PositionService.LocalBinder)service;
            posServer = mLocalBinder.getServerInstance();
        }
    };

}


