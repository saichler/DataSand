package org.datasand.codec.bytearray;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 1/7/16.
 */
public class VColumn {
    public static boolean IS_SERVER_SIDE = false;

    private final String javaGetMethodName;
    private final String javaClassName;
    private final String vColumnName;
    private final String vTableName;

    private Method getMethod = null;
    private String augmentedTableName = null;
    private Class<?> myClass = null;
    private boolean collection = false;
    private Class<?> returntype = null;
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
        this.javaGetMethodName = getMethod.getName();
        this.javaClassName = this.myClass.getName();
        this.vColumnName = extractVColumnName();
        this.vTableName = this.javaClassName.substring(this.javaClassName.lastIndexOf(".") + 1);

        this.getMethod = getMethod;
        this.myClass = clazz;
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

    public String getMethodName() {
        return this.methodName;
    }

    public String getClassName() {
        return this.className;
    }

    public int getCharWidth() {
        return this.charWidth;
    }

    public void setCharWidth(int i) {
        this.charWidth = i;
    }

    public String getColumnName() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        if (this.columnName == null) {
            if (this.methodName.startsWith("get")) {
                this.columnName = this.methodName.substring(3);
            } else if (this.methodName.startsWith("is")) {
                this.columnName = this.methodName.substring(2);
            }
        }
        return this.columnName;
    }

    public String getTableName() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        if (this.tableName == null) {
            this.tableName = this.className.substring(this.className
                    .lastIndexOf(".") + 1);
        }
        return tableName;
    }

    @Override
    public int hashCode() {
        return this.methodName.hashCode() + this.className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        AttributeDescriptor other = (AttributeDescriptor) obj;
        if (className.equals(other.className)
                && methodName.equals(other.methodName))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return className + "." + methodName;
    }

    public boolean isMethodInitialized() {
        return (this.getMethod != null || !IS_SERVER_SIDE);
    }

    public boolean isCollection() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.collection;
    }

    public Class<?> getReturnType() {
        if (!isMethodInitialized()) {
            initMethod();
        }
        return this.returntype;
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

    public Object get(Object element, Map<?, ?> augmentationMap, Class<?> elementClass) {
        if (this.getMethod == null) {
            initMethod();
        }
        if (this.myClass.equals(elementClass)) {
            try {
                return getMethod.invoke(element, (Object[]) null);
            } catch (Exception err) {
                err.printStackTrace();
            }
        } else {
            Object augmentingElement = augmentationMap.get(this.myClass);
            try {
                return getMethod.invoke(augmentingElement, (Object[]) null);
            } catch (Exception err) {
                err.printStackTrace();
            }
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

        if (this.myClass == null || this.getMethod == null) {
            try {
                this.myClass = getClass().getClassLoader().loadClass(this.className);
                this.getMethod = this.myClass.getMethod(this.methodName,(Class[]) null);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        if (this.getMethod.getReturnType().isArray()) {
            this.collection = true;
            this.returntype = this.getMethod.getReturnType().getComponentType();
        } else if (List.class.isAssignableFrom(this.getMethod.getReturnType())
                || Set.class.isAssignableFrom(this.getMethod.getReturnType())
                || Map.class.isAssignableFrom(this.getMethod.getReturnType())) {
            this.collection = true;
            this.returntype = getMethodReturnTypeFromGeneric(this.getMethod);
        } else {
            this.returntype = this.getMethod.getReturnType();
        }

        if (this.returntype.equals(int.class))
            this.isint = true;
        else if (this.returntype.equals(boolean.class))
            this.isboolean = true;
        else if (this.returntype.equals(long.class))
            this.islong = true;
        else if (this.returntype.equals(byte.class))
            this.isbyte = true;
    }

    public void encode(EncodeDataContainer ba) {
        ba.getEncoder().encodeString(this.methodName, ba);
        ba.getEncoder().encodeString(this.className, ba);
        ba.getEncoder().encodeString(this.augmentedTableName, ba);
    }

    public static AttributeDescriptor decode(EncodeDataContainer ba) {
        String mName = ba.getEncoder().decodeString(ba);
        String cName = ba.getEncoder().decodeString(ba);
        String aName = ba.getEncoder().decodeString(ba);
        AttributeDescriptor p = new AttributeDescriptor(mName,cName);
        p.setAugmentedTableName(aName);
        return p;
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
}
