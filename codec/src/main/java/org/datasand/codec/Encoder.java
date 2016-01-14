/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.codec.serialize.SerializerGenerator;
import org.datasand.codec.serialize.SerializersManager;
import org.datasand.codec.serialize.StringSerializer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Encoder {

    public static final byte[] NULL = new byte[] {'-','N','U','L','L','-'};
    private static final SerializersManager serializersManager = new SerializersManager();

    static {
        registerSerializer(String.class, new StringSerializer());
    }

    public static final void registerSerializer(Class<?> cls,ISerializer serializer){
        serializersManager.registerSerializer(cls,serializer);
    }

    public static final ISerializer getSerializerByClass(Class<?> cls){
        ISerializer serializer =  serializersManager.getSerializerByClass(cls);
        if(serializer==null){
            VTable table = VSchema.instance.getVTable(cls);
            if(table!=null){
                try {
                    serializer = SerializerGenerator.generateSerializer(table);
                    serializersManager.registerSerializer(cls,serializer);
                } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                    VLogger.error("Failed to create a serializer to class "+cls.getName(),e);
                }
            }
        }
        return serializer;
    }

    public static final Class<?> getClassByMD5(MD5ID id){
        return serializersManager.getClassByMD5(id);
    }

    //Object
    public static final void encodeObject(Object value,BytesArray ba){
        if(ba instanceof HierarchyBytesArray && ba.getLocation()!=0){
            HierarchyBytesArray hba = (HierarchyBytesArray)ba;
            ba = hba.addNewChild();
        }
        if(value==null){
            encodeNULL(ba);
        }else{
            ba.adjustSize(16);
            MD5ID id = serializersManager.getMD5ByObject(value);
            encodeInt64(id.getMd5Long1(),ba);
            encodeInt64(id.getMd5Long2(),ba);
            if(ba instanceof HierarchyBytesArray){
                ((HierarchyBytesArray)ba).setJavaTypeMD5(id);
            }
            ISerializer serializer = serializersManager.getSerializerByMD5(id);
            serializer.encode(value,ba);
        }
    }

    public static final Object decodeObject(BytesArray ba){
        if(ba instanceof HierarchyBytesArray && ba.getLocation()>0){
            HierarchyBytesArray hba = (HierarchyBytesArray)ba;
            ba = hba.nextChild();
        }
        if(isNULL(ba)){
            return null;
        }else{
            if(ba.getBytes().length==0){
                return null;
            }
            try {
                long a = decodeInt64(ba);
                long b = decodeInt64(ba);
                ISerializer serializer = serializersManager.getSerializerByLongs(a, b);
                return serializer.decode(ba);
            }catch (Exception err){
                err.printStackTrace();
                return null;
            }
        }
    }

    //INT16
    public static final void encodeInt16(int value, byte[] byteArray,int location) {
        byteArray[location + 1] = (byte) (value >> 8);
        byteArray[location] = (byte) (value);
    }

    public static final int decodeInt16(byte[] byteArray, int location) {
        int value = 0;
        if (byteArray[location] > 0) {
            value += ((int) byteArray[location] & 0xFFL);
        } else {
            value += ((int) (256 + byteArray[location]) & 0xFFL);
        }

        if (byteArray[location + 1] > 0) {
            value += ((int) byteArray[location + 1] & 0xFFL) << (8);
        } else {
            value += ((int) (256 + byteArray[location + 1]) & 0xFFL) << (8);
        }
        return value;
    }

    public static final void encodeInt16(int value, BytesArray ba) {
        ba.adjustSize(2);
        encodeInt16(value, ba.getBytes(), ba.getLocation());
        ba.advance(2);
    }

    public static final int decodeInt16(BytesArray ba) {
        int value = decodeInt16(ba.getBytes(), ba.getLocation());
        ba.advance(2);
        return value;
    }

    //INT32
    public static final void encodeInt32(int value, byte byteArray[],int location) {
        byteArray[location] = (byte) ((value >> 24) & 0xff);
        byteArray[location + 1] = (byte) ((value >> 16) & 0xff);
        byteArray[location + 2] = (byte) ((value >> 8) & 0xff);
        byteArray[location + 3] = (byte) ((value) & 0xff);
    }

    public static final int decodeInt32(byte[] byteArray, int location) {
        return (int) (0xff & byteArray[location]) << 24
                | (int) (0xff & byteArray[location + 1]) << 16
                | (int) (0xff & byteArray[location + 2]) << 8
                | (int) (0xff & byteArray[location + 3]);
    }

    public static final void encodeInt32(Integer value, BytesArray ba) {
        if(value==null) value = 0;
        ba.adjustSize(4);
        encodeInt32(value, ba.getBytes(), ba.getLocation());
        ba.advance(4);
    }

    public static final int decodeInt32(BytesArray ba) {
        int value = decodeInt32(ba.getBytes(), ba.getLocation());
        ba.advance(4);
        return value;
    }

    //INT64 - long
    public static final void encodeInt64(long value, byte[] byteArray,int location) {
        byteArray[location] = (byte) ((value >> 56) & 0xff);
        byteArray[location + 1] = (byte) ((value >> 48) & 0xff);
        byteArray[location + 2] = (byte) ((value >> 40) & 0xff);
        byteArray[location + 3] = (byte) ((value >> 32) & 0xff);
        byteArray[location + 4] = (byte) ((value >> 24) & 0xff);
        byteArray[location + 5] = (byte) ((value >> 16) & 0xff);
        byteArray[location + 6] = (byte) ((value >> 8) & 0xff);
        byteArray[location + 7] = (byte) ((value) & 0xff);
    }

    public static final long decodeInt64(byte[] byteArray, int location) {
        return (long) (0xff & byteArray[location]) << 56
                | (long) (0xff & byteArray[location + 1]) << 48
                | (long) (0xff & byteArray[location + 2]) << 40
                | (long) (0xff & byteArray[location + 3]) << 32
                | (long) (0xff & byteArray[location + 4]) << 24
                | (long) (0xff & byteArray[location + 5]) << 16
                | (long) (0xff & byteArray[location + 6]) << 8
                | (long) (0xff & byteArray[location + 7]);
    }

    public static final void encodeInt64(Long value, BytesArray ba) {
        if(value==null)
            value = 0L;
        ba.adjustSize(8);
        encodeInt64(value, ba.getBytes(), ba.getLocation());
        ba.advance(8);
    }

    public static final long decodeInt64(BytesArray ba) {
        long value = decodeInt64(ba.getBytes(), ba.getLocation());
        ba.advance(8);
        return value;
    }

    //NULL
    public static final void encodeNULL(BytesArray ba) {
        ba.adjustSize(NULL.length);
        System.arraycopy(NULL,0,ba.getBytes(),ba.getLocation(),NULL.length);
        ba.advance(NULL.length);
    }

    public static final boolean isNULL(BytesArray ba) {
        if(ba.getBytes().length<ba.getLocation()+NULL.length){
            return false;
        }
        for(int i=0;i<NULL.length;i++){
            if(ba.getBytes()[ba.getLocation()+i]!=NULL[i]) {
                return false;
            }
        }
        ba.advance(NULL.length);
        return true;
    }

    //String
    public static final void encodeString(String value, byte[] byteArray, int location) {
        byte bytes[] = value.getBytes();
        encodeInt16(bytes.length, byteArray, location);
        System.arraycopy(bytes, 0, byteArray, location + 2, bytes.length);
    }

    public static final String decodeString(byte[] byteArray, int location) {
        int size = decodeInt16(byteArray, location);
        return new String(byteArray, location + 2, size);
    }

    public static final void encodeString(String value, BytesArray ba) {
        if (value == null) {
            encodeNULL(ba);
        } else {
            int size = value.getBytes().length+2;
            ba.adjustSize(size);
            encodeString(value,ba.getBytes(),ba.getLocation());
            ba.advance(size);
        }
    }

    public static final String decodeString(BytesArray ba) {
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt16(ba.getBytes(), ba.getLocation());
        String result = new String(ba.getBytes(), ba.getLocation() + 2, size);
        ba.advance(size + 2);
        return result;
    }

    //Short
    public static final void encodeShort(short value, byte[] byteArray,int location) {
        byteArray[location + 1] = (byte) (value >> 8);
        byteArray[location] = (byte) (value);
    }

    public static final short decodeShort(byte[] byteArray, int location) {
        short value = 0;
        if (byteArray[location] > 0) {
            value += ((int) byteArray[location] & 0xFFL);
        } else {
            value += ((int) (256 + byteArray[location]) & 0xFFL);
        }

        if (byteArray[location + 1] > 0) {
            value += ((int) byteArray[location + 1] & 0xFFL) << (8 * (1));
        } else {
            value += ((int) (256 + byteArray[location + 1]) & 0xFFL) << (8 * (1));
        }
        return value;
    }

    public static final void encodeShort(Short value, BytesArray ba) {
        if(value==null) value = 0;
        ba.adjustSize(2);
        encodeShort(value, ba.getBytes(), ba.getLocation());
        ba.advance(2);
    }

    public static final short decodeShort(BytesArray ba) {
        short value = decodeShort(ba.getBytes(), ba.getLocation());
        ba.advance(2);
        return value;
    }

    //Byte Array
    public static final void encodeByteArray(byte[] value, byte byteArray[],int location) {
        encodeInt32(value.length, byteArray, location);
        System.arraycopy(value, 0, byteArray, location + 4, value.length);
    }

    public static final byte[] decodeByteArray(byte[] byteArray, int location) {
        int size = decodeInt32(byteArray, location);
        byte array[] = new byte[size];
        System.arraycopy(byteArray, location + 4, array, 0, array.length);
        return array;
    }

    public static final void encodeByteArray(byte[] value, BytesArray ba) {
        if (value == null) {
            encodeNULL(ba);
        } else {
            ba.adjustSize(value.length + 4);
            encodeByteArray(value, ba.getBytes(), ba.getLocation());
            ba.advance(value.length + 4);
        }
    }

    public static final byte[] decodeByteArray(BytesArray ba) {
        if (isNULL(ba)) {
            return null;
        }
        byte[] array = decodeByteArray(ba.getBytes(), ba.getLocation());
        ba.advance(array.length + 4);
        return array;
    }

    //Boolean
    public static final void encodeBoolean(boolean value, byte[] byteArray,int location) {
        if (value)
            byteArray[location] = 1;
        else
            byteArray[location] = 0;
    }

    public static final boolean decodeBoolean(byte[] byteArray, int location) {
        if (byteArray[location] == 1)
            return true;
        else
            return false;
    }

    public static final void encodeBoolean(Boolean value, BytesArray ba) {
        if(value==null) value = false;
        ba.adjustSize(1);
        encodeBoolean(value, ba.getBytes(), ba.getLocation());
        ba.advance(1);
    }

    public static final Boolean decodeBoolean(BytesArray ba) {
        boolean result = decodeBoolean(ba.getBytes(), ba.getLocation());
        ba.advance(1);
        return result;
    }

    //Byte
    public static final void encodeByte(Byte value, BytesArray ba) {
        if(value==null) value = (byte)0;
        ba.adjustSize(1);
        ba.getBytes()[ba.getLocation()] = value;
        ba.advance(1);
    }

    public static final byte decodeByte(BytesArray ba){
        ba.advance(1);
        return ba.getBytes()[ba.getLocation()-1];
    }

    //Int array
    public static final void encodeIntArray(int value[], BytesArray ba) {
        if (value == null) {
            encodeNULL(ba);
        } else {
            encodeInt32(value.length, ba);
            for (int i = 0; i < value.length; i++) {
                encodeInt32(value[i], ba);
            }
        }
    }

    public static final int[] decodeIntArray(BytesArray ba) {
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt32(ba);
        int result[] = new int[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = decodeInt32(ba);
        }
        return result;
    }

    //Array of objects
    public static final void encodeArray(Object value[], BytesArray ba) {
        if (value == null) {
            encodeNULL(ba);
        } else {
            encodeInt32(value.length, ba);
            MD5ID id = serializersManager.getMD5ByClass(value.getClass().getComponentType());
            encodeInt64(id.getMd5Long1(),ba);
            encodeInt64(id.getMd5Long2(),ba);
            ISerializer serializer = serializersManager.getSerializerByMD5(id);
            for (int i = 0; i < value.length; i++) {
                serializer.encode(value[i],ba);
            }
        }
    }

    public static final Object[] decodeArray(BytesArray ba) {
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt32(ba);
        long a = decodeInt64(ba);
        long b = decodeInt64(ba);
        MD5ID id = MD5ID.create(a,b);
        ISerializer serializer = serializersManager.getSerializerByMD5(id);
        Class cls = serializersManager.getClassByMD5(id);
        Object result[] = (Object[]) Array.newInstance(cls, size);
        for (int i = 0; i < result.length; i++) {
            result[i] = serializer.decode(ba);
        }
        return result;
    }

    //List
    public static final void encodeList(List<?> list, BytesArray ba) {
        if(ba instanceof HierarchyBytesArray && ba.getLocation()!=0){
            HierarchyBytesArray hba = (HierarchyBytesArray)ba;
            ba = hba.addNewChild();
        }
        if (list == null || list.size()==0) {
            encodeNULL(ba);
        } else {
            encodeInt32(list.size(), ba);
            MD5ID id = serializersManager.getMD5ByClass(list.get(0).getClass());
            encodeInt64(id.getMd5Long1(),ba);
            encodeInt64(id.getMd5Long2(),ba);
            if(ba instanceof HierarchyBytesArray){
                ((HierarchyBytesArray)ba).setJavaTypeMD5(id);
            }
            ISerializer serializer = serializersManager.getSerializerByMD5(id);
            for (Object o: list) {
                serializer.encode(o,ba);
            }
        }
    }

    public static final List<?> decodeList(BytesArray ba) {
        if(ba instanceof HierarchyBytesArray && ba.getLocation()>0){
            HierarchyBytesArray hba = (HierarchyBytesArray)ba;
            ba = hba.nextChild();
        }
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt32(ba);
        long a = decodeInt64(ba);
        long b = decodeInt64(ba);
        MD5ID id = MD5ID.create(a,b);
        ISerializer serializer = serializersManager.getSerializerByMD5(id);
        List result = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            result.add(serializer.decode(ba));
        }
        return result;
    }


    /*

    public int decodeUInt32(byte[] byteArray, int location) {
        int value = 0;
        if (byteArray[location] > 0) {
            value += ((int) byteArray[location] & 0xFFL);
        } else {
            value += ((int) (256 + byteArray[location]) & 0xFFL);
        }

        if (byteArray[location + 1] > 0) {
            value += ((int) byteArray[location + 1] & 0xFFL) << (8 * (1));
        } else {
            value += ((int) (256 + byteArray[location + 1]) & 0xFFL) << (8 * (1));
        }

        if (byteArray[location + 2] > 0) {
            value += ((int) byteArray[location + 2] & 0xFFL) << (8 * (2));
        } else {
            value += ((int) (256 + byteArray[location + 2]) & 0xFFL) << (8 * (2));
        }

        if (byteArray[location + 3] > 0) {
            value += ((int) byteArray[location + 3] & 0xFFL) << (8 * (3));
        } else {
            value += ((int) (256 + byteArray[location + 3]) & 0xFFL) << (8 * (3));
        }
        return value;
    }



    public void encodeBigDecimal(BigDecimal value, EncodeDataContainer ba) {
        if(value==null){
            encodeNULL(ba);
            return;
        }
        encodeString(value.toString(), ba);
        /* The method Double.doubleToLongBits is EXTREMELY slow! Need
         * To find other solution to that
         *
        long longValue = Double.doubleToLongBits(value.doubleValue());
        ba.adjustSize(8);
        encodeInt64(longValue, ba.getBytes(), ba.getLocation());
        ba.advance(8);

    }

    public BigDecimal decodeBigDecimal(EncodeDataContainer ba){
        if(isNULL(ba))
            return null;
        return new BigDecimal(decodeString(ba));
        /*
        long longValue = decodeInt64(ba);
        return new BigDecimal(Double.longBitsToDouble(longValue));

    }

    public long decodeUInt64(byte[] byteArray, int location) {
        long value = 0;
        if (byteArray[location] > 0) {
            value += ((long) byteArray[location] & 0xFFL);
        } else {
            value += ((long) (256 + byteArray[location]) & 0xFFL);
        }

        if (byteArray[location + 1] > 0) {
            value += ((long) byteArray[location + 1] & 0xFFL) << (8 * (1));
        } else {
            value += ((long) (256 + byteArray[location + 1]) & 0xFFL) << (8 * (1));
        }

        if (byteArray[location + 2] > 0) {
            value += ((long) byteArray[location + 2] & 0xFFL) << (8 * (2));
        } else {
            value += ((long) (256 + byteArray[location + 2]) & 0xFFL) << (8 * (2));
        }

        if (byteArray[location + 3] > 0) {
            value += ((long) byteArray[location + 3] & 0xFFL) << (8 * (3));
        } else {
            value += ((long) (256 + byteArray[location + 3]) & 0xFFL) << (8 * (3));
        }

        if (byteArray[location + 4] > 0) {
            value += ((long) byteArray[location + 4] & 0xFFL) << (8 * (4));
        } else {
            value += ((long) (256 + byteArray[location + 4]) & 0xFFL) << (8 * (4));
        }

        if (byteArray[location + 5] > 0) {
            value += ((long) byteArray[location + 5] & 0xFFL) << (8 * (5));
        } else {
            value += ((long) (256 + byteArray[location + 5]) & 0xFFL) << (8 * (5));
        }

        if (byteArray[location + 6] > 0) {
            value += ((long) byteArray[location + 6] & 0xFFL) << (8 * (6));
        } else {
            value += ((long) (256 + byteArray[location + 6]) & 0xFFL) << (8 * (6));
        }

        if (byteArray[location + 7] > 0) {
            value += ((long) byteArray[location + 7] & 0xFFL) << (8 * (7));
        } else {
            value += ((long) (256 + byteArray[location + 7]) & 0xFFL) << (8 * (7));
        }

        return value;
    }










    public void encodeAugmentations(Object value, EncodeDataContainer ba) {
        IAugmetationObserver ao = ba.getTypeDescriptorContainer().getAugmentationObserver();
        if(ao!=null){
            ao.encodeAugmentations(value, ba);
        }
    }

    public void decodeAugmentations(Object builder, EncodeDataContainer ba,Class<?> augmentedClass) {
        IAugmetationObserver ao = ba.getTypeDescriptorContainer().getAugmentationObserver();
        if(ao!=null){
            ao.decodeAugmentations(builder, ba, augmentedClass);
        }
    }


    /*
    public void encodeAndAddObject(Object value, EncodeDataContainer _ba,Class<?> objectType) {
        BytesArray ba = (BytesArray)_ba;
        if (value == null) {
            encodeNULL(ba);
        } else {
            VTable tbl = ba.getTypeDescriptorContainer().getTypeDescriptorByClass(objectType);
            int classCode = tbl.getClassCode();
            encodeInt16(classCode,ba);
            EncodeDataContainer subBA = new BytesArray(1024,ba.getTypeDescriptorContainer().getTypeDescriptorByCode(classCode));
            ISerializer serializer = getSerializer(objectType,ba.getTypeDescriptorContainer());
            if (serializer != null) {
                encodeInt16(classCode, subBA);
                serializer.encode(value, subBA);
                ba.addSubElementData(classCode, subBA,tbl.getMD5IDForObject(value));
            } else {
                System.err.println("Can't find a serializer for " + objectType);
            }
        }
    }

    public void encodeObject(Object value, EncodeDataContainer ba,Class<?> objectType) {
        if(value instanceof String){
            encodeInt16(FrameworkClassCodes.CODE_String,ba);
            encodeString((String)value, ba);
            return;
        }else
        if(value instanceof Integer){
            encodeInt16(FrameworkClassCodes.CODE_Integer,ba);
            encodeInt32((Integer)value,ba);
            return;
        }else
        if(value instanceof Long){
            encodeInt16(FrameworkClassCodes.CODE_Long,ba);
            encodeInt64((Long)value,ba);
            return;
        }else
        if(value instanceof List){
            encodeInt16(FrameworkClassCodes.CODE_List, ba);
            encodeList((List<?>)value, ba);
            return;
        }

        if (value == null) {
            encodeNULL(ba);
            return;
        }

        if(objectType==null)
            objectType = value.getClass();

        ISerializer serializer = getSerializer(objectType,ba.getTypeDescriptorContainer());
        if (serializer != null) {
            Integer classCode = registeredSerializersClassCode.get(objectType);
            if(classCode==null){
                classCode = ba.getTypeDescriptorContainer().getTypeDescriptorByClass(objectType).getClassCode();
            }
            if(classCode==null){
                System.err.println("Unable To find a code for this class:"+objectType.getName());
                return;
            }
            encodeInt16(classCode, ba);
            serializer.encode(value, ba);
        } else {
            System.err.println("Can't find a serializer for " + objectType);
        }
    }*/

    /*
    public Object decodeObject(EncodeDataContainer ba) {
        if (isNULL(ba)) {
            return null;
        }
        int classCode = decodeInt16(ba);
        if(classCode==FrameworkClassCodes.CODE_String){
            return decodeString(ba);
        }else
        if(classCode==FrameworkClassCodes.CODE_Integer){
            return decodeInt32(ba);
        }else
        if(classCode==FrameworkClassCodes.CODE_Long){
            return decodeInt64(ba);
        }else
        if(classCode==FrameworkClassCodes.CODE_List){
            return decodeList(ba);
        }

        ISerializer serializer = getSerializer(classCode,ba.getTypeDescriptorContainer());
        if (serializer == null) {
        	VTable ts = ba.getTypeDescriptorContainer().getTypeDescriptorByCode(classCode);
        	if(ts!=null)
        		ts.getJavaClassType();
        	else
        		System.err.println("Missing class code="+classCode);
        }
        if (serializer != null) {
            return serializer.decode(ba, 0);
        } else {
            System.err.println("Missing class code=" + classCode);
        }
        return null;
    }*/

    /*
    public Object decodeAndObject(EncodeDataContainer _ba) {
        BytesArray ba = (BytesArray)_ba;
        if (isNULL(ba)) {
            return null;
        }
        int classCode = decodeInt16(ba);
        ISerializer serializer = getSerializer(classCode,ba.getTypeDescriptorContainer());
        if (serializer == null) {
            ba.getTypeDescriptorContainer().getTypeDescriptorByCode(classCode).getJavaClassType();
        }
        if (serializer != null) {
            if(ba.getSubElementsData().get(classCode)!=null){
                BytesArray subBA = (BytesArray)ba.getSubElementsData().get(classCode).get(0);
                subBA.advance(2);
                return serializer.decode(subBA, 0);
            }else
                return null;
        } else {
            System.err.println("Missing class code=" + classCode);
        }
        return null;
    }

    public void encodeArray(Object value[], EncodeDataContainer ba,Class<?> componentType) {
        if (value == null) {
            encodeNULL(ba);
        } else {
            encodeInt32(value.length, ba);
            for (int i = 0; i < value.length; i++) {
                if (String.class.equals(componentType)) {
                    encodeString((String) value[i], ba);
                } else if (ISerializer.class.isAssignableFrom(componentType)) {
                    ((ISerializer) value[i]).encode(value[i], ba);
                } else {
                    encodeObject(value[i], ba, componentType);
                }
            }
        }
    }

    public void encodeList(List<?> list, EncodeDataContainer ba) {
        if (list == null) {
            encodeNULL(ba);
        } else {
            encodeInt32(list.size(), ba);
            for (Object o: list) {
                encodeObject(o, ba, null);
            }
        }
    }


    public void encodeAndAddList(List<?> list, EncodeDataContainer ba,Class<?> componentType) {
        if (list == null) {
            encodeNULL(ba);
        } else {
            encodeInt32(list.size(), ba);
            int componentCode = ba.getTypeDescriptorContainer().getTypeDescriptorByClass(componentType).getClassCode();
            for (Object o: list) {
                BytesArray subBA = new BytesArray(1024,ba.getTypeDescriptorContainer().getTypeDescriptorByCode(componentCode));
                if (String.class.equals(componentType)) {
                    encodeString((String) o, subBA);
                } else if (ISerializer.class.isAssignableFrom(componentType)) {
                    ((ISerializer) o).encode(o, subBA);
                } else {
                    encodeObject(o, subBA, componentType);
                }
                ba.addSubElementData(componentCode, subBA,o);
            }
        }
    }

    public List<Object> decodeList(EncodeDataContainer ba,Class<?> componentType) {
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt32(ba);
        List<Object> result = new ArrayList<Object>(size);
        for (int i = 0; i < size; i++) {
            if (int.class.equals(componentType)) {
                result.add(decodeInt32(ba));
            } else if (long.class.equals(componentType)) {
                result.add(decodeInt64(ba));
            } else if (boolean.class.equals(componentType)) {
                result.add(decodeBoolean(ba));
            } else if (String.class.equals(componentType)) {
                result.add(decodeString(ba));
            } else if (ISerializer.class.isAssignableFrom(componentType)) {
                ISerializer serializer = getSerializer(componentType,ba.getTypeDescriptorContainer());
                if (serializer != null) {
                    result.add(serializer.decode(ba, 0));
                } else {
                    System.err.println("Can't find Serializer for class:"
                            + componentType.getName());
                }
            } else {
                result.add(decodeObject(ba));
            }
        }
        return result;
    }

    public List decodeAndList(EncodeDataContainer ba,Class<?> componentType) {
        if (isNULL(ba)) {
            return null;
        }
        int size = decodeInt32(ba);
        List result = new ArrayList(size);
        List<EncodeDataContainer> subElementData = ba.getSubElementsData().get(ba.getTypeDescriptorContainer().getTypeDescriptorByClass(componentType).getClassCode());
        if(subElementData!=null){
            for (int i = 0; i < subElementData.size(); i++) {
                EncodeDataContainer subBA = subElementData.get(i);
                if (int.class.equals(componentType)) {
                    result.add(decodeInt32(subBA));
                } else if (long.class.equals(componentType)) {
                    result.add(decodeInt64(subBA));
                } else if (boolean.class.equals(componentType)) {
                    result.add(decodeBoolean(subBA));
                } else if (String.class.equals(componentType)) {
                    result.add(decodeString(subBA));
                } else if (ISerializer.class.isAssignableFrom(componentType)) {
                    ISerializer serializer = getSerializer(componentType,ba.getTypeDescriptorContainer());
                    if (serializer != null) {
                        result.add(serializer.decode(subBA, 0));
                    } else {
                        System.err.println("Can't find Serializer for class:"
                                + componentType.getName());
                    }
                } else {
                    result.add(decodeObject(subBA));
                }
            }
        }
        if(result.size()==0)
            return null;
        return result;
    }*/

    public static String getLocalIPAddress() {
        try {
            for (NetworkInterface in : Collections.list(NetworkInterface
                    .getNetworkInterfaces())) {
                if (in.isLoopback())
                    continue;
                if (!in.isUp())
                    continue;
                if (in.isVirtual())
                    continue;
                if (in.getDisplayName().indexOf("vm") != -1)
                    continue;
                for (InterfaceAddress addr : in.getInterfaceAddresses()) {
                    if (addr.getAddress() instanceof Inet6Address)
                        continue;
                    if (!addr.getAddress().getHostAddress().startsWith("127.0")) {
                        return addr.getAddress().getHostAddress();
                    }
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
