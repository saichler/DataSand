package org.datasand.filesystem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.datasand.microservice.AutonomousAgent;
import org.datasand.microservice.AutonomousAgentManager;
import org.datasand.microservice.Message;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.ServiceID;

public class FileManagerAgent extends AutonomousAgent{
	
	public static final int FILE_MANAGER_MULTICAST_GROUP = 616;
	public static final int TYPE_REPO_MANIFEST = 1;
	public static final int TYPE_FILE_DATA = 2;

	static {
		Encoder.registerSerializer(FileData.class, new FileDataSerializer());
		Encoder.registerSerializer(FileRepositoryManifest.class, new FileRepositoryManifestSerializer());
	}

	private Map<String,FileRepositoryManifest> repositories = new HashMap<String, FileRepositoryManifest>();
	private Message helloMSG = new Message(); 
	
	public FileManagerAgent(AutonomousAgentManager manager){
		super(FILE_MANAGER_MULTICAST_GROUP,manager);
		this.setARPGroup(FILE_MANAGER_MULTICAST_GROUP);		
		this.registerRepetitiveMessage(10000, -1, 2, helloMSG);
	}

	@Override
	public void processDestinationUnreachable(Message message,ServiceID unreachableSource) {
	}

	@Override
	public void processMessage(Message message, ServiceID source, ServiceID destination) {
		if(message==helloMSG){
			publishRepositorys();
			return;
		}
		if(source.equals(this.getAgentID())) return;
		switch(message.getMessageType()){
			case TYPE_REPO_MANIFEST:
				processPeerRepository(source, (FileRepositoryManifest)message.getMessageData());
				break;
		}
	}

	public void processPeerRepository(ServiceID source, FileRepositoryManifest frm){
		String repoName = frm.getDirectory();
		if(repoName.equals("./repo1"))
			repoName = "./repo2";
		else
			repoName = "./repo1";
		if(!this.repositories.isEmpty()){
			FileRepositoryManifest fd = repositories.get(repoName);
			fd.compareToOther(frm);
		}
	}
	
	public void addRepository(String repositoryDir){
		FileRepositoryManifest repo = new FileRepositoryManifest(repositoryDir);
		this.repositories.put(repositoryDir, repo);
	}

	public void publishRepositorys(){
		for(FileRepositoryManifest rmf:this.repositories.values()){
			rmf.collectFiles();
			sendARP(new Message(TYPE_REPO_MANIFEST,rmf));
		}
	}
	
	private File currentFile = null;
	
	public void sendFile(String fileName,ServiceID dest){
		File f = new File(fileName);
		if(f.exists()){
			FileData fileData = new FileData(f);
			Message fileDataMsg = new Message(TYPE_FILE_DATA,fileData);
			this.send(fileDataMsg, dest);

			BytesArray ba = new BytesArray(1024);
			while(ba.getLocation()>2){
				send(ba.getData(), dest);
				ba.resetLocation();
				Encoder.encodeObject(fileData, ba);
			}
		}
	}

	public void start(){
	}

	/*
	@Override
	public void processNext(Packet frame, Object obj) {
		if(obj==repositories){
			multicastRepositories();
		}else
		if(obj instanceof FileData){
			FileData fileData = (FileData)obj;
			if(fileData.getPart()==-1){
				currentFile = new File(fileData.getFile().getPath()+".new");
				if(!currentFile.exists()){
					try{
						currentFile.createNewFile();
					}catch(Exception err){
						err.printStackTrace();
					}
				}
			}else{
				try{
					RandomAccessFile r = new RandomAccessFile(currentFile, "rw");
					r.seek(fileData.getPart()*FileData.MAX_PART_SIZE);
					r.write(fileData.getData(),0, fileData.getData().length);
					r.close();
				}catch(Exception err){
					err.printStackTrace();
				}
				if(fileData.getPart()==fileData.getTotal()-1){
					currentFile = null;
				}
			}
		}
	}*/

	@Override
	public String getName() {
		return "FileSystem";
	}
}
