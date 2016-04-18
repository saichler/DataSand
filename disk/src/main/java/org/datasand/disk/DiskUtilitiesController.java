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
import org.datasand.disk.model.DirectoryObserver;
import org.datasand.disk.tasks.SumFilesInDirectoryTask;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DiskUtilitiesController {

    public static final ThreadPool threadPool = new ThreadPool(300,"Collect Directory Size",500);
    public static final DirectoryComparatorSize comparator = new DirectoryComparatorSize();
    public static final long K = 1024;
    public static final long MEG = 1024*1024;
    public static final long GIG = 1024*1024*1024;
    public static final DecimalFormat kFormat = new DecimalFormat("#.##");

    public static void collect(DirectoryNode ds){
        SumFilesInDirectoryTask task = new SumFilesInDirectoryTask(ds);
        threadPool.addTask(task);
        threadPool.waitForEmpty();
    }

    public static void compute(DirectoryNode ds,boolean sort){
        ds.setSize(ds.getLocalSize());
        for(DirectoryNode d:ds.getChildren()){
            compute(d,sort);
            ds.setSize(ds.getSize()+ d.getSize());
        }
        if(sort) {
            Collections.sort(ds.getChildren(), comparator);
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

    public static void scanAndDeleteTargetDirectory(File f, DirectoryObserver observer){
        if(f.getName().equals("target")){
            observer.observe(f,2);
            deleteDirectory(f);
        }else
        if(f.isDirectory()){
            File files[] = f.listFiles();
            for(File file:files){
                if(file.isDirectory()){
                    scanAndDeleteTargetDirectory(file,observer);
                }
            }
        }
    }

    private static void deleteDirectory(File dir){
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
