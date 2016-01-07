package org.datasand.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.datasand.codec.bytearray.BytesArray;

public class FileRepositoryManifest {
	
	private String directory = null;
	private Set<String> fileExtentions = new HashSet<>();
	private Map<String,FileDescription> filesInRepository = new HashMap<>();
	
	public FileRepositoryManifest(String _directory){
		this.directory = _directory;
		File dir = new File(_directory);
		if(dir.exists()){
			collectFiles(dir);
		}
	}

	private FileRepositoryManifest(){
	}
	
	public void compareToOther(FileRepositoryManifest other){
		for(FileDescription fd:other.filesInRepository.values()){
			System.out.println(fd.name);
		}
	}
	
	public String getDirectory(){
		return this.directory;
	}
	
	private void addFileDescription(File f){
		if(filesInRepository.containsKey(f.getPath()))
			return;
		
		if(!fileExtentions.isEmpty() && !fileExtentions.contains("*")){
			int i = f.getName().lastIndexOf(".");
			if(i==-1)
				return;
			String ext = f.getName().substring(i+1).trim();
			if(!fileExtentions.contains(ext)){
				return;
			}
		}
		
		FileDescription fd = new FileDescription();
		fd.name = f.getPath();
		fd.lastChanged = f.lastModified();
		fd.length = f.length();
		fd.version = 0;
		long md5[] = getFileMD5(f);
		fd.md5A = md5[0];
		fd.md5B = md5[1];
		filesInRepository.put(fd.name, fd);
	}

	public void collectFiles(){
		collectFiles(new File(this.directory));
	}

	private void collectFiles(File dir){
		File files[] = dir.listFiles();
		if(files!=null){
			for(File f:files){
				if(f.isDirectory()){
					collectFiles(f);
				}else{
					addFileDescription(f);
				}
			}
		}
	}
		
	private static class FileDescription {
		private String name = null;
		private long lastChanged = -1;
		private long length = -1;
		private int version = -1;
		private long md5A = -1;
		private long md5B = -1;		
	}
	
	public static void encode(Object obj,BytesArray ba){
		FileRepositoryManifest rm = (FileRepositoryManifest)obj;
		ba.getEncoder().encodeString(rm.directory, ba);
		ba.getEncoder().encodeSize(rm.fileExtentions.size(),ba);
		for(String ext:rm.fileExtentions){
			ba.getEncoder().encodeString(ext, ba);
		}
		ba.getEncoder().encodeSize(rm.filesInRepository.size(), ba);
		for(FileDescription fd:rm.filesInRepository.values()){
			encodeFileDesc(fd, ba);
		}
	}
	
	public static Object decode(BytesArray ba){
		FileRepositoryManifest rm = new FileRepositoryManifest();
		rm.directory = ba.getEncoder().decodeString(ba);
		int size = ba.getEncoder().decodeSize(ba);
		for(int i=0;i<size;i++){
			rm.fileExtentions.add(ba.getEncoder().decodeString(ba));
		}
		size = ba.getEncoder().decodeSize(ba);
		for(int i=0;i<size;i++){
			FileDescription fd = (FileDescription)decodeFileDesc(ba);
			rm.filesInRepository.put(fd.name, fd);
		}
		return rm;
	}
	
	public static void encodeFileDesc(Object obj,BytesArray ba){
		FileDescription fd = (FileDescription)obj;
		ba.getEncoder().encodeString(fd.name, ba);
		ba.getEncoder().encodeInt64(fd.lastChanged, ba);
		ba.getEncoder().encodeInt64(fd.length, ba);
		ba.getEncoder().encodeInt16(fd.version, ba);
		ba.getEncoder().encodeInt64(fd.md5A, ba);
		ba.getEncoder().encodeInt64(fd.md5B, ba);
	}

	public static FileDescription decodeFileDesc(BytesArray ba){
		FileDescription fd = new FileDescription();
		fd.name = ba.getEncoder().decodeString(ba);
		fd.lastChanged = ba.getEncoder().decodeInt64(ba);
		fd.length = ba.getEncoder().decodeInt64(ba);
		fd.version = ba.getEncoder().decodeInt16(ba);
		fd.md5A = ba.getEncoder().decodeInt64(ba);
		fd.md5B = ba.getEncoder().decodeInt64(ba);
		return fd;
	}
	
   public static long[] getFileMD5(File f){
        try{
            if(f.exists()){
                FileInputStream fis =  new FileInputStream(f);
                byte[] buffer = new byte[1024*1024];
                MessageDigest complete = MessageDigest.getInstance("MD5");
                int numRead;
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        complete.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);
                fis.close();                
                byte by[] = complete.digest();
                long a = 0;
        	    a = (a << 8) + (by[0] & 0xff);		
        	    a = (a << 8) + (by[1] & 0xff);
        	    a = (a << 8) + (by[2] & 0xff);
        	    a = (a << 8) + (by[3] & 0xff);
        	    a = (a << 8) + (by[4] & 0xff);
        	    a = (a << 8) + (by[5] & 0xff);
        	    a = (a << 8) + (by[6] & 0xff);
        	    a = (a << 8) + (by[7] & 0xff);	    
        	    
        		long b = 0;
        		b = (b << 8) + (by[8] & 0xff);
        		b = (b << 8) + (by[9] & 0xff);
        		b = (b << 8) + (by[10] & 0xff);
        		b = (b << 8) + (by[11] & 0xff);
        		b = (b << 8) + (by[12] & 0xff);
        		b = (b << 8) + (by[13] & 0xff);
        		b = (b << 8) + (by[14] & 0xff);
        		b = (b << 8) + (by[15] & 0xff);				
        		return new long[]{a,b};
            }    
        }catch(Exception err){
            err.printStackTrace();
        }
        return null;
    }	
}
