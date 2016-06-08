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
import org.datasand.network.HabitatID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;
import org.datasand.network.habitat.HabitatsConnection;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class MicroService implements Runnable {

    private final HabitatID microServiceID;
    private final HabitatID microServiceGroup;
    private final MicroServicesManager microServiceManager;
    private final  PriorityLinkedList<Packet> queue = new PriorityLinkedList<Packet>();
    private boolean busy = false;
    private Packet currentFrame = null;
    private final List<RepetitiveFrameEntry> repetitiveTasks = new ArrayList<RepetitiveFrameEntry>();
    private long lastRepetitiveCheck = 0;
    private final Map<Long,MessageEntry> journal = new LinkedHashMap<Long, MessageEntry>();
    private static final Message timeoutIdentifier = new Message();
    private final MicroServicePeers microServicePeers = new MicroServicePeers(this);

    public boolean _ForTestOnly_pseudoSendEnabled = false;

    static {
        Encoder.registerSerializer(Message.class, new Message());
    }

    public MicroService(int microServiceGroup, MicroServicesManager manager) {
        this.microServiceManager = manager;
        this.microServiceID = new HabitatID(microServiceManager.getHabitat().getLocalHost().getIPv4Address(),
                microServiceManager.getHabitat().getLocalHost().getPort(),
                microServiceManager.getNextMicroServiceID());
        this.microServiceGroup = new HabitatID(HabitatsConnection.PROTOCOL_ID_BROADCAST.getIPv4Address(),microServiceGroup,microServiceGroup);
        this.microServiceManager.registerMicroService(this);
        this.microServiceManager.registerForMulticast(microServiceGroup,this);
        registerRepetitiveMessage(10000, 10000, 0, timeoutIdentifier);
    }

    public void multicast(int msgType){
        this.send(new Message(msgType,null), this.microServiceGroup);
    }

    public void multicast(Message msg){
        this.send(msg, this.microServiceGroup);
    }

    public HabitatID getMicroServiceID() {
        return this.microServiceID;
    }

    public HabitatID getMicroServiceGroup(){
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
        if(currentFrame.getSource().getServiceID()== HabitatsConnection.DESTINATION_UNREACHABLE){
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

    public abstract void processDestinationUnreachable(Message message,HabitatID unreachableSource);
    public abstract void processMessage(Message message, HabitatID source, HabitatID destination);
    public abstract void start();
    public abstract String getName();

    public void send(Message obj, HabitatID destination) {
        if(_ForTestOnly_pseudoSendEnabled) return;
        if(this.getMicroServiceID().equals(destination)){
            processMessage(obj, destination, destination);
            return;
        }
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeObject(obj, ba);
        microServiceManager.getHabitat().send(ba.getData(), this.microServiceID, destination);
    }

    public void send(byte data[], HabitatID destination) {
        microServiceManager.getHabitat().send(data, this.microServiceID, destination);
    }

    public void registerRepetitiveMessage(long interval,long intervalStart,int priority,Message message){
        Packet p = new Packet(message,this.getMicroServiceID());
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
                for(HabitatID peer:e.getPeers()){
                    handleTimedOutMessage(e.getMessage(),peer);
                }
            }
        }
    }

    public void handleTimedOutMessage(Message message,HabitatID peer){

    }

    public void addMessageEntry(MessageEntry entry){
        this.journal.put(entry.getMessage().getMessageID(), entry);
    }

    public MessageEntry addUnicastJournal(Message m,HabitatID peer){
        MessageEntry entry = new MessageEntry(m, peer, MessageEntry.DEFAULT_TIMEOUT);
        this.journal.put(m.getMessageID(), entry);
        return entry;
    }

    public MessageEntry addARPJournal(Message message,boolean includeSelf){
        return this.microServicePeers.addARPJournal(message,includeSelf);
    }

    public MicroServicePeerEntry getPeerEntry(HabitatID source){
        return this.microServicePeers.getPeerEntry(source);
    }

    public void replacePeerEntry(HabitatID source, MicroServicePeerEntry entry){
        this.microServicePeers.replacePeerEntry(source,entry);
    }

    public void addPeerToARPJournal(Message m,HabitatID peer){
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

    public HabitatID getAPeer(){
        return this.microServicePeers.getAPeer();
    }
}
