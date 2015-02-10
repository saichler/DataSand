
package org.opendaylight.datasand.agents.tests;
import org.opendaylight.datasand.codec.EncodeDataContainer;
import org.opendaylight.datasand.codec.ISerializer;
import org.opendaylight.datasand.agents.tests.TestObject;

public class TestObjectSerializer implements ISerializer{
    @Override
    public void encode(Object value, byte[] byteArray, int location) {
    }

    @Override
    public void encode(Object value, EncodeDataContainer ba) {
        TestObject element = (TestObject) value;
        ba.setCurrentAttributeName("Address");
        ba.getEncoder().encodeString(element.getAddress(), ba);
        ba.setCurrentAttributeName("Name");
        ba.getEncoder().encodeString(element.getName(), ba);
        ba.setCurrentAttributeName("Social");
        ba.getEncoder().encodeInt64(element.getSocial(), ba);
        ba.setCurrentAttributeName("Zipcode");
        ba.getEncoder().encodeInt32(element.getZipcode(), ba);
    }
    @Override
    public Object decode(byte[] byteArray, int location, int length) {
        return null;
    }
    @Override
    public Object decode(EncodeDataContainer ba, int length) {
        TestObject builder = new TestObject();
        ba.setCurrentAttributeName("Address");
        builder.setAddress(ba.getEncoder().decodeString(ba));
        ba.setCurrentAttributeName("Name");
        builder.setName(ba.getEncoder().decodeString(ba));
        ba.setCurrentAttributeName("Social");
        builder.setSocial(ba.getEncoder().decodeInt64(ba));
        ba.setCurrentAttributeName("Zipcode");
        builder.setZipcode(ba.getEncoder().decodeInt32(ba));
        return builder;
    }
    public String getShardName(Object obj) {
        return "Default";
    }
    public String getRecordKey(Object obj) {
        return null;
    }
}
