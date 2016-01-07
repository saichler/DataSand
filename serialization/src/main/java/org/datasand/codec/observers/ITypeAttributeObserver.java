package org.datasand.codec.observers;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.bytearray.VColumn;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ITypeAttributeObserver {
    public boolean isTypeAttribute(VColumn vColumn);
}
