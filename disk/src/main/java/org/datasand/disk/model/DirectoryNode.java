/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import org.datasand.disk.DiskUtilitiesController;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public final class DirectoryNode implements TreeNode {
    private final File directoryFile;
    private final DirectoryNode parent;
    private final List<DirectoryNode> children = new ArrayList<>();

    private long size = 0;
    private long localSize = 0;
    private final DirectoryObserver observer;

    public String toString(){
        double s = this.size;
        double k = s/ DiskUtilitiesController.K;
        double m = s/ DiskUtilitiesController.MEG;
        double g = s/ DiskUtilitiesController.GIG;
        StringBuilder sb = new StringBuilder(this.directoryFile.getName());
        sb.append(" - ");
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

    public DirectoryNode(DirectoryNode parent, File path, DirectoryObserver observer){
        this.directoryFile = path;
        this.observer = observer;
        this.parent = parent;
        if(this.parent!=null) {
            this.parent.children.add(this);
        }
    }

    public DirectoryNode(DirectoryNode parent, File path){
        this(parent,path,parent.observer);
    }

    public File getDirectoryFile() {
        return this.directoryFile;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size){
        this.size = size;
    }

    public DirectoryNode getParent() {
        return parent;
    }

    public List<DirectoryNode> getChildren() {
        return children;
    }

    public void observe(int taskID) {
        if(this.observer!=null){
            this.observer.observe(this.directoryFile,taskID);
        }
    }

    public long getLocalSize() {
        return localSize;
    }

    public void setLocalSize(long size){
        this.localSize = size;
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
        for(DirectoryNode d:this.children){
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

