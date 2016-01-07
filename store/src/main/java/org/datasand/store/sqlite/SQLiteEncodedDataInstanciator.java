package org.datasand.store.sqlite;

import org.datasand.codec.EncodeDataContainerFactory.EncodeDataContainerInstantiator;
import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SQLiteEncodedDataInstanciator implements EncodeDataContainerInstantiator{
    @Override
    public EncodeDataContainer newEncodeDataContainer(Object data, Object key,TypeDescriptor _ts) {
        return new SQLiteEncodeDataContainer(_ts);
    }
}
