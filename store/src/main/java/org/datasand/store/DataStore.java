/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.HierarchyBytesArray;
import org.datasand.codec.MD5ID;
import org.datasand.codec.VColumn;
import org.datasand.codec.VSchema;
import org.datasand.codec.VTable;
import org.datasand.codec.util.ThreadPool;
import org.datasand.store.jdbc.JDBCServer;
import org.datasand.store.jdbc.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/12/16.
 */
public class DataStore {

    private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);
    private ThreadPool threadpool = new ThreadPool(5,"DataBase",2000);
    private JDBCServer jdbcServer = null;
    
    public int put(Object key,Object object){
        HierarchyBytesArray objectBytesArray = new HierarchyBytesArray();
        Encoder.encodeObject(object,objectBytesArray);
        DataKey dataKey = getDataKeyFromKey(key);
        return put(dataKey,objectBytesArray,-1);
    }

    public void prepareTable(Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        DataFileManager.instance.getDataFile(id);
    }

    public void startJDBC(){
        jdbcServer = new JDBCServer(this);
    }

    public void shutdownJDBC(){
        if(this.jdbcServer!=null){
            jdbcServer.close();
        }
    }

    private DataKey getDataKeyFromKey(Object key){
        DataKey dataKey = null;
        if(key!=null){
            if(key instanceof Integer){
                dataKey = new DataKey((Integer)key);
            }else
            if(key instanceof Long){
                dataKey = new DataKey((Long)key);
            }else
            if(key!=null){
                BytesArray ba = new BytesArray(256);
                Encoder.encodeObject(key,ba);
                MD5ID id = MD5ID.create(ba.getData());
                dataKey = new DataKey(id.getMd5Long1(),id.getMd5Long2());
            }
        }
        return dataKey;
    }

    private int put(DataKey dataKey,HierarchyBytesArray objectData,int parentIndex){
        DataFile df = DataFileManager.instance.getDataFile(objectData.getJavaTypeMD5());
        int myIndex = -1;
        try {
            myIndex = df.write(dataKey,objectData,parentIndex);
            if(objectData.getChildren()!=null){
                for(HierarchyBytesArray child:objectData.getChildren()){
                    put(null,child,myIndex);
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to write to data file",e);
        }
        return myIndex;
    }

    public Object getByKey(Object key,Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            DataKey dataKey = getDataKeyFromKey(key);
            HierarchyBytesArray hba = df.readByKey(dataKey);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            LOG.error("Failed to read object",e);
        }
        return null;
    }

    public Object getByIndex(int index,Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            HierarchyBytesArray hba = df.readByIndex(index);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            LOG.error("Failed to read object",e);
        }
        return null;
    }

    public Object deleteByIndex(int index,Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            HierarchyBytesArray hba = df.deleteByIndex(index);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            LOG.error("Failed to read object",e);
        }
        return null;
    }

    public Object deleteByKey(Object key,Class<?> type){
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            DataKey dataKey = getDataKeyFromKey(key);
            HierarchyBytesArray hba = df.deleteByKey(dataKey);
            Object o = Encoder.decodeObject(hba);
            return o;
        } catch (IOException e) {
            LOG.error("Failed to read object",e);
        }
        return null;
    }

    public void commit(){
        DataFileManager.instance.commit();
    }

    public void close() {
        if(this.jdbcServer!=null){
            this.jdbcServer.close();
        }
        DataFileManager.instance.close(true);
    }

    public void truncateAll(){
        DataFileManager.instance.close(false);
        File f = new File("./database");
        if(f.exists()){
            File files[] = f.listFiles();
            for(File file:files){
                file.delete();
            }
        }
        f.delete();
    }

    public void execute(ResultSet rs){
        VTable table = rs.getMainTable();
        NETask task = new NETask(rs, table, this);
        rs.numberOfTasks = 1;
        threadpool.addTask(task);
    }

    public static class NETask implements Runnable {

        private ResultSet rs = null;
        private VTable mainTable = null;
        private DataStore db = null;

        public NETask(ResultSet _rs, VTable _main, DataStore _db) {
            this.rs = _rs;
            this.mainTable = _main;
            this.db = _db;
        }

        public void run() {
            boolean end = false;
            for (int i = rs.fromIndex; i < rs.toIndex && !end; i++) {
                List<HObject> list = db.getHierarchyByIndex(i,this.mainTable.getJavaClassType());
                if(list==null){
                    break;
                }
                for(HObject o:list) {
                    if(o.getObject()==null){
                        end = true;
                        break;
                    }
                    rs.addRecords(o, true);
                }
            }
            synchronized (rs) {
                rs.numberOfTasks--;
                if (rs.numberOfTasks == 0) {
                    rs.setFinished(true);
                    rs.notifyAll();
                }
            }
        }
    }

    public List<HObject> getHierarchyByIndex(int index, Class<?> type){
        List<HObject> result = new LinkedList<>();
        MD5ID id = Encoder.getMD5ByClass(type);
        VTable vTable = VSchema.instance.getVTable(type);
        DataFile df = DataFileManager.instance.getDataFile(id);
        try {
            HierarchyBytesArray hba = df.readByIndex(index);
            VColumn parentColumn = null;
            int parentIndex = df.getParentIndex(index);
            HObject parentHObject = null;
            if(parentIndex!=-1){
                Map.Entry<VColumn,VTable> entry = vTable.getParents().entrySet().iterator().next();
                parentColumn = entry.getKey();
                parentHObject = getHierarchyByIndex(parentIndex,entry.getValue().getJavaClassType()).get(0);
            }
            if(parentColumn==null || !parentColumn.isList()) {
                HObject hobject = new HObject();
                Object o = Encoder.decodeObject(hba);
                hobject.setObject(o);
                hobject.setParent(parentHObject);
                result.add(hobject);
            }else
            if(parentColumn!=null && parentColumn.isList()){
                List list = Encoder.decodeList(hba);
                for(Object o:list){
                    HObject hobject = new HObject();
                    hobject.setObject(o);
                    hobject.setParent(parentHObject);
                    result.add(hobject);
                }
            }
            return result;
        } catch (IOException e) {
            LOG.error("Failed to read object",e);
        }
        return null;
    }
}
