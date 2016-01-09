package org.datasand.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import org.datasand.codec.BytesArray;
import org.datasand.network.Packet;

public class FileData {
	
	public static int MAX_PART_SIZE = Packet.MAX_DATA_IN_ONE_PACKET-10;
	private int part = -1;
	private int total = -1;
	private File file = null;
	private byte[] data = null;
	
	public FileData(){
	}
	
	public FileData(File f){
		this.file = f;
		this.total = (int)this.file.length()/MAX_PART_SIZE;
		if(this.total==0){
			this.total = 1;
		}else
		if(this.file.length()%MAX_PART_SIZE>0){
			total++;
		}
	}
	
	public boolean isFinished(){
		if(total==part+1)
			return true;
		return false;
	}
	
	public int getPart() {
		return part;
	}

	public int getTotal() {
		return total;
	}

	public File getFile() {
		return file;
	}

	public byte[] getData(){
		return this.data;
	}
	
	public static void encode(Object value, BytesArray ba) {
		FileData fileData = (FileData)value;
		
		if(fileData.part==fileData.total) return;
		
		if(fileData.part==-1){
			String filename = fileData.file.getPath();
			ba.getEncoder().encodeInt32(fileData.part, ba);
			ba.getEncoder().encodeInt32(fileData.total, ba);
			ba.getEncoder().encodeInt16(1, ba);
			ba.getEncoder().encodeString(filename, ba);
		}else
		if(fileData.total==1){
			ba.getEncoder().encodeInt32(fileData.part, ba);
			ba.getEncoder().encodeInt32(fileData.total, ba);
			ba.getEncoder().encodeInt16(1, ba);
			try{
				FileInputStream in = new FileInputStream(fileData.file);
				byte[] data = new byte[(int)fileData.file.length()];
				in.read(data);
				in.close();
				ba.getEncoder().encodeByteArray(data, ba);
			}catch(Exception err){
				err.printStackTrace();
			}
		}else		
		if(fileData.part<fileData.total-1){
			ba.getEncoder().encodeInt32(fileData.part, ba);
			ba.getEncoder().encodeInt32(fileData.total, ba);
			ba.getEncoder().encodeInt16(1, ba);
			try{
				byte data[] = new byte[MAX_PART_SIZE];
				RandomAccessFile r = new RandomAccessFile(fileData.file, "r");
				r.seek(fileData.part*MAX_PART_SIZE);
				r.read(data);
				r.close();
				ba.getEncoder().encodeByteArray(data, ba);
			}catch(Exception err){
				err.printStackTrace();
			}
		}else{
			ba.getEncoder().encodeInt32(fileData.part, ba);
			ba.getEncoder().encodeInt32(fileData.total, ba);
			ba.getEncoder().encodeInt16(1, ba);
			try{
				byte data[] = new byte[(int)(fileData.file.length()-fileData.part*MAX_PART_SIZE)];
				RandomAccessFile r = new RandomAccessFile(fileData.file, "r");
				r.seek(fileData.part*MAX_PART_SIZE);
				r.read(data);
				r.close();
				ba.getEncoder().encodeByteArray(data, ba);				
			}catch(Exception err){
				err.printStackTrace();
			}
		}
		fileData.part++;
	}

	public static Object decode(BytesArray ba, int length) {
		FileData fd = new FileData();
		fd.part = ba.getEncoder().decodeInt32(ba);
		fd.total = ba.getEncoder().decodeInt32(ba);
		ba.getEncoder().decodeInt16(ba);		
		if(fd.part==-1l){
			String filename = ba.getEncoder().decodeString(ba);
			fd.file = new File(filename);
		}else{
			fd.data = ba.getEncoder().decodeByteArray(ba);
		}
		return fd;
	}
}