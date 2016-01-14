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
import java.util.Collections;
import java.util.Comparator;
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
    private VColumn parentVColumn = null;
    private final Map<VColumn,VTable> childrenToColumn = new HashMap<>();
    private final List<VTable> children = new ArrayList<>();
    private final Map<VColumn,VTable> parents = new HashMap<>();
    private final List<VColumn> columns = new ArrayList<>();
    private final Map<String,VColumn> nameToColumn = new HashMap<>();

    public VTable(Class<?> cls){
        this.javaClassType = cls;
        this.name = this.javaClassType.getSimpleName().toUpperCase();
        VSchema.instance.registerVTable(this);
    }

    public Class<?> getJavaClassType() {
        return javaClassType;
    }

    public List<VColumn> getColumns() {
        return columns;
    }

    public Map<VColumn, VTable> getParents() {
        return parents;
    }

    public List<VTable> getChildren() {
        return children;
    }

    public VColumn getColumnByName(String name){
        return this.nameToColumn.get(name.toUpperCase());
    }

    public String getName(){
        return this.name;
    }

    public boolean analyze(){
        return analyze(new HashSet<Class<?>>());
    }

    public VColumn getVColumn(){
        return this.parentVColumn;
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
                if(!beenHere.contains(vColumn.getJavaMethodReturnType())) {
                    VTable child = new VTable(vColumn.getJavaMethodReturnType());
                    childrenToColumn.put(vColumn, child);
                    children.add(child);
                    child.parentVColumn = vColumn;
                    child.parents.put(vColumn, this);
                    child.analyze(beenHere);
                }
            } else {
                columns.add(vColumn);
                nameToColumn.put(vColumn.getvColumnName().toUpperCase(),vColumn);
            }
        }
        Collections.sort(columns, new Comparator<VColumn>() {
            @Override
            public int compare(VColumn o1, VColumn o2) {
                if(o1.getvColumnName().hashCode()<o2.getvColumnName().hashCode()) {
                    return -1;
                }else{
                    return 1;
                }
            }
        });
        return true;
    }
}
