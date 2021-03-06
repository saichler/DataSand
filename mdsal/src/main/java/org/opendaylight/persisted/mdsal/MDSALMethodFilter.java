package org.opendaylight.persisted.mdsal;

import java.lang.reflect.Method;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.observers.IMethodFilterObserver;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class MDSALMethodFilter implements IMethodFilterObserver{

    @Override
    public boolean isValidModelMethod(Method m) {
        if (m.getName().equals("getImplementedInterface"))
            return false;
        if(m.getReturnType().isArray() && m.getReturnType().getComponentType().equals(boolean.class))
            return false;
        return true;
    }

    @Override
    public boolean isValidAttribute(AttributeDescriptor ad) {
        if(ad.getReturnType().getName().equals("org.opendaylight.yangtools.yang.binding.Identifier"))
            return false;
         return true;
    }

}
