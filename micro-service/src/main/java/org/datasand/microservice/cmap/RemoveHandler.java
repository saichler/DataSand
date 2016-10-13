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
import org.datasand.network.NetUUID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class RemoveHandler<K,V> implements ICNodeCommandHandler<Map<K,V>,CMapEntry<K,V>>{

    @Override
    public void handleMessage(Message cNodeCommand, NetUUID source, NetUUID destination, CMicroServicePeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
        CMap<K, V> cmap = (CMap<K, V>)node;
        Object o = peerEntry.getPeerData().remove(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey());
        if(o!=null && !cmap.containsKey(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey())){
            cmap.decreaseSize();
        }
        peerEntry.timeStamp();
        peerEntry.setLastID(cNodeCommand.getMessageID());
        node.sendAcknowledge(cNodeCommand, source);
        if(cmap.getListener()!=null){
            cmap.getListener().peerRemove(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey());
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand, NetUUID unreachableSource, CMicroServicePeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
    }
}
