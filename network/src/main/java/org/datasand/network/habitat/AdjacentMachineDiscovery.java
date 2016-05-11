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
import org.datasand.network.HabitatID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AdjacentMachineDiscovery extends ThreadNode{
    private final DatagramSocket datagramSocket;
    private final HabitatID localHost;
    private final AdjacentMachineListener listener;

    public static interface AdjacentMachineListener {
        public void notifyAdjacentDiscovered(HabitatID adjacentID);
    }

    public AdjacentMachineDiscovery(HabitatID localHost, AdjacentMachineListener listener){
        super((ThreadNode)listener,"Adjacent Machine Discovery Listener");
        this.localHost = localHost;
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
        HabitatID id = (HabitatID) Encoder.getSerializerByClass(HabitatID.class).decode(ba);
        if(!id.equals(localHost)){
            listener.notifyAdjacentDiscovered(id);
        }
    }

    private static class AdjacentMachineDiscoveryPulse extends ThreadNode{

        private final HabitatID localHost;

        private AdjacentMachineDiscoveryPulse(ThreadNode th,HabitatID localHost){
            super(th,"Discovery Pluse");
            this.localHost = localHost;
        }

        public void initialize(){}
        public void distruct(){}

        public void execute() throws Exception{
            BytesArray ba = new BytesArray(new byte[8]);
            Encoder.getSerializerByClass(HabitatID.class).encode(localHost, ba);
            byte data[] = ba.getData();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 49999);
            DatagramSocket s = new DatagramSocket();
            s.send(packet);
            Thread.sleep(10000);
            s.close();
        }
    }

}

