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
import org.datasand.disk.DiskUtilitiesController;
import org.datasand.disk.model.DirectoryNode;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public final class SumFilesInDirectoryTask implements Runnable{

    public static boolean pause = false;
    public static final Object pauseSync = new Object();
    public static int pauseCount = 0;

    private final DirectoryNode directoryNode;

    public SumFilesInDirectoryTask(DirectoryNode directoryNode){
        this.directoryNode = directoryNode;
    }

    public void run (){
        if(pause){
            synchronized(pauseSync) {
                pauseCount++;
                try{pauseSync.wait();}catch(InterruptedException e){}
                pauseCount--;
            }
        }
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
                            SumFilesInDirectoryTask newTask = new SumFilesInDirectoryTask(subDir);
                            DiskUtilitiesController.addCollectDirectoryTask(newTask);
                        } else {
                            this.directoryNode.setLocalSize(this.directoryNode.getLocalSize() + file.length());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

