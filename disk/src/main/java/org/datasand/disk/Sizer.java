/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.datasand.codec.ThreadPool;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Sizer {

    public static final ThreadPool threadPool = new ThreadPool(300,"Collect Directory Size",500);
    public static final DirectoryComparator comparator = new DirectoryComparator();
    public static final long K = 1024;
    public static final long MEG = 1024*1024;
    public static final long GIG = 1024*1024*1024;
    public static final DecimalFormat kFormat = new DecimalFormat("#.##");
    public static final DecimalFormat mFormat = new DecimalFormat("#.###");
    public static final DecimalFormat gFormat = new DecimalFormat("#.####");
    public static boolean pause = false;
    public static final Object pauseSync = new Object();
    public static int pauseCount = 0;

    public static interface SizerVisitor {
        public void currentFile(File file);
    }

    public static final class Directory implements TreeNode {
        private final File dir;
        private final Directory parent;
        private final List<Directory> children = new ArrayList<>();

        private long size = 0;
        private long localSize = 0;
        private final SizerVisitor visitor;

        public String toString(){
            double s = this.size;
            double k = s/K;
            double m = s/MEG;
            double g = s/GIG;
            return this.dir.getName()+" - "+kFormat.format(k)+"k / "+mFormat.format(m)+"m / "+gFormat.format(g)+"g";
        }

        public Directory(Directory parent, File path, SizerVisitor visitor){
            this.dir = path;
            this.visitor = visitor;
            this.parent = parent;
            if(this.parent!=null) {
                this.parent.children.add(this);
            }
        }

        public File getDir() {
            return this.dir;
        }

        public long getSize() {
            return size;
        }

        public SizerVisitor getVisitor() {
            return visitor;
        }

        public Directory getParent() {
            return parent;
        }

        public List<Directory> getChildren() {
            return children;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return this.children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return this.children.size();
        }

        @Override
        public int getIndex(TreeNode node) {
            int i = 0;
            for(Directory d:this.children){
                if(d==node){
                    return i;
                }
                i++;
            }
            return -1;
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return this.children.size()==0;
        }

        @Override
        public Enumeration children() {
            return Collections.enumeration(this.children);
        }
    }

    public static final class CollectDirectorySizeTask implements Runnable{
        private final Directory dir;

        public CollectDirectorySizeTask(Directory dir){
            this.dir = dir;
        }

        public void run (){
            if(pause){
                synchronized(pauseSync) {
                    Sizer.pauseCount++;
                    try{pauseSync.wait();}catch(InterruptedException e){}
                }
                synchronized(pauseSync) {
                    Sizer.pauseCount--;
                }
            }
            if (dir.visitor != null) {
                dir.visitor.currentFile(this.dir.dir);
            }
            final File[] files = dir.dir.listFiles();
            this.dir.localSize += this.dir.dir.length();

            if (files != null) {
                for (File file : files) {
                    try {
                        if(file.getName().equals("kcore")){
                            continue;
                        }
                        if(file.getAbsolutePath().equals(file.getCanonicalPath())){
                            if (file.isDirectory()) {
                                Directory subDir = new Directory(this.dir, file, this.dir.visitor);
                                CollectDirectorySizeTask newTask = new CollectDirectorySizeTask(subDir);
                                threadPool.addTask(newTask);
                            } else {
                                this.dir.localSize += file.length();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void collect(Directory ds){
        CollectDirectorySizeTask task = new CollectDirectorySizeTask(ds);
        threadPool.addTask(task);
        while(!threadPool.isEmpty()){
            try{Thread.sleep(500);}catch(InterruptedException e){}
        }
    }

    public static void compute(Directory ds,boolean sort){
        ds.size = ds.localSize;
        for(Directory d:ds.children){
            compute(d,sort);
            ds.size+= d.size;
        }
        if(sort) {
            Collections.sort(ds.children, comparator);
        }
    }

    public static class DirectoryComparator implements Comparator<Directory>{
        @Override
        public int compare(Directory o1, Directory o2) {
            if(o1.size>o2.size)
                return -1;
            if(o1.size<o2.size)
                return 1;
            return 0;
        }
    }

    public static void main(String args[]){
        Directory dirSize = new Directory(null,new File("/root"),null);
        CollectDirectorySizeTask task = new CollectDirectorySizeTask(dirSize);
        threadPool.addTask(task);
        while(!threadPool.isEmpty()){
            try{Thread.sleep(1000);}catch(InterruptedException e){}
        }
        System.out.println((dirSize.size/1024));
    }
}
