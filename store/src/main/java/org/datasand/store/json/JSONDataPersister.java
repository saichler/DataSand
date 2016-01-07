package org.datasand.store.json;

import org.datasand.codec.TypeDescriptorsContainer;
import org.datasand.store.DataPersister;
import org.datasand.store.Shard;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JSONDataPersister extends DataPersister{

    public JSONDataPersister(Shard s,Class<?> type,TypeDescriptorsContainer _con){
        super(s,type,_con);
    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public void load() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getObjectCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean contain(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleted(Object data) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int write(int parentRecordIndex, int recordIndex,
            EncodeDataContainer ba) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object delete(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object delete(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] delete(int[] recordIndexs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object read(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object read(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] read(int[] recordIndexs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getParentIndex(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getIndexByKey(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getParentIndexByKey(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] getRecordIndexesByParentIndex(int parentRecordIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
