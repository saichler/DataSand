/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode;

import org.datasand.microservice.MicroServicePeerEntry;
import org.datasand.network.NID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class CMicroServicePeerEntry<DataType> extends MicroServicePeerEntry {
    private DataType peerData;

    public CMicroServicePeerEntry(NID _netNID, DataType _peerData){
        super(_netNID);
        this.peerData = _peerData;
    }

    public DataType getPeerData() {
        return peerData;
    }
}
