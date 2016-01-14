package org.datasand.store;

import java.io.IOException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.VLogger;

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
        DataFile df = DataFileManager.instance.getDataFile(objectBytesArray.getJavaTypeMD5());
        try {
            df.write(keyBytesArray,objectBytesArray);
        } catch (IOException e) {
            VLogger.error("Failed to write to data file",e);
        }
    }

    public Object get(Object key){
        return null;
    }
}
