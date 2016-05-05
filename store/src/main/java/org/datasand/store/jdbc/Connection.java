/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.net.InetAddress;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.datasand.codec.BytesArray;
import org.datasand.microservice.Message;
import org.datasand.microservice.MessageEntry;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.network.HabitatID;
import org.datasand.store.DataStore;
import org.datasand.store.jdbc.ResultSet.RSID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Connection extends MicroService implements java.sql.Connection {
    private MetaData metaData = null;
    private DataStore dataDataStore;
    private HabitatID destination = null;
    private Map<RSID,QueryContainer> queries = new HashMap<RSID,QueryContainer>();
    private Map<RSID,QueryUpdater> updaters = new HashMap<RSID,QueryUpdater>();
    private Message repetitve = new Message(-1,null);

    static{
    	Message.passThroughIncomingMessage.put(JDBCMessage.TYPE_DELEGATE_QUERY_RECORD, JDBCMessage.TYPE_DELEGATE_QUERY_RECORD);
    	Message.passThroughOutgoingMessage.put(JDBCMessage.TYPE_QUERY_RECORD, JDBCMessage.TYPE_QUERY_RECORD);
    }
    
    public Connection(MicroServicesManager manager, DataStore _dataStore) {
        super(107,manager);
        this.dataDataStore = _dataStore;
        this.setARPGroup(107);
        this.sendARP(JDBCMessage.TYPE_HELLO_GROUP);
        this.registerRepetitiveMessage(10000, 10000, 0,repetitve);
    }

    public Connection(MicroServicesManager manager, String addr) {
        super(107,manager);
        try{
            if(manager.getHabitat().getLocalHost().getPort()==50000){
                destination = HabitatID.valueOf(InetAddress.getByName(addr).getHostAddress() + ":50000:107");
                manager.getHabitat().joinNetworkAsSingle(addr);
            }else{
                destination = HabitatID.valueOf(this.getMicroServiceManager().getHabitat().getLocalHost().getIPv4AddressAsString()+":50000:107");
            }
        }catch(Exception err){
            err.printStackTrace();
        }
    }
    public java.sql.Connection getProxy() {
        return this;
        /*
        return (Connection) Proxy.newProxyInstance(this.getClass()
                .getClassLoader(), new Class[] { Connection.class },
                new JDBCProxy(this));
                */
    }

    @Override
    public void processDestinationUnreachable(Message message,HabitatID unreachableSource) {
        System.out.println("Destination Unreachable:"+message.getMessageType()+":"+unreachableSource);
        try{
            throw new Exception("EX");
        }catch(Exception err){
            err.printStackTrace();
        }
    }
    public void sendToDestination(JDBCMessage m){
        this.send(m, destination);
    }

    @Override
    public void processMessage(Message message, HabitatID source, HabitatID destination) {
        if(message==repetitve){
            sendARP(JDBCMessage.TYPE_HELLO_GROUP);
            return;
        }
        if(message instanceof JDBCMessage){
            JDBCMessage msg = (JDBCMessage)message;

            switch (msg.getMessageType()) {
            case JDBCMessage.TYPE_METADATA_REPLY:
                this.metaData = msg.getMetaData();
                synchronized (this) {
                    this.notifyAll();
                }
                break;
            case JDBCMessage.TYPE_METADATA:
                send(new JDBCMessage("MetaData"),source);
                break;
            case JDBCMessage.TYPE_DELEGATE_QUERY:
                try{
                    System.out.println("Starting to execute Query-"+getMicroServiceID()+" From aggregator="+source);
                    JDBCServer.execute(msg.getRS(), this.dataDataStore,true);
                    QueryUpdater u = new QueryUpdater(msg.getRS(),source);
                    updaters.put(msg.getRS().getRSID(), u);
                    new Thread(u).start();
                }catch (Exception err) {
                    send(new JDBCMessage(err, msg.getRSID()),source);
                }
                break;
            case JDBCMessage.TYPE_EXECUTE_QUERY:
                System.out.println("Execute Query:"+getMicroServiceID());
                try {
                    QueryContainer qc = new QueryContainer(this,source, msg.getRS());
                    this.queries.put(msg.getRSID(),qc);
                    JDBCServer.execute(msg.getRS(), this.dataDataStore,false);
                    send(new JDBCMessage(msg.getRS(),0),source);
                } catch (Exception err) {
                    send(new JDBCMessage(err, msg.getRSID()),source);
                }
                break;
            case JDBCMessage.TYPE_QUERY_REPLY:
                ResultSet rs1 = Statement.getQuery(msg.getRS().getRSID());
                rs1.updateData(msg.getRS());
                break;
            case JDBCMessage.TYPE_DELEGATE_QUERY_RECORD:
            {
                QueryContainer c = queries.get(msg.getRSID());
                if(msg.getMessageData() instanceof JDBCDataContainer){
	                JDBCMessage m = new JDBCMessage(msg.getRecords(),msg.getRSID());
	                this.send(m, c.getSource());
                }else{
	                JDBCMessage m = new JDBCMessage((BytesArray)msg.getMessageData(),msg.getRSID());
	                this.send(m, c.getSource());
                }
            }
                break;
            case JDBCMessage.TYPE_QUERY_RECORD:
                ResultSet rs2 = Statement.getQuery(msg.getRSID());
                for(Map record:msg.getRecords()){
                	rs2.addRecord(record,false);
                }
                break;
            case JDBCMessage.TYPE_QUERY_FINISH:
                ResultSet rs3 = Statement.removeQuery(msg.getRSID());
                rs3.setFinished(true);
                break;
            case JDBCMessage.TYPE_DELEGATE_QUERY_FINISH:
            {
                System.out.println("Finished Query "+getMicroServiceID());
                QueryContainer c = queries.get(msg.getRSID());
                MessageEntry e = this.getJournalEntry(c.getMsg());
                e.removePeer(source);
                if(e.isFinished()){
                    JDBCMessage end = new JDBCMessage(msg.getRSID());
                    send(end,c.getSource());
                }
            }
                break;
            case JDBCMessage.TYPE_QUERY_ERROR:
                System.err.println("ERROR Executing Query\n");
                msg.getERROR().printStackTrace();
                ResultSet rs4 = Statement.removeQuery(msg.getRSID());
                rs4.setError(msg.getERROR());
                rs4.setFinished(true);
                synchronized (rs4) {
                    rs4.notifyAll();
                }
            case JDBCMessage.TYPE_DELEGATE_WAITING:
                QueryContainer qc = queries.get(msg.getRSID());
                send(new JDBCMessage(msg.getWaiting(),msg.getRSID(),0),qc.getSource());
                break;
            case JDBCMessage.TYPE_NODE_WAITING_MARK:
                send(new JDBCMessage(msg.getWaiting(),msg.getRSID(),0,0),source);
                break;
            case JDBCMessage.TYPE_NODE_WAITING_MARK_REPLY:
                send(new JDBCMessage(msg.getWaiting(),msg.getRSID(),0,0,0),msg.getWaiting());
                break;
            case JDBCMessage.TYPE_DELEGATE_CONTINUE:
                QueryUpdater u = updaters.get(msg.getRSID());
                synchronized(u.waitingObject){
                    u.waitingObject.notifyAll();
                }
                break;
            }
        }else
        if(message.getMessageType()== JDBCMessage.TYPE_HELLO_GROUP){
            getPeerEntry(source);
        }
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
        return "Data Sand JDBC Connection";
    }

    private class QueryUpdater implements Runnable {

        private final ResultSet rs;
        private final HabitatID source;
        private Object waitingObject = new Object();

        public QueryUpdater(ResultSet _rs, HabitatID _source) {
            this.rs = _rs;
            this.source = _source;
        }

        public void run() {
            int count = 0;
            List<Map> records = new ArrayList<Map>(ResultSet.RECORD_Threshold);
            while (rs.next()) {
            	records.add(rs.getCurrent());
                count++;
                if(count>= ResultSet.RECORD_Threshold){
                    JDBCMessage recs = new JDBCMessage(records, rs.getRSID(),0);
                    send(recs,source);
                    synchronized(waitingObject){
                    	records.clear();
                        JDBCMessage m = new JDBCMessage(getMicroServiceID(),rs.getRSID());
                        send(m,source);
                        try{waitingObject.wait();}catch(Exception err){err.printStackTrace();}
                        count = 0;
                    }
                }
            }
            if(!records.isEmpty()){
                JDBCMessage recs = new JDBCMessage(records, rs.getRSID(),0);
                send(recs,source);                
            }
            updaters.remove(rs.getRSID());
            JDBCMessage end = new JDBCMessage(rs.getRSID(),0,0);
            send(end,source);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws SQLException {
        this.getMicroServiceManager().shutdown();
    }

    @Override
    public void commit() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public java.sql.Statement createStatement() throws SQLException {
        return new Statement(this).getProxy();
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return new Statement(this).getProxy();
    }

    @Override
    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return new Statement(this).getProxy();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCatalog() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (this.metaData == null) {
            JDBCMessage cmd = new JDBCMessage(-1,-1);
            synchronized (this) {
                send(cmd,destination);
                try {
                    this.wait();
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
        return metaData;
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        System.err.println("SQL 1=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        System.err.println("SQL 2=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        System.err.println("SQL 3=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException {
        System.err.println("SQL 4=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        System.err.println("SQL 5=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        System.err.println("SQL 6=" + sql);
        return new Statement(this, sql).getProxy();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo(Properties properties)
            throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientInfo(String name, String value)
            throws SQLClientInfoException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSchema(String schema) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSchema() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

}
