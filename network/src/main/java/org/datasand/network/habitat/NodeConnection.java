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
import org.datasand.codec.VLogger;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.ConnectionID;
import org.datasand.network.NID;
import org.datasand.network.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class NodeConnection extends ThreadNode {

    private static final Logger LOG = LoggerFactory.getLogger(NodeConnection.class);
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Node node;
    private final boolean unicast;
    private final Switch hcSwitch;
    private final ConnectionID connectionID;

    public NodeConnection(Node node, InetAddress addr, int port, boolean unicastOnly) {
        super(node, node.getName()+" Connection");
        this.node = node;
        Socket tmpSocket = null;
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;
        BytesArray myData = new BytesArray(20);
        BytesArray otherData = new BytesArray(20);
        NID id = this.node.getNID();
        id.encode(id,myData);
        try {
            tmpSocket = new Socket(addr, port);
            tmpIn = new DataInputStream(new BufferedInputStream(tmpSocket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(tmpSocket.getOutputStream()));
            tmpOut.write(myData.getData());
            if(unicastOnly){
                tmpOut.writeInt(1);
            }else{
                tmpOut.writeInt(0);
            }
            tmpOut.flush();
            tmpIn.read(otherData.getBytes());
        } catch(IOException e){
            LOG.error("Failed to open socket",e);
        }

        this.socket = tmpSocket;
        NID oID = (NID)id.decode(otherData);
        this.connectionID = new ConnectionID(id.getNetwork(),id.getUuidA(),id.getUuidB(),oID.getNetwork(),oID.getUuidA(),oID.getUuidB());
        this.in = tmpIn;
        this.out = tmpOut;
        this.unicast = unicastOnly;
        this.setName(this.node.getServicePort()+" connection "+this.connectionID);
        hcSwitch = new Switch(this);
    }

    public NodeConnection(Node node, InetAddress addr, int port) {
        this(node,addr,port,false);
    }

    public NodeConnection(Node node, Socket socket) {
        super(node, node.getName()+" Connection");
        this.socket = socket;
        this.node = node;
        this.setDaemon(true);
        DataInputStream tmpIn=null;
        DataOutputStream tmpOut = null;
        int unicastOnly = 0;
        BytesArray myData = new BytesArray(20);
        BytesArray otherData = new BytesArray(20);
        NID id = this.node.getNID();
        id.encode(id,myData);
        try {
            tmpIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            tmpOut = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            tmpIn.read(otherData.getBytes());
            unicastOnly = tmpIn.readInt();
            tmpOut.write(myData.getData());
            tmpOut.flush();
        } catch (Exception e) {
            LOG.error("Failed to use input/output of socket",e);
        }
        this.in = tmpIn;
        this.out = tmpOut;
        NID oID = (NID)id.decode(otherData);
        this.connectionID = new ConnectionID(id.getNetwork(),id.getUuidA(),id.getUuidB(),oID.getNetwork(),oID.getUuidA(),oID.getUuidB());

        if(unicastOnly==1){
            this.unicast = true;
        }else{
            this.unicast = false;
        }
        this.setName(this.node.getServicePort()+" connection "+this.connectionID);
        this.hcSwitch = new Switch(this);
    }

    public boolean isUnicast(){
        return this.unicast;
    }

    protected Node getNode(){
        return this.node;
    }

    public boolean isASide(){
        if(this.connectionID.getaSide().equals(this.node.getNID())){
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
                    return Packet.markAsUnreachable(ba);
                }
            }else{
                return Packet.markAsUnreachable(ba);
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

    public static final BytesArray addUnreachableAddressForMulticast(BytesArray ba, NID destination){
        byte[] data = ba.getBytes();
        byte[] _unreachable = new byte[data.length+Packet.PACKET_SOURCE_LENGHT];
        System.arraycopy(data, 0, _unreachable,0, data.length);
        Packet.PROTOCOL_ID_UNREACHABLE.encode(destination,_unreachable,data.length);
        return new BytesArray(_unreachable);
    }
}