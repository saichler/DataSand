/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VLogger;
import org.datasand.network.IFrameListener;
import org.datasand.network.Packet;
import org.datasand.network.ServiceID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServiceNode extends Thread {

    private static final int SERVICE_NODE_SWITCH_PORT = 50000;
    private final ServerSocket socket;
    private final ServiceID localHost;
    private boolean running = false;

    private ServiceNodeConnection[] connections = new ServiceNodeConnection[0];
    private ServiceNodeConnection[] unicastOnlyRoutingTable = new ServiceNodeConnection[0];
    private final Map<String,Integer> connIndex = new HashMap<>();
    private final Map<Integer,Map<Integer,Integer>> routingTable = new HashMap<>();

    private final Set<ServiceID> incomingConnections = new HashSet<ServiceID>();

    private ServicePacketProcessor packetProcessor = null;
    private IFrameListener frameListener = null;
    private boolean unicast = false;
    private DiscoverNetworkAdjacentsListener discoveryListener = null;
    private final ServiceNodeMetrics serviceNodeMetrics = new ServiceNodeMetrics();

    public ServiceNode(IFrameListener _frameListener) {
        this(_frameListener,false);
    }

    protected Set<ServiceID> getIncomingConnections(){
        return this.incomingConnections;
    }

    public ServiceNode(IFrameListener _frameListener, boolean unicastOnly) {
        this.frameListener = _frameListener;
        this.unicast = unicastOnly;
        synchronized(ServiceNode.class) {
            ServerSocket s = null;
            ServiceID id = null;
            for (int i = SERVICE_NODE_SWITCH_PORT; i < SERVICE_NODE_SWITCH_PORT + 10000; i++) {
                try {
                    new ServiceID(0, 0, 0);
                    new Packet(id, id, (byte[]) null);
                    int localhost = ServiceID.valueOf(Encoder.getLocalIPAddress() + ":0:0").getIPv4Address();
                    this.packetProcessor = new ServicePacketProcessor(this);
                    s = new ServerSocket(i);
                    id = new ServiceID(localhost, i, 0);
                    this.setName("Service Node-" + this.getLocalHost());
                    this.start();
                    VLogger.info("Started Node on port" + i);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                if (s != null)
                    break;
            }
            this.socket = s;
            this.localHost = id;
        }
    }

    public ServiceNodeMetrics getServiceNodeMetrics(){
        return this.serviceNodeMetrics;
    }

    protected IFrameListener getFrameListener(){
        return this.frameListener;
    }

    public void shutdown() {
        this.running = false;
        this.packetProcessor.shutdown();

        try {
            this.connection.shutdown();
        } catch (Exception err) {
        }

        try {
            this.socket.close();
        } catch (Exception err) {
        }

        if(this.discoveryListener!=null){
            try{this.discoveryListener.datagramSocket.close();}catch(Exception err){}
        }

        for(ServiceNodeConnection con:connections){
            try {
                con.shutdown();
            } catch (Exception err) {
            }
        }

        for (ServiceNodeConnection con: unicastOnlyRoutingTable) {
            try {
                con.shutdown();
            } catch (Exception err) {
            }
        }
    }

    private void addConnection(ServiceNodeConnection c) throws UnknownHostException {
        synchronized (this.connections){
            int connectionIndex = -1;
            if(this.connIndex.containsKey(c.getConnectionKey())){
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
                ports.put(c.getIntPort(),connectionIndex)
            }else
            if(nullPosition!=-1){
                connIndex.put(c.getConnectionKey(),nullPosition);
                connections[nullPosition] = c;
                ports.put(c.getIntPort(),nullPosition);
            }else{
                ServiceNodeConnection temp[] = new ServiceNodeConnection[connections.length+1];
                System.arraycopy(connections,0,temp,0,connections.length);
                connIndex.put(c.getConnectionKey(),connections.length);
                temp[connections.length] = c;
                ports.put(c.getIntPort(),connections.length);
                connections = temp;
            }
        }
    }


    public void run() {
        running = true;
        if(this.getLocalHost().getPort()==50000 && !unicast){
            this.discoveryListener = new DiscoverNetworkAdjacentsListener();
            new DiscoverNetworkAdjacentsPulse();
        }
        try {
            while (running) {
                Socket s = socket.accept();
                addConnection(new ServiceNodeConnection(this, s));
            }
        } catch (Exception err) {
            // err.printStackTrace();
        }
        System.out.println(this.getName()+" was shutdown.");
    }

    public void send(byte data[], ServiceID source, ServiceID dest) {
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

        if(this.connections.length==0){
            VLogger.error("No Connections Exist for "+this.getLocalHost(),null);
            return;
        }else
        if(this.connections.length==1){
            m.encode(m,ba);
            try {
                this.connections[0].sendPacket(ba);
            } catch (IOException e) {
                VLogger.error("Failed to send packet",e);
            }
        }else{
            //This is the switch and it is a Multicast/Broadcast packet
            if(m.getDestination().getIPv4Address()==0){
                ServiceNodeConnection sourceCon = getNodeConnection(m.getSource().getIPv4Address(), m.getSource().getPort(),false);
                for(ServiceNodeConnection con:this.connections){
                    m.encode(m, ba);
                    try {
                        BytesArray unreachable = con.sendPacket(ba);
                        if(unreachable!=null){
                            unreachable = ServiceNodeConnection.addUnreachableAddressForMulticast(unreachable, destAddress, destPort);
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
            }
        }

        if (this.connection != null) {
            try {
                m.encode(m, ba);
                this.connection.sendPacket(ba);
            } catch (Exception err) {
                err.printStackTrace();
            }
        } else {
            //Multicast/Broadcast from the switch
            if(m.getDestination().getIPv4Address()==0){
                ServiceNodeConnection sourceCon = getNodeConnection(m.getSource().getIPv4Address(), m.getSource().getPort(),false);
                for(Map.Entry<Integer,Map<Integer, ServiceNodeConnection>> entry:this.connections){
                    int destAddress = entry.getKey();
                    Map<Integer,ServiceNodeConnection> map = entry.getValue();
                    for(Map.Entry<Integer, ServiceNodeConnection> pMap:map.entrySet()){
                        int destPort = pMap.getKey();
                        ServiceNodeConnection c = pMap.getValue();
                        try {
                            m.encode(m, ba);
                            BytesArray unreachable = c.sendPacket(ba);
                            if(unreachable!=null){
                                unreachable = ServiceNodeConnection.addUnreachableAddressForMulticast(unreachable, destAddress, destPort);
                                if(sourceCon!=null){
                                    sourceCon.sendPacket(unreachable);
                                }else{
                                    this.receivedPacket(unreachable);
                                }
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                }
                m.encode(m, ba);
                this.receivedPacket(ba);
            }else{
                ServiceNodeConnection c = this.getNodeConnection(m.getDestination().getIPv4Address(), m.getDestination().getPort(),true);
                if (c != null) {
                    try {
                        m.encode(m, ba);
                        BytesArray unreachable = c.sendPacket(ba);
                        if(unreachable!=null){
                            unregisterNetworkNodeConnection(m.getDestination());
                            this.receivedPacket(unreachable);
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                } else {
                    m.encode(m,ba);
                    BytesArray unreachable = ServiceNodeConnection.markAsUnreachable(ba);
                    this.receivedPacket(ba);
                }
            }
        }
    }

    private class DiscoverNetworkAdjacentsPulse extends Thread{
        private DiscoverNetworkAdjacentsPulse(){
            this.setName("DiscoveryPluse");
            this.start();
        }
        public void run(){
            if(unicast) return;
            while(running){
                BytesArray ba = new BytesArray(new byte[8]);
                Encoder.getSerializerByClass(ServiceID.class).encode(getLocalHost(), ba);
                byte data[] = ba.getData();
                try{
                    DatagramPacket packet = new DatagramPacket(data,data.length,InetAddress.getByName("255.255.255.255"),49999);
                    DatagramSocket s = new DatagramSocket();
                    s.send(packet);
                    Thread.sleep(10000);
                    s.close();
                }catch(Exception err){
                    err.printStackTrace();
                }
            }
        }
    }

    private class DiscoverNetworkAdjacentsListener extends Thread{
        private DatagramSocket datagramSocket = null;
        public DiscoverNetworkAdjacentsListener(){
            this.setName("DiscoveryListener");
            try{
                this.datagramSocket = new DatagramSocket(49999);
            }catch(Exception err){
                err.printStackTrace();
            }
            this.start();
        }

        public void run(){
            while(running){
                byte data[] = new byte[8];
                DatagramPacket packet = new DatagramPacket(data,data.length);
                try{
                    this.datagramSocket.receive(packet);
                    processIncomingPacket(packet);
                }catch(Exception err){
                    break;
                }
            }
            try{
                this.datagramSocket.close();
            }catch(Exception err){
            }
        }
        private void processIncomingPacket(DatagramPacket p){
            BytesArray ba = new BytesArray(p.getData());
            ServiceID id = (ServiceID) Encoder.getSerializerByClass(ServiceID.class).decode(ba);
            if(!id.equals(getLocalHost())){
                ServiceNodeConnection node = getNodeConnection(id.getIPv4Address(), id.getPort(), true);
                if(node==null){
                    if(unicast){
                        joinNetworkAsSingle(id.getIPv4AddressAsString());
                    }else{
                        joinNetwork(id.getIPv4AddressAsString());
                    }
                }
            }
        }
    }

    public ServiceID getLocalHost() {
        return this.localHost;
    }

    public boolean registerNetworkNodeConnection(ServiceNodeConnection c, ServiceID source) {
        synchronized (this) {
            c.setName("Con " + getLocalHost() + "<->" + source);
            if (this.localHost.getPort() != 50000 && this.connection == null) {
                this.connection = c;
                return true;
            } else {
                Map<Integer, ServiceNodeConnection> map = this.routingTable.get(source.getIPv4Address());
                if (map == null) {
                    map = new ConcurrentHashMap<Integer, ServiceNodeConnection>();
                    this.routingTable.put(source.getIPv4Address(), map);
                }
                if (map.containsKey(source.getPort())) {
                    return false;
                }
                map.put(source.getPort(), c);
                return true;
            }
        }
    }

    public boolean registerSingleNetworkNodeConnection(ServiceNodeConnection c, ServiceID source) {
        synchronized (this) {
            c.setName("Con " + getLocalHost() + "<->" + source);
            System.out.println("Registering Unicast Agent-"+source);
            if (this.localHost.getPort() != 50000 && this.connection == null) {
                this.connection = c;
                return true;
            } else {
                Map<Integer, ServiceNodeConnection> map = this.unicastOnlyRoutingTable.get(source.getIPv4Address());
                if (map == null) {
                    map = new ConcurrentHashMap<Integer, ServiceNodeConnection>();
                    this.unicastOnlyRoutingTable.put(source.getIPv4Address(), map);
                }
                if (map.containsKey(source.getPort())) {
                    return false;
                }
                map.put(source.getPort(), c);
                return true;
            }
        }
    }

    public void unregisterNetworkNodeConnection(ServiceID source){
        synchronized (this) {
            System.out.println("Unregister "+source);
            Map<Integer, ServiceNodeConnection> map = this.routingTable.get(source.getIPv4Address());
            if(map!=null){
                map.remove(source.getPort());
            }
            incomingConnections.remove(source);
        }
    }

    public void broadcast(BytesArray ba) {
        int sourceAddress = Encoder.decodeInt32(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION);
        int sourcePort = Encoder.decodeInt16(ba.getBytes(),Packet.PACKET_SOURCE_LOCATION+4);
        ServiceNodeConnection sourceCon = getNodeConnection(sourceAddress, sourcePort,false);
        List<ServiceID> unreachableDest = new LinkedList<ServiceID>();
        for (Map.Entry<Integer, Map<Integer, ServiceNodeConnection>> addrEntry : this.routingTable.entrySet()) {
            for (Map.Entry<Integer, ServiceNodeConnection> portEntry : addrEntry.getValue().entrySet()) {
                BytesArray unreachable = null;
                if (sourceAddress != this.getLocalHost().getIPv4Address()) {
                    if (addrEntry.getKey() == this.getLocalHost().getIPv4Address()) {
                        try {
                            unreachable = portEntry.getValue().sendPacket(ba);
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                } else {
                    try {
                        unreachable = portEntry.getValue().sendPacket(ba);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
                if(unreachable!=null){
                    ServiceID nid = new ServiceID(addrEntry.getKey(),portEntry.getKey(), 0);
                    unreachableDest.add(nid);
                    unreachable = ServiceNodeConnection.addUnreachableAddressForMulticast(unreachable, addrEntry.getKey(), portEntry.getKey());
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
        }
        if(!unreachableDest.isEmpty()){
            for(ServiceID unreach:unreachableDest){
                unregisterNetworkNodeConnection(unreach);
            }
        }
        this.receivedPacket(ba);
    }

    public void receivedPacket(BytesArray ba) {
        packetProcessor.addPacket(ba);
    }

    public ServiceNodeConnection getNodeConnection(int address, int port, boolean includeUnicastOnlyNodes) {
        Map<Integer, Integer> map = routingTable.get(address);
        if(!includeUnicastOnlyNodes){
            if (map == null){
                return null;
            }
            if(address==this.getLocalHost().getIPv4Address()) {
                Integer index = map.get(port);
                return connections[index];
            }else {
                Integer index = map.get(SERVICE_NODE_SWITCH_PORT);
                return connections[index];
            }
        }else{
            ServiceNodeConnection connection = null;
            if(map != null){
                if(address==this.getLocalHost().getIPv4Address()) {
                    Integer index = map.get(port);
                    connection = connections[index];
                }else {
                    Integer index = map.get(SERVICE_NODE_SWITCH_PORT);
                    connection = connections[index];
                }
            }

            if(connection!=null){
                return connection;
            }

            map = unicastOnlyRoutingTable.get(address);
            if(map != null){
                if(address==this.getLocalHost().getIPv4Address())
                    connection = map.get(port);
                else
                    connection = map.get(50000);
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
            new ServiceNodeConnection(this, InetAddress.getByName(host), 50000,true);
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
            new ServiceNodeConnection(this, InetAddress.getByName(host), 50000);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
