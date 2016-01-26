/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.VTable;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/12/16.
 */
public class DataFile {
    private RandomAccessFile raf;
    private int currentIndex = 0;
    private int currentLocation = 0;
    private final VTable vTable;
    private final File file;
    private static final int BUFFER_SIZE = 1024*1024*10;
    private static final byte[] DELETE_MARK = new byte[]{'-','D','D','-'};

    private final Map<Integer,DataLocation> mainIndex = new HashMap<>();
    private final Map<Integer,Integer> parentToChildrenIndex = new HashMap<>();
    private final Map<DataKey,Integer> dataKeyToIndex = new HashMap<>();

    private BytesArray writeBuffer = new BytesArray(BUFFER_SIZE);
    private int writeBufferLocation = 0;
    private BytesArray readBuffer = null;
    private int readBufferLocation = 0;

    public DataFile(File file,VTable vTable) throws IOException {
        this.vTable = vTable;
        this.file = file;
        if(!this.file.getParentFile().exists()){
            this.file.getParentFile().mkdirs();
        }
        loadIndex();
        raf = new RandomAccessFile(this.file,"rw");
        if(raf.length()==0){
            byte data[] = new byte[BUFFER_SIZE];
            raf.write(data);
            raf.seek(0);
        }
    }

    public void close(boolean commit) throws IOException{
        if(commit) {
            commit();
        }
        raf.close();
    }

    public void commit() throws IOException{
        raf.seek(writeBufferLocation);
        raf.write(writeBuffer.getBytes(),0,writeBuffer.getLocation());
        writeBufferLocation +=writeBuffer.getLocation();
        saveIndex();
        writeBuffer = new BytesArray(BUFFER_SIZE);
    }

    private void loadIndex() throws  IOException {
        File indexFile = new File(file.getAbsolutePath()+".index");
        if(indexFile.exists() && indexFile.length()>0) {
            FileInputStream in = new FileInputStream(indexFile);
            byte data[] = new byte[(int) indexFile.length()];
            in.read(data);
            in.close();
            BytesArray ba = new BytesArray(data);
            int size = Encoder.decodeInt32(ba);
            boolean isClean = currentIndex == 0;
            for (int i = 0; i < size; i++) {
                DataLocation dl = DataLocation.decode(ba);
                mainIndex.put(dl.getRecordIndex(), dl);
                if(dl.getParentIndex()!=-1) {
                    parentToChildrenIndex.put(dl.getParentIndex(), dl.getRecordIndex());
                }
                if (isClean) {
                    if (currentIndex <= dl.getRecordIndex()) {
                        currentIndex = dl.getRecordIndex() + 1;
                    }
                }
            }
            currentLocation = (int) file.length();
            writeBufferLocation = currentLocation;
        }
    }

    private void saveIndex() throws IOException{
        File indexFile = new File(file.getAbsolutePath()+".index");
        BytesArray ba = new BytesArray(1024);
        Encoder.encodeInt32(mainIndex.size(),ba);
        for(DataLocation dl:mainIndex.values()){
            dl.encode(ba);
        }
        FileOutputStream out = new FileOutputStream(indexFile);
        out.write(ba.getBytes(),0,ba.getLocation());
        out.close();
    }

    public int write(DataKey dataKey, HierarchyBytesArray obj,int parentIndex) throws IOException {
        byte data[] = obj.getData();
        DataLocation dl = null;
        if(dataKey!=null){
            Integer index = dataKeyToIndex.get(dataKey);
            if(index!=null) {
                dl = mainIndex.get(index);
            }
        }else
        if(parentIndex!=-1){
            Integer childIndex = this.parentToChildrenIndex.get(parentIndex);
            if(childIndex!=null){
                dl = mainIndex.get(childIndex);
            }
        }

        //New Record
        if(dl==null) {
            dl = new DataLocation(currentLocation, data.length, currentIndex, parentIndex);
            writeBuffer.insert(data);
            if (writeBuffer.getBytes().length > BUFFER_SIZE) {
                commit();
            }
            return updateIndexes(dl,data.length,parentIndex,dataKey);
        }else{
            if(dl.getLength()>=data.length){
                if(dl.getStartPosition()>= writeBufferLocation){
                    int loc = dl.getStartPosition()- writeBufferLocation;
                    System.arraycopy(data,0,writeBuffer.getBytes(),loc,data.length);
                }else{
                    raf.seek(dl.getStartPosition());
                    raf.write(data);
                }
                return dl.getRecordIndex();
            } else {
                dataKeyToIndex.remove(dataKey);
                return write(dataKey,obj,parentIndex);
            }
        }
    }

    private int updateIndexes(DataLocation dl,int dataLength,int parentIndex,DataKey dataKey){
        mainIndex.put(currentIndex,dl);
        if(parentIndex!=-1) {
            this.parentToChildrenIndex.put(parentIndex,currentIndex);
        }
        int myIndex = currentIndex;
        currentIndex++;
        currentLocation+=dataLength;
        if(dataKey!=null){
            dataKeyToIndex.put(dataKey,myIndex);
        }
        return myIndex;
    }

