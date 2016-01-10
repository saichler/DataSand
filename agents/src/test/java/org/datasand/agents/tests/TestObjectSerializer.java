
package org.datasand.agents.tests;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.agents.tests.TestObject;
import org.datasand.codec.serialize.ISerializer;

public class TestObjectSerializer implements ISerializer{

    @Override
    public void encode(Object value, BytesArray ba) {
        TestObject element = (TestObject) value;
        Encoder.encodeInt32(element.getZipcode(), ba);
        Encoder.encodeInt64(element.getSocial(), ba);
        Encoder.encodeString(element.getAddress(), ba);
        Encoder.encodeString(element.getName(), ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        TestObject builder = new TestObject();
        builder.setZipcode(Encoder.decodeInt32(ba));
        builder.setSocial(Encoder.decodeInt64(ba));
        builder.setAddress(Encoder.decodeString(ba));
        builder.setName(Encoder.decodeString(ba));
        return builder;
    }
}
