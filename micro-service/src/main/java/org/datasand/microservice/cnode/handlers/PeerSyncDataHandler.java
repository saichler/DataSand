/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode.handlers;

import org.datasand.microservice.Message;
import org.datasand.microservice.cnode.CNode;
import org.datasand.microservice.cnode.CMicroServicePeerEntry;
import org.datasand.microservice.cnode.ICNodeCommandHandler;
import org.datasand.network.HabitatID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PeerSyncDataHandler <DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, HabitatID source, HabitatID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        node.handlePeerSyncData((DataTypeElement)cNodeCommand.getMessageData(),source);
        /*
        if(listener!=null){
            listener.peerPut((K) cmd.getKey(), (V) cmd.getValue());
        }*/
        node.sendAcknowledge(cNodeCommand, source);
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, HabitatID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
