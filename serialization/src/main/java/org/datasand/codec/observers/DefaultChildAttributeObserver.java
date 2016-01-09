package org.datasand.codec.observers;

import org.datasand.codec.VColumn;
import org.datasand.codec.VTable;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DefaultChildAttributeObserver implements IChildAttributeObserver{

    @Override
    public boolean isChildAttribute(VColumn vColumn) {
        if(vColumn.isCollection() && !vColumn.getJavaMethodReturnType().getPackage().getName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean isChildAttribute(VTable vTable) {
        if(!vTable.getJavaClassType().getName().startsWith("java"))
            return true;
        return false;
    }

    @Override
    public boolean supportAugmentation(VColumn vColumn) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean supportAugmentation(VTable vTable) {
        // TODO Auto-generated method stub
        return false;
    }

}
