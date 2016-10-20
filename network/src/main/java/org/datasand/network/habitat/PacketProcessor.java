/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PacketProcessor extends ThreadNode {

    private static final Logger LOG = LoggerFactory.getLogger(PacketProcessor.class);
    private LinkedList<BytesArray> incomingFrames = new LinkedList<BytesArray>();
    private Map<Packet, MultiPartContainer> multiparts = new HashMap<Packet, MultiPartContainer>();
    private final Packet serializer = new Packet((Object) null, null);
    private final Node node;

    public PacketProcessor(Node node) {
        super(node, node.getName()+" packet processor");
        this.node = node;
    }

    public void addPacket(BytesArray ba) {
        boolean multiPart = ba.getBytes()[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] % 2 == 1;
        node.getNetMetrics().addIncomingPacketCount();

        // The packet is a complete frame
        if (!multiPart) {
            addFrame(ba);
        } else {
            Packet pID = (Packet) serializer.decode(ba);
            MultiPartContainer mpc = multiparts.get(pID);
            if (mpc == null) {
                mpc = new MultiPartContainer();
                multiparts.put(pID, mpc);
                mpc.expectedCount = Encoder.decodeInt32(Encoder.decodeByteArray(ba.getBytes(), Packet.PACKET_DATA_LOCATION),0);
            } else {
                LOG.info("Received part "+pID.getPart()+" out of "+mpc.expectedCount);
                mpc.addPart(pID);
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
            node.getNetMetrics().addIncomingFrameCount();
            incomingFrames.notifyAll();
        }
    }

    public void initialize(){}
    public void distruct(){}

    public void execute() throws Exception{
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
            if (node.getFrameListener() != null) {
                if (f.getSource().equals(Packet.PROTOCOL_ID_UNREACHABLE)) {
                    node.getNetMetrics().addUnreachableFrameCount();
                    node.getFrameListener().processDestinationUnreachable(f);
                } else if (f.getDestination().equals(Packet.PROTOCOL_ID_BROADCAST)) {
                    node.getNetMetrics().addBroadcastFrameCount();
                    node.getFrameListener().processBroadcast(f);
                } else if (f.getDestination().getUuidA()==0) {
                    node.getNetMetrics().addMulticastFrameCount();
                    node.getFrameListener().processMulticast(f);
                } else {
                    node.getNetMetrics().addRegularFrameCount();
                    node.getFrameListener().process(f);
                }
            } else {
                if (f.getSource().equals(Packet.PROTOCOL_ID_UNREACHABLE)) {
                    node.getNetMetrics().addUnreachableFrameCount();
                    LOG.info(node.getNID()+" No Frame Listener, Received Unreachable Frame"+f);
                } else if (f.getDestination().equals(Packet.PROTOCOL_ID_BROADCAST)) {
                    node.getNetMetrics().addBroadcastFrameCount();
                    LOG.info(node.getNID()+" No Frame Listener, Received Broadcast Frame"+f);
                } else if (f.getDestination().getUuidA()==0) {
                    node.getNetMetrics().addMulticastFrameCount();
                    LOG.info(node.getNID()+" No Frame Listener, Received Multicast Frame"+f);
                } else {
                    node.getNetMetrics().addRegularFrameCount();
                    LOG.info(node.getNID()+" No Frame Listener, Received Regular Frame"+f);
                }
            }
        }
    }

    private static class MultiPartContainer {
        private final List<Packet> parts = new LinkedList<>();
        private int expectedCount = -1;


        public void addPart(Packet p){
            this.parts.add(p);
        }

        public BytesArray toFrame() {
            byte data[] = new byte[this.parts.size()* Packet.MAX_DATA_IN_ONE_PACKET];
            for(Packet b:this.parts){
                int part = b.getPart();
                System.arraycopy(b.getData(),0,data,part*Packet.MAX_DATA_IN_ONE_PACKET,b.getData().length);
            }

            /*
            BytesArray firstPart = parts.get(0);
            byte[] data = new byte[(firstPart.getBytes().length - Packet.PACKET_DATA_LOCATION) * parts.size() + Packet.PACKET_DATA_LOCATION];
            System.arraycopy(firstPart, 0, data, 0, Packet.PACKET_DATA_LOCATION);
            int location = Packet.PACKET_DATA_LOCATION;
            for (BytesArray p : parts) {
                System.arraycopy(p.getBytes(), Packet.PACKET_DATA_LOCATION, data, location, p.getBytes().length - Packet.PACKET_DATA_LOCATION);
                location += (p.getBytes().length - Packet.PACKET_DATA_LOCATION);
            }*/
            Packet p = this.parts.get(0);
            p.setData(data);
            BytesArray ba = new BytesArray(p.getData().length+Packet.PACKET_DATA_LOCATION);
            p.encode(p,ba);
            return ba;
        }
    }

}
