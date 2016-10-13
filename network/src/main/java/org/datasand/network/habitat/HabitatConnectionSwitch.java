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
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.PriorityLinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HabitatConnectionSwitch extends ThreadNode {

    private static final Logger LOG = LoggerFactory.getLogger(HabitatConnectionSwitch.class);
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
            NetUUID dest = new NetUUID(Encoder.decodeInt64(ba.getBytes(), Packet.PACKET_DEST_LOCATION),
                    Encoder.decodeInt64(ba.getBytes(), Packet.PACKET_DEST_LOCATION + 8));

            if (dest.getA() == 0) {
                if (connection.getServicesHabitat().getServicePort() != ServicesHabitat.SERVICE_NODE_SWITCH_PORT) {
                    connection.getServicesHabitat().receivedPacket(ba);
                } else {
                    connection.getServicesHabitat().broadcast(ba);
                }
            } else if (dest.equals(connection.getServicesHabitat().getNetUUID())) {
                connection.getServicesHabitat().receivedPacket(ba);
            } else if(connection.getServicesHabitat().getServicePort() == ServicesHabitat.SERVICE_NODE_SWITCH_PORT) {
                HabitatsConnection other = connection.getServicesHabitat().getNodeConnection(dest,true);
                if(other!=null){
                    other.sendPacket(ba);
                } else {
                    ba = this.connection.markAsUnreachable(ba);
                    //mark unreachable has switch the source & the destination,
                    // hence we re-decode the destination (which is the source)
                    dest = new NetUUID(Encoder.decodeInt64(ba.getBytes(), Packet.PACKET_DEST_LOCATION),
                            Encoder.decodeInt64(ba.getBytes(), Packet.PACKET_DEST_LOCATION + 8));

                    HabitatsConnection source = connection.getServicesHabitat().getNodeConnection(dest,true);
                    if (source != null) {
                        source.sendPacket(ba);
                    } else {
                        LOG.error("Source unreachable:"+dest);
                    }
                }
            }
        }
    }
}
