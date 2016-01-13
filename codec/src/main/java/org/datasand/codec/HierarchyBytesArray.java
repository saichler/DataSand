/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HierarchyBytesArray extends BytesArray{

    private MD5ID javaTypeMD5 = null;
    private final List<HierarchyBytesArray> children = new ArrayList<>();
    private int childLocation = 0;

    public HierarchyBytesArray(){
        super(new byte[1024]);
    }

    public MD5ID getJavaTypeMD5() {
        return javaTypeMD5;
    }

    public void setJavaTypeMD5(MD5ID javaTypeMD5) {
        this.javaTypeMD5 = javaTypeMD5;
    }

    public HierarchyBytesArray addNewChild(){
        HierarchyBytesArray child = new HierarchyBytesArray();
        children.add(child);
        return child;
    }

    public HierarchyBytesArray nextChild(){
        HierarchyBytesArray child = this.children.get(childLocation);
        childLocation++;
        return child;
    }

    public List<HierarchyBytesArray> getChildren() {
        return children;
    }

    @Override
    public void resetLocation() {
        super.resetLocation();
        this.childLocation = 0;
        for(HierarchyBytesArray child:this.children){
            child.resetLocation();
        }
    }
}
