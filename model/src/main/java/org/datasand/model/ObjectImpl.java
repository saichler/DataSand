package org.datasand.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by saichler on 6/6/16.
 */
public class ObjectImpl implements InvocationHandler{

    private final Map<String,Object> data = new HashMap<String, Object>();
    private final Class<? extends IObject> oType;

    public ObjectImpl(Class<? extends IObject> oType){
        this.oType = oType;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().startsWith("set")){
            data.put(method.getName().substring(3),args[0]);
        }else
        if(method.getName().startsWith("get")){
            return data.get(method.getName().substring(3));
        }else
        if(method.getName().startsWith("is")){
            return data.get(method.getName().substring(2));
        }else
        if(method.getName().startsWith("add")){
            List lst = (List)data.get(method.getName().substring(3));
            if(lst==null){

            }
            lst.add(args[0]);
        }else
        if(method.getName().startsWith("del")){
            List lst = (List)data.get(method.getName().substring(3));
            if(lst==null){
                return null;
            }
            lst.remove(args[0]);
        }
        return null;
    }

}
