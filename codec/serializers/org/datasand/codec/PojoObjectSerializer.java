
package org.datasand.codec;
import java.util.List;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.codec.SubPojoObject;
import org.datasand.codec.SubPojoList;
import org.datasand.codec.PojoObject;

public class PojoObjectSerializer implements ISerializer{
    @Override
    public void encode(Object value, BytesArray ba) {
        PojoObject element = (PojoObject) value;
        Encoder.encodeInt64(element.getTestLong(), ba);
        Encoder.encodeString(element.getTestString(), ba);
        Encoder.encodeInt32(element.getTestIndex(), ba);
        Encoder.encodeShort(element.getTestShort(), ba);
        Encoder.encodeBoolean(element.isTestBoolean(), ba);
        Encoder.encodeObject(element.getSubPojo(), ba);
        Encoder.encodeList(element.getList(), ba);
    }
    @Override
    public Object decode(BytesArray ba) {
        PojoObject builder = new PojoObject();
        builder.setTestLong(Encoder.decodeInt64(ba));
        builder.setTestString(Encoder.decodeString(ba));
        builder.setTestIndex(Encoder.decodeInt32(ba));
        builder.setTestShort(Encoder.decodeShort(ba));
        builder.setTestBoolean(Encoder.decodeBoolean(ba));
        builder.setSubPojo((SubPojoObject)Encoder.decodeObject(ba));
        builder.setList((List<SubPojoList>)Encoder.decodeList(ba));
        return builder;
    }
}
