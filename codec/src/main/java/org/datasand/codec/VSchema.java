package org.datasand.codec;

import java.util.HashMap;
import java.util.Map;
import org.datasand.codec.serialize.ISerializer;

/**
 * Created by root on 1/8/16.
 */
public class VSchema {
    public static VSchema instance = new VSchema();

    private final Map<Class,VTable> vtables = new HashMap<>();

    private VSchema(){}

    public VTable getVTable(Class<?> cls){
        return vtables.get(cls);
    }

    public void registerVTable(VTable tbl){
        this.vtables.put(tbl.getJavaClassType(),tbl);
    }
}
