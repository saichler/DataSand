/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.datasand.codec;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class BytesArrayTest {
    @Test
    public void testArrayEnlarge(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeString("Test",ba);
        ba.resetLocation();
        String test = Encoder.decodeString(ba);
        Assert.assertEquals("Test",test);
    }

    @Test
    public void testGetData(){
        BytesArray ba = new BytesArray(new byte[1024]);
        Encoder.encodeString("Test",ba);
        byte[] data = ba.getData();
        Assert.assertNotEquals(data.length,1024);
    }

    @Test
    public void testInsert(){
        BytesArray ba1 = new BytesArray(new byte[1024]);
        BytesArray ba2 = new BytesArray(0);
        Encoder.encodeString("Test",ba2);
        ba1.insert(ba2,0);
        Assert.assertEquals(ba1.getData().length,ba2.getData().length);
        Assert.assertNotEquals(ba1.getBytes().length,ba2.getBytes().length);
    }

    @Test
    public void testOverflowInsert(){
        BytesArray ba1 = new BytesArray(new byte[1024]);
        ba1.setLocation(1024);
        BytesArray ba2 = new BytesArray(0);
        ba2.insert(ba1,0);
        Assert.assertEquals(ba1.getData().length,ba2.getData().length);
        Assert.assertEquals(ba1.getBytes().length,ba2.getBytes().length);

    }

    @Test
    public void testInsertBytes(){
        BytesArray ba = new BytesArray(new byte[1024]);
        ba.setLocation(1024);
        byte data[] = new byte[1035];
        ba.insert(data);
        Assert.assertEquals(ba.getLocation(),1024+1035);
    }
}
