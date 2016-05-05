/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.tests;

import java.io.File;
import org.datasand.microservice.Message;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.microservice.cmap.CMap;
import org.datasand.network.HabitatID;
import org.datasand.network.habitat.HabitatsConnection;
import org.datasand.network.habitat.ServicesHabitat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class AgentsTest {

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testBroadcastToNodes() {
        ServicesHabitat nodes[] = new ServicesHabitat[10];
        for (int i = 0; i < 10; i++) {
            nodes[i] = new ServicesHabitat(null);
        }
        try {
            Thread.sleep(2000);
        } catch (Exception err) {
        }
        System.out.println("Ready");
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), HabitatsConnection.PROTOCOL_ID_BROADCAST);
        HabitatID unreach = new HabitatID(nodes[3].getLocalHost().getIPv4Address(), 56565, 0);
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), unreach);
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }

        int broascastCount = 0;
        int unreachableCount = 0;
        for (int i = 0; i < nodes.length; i++) {
            broascastCount+=nodes[i].getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachableCount+=nodes[i].getServicesHabitatMetrics().getUnreachableFrameCount();
            nodes[i].shutdown();
        }

        try {
            Assert.assertEquals(new Integer(11), new Integer(broascastCount));
            Assert.assertEquals(true, unreachableCount>0);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Test
    public void testBroadcastToAgent() {
        MicroServicesManager nodes[] = new MicroServicesManager[10];
        MicroService agent[] = new MicroService[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new MicroServicesManager();
            agent[i] = new TestAgent(nodes[i].getHabitat().getLocalHost(),nodes[i]);
        }

        try {
            Thread.sleep(2000);
        } catch (Exception err) {
        }

        System.out.println("Ready!");
        agent[4].send(new Message(0,createTestObject()), HabitatsConnection.PROTOCOL_ID_BROADCAST);

        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }

        int broascastCount = 0;
        int unreachableCount = 0;
        int regularCount = 0;
        for (int i = 0; i < nodes.length; i++) {
            broascastCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachableCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getUnreachableFrameCount();
            regularCount += nodes[i].getHabitat().getServicesHabitatMetrics().getRegularFrameCount();
            nodes[i].shutdown();
        }

        try {
            Assert.assertEquals(new Integer(11),new Integer(broascastCount));
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

    @Test
    public void testMulticast() {
        MicroServicesManager nodes[] = new MicroServicesManager[10];
        MicroService agent[] = new MicroService[nodes.length];
        // Arbitrary number greater than 10 and not equal to 9999 (which is the
        // destination unreachable code)
        int MULTICAST_GROUP = 27;
        HabitatID multiCast = new HabitatID(
                HabitatsConnection.PROTOCOL_ID_BROADCAST.getIPv4Address(),
                MULTICAST_GROUP, MULTICAST_GROUP);
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new MicroServicesManager();
            agent[i] = new TestAgent(nodes[i].getHabitat().getLocalHost(),
                    nodes[i]);
            // only 5 microservice are registered for this multicast
            if (i % 2 == 0) {
                nodes[i].registerForMulticast(MULTICAST_GROUP, agent[i]);
            }
        }

        System.out.println("Ready!");
        agent[2].send(new Message(0,createTestObject()), multiCast);

        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }

        int broascastCount = 0;
        int unreachableCount = 0;
        int regularCount = 0;
        int multicastCount = 0;
        for (int i = 0; i < nodes.length; i++) {
            broascastCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachableCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getUnreachableFrameCount();
            regularCount += nodes[i].getHabitat().getServicesHabitatMetrics().getRegularFrameCount();
            multicastCount += nodes[i].getHabitat().getServicesHabitatMetrics().getMulticastFrameCount();
            nodes[i].shutdown();
        }

        try {
            Assert.assertEquals(new Integer(11), new Integer(multicastCount));
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Test
    public void testUnicast() {
        MicroServicesManager nodes[] = new MicroServicesManager[10];
        MicroService agent[] = new MicroService[nodes.length];
        HabitatID destination = null;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new MicroServicesManager();
            agent[i] = new TestAgent(nodes[i].getHabitat().getLocalHost(),
                    nodes[i]);
            if (i == 7)
                destination = agent[i].getMicroServiceID();
        }

        System.out.println("Ready!");
        agent[2].send(new Message(0,createTestObject()), destination);

        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }

        int broascastCount = 0;
        int unreachableCount = 0;
        int regularCount = 0;
        int multicastCount = 0;
        for (int i = 0; i < nodes.length; i++) {
            broascastCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachableCount+=nodes[i].getHabitat().getServicesHabitatMetrics().getUnreachableFrameCount();
            regularCount += nodes[i].getHabitat().getServicesHabitatMetrics().getRegularFrameCount();
            multicastCount += nodes[i].getHabitat().getServicesHabitatMetrics().getMulticastFrameCount();
            nodes[i].shutdown();
        }

        try {
            Assert.assertEquals(new Integer(1), new Integer(regularCount));
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Test
    public void testCMapString() {
        MicroServicesManager m1 = new MicroServicesManager();
        MicroServicesManager m2 = new MicroServicesManager();
        CMap<String, String> map1 = new CMap<String,String>(125, m1,252,null);
        CMap<String, String> map2 = new CMap<String,String>(125, m2,252,null);

        map1.put("TestKey1", "Value1");
        map1.put("TestKey2", "Value2");
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Assert.assertEquals(map1.get("TestKey1"), map2.get("TestKey1"));
        Assert.assertEquals(map1.get("TestKey2"), map2.get("TestKey2"));
        map2.put("TestKey3", "Value3");
        map2.put("TestKey4", "Value4");
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Assert.assertEquals(map2.get("TestKey3"), map1.get("TestKey3"));
        Assert.assertEquals(map2.get("TestKey4"), map1.get("TestKey4"));
        m1.shutdown();
        m2.shutdown();
    }

    @Test
    public void testCMapTestObject() {
        MicroServicesManager m1 = new MicroServicesManager();
        MicroServicesManager m2 = new MicroServicesManager();
        CMap<String, TestObject> map1 = new CMap<String, TestObject>(125, m1,252,null);
        CMap<String, TestObject> map2 = new CMap<String, TestObject>(125, m2,252,null);

        map1.put("TestKey1", createTestObject());
        map1.put("TestKey2", createTestObject());
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Assert.assertEquals(map1.get("TestKey1"), map2.get("TestKey1"));
        Assert.assertEquals(map1.get("TestKey2"), map2.get("TestKey2"));
        map2.put("TestKey3", createTestObject());
        map2.put("TestKey4", createTestObject());
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Assert.assertEquals(map2.get("TestKey3"), map1.get("TestKey3"));
        Assert.assertEquals(map2.get("TestKey4"), map1.get("TestKey4"));
        m1.shutdown();
        m2.shutdown();
    }

    public static int countSubstring(String substr, String output) {
        System.out.println(output);
        int index = output.indexOf(substr);
        int count = 0;
        while (index != -1) {
            count++;
            index = output.indexOf(substr, index + 1);
        }
        return count;
    }

    public static TestObject createTestObject() {
        TestObject object = new TestObject();
        object.setName("Test me");
        object.setAddress("My Test address");
        object.setZipcode(95014);
        object.setSocial(55366354);
        return object;
    }

    @Test
    public void testUnreachable(){
        MicroServicesManager m1 = new MicroServicesManager();
        CMap<String, TestObject> map1 = new CMap<String, TestObject>(125, m1,252,null);

        map1.put("TestKey1", createTestObject());
        map1.put("TestKey2", createTestObject());

        MicroServicesManager m2 = new MicroServicesManager();
        CMap<String, TestObject> map2 = new CMap<String, TestObject>(125, m2,252,null);
        map2.put("TestKey7", createTestObject());
        map1.remove("TestKey1");

        MicroServicesManager m3 = new MicroServicesManager();
        CMap<String, TestObject> map3 = new CMap<String, TestObject>(125, m3,252,null);

        map1.put("TestKey1", createTestObject());
        map2.put("TestKey3", createTestObject());
        map3.put("TestKey4", createTestObject());

        System.out.println("Sleeping 5 seconds to allow map1,2 & 3 to sync");
        try {
            Thread.sleep(5000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Assert.assertEquals(map1.get("TestKey1"), map2.get("TestKey1"));
        Assert.assertEquals(map1.get("TestKey2"), map2.get("TestKey2"));
        Assert.assertEquals(map1.get("TestKey2"), map3.get("TestKey2"));
        Assert.assertEquals(map1.get("TestKey3"), map3.get("TestKey3"));
        Assert.assertEquals(map1.get("TestKey4"), map2.get("TestKey4"));

        MicroServicesManager m4 = new MicroServicesManager();
        CMap<String, TestObject> map4 = new CMap<String, TestObject>(125, m4,252,null);
        map4.put("TestKey11", createTestObject());

        try{Thread.sleep(2000);}catch(Exception err){}

        map2._ForTestOnly_pseudoSendEnabled = true;
        map1._ForTestOnly_pseudoSendEnabled = true;
        map3._ForTestOnly_pseudoSendEnabled = true;

        m4.shutdown(); //Simulate node down/unreachable during synchronization
        System.out.println("Sleeping 6 seconds to allow node 4 shutdown...");
        try{Thread.sleep(6000);}catch(Exception err){}

        map2.put("TestKey3", createTestObject());
        map3.put("TestKey3", createTestObject());
        map2.put("TestKey4", createTestObject());

        map1.put("TestKey5", createTestObject());
        map3.remove("TestKey3");
        map2.remove("TestKeTestKey4y4");

        boolean firstTime = true;

        map2._ForTestOnly_pseudoSendEnabled = false;
        map1._ForTestOnly_pseudoSendEnabled = false;
        map1.sendARPBroadcast();
        map2.sendARPBroadcast();
        map1.put("TestKey6", createTestObject());
        map3._ForTestOnly_pseudoSendEnabled = false;
        map3.sendARPBroadcast();
        map4.sendARPBroadcast();
        map2.sendARPBroadcast();

        m4 = new MicroServicesManager();
        map4 = new CMap<String, TestObject>(125, m4,252,null);

        try {
            System.out.println("Sleeping 5 seconds to allow node 4 to load and sync");
            Thread.sleep(5000);
        } catch (Exception err) {
            err.printStackTrace();
        }

        Assert.assertEquals(map2.get("TestKey3"), map1.get("TestKey3"));
        Assert.assertEquals(map2.get("TestKey4"), map1.get("TestKey4"));
        Assert.assertEquals(map2.get("TestKey4"), map3.get("TestKey4"));
        Assert.assertEquals(map2.get("TestKey6"), map3.get("TestKey6"));
        Assert.assertEquals(map1.get("TestKey6"), map2.get("TestKey6"));
        Assert.assertEquals(map4.get("TestKey6"), map2.get("TestKey6"));

        Assert.assertEquals(8, map1.size());
        Assert.assertEquals(8, map2.size());
        Assert.assertEquals(8, map3.size());
        Assert.assertEquals(8, map4.size());

        map2._ForTestOnly_pseudoSendEnabled = true;
        map1._ForTestOnly_pseudoSendEnabled = true;
        map3._ForTestOnly_pseudoSendEnabled = true;
        map4._ForTestOnly_pseudoSendEnabled = true; //Simulate node timeout during synchronization

        map2.put("TestKey8", createTestObject());

        map2._ForTestOnly_pseudoSendEnabled = false;
        map1._ForTestOnly_pseudoSendEnabled = false;
        map3._ForTestOnly_pseudoSendEnabled = false;
        map1.sendARPBroadcast();
        map2.sendARPBroadcast();
        map3.sendARPBroadcast();

        try {
            System.out.println("Sleeping 10 seconds to allow nodes to sync after timeout");
            Thread.sleep(10000);
        } catch (Exception err) {
            err.printStackTrace();
        }
        map4._ForTestOnly_pseudoSendEnabled = false;
        map4.sendARPBroadcast();
        map1.sendARPBroadcast();
        try {
            Thread.sleep(20000);
        } catch (Exception err) {
            err.printStackTrace();
        }

        Assert.assertEquals(map3.get("TestKey8"), map2.get("TestKey8"));
        Assert.assertEquals(map4.get("TestKey8"), map1.get("TestKey8"));

        System.out.println("Finish");
        m1.shutdown();
        m2.shutdown();
        m3.shutdown();
        m4.shutdown();
        //m4.shutdown();
    }

    /*
    @Test
    public void testClusterTypeDescriptors(){

        File node1 = new File("./node1");
        node1.mkdirs();
        MicroServicesManager m1 = new MicroServicesManager();
        CMap<String, TypeDescriptor> cm1 = new CMap<String, TypeDescriptor>(223, m1,255,new TypeDescriptorListener<String,TypeDescriptor>(container1));
        container1.setClusterMap(cm1);

        File node2 = new File("./node2");
        node2.mkdirs();
        TypeDescriptorsContainer container2 = new TypeDescriptorsContainer("./node2");
        MicroServicesManager m2 = new MicroServicesManager(container2);
        CMap<String, TypeDescriptor> cm2 = new CMap<String, TypeDescriptor>(223, m2,255,new TypeDescriptorListener<String,TypeDescriptor>(container2));
        container2.setClusterMap(cm2);


        TestObject to = createTestObject();
        TypeDescriptor td = container1.getTypeDescriptorByObject(to);
        try{Thread.sleep(5000);}catch(Exception err){}
        TypeDescriptor td2 = container2.checkTypeDescriptorByClass(TestObject.class);
        Assert.assertEquals(true, td2!=null);
        m1.shutdown();
        m2.shutdown();
    }*/

    @AfterClass
    public static void clean(){
        File f = new File("./node1");
        deleteDirectory(f);
        f = new File("./node2");
        deleteDirectory(f);
    }

    public static void deleteDirectory(File dir) {
        File files[] = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    deleteDirectory(file);
                else
                    file.delete();
            }
        }
        dir.delete();
    }
}
