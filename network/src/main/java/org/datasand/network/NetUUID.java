/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import java.util.UUID;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class NetUUID implements ISerializer {

    static {
        Encoder.registerSerializer(NetUUID.class,new NetUUID());
    }

    private final UUID address;

    private NetUUID(){
        this.address = null;
    }

    public NetUUID(UUID address) {
        this.address = address;
    }

    public NetUUID(long a, long b) {
        this.address = new UUID(a, b);
    }

    public long getA(){
        return this.address.getMostSignificantBits();
    }

    public long getB(){
        return this.address.getLeastSignificantBits();
    }

    public String toString() {
        return address.toString();
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        NetUUID other = (NetUUID) obj;
        if (other.address.equals(this.address)) {
            return true;
        }
        return false;
    }

    public void encode(NetUUID netID, byte[] data, int location) {
        long a = netID.address.getMostSignificantBits();
        long b = netID.address.getLeastSignificantBits();
        Encoder.encodeInt64(a, data, location);
        Encoder.encodeInt64(b, data, location + 8);
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        NetUUID id = (NetUUID) value;
        long a = id.address.getMostSignificantBits();
        long b = id.address.getLeastSignificantBits();
        Encoder.encodeInt64(a, ba);
        Encoder.encodeInt64(b, ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        long a = Encoder.decodeInt64(ba);
        long b = Encoder.decodeInt64(ba);
        return new NetUUID(a, b);
    }
}
