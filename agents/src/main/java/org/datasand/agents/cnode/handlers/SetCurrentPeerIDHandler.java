/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cnode.handlers;

import org.datasand.agents.Message;
import org.datasand.agents.cnode.CNode;
import org.datasand.agents.cnode.CPeerEntry;
import org.datasand.agents.cnode.ICNodeCommandHandler;
import org.datasand.network.HabitatID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SetCurrentPeerIDHandler <DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, HabitatID source, HabitatID destination, CPeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        peerEntry.setLastID(cNodeCommand.getMessageID());
        if(node.getNextID()==1000)
            node.incrementID();
        node.send(new Message(node.getNextID()-1,CNode.SET_CURRENT_PEER_ID_REPLY,null), source);
        synchronized(node){
            node.log("Finished Synchronizing with "+source.getPort());
            node.setSynchronizing(false);
            node.notifyAll();
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, HabitatID unreachableSource, CPeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
