/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/21/16.
 */
public class HObject {
    private HObject parent = null;
    private Object object = null;

    public HObject getParent() {
        return parent;
    }

    public void setParent(HObject parent) {
        this.parent = parent;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
