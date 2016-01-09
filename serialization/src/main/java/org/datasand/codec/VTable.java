/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class VTable {

    private final Class<?> javaClassType;
    private final String name;
    private VColumn keyColumn = null;
    private final Map<VColumn,VColumn> children = new HashMap<>();
    private final Map<VColumn,VColumn> parents = new HashMap<>();
    private final List<VColumn> columns = new ArrayList<>();

    public VTable(Class<?> cls){
        this.javaClassType = cls;
        this.name = this.javaClassType.getSimpleName();
    }

    public Class<?> getJavaClassType() {
        return javaClassType;
    }

    public List<VColumn> getColumns() {
        return columns;
    }

    public Map<VColumn, VColumn> getParents() {
        return parents;
    }

    public Map<VColumn, VColumn> getChildren() {
        return children;
    }

    public boolean analyze(HashSet<Class<?>> beenHere){
        if(beenHere.contains(this.javaClassType)){
            return false;
        }
        beenHere.add(this.javaClassType);

        Method mth[] = this.javaClassType.getMethods();
        for (Method m : mth) {
            if(!Observers.instance.isValidModelMethod(m)) {
                continue;
            }
            VColumn vColumn = new VColumn(m, this.javaClassType);

            if(!Observers.instance.isValidModelAttribute(vColumn))
                continue;

            if(vColumn.getJavaGetMethod().equals("getKey")){
                this.keyColumn = vColumn;
            }

            if (Observers.instance.isChildAttribute(vColumn)) {
                if(!beenHere.contains(vColumn.getJavaClass())) {
                    children.put(vColumn, vColumn);
                    VTable child = new VTable(vColumn.getJavaClass());
                    child.parents.put(vColumn, vColumn);
                    child.analyze(beenHere);
                }
            } else {
                if (Observers.instance.isTypeAttribute(vColumn)) {

                }
                columns.add(vColumn);
            }
        }
        return true;
    }
}
