/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.datasand.codec.VLogger;
import org.datasand.codec.util.ThreadNode;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class RepetitiveTaskContainer extends ThreadNode{

    private final List<RepetitiveTask> tasks = new ArrayList<>();

    public RepetitiveTaskContainer(ServicesHabitat servicesHabitat){
        super(servicesHabitat,servicesHabitat.getName()+" Pulse");
    }

    @Override
    public void initialize() {

    }

    @Override
    public void execute() throws Exception {
        synchronized(this) {
            for (RepetitiveTask task : this.tasks) {
                task.executeIfTime();
            }
        }
        Thread.sleep(1000);
    }

    @Override
    public void distruct() {

    }

    public synchronized void registerRepetitiveTask(RepetitiveTask task){
        for(Iterator<RepetitiveTask> iter=tasks.iterator();iter.hasNext();){
            if(iter.next()==task){
                return;
            }
        }
        this.tasks.add(task);
    }

    public synchronized void unregisterRepetitiveTask(RepetitiveTask task){
        for(Iterator<RepetitiveTask> iter=tasks.iterator();iter.hasNext();){
            if(iter.next()==task){
                iter.remove();
                break;
            }
        }
    }

    public interface RepetitveTask{
        public void runTask(Object taskData);
    }

    public abstract static class RepetitiveTask {
        private final String name;
        private final long interval;
        private final long intervalStart;
        private final int priority;
        private long lastExecuted = System.currentTimeMillis();
        private boolean started = false;

        public RepetitiveTask(String name,long _interval,long _intervalStart, int _priority) {
            this.name = name;
            this.interval = _interval;
            this.intervalStart = _intervalStart;
            this.priority = _priority;
        }

        private void executeIfTime() {
            if (System.currentTimeMillis() - lastExecuted > interval){
                lastExecuted = System.currentTimeMillis();
                try{
                    execute();
                }catch(Exception e){
                    VLogger.error("Failed to execute repetitive task "+name,e);
                }
            }else
            if (!started && (System.currentTimeMillis() - lastExecuted > intervalStart || intervalStart == 0)) {
                started = true;
                lastExecuted = System.currentTimeMillis();
                try{
                    execute();
                }catch(Exception e){
                    VLogger.error("Failed to execute repetitive task "+name,e);
                }
            }
        }

        public abstract void execute();
    }

}
