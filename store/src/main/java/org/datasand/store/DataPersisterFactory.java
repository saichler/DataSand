package org.datasand.store;

import org.datasand.codec.TypeDescriptorsContainer;
import org.datasand.store.bytearray.ByteArrayDataPersister;
import org.datasand.store.sqlite.SQLiteDataPersister;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DataPersisterFactory {
    public static DataPersister newDataPersister(int encoderType, Shard _shard,Class<?> _type,TypeDescriptorsContainer _container){
        switch(encoderType){
            case EncodeDataContainer.ENCODER_TYPE_BYTE_ARRAY:
                return new ByteArrayDataPersister(_shard, _type, _container);
            case EncodeDataContainer.ENCODER_TYPE_SQLITE:
                return new SQLiteDataPersister(_shard, _type, _container);
        }
        return null;
    }
}
