package org.datasand.network;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Packet implements ISerializer {

    public static final int PACKET_SOURCE_LOCATION = 0;
    public static final int PACKET_SOURCE_LENGHT = 8;
    public static final int PACKET_DEST_LOCATION = PACKET_SOURCE_LOCATION + PACKET_SOURCE_LENGHT;
    public static final int PACKET_DEST_LENGTH = 8;
    public static final int PACKET_ID_LOCATION = PACKET_DEST_LOCATION + PACKET_DEST_LENGTH;
    public static final int PACKET_ID_LENGTH = 2;
    public static final int PACKET_MULTIPART_AND_PRIORITY_LOCATION = PACKET_ID_LOCATION + PACKET_ID_LENGTH;
    public static final int PACKET_MULTIPART_AND_PRIORITY_LENGTH = 1;
    public static final int PACKET_DATA_LOCATION = PACKET_MULTIPART_AND_PRIORITY_LOCATION + PACKET_MULTIPART_AND_PRIORITY_LENGTH;
    public static final int MAX_DATA_IN_ONE_PACKET = 1024 * 512;

    private NetworkID source = null;
    private NetworkID destination = null;
    private int packetID = -1;
    private boolean multiPart = false;
    private int priority = 2;
    private byte[] data = null;
    private Object decodedObject = null;

    private static int nextPacketID = 1000;
    static {
        Encoder.registerSerializer(Packet.class, new Packet());
    }

    private Packet() {
    }

    public Packet(Object _decodedObject, NetworkID _source, NetworkID _dest) {
        this(_source, _dest, (byte[]) null);
        this.decodedObject = _decodedObject;
    }

    public Packet(NetworkID _source, NetworkID _destination, byte[] _data) {
        this(_source, _destination, _data, -1, false);
    }

    public Packet(NetworkID _source, NetworkID _destination, byte[] _data,
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

    public NetworkID getSource() {
        return source;
    }

    public NetworkID getDestination() {
        return destination;
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

    public int getPriority() {
        return this.priority;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        Packet p = (Packet) value;
        p.source.encode(p.source,ba);
        p.destination.encode(p.destination,ba);
        Encoder.encodeInt16(p.packetID,ba);

        if (p.multiPart) {
            byte m_p = (byte) (p.priority * 2 + 1);
            Encoder.encodeByte(m_p,ba);
        } else {
            byte m_p = (byte) (p.priority * 2);
            Encoder.encodeByte(m_p,ba);
        }
        Encoder.encodeByteArray(p.data,ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        Packet m = new Packet();
        m.source = (NetworkID) Encoder.getSerializerByClass(NetworkID.class).decode(ba);
        m.destination = (NetworkID) Encoder.getSerializerByClass(NetworkID.class).decode(ba);
        m.packetID = Encoder.decodeInt16(ba);
        m.priority = ((int) ba.getBytes()[ba.getLocation()]) / 2;
        m.multiPart = ba.getBytes()[ba.getLocation()] % 2 == 1;
        ba.advance(1);
        if(m.getSource().getPort()==9999 && ba.getBytes().length>Packet.PACKET_DATA_LOCATION) {
            m.data = Encoder.decodeByteArray(ba);
        }
        return m;
    }

    @Override
    public int hashCode() {
        return source.getIPv4Address() ^ destination.getIPv4Address()
                ^ source.getPort() ^ destination.getPort()
                ^ source.getSubSystemID() ^ destination.getSubSystemID();
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

    public void decode() {
        if (this.decodedObject == null) {
            if(this.getSource().equals(NetworkNodeConnection.PROTOCOL_ID_UNREACHABLE)){
                byte origData[] = new byte[this.getData().length-PACKET_DATA_LOCATION];
                System.arraycopy(this.getData(), PACKET_DATA_LOCATION, origData, 0, origData.length);
                BytesArray ba = new BytesArray(origData);
                try{
                    decodedObject = Encoder.decodeObject(ba);
                }catch(Exception err){
                    System.err.println("Unable to decode...");
                }
            }else{
                BytesArray ba = new BytesArray(this.getData());
                try{
                    decodedObject = Encoder.decodeObject(ba);}
                catch(Exception err){
                    System.err.println("Unable to decode...");
                }
            }
        }
    }

    public Object getDecodedObject() {
        return this.decodedObject;
    }

    public NetworkID getUnreachableOrigAddress(){
        int destAddress = Encoder.decodeInt32(this.getData(),PACKET_DEST_LOCATION);
        int destPort = Encoder.decodeInt16(this.getData(), PACKET_DEST_LOCATION+4);
        if(destAddress==0){
            destAddress = Encoder.decodeInt32(this.getData(), this.getData().length-6);
            destPort = Encoder.decodeInt16(this.getData(), this.getData().length-2);
        }
        return new NetworkID(destAddress, destPort, 0);
    }
}
