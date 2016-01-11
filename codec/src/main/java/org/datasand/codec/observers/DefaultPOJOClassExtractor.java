package org.datasand.codec.observers;

import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultPOJOClassExtractor implements IClassExtractorObserver{

    @Override
    public Class<?> getObjectClass(Object obj) {
        return obj.getClass();
    }

    @Override
    public Class<?> getBuilderClass(VTable vTable) {
        return vTable.getJavaClassType();
    }

    @Override
    public String getBuilderMethod(VTable td) {
        return null;
    }

}
