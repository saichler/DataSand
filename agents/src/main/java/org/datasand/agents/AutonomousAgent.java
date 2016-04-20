/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.ServiceID;
import org.datasand.network.ServiceNodeConnection;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public abstract class AutonomousAgent implements Runnable {

    private ServiceID agentID = null;
    private ServiceID arpGroup = null;
    private AutonomousAgentManager manager = null;
    protected PriorityLinkedList<Packet> incoming = new PriorityLinkedList<Packet>();
    protected boolean working = false;
    private Packet currentFrame = null;
    private List<RepetitiveFrameEntry> repetitiveTasks = new ArrayList<RepetitiveFrameEntry>();
    private long lastRepetitiveCheck = 0;
    private Map<Long,MessageEntry> journal = new LinkedHashMap<Long, MessageEntry>();
    private Map<ServiceID,PeerEntry> peers = new HashMap<ServiceID,PeerEntry>();
    private static final Message timeoutID = new Message();

    public boolean _ForTestOnly_pseudoSendEnabled = false;

    static {
        Encoder.registerSerializer(Message.class, new Message());
    }

    public AutonomousAgent(int subSystemID,AutonomousAgentManager _manager) {
        this.agentID = new ServiceID(_manager.getServiceNode().getLocalHost().getIPv4Address(),_manager.getServiceNode().getLocalHost().getPort(), subSystemID);
        this.manager = _manager;
        this.manager.registerAgent(this);
        registerRepetitiveMessage(10000, 10000, 0, timeoutID);
    }

    public void setARPGroup(int group){
        this.arpGroup = new ServiceID(ServiceNodeConnection.PROTOCOL_ID_BROADCAST.getIPv4Address(),group,group);
        this.getAgentManager().registerForMulticast(group, this);
    }

    public ServiceID getARPGroup(){
        return this.arpGroup;
    }

    public void sendARP(int msgType){
        this.send(new Message(msgType,null), arpGroup);
    }

    public void sendARP(Message msg){
        this.send(msg, arpGroup);
    }

    public ServiceID getAgentID() {
        return this.agentID;
    }

    public void addFrame(Packet p) {
        incoming.add(p, p.getPriority());
    }

    public void checkForRepetitive() {
        if (System.currentTimeMillis() - lastRepetitiveCheck > 10000) {
            for (RepetitiveFrameEntry e : repetitiveTasks) {
                if (e.shouldExecute()) {
                    incoming.add(e.frame, e.priority);
                }
            }
            lastRepetitiveCheck = System.currentTimeMillis();
        }
    }

    public void pop() {
        working = true;
        currentFrame = incoming.next();
    }

    public void run() {
        currentFrame.decode();
        if(currentFrame.getMessage()==timeoutID){
            this.checkForTimeoutMessages();
        }else
        if(currentFrame.getSource().getSubSystemID()== ServiceNodeConnection.DESTINATION_UNREACHABLE){
            processDestinationUnreachable((Message)currentFrame.getMessage(),currentFrame.getUnreachableOrigAddress());
        }else
        if (currentFrame.getMessage() instanceof ISideTask) {
            this.getAgentManager().runSideTask((ISideTask) currentFrame.getMessage());
        } else {
            processMessage((Message)currentFrame.getMessage(),currentFrame.getSource(),currentFrame.getDestination());
        }
        currentFrame = null;
        synchronized (manager.getSyncObject()) {
            working = false;
            manager.getSyncObject().notifyAll();
        }
    }

    public abstract void processDestinationUnreachable(Message message,ServiceID unreachableSource);
    public abstract void processMessage(Message message, ServiceID source, ServiceID destination);
    public abstract void start();
    public abstract String getName();

    public void send(Message obj, ServiceID destination) {
        if(_ForTestOnly_pseudoSendEnabled) return;
        if(this.getAgentID().equals(destination)){
            processMessage(obj, destination, destination);
            return;
        }
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeObject(obj, ba);
        manager.getServiceNode().send(ba.getData(), this.agentID, destination);
    }

    public void send(byte data[], ServiceID destination) {
        manager.getServiceNode().send(data, this.agentID, destination);
    }

    public void registerRepetitiveMessage(long interval,long intervalStart,int priority,Message message){
        Packet p = new Packet(message,this.getAgentID());
        RepetitiveFrameEntry entry = new RepetitiveFrameEntry(p, interval,intervalStart, priority);
        if(entry.shouldExecute()){
            incoming.add(entry.frame, entry.priority);
            this.getAgentManager().messageWasEnqueued();
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

    protected AutonomousAgentManager getAgentManager() {
        return this.manager;
    }

    public void checkForTimeoutMessages(){
        for(Iterator<MessageEntry> iter=journal.values().iterator();iter.hasNext();){
            MessageEntry e = iter.next();
            if(e.hasTimedOut()){
                for(ServiceID peer:e.getPeers()){
                    handleTimedOutMessage(e.getMessage(),peer);
                }
            }
        }
    }

    public void handleTimedOutMessage(Message message,ServiceID peer){

    }

    public void addMessageEntry(MessageEntry entry){
        this.journal.put(entry.getMessage().getMessageID(), entry);
    }

    public MessageEntry addUnicastJournal(Message m,ServiceID peer){
        MessageEntry entry = new MessageEntry(m, peer, MessageEntry.DEFAULT_TIMEOUT);
        this.journal.put(m.getMessageID(), entry);
        return entry;
    }

    public MessageEntry addARPJournal(Message message,boolean includeSelf){
        if(this.peers.size()>0 || includeSelf){
            MessageEntry entry = new MessageEntry(message);
            boolean hasOnePeer = false;
            for(PeerEntry e:this.peers.values()){
                if(!e.isUnreachable()){
                    hasOnePeer=true;
                    entry.addPeer(e.getServiceID());
                }
            }
            if(includeSelf){
                hasOnePeer = true;
                entry.addPeer(this.getAgentID());
            }
            if(hasOnePeer)
                this.addMessageEntry(entry);
            return entry;
        }
        return null;
    }

    public PeerEntry getPeerEntry(ServiceID source){
        if(source.equals(this.getAgentID())) return null;
        PeerEntry peerEntry = peers.get(source);
        if(peerEntry==null){
            System.out.println("Add Source-"+source+" Count="+(peers.size()+1));
            peerEntry = new PeerEntry(source);
            peers.put(source, peerEntry);
        }
        return peerEntry;
    }

    public void replacePeerEntry(ServiceID source, PeerEntry entry){
        this.peers.put(source, entry);
    }

    public void addPeerToARPJournal(Message m,ServiceID peer){
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
}
