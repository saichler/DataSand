/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cmap;

import java.util.Map;

import org.datasand.agents.Message;
import org.datasand.agents.cnode.CNode;
import org.datasand.agents.cnode.CPeerEntry;
import org.datasand.agents.cnode.ICNodeCommandHandler;
import org.datasand.network.ServiceID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PutHandler<K,V> implements ICNodeCommandHandler<Map<K,V>,CMapEntry<K,V>>{

    @Override
    public void handleMessage(Message cNodeCommand, ServiceID source, ServiceID destination, CPeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
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
    public void handleUnreachableMessage(Message cNodeCommand, ServiceID unreachableSource, CPeerEntry<Map<K, V>> peerEntry, CNode<Map<K, V>, CMapEntry<K, V>> node) {
    }
}
