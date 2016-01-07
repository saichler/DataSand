package org.datasand.store.json;

import org.datasand.codec.EncodeDataContainerFactory.EncodeDataContainerInstantiator;
import org.datasand.codec.TypeDescriptor;
import org.datasand.codec.json.JsonEncodeDataContainer;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JSONEncodeDataContainerInstanciator implements EncodeDataContainerInstantiator{
    @Override
    public EncodeDataContainer newEncodeDataContainer(Object data, Object key,TypeDescriptor _ts) {
        return new JsonEncodeDataContainer(_ts);
    }
}
