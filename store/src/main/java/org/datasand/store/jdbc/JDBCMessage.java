/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.util.List;
import java.util.Map;
import org.datasand.microservice.Message;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.ServiceID;
import org.datasand.store.jdbc.ResultSet.RSID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JDBCMessage extends Message {

    public static final int TYPE_EXECUTE_QUERY = 1;
    public static final int TYPE_QUERY_REPLY = 2;
    public static final int TYPE_QUERY_RECORD = 3;
    public static final int TYPE_QUERY_FINISH = 4;
    public static final int TYPE_QUERY_ERROR = 5;
    public static final int TYPE_METADATA = 6;
    public static final int TYPE_METADATA_REPLY = 7;
    public static final int TYPE_HELLO_GROUP = 8;
    public static final int TYPE_DELEGATE_QUERY = 9;
    public static final int TYPE_DELEGATE_QUERY_RECORD = 10;
    public static final int TYPE_DELEGATE_QUERY_FINISH = 11;
    public static final int TYPE_DELEGATE_WAITING = 12;
    public static final int TYPE_DELEGATE_CONTINUE = 13;
    public static final int TYPE_NODE_WAITING_MARK = 14;
    public static final int TYPE_NODE_WAITING_MARK_REPLY = 15;
    
    //Temp rsid when the message is pass through
    private RSID rsid = null;

    public JDBCMessage() {
        super();
    }

    public JDBCMessage(long id, int type, Object data){
        super(id,type,data);
    }

    public JDBCMessage(Exception _err, RSID _RSID) {
        super(TYPE_QUERY_ERROR,new JDBCDataContainer(_err, _RSID));
    }

    public JDBCMessage(String bl) {
        super(TYPE_METADATA_REPLY,new JDBCDataContainer(bl,null));
    }

    public JDBCMessage(ResultSet _rs, int temp) {
        super(TYPE_QUERY_REPLY,new JDBCDataContainer(_rs,_rs.getRSID()));
    }

    public JDBCMessage(ResultSet _rs) {
        super(TYPE_EXECUTE_QUERY,new JDBCDataContainer(_rs,_rs.getRSID()));
    }

    public JDBCMessage(ResultSet _rs, int temp, int temp2) {
        super(TYPE_DELEGATE_QUERY,new JDBCDataContainer(_rs,_rs.getRSID()));
    }

    public JDBCMessage(List<Map> _records, RSID _rsID, int temp) {
        super(TYPE_DELEGATE_QUERY_RECORD,new JDBCDataContainer(_records,_rsID));
    }

    public JDBCMessage(List<Map> records, RSID _rsID) {
        super(TYPE_QUERY_RECORD,new JDBCDataContainer(records, _rsID));
    }

    public JDBCMessage(BytesArray edc, RSID _rsID) {
        super(TYPE_QUERY_RECORD,edc);
    }

    public JDBCMessage(RSID _rsID) {
        super(TYPE_QUERY_FINISH,new JDBCDataContainer(null,_rsID));
    }

    public JDBCMessage(RSID _rsID, int temp, int temp2) {
        super(TYPE_DELEGATE_QUERY_FINISH,new JDBCDataContainer(null,_rsID));
    }

    public JDBCMessage(int _temp, int _temp2){
        super(TYPE_METADATA,new JDBCDataContainer(null,null));
    }

    public JDBCMessage(ServiceID netID, RSID rsID){
        super(TYPE_DELEGATE_WAITING,new JDBCDataContainer(netID,rsID));
    }

    public JDBCMessage(ServiceID netID, RSID rsID, int temp){
        super(TYPE_NODE_WAITING_MARK,new JDBCDataContainer(netID,rsID));
    }

    public JDBCMessage(ServiceID netID, RSID rsID, int temp, int temp1){
        super(TYPE_NODE_WAITING_MARK_REPLY,new JDBCDataContainer(netID,rsID));
    }

    public JDBCMessage(ServiceID netID, RSID rsID, int temp, int temp1, int temp2){
        super(TYPE_DELEGATE_CONTINUE,new JDBCDataContainer(netID,rsID));
    }

    public ResultSet getRS() {
        return (ResultSet)((JDBCDataContainer)this.getMessageData()).getData();
    }

    public List<Map> getRecords() {
        return (List<Map>)((JDBCDataContainer)this.getMessageData()).getData();
    }

    public RSID getRSID() {
    	if(this.rsid!=null) return this.rsid;
    	if(this.getMessageData() instanceof JDBCDataContainer){
    		return ((JDBCDataContainer)this.getMessageData()).getRsID();
    	}else
    	if(this.getMessageData() instanceof BytesArray){
    		BytesArray edc = (BytesArray)this.getMessageData();
    		//reset the location to 0
    		edc.resetLocation();
    		//decode only the RDID
    		//2 - for the type Message
    		//4 - for the Message Type
    		//8 - for the Message ID
    		//2 - for the DataSandObjectContainer Type
    		// == 16
    		edc.advance(16);
    		int a = Encoder.decodeInt32(edc);
    		long b = Encoder.decodeInt64(edc);
    		int c = Encoder.decodeInt32(edc);
    		this.rsid = new RSID(a,b,c);
    		return this.rsid;
    	}
    	return null;
    }

    public Exception getERROR() {
        return (Exception)((JDBCDataContainer)this.getMessageData()).getData();
    }

    public MetaData getMetaData(){
        return (MetaData)((JDBCDataContainer)this.getMessageData()).getData();
    }
    public ServiceID getWaiting(){
        return (ServiceID)((JDBCDataContainer)this.getMessageData()).getData();
    }
    @Override
    public void encode(Object value, BytesArray edc) {
        // TODO Auto-generated method stub
        super.encode(value, edc);
    }

    @Override
    public Object decode(BytesArray edc) {
        Message m = (Message)super.decode(edc);
        JDBCMessage m2 = new JDBCMessage(m.getMessageID(),m.getMessageType(),m.getMessageData());
        return m2;
    }
}
