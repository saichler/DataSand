package org.datasand.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by saichler on 6/6/16.
 */
public class ObjectIDImpl implements InvocationHandler{
    private String data = "";
    private final Class<? extends IObject> oType;

    public ObjectIDImpl(Class<? extends IObject> oType){
        this.oType = oType;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
