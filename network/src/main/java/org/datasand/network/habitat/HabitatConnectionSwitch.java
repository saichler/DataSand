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
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.HabitatID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HabitatConnectionSwitch extends ThreadNode {

    private final HabitatsConnection connection;
    private final PriorityLinkedList<byte[]> incoming = new PriorityLinkedList<byte[]>();

    public HabitatConnectionSwitch(HabitatsConnection con) {
        super(con,con.getName()+" Switch");
        this.connection = con;
    }

    public void addPacket(byte data[]){
        synchronized (incoming) {
            incoming.add(data, data[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] / 2);
            incoming.notifyAll();
        }
    }

    public void initialize(){
    }

    public void distruct(){
    }

    public void execute() throws Exception{
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
            int destAddr = Encoder.decodeInt32(ba.getBytes(), Packet.PACKET_DEST_LOCATION);
            int destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
            if (destAddr == 0) {
                if (connection.getServicesHabitat().getLocalHost().getPort() != 50000) {
                    connection.getServicesHabitat().receivedPacket(ba);
                } else {
                    connection.getServicesHabitat().broadcast(ba);
                }
            } else if (destAddr == connection.getServicesHabitat().getLocalHost().getIPv4Address() && destPort == connection.getServicesHabitat().getLocalHost().getPort()) {
                connection.getServicesHabitat().receivedPacket(ba);
            } else if (destAddr == connection.getServicesHabitat().getLocalHost().getIPv4Address() && connection.getServicesHabitat().getLocalHost().getPort() == 50000 && destPort != 50000) {
                HabitatsConnection other = connection.getServicesHabitat().getNodeConnection(destAddr, destPort,true);
                if (other != null && other.isRunning()) {
                    try {
                        other.sendPacket(ba);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                } else {
                    ba = this.connection.markAsUnreachable(ba);
                    destAddr = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_DEST_LOCATION);
                    destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
                    HabitatsConnection source = connection.getServicesHabitat().getNodeConnection(destAddr, destPort,true);
                    if (source != null) {
                        try {
                            source.sendPacket(ba);
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    } else {
                        VLogger.error("Source unreachable:"
                                + new HabitatID(destAddr, destPort,
                                Encoder.decodeInt16(ba.getBytes(), 16)),null);
                    }
                }
            } else if (destAddr != connection.getServicesHabitat().getLocalHost().getIPv4Address() && connection.getServicesHabitat().getLocalHost().getPort() == 50000) {
                HabitatsConnection other = connection.getServicesHabitat().getNodeConnection(destAddr, 50000,true);
                if (other != null) {
                    try {
                        other.sendPacket(ba);
                    } catch (Exception err) {
                        VLogger.error("Failed to send packet",err);
                    }
                } else {
                    ba = this.connection.markAsUnreachable(ba);
                    destAddr = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_DEST_LOCATION);
                    destPort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_DEST_LOCATION + 4);
                    HabitatsConnection source = connection.getServicesHabitat().getNodeConnection(destAddr, destPort,true);
                    if (source != null) {
                        try {
                            source.sendPacket(ba);
                        } catch (Exception err) {
                            VLogger.error("Failed to send packet",err);
                        }
                    } else {
                        VLogger.error("Source unreachable:"
                                + new HabitatID(destAddr, destPort,
                                Encoder.decodeInt16(ba.getBytes(), 16)),null);
                    }
                }
            }

        }
    }
}
