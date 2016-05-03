/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.network.Packet;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServicePacketProcessor extends Thread {
    private LinkedList<BytesArray> incomingFrames = new LinkedList<BytesArray>();
    private Map<Packet, MultiPartContainer> multiparts = new HashMap<Packet, MultiPartContainer>();
    private final Packet serializer = new Packet((Object) null, null);
    private boolean running = true;
    private final ServiceNode serviceNode;

    public ServicePacketProcessor(ServiceNode serviceNode) {
        this.serviceNode = serviceNode;
        this.setName(serviceNode.getName()+" packet processor");
        this.setDaemon(true);
        this.start();
    }

    public void addPacket(BytesArray ba) {
        boolean multiPart = ba.getBytes()[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] % 2 == 1;
        serviceNode.getServiceNodeMetrics().addIncomingPacketCount();

        // The packet is a complete frame
        if (!multiPart) {
            addFrame(ba);
        } else {
            Packet pID = (Packet) serializer.decode(ba);
            MultiPartContainer mpc = multiparts.get(pID);
            if (mpc == null) {
                mpc = new MultiPartContainer();
                multiparts.put(pID, mpc);
                mpc.expectedCount = Encoder.decodeInt32(ba.getBytes(), Packet.PACKET_DATA_LOCATION);
            } else {
                mpc.parts.add(ba);
                if (mpc.parts.size() == mpc.expectedCount) {
                    multiparts.remove(pID);
                    addFrame(mpc.toFrame());
                }
            }
        }
    }

    private void addFrame(BytesArray frame){
        synchronized (incomingFrames) {
            incomingFrames.add(frame);
            serviceNode.getServiceNodeMetrics().addIncomingFrameCount();
            incomingFrames.notifyAll();
        }
    }

    public void run() {
        while (running) {
            BytesArray frame = null;
            synchronized (incomingFrames) {
                if (incomingFrames.size() == 0) {
                    try {
                        incomingFrames.wait(5000);
                    } catch (Exception err) {
                    }
                }
                if (incomingFrames.size() > 0) {
                    frame = incomingFrames.removeFirst();
                }
            }

            if (frame != null) {
                Packet f = (Packet) serializer.decode(frame);
                if (serviceNode.getFrameListener() != null) {
                    if (f.getSource().getIPv4Address() == 0 && f.getSource().getSubSystemID() == 9999) {
                        serviceNode.getServiceNodeMetrics().addUnreachableFrameCount();
                        serviceNode.getFrameListener().processDestinationUnreachable(f);
                    } else if (f.getDestination().getIPv4Address() == 0
                            && f.getDestination().getSubSystemID() == ServiceNodeConnection.DESTINATION_BROADCAST) {
                        serviceNode.getServiceNodeMetrics().addBroadcastFrameCount();
                        serviceNode.getFrameListener().processBroadcast(f);
                    } else if (f.getDestination().getIPv4Address() == 0
                            && f.getDestination().getSubSystemID() > ServiceNodeConnection.DESTINATION_BROADCAST) {
                        serviceNode.getServiceNodeMetrics().addMulticastFrameCount();
                        serviceNode.getFrameListener().processMulticast(f);
                    } else {
                        serviceNode.getServiceNodeMetrics().addRegularFrameCount();
                        serviceNode.getFrameListener().process(f);
                    }
                } else {
                    if (f.getSource().getIPv4Address() == 0 && f.getSource().getSubSystemID() == 9999) {
                        serviceNode.getServiceNodeMetrics().addUnreachableFrameCount();
                        VLogger.info("No Frame Listener, Received Unreachable Frame"+f);
                    } else if (f.getDestination().getIPv4Address() == 0
                            && f.getDestination().getSubSystemID() == ServiceNodeConnection.DESTINATION_BROADCAST) {
                        serviceNode.getServiceNodeMetrics().addBroadcastFrameCount();
                        VLogger.info("No Frame Listener, Received Broadcast Frame"+f);
                    } else if (f.getDestination().getIPv4Address() == 0
                            && f.getDestination().getSubSystemID() > ServiceNodeConnection.DESTINATION_BROADCAST) {
                        serviceNode.getServiceNodeMetrics().addMulticastFrameCount();
                        VLogger.info("No Frame Listener, Received Multicast Frame"+f);
                    } else {
                        serviceNode.getServiceNodeMetrics().addRegularFrameCount();
                        VLogger.info("No Frame Listener, Received Regular Frame"+f);
                    }
                }
            }
        }
    }

    public void shutdown(){
        this.running = false;
    }

    private static class MultiPartContainer {
        private final List<BytesArray> parts = new LinkedList<BytesArray>();
        private int expectedCount = -1;

        public BytesArray toFrame() {
            BytesArray firstPart = parts.get(0);
            byte[] data = new byte[(firstPart.getBytes().length - Packet.PACKET_DATA_LOCATION) * parts.size() + Packet.PACKET_DATA_LOCATION];
            System.arraycopy(firstPart, 0, data, 0, Packet.PACKET_DATA_LOCATION);
            int location = Packet.PACKET_DATA_LOCATION;
            for (BytesArray p : parts) {
                System.arraycopy(p.getBytes(), Packet.PACKET_DATA_LOCATION, data, location, p.getBytes().length - Packet.PACKET_DATA_LOCATION);
                location += (p.getBytes().length - Packet.PACKET_DATA_LOCATION);
            }
            return new BytesArray(data);
        }
    }

}
