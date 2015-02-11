package org.datasand.codec.observers;

import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IClassExtractorObserver {
    public Class<?> getObjectClass(Object obj);
    public Class<?> getBuilderClass(TypeDescriptor td);
    public String getBuilderMethod(TypeDescriptor td);
}
