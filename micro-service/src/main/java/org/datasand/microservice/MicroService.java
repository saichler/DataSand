/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;

import java.util.*;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class MicroService implements Runnable {

    private static final Message timeoutIdentifier = new Message(-1, -1, null);

    private final NetUUID microServiceID;
    private final String microServiceTypeName;
    private final NetUUID microServiceTypeID;
    private final MicroServicesManager microServiceManager;
    private final PriorityLinkedList<Packet> queue = new PriorityLinkedList<Packet>();
    private boolean busy = false;
    private Packet currentFrame = null;
    private final List<RepetitiveFrameEntry> repetitiveTasks = new ArrayList<RepetitiveFrameEntry>();
    private long lastRepetitiveCheck = 0;
    private final Map<Long, MessageEntry> journal = new LinkedHashMap<Long, MessageEntry>();
    private final MicroServicePeers microServicePeers = new MicroServicePeers(this);

    public boolean _ForTestOnly_pseudoSendEnabled = false;

    public MicroService(String microServiceTypeName, MicroServicesManager manager) {
        if (microServiceTypeName.length() > 8) {
            throw new IllegalArgumentException("Service Type Name should not be longer than 8 chars.");
        }
        this.microServiceTypeName = microServiceTypeName;
        this.microServiceManager = manager;
        this.microServiceID = microServiceManager.getNextMicroServiceID();
        byte[] typeData = new byte[8];
        for (int i = 0; i < this.microServiceTypeName.length(); i++) {
            typeData[i] = (byte) this.microServiceTypeName.charAt(i);
        }
        long typeID = Encoder.decodeInt32(typeData, 0);
        this.microServiceTypeID = new NetUUID(0,0,typeID,0);
        this.microServiceManager.registerMicroService(this);
        this.microServiceManager.registerForMulticast(this.microServiceTypeID.getUuidB(), this);
        registerRepetitiveMessage(10000, 10000, 0, timeoutIdentifier);
    }

    public void multicast(int msgType) {
        this.send(new Message(msgType, null), this.microServiceTypeID);
    }

    public void multicast(Message msg) {
        this.send(msg, this.microServiceTypeID);
    }

    public NetUUID getMicroServiceID() {
        return this.microServiceID;
    }

    public NetUUID getMicroServiceTypeID() {
        return this.microServiceTypeID;
    }

    public void addFrame(Packet p) {
        queue.add(p, p.getPriority());
    }

    public void checkForRepetitive() {
        if (System.currentTimeMillis() - lastRepetitiveCheck > 10000) {
            for (RepetitiveFrameEntry e : repetitiveTasks) {
                if (e.shouldExecute()) {
                    queue.add(e.frame, e.priority);
                }
            }
            lastRepetitiveCheck = System.currentTimeMillis();
        }
    }

    public void pop() {
        busy = true;
        currentFrame = queue.next();
    }

    public void run() {
        currentFrame.decode();
        if (currentFrame.getMessage() == timeoutIdentifier) {
            this.checkForTimeoutMessages();
        } else if (currentFrame.getSource().equals(Packet.PROTOCOL_ID_UNREACHABLE)) {
            processDestinationUnreachable((Message) currentFrame.getMessage(), currentFrame.getOriginalAddress());
        } else if (currentFrame.getMessage() instanceof ISideTask) {
            this.getMicroServiceManager().runSideTask((ISideTask) currentFrame.getMessage());
        } else {
            processMessage((Message) currentFrame.getMessage(), currentFrame.getSource(), currentFrame.getDestination());
        }
        currentFrame = null;
        synchronized (microServiceManager.getSyncObject()) {
            busy = false;
            microServiceManager.getSyncObject().notifyAll();
        }
    }

    public abstract void processDestinationUnreachable(Message message, NetUUID unreachableSource);

    public abstract void processMessage(Message message, NetUUID source, NetUUID destination);

    public abstract void start();

    public abstract String getName();

    public void send(Message msg, NetUUID destination) {
        if (_ForTestOnly_pseudoSendEnabled) {
            return;
        }

        if (destination.equals(this.microServiceID)) {
            processMessage(msg, destination, destination);
            return;
        }
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeObject(msg, ba);
        microServiceManager.getHabitat().send(ba.getData(), this.microServiceID, destination);
    }

    public void registerRepetitiveMessage(long interval, long intervalStart, int priority, Message message) {
        Packet p = new Packet(message, this.microServiceID);
        RepetitiveFrameEntry entry = new RepetitiveFrameEntry(p, interval, intervalStart, priority);
        if (entry.shouldExecute()) {
            queue.add(entry.frame, entry.priority);
            this.getMicroServiceManager().messageWasEnqueued();
        }
        repetitiveTasks.add(entry);
    }

    private static class RepetitiveFrameEntry {
        private Packet frame = null;
        private long interval = -1;
        private long intervalStart = -1;
        private long lastExecuted = System.currentTimeMillis();
        private boolean started = false;
        private int priority = 2;

        public RepetitiveFrameEntry(Packet _frame, long _interval, long _intervalStart, int _priority) {
            this.frame = _frame;
            this.interval = _interval;
            this.intervalStart = _intervalStart;
            this.priority = _priority;
        }

        public boolean shouldExecute() {
            if (System.currentTimeMillis() - lastExecuted > interval) {
                lastExecuted = System.currentTimeMillis();
                return true;
            }

            if (!started && (System.currentTimeMillis() - lastExecuted > intervalStart || intervalStart == 0)) {
                started = true;
                lastExecuted = System.currentTimeMillis();
                return true;
            }
            return false;
        }
    }

    protected MicroServicesManager getMicroServiceManager() {
        return this.microServiceManager;
    }

    public void checkForTimeoutMessages() {
        for (Iterator<MessageEntry> iter = journal.values().iterator(); iter.hasNext(); ) {
            MessageEntry e = iter.next();
            if (e.hasTimedOut()) {
                for (NetUUID peer : e.getPeers()) {
                    handleTimedOutMessage(e.getMessage(), peer);
                }
            }
        }
    }

    public void handleTimedOutMessage(Message message, NetUUID peer) {

    }

    public void addMessageEntry(MessageEntry entry) {
        this.journal.put(entry.getMessage().getMessageID(), entry);
    }

    public MessageEntry addUnicastJournal(Message m, NetUUID peer) {
        MessageEntry entry = new MessageEntry(m, peer, MessageEntry.DEFAULT_TIMEOUT);
        this.journal.put(m.getMessageID(), entry);
        return entry;
    }

    public MessageEntry addARPJournal(Message message, boolean includeSelf) {
        return this.microServicePeers.addARPJournal(message, includeSelf);
    }

    public MicroServicePeerEntry getPeerEntry(NetUUID source) {
        return this.microServicePeers.getPeerEntry(source);
    }

    public void replacePeerEntry(NetUUID source, MicroServicePeerEntry entry) {
        this.microServicePeers.replacePeerEntry(source, entry);
    }

    public void addPeerToARPJournal(Message m, NetUUID peer) {
        MessageEntry entry = journal.get(m.getMessageID());
        entry.addPeer(peer);
    }

    public MessageEntry getJournalEntry(Message m) {
        return this.journal.get(m.getMessageID());
    }

    public MessageEntry removeJournalEntry(Message m) {
        return this.journal.remove(m.getMessageID());
    }

    public Collection<MessageEntry> getJournalEntries() {
        return this.journal.values();
    }

    public boolean isBusy() {
        return this.busy;
    }

    public int getQueueSize() {
        return this.queue.size();
    }

    public NetUUID getAPeer() {
        return this.microServicePeers.getAPeer();
    }
}
