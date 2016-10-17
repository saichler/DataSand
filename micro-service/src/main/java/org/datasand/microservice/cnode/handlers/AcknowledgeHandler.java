/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode.handlers;

import org.datasand.microservice.Message;
import org.datasand.microservice.MessageEntry;
import org.datasand.microservice.cnode.CNode;
import org.datasand.microservice.cnode.CMicroServicePeerEntry;
import org.datasand.microservice.cnode.ICNodeCommandHandler;
import org.datasand.network.NID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AcknowledgeHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{
    @Override
    public void handleMessage(Message cNodeCommand, NID source, NID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        MessageEntry entry = node.getJournalEntry(cNodeCommand);
        if(entry!=null){
            entry.removePeer(source);
            if(entry.isFinished()){
                node.removeJournalEntry(cNodeCommand);
            }
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, NID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
