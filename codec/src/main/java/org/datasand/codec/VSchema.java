/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class VSchema {
    public static VSchema instance = new VSchema();

    private final Map<Class,VTable> classToVTable = new HashMap<>();
    private final Map<String,VTable> classNameToVTable = new HashMap<>();

    private VSchema(){}

    public VTable getVTable(Class<?> cls){
        return classToVTable.get(cls);
    }

    public void registerVTable(VTable tbl){
        this.classToVTable.put(tbl.getJavaClassType(),tbl);
        this.classNameToVTable.put(tbl.getJavaClassTypeName(),tbl);
    }

    public byte[] getRepositoryData() {
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeInt16(classToVTable.size(), ba);
        for (VTable type : classToVTable.values()) {
            VTable.encode(type,ba);
        }
        return ba.getData();
    }

    public void load(byte data[]){
        BytesArray ba = new BytesArray(data);
        int size = Encoder.decodeInt16(ba);
        for (int i = 0; i < size; i++) {
            VTable type = VTable.decode(ba);
            classNameToVTable.put(type.getJavaClassTypeName(), type);
        }
    }

    public VTable getVTableByName(String typeName) {
        VTable result = this.classNameToVTable.get(typeName);

        if(result!=null){
            return result;
        }

        if (typeName.indexOf(".") != -1) {
            typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
        }

        for (VTable t : classNameToVTable.values()) {
            if (t.getJavaClassTypeName().endsWith(typeName)) {
                return t;
            }
        }

        for (VTable t : classNameToVTable.values()) {
            if (t.getName().endsWith(typeName.toUpperCase())) {
                return t;
            }
        }

        for (VTable t : classNameToVTable.values()) {
            if (t.getJavaClassTypeName().toLowerCase().indexOf(typeName.toLowerCase()) != -1) {
                return t;
            }
        }
        return null;
    }

}
