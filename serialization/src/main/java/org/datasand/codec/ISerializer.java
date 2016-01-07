package org.datasand.codec;

import org.datasand.codec.bytearray.BytesArray;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ISerializer {

    public void encode(Object value, BytesArray ba);
    public Object decode(BytesArray ba);

}
