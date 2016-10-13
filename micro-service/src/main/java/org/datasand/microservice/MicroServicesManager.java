/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.util.ThreadNode;
import org.datasand.codec.util.ThreadPool;
import org.datasand.network.IFrameListener;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.habitat.ServicesHabitat;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class MicroServicesManager extends ThreadNode implements IFrameListener {

    private final ServicesHabitat habitat;
    private final Map<Integer, MicroService> id2MicroService = new HashMap<>();
    private final Map<String, Integer> handlerNameToID = new HashMap<>();
    private ThreadPool threadPool = new ThreadPool(20, "Handlers Threads", 2000);
    private final Object servicesSeynchronizeObject = new Object();
    private Map<Integer, Set<MicroService>> multicasts = new HashMap<Integer, Set<MicroService>>();
    private int nextMicroServiceID = 1000;
    private final ServiceInventory serviceInventory = new ServiceInventory();
    private long lastServiceInventoryBroadcast = 0;

    public MicroServicesManager() {
        this(false);
    }

    public MicroServicesManager(boolean unicastOnly) {
        super(null,"");
        this.habitat = new ServicesHabitat(this,unicastOnly);
        this.setName("Micro Service Manager - " + habitat.getName());
        this.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getNextMicroServiceID(){
        synchronized (this){
            try{
                nextMicroServiceID++;
            }finally {
                return nextMicroServiceID;
            }
        }
    }

    protected Object getSyncObject() {
        return this.servicesSeynchronizeObject;
    }

    public void initialize(){}
    public void distruct(){}

    public void execute() throws Exception {
        boolean addedTask = false;
        for (MicroService h : id2MicroService.values()) {
            if (!h.isBusy()) {
                h.checkForRepetitive();
                if (h.getQueueSize() > 0) {
                    h.pop();
                    threadPool.addTask(h);
                    addedTask = true;
                }
            }
        }
        if(System.currentTimeMillis()-lastServiceInventoryBroadcast>5000){
            lastServiceInventoryBroadcast = System.currentTimeMillis();
            BytesArray ba = new BytesArray(1024);
            serviceInventory.encode(serviceInventory,ba);
            this.getHabitat().send(ba.getData(),this.habitat.getNetUUID(), Packet.PROTOCOL_ID_BROADCAST);
        }
        if (!addedTask) {
            synchronized (servicesSeynchronizeObject) {
                try {
                    servicesSeynchronizeObject.wait(5000);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    public ServicesHabitat getHabitat() {
        return this.habitat;
    }

    public MicroService createHanlder(String className, ClassLoader cl) {
        return null;
    }

    public void addMicroService(MicroService h) {
        this.id2MicroService.put(h.getMicroServiceID().getMicroServiceID(), h);
    }

    public void registerMicroService(MicroService h) {
        id2MicroService.put(h.getMicroServiceID().getMicroServiceID(), h);
        handlerNameToID.put(h.getName(), h.getMicroServiceID().getMicroServiceID());
        serviceInventory.addService(h.getMicroServiceGroup().getB(),h.getMicroServiceID());
    }

    public MicroService getHandlerByID(int id) {
        return this.id2MicroService.get(id);
    }

    public MicroService getHandlerByName(String name) {
        Integer id = this.handlerNameToID.get(name);
        if (id != null) {
            return this.id2MicroService.get(id);
        }
        return null;
    }

    private int getMicroServiceIDFromFrame(Packet frame){
        return Encoder.decodeInt32(frame.getData(),Packet.PACKET_DATA_LOCATION+16);
    }

    @Override
    public void process(Packet frame) {
        int microServiceID = getMicroServiceIDFromFrame(frame);
        MicroService h = id2MicroService.get(microServiceID);
        if (h != null) {
            h.addFrame(frame);
            synchronized (servicesSeynchronizeObject) {
                servicesSeynchronizeObject.notifyAll();
            }
        }
    }

    public void messageWasEnqueued(){
        synchronized (servicesSeynchronizeObject) {
            servicesSeynchronizeObject.notifyAll();
        }
    }

    @Override
    public void processDestinationUnreachable(Packet frame) {
        int microServiceID = getMicroServiceIDFromFrame(frame);
        MicroService h = id2MicroService.get(microServiceID);
        if (h != null) {
            h.addFrame(frame);
            synchronized (servicesSeynchronizeObject) {
                servicesSeynchronizeObject.notifyAll();
            }
        }
    }

    @Override
    public void processBroadcast(Packet frame) {
        for (MicroService ms : habitatIDtoMicroService.values()) {
            ms.addFrame(frame);
        }
        synchronized (servicesSeynchronizeObject) {
            servicesSeynchronizeObject.notifyAll();
        }
    }

    @Override
    public void processMulticast(Packet frame) {
        long multicastGroupID = frame.getDestination().getB();
        Set<MicroService> handlers = this.multicasts.get(multicastGroupID);
        if (handlers != null) {
            for (MicroService h : handlers) {
                h.addFrame(frame);
                synchronized (servicesSeynchronizeObject) {
                    servicesSeynchronizeObject.notifyAll();
                }
            }
        }
    }

    private static final String getClassNameFromEntryName(String entryName) {
        String result = replaceAll(entryName, "/", ".");
        int index = result.lastIndexOf(".");
        return result.substring(0, index);
    }

    public List<NetUUID> installJar(String jarFileName) {

        List<NetUUID> result = new ArrayList<NetUUID>();

        File f = new File(jarFileName);
        if (f.exists()) {
            try {
                JarInputStream in = new JarInputStream(new FileInputStream(f));
                JarEntry e = (JarEntry) in.getNextEntry();
                while (e != null) {
                    if (e.getName().indexOf("Handler") != -1) {
                        String className = getClassNameFromEntryName(e.getName());
                        try {
                            @SuppressWarnings("deprecation")
                            URLClassLoader cl = new URLClassLoader(new URL[] { f.toURL() }, this.getClass().getClassLoader());
                            /*
                             * @TODO ClassLoaders
                             */
                            // ModelClassLoaders.getInstance().addClassLoader(cl);
                            Class<?> handlerClass = cl.loadClass(className);
                            MicroService newHandler = (MicroService) handlerClass.getConstructor(new Class[] {NetUUID.class,MicroServicesManager.class })
                                    .newInstance(
                                            new Object[] {
                                                    this.habitat
                                                            .getNetUUID(),
                                                    this });
                            registerMicroService(newHandler);
                            newHandler.start();
                            result.add(newHandler.getMicroServiceID());
                            cl.close();
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                    e = (JarEntry) in.getNextEntry();
                }
                in.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        return null;
    }

    public void registerForMulticast(int multicastGroup, MicroService h) {
        Set<MicroService> handlers = this.multicasts.get(multicastGroup);
        if (handlers == null) {
            handlers = new HashSet<MicroService>();
            this.multicasts.put(multicastGroup, handlers);
        }
        handlers.add(h);
    }

    public static String replaceAll(String src, String that, String withThis) {
        StringBuffer buff = new StringBuffer();
        int index0 = 0;
        int index1 = src.indexOf(that);
        if (index1 == -1)
            return src;
        while (index1 != -1) {
            buff.append(src.substring(index0, index1));
            buff.append(withThis);
            index0 = index1 + that.length();
            index1 = src.indexOf(that, index0);
        }
        buff.append(src.substring(index0));
        return buff.toString();
    }

    public void runSideTask(ISideTask task) {
        this.threadPool.addTask(task);
    }
}
