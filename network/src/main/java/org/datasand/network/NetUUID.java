/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

import java.util.Objects;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class NetUUID implements ISerializer {

    static {
        Encoder.registerSerializer(NetUUID.class, new NetUUID(-1,-1,-1,-1));
    }

    private final int network;
    private final long uuidA;
    private final long uuidB;
    private final int serviceID;

    public NetUUID(int network, long uuidA, long uuidB, int serviceID) {
        this.network = network;
        this.uuidA = uuidA;
        this.uuidB = uuidB;
        this.serviceID = serviceID;
    }

    public int getNetwork() {
        return network;
    }

    public long getUuidA() {
        return uuidA;
    }

    public long getUuidB() {
        return uuidB;
    }

    public int getServiceID() {
        return serviceID;
    }

    public String toString() {
        return "N:" + this.network + " U:" + this.uuidA +"/"+ this.uuidB + " S:" + this.serviceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(network, uuidA, uuidB, serviceID);
    }

    @Override
    public boolean equals(Object obj) {
        NetUUID other = (NetUUID) obj;
        if (this.network == other.network &&
                this.uuidA == other.uuidA &&
                this.uuidB == other.uuidB &&
                this.serviceID == other.serviceID) {
            return true;
        }
        return false;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        NetUUID id = (NetUUID) value;
        Encoder.encodeInt16(id.network, ba);
        Encoder.encodeInt64(id.uuidA, ba);
        Encoder.encodeInt64(id.uuidB, ba);
        Encoder.encodeInt16(id.serviceID, ba);
    }

    public void encode(Object value, byte[] data,int location) {
        NetUUID id = (NetUUID) value;
        Encoder.encodeInt16(id.network, data,location);
        Encoder.encodeInt64(id.uuidA, data,location+2);
        Encoder.encodeInt64(id.uuidB, data,location+10);
        Encoder.encodeInt16(id.serviceID, data,location+18);
    }

    @Override
    public Object decode(BytesArray ba) {
        int network = Encoder.decodeInt16(ba);
        long a = Encoder.decodeInt64(ba);
        long b = Encoder.decodeInt64(ba);
        int serviceID = Encoder.decodeInt16(ba);
        return new NetUUID(network, a, b, serviceID);
    }

    public NetUUID decode(byte[] data,int location) {
        int network = Encoder.decodeInt16(data,location);
        long a = Encoder.decodeInt64(data,location+2);
        long b = Encoder.decodeInt64(data,location+10);
        int serviceID = Encoder.decodeInt16(data,location+18);
        return new NetUUID(network, a, b, serviceID);
    }
}
