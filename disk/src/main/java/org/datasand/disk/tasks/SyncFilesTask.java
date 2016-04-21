/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.datasand.disk.model.Job;
import org.datasand.disk.model.JobsTableModel;
import org.datasand.disk.model.SyncDataListener;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SyncFilesTask extends Job{

    private static long FILE_PART_SIZE = 1024*1024*1;

    private final SyncDataListener listener;
    private final File source;
    private final File destination;

    public static boolean isrunning = false;

    public SyncFilesTask(SyncDataListener l, File source, File dest, JobsTableModel model){
        super(model);
        this.listener = l;
        this.source = source;
        this.destination = dest;
        isrunning = true;
    }

    public void run(){
        try {
            setStatus(JobStatusEnum.JOB_STATUS_RUNNING);
            doSync(source, destination, listener);
            listener.notifyDone(source,destination);
            setStatus(JobStatusEnum.JOB_STATUS_FINISHED_SUCCESSFULY);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void doSync(File from, File to,SyncDataListener listener) throws IOException {
        if(!from.getAbsolutePath().equals(from.getCanonicalPath())){
            System.err.println("Skipping "+from.getAbsolutePath()+" as it is symbolic link");
            return;
        }
        listener.notifyCurrentDirectory(from);
        if(!to.exists()){
            to.mkdirs();
        }

        File fromFiles[] = from.listFiles();
        for(File file:fromFiles){
            if(!isrunning){
                return;
            }

            if(file.isDirectory()){
                File destDir = new File(to.getPath()+"/"+file.getName());
                doSync(file, destDir,listener);
            }else{
                File dest = new File(to.getPath()+"/"+file.getName());
                if(!dest.exists()){
                    copyFile(file, dest,listener);
                }else{
                    if(file.length()!=dest.length()){
                        copyFile(file, dest,listener);
                    }
                }
            }
        }
    }

    private static void copyFile(File file,File dest,SyncDataListener listener) throws IOException{
        listener.notifyCurrentFile(file);
        long fileSize = file.length();
        int pieces = (int)(fileSize/FILE_PART_SIZE);
        long lastPiece = fileSize%FILE_PART_SIZE;
        if(!file.exists()){
            System.err.println("file "+file.getAbsolutePath()+" does not exist...");
            return;
        }
        FileInputStream source = new FileInputStream(file);
        FileOutputStream target = new FileOutputStream(dest);

        for(int i=0;i<pieces;i++){
            listener.notifyCurrentFileProgress(file,i,pieces);
            byte data[] = new byte[(int)FILE_PART_SIZE];
            source.read(data);
            target.write(data);
        }

        byte lastData[] = new byte[(int)lastPiece];
        source.read(lastData);
        target.write(lastData);
        source.close();
        target.close();
    }

    @Override
    public String getJobName() {
        return "Sync File from "+this.source.getName()+" to "+this.destination.getName();
    }
}
