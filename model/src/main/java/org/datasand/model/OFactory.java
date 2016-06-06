package org.datasand.model;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sharonaicler on 6/6/16.
 */
public class OFactory {

    private static Map<Class<?>,ClassLoader> classToLoader = new HashMap<>();

    public static Object newObject(Class<? extends IObject> oType){
        ClassLoader loader = classToLoader.get(oType);
        return Proxy.newProxyInstance(loader,new Class[]{oType},new ObjectImpl(oType));
    }

    public static Object newObjectID(Class<? extends IObject> oType){
        ClassLoader loader = classToLoader.get(oType);
        return Proxy.newProxyInstance(loader,new Class[]{oType},new ObjectIDImpl(oType));
    }

    public static void addClassLoader(Class<? extends IObject> oType,ClassLoader cl){
        classToLoader.put(oType,cl);
    }
}
