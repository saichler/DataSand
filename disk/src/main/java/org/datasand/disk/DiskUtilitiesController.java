/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import org.datasand.codec.ThreadPool;
import org.datasand.disk.model.DirectoryNode;
import org.datasand.disk.model.DirectoryScanListener;
import org.datasand.disk.tasks.SumFilesInDirectoryTask;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DiskUtilitiesController {

    private static final ThreadPool directoryCollectThreadPool = new ThreadPool(300,"Directory Collect",500);
    private static final ThreadPool guiThreadPool = new ThreadPool(10,"GUI Thread Pool",500);
    public static final DirectoryComparatorSize sizeComparator = new DirectoryComparatorSize();
    public static final DirectoryComparatorName nameComparator = new DirectoryComparatorName();
    public static final long K = 1024;
    public static final long MEG = 1024*1024;
    public static final long GIG = 1024*1024*1024;
    public static final DecimalFormat kFormat = new DecimalFormat("##.##");

    public static final void addCollectDirectoryTask(Runnable task){
        directoryCollectThreadPool.addTask(task);
    }

    public static final int getCollectDirectoryThreadpoolSize(){
        return directoryCollectThreadPool.getNumberOfThreads();
    }

    public static final void addGUITask(Runnable runthis){
        guiThreadPool.addTask(runthis);
    }

    public static void collect(DirectoryNode ds){
        SumFilesInDirectoryTask task = new SumFilesInDirectoryTask(ds);
        directoryCollectThreadPool.addTask(task);
        directoryCollectThreadPool.waitForEmpty();
    }

    public static void compute(DirectoryNode ds,int sortBy){
        ds.setSize(ds.getLocalSize());
        for(DirectoryNode d:ds.getChildren()){
            compute(d,sortBy);
            ds.setSize(ds.getSize()+ d.getSize());
        }
        if(sortBy==1) {
            Collections.sort(ds.getChildren(), sizeComparator);
        }else if (sortBy==2){
            Collections.sort(ds.getChildren(), nameComparator);
        }
    }

    public static class DirectoryComparatorSize implements Comparator<DirectoryNode>{
        @Override
        public int compare(DirectoryNode o1, DirectoryNode o2) {
            if(o1.getSize()>o2.getSize())
                return -1;
            if(o1.getSize()<o2.getSize())
                return 1;
            return 0;
        }
    }

    public static class DirectoryComparatorName implements Comparator<DirectoryNode>{
        @Override
        public int compare(DirectoryNode o1, DirectoryNode o2) {
            return o1.getDirectoryFile().getName().toLowerCase().compareTo(o2.getDirectoryFile().getName().toLowerCase());
        }
    }

    public static class FileComparatorSize implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            if(o1.length()>o2.length())
                return -1;
            if(o1.length()<o2.length())
                return 1;
            return 0;
        }
    }

    public static class FileComparatorDate implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            if(o1.lastModified()>o2.lastModified())
                return -1;
            if(o1.lastModified()<o2.lastModified())
                return 1;
            return 0;
        }
    }

    public static class FileComparatorName implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    }

    public static String getFileSize(File f){
        double s = f.length();
        double k = s/ DiskUtilitiesController.K;
        double m = s/ DiskUtilitiesController.MEG;
        double g = s/ DiskUtilitiesController.GIG;
        StringBuilder sb = new StringBuilder();
        if(g>1){
            sb.append(DiskUtilitiesController.kFormat.format(g)).append("g");
        }else
        if(m>1){
            sb.append(DiskUtilitiesController.kFormat.format(m)).append("m");
        } else {
            sb.append(DiskUtilitiesController.kFormat.format(k)).append("k");
        }
        return sb.toString();

    }

    public static void scanAndDeleteTargetDirectory(DirectoryNode directoryNode, DirectoryScanListener observer){
        if(directoryNode.getDirectoryFile().getName().equals("target")){
            observer.observe(directoryNode.getDirectoryFile(),2);
            deleteDirectory(directoryNode.getDirectoryFile());
        } else {
            for(DirectoryNode dir:directoryNode.getChildren()){
                scanAndDeleteTargetDirectory(dir,observer);
            }
        }
    }

    public static void deleteDirectory(File dir){
        File files[] = dir.listFiles();
        if(files!=null){
            for(File file:files){
                if(file.isDirectory()){
                    deleteDirectory(file);
                }else{
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
