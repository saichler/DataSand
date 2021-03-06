/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JDBCProxy implements InvocationHandler {

    private Object myObject = null;
    private Class<?> myObjectClass = null;

    public JDBCProxy(Object obj) {
        this.myObject = obj;
        this.myObjectClass = this.myObject.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        System.err.println("Class " + this.myObjectClass.getSimpleName()
                + " Method " + method.getName());
        return method.invoke(this.myObject, args);
    }

}
