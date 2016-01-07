/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.bytearray;

import java.lang.reflect.Method;
import org.datasand.codec.AttributeDescriptor;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class TypeDescriptor {
    private static final Observers observers = new Observers();

    private final Class<?> typeClass;

    public TypeDescriptor(Class<?> cls){
        this.typeClass = cls;
    }

    private void analyze(){
        Method mth[] = this.typeClass.getMethods();
        for (Method m : mth) {
            if(!container.isValidModelMethod(m))
                continue;
            AttributeDescriptor p = new AttributeDescriptor(m, cls);
            if(!container.isValidModelAttribute(p))
                continue;

            if(p.getMethodName().equals("getKey")){
                this.keyColumn = p;
            }

            if (container.isChildAttribute(p)) {
                children.put(p, p);
                TypeDescriptor child = container.getTypeDescriptorByClass(p.getReturnType(),this.hierarchyLevel+1);
                child.parents.put(p, p);
            } else {
                if (container.isTypeAttribute(p)) {
                    if(container.checkTypeDescriptorByClass(p.getReturnType())==null){
                        container.newTypeDescriptor(p.getReturnType(), 0);
                    }
                }
                pList.add(p);
            }
        }

    }
}
