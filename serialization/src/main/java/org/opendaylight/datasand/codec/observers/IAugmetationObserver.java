/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.datasand.codec.observers;

import org.opendaylight.datasand.codec.EncodeDataContainer;
/**
 * @author - Sharon Aicler (saichler@cisco.com)
 *
 */
public interface IAugmetationObserver {
    public void encodeAugmentations(Object value, EncodeDataContainer ba);
    public void decodeAugmentations(Object builder, EncodeDataContainer ba,Class<?> augmentedClass);
}
