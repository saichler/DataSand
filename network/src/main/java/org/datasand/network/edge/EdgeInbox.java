/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.edge;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.datasand.codec.VLogger;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class EdgeInbox extends Thread{
    private final Map<UUID,InboxEntry> inbox = new ConcurrentHashMap<>();
    private final EdgeNode edgeNode;

    public EdgeInbox(EdgeNode edgeNode){
        this.edgeNode = edgeNode;
        this.setDaemon(true);
        this.start();
    }

    public void addFrame(EdgeFrame frame){
        InboxEntry inboxEntry = inbox.get(frame.getUuid());
        if(inboxEntry==null){
            inboxEntry = new InboxEntry();
            this.inbox.put(frame.getUuid(), inboxEntry);
        }
        inboxEntry.addFrame(frame);
    }

    public void shutdown(){
        this.inbox.clear();
    }

    public void run(){
        while(edgeNode.isRunning()){
            try{
                Thread.sleep(1000);
                for(InboxEntry entry:inbox.values()){
                    List<Integer> toRemove = new LinkedList<>();
                    for(MessageEntry msgEntry:entry.messages.values()){
                        if(msgEntry.isTimeout()){
                            System.out.println("Remove Incomplete Inbox Message "+msgEntry.msgID);
                            toRemove.add(msgEntry.msgID);
                        }
                    }
                    for(Integer msgID:toRemove){
                        entry.messages.remove(msgID);
                    }
                }
            }catch(Exception e){
                VLogger.error("Error in Inbox", e);
            }
        }
    }

    private class InboxEntry {
        private final Map<Integer,MessageEntry> messages = new ConcurrentHashMap<>();

        private InboxEntry(){
        }

        private void addFrame(EdgeFrame frame){
            MessageEntry msgEntry = this.messages.get(frame.getMessageID());
            if(msgEntry==null){
                msgEntry = new MessageEntry(frame.getMessageID());
                this.messages.put(frame.getMessageID(), msgEntry);
            }
            EdgeFrame completeFrame = msgEntry.addFrame(frame);
            if(completeFrame!=null){
                this.messages.remove(frame.getMessageID());
                edgeNode.handleFrame(completeFrame);
            }
        }
    }

    private class MessageEntry {
        private final Integer msgID;
        private final Map<Integer,EdgeFrame> frames = new ConcurrentHashMap<>();
        private long timestamp = System.currentTimeMillis();

        public MessageEntry(Integer _msgID){
            this.msgID = _msgID;
        }

        public EdgeFrame addFrame(EdgeFrame frame){
            this.timestamp = System.currentTimeMillis();
            ResultContainer o = edgeNode.getOutput(frame.getOrigMessageID(), false);
            if(o!=null){
                o.setTimestamp(System.currentTimeMillis());
            }
            this.frames.put(frame.getFrameID(), frame);
            EdgeFrame headFrame = frames.get(0);
            if(headFrame!=null){
                if(headFrame.getTotalFrameCount()==frames.size()){
                    EdgeFrame clonedFrame= frame.clone();
                    clonedFrame.setFrames(frames.values());
                    return clonedFrame;
                }
            }
            return null;
        }
        public boolean isTimeout(){
            if(System.currentTimeMillis()-timestamp>30000){
                return true;
            }
            return false;
        }
    }
}