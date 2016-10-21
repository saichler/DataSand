/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.nnode;

import org.datasand.codec.BytesArray;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.NID;
import org.datasand.network.Packet;
import org.datasand.network.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Switch extends ThreadNode {

    private static final Logger LOG = LoggerFactory.getLogger(Switch.class);
    private final NodeConnection connection;
    private final Queue<byte[]> incoming = new Queue<byte[]>();

    public Switch(NodeConnection con) {
        super(con, con.getName() + " Switch");
        this.connection = con;
    }

    public void addPacket(byte data[]) {
        synchronized (incoming) {
            incoming.add(data, data[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] / 2);
            incoming.notifyAll();
        }
    }

    public void initialize() {
    }

    public void distruct() {
    }

    public void execute() throws Exception {
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
            NID dest = Packet.PROTOCOL_ID_BROADCAST.decode(ba.getBytes(), Packet.PACKET_DEST_LOCATION);

            if (dest.getUuidA() == 0) {
                if (connection.getNode().getServicePort() != Node.SERVICE_NODE_SWITCH_PORT) {
                    connection.getNode().receivedPacket(ba);
                } else {
                    connection.getNode().broadcast(ba);
                }
            } else if (isTargetThis(dest)) {
                connection.getNode().receivedPacket(ba);
            } else if (connection.getNode().getServicePort() == Node.SERVICE_NODE_SWITCH_PORT) {
                NodeConnection other = connection.getNode().getNodeConnection(dest, true);
                if (other != null) {
                    other.sendPacket(ba);
                } else {
                    ba = Packet.markAsUnreachable(ba);
                    //mark unreachable has switch the source & the destination,
                    // hence we re-decode the destination (which is the source)
                    dest = Packet.PROTOCOL_ID_BROADCAST.decode(ba.getBytes(), Packet.PACKET_DEST_LOCATION);

                    NodeConnection sourceConnection = connection.getNode().getNodeConnection(dest, true);
                    if (sourceConnection != null) {
                        sourceConnection.sendPacket(ba);
                    } else {
                        LOG.error("Source unreachable:" + dest);
                    }
                }
            }
        }
    }

    private boolean isTargetThis(NID addr){
        if(this.connection.getNode().getNID().getNetwork() == addr.getNetwork() &&
                this.connection.getNode().getNID().getUuidA() == addr.getUuidA() &&
                this.connection.getNode().getNID().getUuidB() == addr.getUuidB())
            return true;
        return false;
    }
}
