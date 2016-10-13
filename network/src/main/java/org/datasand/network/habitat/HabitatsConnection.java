/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.ConnectionID;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HabitatsConnection extends ThreadNode {

    private static final Logger LOG = LoggerFactory.getLogger(HabitatsConnection.class);
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ServicesHabitat servicesHabitat;
    private final boolean unicast;
    private final HabitatConnectionSwitch hcSwitch;
    private final ConnectionID connectionID;

    public HabitatsConnection(ServicesHabitat servicesHabitat, InetAddress addr, int port, boolean unicastOnly) {
        super(servicesHabitat,servicesHabitat.getName()+" Connection");
        this.servicesHabitat = servicesHabitat;
        Socket tmpSocket = null;
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;
        byte otherData[] = new byte[16];
        byte myData[] = new byte[16];
        this.servicesHabitat.getNetUUID().encode(this.servicesHabitat.getNetUUID(),myData,0);
        try {
            tmpSocket = new Socket(addr, port);
            tmpIn = new DataInputStream(new BufferedInputStream(tmpSocket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(tmpSocket.getOutputStream()));
            tmpOut.write(myData);
            if(unicastOnly){
                tmpOut.writeInt(1);
            }else{
                tmpOut.writeInt(0);
            }
            tmpOut.flush();
            tmpIn.read(otherData);
        } catch(IOException e){
            LOG.error("Failed to open socket",e);
        }

        this.socket = tmpSocket;
        long a = Encoder.decodeInt64(myData,0);
        long b = Encoder.decodeInt64(myData,8);
        long a1 = Encoder.decodeInt64(otherData,0);
        long b1 = Encoder.decodeInt64(otherData,8);
        this.connectionID = new ConnectionID(a,b,a1,b1);
        this.in = tmpIn;
        this.out = tmpOut;
        this.unicast = unicastOnly;
        this.setName(this.servicesHabitat.getServicePort()+" connection "+this.connectionID);
        hcSwitch = new HabitatConnectionSwitch(this);
    }

    public HabitatsConnection(ServicesHabitat servicesHabitat, InetAddress addr, int port) {
        this(servicesHabitat,addr,port,false);
    }

    public HabitatsConnection(ServicesHabitat servicesHabitat, Socket socket) {
        super(servicesHabitat,servicesHabitat.getName()+" Connection");
        this.socket = socket;
        this.servicesHabitat = servicesHabitat;
        this.setDaemon(true);
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;
        int unicastOnly = 0;
        byte otherData[] = new byte[16];
        byte myData[] = new byte[16];
        this.servicesHabitat.getNetUUID().encode(this.servicesHabitat.getNetUUID(),myData,0);
        try {
            tmpIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            tmpIn.read(otherData);
            unicastOnly = tmpIn.readInt();
            tmpOut.write(myData);
            tmpOut.flush();
        } catch (Exception e) {
            LOG.error("Failed to use input/output of socket",e);
        }
        this.in = tmpIn;
        this.out = tmpOut;
        long a = Encoder.decodeInt64(myData,0);
        long b = Encoder.decodeInt64(myData,8);
        long a1 = Encoder.decodeInt64(otherData,0);
        long b1 = Encoder.decodeInt64(otherData,8);
        this.connectionID = new ConnectionID(a,b,a1,b1);

        if(unicastOnly==1){
            this.unicast = true;
        }else{
            this.unicast = false;
        }
        this.setName(this.servicesHabitat.getServicePort()+" connection "+this.connectionID);
        this.hcSwitch = new HabitatConnectionSwitch(this);
    }

    public boolean isUnicast(){
        return this.unicast;
    }

    protected ServicesHabitat getServicesHabitat(){
        return this.servicesHabitat;
    }

    public boolean isASide(){
        if(this.connectionID.getaSide().equals(this.servicesHabitat.getNetUUID())){
            return true;
        }
        return false;
    }

    public ConnectionID getConnectionID(){
        return this.connectionID;
    }

    public BytesArray sendPacket(BytesArray ba) throws IOException {
        byte data[] = ba.getBytes();
        synchronized (out) {
            if(this.isRunning()){
                try{
                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();
                    return null;
                }catch(SocketException serr){
                    VLogger.info("Connection was probably terminated for "+socket.getInetAddress().getHostName()+":"+socket.getPort());
                    this.shutdown();
                    return markAsUnreachable(ba);
                }
            }else{
                return markAsUnreachable(ba);
            }
        }
    }

    public void shutdown() {
        super.shutdown();
        try {
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (Exception err) {
        }
    }

    public void initialize(){
    }

    public void distruct(){
    }

    public void execute() throws Exception {
        try {
            int size = in.readInt();
            byte data[] = new byte[size];
            in.readFully(data);
            hcSwitch.addPacket(data);
        }catch(EOFException e){
            LOG.info("Connection "+this.getName()+"was closed");
        }
    }

    public static final BytesArray markAsUnreachable(BytesArray ba) {
        byte[] mark = new byte[ba.getBytes().length+Packet.PACKET_SOURCE_LENGHT];
        //copy all bytes
        System.arraycopy(ba.getBytes(), 0, mark, 0, ba.getBytes().length);
        //copy data to +8
        System.arraycopy(ba.getBytes(),Packet.PACKET_DATA_LOCATION,mark,Packet.PACKET_DATA_LOCATION+Packet.PACKET_SOURCE_LENGHT,
                ba.getBytes().length-Packet.PACKET_DATA_LOCATION);
        //copy destionation to begin of data
        System.arraycopy(ba.getBytes(),Packet.PACKET_DEST_LOCATION,mark,Packet.PACKET_DATA_LOCATION,Packet.PACKET_DEST_LENGTH);
        //copy source to destination
        System.arraycopy(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION,mark,Packet.PACKET_DEST_LOCATION,Packet.PACKET_SOURCE_LENGHT);
        //mark source as unreachable
        Packet.PROTOCOL_ID_UNREACHABLE.encode(Packet.PROTOCOL_ID_UNREACHABLE, mark,Packet.PACKET_SOURCE_LOCATION);
        return new BytesArray(mark);
    }


    public static final BytesArray addUnreachableAddressForMulticast(BytesArray ba, NetUUID destination){
        byte[] data = ba.getBytes();
        byte[] _unreachable = new byte[data.length+16];
        System.arraycopy(data, 0, _unreachable,0, data.length);
        Encoder.encodeInt64(destination.getA(), _unreachable, data.length);
        Encoder.encodeInt64(destination.getB(), _unreachable, data.length+8);
        return new BytesArray(_unreachable);
    }
}