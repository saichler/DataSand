package org.datasand.agents.tests;

import org.datasand.agents.AutonomousAgent;
import org.datasand.agents.AutonomousAgentManager;
import org.datasand.agents.Message;
import org.datasand.network.NetworkID;
import org.junit.Assert;

public class TestAgent extends AutonomousAgent {

    private TestObject testObject = AgentsTest.createTestObject();

    public TestAgent(NetworkID localHost, AutonomousAgentManager m) {
        super(19, m);
        TestObject o = new TestObject();
        m.getTypeDescriptorsContainer().getTypeDescriptorByObject(o);
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
