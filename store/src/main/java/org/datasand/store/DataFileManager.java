package org.datasand.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.Encoder;
import org.datasand.codec.MD5ID;
import org.datasand.codec.VSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 1/14/16.
 */
public class DataFileManager {
    private static final Logger LOG = LoggerFactory.getLogger(DataFileManager.class);
    private final Map<MD5ID,List<DataFile>> dataFiles = new HashMap<>();
    public static final DataFileManager instance = new DataFileManager();

    private DataFileManager(){
    }

    public DataFile getDataFile(MD5ID id){
        Class cls = Encoder.getClassByMD5(id);
        if(cls==null){
            LOG.error("Cannot find class by MD5");
            return null;
        }
        List<DataFile> dataFileList = dataFiles.get(id);
        if(dataFileList == null) {
            dataFileList = new ArrayList<>(2);
            dataFiles.put(id,dataFileList);
        }
        if(dataFileList.isEmpty()){
            try {
                DataFile df = new DataFile(new File("./database/"+cls.getName()+".data"), VSchema.instance.getVTable(cls));
                dataFileList.add(df);
            } catch (IOException e) {
                LOG.error("Unable to create data file",e);
                return null;
            }
        }
        return dataFileList.get(0);
    }

    public void commit(){
        for(List<DataFile> list:dataFiles.values()){
            for(DataFile df:list){
                try {
                    df.commit();
                } catch (IOException e) {
                    LOG.error("Failed to commit to datafile ",e);
                }
            }
        }
    }

    public void close(boolean commit){
        for(List<DataFile> list:dataFiles.values()){
            for(DataFile df:list){
                try {
                    df.close(commit);
                } catch (IOException e) {
                    LOG.error("Failed to commit to datafile ",e);
                }
            }
        }
        dataFiles.clear();
    }
}
