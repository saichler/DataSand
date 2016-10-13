/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.codec.util.ThreadNode;
import org.datasand.network.ConnectionID;
import org.datasand.network.IFrameListener;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.RoutingTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServicesHabitat extends ThreadNode implements AdjacentMachineDiscovery.AdjacentMachineListener{

    public static final int SERVICE_NODE_SWITCH_PORT = 50000;
    private static final Logger LOG = LoggerFactory.getLogger(ServicesHabitat.class);
    private final ServerSocket socket;
    private final NetUUID netUUID;
    private final int servicePort;

    private HabitatsConnection[] connections = new HabitatsConnection[0];
    private HabitatsConnection[] clientConnections = new HabitatsConnection[0];

    private final Map<ConnectionID,Integer> connIndex = new HashMap<>();
    private final PacketProcessor packetProcessor = new PacketProcessor(this);
    private IFrameListener frameListener = null;
    private boolean unicast = false;
    private final ServicesHabitatMetrics servicesHabitatMetrics = new ServicesHabitatMetrics();
    private final AdjacentMachineDiscovery discovery;
    private final RepetitiveTaskContainer repetitiveTaskContainer = new RepetitiveTaskContainer(this);
    private final RoutingTable routingTable = new RoutingTable();

    public ServicesHabitat(IFrameListener _frameListener) {
        this(_frameListener,false);
    }

    public ServicesHabitat(IFrameListener _frameListener, boolean unicastOnly) {
        super((ThreadNode)_frameListener,"");
        this.frameListener = _frameListener;
        this.unicast = unicastOnly;
        synchronized(ServicesHabitat.class) {
            ServerSocket s = null;
            int selectedPort = -1;
            for (int i = SERVICE_NODE_SWITCH_PORT; i < SERVICE_NODE_SWITCH_PORT + 10000; i++) {
                try {
                    s = new ServerSocket(i);
                    selectedPort = i;
                    this.setName("Service Habitat-" + selectedPort);
                } catch (Exception err) {
                    LOG.info("Failed to bind to port "+i+", will try next one.");
                }
                if (s != null)
                    break;
            }
            this.socket = s;
            this.servicePort = selectedPort;
            this.netUUID = loadHabitatID();
            if(!this.unicast && this.servicePort ==SERVICE_NODE_SWITCH_PORT){
                this.discovery = new AdjacentMachineDiscovery(this.netUUID,this);
            }else{
                this.discovery = null;
            }
        }

        if(_frameListener==null) {
            this.start();
        }
    }

    private NetUUID loadHabitatID() {
        File habitatIdFile = new File("./data/ids/hid-"+this.servicePort +".txt");
        NetUUID result = null;
        if(habitatIdFile.exists()){
            try {
                FileInputStream in = new FileInputStream(habitatIdFile);
                byte data[] = new byte[(int)habitatIdFile.length()];
                in.read(data);
                in.close();
                result = new NetUUID(UUID.fromString(new String(data)));
            }catch(IOException e){
                LOG.error("Failed to load habitat ID.",e);
            }
        }else {
            if(!habitatIdFile.getParentFile().exists()){
                habitatIdFile.getParentFile().mkdirs();
            }
            try {
                FileOutputStream out = new FileOutputStream(habitatIdFile);
                result = new NetUUID(UUID.randomUUID());
                out.write(result.toString().getBytes());
                out.close();
            }catch(IOException e){
                LOG.error("Failed to write Habitat ID",e);
            }
        }
        return result;
    }

    public int getServicePort(){
        return this.servicePort;
    }

    private void connectToSwitch(boolean unicast){
        try {
            HabitatsConnection conn = new HabitatsConnection(this, InetAddress.getByName(Encoder.getLocalIPAddress()), SERVICE_NODE_SWITCH_PORT,unicast);
            addConnection(conn);
        }catch(UnknownHostException e){
            LOG.error("Error opening connection to switch",e);
        }
    }

    public ServicesHabitatMetrics getServicesHabitatMetrics(){
        return this.servicesHabitatMetrics;
    }

    protected IFrameListener getFrameListener(){
        return this.frameListener;
    }

    public void shutdown() {
        super.shutdown();
        try {
            this.socket.close();
        } catch (Exception err) {
        }
    }

    private void addConnection(HabitatsConnection c) throws UnknownHostException {
        c.start();
        synchronized (this.connections){
            if(c.isUnicast()){
                boolean foundSpace = false;
                for(int i=0;i<this.clientConnections.length;i++){
                    if(this.clientConnections[i]==null || !this.clientConnections[i].isAlive()){
                        this.clientConnections[i]=c;
                        foundSpace=true;
                    }
                }
                if(!foundSpace){
                    HabitatsConnection tmp[] = new HabitatsConnection[this.clientConnections.length+1];
                    System.arraycopy(this.clientConnections,0,tmp,0,this.clientConnections.length);
                    tmp[this.clientConnections.length]=c;
                    this.clientConnections=tmp;
                }
                return;
            }

            int connectionIndex = -1;
            if(this.connIndex.containsKey(c.getConnectionID())){
                LOG.info("Found an old connection");
                if(c.isASide()){
                    connectionIndex = this.connIndex.get(c.getConnectionID());
                    this.connections[connectionIndex].shutdown();
                    this.connections[connectionIndex] = null;
                }else {
                    c.shutdown();
                    return;
                }
            }

            int nullPosition = -1;
            for(int i=0;i<connections.length;i++){
                if(connections[i]!=null && !connections[i].isAlive()){
                    connections[i].shutdown();
                    connections[i] = null;
                    nullPosition = -1;
                }
            }

            if(connectionIndex!=-1){
                connections[connectionIndex] = c;
            }else
            if(nullPosition!=-1){
                connIndex.put(c.getConnectionID(),nullPosition);
                connections[nullPosition] = c;
            }else{
                HabitatsConnection temp[] = new HabitatsConnection[connections.length+1];
                System.arraycopy(connections,0,temp,0,connections.length);
                connIndex.put(c.getConnectionID(),connections.length);
                temp[connections.length] = c;
                connections = temp;
            }
            this.routingTable.add(c.getConnectionID().getAdjacentNetUUID(this.netUUID),c.getConnectionID());
            LOG.info(this.netUUID+" added Connection for "+c.getConnectionID());
        }
    }


    public void initialize(){
        if(this.unicast || this.getServicePort()>SERVICE_NODE_SWITCH_PORT){
            connectToSwitch(this.unicast);
        }
    }

    public void distruct(){

    }

    public void execute() throws Exception{
        Socket s = socket.accept();
        addConnection(new HabitatsConnection(this, s));
    }

    public void send(byte data[], NetUUID source, NetUUID dest) {
        if (data.length < Packet.MAX_DATA_IN_ONE_PACKET) {
            Packet p = new Packet(source, dest, data);
            send(p);
        } else {
            int count = data.length / Packet.MAX_DATA_IN_ONE_PACKET;
            if (data.length % Packet.MAX_DATA_IN_ONE_PACKET > 0)
                count++;
            byte[] countData = new byte[4];
            Encoder.encodeInt32(count, countData, 0);
            Packet header = new Packet(source, dest, countData, -1, true);
            send(header);
            for (int i = 0; i < count; i++) {
                byte[] pData = new byte[Packet.MAX_DATA_IN_ONE_PACKET];
                if (i < count - 1) {
                    System.arraycopy(data, i * Packet.MAX_DATA_IN_ONE_PACKET,
                            pData, 0, pData.length);
                } else {
                    System.arraycopy(data, i * Packet.MAX_DATA_IN_ONE_PACKET,
                            pData, 0, data.length
                                    - (i * Packet.MAX_DATA_IN_ONE_PACKET));
                }
                send(new Packet(source, dest, pData, header.getPacketID(), true));
            }
        }
    }

    public void send(Packet m) {
        BytesArray ba = new BytesArray(new byte[Packet.PACKET_DATA_LOCATION + m.getData().length]);

        if(this.unicast){
            try {
                m.encode(m,ba);
                this.clientConnections[0].sendPacket(ba);
            } catch (IOException e) {
                LOG.error("failed to send unicast packet",e);
            }
        }else if(this.connections.length==0 && this.clientConnections.length==0){
            LOG.error("No Connections Exist for "+this.netUUID);
            return;
        } else {
            //This is the switch and it is a Multicast/Broadcast packet
            if(m.getDestination().getA()==0){
                HabitatsConnection sourceCon = getNodeConnection(m.getSource(),false);
                for(HabitatsConnection con:this.connections){
                    if(con==null){
                        continue;
                    }
                    m.encode(m, ba);
                    try {
                        BytesArray unreachable = con.sendPacket(ba);
                        if(unreachable!=null){
                            unreachable = HabitatsConnection.addUnreachableAddressForMulticast(unreachable,con.getConnectionID().getAdjacentNetUUID(this.netUUID));
                            if(sourceCon!=null){
                                sourceCon.sendPacket(unreachable);
                            }else{
                                this.receivedPacket(unreachable);
                            }
                        }
                    } catch (IOException e) {
                        VLogger.error("Failed to multicast/broadcast packet",e);
                    }
                }
                m.encode(m, ba);
                this.receivedPacket(ba);
            }else{
                HabitatsConnection c = this.getNodeConnection(m.getDestination(),true);
                if (c != null) {
                    try {
                        m.encode(m, ba);
                        BytesArray unreachable = c.sendPacket(ba);
                        if(unreachable!=null){
                            unregisterNetworkNodeConnection(m.getDestination());
                            this.receivedPacket(unreachable);
                        }
                    } catch (Exception err) {
                        VLogger.error("Failed to send packet",err);
                    }
                } else {
                    m.encode(m,ba);
                    BytesArray unreachable = HabitatsConnection.markAsUnreachable(ba);
                    this.receivedPacket(unreachable);
                }
            }
        }
    }

    public NetUUID getNetUUID() {
        return this.netUUID;
    }

    public void unregisterNetworkNodeConnection(NetUUID source){
        synchronized (this.connections) {
            LOG.info("Unregister "+source);
            for(int i=0;i<this.connections.length;i++){
                if(this.connections[i].getConnectionID().getAdjacentNetUUID(this.netUUID).equals(source)){
                    this.connIndex.remove(this.connections[i].getConnectionID());
                    this.connections[i].shutdown();
                    this.connections[i] = null;
                    HabitatsConnection temp[] = new HabitatsConnection[this.connections.length-1];
                    System.arraycopy(this.connections,0,temp,0,i);
                    System.arraycopy(this.connections,i+1,temp,i,this.connections.length-i-1);
                    this.connections = temp;
                    break;
                }
            }
        }
    }

    public void broadcast(BytesArray ba) {
        NetUUID source = new NetUUID(Encoder.decodeInt64(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION),
                Encoder.decodeInt64(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION+8));

        HabitatsConnection sourceCon = getNodeConnection(source,false);
        List<NetUUID> unreachableDest = new LinkedList<NetUUID>();
        for(HabitatsConnection connection:this.connections){
            BytesArray unreachable = null;
            try {
                unreachable = connection.sendPacket(ba);
            } catch (Exception e) {
                LOG.error("Failed to broadcast",e);
            }

            if(unreachable!=null){
                NetUUID nid = connection.getConnectionID().getAdjacentNetUUID(this.netUUID);
                unreachableDest.add(nid);
                unreachable = HabitatsConnection.addUnreachableAddressForMulticast(unreachable,nid);
                if(sourceCon!=null){
                    try{
                        sourceCon.sendPacket(unreachable);
                    }catch(Exception err){
                        err.printStackTrace();
                    }
                }else{
                    this.receivedPacket(unreachable);
                }
            }
        }
        if(!unreachableDest.isEmpty()){
            for(NetUUID unreach:unreachableDest){
                unregisterNetworkNodeConnection(unreach);
            }
        }
        this.receivedPacket(ba);
    }

    public void receivedPacket(BytesArray ba) {
        packetProcessor.addPacket(ba);
    }

    public HabitatsConnection getNodeConnection(NetUUID dest, boolean includeUnicastOnlyNodes) {
        if(this.servicePort==SERVICE_NODE_SWITCH_PORT) {
            ConnectionID connID = this.routingTable.get(dest);
            if (connID == null) {
                return null;
            }
            Integer index = this.connIndex.get(connID);
            if (index != null) {
                return this.connections[index];
            }

            if (includeUnicastOnlyNodes) {
                for (HabitatsConnection c : this.clientConnections) {
                    if (c.getConnectionID().equals(connID)) {
                        return c;
                    }
                }
            }
        }else{
          return this.connections[0];
        }
        return null;
    }

    public void joinNetworkAsSingle(String host) {
        if (this.getServicePort() != 50000) {
            System.err.println("Only the node binded to port 50000 can join an external network.");
            return;
        }
        try {
            new HabitatsConnection(this, InetAddress.getByName(host), 50000,true);
        } catch (Exception e) {
            LOG.error("Failed to join network",e);
        }
    }

    public void joinNetwork(String host) {
        if (this.getServicePort() != 50000) {
            System.err.println("Only the node binded to port 50000 can join an external network.");
            return;
        }
        try {
            new HabitatsConnection(this, InetAddress.getByName(host), 50000);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void notifyAdjacentDiscovered(NetUUID adjacentID,String host) {
        HabitatsConnection existingConnection = getNodeConnection(adjacentID,true);
        if(existingConnection==null || !existingConnection.isAlive()){
            joinNetwork(host);
        }
    }
}
