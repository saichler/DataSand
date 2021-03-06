/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import java.util.LinkedList;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Queue<E> {
    private LinkedList<E> queues[] = new LinkedList[6];
    private int count = 0;

    public Queue() {
        for (int i = 0; i < queues.length; i++) {
            queues[i] = new LinkedList<E>();
        }
    }

    public synchronized void add(E data, int priority) {
        queues[priority].add(data);
        count++;
    }

    public int size() {
        int size = queues[0].size() + queues[1].size() + queues[2].size()
                + queues[3].size() + queues[4].size() + queues[5].size();
        return size;
    }

    public synchronized E next() {
        if (!queues[5].isEmpty()) {
            count--;
            return queues[5].removeFirst();
        } else if (!queues[4].isEmpty()) {
            count--;
            return queues[4].removeFirst();
        } else if (!queues[3].isEmpty()) {
            count--;
            return queues[3].removeFirst();
        } else if (!queues[2].isEmpty()) {
            count--;
            return queues[2].removeFirst();
        } else if (!queues[1].isEmpty()) {
            count--;
            return queues[1].removeFirst();
        } else if (!queues[0].isEmpty()) {
            count--;
            return queues[0].removeFirst();
        }
        return null;
    }
}
