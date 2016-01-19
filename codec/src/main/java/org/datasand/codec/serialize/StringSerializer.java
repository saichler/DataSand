/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.serialize;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/08/16.
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
