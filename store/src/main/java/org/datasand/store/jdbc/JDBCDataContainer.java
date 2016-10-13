/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.VSchema;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.network.NetUUID;
import org.datasand.store.jdbc.ResultSet.RSID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JDBCDataContainer implements ISerializer{
    private Object data = null;
    private RSID rsID = null;

    public JDBCDataContainer(){
    }

    public JDBCDataContainer(Object _data, RSID _rsID){
        this.data = _data;
        this.rsID = _rsID;
    }

    public Object getData() {
        return data;
    }

    public RSID getRsID() {
        return rsID;
    }
    
    @Override
    public void encode(Object value, BytesArray ba) {
        JDBCDataContainer dc = (JDBCDataContainer)value;
        if(dc.rsID!=null){
            Encoder.encodeInt32(dc.rsID.getAddress(), ba);
            Encoder.encodeInt64(dc.rsID.getTime(), ba);
            Encoder.encodeInt32(dc.rsID.getLocalID(), ba);
        }else
            Encoder.encodeNULL(ba);
        if(dc.data instanceof NetUUID){
            Encoder.encodeInt16(5, ba);
            Encoder.encodeObject(dc.data, ba);
        }else
        if(dc.data instanceof ResultSet){
            Encoder.encodeInt16(1, ba);
            ResultSet.encode((ResultSet)dc.data, ba);
        }else
        if(dc.data instanceof String){
            Encoder.encodeInt16(2, ba);
            byte[] data = VSchema.instance.getRepositoryData();
            Encoder.encodeByteArray(data,ba);
        }else
        if(dc.data instanceof List){
            Encoder.encodeInt16(3, ba);
            List<Map> records = (List<Map>)dc.data;
            Encoder.encodeInt16(records.size(), ba);
            for(Map m:records){
	            Encoder.encodeInt16(m.size(),ba);
	            for(Object o:m.entrySet()){
	                Map.Entry e = (Map.Entry)o;
	                Encoder.encodeObject(e.getKey(), ba);
	                Encoder.encodeObject(e.getValue(), ba);
	            }
            }
        }else
        if(dc.data instanceof Exception){
            Encoder.encodeInt16(4, ba);
            Exception e = (Exception)dc.data;
            Encoder.encodeString(e.getMessage(),ba);
        }else{
            Encoder.encodeInt16(6, ba);
        }
    }

    @Override
    public Object decode(BytesArray ba) {
        JDBCDataContainer dc = new JDBCDataContainer();
        if(!Encoder.isNULL(ba)){
            dc.rsID = new RSID(Encoder.decodeInt32(ba),Encoder.decodeInt64(ba),Encoder.decodeInt32(ba));
        }
        int type = Encoder.decodeInt16(ba);
        if(type==1){
            dc.data = ResultSet.decode(ba);
        }else
        if(type==2){
            byte data[] = Encoder.decodeByteArray(ba);
            VSchema.instance.load(data);
        }else
        if(type==3){
        	int listSize = Encoder.decodeInt16(ba);
        	List<Map> records = new ArrayList<Map>(listSize);
        	for(int i=0;i<listSize;i++){
	            Map m = new HashMap();
	            int size = Encoder.decodeInt16(ba);
	            for(int j=0;j<size;j++){
	                Object key = Encoder.decodeObject(ba);
	                Object value = Encoder.decodeObject(ba);
	                m.put(key, value);
	            }
	            records.add(m);
        	}
            dc.data = records;
        }else
        if(type==4){
            String msg = Encoder.decodeString(ba);
            Exception e = new Exception(msg);
            dc.data = e;
        }else
        if(type==5){
            dc.data = Encoder.decodeObject(ba);
        }
        return dc;
    }
}
