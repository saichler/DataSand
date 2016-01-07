/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.serialize;

import java.math.BigDecimal;
import org.datasand.codec.bytearray.Observers;
import org.datasand.codec.bytearray.VColumn;
import org.datasand.codec.bytearray.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SerializerGenerator {

    private static final void append(String text, int level, StringBuffer buff) {
        for (int i = 0; i < level; i++) {
            buff.append(" ");
        }
        buff.append(text);
        buff.append("\n");
    }

    public static String replaceAll(String src, String that, String withThis) {
        StringBuffer buff = new StringBuffer();
        int index0 = 0;
        int index1 = src.indexOf(that);
        if (index1 == -1)
            return src;
        while (index1 != -1) {
            buff.append(src.substring(index0, index1));
            buff.append(withThis);
            index0 = index1 + that.length();
            index1 = src.indexOf(that, index0);
        }
        buff.append(src.substring(index0));
        return buff.toString();
    }

    public String generateSerializer(VTable vTable){
        StringBuffer buff = new StringBuffer();

        append("", 0, buff);
        append("package " + vTable.getJavaClassType().getPackage().getName() + ";", 0,buff);
        append("import org.datasand.codec.EncodeDataContainer;", 0, buff);
        append("import org.datasand.codec.serialize.ISerializer;", 0, buff);
        String className = vTable.getJavaClassType().getName();
        className = replaceAll(className, "$", ".");
        append("import " + className + ";", 0, buff);
        Class<?> builderClass = Observers.instance.getClassExtractor().getBuilderClass(vTable);
        if (builderClass!=null && !builderClass.equals(vTable.getJavaClassType()))
            append("import " + builderClass.getName()+";", 0, buff);
        append("", 0, buff);
        String serializerName = vTable.getJavaClassType().getSimpleName();
        if(serializerName.indexOf("$")!=-1){
            serializerName = serializerName.substring(serializerName.indexOf("$")+1);
        }
        append("public class " + serializerName + "Serializer implements ISerializer{", 0, buff);
        append("@Override", 4, buff);
        append("public void encode(Object value, byte[] byteArray, int location) {",4, buff);
        append("}", 4, buff);
        append("", 0, buff);

        append("@Override", 4, buff);
        append("public void encode(Object value, EncodeDataContainer ba) {", 4, buff);
        append(vTable.getJavaClassType().getSimpleName() + " element = ("+ vTable.getJavaClassType().getSimpleName() + ") value;", 8, buff);
        for (VColumn column : vTable.getColumns()) {
            append("ba.setCurrentAttributeName(\""+column.getvColumnName()+"\");",8,buff);
            if (column.getJavaMethodReturnType().equals(short.class) || column.getJavaMethodReturnType().equals(Short.class)) {
                append("Encoder.encodeShort(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            } else
            if (column.getJavaMethodReturnType().equals(boolean.class) || column.getJavaMethodReturnType().equals(Boolean.class)) {
                append("Encoder.encodeBoolean(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            } else
            if (!column.isCollection() && (column.getJavaMethodReturnType().equals(byte.class) || column.getJavaMethodReturnType().equals(Byte.class))) {
                append("Encoder.encodeByte(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            }else
            if (column.isCollection() && (column.getJavaMethodReturnType().equals(byte.class) || column.getJavaMethodReturnType().equals(Byte.class))) {
                append("Encoder.encodeByteArray(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            }else
            if (column.getJavaMethodReturnType().equals(BigDecimal.class)) {
                append("Encoder.encodeBigDecimal(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            }else
            if (column.getJavaMethodReturnType().equals(String.class)) {
                append("Encoder.encodeString(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            } else if (column.getJavaMethodReturnType().equals(int.class) || column.getJavaMethodReturnType().equals(Integer.class)) {
                append("Encoder.encodeInt32(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            } else if (column.getJavaMethodReturnType().equals(long.class) || column.getJavaMethodReturnType().equals(Long.class)) {
                append("Encoder.encodeInt64(element." + column.getJavaGetMethodName() + "(), ba);", 8, buff);
            } else if (Observers.instance.isTypeAttribute(column)) {
                append("Encoder.encodeObject(element." + column.getJavaGetMethodName() + "(), ba, " + column.getJavaMethodReturnType().getName() + ".class);", 8, buff);
            }
        }

        if(Observers.instance.supportAugmentations(vTable)){
            append("ba.setCurrentAttributeName(\"Augmentations\");",8,buff);
            append("Encoder.encodeAugmentations(value, ba);", 8, buff);
        }

        if (Observers.instance.isChildAttribute(vTable)) {
            for (VColumn child : vTable.getChildren().keySet()) {
                TypeDescriptor subTable = container.getTypeDescriptorByClass(child.getReturnType());
                append("ba.setCurrentAttributeName(\""+child.getColumnName()+"\");",8,buff);
                if(child.isCollection()){
                    append("Encoder.encodeAndAddList(element."+child.getMethodName()+"(), ba,"+subTable.getTypeClassName()+".class);",8,buff);
                }else{
                    append("Encoder.encodeAndAddObject(element."+child.getMethodName()+"(), ba,"+subTable.getTypeClassName()+".class);",8,buff);
                }
            }
        }

        append("}", 4, buff);
        append("@Override", 4, buff);
        append("public Object decode(byte[] byteArray, int location, int length) {",4, buff);
        append("return null;", 8, buff);
        append("}", 4, buff);
        append("@Override", 4, buff);
        append("public Object decode(EncodeDataContainer ba, int length) {", 4, buff);
        if (builderClass!=null) {
            append(builderClass.getSimpleName()+" builder = new "+ builderClass.getSimpleName()+"();", 8, buff);
            for (AttributeDescriptor p : type.attributes) {
                append("ba.setCurrentAttributeName(\""+p.getColumnName()+"\");",8,buff);
                if (column.getJavaMethodReturnType().equals(short.class) || column.getJavaMethodReturnType().equals(Short.class)) {
                    append("builder.set" + p.getColumnName()+ "(Encoder.decodeShort(ba));", 8, buff);
                }else
                if (column.getJavaMethodReturnType().equals(boolean.class) || column.getJavaMethodReturnType().equals(Boolean.class)) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeBoolean(ba));", 8, buff);
                }else
                if (!p.isCollection() && (column.getJavaMethodReturnType().equals(byte.class)
                        || column.getJavaMethodReturnType().equals(Byte.class))) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeByte(ba));", 8, buff);
                }else
                if (p.isCollection() && (column.getJavaMethodReturnType().equals(byte.class)
                        || column.getJavaMethodReturnType().equals(Byte.class))) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeByteArray(ba));", 8, buff);
                }else
                if (column.getJavaMethodReturnType().equals(BigDecimal.class)) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeBigDecimal(ba));", 8, buff);
                }else
                if (column.getJavaMethodReturnType().equals(String.class)) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeString(ba));", 8, buff);
                } else if (column.getJavaMethodReturnType().equals(int.class)
                        || column.getJavaMethodReturnType().equals(Integer.class)) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeInt32(ba));", 8, buff);
                } else if (column.getJavaMethodReturnType().equals(long.class)
                        || column.getJavaMethodReturnType().equals(Long.class)) {
                    append("builder.set" + p.getColumnName()
                            + "(Encoder.decodeInt64(ba));", 8, buff);
                } else if (container.isTypeAttribute(p)) {
                    append("builder.set" + p.getColumnName() + "(("
                            + column.getJavaMethodReturnType().getName()
                            + ")Encoder.decodeObject(ba));", 8, buff);
                }
            }

            if(container.supportAugmentations(type)){
                append("ba.setCurrentAttributeName(\"Augmentations\");",8,buff);
                append("Encoder.decodeAugmentations(builder, ba,"+ type.getTypeClass().getSimpleName() + ".class);", 8,buff);
            }

            if (container.isChildAttribute(type)) {
                for (AttributeDescriptor child : type.getChildren().keySet()) {
                    TypeDescriptor subTable = container.getTypeDescriptorByClass(child.getReturnType());
                    append("ba.setCurrentAttributeName(\""+child.getColumnName()+"\");",8,buff);
                    if(child.isCollection()){
                        append("builder.set"+child.getColumnName()+"(Encoder.decodeAndList(ba,"+subTable.getTypeClassName()+".class));",8,buff);
                    }else{
                        append("builder.set"+child.getColumnName()+"(("+subTable.getTypeClassName()+")Encoder.decodeAndObject(ba));",8,buff);
                    }
                }
            }
            if(container.getClassExtractor().getBuilderMethod(type)!=null){
                append("return builder."+container.getClassExtractor().getBuilderMethod(type)+";", 8, buff);
            }else{
                append("return builder;", 8, buff);
            }
        } else {
            if(!type.getTypeClass().isEnum()){
                append(type.getTypeClass().getSimpleName()
                        + " instance = new "
                        + type.getTypeClass().getSimpleName()+"(",8,buff);
            }
            boolean first = true;
            for (AttributeDescriptor p : type.attributes) {
                if (column.getJavaMethodReturnType().equals(boolean.class) || column.getJavaMethodReturnType().equals(Boolean.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeBoolean(ba)", 8, buff);
                }else
                if (column.getJavaMethodReturnType().equals(String.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeString(ba)", 8, buff);
                } else if (column.getJavaMethodReturnType().equals(int.class) || column.getJavaMethodReturnType().equals(Integer.class)) {
                    if (type.getTypeClass().isEnum()) {
                        append(type.getTypeClass().getSimpleName()
                                        + " instance = "
                                        + type.getTypeClass().getSimpleName()
                                        + ".forValue(Encoder.decodeInt32(ba));",
                                8, buff);
                    } else{
                        if(!first)
                            append(",",12,buff);
                        first = false;
                        append("Encoder.decodeInt32(ba)", 8, buff);
                    }
                } else if (column.getJavaMethodReturnType().equals(long.class) || column.getJavaMethodReturnType().equals(Long.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeInt64(ba)", 8, buff);
                }
            }
            if(!type.getTypeClass().isEnum()){
                append(");",8,buff);
            }
            append("return instance;", 8, buff);
        }

        append("}", 4, buff);

        append("public String getShardName(Object obj) {", 4, buff);
        append("return \"Default\";", 8, buff);
        append("}", 4, buff);
        append("public String getRecordKey(Object obj) {", 4, buff);
        append("return null;", 8, buff);
        append("}", 4, buff);
        append("}", 0, buff);
        return buff.toString();
    }
}
