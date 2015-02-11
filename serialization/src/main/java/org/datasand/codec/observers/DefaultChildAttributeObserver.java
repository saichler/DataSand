package org.datasand.codec.observers;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultChildAttributeObserver implements IChildAttributeObserver{

    @Override
    public boolean isChildAttribute(AttributeDescriptor ad) {
        if(ad.isCollection() && !ad.getReturnType().getPackage().getName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean isChildAttribute(TypeDescriptor td) {
        if(!td.getTypeClassName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean supportAugmentation(AttributeDescriptor ad) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportAugmentation(TypeDescriptor td) {
        // TODO Auto-generated method stub
        return false;
    }

}
