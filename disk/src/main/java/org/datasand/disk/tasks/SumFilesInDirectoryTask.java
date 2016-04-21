/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.tasks;

import java.io.File;
import java.io.IOException;
import org.datasand.codec.util.ThreadPool;
import org.datasand.disk.model.DirectoryNode;
import org.datasand.disk.model.Job;
import org.datasand.disk.model.JobsTableModel;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public final class SumFilesInDirectoryTask extends Job {

    private static final ThreadPool threadPool = new ThreadPool(300,"Directory Collect",500);
    public static boolean pause = false;
    public static final Object pauseSync = new Object();
    public static int pauseCount = 0;

    private final DirectoryNode directoryNode;

    public SumFilesInDirectoryTask(DirectoryNode directoryNode, JobsTableModel model){
        super(model);
        this.directoryNode = directoryNode;
    }

    public void addJob(Job job){
        this.model.addJob(job);
        threadPool.addTask(job);
    }

    public static void start(DirectoryNode ds,JobsTableModel model){
        SumFilesInDirectoryTask task = new SumFilesInDirectoryTask(ds,model);
        threadPool.addTask(task);
        threadPool.waitForEmpty();
    }

    public static int getThreadCount(){
        return threadPool.getNumberOfThreads();
    }

    public void run (){
        if(pause){
            setStatus(JobStatusEnum.JOB_STATUS_PAUSED);
            synchronized(pauseSync) {
                pauseCount++;
                try{pauseSync.wait();}catch(InterruptedException e){}
                pauseCount--;
            }
        }
        setStatus(JobStatusEnum.JOB_STATUS_RUNNING);
        directoryNode.observe(1);
        final File[] files = directoryNode.getDirectoryFile().listFiles();
        this.directoryNode.setLocalSize(this.directoryNode.getDirectoryFile().length());

        if (files != null) {
            for (File file : files) {
                try {
                    if(file.getName().equals("kcore")){
                        continue;
                    }
                    if(file.getAbsolutePath().equals(file.getCanonicalPath())){
                        if (file.isDirectory()) {
                            DirectoryNode subDir = new DirectoryNode(this.directoryNode, file);
                            SumFilesInDirectoryTask newTask = new SumFilesInDirectoryTask(subDir,model);
                            addJob(newTask);
                        } else {
                            this.directoryNode.setLocalSize(this.directoryNode.getLocalSize() + file.length());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        setStatus(JobStatusEnum.JOB_STATUS_FINISHED_SUCCESSFULY);
    }

    @Override
    public String getJobName() {
        return "Scan Directory \""+directoryNode.getDirectoryFile().getName()+"\"";
    }
}

