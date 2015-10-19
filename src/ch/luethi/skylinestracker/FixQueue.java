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

import java.io.*;
import java.util.Stack;

public class FixQueue<E> extends Stack<E> {

    private static transient Context ctx;

    public FixQueue(Context ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public synchronized E push(E object) {
        E e = super.push(object);
        store();
        return e;
    }

    @Override
    public synchronized E pop() {
        E e = super.pop();
        if (isEmpty()) {
            store();
        }
        return e;
    }

    @Override
    public synchronized void removeElementAt(int location) {
        super.removeElementAt(location);
        store();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    private final void store() {
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(ctx.openFileOutput("FixQueue.data", Context.MODE_PRIVATE)));
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public final FixQueue<E> load() {
        ObjectInputStream in = null;
        FixQueue<E> fq = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(ctx.openFileInput(("FixQueue.data"))));
            try {
                fq = (FixQueue<E>) in.readObject();
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
            }
        } catch (IOException e) {
            // e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fq == null ? this : fq;
    }

}
