/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.nnode;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class NetMetrics {
    private long incomingPacketCount=0;
    private long outgoingPacketCount=0;
    private long incomingPacketQueue=0;
    private long outgoingPacketQueue=0;

    private long incomingFrameCount=0;
    private long outgoingFrameCount=0;
    private long unreachableFrameCount=0;
    private long broadcastFrameCount=0;
    private long multicastFrameCount=0;
    private long regularFrameCount=0;

    public long getIncomingPacketCount() {
        return incomingPacketCount;
    }

    public void addIncomingPacketCount() {
        this.incomingPacketCount++;
    }

    public long getOutgoingPacketCount() {
        return outgoingPacketCount;
    }

    public void addOutgoingPacketCount() {
        this.outgoingPacketCount++;
    }

    public long getIncomingPacketQueue() {
        return incomingPacketQueue;
    }

    public void addIncomingPacketQueue() {
        this.incomingPacketQueue++;
    }

    public long getOutgoingPacketQueue() {
        return outgoingPacketQueue;
    }

    public void addOutgoingPacketQueue() {
        this.outgoingPacketQueue++;
    }

    public long getIncomingFrameCount() {
        return incomingFrameCount;
    }

    public void addIncomingFrameCount() {
        this.incomingFrameCount++;
    }

    public long getOutgoingFrameCount() {
        return outgoingFrameCount;
    }

    public void addOutgoingFrameCount() {
        this.outgoingFrameCount++;
    }

    public long getBroadcastFrameCount() {
        return broadcastFrameCount;
    }

    public void addBroadcastFrameCount() {
        this.broadcastFrameCount++;
    }

    public long getUnreachableFrameCount() {
        return unreachableFrameCount;
    }

    public void addUnreachableFrameCount() {
        this.unreachableFrameCount++;
    }

    public long getMulticastFrameCount() {
        return multicastFrameCount;
    }

    public void addMulticastFrameCount() {
        this.multicastFrameCount++;
    }

    public long getRegularFrameCount() {
        return regularFrameCount;
    }

    public void addRegularFrameCount() {
        this.regularFrameCount++;
    }
}
