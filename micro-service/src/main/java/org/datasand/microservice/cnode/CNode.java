/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.microservice.Message;
import org.datasand.microservice.MessageEntry;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicePeerEntry;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.microservice.cnode.handlers.ARPMulticastHandler;
import org.datasand.microservice.cnode.handlers.AcknowledgeHandler;
import org.datasand.microservice.cnode.handlers.EnterSyncModeHandler;
import org.datasand.microservice.cnode.handlers.NodeJoinHandler;
import org.datasand.microservice.cnode.handlers.NodeOriginalDataHandler;
import org.datasand.microservice.cnode.handlers.PeerSyncDataHandler;
import org.datasand.microservice.cnode.handlers.RequestJournalDataHandler;
import org.datasand.microservice.cnode.handlers.SetCurrentPeerIDHandler;
import org.datasand.microservice.cnode.handlers.SetCurrentPeerIDReplyHandler;
import org.datasand.network.NID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class CNode<DataType, DataTypeElement> extends MicroService {

    public static final Logger LOG = LoggerFactory.getLogger(CNode.class);
    public static final int NODE_JOIN = 100;
    public static final int ARP_MULTICAST = 110;
    public static final int ENTER_SYNC_MODE = 120;
    public static final int NODE_ORIGINAL_DATA = 130;
    public static final int REQUEST_JOURNAL_DATA = 140;
    public static final int PEER_SYNC_DATA = 150;
    public static final int SET_CURRENT_PEER_ID = 160;
    public static final int SET_CURRENT_PEER_ID_REPLY = 170;
    public static final int ACKNOWLEDGE = 180;

    private Message arpID = new Message(-1, null);
    private long nextID = 1000;
    private DataType localData = createDataTypeInstance();
    private Map<Integer, ICNodeCommandHandler<DataType, DataTypeElement>> handlers = new HashMap<Integer, ICNodeCommandHandler<DataType, DataTypeElement>>();
    private boolean synchronizing = false;
    private NID sortedNIDs[] = new NID[0];

    public CNode(String clusterName, MicroServicesManager m) {
        super(clusterName, m);
        this.addToSortedNetworkIDs(this.getMicroServiceID());

        this.registerHandler(NODE_JOIN, new NodeJoinHandler<DataType, DataTypeElement>());
        this.registerHandler(ACKNOWLEDGE, new AcknowledgeHandler<DataType, DataTypeElement>());
        this.registerHandler(NODE_ORIGINAL_DATA, new NodeOriginalDataHandler<DataType, DataTypeElement>());
        this.registerHandler(ENTER_SYNC_MODE, new EnterSyncModeHandler<DataType, DataTypeElement>());
        this.registerHandler(REQUEST_JOURNAL_DATA, new RequestJournalDataHandler<DataType, DataTypeElement>());
        this.registerHandler(PEER_SYNC_DATA, new PeerSyncDataHandler<DataType, DataTypeElement>());
        this.registerHandler(SET_CURRENT_PEER_ID, new SetCurrentPeerIDHandler<DataType, DataTypeElement>());
        this.registerHandler(SET_CURRENT_PEER_ID_REPLY, new SetCurrentPeerIDReplyHandler<DataType, DataTypeElement>());
        this.registerHandler(ARP_MULTICAST, new ARPMulticastHandler<DataType, DataTypeElement>());

        this.registerRepetitiveMessage(10000, 10000, 0, arpID);
        this.multicast(new Message(NODE_JOIN, null));
    }

    public long incrementID() {
        long result = this.nextID++;
        return result;
    }

    private void addToSortedNetworkIDs(NID neid) {
        NID temp[] = new NID[this.sortedNIDs.length + 1];
        System.arraycopy(this.sortedNIDs, 0, temp, 0, sortedNIDs.length);
        temp[this.sortedNIDs.length] = neid;
        Arrays.sort(temp, new NetworkIDComparator());
        this.sortedNIDs = temp;
    }

    public NID[] getSortedNIDs() {
        return this.sortedNIDs;
    }

    public void registerHandler(Integer pType, ICNodeCommandHandler<DataType, DataTypeElement> handler) {
        this.handlers.put(pType, handler);
    }

    public long getNextID() {
        return this.nextID;
    }

    public void setSynchronizing(boolean b) {
        this.synchronizing = b;
    }

    public boolean isSynchronizing() {
        return this.synchronizing;
    }

    public void sendARPBroadcast() {
        multicast(new Message(this.nextID - 1, ARP_MULTICAST, null));
    }

    public CMicroServicePeerEntry<DataType> getPeerEntry(NID source) {
        MicroServicePeerEntry pEntry = super.getPeerEntry(source);
        if (pEntry != null && !(pEntry instanceof CMicroServicePeerEntry)) {
            pEntry = new CMicroServicePeerEntry<DataType>(source, createDataTypeInstance());
            this.replacePeerEntry(source, pEntry);
            this.addToSortedNetworkIDs(source);
        }
        return (CMicroServicePeerEntry<DataType>) pEntry;
    }

    @Override
    public void processDestinationUnreachable(Message message, NID unreachableSource) {
        CMicroServicePeerEntry<DataType> peerEntry = getPeerEntry(unreachableSource);
        ICNodeCommandHandler<DataType, DataTypeElement> handle = this.handlers.get(message.getMessageType());
        handle.handleUnreachableMessage(message, unreachableSource, peerEntry, this);
    }

    public void processMessage(Message cmd, NID source, NID destination) {
        if (cmd == arpID) {
            sendARPBroadcast();
            return;
        }
        CMicroServicePeerEntry<DataType> peerEntry = getPeerEntry(source);

        //myself, do nothing
        if (peerEntry == null) return;

        ICNodeCommandHandler<DataType, DataTypeElement> handle = this.handlers.get(cmd.getMessageType());
        handle.handleMessage(cmd, source, destination, peerEntry, this);
    }

    public DataType getLocalData() {
        return this.localData;
    }

    public void cleanJournalHistoryForSource(NID source) {
        //Remove any expected replys from this node as it is up.
        List<Message> finished = new LinkedList<Message>();
        for (Object meo : this.getJournalEntries()) {
            MessageEntry me = (MessageEntry) meo;
            me.removePeer(source);
            if (me.isFinished()) {
                finished.add(me.getMessage());
            }
        }
        for (Message m : finished) {
            this.removeJournalEntry(m);
        }
        //Update this node change id it the source node
        this.send(new Message(this.nextID - 1, SET_CURRENT_PEER_ID, null), source);
    }

    public void sendAcknowledge(Message Message, NID source) {
        send(new Message(Message.getMessageID(), ACKNOWLEDGE, null), source);
    }

    public abstract DataType createDataTypeInstance();

    public abstract Collection<DataTypeElement> getDataTypeElementCollection(DataType data);

    public abstract void handleNodeOriginalData(DataTypeElement dataTypeElement);

    public abstract void handlePeerSyncData(DataTypeElement dataTypeElement, NID source);

    public abstract boolean isLocalPeerCopyContainData(DataType data);

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getName() {
        return "Cluster Node " + this.getMicroServiceID();
    }

    public void log(String str) {
        StringBuffer buff = new StringBuffer("Node ").append(this.getMicroServiceID()).append(" - ");
        buff.append(str);
        LOG.info(str);
    }
}
