package org.datasand.codec.observers;

import java.lang.reflect.Method;

import org.datasand.codec.AttributeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IMethodFilterObserver {
    public boolean isValidModelMethod(Method m);
    public boolean isValidAttribute(AttributeDescriptor ad);
}
