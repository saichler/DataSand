package org.datasand.store.json;

import java.io.PrintStream;
import java.sql.ResultSet;

import org.datasand.store.ObjectDataStore;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JSONObjectStore extends ObjectDataStore{
    static{
        EncodeDataContainerFactory.registerInstantiator(EncodeDataContainer.ENCODER_TYPE_JSON, new JSONEncodeDataContainerInstanciator());
    }

    public JSONObjectStore(){
        super("",true,EncodeDataContainer.ENCODER_TYPE_JSON);
    }

    @Override
    public void deleteDatabase() {
        // TODO Auto-generated method stub
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public ResultSet executeSql(String sql,boolean execute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void executeSql(String sql, PrintStream out, boolean toCsv) {
        // TODO Auto-generated method stub
    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

}
