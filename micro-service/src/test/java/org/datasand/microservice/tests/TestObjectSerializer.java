/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.tests;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
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