    public HierarchyBytesArray readByKey(DataKey key) throws IOException {
        Integer index = dataKeyToIndex.get(key);
        if(index!=null){
            return readByIndex(index);
        }
        return null;
    }

    public boolean isDeleted(byte data[]){
        if(data.length<DELETE_MARK.length){
            return false;
        }
        if(data[0] == DELETE_MARK[0] &&
                data[1] == DELETE_MARK[1] &&
                data[2] == DELETE_MARK[2] &&
                data[3] == DELETE_MARK[3]) {
            return true;
        }
        return false;
    }

    private void moveReadBuffer(int location,int size) throws IOException{
        if(readBuffer==null) {
            byte data[] = null;
            if (file.length() < BUFFER_SIZE) {
                data = new byte[(int) file.length()];
            } else {
                data = new byte[BUFFER_SIZE];
            }
            raf.seek(0);
            raf.read(data);
            readBuffer = new BytesArray(data);
            readBufferLocation = 0;
        }

        if(location<readBufferLocation || location+size>readBufferLocation+readBuffer.getBytes().length){
            if(location>file.length()-BUFFER_SIZE){
                readBufferLocation = (int)(file.length()-BUFFER_SIZE);
            }else{
                readBufferLocation = location;
            }
            byte data[] = new byte[BUFFER_SIZE];
            raf.seek(readBufferLocation);
            raf.read(data);
            readBuffer = new BytesArray(data);
        }
    }

    public HierarchyBytesArray readByIndex(int index) throws IOException {
        DataLocation dl = this.mainIndex.get(index);
        if(dl==null){
            return null;
        }
        byte data[] = new byte[dl.getLength()];
        if(dl.getStartPosition()>= writeBufferLocation){
            int bufferStartPosition = (int)(dl.getStartPosition()- writeBufferLocation);
            System.arraycopy(writeBuffer.getBytes(),bufferStartPosition,data,0,data.length);
        }else{
            moveReadBuffer(dl.getStartPosition(),dl.getLength());
            int pos = dl.getStartPosition()-readBufferLocation;
            System.arraycopy(readBuffer.getBytes(),pos,data,0,data.length);

        }
        if(isDeleted(data)){
            return null;
        }
        HierarchyBytesArray ba = new HierarchyBytesArray();
        ba.setBytesData(data);
        for(VTable child:this.vTable.getChildren()){
            DataFile childDf = DataFileManager.instance.getDataFile(Encoder.getMD5ByClass(child.getJavaClassType()));
            ba.getChildren().addAll(childDf.readChildren(index));
        }
        return ba;
    }

    public HierarchyBytesArray deleteByIndex(int index) throws IOException {
        DataLocation dl = this.mainIndex.get(index);
        byte data[] = new byte[dl.getLength()];
        if(dl.getStartPosition()>= writeBufferLocation){
            int bufferStartPosition = (int)(dl.getStartPosition()- writeBufferLocation);
            System.arraycopy(writeBuffer.getBytes(),bufferStartPosition,data,0,data.length);
            System.arraycopy(DELETE_MARK,0,writeBuffer.getBytes(),bufferStartPosition,DELETE_MARK.length);
        }else{
            raf.seek(dl.getStartPosition());
            raf.read(data);
            raf.seek(dl.getStartPosition());
            raf.write(DELETE_MARK);
        }
        HierarchyBytesArray ba = new HierarchyBytesArray();
        ba.setBytesData(data);
        for(VTable child:this.vTable.getChildren()){
            DataFile childDf = DataFileManager.instance.getDataFile(Encoder.getMD5ByClass(child.getJavaClassType()));
            ba.getChildren().addAll(childDf.deleteChildren(index));
        }
        this.parentToChildrenIndex.remove(dl.getParentIndex());
        this.mainIndex.remove(dl.getRecordIndex());
        return ba;
    }

    public HierarchyBytesArray deleteByKey(DataKey key) throws IOException {
        Integer index = dataKeyToIndex.get(key);
        if(index!=null){
            return deleteByIndex(index);
        }
        return null;
    }

    public List<HierarchyBytesArray> readChildren(int parentIndex) throws IOException {
        Integer childrenIndex = this.parentToChildrenIndex.get(parentIndex);
        List<HierarchyBytesArray> result = new LinkedList<>();
        if(childrenIndex!=null){
            HierarchyBytesArray ba = readByIndex(childrenIndex);
            if(ba!=null) {
                result.add(ba);
            }
        }
        return result;
    }

    public List<HierarchyBytesArray> deleteChildren(int parentIndex) throws IOException {
        Integer childrenIndex = this.parentToChildrenIndex.get(parentIndex);
        List<HierarchyBytesArray> result = new LinkedList<>();
        if(childrenIndex!=null){
            HierarchyBytesArray ba = deleteByIndex(childrenIndex);
            if(ba!=null) {
                result.add(ba);
            }
        }
        return result;
    }

    public int getParentIndex(int index){
        DataLocation dl = this.mainIndex.get(index);
        if(dl!=null){
            return dl.getParentIndex();
        }
        return -1;
    }
}
