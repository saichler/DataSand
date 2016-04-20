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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import org.datasand.codec.VLogger;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 4/20/16.
 */
public class EdgeNode extends Thread implements ResultContainerCallback {

    private final DatagramSocket datagramSocket;
    private final Map<UUID, InetAddress> adjacentsEdgeNodes = new HashMap<>();
    private boolean running = true;
    private final LinkedList<DatagramPacket> incomingDatagranPackets = new LinkedList<DatagramPacket>();
    private int nextMessageID = 100;
    private final EdgeOutbox outbox = new EdgeOutbox(this);
    private final EdgeInbox inbox;
    private final UUID edgeUUID;

    private IFrameHandler handler = null;

    public EdgeNode(){
        this.inbox = new EdgeInbox(this);
        DatagramSocket socket = null;
        for(int i=40000;i<49000;i++){
            boolean success = false;
            try{
                socket = new DatagramSocket(i,InetAddress.getByName("0.0.0.0"));
                new DatagramPacketProcessor(this);
                success = true;
            }catch(Exception err){}
            if(success){
                VLogger.info("Opened socket on port "+socket.getLocalPort());
                this.start();
                break;
            }
        }
        this.datagramSocket = socket;
        this.edgeUUID= UUID.randomUUID();
    }

    public boolean isRunning(){
        return this.running;
    }

    public void addEdgeNodeAjacent(UUID uuid,InetAddress source){
        this.adjacentsEdgeNodes.put(uuid, source);
    }

    public boolean isKnownAdacent(UUID uuid){
        if(uuid.equals(this.edgeUUID)) {
            return true;
        }
        return this.adjacentsEdgeNodes.containsKey(uuid);
    }

    public int getNextMessageID(){
        return this.nextMessageID++;
    }

    public void shutdown(){
        this.running = false;
        this.outbox.shutdown();
        this.inbox.shutdown();
        try{datagramSocket.close();}catch(Exception err){}
    }

    public ResultContainer send(ResultContainer rc){
        this.outbox.addResultContainer(rc);
        if(rc.isSynchronize()){
            synchronized(rc){
                send(rc.getFrame());
                try{rc.wait(EdgeOutbox.TIMEOUT*3);}catch(Exception err){}
                this.outbox.getResultContainer(rc.getMessageID(), true);
                return rc;
            }
        }else{
            send(rc.getFrame());
        }
        return rc;
    }

    public void send(EdgeFrame frame){
        this.outbox.send(frame);
    }

    protected void sendDatagramPacket(DatagramPacket dp) throws IOException{
        synchronized (this.datagramSocket) {
            this.datagramSocket.send(dp);
        }
    }

    public void run(){
        while(running){
            byte data[] = new byte[EdgeFrame.MAX_FRAME_SIZE];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try{
                datagramSocket.receive(packet);
                enqueuePacket(packet);
            }catch(Exception err){
                if(running)
                    VLogger.error("Socket issue",err);
            }
        }
    }

    private void enqueuePacket(DatagramPacket packet){
        synchronized(incomingDatagranPackets){
            incomingDatagranPackets.add(packet);
            incomingDatagranPackets.notifyAll();
        }
    }

    private static class DatagramPacketProcessor extends Thread{

        private final EdgeNode edgeNode;

        public DatagramPacketProcessor(EdgeNode edgeNode){
            super(edgeNode.getName()+" Frame Processor");
            this.edgeNode = edgeNode;
            this.setDaemon(true);
            this.start();
        }

        public void run(){
            DatagramPacket dp = null;
            while(edgeNode.running){
                synchronized(edgeNode.incomingDatagranPackets){
                    if(edgeNode.incomingDatagranPackets.isEmpty()){
                        try {
                            edgeNode.incomingDatagranPackets.wait(2000);
                        } catch (InterruptedException e) {
                            VLogger.error("Interrupted",e);
                        }
                    }
                    if(!edgeNode.incomingDatagranPackets.isEmpty()){
                        dp = edgeNode.incomingDatagranPackets.removeFirst();
                    }
                }
                if(dp!=null){
                    processDatagramPacket(dp);
                }
                dp = null;
            }
        }

        private void processDatagramPacket(DatagramPacket packet){
            if(EdgeFrame.isAckFrame(packet)){
                edgeNode.outbox.handeAckPacket(packet);
            }else{
                EdgeFrame frame = new EdgeFrame(packet);
                this.edgeNode.sendAcknowledgeFrame(frame);
                if(frame.getFrameID()!=-1){
                    edgeNode.inbox.addFrame(frame);
                }else{
                    edgeNode.handleFrame(frame);
                }
            }
        }
    }

    protected void handleFrame(EdgeFrame frame){
        this.handler.handleFrame(frame);
    }

    private void sendAcknowledgeFrame(EdgeFrame sourceFrame){
        int frameID = sourceFrame.getFrameID();
        if(frameID==-1){
            frameID = 0;
        }
        DatagramPacket dp = new DatagramPacket(sourceFrame.getAckData(), sourceFrame.getAckData().length,sourceFrame.getDatagramPackets()[0].getAddress(),sourceFrame.getDatagramPackets()[0].getPort());
        try{
            sendDatagramPacket(dp);
        }catch(IOException err){
            VLogger.error(err.getMessage(),err);
        }
    }

    public ResultContainer getOutput(int msgID,boolean remove){
        return this.outbox.getResultContainer(msgID, remove);
    }

    public InetAddress resolveAddress(UUID uuid){
        return this.adjacentsEdgeNodes.get(uuid);
    }

    @Override
    public void resultContainerReceived(ResultContainer resultContainer) {

    }
}