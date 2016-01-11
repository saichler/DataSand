/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class EncoderTest {

    @Test
    public void testBoolean(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeBoolean(true,ba);
        ba.resetLocation();
        Boolean i = Encoder.decodeBoolean(ba);
        Assert.assertEquals(true,i);
        ba.resetLocation();
        Encoder.encodeBoolean(null,ba);
        ba.resetLocation();
        i = Encoder.decodeBoolean(ba);
        Assert.assertFalse(i);
    }

    @Test
    public void testString(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeString("test",ba);
        ba.resetLocation();
        String i = Encoder.decodeString(ba);
        Assert.assertEquals("test",i);
        ba.resetLocation();
        Encoder.encodeString(null,ba);
        ba.resetLocation();
        i = Encoder.decodeString(ba);
        Assert.assertNull(i);
    }

    @Test
    public void testInt16(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeInt16(50000,ba);
        ba.resetLocation();
        int i = Encoder.decodeInt16(ba);
        Assert.assertEquals(50000,i);
    }

    @Test
    public void testByte(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeByte((byte)17,ba);
        ba.resetLocation();
        byte i = Encoder.decodeByte(ba);
        Assert.assertEquals(17,i);
    }

    @Test
    public void testInt32(){
        BytesArray ba = new BytesArray(0);
        Encoder.encodeInt32(500000,ba);
        ba.resetLocation();
        int i = Encoder.decodeInt32(ba);
        Assert.assertEquals(500000,i);
        ba.resetLocation();
        Encoder.encodeInt32(null,ba);
        ba.resetLocation();
        i = Encoder.decodeInt32(ba);
        Assert.assertEquals(0,i);
    }

    @Test
    public void testInt64(){
        long l = System.currentTimeMillis();
        BytesArray ba = new BytesArray(0);
        Encoder.encodeInt64(l,ba);
        ba.resetLocation();
        long i = Encoder.decodeInt64(ba);
        Assert.assertEquals(l,i);
        ba.resetLocation();
        Encoder.encodeInt64(null,ba);
        ba.resetLocation();
        i = Encoder.decodeInt64(ba);
        Assert.assertEquals(0,i);

    }

    @Test
    public void testShort(){
        BytesArray ba = new BytesArray(0);
        short s = 44;
        Encoder.encodeShort(s,ba);
        ba.resetLocation();
        short i = Encoder.decodeShort(ba);
        Assert.assertEquals(s,i);
        ba.resetLocation();
        Encoder.encodeShort(null,ba);
        ba.resetLocation();
        i = Encoder.decodeShort(ba);
        Assert.assertEquals(0,i);
    }

    @Test
    public void testGetLocalIP(){
        String str = Encoder.getLocalIPAddress();
        Assert.assertNotNull(str);
    }

    @Test
    public void testEncodeList(){
        List<String> list = new ArrayList<>(1);
        list.add("Hello");
        BytesArray ba = new BytesArray(0);
        Encoder.encodeList(list,ba);
        ba.resetLocation();
        List l = Encoder.decodeList(ba);
        Assert.assertEquals(list.size(),l.size());
        Assert.assertEquals(list.get(0),l.get(0));
        ba.resetLocation();
        Encoder.encodeList(null,ba);
        ba.resetLocation();
        l = Encoder.decodeList(ba);
        Assert.assertNull(l);

    }

    @Test
    public void testEncodeArray(){
        String[] arr = new String[1];
        arr[0] ="Hello";
        BytesArray ba = new BytesArray(0);
        Encoder.encodeArray(arr,ba);
        ba.resetLocation();
        String[] a = (String[])Encoder.decodeArray(ba);
        Assert.assertEquals(arr.length,a.length);
        Assert.assertEquals(arr[0],a[0]);
        ba.resetLocation();
        Encoder.encodeArray(null,ba);
        ba.resetLocation();
        a = (String[])Encoder.decodeArray(ba);
        Assert.assertNull(a);
    }

    @Test
    public void testEncodeObject(){
        String obj = "Test";
        BytesArray ba = new BytesArray(0);
        Encoder.encodeObject(obj,ba);
        ba.resetLocation();
        Object a = Encoder.decodeObject(ba);
        Assert.assertEquals(obj,a);
        ba.resetLocation();
        Encoder.encodeObject(null,ba);
        ba.resetLocation();
        a = Encoder.decodeObject(ba);
        Assert.assertNull(a);
    }

    @Test
    public void testEncodeIntArray(){
        int[] arr = new int[1];
        arr[0] = 567;
        BytesArray ba = new BytesArray(0);
        Encoder.encodeIntArray(arr,ba);
        ba.resetLocation();
        int[] a = Encoder.decodeIntArray(ba);
        Assert.assertEquals(arr.length,a.length);
        Assert.assertEquals(arr[0],a[0]);
        ba.resetLocation();
        Encoder.encodeIntArray(null,ba);
        ba.resetLocation();
        a = Encoder.decodeIntArray(ba);
        Assert.assertNull(a);
    }

    @Test
    public void testEncodeByteArray(){
        byte[] arr = new byte[1];
        arr[0] = (byte)189;
        BytesArray ba = new BytesArray(0);
        Encoder.encodeByteArray(arr,ba);
        ba.resetLocation();
        byte[] a = Encoder.decodeByteArray(ba);
        Assert.assertEquals(arr.length,a.length);
        Assert.assertEquals(arr[0],a[0]);
        ba.resetLocation();
        Encoder.encodeByteArray(null,ba);
        ba.resetLocation();
        a = Encoder.decodeByteArray(ba);
        Assert.assertNull(a);
    }

}
