/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.serialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.datasand.codec.MD5ID;
import org.datasand.codec.Observers;
import org.datasand.codec.VSchema;
import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SerializersManager {

    private final Map<MD5ID,ISerializer> md5ToSerializer = new HashMap<>();
    private final Map<Class<?>,MD5ID> classToMD5 = new HashMap<>();
    private final Map<MD5ID,Class<?>> md5ToClass = new HashMap<>();

    public SerializersManager(){

    }

    public void registerSerializer(Class<?> cls,ISerializer serializer){
        MD5ID md5ID = MD5ID.create(cls.getName());
        this.md5ToSerializer.put(md5ID,serializer);
        this.classToMD5.put(cls,md5ID);
        this.md5ToClass.put(md5ID,cls);
    }

    public ISerializer getSerializerByClass(Class cls){
        MD5ID id = getMD5ByClass(cls);
        return getSerializerByMD5(id);
    }

    public MD5ID getMD5ByClass(Class cls){
        MD5ID id = classToMD5.get(cls);
        if(id==null){
            VTable t = VSchema.instance.getVTable(cls);
            if(t==null){
                t = new VTable(cls);
                t.analyze();
                VSchema.instance.registerVTable(t);
            }

            try {
                ISerializer s = SerializerGenerator.generateSerializer(t);
                registerSerializer(cls,s);
                id = getMD5ByClass(cls);
            } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public MD5ID getMD5ByObject(Object o){
        Class<?> cls = Observers.instance.getClassExtractor().getObjectClass(o);
        return getMD5ByClass(cls);
    }

    public ISerializer getSerializerByMD5(MD5ID id){
        return md5ToSerializer.get(id);
    }

    public Class getClassByMD5(MD5ID id){
        return md5ToClass.get(id);
    }

    public ISerializer getSerializerByObject(Object o){
        Class<?> objectClass = Observers.instance.getClassExtractor().getObjectClass(o);
        return getSerializerByClass(objectClass);
    }

    public ISerializer getSerializerByLongs(long a,long b){
        return getSerializerByMD5(MD5ID.create(a,b));
    }
}
