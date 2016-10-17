/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.NID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AdjacentMachineDiscovery extends ThreadNode{
    private final DatagramSocket datagramSocket;
    private final AdjacentMachineListener listener;
    private final NID NID;

    public  interface AdjacentMachineListener {
        void notifyAdjacentDiscovered(NID adjacentID, String host);
    }

    public AdjacentMachineDiscovery(NID NID, AdjacentMachineListener listener){
        super((ThreadNode)listener,"Adjacent Machine Discovery Listener");
        this.NID = NID;
        this.listener = listener;
        DatagramSocket socket=null;
        try {
            socket = new DatagramSocket(49999);
        } catch (SocketException e) {
            VLogger.error("Failed to open socket for discovery",e);
        }
        this.datagramSocket = socket;
    }

    public void shutdown(){
        super.shutdown();
        this.datagramSocket.close();
    }

    public void initialize(){}

    public void distruct(){
        this.datagramSocket.close();
    }

    public void execute() throws Exception{
        byte data[] = new byte[8];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        this.datagramSocket.receive(packet);
        processIncomingPacket(packet);
    }

    private void processIncomingPacket(DatagramPacket p){
        BytesArray ba = new BytesArray(p.getData());
        NID id = (NID) Encoder.getSerializerByClass(NID.class).decode(ba);
        if(!id.equals(NID)){
            listener.notifyAdjacentDiscovered(id,p.getAddress().getHostName());
        }
    }

    private static class AdjacentMachineDiscoveryPulse extends ThreadNode{

        private final NID localHost;

        private AdjacentMachineDiscoveryPulse(ThreadNode th,NID localHost){
            super(th,"Discovery Pluse");
            this.localHost = localHost;
        }

        public void initialize(){}
        public void distruct(){}

        public void execute() throws Exception{
            BytesArray ba = new BytesArray(new byte[8]);
            Encoder.getSerializerByClass(NID.class).encode(localHost, ba);
            byte data[] = ba.getData();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 49999);
            DatagramSocket s = new DatagramSocket();
            s.send(packet);
            Thread.sleep(10000);
            s.close();
        }
    }

}

