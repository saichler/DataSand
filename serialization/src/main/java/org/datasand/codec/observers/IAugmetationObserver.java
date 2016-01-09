package org.datasand.codec.observers;

import org.datasand.codec.BytesArray;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IAugmetationObserver {
    public void encodeAugmentations(Object value, BytesArray ba);
    public void decodeAugmentations(Object builder, BytesArray ba,Class<?> augmentedClass);
}
