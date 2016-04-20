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

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ServiceNode extends Thread {

    private final ServerSocket socket;
    private final ServiceID localHost;
    private boolean running = false;

    private ServiceNodeConnection connection = null;
    private Map<Integer, Map<Integer, ServiceNodeConnection>> routingTable = new ConcurrentHashMap<Integer, Map<Integer, ServiceNodeConnection>>();
    private Map<Integer, Map<Integer, ServiceNodeConnection>> unicastOnlyRoutingTable = new ConcurrentHashMap<Integer, Map<Integer, ServiceNodeConnection>>();

    protected Set<ServiceID> incomingConnections = new HashSet<ServiceID>();

    private PacketProcessor packetProcessor = null;
    private IFrameListener frameListener = null;
    private Packet serializer = new Packet((Object) null, null);
    private boolean unicast = false;
    private DiscoverNetworkAdjacentsListener discoveryListener = null;

    public ServiceNode(IFrameListener _frameListener) {
        this(_frameListener,false);
    }
    public ServiceNode(IFrameListener _frameListener, boolean unicastOnly) {
        this.frameListener = _frameListener;
        this.unicast = unicastOnly;
        ServerSocket s = null;
        ServiceID id = null;
        for (int i = 50000; i < 60000; i++) {
            try {
                new ServiceID(0, 0, 0);
                new Packet(id, id, (byte[]) null);
                int localhost = ServiceID.valueOf(
                        Encoder.getLocalIPAddress() + ":0:0").getIPv4Address();
                s = new ServerSocket(i);
                id = new ServiceID(localhost, i, 0);
                if (i > 50000){
                    new RequestConnection(unicastOnly);
                }
                this.setName("Node-" + this.getLocalHost());
                this.start();
            } catch (Exception err) {
            }
            if (s != null)
                break;
        }
        this.socket = s;
        this.localHost = id;
    }

    public void shutdown() {
        this.running = false;
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
        for (Map<Integer, ServiceNodeConnection> map : routingTable.values()) {
            for (ServiceNodeConnection con : map.values()) {
                try {
                    con.shutdown();
                } catch (Exception err) {
                }
            }
        }
        for (Map<Integer, ServiceNodeConnection> map : unicastOnlyRoutingTable.values()) {
            for (ServiceNodeConnection con : map.values()) {
                try {
                    con.shutdown();
                } catch (Exception err) {
                }
            }
        }
    }

    public void run() {
        running = true;
        if(this.getLocalHost().getPort()==50000 && !unicast){
            this.discoveryListener = new DiscoverNetworkAdjacentsListener();
            new DiscoverNetworkAdjacentsPulse();
        }
        packetProcessor = new PacketProcessor();
        try {
            while (running) {
                Socket s = socket.accept();
                new ServiceNodeConnection(this, s);
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
                for(Map.Entry<Integer,Map<Integer, ServiceNodeConnection>> entry:this.routingTable.entrySet()){
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

    private class RequestConnection extends Thread {

        private String connType = "R";

        public RequestConnection(boolean single) {
            if(single)
                connType = "U";
            this.setName("Request Connection Thread "+connType+":"+ getLocalHost().getPort() + "    ");
            this.start();
        }

        public void run() {
            while (connection == null) {
                String conString = connType+":" + getLocalHost().getPort() + "    ";
                try {
                    Socket s = new Socket("localhost", 50000);
                    s.getOutputStream().write(conString.getBytes());
                    s.getOutputStream().flush();
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(1000);
                        if (connection != null)
                            break;
                    }
                    s.close();
                } catch (Exception err) {
                    err.printStackTrace();
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
        Map<Integer, ServiceNodeConnection> map = routingTable.get(address);
        if(!includeUnicastOnlyNodes){
            if (map == null){
                return null;
            }
            if(address==this.getLocalHost().getIPv4Address())
                return map.get(port);
            else
                return map.get(50000);
        }else{
            ServiceNodeConnection connection = null;
            if(map != null){
                if(address==this.getLocalHost().getIPv4Address())
                    connection = map.get(port);
                else
                    connection = map.get(50000);
            }
            if(connection!=null) return connection;
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

    private class PacketProcessor extends Thread {
        private LinkedList<BytesArray> incomingFrames = new LinkedList<BytesArray>();
        private Map<Packet, MultiPartContainer> multiparts = new HashMap<Packet, MultiPartContainer>();

        private class MultiPartContainer {
            private List<BytesArray> parts = new LinkedList<BytesArray>();
            private int expectedCount = -1;

            public BytesArray toFrame() {
                BytesArray firstPart = parts.get(0);
                byte[] data = new byte[(firstPart.getBytes().length - Packet.PACKET_DATA_LOCATION)
                        * parts.size() + Packet.PACKET_DATA_LOCATION];
                System.arraycopy(firstPart, 0, data, 0,
                        Packet.PACKET_DATA_LOCATION);
                int location = Packet.PACKET_DATA_LOCATION;
                for (BytesArray p : parts) {
                    System.arraycopy(p.getBytes(), Packet.PACKET_DATA_LOCATION, data,
                            location, p.getBytes().length - Packet.PACKET_DATA_LOCATION);
                    location += (p.getBytes().length - Packet.PACKET_DATA_LOCATION);
                }
                return new BytesArray(data);
            }
        }

        public PacketProcessor() {
            this.setName("Packet Processor For " + getLocalHost());
            this.start();
        }

        public void addPacket(BytesArray ba) {
            boolean multiPart = ba.getBytes()[Packet.PACKET_MULTIPART_AND_PRIORITY_LOCATION] % 2 == 1;

            // The packet is a complete frame
            if (!multiPart) {
                synchronized (incomingFrames) {
                    incomingFrames.add(ba);
                    incomingFrames.notifyAll();
                }
            } else {
                Packet pID = (Packet) serializer.decode(ba);
                MultiPartContainer mpc = multiparts.get(pID);
                if (mpc == null) {
                    mpc = new MultiPartContainer();
                    multiparts.put(pID, mpc);
                    mpc.expectedCount = Encoder.decodeInt32(ba.getBytes(), Packet.PACKET_DATA_LOCATION);
                } else {
                    mpc.parts.add(ba);
                    if (mpc.parts.size() == mpc.expectedCount) {
                        multiparts.remove(pID);
                        BytesArray frame = mpc.toFrame();
                        synchronized (incomingFrames) {
                            incomingFrames.add(frame);
                            incomingFrames.notifyAll();
                        }
                    }
                }
            }
        }

        public void run() {
            while (running) {
                BytesArray frame = null;
                synchronized (incomingFrames) {
                    if (incomingFrames.size() == 0) {
                        try {
                            incomingFrames.wait(5000);
                        } catch (Exception err) {
                        }
                    }
                    if (incomingFrames.size() > 0) {
                        frame = incomingFrames.removeFirst();
                    }
                }

                if (frame != null) {
                    Packet f = (Packet) serializer.decode(frame);
                    if (frameListener != null) {
                        if (f.getSource().getIPv4Address() == 0 && f.getSource().getSubSystemID() == 9999) {
                            frameListener.processDestinationUnreachable(f);
                        } else if (f.getDestination().getIPv4Address() == 0
                                && f.getDestination().getSubSystemID() == ServiceNodeConnection.DESTINATION_BROADCAST) {
                            frameListener.processBroadcast(f);
                        } else if (f.getDestination().getIPv4Address() == 0
                                && f.getDestination().getSubSystemID() > ServiceNodeConnection.DESTINATION_BROADCAST) {
                            frameListener.processMulticast(f);
                        } else
                            frameListener.process(f);
                    } else {
                        if (f.getSource().getIPv4Address() == 0 && f.getSource().getSubSystemID() == 9999) {
                            System.out.println("Unreachable:" + f);
                        } else if (f.getDestination().getIPv4Address() == 0
                                && f.getDestination().getSubSystemID() == ServiceNodeConnection.DESTINATION_BROADCAST) {
                            System.out.println("Broadcast:" + f);
                        } else if (f.getDestination().getIPv4Address() == 0
                                && f.getDestination().getSubSystemID() > ServiceNodeConnection.DESTINATION_BROADCAST) {
                            System.out.println("Multicast:" + f);
                        } else
                            System.out.println("Regular:" + f);
                    }
                }
            }
        }
    }
}
