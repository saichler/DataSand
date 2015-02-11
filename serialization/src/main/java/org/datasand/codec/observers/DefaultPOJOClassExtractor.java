package org.datasand.codec.observers;

import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultPOJOClassExtractor implements IClassExtractorObserver{

    @Override
    public Class<?> getObjectClass(Object obj) {
        return obj.getClass();
    }

    @Override
    public Class<?> getBuilderClass(TypeDescriptor td) {
        return td.getTypeClass();
    }

    @Override
    public String getBuilderMethod(TypeDescriptor td) {
        return null;
    }

}
