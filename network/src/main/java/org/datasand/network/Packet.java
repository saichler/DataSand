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

    public static final NID PROTOCOL_ID_UNREACHABLE = new NID(0, 0, DESTINATION_UNREACHABLE, 0);
    public static final NID PROTOCOL_ID_BROADCAST = new NID(0, 0, DESTINATION_BROADCAST, 0);


    private NID source = null;
    private NID destination = null;
    private NID originalAddress = null;

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

    public Packet(NID _source, NID _dest) {
        this(_source, _dest, null);
    }

    public Packet(NID _source, NID _destination, byte[] _data) {
        this(_source, _destination, _data, -1, false);
    }

    public Packet(NID _source, NID _destination, byte[] _data,
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

    public Packet(Object message, NID source) {
        this.source = source;
        this.destination = source;
        this.message = message;
    }

    public void setSource(NID s){
        this.source = s;
    }

    public NID getSource() {
        return source;
    }

    public void setDestination(NID dest){
        this.destination = dest;
    }

    public NID getDestination() {
        return destination;
    }

    public NID getOriginalAddress(){
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
        ISerializer serializer = Encoder.getSerializerByClass(NID.class);
        m.source = (NID) serializer.decode(ba);
        m.destination = (NID) serializer.decode(ba);
        m.packetID = Encoder.decodeInt16(ba);
        m.priority = ((int) ba.getBytes()[ba.getLocation()]) / 2;
        m.multiPart = ba.getBytes()[ba.getLocation()] % 2 == 1;
        ba.advance(1);
        if (ba.getBytes().length > Packet.PACKET_DATA_LOCATION) {
            if (isUnreachable(m.source)) {
                m.originalAddress = (NID) serializer.decode(ba);
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

    public Object getMessage() {
        return this.message;
    }

    public void markAsUnreachable(){
        BytesArray ba = new BytesArray(1024);
        this.getDestination().encode(this.getDestination(),ba);
        Encoder.encodeByteArray(this.getData(),ba);
        this.setData(ba.getData());
        this.setDestination(this.getSource());
        this.setSource(Packet.PROTOCOL_ID_UNREACHABLE);
        this.isUnreachableReply = true;

    }

    public static final BytesArray markAsUnreachable(BytesArray ba) {
        byte[] origData = ba.getBytes();
        byte[] mark = new byte[ba.getBytes().length+Packet.PACKET_SOURCE_LENGHT];
        //copy packet header
        System.arraycopy(origData,0,mark,0,Packet.PACKET_DATA_LOCATION);
        //replace destination with source
        System.arraycopy(origData, Packet.PACKET_SOURCE_LOCATION,mark, Packet.PACKET_DEST_LOCATION,Packet.PACKET_SOURCE_LENGHT);
        //replace source as unreachable
        byte[] unreachable = new byte[Packet.PACKET_DEST_LENGTH];
        Packet.PROTOCOL_ID_UNREACHABLE.encode(Packet.PROTOCOL_ID_UNREACHABLE,unreachable,0);
        System.arraycopy(unreachable,0,mark,Packet.PACKET_SOURCE_LOCATION,Packet.PACKET_SOURCE_LENGHT);
        //copy original address to data location
        System.arraycopy(origData,Packet.PACKET_DEST_LOCATION,mark,Packet.PACKET_DATA_LOCATION,Packet.PACKET_DEST_LENGTH);
        //copy original data to data+dest.lenth
        System.arraycopy(origData,Packet.PACKET_DATA_LOCATION,mark,Packet.PACKET_DATA_LOCATION+Packet.PACKET_DEST_LENGTH,origData.length-Packet.PACKET_DATA_LOCATION);
        return new BytesArray(mark);
    }

    public static final boolean isUnreachable(NID id) {
        return id.equals(PROTOCOL_ID_UNREACHABLE);
    }

    public static final boolean isBroadcast(NID id) {
        return id.equals(PROTOCOL_ID_BROADCAST);
    }

}
