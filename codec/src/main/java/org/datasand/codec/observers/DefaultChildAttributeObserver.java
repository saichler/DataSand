/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.observers;

import org.datasand.codec.VColumn;
import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultChildAttributeObserver implements IChildAttributeObserver{

    @Override
    public boolean isChildAttribute(VColumn vColumn) {
        if(vColumn.getJavaMethodReturnType().isPrimitive()){
            return false;
        }
        if(!vColumn.getJavaMethodReturnType().getPackage().getName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean isChildAttribute(VTable vTable) {
        if(!vTable.getJavaClassType().getName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean supportAugmentation(VColumn vColumn) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportAugmentation(VTable vTable) {
        // TODO Auto-generated method stub
        return false;
    }

}
