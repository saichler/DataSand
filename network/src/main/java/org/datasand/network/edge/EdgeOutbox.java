/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.edge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class EdgeOutbox extends Thread{
    public static final long TIMEOUT = 10000;
    private final EdgeNode edgeNode;
    private volatile Map<Integer,ResultContainer> pending = new HashMap<>();
    private final Map<UUID,Map<Integer,SentEntry>> sentDatagramPackets = new HashMap<>();
    private final List<EdgeFrame> outputQueue = new LinkedList<EdgeFrame>();

    public EdgeOutbox(EdgeNode edgeNode){
        super(edgeNode.getName()+" Outbox");
        this.edgeNode = edgeNode;
        this.setDaemon(true);
        this.start();
        new TimeoutThread(this);
    }

    public void shutdown(){
        this.pending.clear();
        this.sentDatagramPackets.clear();
    }

    public void addResultContainer(ResultContainer rc){
        rc.setTimestamp(System.currentTimeMillis());
        synchronized(this.pending){
            this.pending.put(rc.getFrame().getMessageID(),rc);
        }
    }

    public boolean isWaitingForReply(Integer msgID){
        return pending.containsKey(msgID);
    }

    public ResultContainer getResultContainer(int msgID,boolean remove){
        synchronized(pending){
            if(remove){
                return this.pending.remove(msgID);
            }else{
                return this.pending.get(msgID);
            }
        }
    }

    public void run(){
        EdgeFrame frame = null;
        while(edgeNode.isRunning()){
            synchronized(outputQueue){
                if(outputQueue.isEmpty())
                    try{
                        outputQueue.wait(2000);
                    }catch(InterruptedException e){
                        VLogger.error("Interrupted!",e);
                    }
                if(!outputQueue.isEmpty())
                    frame = outputQueue.remove(0);
            }
            if(frame!=null && this.edgeNode.isRunning()){
                try{
                    Map<Integer,SentEntry> sends = sentDatagramPackets.get(frame.getUuid());
                    if(sends==null){
                        sends = new ConcurrentHashMap<>();
                        synchronized (sentDatagramPackets) {
                            sentDatagramPackets.put(frame.getUuid(), sends);
                        }
                    }
                    SentEntry sentEntry = new SentEntry(frame);
                    if(frame.getDatagramPackets().length>100){
                        VLogger.info(this.getName()+":Start Sending "+frame.getDatagramPackets().length+" packets of message:"+frame.getMessageID());
                    }

                    for(DatagramPacket dp:frame.getDatagramPackets()){
                        edgeNode.sendDatagramPacket(dp);
                    }

                    sentEntry.setTimeStamp();
                    if(frame.getDatagramPackets().length>100){
                        VLogger.info(this.getName()+":Finished Sending "+frame.getDatagramPackets().length+" packets of message:"+frame.getMessageID());
                    }
                    sends.put(frame.getMessageID(), sentEntry);
                }catch(Exception e){
                    VLogger.error("Failed to send DatagramPacket", e);
                }
            }
            frame = null;
        }
    }

    public void send(EdgeFrame frame){
        synchronized(this.outputQueue){
            if(frame.getDatagramPackets().length>100){
                System.out.println("Sending message id:"+frame.getMessageID());
            }
            this.outputQueue.add(frame);
            this.outputQueue.notifyAll();
        }
    }

    public void handeAckPacket(DatagramPacket packet){
        byte data[] = packet.getData();
        UUID uuid = new UUID(Encoder.decodeInt64(data,EdgeFrame.B01_LOCATION_UUID),Encoder.decodeInt64(data,EdgeFrame.B01_LOCATION_UUID+8));
        int messageID = Encoder.decodeInt32(data, EdgeFrame.B02_LOCATION_MESSAGES_IDS);
        int frameID = Encoder.decodeInt32(data, EdgeFrame.B03_LOCATION_FRAME_ID);
        Map<Integer,SentEntry> sends = sentDatagramPackets.get(uuid);
        if(sends!=null){
            if(frameID==-1){
                sends.remove(messageID);
            }else{
                SentEntry sentEntry = sends.get(messageID);
                if(sentEntry!=null){
                    sentEntry.frame.setDatagramPacketToNull(frameID);
                    sentEntry.setTimeStamp();
                    if(sentEntry.frame.getTotalFrameCount()==0){
                        if(sentEntry.frame.getDatagramPackets().length>100){
                            System.out.println("Removing Message:"+messageID+" frame length:"+sentEntry.frame.getDatagramPackets().length);
                        }
                        sends.remove(messageID);
                    }
                }
            }
        }
    }

    private static class SentEntry {
        private final EdgeFrame frame;
        private long timeStamp = 0;
        private int retryCount[] = null;
        public SentEntry(EdgeFrame frame){
            this.frame = frame;
            this.retryCount = new int[this.frame.getDatagramPackets().length];
        }

        public void setTimeStamp(){
            this.timeStamp = System.currentTimeMillis();
        }

        public boolean hasTimeout(){
            if(System.currentTimeMillis()-timeStamp>2000){
                return true;
            }
            return false;
        }

        public boolean shouldRetry(int frameID){
            if(frameID==-1)
                frameID = 0;
            retryCount[frameID]++;
            if(retryCount[frameID]<3)
                return true;
            return false;
        }
    }

    private static class RemoveKey{
        private final UUID uuid;
        private final Integer msgID;

        public RemoveKey(UUID uuid,Integer msg){
            this.uuid = uuid;
            this.msgID = msg;
        }
    }

    private static class TimeoutThread extends Thread{

        private final EdgeOutbox outbox;

        public TimeoutThread(EdgeOutbox outbox){
            this.outbox = outbox;
            this.setName(outbox.getName()+" Timeout");
            this.setDaemon(true);
            this.start();
        }

        public void run(){
            while(outbox.edgeNode.isRunning()){
                try{Thread.sleep(1000);}catch(Exception err){}
                if(outbox.edgeNode.isRunning()){
                    List<RemoveKey> toRemove = new LinkedList<RemoveKey>();
                    synchronized(outbox.sentDatagramPackets) {
                        for (Map.Entry<UUID, Map<Integer, SentEntry>> uuidEntry : outbox.sentDatagramPackets.entrySet()) {
                            for (Map.Entry<Integer, SentEntry> messageEntry : uuidEntry.getValue().entrySet()) {
                                boolean shouldRemove = true;
                                SentEntry sentEntry = messageEntry.getValue();
                                if (!sentEntry.hasTimeout()) {
                                    continue;
                                }
                                for (int frameID = 0; frameID < sentEntry.frame.getDatagramPackets().length; frameID++) {
                                    if (sentEntry.frame.getDatagramPackets()[frameID] == null)
                                        continue;
                                    if (sentEntry.shouldRetry(frameID)) {
                                        try {
                                            outbox.edgeNode.sendDatagramPacket(sentEntry.frame.getDatagramPackets()[frameID]);
                                        } catch (IOException e) {
                                            VLogger.error(e.getMessage(),e);
                                        }
                                        shouldRemove = false;
                                    } else {
                                        sentEntry.frame.getDatagramPackets()[frameID] = null;
                                    }
                                }
                                if (shouldRemove) {
                                    toRemove.add(new RemoveKey(uuidEntry.getKey(), messageEntry.getKey()));
                                } else
                                    sentEntry.setTimeStamp();
                            }
                        }
                    }

                    for(RemoveKey rk:toRemove){
                        Map<Integer,SentEntry> sends = outbox.sentDatagramPackets.get(rk.uuid);
                        if(sends!=null){
                            sends.remove(rk.msgID);
                        }
                    }

                    List<ResultContainer> myPending = new LinkedList<ResultContainer>();
                    synchronized(outbox.pending){
                        myPending.addAll(outbox.pending.values());
                    }

                    for(ResultContainer o:myPending){
                        if(System.currentTimeMillis()-o.getTimestamp()>=TIMEOUT*3){
                            o.setTimeout();
                            if(o.shouldRetry()){
                                outbox.edgeNode.send(o);
                            }else{
                                outbox.getResultContainer(o.getMessageID(), true);
                            }
                            //can happen if the entity was shutdown while the outbox was working.
                            if(outbox.edgeNode!=null){
                                outbox.edgeNode.resultContainerReceived(o);
                            }
                        }
                    }
                }
            }
        }
    }
}