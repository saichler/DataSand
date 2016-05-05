package org.datasand.filesystem;

import org.datasand.microservice.MicroServicesManager;

public class FileSystemTest {
	public static void main(String args[]){
		MicroServicesManager am = new MicroServicesManager();
		FileManagerAgent fa = new FileManagerAgent(am);
		fa.addRepository("./repo1");

		MicroServicesManager am1 = new MicroServicesManager();
		FileManagerAgent fa1 = new FileManagerAgent(am1);
		fa1.addRepository("./repo2");
	}
}
