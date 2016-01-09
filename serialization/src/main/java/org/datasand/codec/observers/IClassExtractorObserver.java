package org.datasand.codec.observers;

import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IClassExtractorObserver {
    public Class<?> getObjectClass(Object obj);
    public Class<?> getBuilderClass(VTable td);
    public String getBuilderMethod(VTable td);
}
