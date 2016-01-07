package org.datasand.codec.observers;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.TypeDescriptor;
import org.datasand.codec.bytearray.VColumn;
import org.datasand.codec.bytearray.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IChildAttributeObserver {
    public boolean isChildAttribute(VColumn column);
    public boolean isChildAttribute(VTable vTable);
    public boolean supportAugmentation(VColumn column);
    public boolean supportAugmentation(VTable vTable);
}
