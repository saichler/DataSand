package org.datasand.store;

import java.io.IOException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.MD5ID;
import org.datasand.codec.VLogger;
import org.datasand.codec.VSchema;
import org.datasand.codec.VTable;
import org.datasand.store.jdbc.ResultSet;

/**
 * Created by root on 1/12/16.
 */
public class DataStore {

    public int put(Object key,Object object){
        HierarchyBytesArray objectBytesArray = new HierarchyBytesArray();
        Encoder.encodeObject(object,objectBytesArray);
        BytesArray keyBytesArray = null;
        if(key!=null){
            keyBytesArray = new BytesArray(0);
            Encoder.encodeObject(key,keyBytesArray);
        }
        return put(keyBytesArray,objectBytesArray,-1);
    }

    private int put(BytesArray keyData,HierarchyBytesArray objectData,int parentIndex){
        DataFile df = DataFileManager.instance.getDataFile(objectData.getJavaTypeMD5());
        int myIndex = -1;
        try {
            myIndex = df.write(keyData,objectData,parentIndex);
            if(objectData.getChildren()!=null){
                for(HierarchyBytesArray child:objectData.getChildren()){
                    put(null,child,myIndex);
                }
            }
        } catch (IOException e) {
            VLogger.error("Failed to write to data file",e);
        }
        return myIndex;
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

    public void commit(){
        DataFileManager.instance.commit();
    }

    public void execute(ResultSet rs){

    }
}
