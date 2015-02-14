package org.datasand.store.jdbc;

import java.util.Map;

import org.datasand.agents.Message;
import org.datasand.codec.EncodeDataContainer;
import org.datasand.codec.TypeDescriptorsContainer;
import org.datasand.codec.bytearray.ByteArrayEncodeDataContainer;
import org.datasand.network.NetworkID;
import org.datasand.store.jdbc.DataSandJDBCResultSet.RSID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class DataSandJDBCMessage extends Message {

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

    private RSID rsid = null;

    public DataSandJDBCMessage() {
        super();
    }

    public DataSandJDBCMessage(long id,int type,Object data){
        super(id,type,data);
    }

    public DataSandJDBCMessage(Exception _err, RSID _RSID) {
        super(TYPE_QUERY_ERROR,new DataSandJDBCDataContainer(_err, _RSID));
    }

    public DataSandJDBCMessage(TypeDescriptorsContainer bl) {
        super(TYPE_METADATA_REPLY,new DataSandJDBCDataContainer(bl,null));
    }

    public DataSandJDBCMessage(DataSandJDBCResultSet _rs,int temp) {
        super(TYPE_QUERY_REPLY,new DataSandJDBCDataContainer(_rs,_rs.getRSID()));
    }

    public DataSandJDBCMessage(DataSandJDBCResultSet _rs) {
        super(TYPE_EXECUTE_QUERY,new DataSandJDBCDataContainer(_rs,_rs.getRSID()));
    }

    public DataSandJDBCMessage(DataSandJDBCResultSet _rs,int temp,int temp2) {
        super(TYPE_DELEGATE_QUERY,new DataSandJDBCDataContainer(_rs,_rs.getRSID()));
    }

    public DataSandJDBCMessage(Map _record, RSID _rsID,int temp) {
        super(TYPE_DELEGATE_QUERY_RECORD,new DataSandJDBCDataContainer(_record,_rsID));
    }

    public DataSandJDBCMessage(Map record, RSID _rsID) {
        super(TYPE_QUERY_RECORD,new DataSandJDBCDataContainer(record, _rsID));
    }

    public DataSandJDBCMessage(ByteArrayEncodeDataContainer edc, RSID _rsID) {
        super(TYPE_QUERY_RECORD,edc);
    }

    public DataSandJDBCMessage(RSID _rsID) {
        super(TYPE_QUERY_FINISH,new DataSandJDBCDataContainer(null,_rsID));
    }

    public DataSandJDBCMessage(RSID _rsID,int temp,int temp2) {
        super(TYPE_DELEGATE_QUERY_FINISH,new DataSandJDBCDataContainer(null,_rsID));
    }

    public DataSandJDBCMessage(int _temp,int _temp2){
        super(TYPE_METADATA,new DataSandJDBCDataContainer(null,null));
    }

    public DataSandJDBCMessage(NetworkID netID,RSID rsID){
        super(TYPE_DELEGATE_WAITING,new DataSandJDBCDataContainer(netID,rsID));
    }

    public DataSandJDBCMessage(NetworkID netID,RSID rsID,int temp){
        super(TYPE_NODE_WAITING_MARK,new DataSandJDBCDataContainer(netID,rsID));
    }

    public DataSandJDBCMessage(NetworkID netID,RSID rsID,int temp,int temp1){
        super(TYPE_NODE_WAITING_MARK_REPLY,new DataSandJDBCDataContainer(netID,rsID));
    }

    public DataSandJDBCMessage(NetworkID netID,RSID rsID,int temp,int temp1,int temp2){
        super(TYPE_DELEGATE_CONTINUE,new DataSandJDBCDataContainer(netID,rsID));
    }

    public DataSandJDBCResultSet getRS() {
        return (DataSandJDBCResultSet)((DataSandJDBCDataContainer)this.getMessageData()).getData();
    }

    public Map getRecord() {
        return (Map)((DataSandJDBCDataContainer)this.getMessageData()).getData();
    }

    public RSID getRSID() {
    	if(this.rsid!=null) return this.rsid;
    	if(this.getMessageData() instanceof DataSandJDBCDataContainer){
    		return ((DataSandJDBCDataContainer)this.getMessageData()).getRsID();
    	}else
    	if(this.getMessageData() instanceof ByteArrayEncodeDataContainer){
    		ByteArrayEncodeDataContainer edc = (ByteArrayEncodeDataContainer)this.getMessageData();
    		edc.resetLocation();    		
    		edc.advance(16);
    		int a = edc.getEncoder().decodeInt32(edc);
    		long b = edc.getEncoder().decodeInt64(edc);
    		int c = edc.getEncoder().decodeInt32(edc);
    		this.rsid = new RSID(a,b,c);
    		return this.rsid;
    	}
    	return null;
    }

    public Exception getERROR() {
        return (Exception)((DataSandJDBCDataContainer)this.getMessageData()).getData();
    }

    public DataSandJDBCMetaData getMetaData(){
        return (DataSandJDBCMetaData)((DataSandJDBCDataContainer)this.getMessageData()).getData();
    }
    public NetworkID getWaiting(){
        return (NetworkID)((DataSandJDBCDataContainer)this.getMessageData()).getData();
    }
    @Override
    public void encode(Object value, EncodeDataContainer edc) {
        // TODO Auto-generated method stub
        super.encode(value, edc);
    }

    @Override
    public Object decode(EncodeDataContainer edc, int length) {
        Message m = (Message)super.decode(edc, length);
        DataSandJDBCMessage m2 = new DataSandJDBCMessage(m.getMessageID(),m.getMessageType(),m.getMessageData());
        return m2;
    }
}
