package org.datasand.codec.serialize;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;

/**
 * Created by root on 1/9/16.
 */
public class StringSerializer implements ISerializer{
    @Override
    public void encode(Object value, BytesArray ba) {
        Encoder.encodeString((String)value,ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        return Encoder.decodeString(ba);
    }
}
