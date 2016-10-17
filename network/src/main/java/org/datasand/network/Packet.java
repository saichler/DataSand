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

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Packet implements ISerializer {

    public static final int PACKET_SOURCE_LOCATION = 0;
    public static final int PACKET_SOURCE_LENGHT = 20;
    public static final int PACKET_DEST_LOCATION = PACKET_SOURCE_LOCATION + PACKET_SOURCE_LENGHT;
    public static final int PACKET_DEST_LENGTH = 20;
    public static final int PACKET_ID_LOCATION = PACKET_DEST_LOCATION + PACKET_DEST_LENGTH;
    public static final int PACKET_ID_LENGTH = 2;
    public static final int PACKET_MULTIPART_AND_PRIORITY_LOCATION = PACKET_ID_LOCATION + PACKET_ID_LENGTH;
    public static final int PACKET_MULTIPART_AND_PRIORITY_LENGTH = 1;
    public static final int PACKET_DATA_LOCATION = PACKET_MULTIPART_AND_PRIORITY_LOCATION + PACKET_MULTIPART_AND_PRIORITY_LENGTH;
    public static final int MAX_DATA_IN_ONE_PACKET = 1024 * 512;

    public static final int DESTINATION_UNREACHABLE = 9998;
    public static final int DESTINATION_BROADCAST = 10;

    public static final NetUUID PROTOCOL_ID_UNREACHABLE = new NetUUID(0, 0, DESTINATION_UNREACHABLE, 0);
    public static final NetUUID PROTOCOL_ID_BROADCAST = new NetUUID(0, 0, DESTINATION_BROADCAST, 0);


    private NetUUID source = null;
    private NetUUID destination = null;
    private NetUUID originalAddress = null;

    private int packetID = -1;
    private boolean multiPart = false;
    private int priority = 2;
    private byte[] data = null;
    private Object message = null;
    private boolean isUnreachableReply = false;

    private static int nextPacketID = 1000;

    static {
        Encoder.registerSerializer(Packet.class, new Packet());
    }

    private Packet() {
    }

    public Packet(NetUUID _source, NetUUID _dest) {
        this(_source, _dest, (byte[]) null);
    }

    public Packet(NetUUID _source, NetUUID _destination, byte[] _data) {
        this(_source, _destination, _data, -1, false);
    }

    public Packet(NetUUID _source, NetUUID _destination, byte[] _data,
                  int _id, boolean _multiPart) {
        if (_id == -1) {
            synchronized (Packet.class) {
                this.packetID = nextPacketID;
                nextPacketID++;
            }
        } else {
            this.packetID = _id;
        }
        this.multiPart = _multiPart;
        this.source = _source;
        this.destination = _destination;
        this.data = _data;
        if (this.data == null) {
            this.data = new byte[0];
        }
    }

    public Packet(Object message, NetUUID source) {
        this.source = source;
        this.destination = source;
        this.message = message;
    }

    public void setSource(NetUUID s){
        this.source = s;
    }

    public NetUUID getSource() {
        return source;
    }

    public void setDestination(NetUUID dest){
        this.destination = dest;
    }

    public NetUUID getDestination() {
        return destination;
    }

    public NetUUID getOriginalAddress(){
        return this.originalAddress;
    }

    public int getPacketID() {
        return this.packetID;
    }

    public boolean isMultiPart() {
        return this.multiPart;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte d[]){
        this.data = d;
    }

    public void setUnreachableReply(){
        this.isUnreachableReply = true;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        Packet p = (Packet) value;
        ba.resetLocation();
        p.source.encode(p.source, ba);
        p.destination.encode(p.destination, ba);
        Encoder.encodeInt16(p.packetID, ba);

        if (p.multiPart) {
            byte m_p = (byte) (p.priority * 2 + 1);
            Encoder.encodeByte(m_p, ba);
        } else {
            byte m_p = (byte) (p.priority * 2);
            Encoder.encodeByte(m_p, ba);
        }
        if(p.isUnreachableReply){
            ba.insert(p.data);
        }else {
            Encoder.encodeByteArray(p.data, ba);
        }
    }

    public Object decode() {
        if (this.message != null) {
            return this.message;
        } else {
            BytesArray ba = new BytesArray(this.data);
            this.message = Encoder.decodeObject(ba);
            return this.message;
        }
    }

    @Override
    public Object decode(BytesArray ba) {
        Packet m = new Packet();
        ba.resetLocation();
        ISerializer serializer = Encoder.getSerializerByClass(NetUUID.class);
        m.source = (NetUUID) serializer.decode(ba);
        m.destination = (NetUUID) serializer.decode(ba);
        m.packetID = Encoder.decodeInt16(ba);
        m.priority = ((int) ba.getBytes()[ba.getLocation()]) / 2;
        m.multiPart = ba.getBytes()[ba.getLocation()] % 2 == 1;
        ba.advance(1);
        if (ba.getBytes().length > Packet.PACKET_DATA_LOCATION) {
            if (isUnreachable(m.source)) {
                m.originalAddress = (NetUUID) serializer.decode(ba);
                m.data = Encoder.decodeByteArray(ba);
            } else {
                try {
                    m.data = Encoder.decodeByteArray(ba);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
        return m;
    }

    @Override
    public int hashCode() {
        return source.hashCode() ^ destination.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Packet other = (Packet) obj;
        if (source.equals(other.source)
                && destination.equals(other.destination)
                && packetID == other.packetID)
            return true;
        return false;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("ID=").append(this.packetID).append(" Source=")
                .append(this.source).append(" Dest=").append(this.destination);
        return buff.toString();
    }

    /*
    public NetUUID getUnreachableOrigAddress() {
        int n = Encoder.decodeInt16(this.getData(), PACKET_DEST_LOCATION);
        long a = Encoder.decodeInt64(this.getData(), PACKET_DEST_LOCATION + 2);
        long b = Encoder.decodeInt64(this.getData(), PACKET_DEST_LOCATION + 10);
        int s = Encoder.decodeInt16(this.getData(), PACKET_DEST_LOCATION + 18);
        if (a == 0) {
            n = Encoder.decodeInt16(this.getData(), this.getData().length - 20);
            a = Encoder.decodeInt64(this.getData(), this.getData().length - 18);
            b = Encoder.decodeInt64(this.getData(), this.getData().length - 10);
            s = Encoder.decodeInt16(this.getData(), this.getData().length - 2);
        }
        return new NetUUID(n, a, b, s);
    }*/

    public Object getMessage() {
        return this.message;
    }

    public static final boolean isUnreachable(NetUUID id) {
        return id.equals(PROTOCOL_ID_UNREACHABLE);
    }

    public static final boolean isBroadcast(NetUUID id) {
        return id.equals(PROTOCOL_ID_BROADCAST);
    }
}
