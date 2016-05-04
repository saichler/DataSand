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
public class SetCurrentPeerIDReplyHandler <DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, HabitatID source, HabitatID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        peerEntry.setLastID(cNodeCommand.getMessageID());
        synchronized(node){
            node.log("Finished Synchronizing with "+source.getPort());
            node.setSynchronizing(false);
            node.notifyAll();
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, HabitatID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
