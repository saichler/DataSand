/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode;

import org.datasand.microservice.*;
import org.datasand.microservice.cnode.handlers.*;
import org.datasand.network.HabitatID;
import org.datasand.network.habitat.HabitatsConnection;

import java.util.*;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class CNode<DataType,DataTypeElement> extends MicroService {

    public static final int NODE_JOIN                   = 100;
    public static final int ARP_MULTICAST               = 110;
    public static final int ENTER_SYNC_MODE             = 120;
    public static final int NODE_ORIGINAL_DATA          = 130;
    public static final int REQUEST_JOURNAL_DATA        = 140;
    public static final int PEER_SYNC_DATA              = 150;
    public static final int SET_CURRENT_PEER_ID         = 160;
    public static final int SET_CURRENT_PEER_ID_REPLY   = 170;
    public static final int ACKNOWLEDGE                 = 180;

    private HabitatID multicastGroupHabitatID = null;
    private int multicastGroupID = -1;
    private Message arpID = new Message();
    private long nextID = 1000;
    private DataType localData = createDataTypeInstance();
    private Map<Integer,ICNodeCommandHandler<DataType,DataTypeElement>> handlers = new HashMap<Integer,ICNodeCommandHandler<DataType,DataTypeElement>>();
    private boolean synchronizing = false;
    private HabitatID sortedHabitatIDs[] = new HabitatID[0];

    public CNode(int subSystemId, MicroServicesManager m, int _multicastGroupID){
        super(subSystemId,m);
        this.addToSortedNetworkIDs(this.getMicroServiceID());
        this.multicastGroupID = _multicastGroupID;
        this.multicastGroupHabitatID = new HabitatID(
                HabitatsConnection.PROTOCOL_ID_BROADCAST.getIPv4Address(),
                multicastGroupID, multicastGroupID);

        this.registerHandler(NODE_JOIN, new NodeJoinHandler<DataType,DataTypeElement>());
        this.registerHandler(ACKNOWLEDGE, new AcknowledgeHandler<DataType,DataTypeElement>());
        this.registerHandler(NODE_ORIGINAL_DATA, new NodeOriginalDataHandler<DataType,DataTypeElement>());
        this.registerHandler(ENTER_SYNC_MODE, new EnterSyncModeHandler<DataType,DataTypeElement>());
        this.registerHandler(REQUEST_JOURNAL_DATA, new RequestJournalDataHandler<DataType,DataTypeElement>());
        this.registerHandler(PEER_SYNC_DATA, new PeerSyncDataHandler<DataType,DataTypeElement>());
        this.registerHandler(SET_CURRENT_PEER_ID, new SetCurrentPeerIDHandler<DataType,DataTypeElement>());
        this.registerHandler(SET_CURRENT_PEER_ID_REPLY, new SetCurrentPeerIDReplyHandler<DataType,DataTypeElement>());
        this.registerHandler(ARP_MULTICAST, new ARPMulticastHandler<DataType,DataTypeElement>());

        m.registerForMulticast(multicastGroupID, this);
        this.registerRepetitiveMessage(10000, 10000, 0, arpID);
        this.multicast(new Message(this.nextID,NODE_JOIN,null));
    }

    public long incrementID(){
        long result = this.nextID++;
        return result;
    }

    private void addToSortedNetworkIDs(HabitatID neid){
        HabitatID temp[] = new HabitatID[this.sortedHabitatIDs.length+1];
        System.arraycopy(this.sortedHabitatIDs,0, temp, 0, sortedHabitatIDs.length);
        temp[this.sortedHabitatIDs.length] = neid;
        Arrays.sort(temp,new NetworkIDComparator());
        this.sortedHabitatIDs = temp;
    }

    public HabitatID[] getSortedHabitatIDs(){
        return this.sortedHabitatIDs;
    }

    public void registerHandler(Integer pType,ICNodeCommandHandler<DataType, DataTypeElement> handler){
        this.handlers.put(pType, handler);
    }

    public long getNextID(){
        return this.nextID;
    }

    public void setSynchronizing(boolean b){
        this.synchronizing = b;
    }

    public boolean isSynchronizing(){
        return this.synchronizing;
    }

    public void multicast(Message message){
        this.send(message, this.multicastGroupHabitatID);
    }

    public void sendARPBroadcast(){
        multicast(new Message(this.nextID-1,ARP_MULTICAST,null));
    }

    public CMicroServicePeerEntry<DataType> getPeerEntry(HabitatID source){
        MicroServicePeerEntry pEntry = super.getPeerEntry(source);
        if(pEntry!=null && !(pEntry instanceof CMicroServicePeerEntry)){
            pEntry = new CMicroServicePeerEntry<DataType>(source, createDataTypeInstance());
            this.replacePeerEntry(source, pEntry);
            this.addToSortedNetworkIDs(source);
        }
        return (CMicroServicePeerEntry<DataType>)pEntry;
    }

    @Override
    public void processDestinationUnreachable(Message message,HabitatID unreachableSource) {
        CMicroServicePeerEntry<DataType> peerEntry = getPeerEntry(unreachableSource);
        ICNodeCommandHandler<DataType,DataTypeElement> handle = this.handlers.get(message.getMessageType());
        handle.handleUnreachableMessage(message,unreachableSource,peerEntry,this);
    }

    public void processMessage(Message cmd, HabitatID source, HabitatID destination){
        if(cmd==arpID){
            sendARPBroadcast();
            return;
        }
        CMicroServicePeerEntry<DataType> peerEntry = getPeerEntry(source);

        //myself, do nothing
        if(peerEntry==null) return;

        ICNodeCommandHandler<DataType,DataTypeElement> handle = this.handlers.get(cmd.getMessageType());
        handle.handleMessage(cmd,source,destination,peerEntry,this);
    }

    public DataType getLocalData(){
        return this.localData;
    }

    public void cleanJournalHistoryForSource(HabitatID source){
        //Remove any expected replys from this node as it is up.
        List<Message> finished = new LinkedList<Message>();
        for(Object meo:this.getJournalEntries()){
            MessageEntry me = (MessageEntry)meo;
            me.removePeer(source);
            if(me.isFinished()){
                finished.add(me.getMessage());
            }
        }
        for(Message m:finished){
            this.removeJournalEntry(m);
        }
        //Update this node change id it the source node
        this.send(new Message(this.nextID-1,SET_CURRENT_PEER_ID,null), source);
    }

    public void sendAcknowledge(Message Message,HabitatID source){
        send(new Message(Message.getMessageID(),ACKNOWLEDGE, null), source);
    }

    public abstract DataType createDataTypeInstance();
    public abstract Collection<DataTypeElement> getDataTypeElementCollection(DataType data);
    public abstract void handleNodeOriginalData(DataTypeElement dataTypeElement);
    public abstract void handlePeerSyncData(DataTypeElement dataTypeElement,HabitatID source);
    public abstract boolean isLocalPeerCopyContainData(DataType data);

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getName() {
        return "Cluster Node "+this.getMicroServiceID();
    }

    public void log(String str){
        StringBuffer buff = new StringBuffer("Node ").append(this.getMicroServiceID().getPort()).append(" - ");
        buff.append(str);
        System.out.println(buff);
    }
}