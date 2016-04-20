/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServiceNodeConnection extends Thread {

    public static final int DESTINATION_UNREACHABLE = 9999;
    public static final int DESTINATION_BROADCAST = 10;

    public static final ServiceID PROTOCOL_ID_UNREACHABLE = new ServiceID(0, 0,DESTINATION_UNREACHABLE);
    public static final ServiceID PROTOCOL_ID_BROADCAST = new ServiceID(0, 0,DESTINATION_BROADCAST);

    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private PriorityLinkedList<byte[]> incoming = new PriorityLinkedList<byte[]>();
    private ServiceNode serviceNode = null;
    private boolean running = false;
    private boolean valideConnection = false;
    private String connectionString = null;

    public ServiceNodeConnection(ServiceNode _nn, InetAddress addr, int port, boolean unicastOnly) {
        try {
            this.serviceNode = _nn;
            this.setName("Con of-" + _nn.getName());
            socket = new Socket(addr, port);
            try {
                in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                String conString = "S:" + serviceNode.getLocalHost().getPort()+ "    ";
                out.write(conString.getBytes());
                out.flush();
                byte data[] = new byte[10];
                in.readFully(data);
                connectionString = new String(data).trim();
                this.valideConnection = true;
                this.start();
            } catch (Exception err) {
                err.printStackTrace();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public ServiceNodeConnection(ServiceNode _nn, InetAddress addr, int port) {
        try {
            this.serviceNode = _nn;
            this.setName("Con of-" + _nn.getName());
            socket = new Socket(addr, port);
            try {
                in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
                out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                String conString = "C:" + serviceNode.getLocalHost().getPort()+ "    ";
                out.write(conString.getBytes());
                out.flush();
                byte data[] = new byte[10];
                in.readFully(data);
                connectionString = new String(data).trim();
                this.valideConnection = true;
                this.start();
            } catch (Exception err) {
                err.printStackTrace();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public ServiceNodeConnection(ServiceNode _nn, Socket _s) {
        this.socket = _s;
        this.serviceNode = _nn;
        this.setName("Con of-" + _nn.getName());
        try {
            in = new DataInputStream(new BufferedInputStream(
                    this.socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(
                    this.socket.getOutputStream()));
            byte data[] = new byte[10];
            in.readFully(data);
            connectionString = new String(data);
            String connType = "C";
            if(connectionString.startsWith("S"))
                connType = "S";
            String conString = connType+":" + serviceNode.getLocalHost().getPort()+ "    ";
            out.write(conString.getBytes());
            out.flush();
            this.valideConnection = true;
            this.start();
        } catch (Exception err) {
            err.printStackTrace();
        }
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
        synchronized (serviceNode.incomingConnections) {
            if (!valideConnection)
                return;
            if (connectionString.startsWith("U")) {
                InetAddress source = this.socket.getInetAddress();
                int port = Integer.parseInt(connectionString.substring(connectionString.indexOf(":") + 1).trim());
                String sourceAddress = source.getHostAddress();
                if (sourceAddress.equals("127.0.0.1")) {
                    id = new ServiceID(serviceNode.getLocalHost().getIPv4Address(), port, 0);
                } else
                    id = ServiceID.valueOf(source.getHostAddress() + ":" + port + ":0");
                if (!serviceNode.incomingConnections.contains(id)) {
                    new ServiceNodeConnection(this.serviceNode, source, port,true);
                    serviceNode.incomingConnections.add(id);
                }
                return;
            }else
            if (connectionString.startsWith("R")) {
                InetAddress source = this.socket.getInetAddress();
                int port = Integer.parseInt(connectionString.substring(connectionString.indexOf(":") + 1).trim());
                String sourceAddress = source.getHostAddress();
                if (sourceAddress.equals("127.0.0.1")) {
                    id = new ServiceID(serviceNode.getLocalHost().getIPv4Address(), port, 0);
                } else
                    id = ServiceID.valueOf(source.getHostAddress() + ":" + port + ":0");
                if (!serviceNode.incomingConnections.contains(id)) {
                    new ServiceNodeConnection(this.serviceNode, source, port);
                    serviceNode.incomingConnections.add(id);
                }
                return;
            } else {
                InetAddress source = this.socket.getInetAddress();
                int port = Integer.parseInt(connectionString.substring(connectionString.indexOf(":") + 1).trim());
                String sourceAddress = source.getHostAddress();
                if (sourceAddress.equals("127.0.0.1")) {
                    id = new ServiceID(serviceNode.getLocalHost().getIPv4Address(), port, 0);
                } else
                    id = ServiceID.valueOf(source.getHostAddress() + ":" + port + ":0");

            }
        }
        if(connectionString.startsWith("C:")){
            if (!serviceNode.registerNetworkNodeConnection(this, id)) {
                return;
            }
        }else{
            if (!serviceNode.registerSingleNetworkNodeConnection(this, id)) {
                return;
            }
        }

        running = true;

        new Switch();

        try {
            if (in.available() > 0) {
                byte data[] = new byte[in.available()];
                in.readFully(data);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        try {
            while (socket != null && !socket.isClosed() && running) {
                int size = in.readInt();
                byte data[] = new byte[size];
                in.readFully(data);
                synchronized (incoming) {
                    incoming.add(
                            data,
                            data[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] / 2);
                    incoming.notifyAll();
                }
            }
        } catch (Exception err) {
            // err.printStackTrace();
        }
        System.out.println(this.getName()+" was closed.");
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