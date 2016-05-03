/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;
import org.datasand.network.ServiceID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServiceNodeConnection extends Thread {

    public static final int DESTINATION_UNREACHABLE = 9999;
    public static final int DESTINATION_BROADCAST = 10;

    public static final ServiceID PROTOCOL_ID_UNREACHABLE = new ServiceID(0, 0,DESTINATION_UNREACHABLE);
    public static final ServiceID PROTOCOL_ID_BROADCAST = new ServiceID(0, 0,DESTINATION_BROADCAST);

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ServiceNode serviceNode;
    private final PriorityLinkedList<byte[]> incoming = new PriorityLinkedList<byte[]>();
    private boolean running = true;
    private boolean aSide = false;
    private final boolean unicast;

    private final int intAddress;
    private final int intPort;

    public ServiceNodeConnection(ServiceNode serviceNode, InetAddress addr, int port, boolean unicastOnly) {
        this.serviceNode = serviceNode;
        this.setName(serviceNode.getName()+" Connection");
        Socket tmpSocket = null;
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;

        try {
            tmpSocket = new Socket(addr, port);
            tmpIn = new DataInputStream(new BufferedInputStream(tmpSocket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(tmpSocket.getOutputStream()));
            tmpOut.write(this.serviceNode.getLocalHost().getPort());
        } catch(IOException e){
            VLogger.error("Failed to open socket",e);
        }
        this.intAddress = Encoder.decodeInt32(addr.getAddress(),0);
        this.intPort = port;
        this.socket = tmpSocket;
        this.in = tmpIn;
        this.out = tmpOut;
        this.unicast = unicastOnly;
        this.start();
    }

    public ServiceNodeConnection(ServiceNode serviceNode, InetAddress addr, int port) {
        this(serviceNode,addr,port,false);
    }

    public ServiceNodeConnection(ServiceNode serviceNode, Socket socket) {
        this.socket = socket;
        this.serviceNode = serviceNode;
        this.unicast = false;
        this.setName(serviceNode.getName()+" Connection");
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;

        int port = -1;
        try {
            tmpIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            port = tmpIn.readInt();
        } catch (Exception e) {
            VLogger.error("Failed to use input/output of socket",e);
        }
        this.intAddress = Encoder.decodeInt32(socket.getLocalAddress().getAddress(),0);
        this.intPort = port;
        this.in = tmpIn;
        this.out = tmpOut;
    }

    public int getIntAddress(){
        return this.intAddress;
    }

    public int getIntPort(){
        return this.intPort;
    }

    public String getConnectionKey() throws UnknownHostException {
        String myAddr = InetAddress.getLocalHost().getHostAddress();
        String otherAddr = socket.getInetAddress().getHostAddress();
        if(myAddr.hashCode()<otherAddr.hashCode()){
            this.aSide = true;
        }
        return getConnectionKey(myAddr,otherAddr);
    }

    public static final String getConnectionKey(String aSide,String zSide){
        if(aSide.hashCode()<zSide.hashCode()){
            return aSide+"<->"+zSide;
        }else{
            return zSide+"<->"+aSide;
        }
    }

    public boolean isASide(){
        return this.aSide;
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
                    System.out.println("Connection was probably terminated for "+socket.getInetAddress().getHostName()+":"+socket.getPort());
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
        ServiceID id = null;

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
            VLogger.error("Failed to reaf from socket",err);
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
            this.setName("Switch - " + ServiceNodeConnection.this.getName());
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
                        if (serviceNode.getLocalHost().getPort() != 50000) {
                            serviceNode.receivedPacket(ba);
                        } else {
                            serviceNode.broadcast(ba);
                        }
                    } else if (destAddr == serviceNode.getLocalHost().getIPv4Address() && destPort == serviceNode.getLocalHost().getPort()) {
                        serviceNode.receivedPacket(ba);
                    } else if (destAddr == serviceNode.getLocalHost().getIPv4Address() && serviceNode.getLocalHost().getPort() == 50000 && destPort != 50000) {
                        ServiceNodeConnection other = serviceNode.getNodeConnection(destAddr, destPort,true);
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
                            ServiceNodeConnection source = serviceNode.getNodeConnection(destAddr, destPort,true);
                            if (source != null) {
                                try {
                                    source.sendPacket(ba);
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            } else
                                System.err.println("Source unreachable:"
                                        + new ServiceID(destAddr, destPort,
                                                Encoder.decodeInt16(ba.getBytes(), 16)));
                        }
                    } else if (destAddr != serviceNode.getLocalHost().getIPv4Address() && serviceNode.getLocalHost().getPort() == 50000) {
                        ServiceNodeConnection other = serviceNode.getNodeConnection(destAddr, 50000,true);
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
                            ServiceNodeConnection source = serviceNode.getNodeConnection(destAddr, destPort,true);
                            if (source != null) {
                                try {
                                    source.sendPacket(ba);
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            } else
                                System.err.println("Source unreachable:"
                                        + new ServiceID(destAddr, destPort,
                                                Encoder.decodeInt16(ba.getBytes(), 16)));
                        }
                    }

                }
            }
            System.out.println(this.getName()+" end.");
        }
    }
}