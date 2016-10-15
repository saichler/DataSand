/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class MicroService implements Runnable {

    private final int microServiceID;
    private final NetUUID microServiceGroup;
    private final MicroServicesManager microServiceManager;
    private final  PriorityLinkedList<Packet> queue = new PriorityLinkedList<Packet>();
    private boolean busy = false;
    private Packet currentFrame = null;
    private final List<RepetitiveFrameEntry> repetitiveTasks = new ArrayList<RepetitiveFrameEntry>();
    private long lastRepetitiveCheck = 0;
    private final Map<Long,MessageEntry> journal = new LinkedHashMap<Long, MessageEntry>();
    private static final Message timeoutIdentifier = new Message(-1,-1,-1,-1,null);
    private final MicroServicePeers microServicePeers = new MicroServicePeers(this);

    public boolean _ForTestOnly_pseudoSendEnabled = false;

    static {
        Encoder.registerSerializer(Message.class, new Message(-1,-1,-1,-1,null));
    }

    public MicroService(int microServiceGroup, MicroServicesManager manager) {
        this.microServiceManager = manager;
        this.microServiceID = microServiceManager.getNextMicroServiceID();
        this.microServiceGroup = new NetUUID(0,microServiceGroup);
        this.microServiceManager.registerMicroService(this);
        this.microServiceManager.registerForMulticast(microServiceGroup,this);
        registerRepetitiveMessage(10000, 10000, 0, timeoutIdentifier);
    }

    public void multicast(int msgType){
        this.send(new Message(this.microServiceID,(int)this.microServiceGroup.getB(),msgType,null), this.microServiceGroup);
    }

    public void multicast(Message msg){
        this.send(msg, this.microServiceGroup);
    }

    public int getMicroServiceID() {
        return this.microServiceID;
    }

    public NetUUID getMicroServiceGroup(){
        return this.microServiceGroup;
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
        if(currentFrame.getMessage()==timeoutIdentifier){
            this.checkForTimeoutMessages();
        }else
        if(currentFrame.getSource().equals(Packet.PROTOCOL_ID_UNREACHABLE)){
            processDestinationUnreachable((Message)currentFrame.getMessage(),currentFrame.getUnreachableOrigAddress());
        }else
        if (currentFrame.getMessage() instanceof ISideTask) {
            this.getMicroServiceManager().runSideTask((ISideTask) currentFrame.getMessage());
        } else {
            processMessage((Message)currentFrame.getMessage(),currentFrame.getSource(),currentFrame.getDestination());
        }
        currentFrame = null;
        synchronized (microServiceManager.getSyncObject()) {
            busy = false;
            microServiceManager.getSyncObject().notifyAll();
        }
    }

    public NetUUID getNetUUID(){
        return this.getMicroServiceManager().getHabitat().getNetUUID();
    }

    public abstract void processDestinationUnreachable(Message message,NetUUID unreachableSource);
    public abstract void processMessage(Message message, NetUUID source, NetUUID destination);
    public abstract void start();
    public abstract String getName();

    public void send(Message msg, NetUUID destination) {
        if(_ForTestOnly_pseudoSendEnabled) {
            return;
        }

        if(destination.equals(getNetUUID())
                && this.getMicroServiceID()==msg.getDestination()){
            processMessage(msg, destination, destination);
            return;
        }
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeObject(msg, ba);
        microServiceManager.getHabitat().send(ba.getData(), this.getNetUUID(), destination);
    }

    public void registerRepetitiveMessage(long interval,long intervalStart,int priority,Message message){
        Packet p = new Packet(message,getNetUUID());
        RepetitiveFrameEntry entry = new RepetitiveFrameEntry(p, interval,intervalStart, priority);
        if(entry.shouldExecute()){
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

        public RepetitiveFrameEntry(Packet _frame, long _interval,long _intervalStart, int _priority) {
            this.frame = _frame;
            this.interval = _interval;
            this.intervalStart = _intervalStart;
            this.priority = _priority;
        }

        public boolean shouldExecute() {
            if (System.currentTimeMillis() - lastExecuted > interval){
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

    public void checkForTimeoutMessages(){
        for(Iterator<MessageEntry> iter=journal.values().iterator();iter.hasNext();){
            MessageEntry e = iter.next();
            if(e.hasTimedOut()){
                for(NetUUID peer:e.getPeers()){
                    handleTimedOutMessage(e.getMessage(),peer);
                }
            }
        }
    }

    public void handleTimedOutMessage(Message message,NetUUID peer){

    }

    public void addMessageEntry(MessageEntry entry){
        this.journal.put(entry.getMessage().getMessageID(), entry);
    }

    public MessageEntry addUnicastJournal(Message m,NetUUID peer){
        MessageEntry entry = new MessageEntry(m, peer, MessageEntry.DEFAULT_TIMEOUT);
        this.journal.put(m.getMessageID(), entry);
        return entry;
    }

    public MessageEntry addARPJournal(Message message,boolean includeSelf){
        return this.microServicePeers.addARPJournal(message,includeSelf);
    }

    public MicroServicePeerEntry getPeerEntry(NetUUID source){
        return this.microServicePeers.getPeerEntry(source);
    }

    public void replacePeerEntry(NetUUID source, MicroServicePeerEntry entry){
        this.microServicePeers.replacePeerEntry(source,entry);
    }

    public void addPeerToARPJournal(Message m,NetUUID peer){
        MessageEntry entry = journal.get(m.getMessageID());
        entry.addPeer(peer);
    }

    public MessageEntry getJournalEntry(Message m){
        return this.journal.get(m.getMessageID());
    }

    public MessageEntry removeJournalEntry(Message m){
        return this.journal.remove(m.getMessageID());
    }

    public Collection<MessageEntry> getJournalEntries(){
        return this.journal.values();
    }

    public boolean isBusy(){
        return this.busy;
    }

    public int getQueueSize(){
        return this.queue.size();
    }

    public NetUUID getAPeer(){
        return this.microServicePeers.getAPeer();
    }
}
