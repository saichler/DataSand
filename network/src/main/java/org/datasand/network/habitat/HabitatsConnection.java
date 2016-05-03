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
import org.datasand.network.ConnectionID;
import org.datasand.network.HabitatID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HabitatsConnection extends Thread {

    public static final int DESTINATION_UNREACHABLE = 9999;
    public static final int DESTINATION_BROADCAST = 10;

    public static final HabitatID PROTOCOL_ID_UNREACHABLE = new HabitatID(0, 0,DESTINATION_UNREACHABLE);
    public static final HabitatID PROTOCOL_ID_BROADCAST = new HabitatID(0, 0,DESTINATION_BROADCAST);

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ServicesHabitat servicesHabitat;
    private final PriorityLinkedList<byte[]> incoming = new PriorityLinkedList<byte[]>();
    private boolean running = true;
    private final boolean unicast;

    private final int intAddress;
    private final int intPort;

    public HabitatsConnection(ServicesHabitat servicesHabitat, InetAddress addr, int port, boolean unicastOnly) {
        this.servicesHabitat = servicesHabitat;
        this.setDaemon(true);
        this.setName(servicesHabitat.getName()+" Connection");
        Socket tmpSocket = null;
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;

        try {
            tmpSocket = new Socket(addr, port);
            tmpIn = new DataInputStream(new BufferedInputStream(tmpSocket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(tmpSocket.getOutputStream()));
            tmpOut.writeInt(this.servicesHabitat.getLocalHost().getIPv4Address());
            tmpOut.writeInt(this.servicesHabitat.getLocalHost().getPort());
            if(unicastOnly){
                tmpOut.writeInt(1);
            }else{
                tmpOut.writeInt(0);
            }
            tmpOut.flush();
        } catch(IOException e){
            VLogger.error("Failed to open socket",e);
        }
        HabitatID destID = HabitatID.valueOf(addr.getHostAddress()+":"+port+":0");
        this.intAddress = destID.getIPv4Address();
        this.intPort = port;
        this.socket = tmpSocket;
        this.in = tmpIn;
        this.out = tmpOut;
        this.unicast = unicastOnly;
        this.start();
    }

    public HabitatsConnection(ServicesHabitat servicesHabitat, InetAddress addr, int port) {
        this(servicesHabitat,addr,port,false);
    }

    public HabitatsConnection(ServicesHabitat servicesHabitat, Socket socket) {
        this.socket = socket;
        this.servicesHabitat = servicesHabitat;
        this.setName(servicesHabitat.getName()+" Connection");
        this.setDaemon(true);
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;
        int unicastOnly = 0;
        int port = -1;
        int addr = -1;
        try {
            tmpIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            addr = tmpIn.readInt();
            port = tmpIn.readInt();
            unicastOnly = tmpIn.readInt();
        } catch (Exception e) {
            VLogger.error("Failed to use input/output of socket",e);
        }
        this.intAddress = addr;
        this.intPort = port;
        this.in = tmpIn;
        this.out = tmpOut;
        if(unicastOnly==1){
            unicast = true;
        }else{
            unicast = false;
        }
        this.start();
    }

    public boolean isUnicast(){
        return this.unicast;
    }

    public int getIntAddress(){
        return this.intAddress;
    }

    public int getIntPort(){
        return this.intPort;
    }

    public ConnectionID getConnectionKey() {
        return new ConnectionID(this.intAddress,this.intPort,0,this.servicesHabitat.getLocalHost().getIPv4Address(),this.servicesHabitat.getLocalHost().getPort(),0);
    }

    public boolean isASide(){
        ConnectionID cID = getConnectionKey();
        if(cID.getaSide().equals(new HabitatID(this.intAddress,this.intPort,0))){
            return true;
        }
        return false;
    }


    public BytesArray sendPacket(BytesArray ba) throws IOException {
        byte data[] = ba.getBytes();
        synchronized (out) {
            if(running){
                try{
                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();
                    return null;
                }catch(SocketException serr){
                    VLogger.info("Connection was probably terminated for "+socket.getInetAddress().getHostName()+":"+socket.getPort());
                    this.shutdown();
                    return markAsUnreachable(ba);
                }
            }else{
                return markAsUnreachable(ba);
            }
        }
    }

    public void shutdown() {
        this.running = false;
        try {
            this.socket.close();
        } catch (Exception err) {
        }
    }

    public void run() {
        HabitatID id = null;

        new Switch();

        try {
            while (socket != null && !socket.isClosed() && running) {
                int size = in.readInt();
                byte data[] = new byte[size];
                in.readFully(data);
                synchronized (incoming) {
                    incoming.add(data, data[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] / 2);
                    incoming.notifyAll();
                }
            }
        } catch (Exception err) {
            if(this.running) {
                VLogger.error("Failed to read from socket", err);
            }
        }
        VLogger.info(this.getName()+" was closed.");
    }

    public static final BytesArray markAsUnreachable(BytesArray ba) {
        byte[] mark = new byte[ba.getBytes().length+Packet.PACKET_SOURCE_LENGHT];
        //copy all bytes
        System.arraycopy(ba.getBytes(), 0, mark, 0, ba.getBytes().length);
        //copy data to +8
        System.arraycopy(ba.getBytes(),Packet.PACKET_DATA_LOCATION,mark,Packet.PACKET_DATA_LOCATION+Packet.PACKET_SOURCE_LENGHT,
                ba.getBytes().length-Packet.PACKET_DATA_LOCATION);
        //copy destionation to begin of data
        System.arraycopy(ba.getBytes(),Packet.PACKET_DEST_LOCATION,mark,Packet.PACKET_DATA_LOCATION,Packet.PACKET_DEST_LENGTH);
        //copy source to destination
        System.arraycopy(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION,mark,Packet.PACKET_DEST_LOCATION,Packet.PACKET_SOURCE_LENGHT);
        //mark source as unreachable
        PROTOCOL_ID_UNREACHABLE.encode(PROTOCOL_ID_UNREACHABLE, mark,Packet.PACKET_SOURCE_LOCATION);
        return new BytesArray(mark);
    }


    public static final BytesArray addUnreachableAddressForMulticast(BytesArray ba,int destAddress,int destPort){
        byte[] data = ba.getBytes();
        byte[] _unreachable = new byte[data.length+6];
        System.arraycopy(data, 0, _unreachable,0, data.length);
        Encoder.encodeInt32(destAddress, _unreachable, data.length);
        Encoder.encodeInt16(destPort, _unreachable, data.length+4);
        return new BytesArray(_unreachable);
    }

    private class Switch extends Thread {

        public Switch() {
            this.setName("Switch - " + HabitatsConnection.this.getName());
            this.setDaemon(true);
            this.start();
        }

        public void run() {
            while (running) {
                byte packetData[] = null;
                synchronized (incoming) {
                    if (incoming.size() == 0) {
                        try {
                            incoming.wait(5000);
                        } catch (Exception err) {
                        }
                    }
                    if (incoming.size() > 0) {
                        packetData = incoming.next();
                    }
                }

                if (packetData != null && packetData.length > 0) {
                    BytesArray ba = new BytesArray(packetData);
                    int destAddr = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_DEST_LOCATION);
                    int destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
                    if (destAddr == 0) {
                        if (servicesHabitat.getLocalHost().getPort() != 50000) {
                            servicesHabitat.receivedPacket(ba);
                        } else {
                            servicesHabitat.broadcast(ba);
                        }
                    } else if (destAddr == servicesHabitat.getLocalHost().getIPv4Address() && destPort == servicesHabitat.getLocalHost().getPort()) {
                        servicesHabitat.receivedPacket(ba);
                    } else if (destAddr == servicesHabitat.getLocalHost().getIPv4Address() && servicesHabitat.getLocalHost().getPort() == 50000 && destPort != 50000) {
                        HabitatsConnection other = servicesHabitat.getNodeConnection(destAddr, destPort,true);
                        if (other != null && other.running) {
                            try {
                                other.sendPacket(ba);
                            } catch (Exception err) {
                                err.printStackTrace();
                            }
                        } else {
                            ba = markAsUnreachable(ba);
                            destAddr = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_DEST_LOCATION);
                            destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
                            HabitatsConnection source = servicesHabitat.getNodeConnection(destAddr, destPort,true);
                            if (source != null) {
                                try {
                                    source.sendPacket(ba);
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            } else
                                System.err.println("Source unreachable:"
                                        + new HabitatID(destAddr, destPort,
                                                Encoder.decodeInt16(ba.getBytes(), 16)));
                        }
                    } else if (destAddr != servicesHabitat.getLocalHost().getIPv4Address() && servicesHabitat.getLocalHost().getPort() == 50000) {
                        HabitatsConnection other = servicesHabitat.getNodeConnection(destAddr, 50000,true);
                        if (other != null) {
                            try {
                                other.sendPacket(ba);
                            } catch (Exception err) {
                                err.printStackTrace();
                            }
                        } else {
                            ba = markAsUnreachable(ba);
                            destAddr = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_DEST_LOCATION);
                            destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
                            HabitatsConnection source = servicesHabitat.getNodeConnection(destAddr, destPort,true);
                            if (source != null) {
                                try {
                                    source.sendPacket(ba);
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            } else
                                System.err.println("Source unreachable:"
                                        + new HabitatID(destAddr, destPort,
                                                Encoder.decodeInt16(ba.getBytes(), 16)));
                        }
                    }

                }
            }
            System.out.println(this.getName()+" end.");
        }
    }
}