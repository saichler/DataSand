
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

public class SubPojoListSerializer implements ISerializer{
    @Override
    public void encode(Object value, BytesArray ba) {
        SubPojoList element = (SubPojoList) value;
        Encoder.encodeString(element.getName(), ba);
    }
    @Override
    public Object decode(BytesArray ba) {
        SubPojoList builder = new SubPojoList();
        builder.setName(Encoder.decodeString(ba));
        return builder;
    }
}
