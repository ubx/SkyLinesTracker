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
import org.pcollections.*;


public class FixQueue2<E> implements FixQueueIF<E> {

    private static transient Context ctx;
    private PStack<E> stack;


    public FixQueue2(Context ctx) {
        super();
        this.ctx = ctx;
        stack = ConsPStack.empty();;
    }
    @Override
    public E push(E object) {
        stack = stack.plus(object);
        return object;
    }

    @Override
    public E pop() {
        E e = stack.get(0);
        stack = stack.minus(0);
        return e;
    }

    @Override
    public void removeElementAt(int location) {
        stack = stack.minus(location);
    }

    @Override
    public int size() {
        return  stack.size();
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public FixQueueIF<E> load() {
        return this;
    }
}
