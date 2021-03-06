/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class VColumn {
    private static final Logger LOG = LoggerFactory.getLogger(VColumn.class);
    public static boolean IS_SERVER_SIDE = false;

    private final String javaGetMethodName;
    private final String javaClassName;
    private final String vColumnName;
    private final String vTableName;
    private Method javaGetMethod = null;
    private Class<?> javaClass = null;
    private Class<?> javaMethodReturnType = null;

    private String augmentedTableName = null;

    private boolean collection = false;

    private boolean isint = false;
    private boolean isboolean = false;
    private boolean islong = false;
    private boolean isbyte = false;
    private int charWidth = 10;

    public VColumn(String logicalColumnName, String logicalTableName,String javaGetMethodName, String javaClassName) {
        this.javaGetMethodName = javaGetMethodName;
        this.javaClassName = javaClassName;
        this.vColumnName = logicalColumnName;
        this.vTableName = logicalTableName;
    }

    public VColumn(Method getMethod, Class<?> clazz) {
        this.javaGetMethod = getMethod;
        this.javaClass = clazz;
        this.javaGetMethodName = this.javaGetMethod.getName();
        this.javaClassName = this.javaClass.getName();
        this.vColumnName = extractVColumnName();
        this.vTableName = this.javaClassName.substring(this.javaClassName.lastIndexOf(".") + 1).toUpperCase();
        initMethod();
    }

    public VColumn(String javaGetMethodName, String javaClassName) {
        this.javaGetMethodName = javaGetMethodName;
        this.javaClassName = javaClassName;
        this.vColumnName = extractVColumnName();
        this.vTableName = this.javaClassName.substring(this.javaClassName.lastIndexOf(".") + 1);
    }

    private String extractVColumnName(){
        if (this.javaGetMethodName.startsWith("get")) {
            return this.javaGetMethodName.substring(3);
        } else if (this.javaGetMethodName.startsWith("is")) {
            return this.javaGetMethodName.substring(2);
        }
        return this.javaGetMethodName;
    }


    public String getJavaGetMethodName() {
        return javaGetMethodName;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public String getvColumnName() {
        return vColumnName;
    }

    public String getvTableName() {
        return vTableName;
    }

    public Method getJavaGetMethod() {
        return javaGetMethod;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.javaClassName,this.javaGetMethodName);
    }

    @Override
    public boolean equals(Object obj) {
        VColumn other = (VColumn) obj;
        if (this.javaClassName.equals(other.javaClassName) && this.javaGetMethodName.equals(other.javaGetMethodName)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.javaClassName + "." + this.javaGetMethodName;
    }

    public boolean isMethodInitialized() {
        return (this.javaGetMethod != null || !IS_SERVER_SIDE);
    }

    public boolean isCollection() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.collection;
    }

    public Class<?> getJavaMethodReturnType() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.javaMethodReturnType;
    }

    public boolean isInt() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.isint;
    }

    public boolean isBoolean() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.isboolean;
    }

    public boolean isLong() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.islong;
    }

    public boolean isByte() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.isbyte;
    }

    public Object get(Object object) {
        if (this.javaGetMethod == null) {
            initMethod();
        }
        try {
            return javaGetMethod.invoke(object, (Object[]) null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Failed to extract value from object",e);
        }
        return null;
    }

    public void setAugmentedTableName(String _augName){
        this.augmentedTableName = _augName;
    }

    public String getAugmentedTableName(){
        return this.augmentedTableName;
    }

    public void initMethod() {
        try {
            if (this.javaClass == null || this.javaGetMethod == null) {
                this.javaClass = ClassLoaderManager.getClassLoader(this.javaClassName).loadClass(this.javaClassName);
                this.javaGetMethod = this.javaClass.getMethod(this.javaGetMethodName, (Class[]) null);
            }

            if (this.javaGetMethod.getReturnType().isArray()) {
                this.collection = true;
                this.javaMethodReturnType = this.javaGetMethod.getReturnType().getComponentType();
            } else if (List.class.isAssignableFrom(this.javaGetMethod.getReturnType())
                    || Set.class.isAssignableFrom(this.javaGetMethod.getReturnType())
                    || Map.class.isAssignableFrom(this.javaGetMethod.getReturnType())) {
                this.collection = true;
                this.javaMethodReturnType = getMethodReturnTypeFromGeneric(this.javaGetMethod);
            } else {
                this.javaMethodReturnType = this.javaGetMethod.getReturnType();
            }

            if (this.javaMethodReturnType.equals(int.class)) {
                this.isint = true;
            } else if (this.javaMethodReturnType.equals(boolean.class)) {
                this.isboolean = true;
            } else if (this.javaMethodReturnType.equals(long.class)) {
                this.islong = true;
            } else if (this.javaMethodReturnType.equals(byte.class)) {
                this.isbyte = true;
            }
        }catch(ClassNotFoundException | NoSuchMethodException e){
            LOG.error("Failed to init the java class & methods",e);
        }
    }

    public static final void encode(VColumn vcol, BytesArray ba) {
        Encoder.encodeString(vcol.javaGetMethodName,ba);
        Encoder.encodeString(vcol.javaClassName,ba);
        Encoder.encodeString(vcol.augmentedTableName,ba);
    }

    public static final VColumn decode(BytesArray ba) {
        VColumn vcol = new VColumn(Encoder.decodeString(ba),Encoder.decodeString(ba));
        vcol.augmentedTableName = Encoder.decodeString(ba);
        return vcol;
    }

    public static Class<?> getGenericType(ParameterizedType type) {
        Type[] typeArguments = type.getActualTypeArguments();
        for (Type typeArgument : typeArguments) {
            if (typeArgument instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) typeArgument;
                return (Class<?>) pType.getRawType();
            } else if (typeArgument instanceof Class) {
                return (Class<?>) typeArgument;
            }
        }
        return null;
    }

    public static Class<?> getMethodReturnTypeFromGeneric(Method m) {
        Type rType = m.getGenericReturnType();
        if (rType instanceof ParameterizedType) {
            return getGenericType((ParameterizedType) rType);
        }
        return null;
    }

    public boolean ofVTable(VTable table){
        if(table.getJavaClassTypeName().equals(this.getJavaClassName())){
            return true;
        }
        return false;
    }

    public boolean isList(){
        if(this.javaGetMethod==null){
            return false;
        }
        if(List.class.isAssignableFrom(this.javaGetMethod.getReturnType())){
            return true;
        }
        return false;
    }
}
