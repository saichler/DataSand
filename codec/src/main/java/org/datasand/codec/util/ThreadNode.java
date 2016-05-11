/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.util;

import java.util.ArrayList;
import java.util.List;
import org.datasand.codec.VLogger;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class ThreadNode extends Thread{

    private boolean running = false;
    private final List<ThreadNode> children = new ArrayList<>();

    public ThreadNode(ThreadNode parent,String name){
        this.setName(name);
        if(parent!=null){
            parent.children.add(this);
            this.setDaemon(true);
        }
    }

    public void run(){
        boolean allChildrenStarted=false;
        while(!allChildrenStarted){
            try{
                Thread.sleep(200);
            }catch (Exception e){
                VLogger.error("Interrupted",e);
            }
            int runningCount=0;
            for(ThreadNode child:children){
                if(child.running){
                    runningCount++;
                }
            }
            if(runningCount==children.size()){
                allChildrenStarted=true;
            }
        }
        this.running = true;
        initialize();
        VLogger.info(this.getName()+" was started.");
        try {
            while (running) {
                execute();
            }
        }catch(Exception e){
            if(running){
                VLogger.error(this.getName()+" was unexpectly terminated",e);
            }
        }
        distruct();
        VLogger.info(this.getName()+" was shutdown.");
    }

    public abstract void initialize();
    public abstract void execute() throws Exception;
    public abstract void distruct();

    public void shutdown(){
        for(ThreadNode child:children){
            child.shutdown();
        }
        this.running = false;
    }

    public void start(){
        for (ThreadNode child:this.children){
            child.start();
        }
        super.start();
    }

    public boolean isRunning(){
        return this.running;
    }
}
