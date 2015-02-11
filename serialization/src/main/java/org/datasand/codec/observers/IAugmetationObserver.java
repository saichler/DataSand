package org.datasand.codec.observers;

import org.datasand.codec.EncodeDataContainer;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IAugmetationObserver {
    public void encodeAugmentations(Object value, EncodeDataContainer ba);
    public void decodeAugmentations(Object builder, EncodeDataContainer ba,Class<?> augmentedClass);
}
