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
public class ARPMulticastHandler<DataType, DataTypeElement> implements ICNodeCommandHandler<DataType, DataTypeElement>{

    @Override
    public void handleMessage(Message cNodeCommand, MicroServiceNetUUID source, NetUUID destination, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
        //update peer data
        peerEntry.timeStamp();
        if(peerEntry.getLastID()>cNodeCommand.getMessageID() && cNodeCommand.getMessageID()!=999){
            node.log("Peer "+source+" Need Synchronize");
        }
        if(cNodeCommand.getMessageID()!=999){
            if(peerEntry.getLastID()!=cNodeCommand.getMessageID()){
                node.log("Detected unsync with "+source);
                node.setSynchronizing(true);
                node.send(new Message(node.getMicroServiceID().getMicroServiceID(),-1,CNode.REQUEST_JOURNAL_DATA,null),source);
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
    public void handleUnreachableMessage(Message cNodeCommand, NetUUID unreachableSource, CMicroServicePeerEntry<DataType> peerEntry, CNode<DataType, DataTypeElement> node) {
    }
}
