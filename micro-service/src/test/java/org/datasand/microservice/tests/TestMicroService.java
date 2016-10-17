/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.tests;

import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.microservice.Message;
import org.datasand.network.NID;
import org.junit.Assert;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class TestMicroService extends MicroService {

    private TestObject testObject = MicroServiceTest.createTestObject();

    public TestMicroService(MicroServicesManager m) {
        super("TestSrvs", m);
        TestObject o = new TestObject();
    }

    @Override
    public void processDestinationUnreachable(Message message,NID unreachableSource) {
    }

    @Override
    public void processMessage(Message message, NID source, NID destination) {
        if (message == null) {
            System.out.println("Received a currapted frame");
        } else {
            System.out.println("Recieved Object, comparing...");
            Assert.assertEquals(testObject, ((TestObject)message.getMessageData()));
        }
    }

    @Override
    public void start() {
    }

    @Override
    public String getName() {
        return "Test Agent";
    }

}
