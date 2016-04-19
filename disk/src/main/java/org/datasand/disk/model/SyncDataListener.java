/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.disk.model;

import java.io.File;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface SyncDataListener {
    public void notifyCurrentDirectory(File directory);
    public void notifyCurrentFile(File file);
    public void notifyCurrentFileProgress(File file,int part, int outOf);
    public void notifyDone(File source,File dest);

}
