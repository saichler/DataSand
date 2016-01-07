package org.datasand.codec;

import org.datasand.codec.bytearray.ByteArrayEncodeDataContainer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ISerializer {

    public void encode(Object value, ByteArrayEncodeDataContainer ba);
    public Object decode(ByteArrayEncodeDataContainer ba);

}
