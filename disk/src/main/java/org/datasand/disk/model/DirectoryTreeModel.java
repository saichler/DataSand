/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.datasand.disk.model.DirectoryNode;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DirectoryTreeModel implements TreeModel {

    private final DirectoryNode root;

    public DirectoryTreeModel(DirectoryNode root){
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((DirectoryNode)parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((DirectoryNode)parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((DirectoryNode)node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((DirectoryNode)parent).getIndex((DirectoryNode)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}