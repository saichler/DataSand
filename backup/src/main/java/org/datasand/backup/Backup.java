package org.datasand.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Backup extends Thread{

	private static long MAX_LOAD = 1024*1024*20;
	private boolean isrunning = true;
	private final String fromPath;
	private final String toPath;
	private final TaskListener listener;

	public static interface TaskListener {
		public void currentFile(File f);
		public void status(String str);
		public void setCopySize(long size);
	}

	public Backup(String fromPath, String toPath, TaskListener l){
		super("Backup Thread");
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.listener = l;
		this.start();
	}

	public void run(){
		try {
			doBackup(new File(this.fromPath), new File(this.toPath));
			this.listener.status("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFileSizeInMB(long size){
		long result = (long)size/1024/1024;
		return ""+result+"m";
	}

	public void doBackup(File from,File to) throws IOException{
		if(!to.exists()){
			this.listener.status("Creating destination dir "+to.getAbsolutePath());
			to.mkdirs();
		}else {
			this.listener.status("Scanning " + to.getAbsolutePath());
		}

		File fromFiles[] = from.listFiles();
		for(File file:fromFiles){
			if(!isrunning){
				return;
			}

			if(file.isDirectory()){
				File destDir = new File(to.getPath()+"/"+file.getName());
				doBackup(file, destDir);
			}else{
				File dest = new File(to.getPath()+"/"+file.getName());
				if(!dest.exists()){
					copyFile(file, dest);
				}else{
					if(file.length()!=dest.length()){
						listener.status("File Size Error "+dest.getAbsolutePath());
						copyFile(file, dest);
					}
				}
			}
		}
	}

	

	public void copyFile(File file,File dest) throws IOException{

		long fileSize = file.length();

		this.listener.currentFile(file);
		this.listener.status("Copying File "+file.getAbsolutePath()+getFileSizeInMB(fileSize)+".");

		int pieces = (int)(fileSize/MAX_LOAD);
		long lastPiece = fileSize%MAX_LOAD;

		FileInputStream source = new FileInputStream(file);
		FileOutputStream target = new FileOutputStream(dest);

		for(int i=0;i<pieces;i++){
			this.listener.setCopySize(i*20);
			byte data[] = new byte[(int)MAX_LOAD];
			source.read(data);
			target.write(data);
		}

		byte lastData[] = new byte[(int)lastPiece];
		source.read(lastData);
		target.write(lastData);
		source.close();
		target.close();
	}

}
