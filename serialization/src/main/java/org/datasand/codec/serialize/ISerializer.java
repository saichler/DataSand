/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.serialize;

import org.datasand.codec.bytearray.BytesArray;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ISerializer {

    public void encode(Object value, BytesArray ba);
    public Object decode(BytesArray ba);

}
