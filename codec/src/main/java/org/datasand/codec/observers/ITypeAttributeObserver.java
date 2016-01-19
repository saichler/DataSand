/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec.observers;

import org.datasand.codec.VColumn;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ITypeAttributeObserver {
    public boolean isTypeAttribute(VColumn vColumn);
}
