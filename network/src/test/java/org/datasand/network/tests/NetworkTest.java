package org.datasand.network.tests;

import java.io.File;

import org.datasand.network.HabitatID;
import org.datasand.network.habitat.HabitatsConnection;
import org.datasand.network.habitat.ServicesHabitat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NetworkTest {

    @Before
    public void before() {
    }

    @After
    public void after() {
        System.out.println("Sleeping for 5 seconds to allow proper nodes shutdown");
        try {
            Thread.sleep(5000);
        } catch (Exception err) {
        }
    }

    @Test
    public void testBroadcastToNodes() throws InterruptedException {
        ServicesHabitat nodes[] = new ServicesHabitat[10];
        nodes[0] = new ServicesHabitat(null);
        Thread.sleep(2000);
        for (int i = 1; i < 10; i++) {
            nodes[i] = new ServicesHabitat(null);
        }

        System.out.println("Sleeping for 10 seconds");
        Thread.sleep(5000);
        System.out.println("Ready");
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), HabitatsConnection.PROTOCOL_ID_BROADCAST);
        HabitatID unreach = new HabitatID(nodes[3].getLocalHost().getIPv4Address(), 56565, 0);
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), unreach);
        try {
            Thread.sleep(5000);
        } catch (Exception err) {
        }
        int brCount = 0;
        int unreachCount=0;
        for (int i = 0; i < nodes.length; i++) {
            brCount+=nodes[i].getServicesHabitatMetrics().getBroadcastFrameCount();
            unreachCount+=nodes[i].getServicesHabitatMetrics().getUnreachableFrameCount();
            nodes[i].shutdown();
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
        HabitatID unreach = new HabitatID(nodes[3].getLocalHost().getIPv4Address(), 56565, 0);
        nodes[3].send(new byte[5], nodes[3].getLocalHost(), unreach);
        HabitatID reachSingle = new HabitatID(nodes[3].getLocalHost().getIPv4Address(), 50010, 0);
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
