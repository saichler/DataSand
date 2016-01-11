package org.datasand.codec.observers;

import org.datasand.codec.VColumn;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultPOJOTypeAttribute implements ITypeAttributeObserver{
    @Override
    public boolean isTypeAttribute(VColumn vColumn) {
        if(vColumn.getJavaMethodReturnType().isPrimitive()) return false;
        if(vColumn.getJavaMethodReturnType().getName().startsWith("java.")) return false;
        return true;
    }
}
