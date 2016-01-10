package org.datasand.filesystem;

import org.datasand.agents.AutonomousAgentManager;

public class FileSystemTest {
	public static void main(String args[]){
		AutonomousAgentManager am = new AutonomousAgentManager();
		FileManagerAgent fa = new FileManagerAgent(am);
		fa.addRepository("./repo1");

		AutonomousAgentManager am1 = new AutonomousAgentManager();
		FileManagerAgent fa1 = new FileManagerAgent(am1);
		fa1.addRepository("./repo2");
	}
}
