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
import org.datasand.network.HabitatID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ARPMulticastHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, HabitatID source, HabitatID destination, CPeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        //update peer data
        peerEntry.timeStamp();
        if(peerEntry.getLastID()>cNodeCommand.getMessageID() && cNodeCommand.getMessageID()!=999){
            node.log("Peer "+source.getPort()+" Need Synchronize");
        }
        if(cNodeCommand.getMessageID()!=999){
            if(peerEntry.getLastID()!=cNodeCommand.getMessageID()){
                node.log("Detected unsync with "+source.getPort());
                node.setSynchronizing(true);
                node.send(new Message(-1,CNode.REQUEST_JOURNAL_DATA,null),source);
                for(MessageEntry e:node.getJournalEntries()){
                    if(e.containPeer(source)){
                        node.send(e.getMessage(), source);
                    }
                }
                node.send(new Message(node.getNextID()-1,CNode.SET_CURRENT_PEER_ID,null), source);
            }else{
                peerEntry.setLastID(cNodeCommand.getMessageID());
            }
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, HabitatID unreachableSource, CPeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
