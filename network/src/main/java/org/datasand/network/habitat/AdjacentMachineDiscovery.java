/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.network.HabitatID;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AdjacentMachineDiscovery extends Thread{
    private final DatagramSocket datagramSocket;
    private final HabitatID localHost;
    private final AdjacentMachineListener listener;
    private boolean running=true;

    public static interface AdjacentMachineListener {
        public void notifyAdjacentDiscovered(HabitatID adjacentID);
    }

    public AdjacentMachineDiscovery(HabitatID localHost, AdjacentMachineListener listener){
        this.setName("Adjacent Machine Discovery Listener");
        this.localHost = localHost;
        this.listener = listener;
        DatagramSocket socket=null;
        try {
            socket = new DatagramSocket(49999);
        } catch (SocketException e) {
            VLogger.error("Failed to open socket for discovery",e);
        }
        this.datagramSocket = socket;
        this.setDaemon(true);
        this.start();
    }

    public void shutdown(){
        this.running = false;
        this.datagramSocket.close();
    }

    public void run(){
        try {
            while (running) {
                byte data[] = new byte[8];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                this.datagramSocket.receive(packet);
                processIncomingPacket(packet);
            }

            this.datagramSocket.close();
        } catch(IOException e){
            VLogger.error("Discovery was close",e);
        }
    }

    private void processIncomingPacket(DatagramPacket p){
        BytesArray ba = new BytesArray(p.getData());
        HabitatID id = (HabitatID) Encoder.getSerializerByClass(HabitatID.class).decode(ba);
        if(!id.equals(localHost)){
            listener.notifyAdjacentDiscovered(id);
        }
    }

    private class AdjacentMachineDiscoveryPulse extends Thread{

        private AdjacentMachineDiscoveryPulse(){
            this.setName("Discovery Pluse");
            this.setDaemon(true);
            this.start();
        }

        public void run(){
            try {
                while (running) {
                    BytesArray ba = new BytesArray(new byte[8]);
                    Encoder.getSerializerByClass(HabitatID.class).encode(localHost, ba);
                    byte data[] = ba.getData();
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 49999);
                    DatagramSocket s = new DatagramSocket();
                    s.send(packet);
                    Thread.sleep(10000);
                    s.close();
                }
            }catch(IOException | InterruptedException e){
                VLogger.error("Error when trying to send pulse",e);
            }
        }
    }

}

