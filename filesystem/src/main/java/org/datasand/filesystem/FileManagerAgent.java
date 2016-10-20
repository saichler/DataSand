package org.datasand.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.Encoder;
import org.datasand.microservice.Message;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.network.NID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManagerAgent extends MicroService {

    private static final Logger LOG = LoggerFactory.getLogger(FileManagerAgent.class);
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
    private final Map<String,Long> requestedFiles = new HashMap<>();

    public FileManagerAgent(MicroServicesManager manager) {
        super(FILE_MANAGER_MULTICAST_GROUP, manager);
        this.registerRepetitiveMessage(10000, -1, 2, helloMSG);
    }

    @Override
    public void processDestinationUnreachable(Message message, NID unreachableSource) {
    }

    @Override
    public void processMessage(Message message, NID source, NID destination) {
        try {
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
                    FileDescription fd = (FileDescription) message.getMessageData();
                    LOG.info("Requesting File "+fd.getRelativePath());
                    sendFile(fd, source);
                    break;
                case TYPE_FILE_DATA:
                    processFileData(message);
            }
        }catch(Exception e){
            LOG.error("File System failed to process message",e);
        }
    }

    public void processPeerRepository(NID source, FileRepositoryManifest frm) {
        String repoName = frm.getName();
        if (!this.repositories.isEmpty()) {
            FileRepositoryManifest fd = repositories.get(repoName);
            if(fd!=null) {
                List<FileDescription> missing = fd.compareToOther(frm);
                for(FileDescription f:missing){
                    if(!this.requestedFiles.containsKey(f.getRelativePath())) {
                        this.requestedFiles.put(f.getRelativePath(),System.currentTimeMillis());
                        Message m = new Message(REQUEST_FILE, f);
                        send(m, source);
                    }
                }
            }
        }
    }

    public void addRepository(String name,String repositoryDir) {
        FileRepositoryManifest repo = new FileRepositoryManifest(name,repositoryDir);
        this.repositories.put(name, repo);
    }

    public FileRepositoryManifest getRepository(String name){
        return this.repositories.get(name);
    }

    public void publishRepositorys() {
        for (FileRepositoryManifest rmf : this.repositories.values()) {
            rmf.collectFiles();
            multicast(new Message(TYPE_REPO_MANIFEST, rmf));
        }
    }

    public void sendFile(FileDescription fileDescription, NID dest) {
        String repoDirectory = repositories.get(fileDescription.getRepoName()).getDirectory();
        File f = new File(repoDirectory+fileDescription.getRelativePath());
        if (f.exists()) {
            FileData fileData = new FileData(f,fileDescription.getRepoName(),repoDirectory);
            Message fileDataMsg = new Message(TYPE_FILE_DATA, fileData);
            this.send(fileDataMsg, dest);
            fileData.setPart(0);
            while (!fileData.isFinishedSending()) {
                LOG.info("Sending part "+fileData.getPart()+" out of "+fileData.getTotal());
                send(fileDataMsg, dest);
                fileData.setPart(fileData.getPart()+1);
                try{Thread.sleep(150);}catch(Exception e){}
            }
        }
    }

    public void start() {
    }


	public void processFileData(Message msg) throws IOException {
        FileData fileData = (FileData)msg.getMessageData();
        if(fileData.getPart()==-1){
            String repoDir = this.repositories.get(fileData.getRepoName()).getDirectory();
            String filename = repoDir+fileData.getRelativePath();
            File currentFile = new File(filename);
            if(!currentFile.exists()){
                if(!currentFile.getParentFile().exists()){
                    currentFile.getParentFile().mkdirs();
                }
                currentFile.createNewFile();
            }
        }else{
            String repoDir = this.repositories.get(fileData.getRepoName()).getDirectory();
            String filename = repoDir+fileData.getRelativePath();
            File currentFile = new File(filename);
            RandomAccessFile r = new RandomAccessFile(currentFile, "rw");
            r.seek(fileData.getPart()*FileData.MAX_PART_SIZE);
            r.write(fileData.getData(),0, fileData.getData().length);
            r.close();
            if(fileData.isFinishedRecieving()){
                FileRepositoryManifest frm = this.repositories.get(fileData.getRepoName());
                frm.addFileDescription(currentFile);
                this.requestedFiles.remove(fileData.getRelativePath());
            }
        }
	}

    @Override
    public String getName() {
        return "FileSystem";
    }
}
