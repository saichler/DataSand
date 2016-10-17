/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cmap;

import java.util.Map;

import org.datasand.microservice.Message;
import org.datasand.microservice.cnode.CNode;
import org.datasand.microservice.cnode.CMicroServicePeerEntry;
import org.datasand.microservice.cnode.ICNodeCommandHandler;
import org.datasand.network.NID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PutHandler<K,V> implements ICNodeCommandHandler<Map<K,V>,CMapEntry<K,V>>{

    @Override
    public void handleMessage(Message cNodeCommand, NID source, NID destination, CMicroServicePeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
        CMap<K,V> cmap = (CMap<K,V>)node;
        node.log("Putting Key:"+((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey());
        if(!cmap.containsKey(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey())){
            cmap.increaseSize();
        }
        peerEntry.getPeerData().put(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey(), ((CMapEntry<K,V>)cNodeCommand.getMessageData()).getValue());
        peerEntry.timeStamp();
        peerEntry.setLastID(cNodeCommand.getMessageID());
        cmap.sendAcknowledge(cNodeCommand, source);
        if(cmap.getListener()!=null){
            cmap.getListener().peerPut(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey(), ((CMapEntry<K,V>)cNodeCommand.getMessageData()).getValue());
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, NID unreachableSource, CMicroServicePeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
    }
}
