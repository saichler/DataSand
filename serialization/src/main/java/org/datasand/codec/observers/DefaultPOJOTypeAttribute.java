package org.datasand.codec.observers;

import org.datasand.codec.AttributeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultPOJOTypeAttribute implements ITypeAttributeObserver{
    @Override
    public boolean isTypeAttribute(AttributeDescriptor ad) {
        if(ad.getReturnType().isPrimitive()) return false;
        if(ad.getReturnType().getName().startsWith("java.")) return false;
        return true;
    }
}
