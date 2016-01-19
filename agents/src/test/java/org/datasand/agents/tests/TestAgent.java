/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.tests;

import org.datasand.agents.AutonomousAgent;
import org.datasand.agents.AutonomousAgentManager;
import org.datasand.agents.Message;
import org.datasand.network.NetworkID;
import org.junit.Assert;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class TestAgent extends AutonomousAgent {

    private TestObject testObject = AgentsTest.createTestObject();

    public TestAgent(NetworkID localHost, AutonomousAgentManager m) {
        super(19, m);
        TestObject o = new TestObject();
    }

    @Override
    public void processDestinationUnreachable(Message message,NetworkID unreachableSource) {
    }

    @Override
    public void processMessage(Message message, NetworkID source,NetworkID destination) {
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
