/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cnode.handlers;

import org.datasand.agents.Message;
import org.datasand.agents.MessageEntry;
import org.datasand.agents.cnode.CNode;
import org.datasand.agents.cnode.CPeerEntry;
import org.datasand.agents.cnode.ICNodeCommandHandler;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class RequestJournalDataHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, NetworkID source,NetworkID destination, CPeerEntry<DataType> peerEntry,CNode<DataType, DataTypeElement> node) {
        node.log("Requested Journal Data from "+source.getPort());
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
    public void handleUnreachableMessage(Message cNodeCommand,NetworkID unreachableSource, CPeerEntry<DataType> peerEntry,CNode<DataType, DataTypeElement> node) {
    }
}
