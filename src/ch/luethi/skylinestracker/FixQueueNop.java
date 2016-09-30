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

public class FixQueueNop  implements FixQueueIF {

    public FixQueueNop(Context ctx) {
    }

    @Override
    public  void  push(byte[] date) {
    }

    @Override
    public byte[] pop() {
        return null;
    }

    @Override
    public byte[][] pop(int cnt) {
        return new byte[0][];
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public FixQueueIF load() {
        return this;
    }

    @Override
    public long getCapacity() {
        return 0;
    }
}
