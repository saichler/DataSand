package org.datasand.network.tests;

import java.io.File;
import java.util.UUID;
import org.datasand.network.NetUUID;
import org.datasand.network.Packet;
import org.datasand.network.habitat.ServicesHabitat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NetworkTest {

    private ServicesHabitat nodes[] = null;

    @Before
    public void before() {
        nodes = new ServicesHabitat[10];
        boolean allStarted = false;
        while(!allStarted){
            allStarted=true;
            for (int i = 0; i < 10; i++) {
                if (nodes[i] == null) {
                    nodes[i] = new ServicesHabitat(null);
                }
                if(!nodes[i].isRunning()){
                    allStarted = false;
                }
            }
        }
    }

    @After
    public void after() {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].shutdown();
        }
    }

    @Test
    public void testBroadcastToNodes() throws InterruptedException {

        nodes[3].send(new byte[5], nodes[3].getNetUUID(), Packet.PROTOCOL_ID_BROADCAST);
        UUID r = UUID.randomUUID();
        NetUUID unreach = new NetUUID(0,r.getMostSignificantBits(),r.getLeastSignificantBits(),0);
        nodes[3].send(new byte[5], nodes[3].getNetUUID(), unreach);
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }
        int brCount = 0;
        int unreachCount=0;
        for (int i = 0; i < nodes.length; i++) {
            brCount+=nodes[i].getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachCount+=nodes[i].getServicesHabitatMetrics().getUnreachableFrameCount();
        }

        try {
            Assert.assertEquals("11", ""+brCount);
            Assert.assertEquals(true, unreachCount>0);
        } catch (Exception err) {
            err.printStackTrace();
        }

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
/*
    public static void main(String args[]){
        ServicesHabitat nodes[] = new ServicesHabitat[11];
        for (int i = 0; i < 10; i++) {
            nodes[i] = new ServicesHabitat(null);
        }
        nodes[10] = new ServicesHabitat(null, true);
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }
        System.out.println("Ready");
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), HabitatsConnection.PROTOCOL_ID_BROADCAST);
        NetUUID unreach = new NetUUID(nodes[3].getLocalHost().getIPv4Address(), 56565, 0);
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), unreach);
        NetUUID reachSingle = new NetUUID(nodes[3].getLocalHost().getIPv4Address(), 50010, 0);
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), reachSingle);
        nodes[10].send(new byte[5], nodes[10].getLocalHost(), HabitatsConnection.PROTOCOL_ID_BROADCAST);
        try {
            Thread.sleep(1000);
        } catch (Exception err) {
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].shutdown();
        }
    }*/
}
