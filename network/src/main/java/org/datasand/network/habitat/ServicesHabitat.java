/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.habitat;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.network.ConnectionID;
import org.datasand.network.HabitatID;
import org.datasand.network.IFrameListener;
import org.datasand.network.Packet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServicesHabitat extends Thread implements AdjacentMachineDiscovery.AdjacentMachineListener{

    private static final int SERVICE_NODE_SWITCH_PORT = 50000;
    private final ServerSocket socket;
    private final HabitatID localHost;
    private boolean running = true;

    private HabitatsConnection[] connections = new HabitatsConnection[0];
    private HabitatsConnection[] clientConnections = new HabitatsConnection[0];
    private final Map<ConnectionID,Integer> connIndex = new HashMap<>();
    private final Map<Integer,Map<Integer,Integer>> routingTable = new HashMap<>();

    private PacketProcessor packetProcessor = null;
    private IFrameListener frameListener = null;
    private boolean unicast = false;
    private final ServicesHabitatMetrics servicesHabitatMetrics = new ServicesHabitatMetrics();
    private final AdjacentMachineDiscovery discovery;

    public ServicesHabitat(IFrameListener _frameListener) {
        this(_frameListener,false);
    }

    public ServicesHabitat(IFrameListener _frameListener, boolean unicastOnly) {
        this.frameListener = _frameListener;
        this.unicast = unicastOnly;
        synchronized(ServicesHabitat.class) {
            ServerSocket s = null;
            HabitatID id = null;
            for (int i = SERVICE_NODE_SWITCH_PORT; i < SERVICE_NODE_SWITCH_PORT + 10000; i++) {
                try {
                    new HabitatID(0, 0, 0);
                    new Packet(id, id, (byte[]) null);
                    int localhost = HabitatID.valueOf(Encoder.getLocalIPAddress() + ":0:0").getIPv4Address();
                    this.packetProcessor = new PacketProcessor(this);
                    s = new ServerSocket(i);
                    id = new HabitatID(localhost, i, 0);
                    this.setName("Service Node-" + id);
                } catch (Exception err) {
                    //err.printStackTrace();
                }
                if (s != null)
                    break;
            }
            this.socket = s;
            this.localHost = id;
            this.start();
            if(!this.unicast && this.localHost.getPort()==SERVICE_NODE_SWITCH_PORT){
                this.discovery = new AdjacentMachineDiscovery(this.localHost,this);
            }else{
                this.discovery = null;
            }
        }
    }

    private void connectToSwitch(boolean unicast){
        try {
            HabitatsConnection conn = new HabitatsConnection(this, InetAddress.getByName(Encoder.getLocalIPAddress()), SERVICE_NODE_SWITCH_PORT,unicast);
            addConnection(conn);
        }catch(UnknownHostException e){
            VLogger.error("Error opening connection to switch",e);
        }
    }

    public ServicesHabitatMetrics getServicesHabitatMetrics(){
        return this.servicesHabitatMetrics;
    }

    protected IFrameListener getFrameListener(){
        return this.frameListener;
    }

    public void shutdown() {
        this.running = false;
        this.packetProcessor.shutdown();

        try {
            this.socket.close();
        } catch (Exception err) {
        }

        if(this.discovery!=null){
            discovery.shutdown();
        }

        for(HabitatsConnection con:connections){
            try {
                con.shutdown();
            } catch (Exception err) {
            }
        }

        for (HabitatsConnection con: clientConnections) {
            try {
                con.shutdown();
            } catch (Exception err) {
            }
        }
    }

    private void addConnection(HabitatsConnection c) throws UnknownHostException {
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
            if(this.connIndex.containsKey(c.getConnectionKey())){
                VLogger.info("Found an old connection");
                if(c.isASide()){
                    connectionIndex = this.connIndex.get(c.getConnectionKey());
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

            Map<Integer,Integer> ports = this.routingTable.get(c.getIntAddress());
            if(ports==null){
                ports = new HashMap<>();
                this.routingTable.put(c.getIntAddress(),ports);
            }

            if(connectionIndex!=-1){
                connections[connectionIndex] = c;
                ports.put(c.getIntPort(),connectionIndex);
            }else
            if(nullPosition!=-1){
                connIndex.put(c.getConnectionKey(),nullPosition);
                connections[nullPosition] = c;
                ports.put(c.getIntPort(),nullPosition);
            }else{
                HabitatsConnection temp[] = new HabitatsConnection[connections.length+1];
                System.arraycopy(connections,0,temp,0,connections.length);
                connIndex.put(c.getConnectionKey(),connections.length);
                temp[connections.length] = c;
                ports.put(c.getIntPort(),connections.length);
                connections = temp;
            }
            VLogger.info(this.localHost+" added Connection for "+c.getIntPort());
        }
    }


    public void run() {

        try {
            VLogger.info("Started Node on port " + this.localHost.getPort());
            if(this.unicast || this.localHost.getPort()>SERVICE_NODE_SWITCH_PORT){
                connectToSwitch(this.unicast);
            }

            while (running) {
                Socket s = socket.accept();
                addConnection(new HabitatsConnection(this, s));
            }
        } catch (IOException e) {
            if(running) {
                VLogger.error(this.getName() + " Socket was Closed", null);
            }
        }
        VLogger.info(this.getName()+" was shutdown.");
    }

    public void send(byte data[], HabitatID source, HabitatID dest) {
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
                VLogger.error("failed to send unicast packet",e);
            }
        }else if(this.connections.length==0 && this.clientConnections.length==0){
            VLogger.error("No Connections Exist for "+this.getLocalHost(),null);
            return;
        } else {
            //This is the switch and it is a Multicast/Broadcast packet
            if(m.getDestination().getIPv4Address()==0){
                HabitatsConnection sourceCon = getNodeConnection(m.getSource().getIPv4Address(), m.getSource().getPort(),false);
                for(HabitatsConnection con:this.connections){
                    if(con==null){
                        continue;
                    }
                    m.encode(m, ba);
                    try {
                        BytesArray unreachable = con.sendPacket(ba);
                        if(unreachable!=null){
                            unreachable = HabitatsConnection.addUnreachableAddressForMulticast(unreachable, con.getIntAddress(), con.getIntPort());
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
                HabitatsConnection c = this.getNodeConnection(m.getDestination().getIPv4Address(), m.getDestination().getPort(),true);
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
                    this.receivedPacket(ba);
                }
            }
        }
    }

    public HabitatID getLocalHost() {
        return this.localHost;
    }

    public void unregisterNetworkNodeConnection(HabitatID source){
        synchronized (this.connections) {
            System.out.println("Unregister "+source);
            for(int i=0;i<this.connections.length;i++){
                if(this.connections[i].getIntAddress()==source.getIPv4Address() && this.connections[i].getIntPort()==source.getPort()){
                    this.connIndex.remove(this.connections[i].getConnectionKey());
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
        int sourceAddress = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION);
        int sourcePort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION+4);
        HabitatsConnection sourceCon = getNodeConnection(sourceAddress, sourcePort,false);
        List<HabitatID> unreachableDest = new LinkedList<HabitatID>();
        for(HabitatsConnection connection:this.connections){
            BytesArray unreachable = null;
            if (sourceAddress != this.getLocalHost().getIPv4Address()) {
                if (connection.getIntAddress() == this.getLocalHost().getIPv4Address()) {
                    try {
                        unreachable = connection.sendPacket(ba);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            } else {
                try {
                    unreachable = connection.sendPacket(ba);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
            if(unreachable!=null){
                HabitatID nid = new HabitatID(connection.getIntAddress(),connection.getIntPort(), 0);
                unreachableDest.add(nid);
                unreachable = HabitatsConnection.addUnreachableAddressForMulticast(unreachable, connection.getIntAddress(), connection.getIntPort());
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
            for(HabitatID unreach:unreachableDest){
                unregisterNetworkNodeConnection(unreach);
            }
        }
        this.receivedPacket(ba);
    }

    public void receivedPacket(BytesArray ba) {
        packetProcessor.addPacket(ba);
    }

    public HabitatsConnection getNodeConnection(int address, int port, boolean includeUnicastOnlyNodes) {
        Map<Integer, Integer> map = routingTable.get(address);
        if(!includeUnicastOnlyNodes){
            if (map == null){
                return null;
            }
            if(address==this.getLocalHost().getIPv4Address()) {
                Integer index = map.get(port);
                if(index==null){
                    VLogger.info(this.getLocalHost()+" No Connectionn for port "+port);
                    return null;
                }
                return connections[index];
            }else {
                Integer index = map.get(SERVICE_NODE_SWITCH_PORT);
                if(index==null){
                    VLogger.info(this.getLocalHost()+" No Connectionn for port "+SERVICE_NODE_SWITCH_PORT);
                    return null;
                }
                return connections[index];
            }
        }else{
            HabitatsConnection connection = null;
            if(map != null){
                if(address==this.getLocalHost().getIPv4Address() && this.getLocalHost().getPort()==SERVICE_NODE_SWITCH_PORT) {
                    Integer index = map.get(port);
                    if(index==null){
                        VLogger.info(this.getLocalHost()+" No Connectionn for port "+port);
                        return null;
                    }
                    connection = connections[index];
                }else {
                    Integer index = map.get(SERVICE_NODE_SWITCH_PORT);
                    if(index==null){
                        VLogger.info(this.getLocalHost()+" No Connectionn for port "+SERVICE_NODE_SWITCH_PORT);
                        return null;
                    }
                    connection = connections[index];
                }
            }

            if(connection!=null){
                return connection;
            }

            for(HabitatsConnection c:this.clientConnections){
                if(c.getIntAddress()==address && c.getIntPort()==port){
                    connection = c;
                    break;
                }
            }

            return connection;
        }
    }

    public void joinNetworkAsSingle(String host) {
        if (this.getLocalHost().getPort() != 50000) {
            System.err.println("Only the node binded to port 50000 can join an external network.");
            return;
        }
        try {
            new HabitatsConnection(this, InetAddress.getByName(host), 50000,true);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void joinNetwork(String host) {
        if (this.getLocalHost().getPort() != 50000) {
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
    public void notifyAdjacentDiscovered(HabitatID adjacentID) {
        HabitatsConnection existingConnection = getNodeConnection(adjacentID.getIPv4Address(),adjacentID.getPort(),true);
        if(existingConnection==null || !existingConnection.isAlive()){
            joinNetwork(adjacentID.getIPv4AddressAsString());
        }
    }
}
