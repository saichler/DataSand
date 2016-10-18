package org.datasand.filesystem;

import java.io.File;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.microservice.Message;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.network.NID;

public class FileManagerAgent extends MicroService {

    public static final String FILE_MANAGER_MULTICAST_GROUP = "FileMngr";
    public static final int TYPE_REPO_MANIFEST = 1;
    public static final int TYPE_FILE_DATA = 2;
    public static final int TYPE_HELLO = 3;
    public static final int REQUEST_FILE = 4;

    static {
        Encoder.registerSerializer(FileData.class, new FileDataSerializer());
        Encoder.registerSerializer(FileRepositoryManifest.class, new FileRepositoryManifestSerializer());
    }

    private final Map<String, FileRepositoryManifest> repositories = new HashMap<String, FileRepositoryManifest>();
    private final Message helloMSG = new Message(TYPE_HELLO, null);

    public FileManagerAgent(MicroServicesManager manager) {
        super(FILE_MANAGER_MULTICAST_GROUP, manager);
        this.registerRepetitiveMessage(10000, -1, 2, helloMSG);
    }

    @Override
    public void processDestinationUnreachable(Message message, NID unreachableSource) {
    }

    @Override
    public void processMessage(Message message, NID source, NID destination) {
        if (message == helloMSG) {
            publishRepositorys();
            return;
        }
        if (source.equals(this.getMicroServiceID())) return;
        switch (message.getMessageType()) {
            case TYPE_REPO_MANIFEST:
                processPeerRepository(source, (FileRepositoryManifest) message.getMessageData());
                break;
            case REQUEST_FILE:
                FileDescription fd = (FileDescription)message.getMessageData();
                sendFile(fd.getName(),source);
        }
    }

    public void processPeerRepository(NID source, FileRepositoryManifest frm) {
        String repoName = frm.getName();
        if (!this.repositories.isEmpty()) {
            FileRepositoryManifest fd = repositories.get(repoName);
            if(fd!=null) {
                List<FileDescription> missing = fd.compareToOther(frm);
                for(FileDescription f:missing){
                    Message m = new Message(REQUEST_FILE,f);
                    send(m,source);
                }
            }
        }
    }

    public void addRepository(String name,String repositoryDir) {
        FileRepositoryManifest repo = new FileRepositoryManifest(name,repositoryDir);
        this.repositories.put(name, repo);
    }

    public void publishRepositorys() {
        for (FileRepositoryManifest rmf : this.repositories.values()) {
            rmf.collectFiles();
            multicast(new Message(TYPE_REPO_MANIFEST, rmf));
        }
    }

    private File currentFile = null;

    public void sendFile(String fileName, NID dest) {
        File f = new File(fileName);
        if (f.exists()) {
            FileData fileData = new FileData(f);
            Message fileDataMsg = new Message(TYPE_FILE_DATA, fileData);
            this.send(fileDataMsg, dest);

            while (!fileData.isFinished()) {
                send(fileDataMsg, dest);
            }
        }
    }

    public void start() {
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
