package org.datasand.codec.observers;

import org.datasand.codec.VColumn;
import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IChildAttributeObserver {
    public boolean isChildAttribute(VColumn column);
    public boolean isChildAttribute(VTable vTable);
    public boolean supportAugmentation(VColumn column);
    public boolean supportAugmentation(VTable vTable);
}
