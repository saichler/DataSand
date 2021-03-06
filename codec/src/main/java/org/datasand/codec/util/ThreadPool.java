/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.util;

import java.util.LinkedList;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ThreadPool {
    private final LinkedList<Runnable> tasks = new LinkedList<Runnable>();
    private int threadCount = 0;
    private int maxThreadCount = 10;
    private String threadPoolName = "Simple Thread Pool";
    private int waitTimeForIdle = 10000;
    private int maxQueueSize = -1;
    public final Object waitForSlotSync = new Object();
    private final Object waitForEmptySync = new Object();

    public ThreadPool(int _maxThreadCount, String name,int _waitTimeForIdle) {
        this.maxThreadCount = _maxThreadCount;
        this.threadPoolName = name;
        this.waitTimeForIdle = _waitTimeForIdle;
    }

    public void addTask(Runnable r) {
        synchronized (tasks) {
            tasks.add(r);
            tasks.notifyAll();
            if (threadCount < maxThreadCount) {
                threadCount++;
                new WorkerThread(threadCount).start();
            }
        }
    }

    private class WorkerThread extends Thread {

        private long lastTimeExecuted = System.currentTimeMillis();

        public WorkerThread(int threadNumber) {
            super(
                "Thread #" + threadNumber + " Of Threadpool " + threadPoolName);
        }

        public void run() {
            Runnable runthis = null;
            while (true) {
                runthis = null;
                if (maxQueueSize != -1) {
                    synchronized (waitForSlotSync) {
                        if (tasks.size() < maxQueueSize) {
                            waitForSlotSync.notifyAll();
                        }
                    }
                }
                synchronized (tasks) {
                    if (tasks.isEmpty()) {
                        try {
                            tasks.wait(2000);
                        } catch (Exception err) {
                        }
                    }

                    if (!tasks.isEmpty()) {
                        runthis = tasks.removeFirst();
                    }
                }
                if (runthis != null) {
                    try {
                        runthis.run();
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                    lastTimeExecuted = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - lastTimeExecuted
                    > waitTimeForIdle) {
                    break;
                }
            }
            synchronized (tasks) {
                threadCount--;
            }
            if(threadCount==0){
                synchronized (waitForEmptySync){
                    waitForEmptySync.notifyAll();
                }
            }
        }
    }

    public int getNumberOfThreads() {
        return threadCount;
    }

    public void waitForSlot() {
        if (tasks.size() > maxQueueSize) {
            synchronized (waitForSlotSync) {
                try {
                    waitForSlotSync.wait();
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    public boolean isEmpty() {
        if (this.threadCount == 0) {
            return true;
        }
        return false;
    }

    public void waitForEmpty(){
        synchronized(this.waitForEmptySync){
            if(this.threadCount>0) {
                try {
                    this.waitForEmptySync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMaxQueueSize(int size) {
        this.maxQueueSize = size;
    }

}

