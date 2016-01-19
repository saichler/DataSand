/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cnode;

import org.datasand.agents.PeerEntry;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class CPeerEntry<DataType> extends PeerEntry{
    private DataType peerData;

    public CPeerEntry(NetworkID _netNetworkID,DataType _peerData){
        super(_netNetworkID);
        this.peerData = _peerData;
    }

    public DataType getPeerData() {
        return peerData;
    }
}
