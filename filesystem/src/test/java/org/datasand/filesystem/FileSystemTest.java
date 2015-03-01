package org.datasand.filesystem;

import org.datasand.agents.AutonomousAgentManager;
import org.datasand.codec.TypeDescriptorsContainer;

public class FileSystemTest {
	public static void main(String args[]){
		AutonomousAgentManager am = new AutonomousAgentManager(new TypeDescriptorsContainer("./AMTest1"));
		FileManagerAgent fa = new FileManagerAgent(am);
		fa.addRepository("./repo1");

		AutonomousAgentManager am1 = new AutonomousAgentManager(new TypeDescriptorsContainer("./AMTest2"));
		FileManagerAgent fa1 = new FileManagerAgent(am1);
		fa1.addRepository("./repo2");
	}
}
