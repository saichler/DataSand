/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.serialize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.datasand.codec.Observers;
import org.datasand.codec.VColumn;
import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SerializerGenerator {

    public static final void append(String text, int level, StringBuilder buff) {
        for (int i = 0; i < level; i++) {
            buff.append("    ");
        }
        buff.append(text);
        buff.append("\n");
    }

    public static String replaceAll(String src, String that, String withThis) {
        StringBuilder buff = new StringBuilder();
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

    public static ISerializer generateSerializer(VTable vTable) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        String clsTxt = generateSerializerText(vTable);
        String filePath = "./serializers/"+replaceAll(vTable.getJavaClassType().getPackage().getName(),".","/");
        File dir = new File(filePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String serializerFileName = dir.getAbsolutePath()+"/"+vTable.getJavaClassType().getSimpleName()+"Serializer.java";
        File f = new File(serializerFileName);
        FileOutputStream out = new FileOutputStream(f);
        out.write(clsTxt.getBytes());
        out.close();
        return compileSerializer(f,vTable);
    }

    private static String generateSerializerText(VTable vTable){
        StringBuilder buff = new StringBuilder();

        append("", 0, buff);
        append("/**\n  * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.\n  * Generated Code! Do Not Edit unless you move the java file. \n**/",0,buff);
        append("package "+vTable.getJavaClassType().getPackage().getName()+";", 0,buff);
        append("import java.util.List;", 0, buff);
        append("import org.datasand.codec.BytesArray;", 0, buff);
        append("import org.datasand.codec.Encoder;", 0, buff);
        append("import org.datasand.codec.serialize.ISerializer;", 0, buff);
        for(VTable c:vTable.getChildren()){
            append("import "+ c.getJavaClassType().getName()+";", 0, buff);
        }
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
        append("public void encode(Object value, BytesArray ba) {", 4, buff);
        append(vTable.getJavaClassType().getSimpleName() + " element = ("+ vTable.getJavaClassType().getSimpleName() + ") value;", 8, buff);
        for (VColumn column : vTable.getColumns()) {
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
            append("Encoder.encodeAugmentations(value, ba);", 8, buff);
        }

        if (Observers.instance.isChildAttribute(vTable)) {
            for (VTable child : vTable.getChildren()) {
                if(child.getVColumn().isCollection()){
                    append("Encoder.encodeList(element."+child.getVColumn().getJavaGetMethodName()+"(), ba);",8,buff);
                }else{
                    append("Encoder.encodeObject(element."+child.getVColumn().getJavaGetMethodName()+"(), ba);",8,buff);
                }
            }
        }

        append("}", 4, buff);
        append("@Override", 4, buff);
        append("public Object decode(BytesArray ba) {", 4, buff);
        if (builderClass!=null) {
            append(builderClass.getSimpleName()+" builder = new "+ builderClass.getSimpleName()+"();", 8, buff);
            for (VColumn vColumn : vTable.getColumns()) {
                if (vColumn.getJavaMethodReturnType().equals(short.class) || vColumn.getJavaMethodReturnType().equals(Short.class)) {
                    append("builder.set" + vColumn.getvColumnName()+ "(Encoder.decodeShort(ba));", 8, buff);
                }else
                if (vColumn.getJavaMethodReturnType().equals(boolean.class) || vColumn.getJavaMethodReturnType().equals(Boolean.class)) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeBoolean(ba));", 8, buff);
                }else
                if (!vColumn.isCollection() && (vColumn.getJavaMethodReturnType().equals(byte.class)
                        || vColumn.getJavaMethodReturnType().equals(Byte.class))) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeByte(ba));", 8, buff);
                }else
                if (vColumn.isCollection() && (vColumn.getJavaMethodReturnType().equals(byte.class)
                        || vColumn.getJavaMethodReturnType().equals(Byte.class))) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeByteArray(ba));", 8, buff);
                }else
                if (vColumn.getJavaMethodReturnType().equals(BigDecimal.class)) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeBigDecimal(ba));", 8, buff);
                }else
                if (vColumn.getJavaMethodReturnType().equals(String.class)) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeString(ba));", 8, buff);
                } else if (vColumn.getJavaMethodReturnType().equals(int.class)
                        || vColumn.getJavaMethodReturnType().equals(Integer.class)) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeInt32(ba));", 8, buff);
                } else if (vColumn.getJavaMethodReturnType().equals(long.class)
                        || vColumn.getJavaMethodReturnType().equals(Long.class)) {
                    append("builder.set" + vColumn.getvColumnName()
                            + "(Encoder.decodeInt64(ba));", 8, buff);
                } else if (Observers.instance.isTypeAttribute(vColumn)) {
                    append("builder.set" + vColumn.getvColumnName() + "(("
                            + vColumn.getJavaMethodReturnType().getName()
                            + ")Encoder.decodeObject(ba));", 8, buff);
                }
            }

            if(Observers.instance.supportAugmentations(vTable)){
                append("Encoder.decodeAugmentations(builder, ba,"+ vTable.getJavaClassType().getSimpleName() + ".class);", 8,buff);
            }

            if (Observers.instance.isChildAttribute(vTable)) {
                for (VTable child : vTable.getChildren()) {
                    if(child.getVColumn().isCollection()){
                        append("builder.set"+child.getVColumn().getvColumnName()+"((List<"+child.getJavaClassType().getSimpleName()+">)Encoder.decodeList(ba));",8,buff);
                    }else{
                        append("builder.set"+child.getVColumn().getvColumnName()+"(("+child.getJavaClassType().getSimpleName()+")Encoder.decodeObject(ba));",8,buff);
                    }
                }
            }
            if(Observers.instance.getClassExtractor().getBuilderMethod(vTable)!=null){
                append("return builder."+Observers.instance.getClassExtractor().getBuilderMethod(vTable)+";", 8, buff);
            }else{
                append("return builder;", 8, buff);
            }
        } else {
            if(!vTable.getJavaClassType().isEnum()){
                append(vTable.getJavaClassType().getSimpleName()
                        + " instance = new "
                        + vTable.getJavaClassType().getSimpleName()+"(",8,buff);
            }
            boolean first = true;
            for (VColumn vColumn : vTable.getColumns()) {
                if (vColumn.getJavaMethodReturnType().equals(boolean.class) || vColumn.getJavaMethodReturnType().equals(Boolean.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeBoolean(ba)", 8, buff);
                }else
                if (vColumn.getJavaMethodReturnType().equals(String.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeString(ba)", 8, buff);
                } else if (vColumn.getJavaMethodReturnType().equals(int.class) || vColumn.getJavaMethodReturnType().equals(Integer.class)) {
                    if (vTable.getJavaClassType().isEnum()) {
                        append(vTable.getJavaClassType().getSimpleName()
                                        + " instance = "
                                        + vTable.getJavaClassType().getSimpleName()
                                        + ".forValue(Encoder.decodeInt32(ba));",
                                8, buff);
                    } else{
                        if(!first)
                            append(",",12,buff);
                        first = false;
                        append("Encoder.decodeInt32(ba)", 8, buff);
                    }
                } else if (vColumn.getJavaMethodReturnType().equals(long.class) || vColumn.getJavaMethodReturnType().equals(Long.class)) {
                    if(!first)
                        append(",",12,buff);
                    first = false;
                    append("Encoder.decodeInt64(ba)", 8, buff);
                }
            }
            if(!vTable.getJavaClassType().isEnum()){
                append(");",8,buff);
            }
            append("return instance;", 8, buff);
        }

        append("}", 4, buff);
        append("}", 0, buff);
        return buff.toString();
    }

    public static ISerializer compileSerializer(File javaFile,VTable vTable) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, javaFile.getPath());
        URLClassLoader cl = new URLClassLoader(new URL[]{ new File("./serializers").toURI().toURL() });

        Class<?> serializerClass = Class.forName(vTable.getJavaClassType().getName()+ "Serializer",true,cl);
        ISerializer serializer = (ISerializer) serializerClass.newInstance();
        cl.close();
        return serializer;
    }

}
