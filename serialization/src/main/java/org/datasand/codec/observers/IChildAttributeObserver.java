package org.datasand.codec.observers;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IChildAttributeObserver {
    public boolean isChildAttribute(AttributeDescriptor ad);
    public boolean isChildAttribute(TypeDescriptor td);
    public boolean supportAugmentation(AttributeDescriptor ad);
    public boolean supportAugmentation(TypeDescriptor td);
}
