
package org.datasand.tests.test;
import java.util.List;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.tests.test.SubPojoObject;

public class SubPojoObjectSerializer implements ISerializer{
    @Override
    public void encode(Object value, BytesArray ba) {
        SubPojoObject element = (SubPojoObject) value;
        Encoder.encodeInt32(element.getNumber(), ba);
        Encoder.encodeString(element.getString(), ba);
    }
    @Override
    public Object decode(BytesArray ba) {
        SubPojoObject builder = new SubPojoObject();
        builder.setNumber(Encoder.decodeInt32(ba));
        builder.setString(Encoder.decodeString(ba));
        return builder;
    }
}
