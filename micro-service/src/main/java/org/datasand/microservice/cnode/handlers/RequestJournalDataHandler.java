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
import org.datasand.network.NetUUID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class RequestJournalDataHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, NetUUID source, NetUUID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        node.log("Requested Journal Data from "+source);
        node.setSynchronizing(true);
        for(MessageEntry e:node.getJournalEntries()){
            if(e.containPeer(source)){
                node.send(e.getMessage(), source);
            }
        }
        node.send(new Message(node.getNextID()-1,CNode.SET_CURRENT_PEER_ID_REPLY,null), source);
        synchronized(node){
            node.log("Finish Sync");
            node.setSynchronizing(false);
            node.notifyAll();
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, NetUUID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
