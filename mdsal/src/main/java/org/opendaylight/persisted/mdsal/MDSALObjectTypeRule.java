package org.opendaylight.persisted.mdsal;

import org.datasand.codec.AttributeDescriptor;
import org.datasand.codec.observers.ITypeAttributeObserver;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class MDSALObjectTypeRule implements ITypeAttributeObserver{

    @Override
    public boolean isTypeAttribute(AttributeDescriptor ad) {
        return (ad.getReturnType().getName().indexOf(".rev")!=-1);
    }
}
