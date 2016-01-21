package org.datasand.store;

import java.io.File;
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
        DataKey dataKey = getDataKeyFromKey(key);
        return put(dataKey,objectBytesArray,-1);
    }

    private DataKey getDataKeyFromKey(Object key){
        DataKey dataKey = null;
        if(key!=null){
            if(key instanceof Integer){
                dataKey = new DataKey((Integer)key);
            }else
            if(key instanceof Long){
                dataKey = new DataKey((Long)key);
            }else
            if(key!=null){
                BytesArray ba = new BytesArray(256);
                Encoder.encodeObject(key,ba);
                MD5ID id = MD5ID.create(ba.getData());
                dataKey = new DataKey(id.getMd5Long1(),id.getMd5Long2());
            }
        }
        return dataKey;
    }

    private int put(DataKey dataKey,HierarchyBytesArray objectData,int parentIndex){
        DataFile df = DataFileManager.instance.getDataFile(objectData.getJavaTypeMD5());
        int myIndex = -1;
        try {
            myIndex = df.write(dataKey,objectData,parentIndex);
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

    public Object get(Object key,Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            DataKey dataKey = getDataKeyFromKey(key);
            HierarchyBytesArray hba = df.readByKey(dataKey);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            VLogger.error("Failed to read object",e);
        }
        return null;
    }

    public Object get(Class<?> type,int index){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            HierarchyBytesArray hba = df.readByIndex(index);
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

    public void close() {
        DataFileManager.instance.close(true);
    }

    public void truncateAll(){
        DataFileManager.instance.close(false);
        File f = new File("./database");
        if(f.exists()){
            File files[] = f.listFiles();
            for(File file:files){
                file.delete();
            }
        }
        f.delete();
    }

    public void execute(ResultSet rs){

    }
}
