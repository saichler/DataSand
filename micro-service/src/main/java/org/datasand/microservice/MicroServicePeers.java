/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import org.datasand.network.HabitatID;

import java.util.HashMap;
import java.util.Map;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class MicroServicePeers {

    private final Map<HabitatID,MicroServicePeerEntry> peers = new HashMap<>();
    private final MicroService service;

    public MicroServicePeers(MicroService service){
        this.service = service;
    }

    public synchronized MessageEntry addARPJournal(Message message,boolean includeSelf){
        if(this.peers.size()>0 || includeSelf){
            MessageEntry entry = new MessageEntry(message);
            boolean hasOnePeer = false;

            for(MicroServicePeerEntry e:this.peers.values()){
                if(!e.isUnreachable()){
                    hasOnePeer=true;
                    entry.addPeer(e.getHabitatID());
                }
            }

            if(includeSelf){
                hasOnePeer = true;
                entry.addPeer(this.service.getMicroServiceID());
            }

            if(hasOnePeer) {
                this.service.addMessageEntry(entry);
            }

            return entry;
        }
        return null;
    }

    public synchronized void replacePeerEntry(HabitatID source, MicroServicePeerEntry entry){
        this.peers.put(source, entry);
    }

    public synchronized MicroServicePeerEntry getPeerEntry(HabitatID source){
        if(source.equals(this.service.getMicroServiceID())) return null;
        MicroServicePeerEntry microServicePeerEntry = peers.get(source);
        if(microServicePeerEntry ==null){
            System.out.println("Add Source-"+source+" Count="+(peers.size()+1));
            microServicePeerEntry = new MicroServicePeerEntry(source);
            peers.put(source, microServicePeerEntry);
        }
        return microServicePeerEntry;
    }

    public synchronized HabitatID getAPeer(){
        if(this.peers.size()>0) {
            return this.peers.values().iterator().next().getHabitatID();
        }
        return null;
    }
}
