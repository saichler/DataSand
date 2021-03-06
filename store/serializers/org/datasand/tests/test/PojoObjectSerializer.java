
/**
  * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
  * Generated Code! Do Not Edit unless you move the java file. 
**/
package org.datasand.tests.test;
import java.util.List;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.tests.test.SubPojoList;
import org.datasand.tests.test.SubPojoObject;
import org.datasand.tests.test.PojoObject;

public class PojoObjectSerializer implements ISerializer{
                @Override
                public void encode(Object value, BytesArray ba) {
                                PojoObject element = (PojoObject) value;
                                Encoder.encodeInt64(element.getTestLong(), ba);
                                Encoder.encodeString(element.getTestString(), ba);
                                Encoder.encodeInt32(element.getTestIndex(), ba);
                                Encoder.encodeShort(element.getTestShort(), ba);
                                Encoder.encodeBoolean(element.isTestBoolean(), ba);
                                Encoder.encodeList(element.getList(), ba);
                                Encoder.encodeObject(element.getSubPojo(), ba);
                }
                @Override
                public Object decode(BytesArray ba) {
                                PojoObject builder = new PojoObject();
                                builder.setTestLong(Encoder.decodeInt64(ba));
                                builder.setTestString(Encoder.decodeString(ba));
                                builder.setTestIndex(Encoder.decodeInt32(ba));
                                builder.setTestShort(Encoder.decodeShort(ba));
                                builder.setTestBoolean(Encoder.decodeBoolean(ba));
                                builder.setList((List<SubPojoList>)Encoder.decodeList(ba));
                                builder.setSubPojo((SubPojoObject)Encoder.decodeObject(ba));
                                return builder;
                }
}
