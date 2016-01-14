package org.datasand.store;

import java.io.IOException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.MD5ID;
import org.datasand.codec.VLogger;
import org.datasand.codec.VSchema;
import org.datasand.codec.VTable;

/**
 * Created by root on 1/12/16.
 */
public class DataStore {

    public void put(Object key,Object object){
        HierarchyBytesArray objectBytesArray = new HierarchyBytesArray();
        Encoder.encodeObject(object,objectBytesArray);
        BytesArray keyBytesArray = null;
        if(key!=null){
            keyBytesArray = new BytesArray(0);
            Encoder.encodeObject(key,keyBytesArray);
        }
        put(keyBytesArray,objectBytesArray,-1);
    }

    private void put(BytesArray keyData,HierarchyBytesArray objectData,int parentIndex){
        DataFile df = DataFileManager.instance.getDataFile(objectData.getJavaTypeMD5());
        int myIndex = -1;
        try {
            myIndex = df.write(keyData,objectData,parentIndex);
        } catch (IOException e) {
            VLogger.error("Failed to write to data file",e);
        }
        if(objectData.getChildren()!=null){
            for(HierarchyBytesArray child:objectData.getChildren()){
                put(null,child,myIndex);
            }
        }
    }

    public Object get(Class<?> type,int index){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            HierarchyBytesArray hba = df.read(index);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            VLogger.error("Failed to read object",e);
        }
        return null;
    }
}
