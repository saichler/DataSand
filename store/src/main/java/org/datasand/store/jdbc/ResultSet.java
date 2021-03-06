/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import org.datasand.codec.*;
import org.datasand.network.NID;
import org.datasand.store.HObject;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import org.datasand.store.ObjectDataStore.ObjectWithInfo;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ResultSet implements java.sql.ResultSet,ResultSetMetaData {

    public static final int COLLECT_TYPE_RECORDS = 0;
    public static final int COLLECT_TYPE_OBJECTS = 1;
    public static final int COLLECT_TYPE_ROOTS   = 2;
    public static final int RECORD_Threshold = 200;
    public static final int RECORD_Threshold_Release = RECORD_Threshold/2;

    private RSID rsid = new RSID();
    private String sql = null;
    private boolean finished = false;
    private List<VTable> tablesInQuery = new ArrayList<VTable>();
    private List<VColumn> fieldsInQuery = new ArrayList<VColumn>();
    private Map<String, VTable> tablesInQueryMap = new ConcurrentHashMap<String, VTable>();
    private Map<String,List<VColumn>> fieldsByTableInQuery = new ConcurrentHashMap<String,List<VColumn>>();
    private LinkedList<Map> records = new LinkedList<Map>();
    private Map<String, Map<VColumn, List<Criteria>>> criteria = new ConcurrentHashMap<String, Map<VColumn, List<Criteria>>>();

    private Map currentRecord = null;
    private static int nextID = 0;
    public int numberOfTasks = 0;
    private Exception err = null;
    private List<JDBCRecord> EMPTY_RESULT = new LinkedList<JDBCRecord>();
    private Map<String,ResultSet> subQueries = new HashMap<String,ResultSet>();
    public int fromIndex = 0;
    public int toIndex = Integer.MAX_VALUE;
    private int collectedDataType = 0;

    public static class RSID{
        private long address = UUID.randomUUID().getLeastSignificantBits();
        private long time = System.currentTimeMillis();
        private int localID = -1;
        public RSID(){
            synchronized(RSID.class){
                localID = nextID;
                nextID++;
            }
        }
        public RSID(int _a,long _t,int _l){
            this.address = _a;
            this.time = _t;
            this.localID = _l;
        }
        public long getAddress(){return this.address;}
        public long getTime(){return this.time;}
        public int getLocalID(){return this.localID;}
        @Override
        public boolean equals(Object obj) {
            RSID other = (RSID)obj;
            if(other.address==this.address &&
               other.time==this.time &&
               other.localID==this.localID){
                return true;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return (int)(address | time | localID);
        }
    }

    protected static void encode(ResultSet rs, BytesArray edc){
        Encoder.encodeInt32(rs.rsid.address, edc);
        Encoder.encodeInt64(rs.rsid.time, edc);
        Encoder.encodeInt32(rs.rsid.localID, edc);
        Encoder.encodeString(rs.sql, edc);
        Encoder.encodeBoolean(rs.finished, edc);

        Encoder.encodeInt16(rs.tablesInQuery.size(), edc);
        for(VTable t:rs.tablesInQuery){
            VTable.encode(t,edc);
        }
        Encoder.encodeInt16(rs.fieldsInQuery.size(), edc);
        for(VColumn t:rs.fieldsInQuery){
            VColumn.encode(t,edc);
        }
        Encoder.encodeInt16(rs.tablesInQueryMap.size(), edc);
        for(Map.Entry<String, VTable> e:rs.tablesInQueryMap.entrySet()){
            Encoder.encodeString(e.getKey(), edc);
            VTable.encode(e.getValue(),edc);
        }
        Encoder.encodeInt16(rs.fieldsByTableInQuery.size(), edc);
        for(Map.Entry<String,List<VColumn>> e:rs.fieldsByTableInQuery.entrySet()){
            Encoder.encodeString(e.getKey(), edc);
            Encoder.encodeInt16(e.getValue().size(), edc);
            for(VColumn at:e.getValue()){
                VColumn.encode(at,edc);
            }
        }
        Encoder.encodeInt16(rs.records.size(), edc);
        for(Map e:rs.records){
            Encoder.encodeInt16(e.size(), edc);
            for(Object obj:e.entrySet()){
                Map.Entry entry = (Map.Entry)obj;
                Encoder.encodeObject(entry.getKey(), edc);
                Encoder.encodeObject(entry.getValue(), edc);
            }
        }
        Encoder.encodeInt16(rs.criteria.size(), edc);
        for(Map.Entry<String, Map<VColumn, List<Criteria>>> e1:rs.criteria.entrySet()){
            Encoder.encodeString(e1.getKey(), edc);
            Encoder.encodeInt16(e1.getValue().size(), edc);
            for(Map.Entry<VColumn, List<Criteria>> e2:e1.getValue().entrySet()){
                VColumn.encode(e2.getKey(),edc);
                Encoder.encodeInt16(e2.getValue().size(), edc);
                for(Criteria c:e2.getValue()){
                    Criteria.encode(c, edc);
                }
            }
        }
    }

    public static ResultSet decode(BytesArray edc){
        ResultSet rs = new ResultSet();
        rs.rsid.address = Encoder.decodeInt32(edc);
        rs.rsid.time = Encoder.decodeInt64(edc);
        rs.rsid.localID = Encoder.decodeInt32(edc);
        rs.sql = Encoder.decodeString(edc);
        rs.finished = Encoder.decodeBoolean(edc);
        int size = Encoder.decodeInt16(edc);

        for(int i=0;i<size;i++){
            VTable ts = VTable.decode(edc);
            rs.tablesInQuery.add(ts);
        }
        size = Encoder.decodeInt16(edc);
        for(int i=0;i<size;i++){
            VColumn at = (VColumn) VColumn.decode(edc);
            rs.fieldsInQuery.add(at);
        }
        size = Encoder.decodeInt16(edc);
        for(int i=0;i<size;i++){
            String key = Encoder.decodeString(edc);
            VTable td = VTable.decode(edc);
            rs.tablesInQueryMap.put(key, td);
        }
        size = Encoder.decodeInt16(edc);
        for(int i=0;i<size;i++){
            String key = Encoder.decodeString(edc);
            int subSize = Encoder.decodeInt16(edc);
            List<VColumn> lst = new ArrayList<VColumn>(subSize);
            for(int j=0;j<subSize;j++){
                VColumn at = (VColumn)VColumn.decode(edc);
                lst.add(at);
            }
            rs.fieldsByTableInQuery.put(key, lst);
        }
        size = Encoder.decodeInt16(edc);
        for(int i=0;i<size;i++){
            int subSize = Encoder.decodeInt16(edc);
            Map rec = new HashMap();
            for(int j=0;j<subSize;j++){
                Object key = Encoder.decodeObject(edc);
                Object value = Encoder.decodeObject(edc);
                rec.put(key, value);
            }
            rs.records.add(rec);
        }
        size = Encoder.decodeInt16(edc);
        for(int i=0;i<size;i++){
            String key = Encoder.decodeString(edc);
            int subSize = Encoder.decodeInt16(edc);
            Map<VColumn,List<Criteria>> map = new HashMap<VColumn,List<Criteria>>();
            for(int j=0;j<subSize;j++){
                VColumn at = VColumn.decode(edc);
                int subSize2 = Encoder.decodeInt16(edc);
                List<Criteria> lst = new ArrayList<Criteria>(subSize2);
                for(int x=0;x<subSize2;x++){
                    Criteria c = Criteria.decode(edc);
                    lst.add(c);
                }
                map.put(at, lst);
            }
            rs.criteria.put(key, map);
        }
        return rs;
    }

    public java.sql.ResultSet getProxy() {
        //return (ResultSet) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {ResultSet.class }, new JDBCProxy(this));
        return this;
    }

    public void setSQL(String _sql) {
        this.sql = _sql;
    }

    public void setCollectedDataType(int t){
        this.collectedDataType = t;
    }

    public int getCollectedDataType(){
        return this.collectedDataType;
    }

    public void addFieldToQuery(VColumn field,String tableName){
        this.fieldsInQuery.add(field);
        List<VColumn> fieldsInTable = fieldsByTableInQuery.get(tableName);
        if(fieldsInTable==null){
            fieldsInTable = new LinkedList<VColumn>();
            fieldsByTableInQuery.put(tableName, fieldsInTable);
        }
        fieldsInTable.add(field);
    }

    public ResultSet addSubQuery(String _sql, String logicalName) {
        if(subQueries == null)
            subQueries = new HashMap<String,ResultSet>();
        ResultSet rs = new ResultSet(_sql);
        this.subQueries.put(logicalName,rs);
        return rs;
    }

    public Map<String,ResultSet> getSubQueries() {
        if(this.subQueries==null)
            this.subQueries = new HashMap<>();
        return this.subQueries;
    }

    private ResultSet() {
    }

    public ResultSet(String _sql) {
        this.sql = _sql;
    }

    public String getSQL() {
        return this.sql;
    }

    public void setError(Exception _err) {
        this.err = _err;
    }

    public Exception getError() {
        return this.err;
    }

    public void updateData(ResultSet rs) {
        synchronized (this) {
            this.tablesInQuery = rs.tablesInQuery;
            this.tablesInQueryMap = rs.tablesInQueryMap;
            this.fieldsInQuery = rs.fieldsInQuery;
            this.notifyAll();
        }
    }

    public int isObjectFitCriteria(Map objValues, String tableName) {
        Map<VColumn, List<Criteria>> tblCriteria = criteria
                .get(tableName);
        if (tblCriteria == null) {
            return 1;
        }
        for (Map.Entry<VColumn, List<Criteria>> cc : tblCriteria
                .entrySet()) {
            for (Criteria c : cc.getValue()) {
                Object value = objValues.get(cc.getKey().toString());
                int result = c.checkValue(value);
                if (result == 0) {
                    return 0;
                }
            }
        }
        return 1;
    }

    public Map<String, Map<VColumn, List<Criteria>>> getCriteria() {
        return this.criteria;
    }

    public RSID getRSID() {
        return this.rsid;
    }

    public List<VTable> getTables() {
        return tablesInQuery;
    }

    public void addTableToQuery(VTable node) {
        if (this.tablesInQueryMap.containsKey(node.getName())) {
            return;
        }
        this.tablesInQuery.add(node);
        this.tablesInQueryMap.put(node.getName(), node);
    }

    public List<VColumn> getFieldsInQuery() {
        return this.fieldsInQuery;
    }

    public VTable getMainTable() {
        if (tablesInQuery.size() == 1) {
            return tablesInQuery.get(0);
        }
        VTable result = null;
        for (VTable node : tablesInQuery) {
            if (result == null) {
                result = node;
            } else if (result.getHierarchyLevel() < node.getHierarchyLevel()) {
                result = node;
            }
        }
        return result;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean b) {
        this.finished = b;
    }

    public int size() {
        return this.records.size();
    }
    private boolean paused = false;
    public void addRecord(Map rec,boolean applyThreshold) {
        synchronized (this) {
            if (records == null) {
                records = new LinkedList<Map>();
            }
            records.add(rec);
            this.notifyAll();
            if(applyThreshold){
                if(records.size()>=RECORD_Threshold){
                    paused = true;
                    try{this.wait();}catch(Exception err){}
                }
            }
        }
    }

    public boolean next() {
        this.currentRecord = null;
        if (records == null) {
            records = new LinkedList<Map>();
        }
        while (!finished || records.size() > 0) {
            synchronized (this) {
                if (records.size() == 0) {
                    try {
                        this.wait(500);
                    } catch (Exception err) {
                    }
                    if (records.size() > 0) {
                        try {
                            currentRecord = records.removeFirst();
                            return true;
                        } finally {
                            if(!paused)
                                this.notifyAll();
                            else
                            if(paused && records.size()<RECORD_Threshold_Release)
                                this.notifyAll();
                        }
                    }
                } else {
                    try {
                        currentRecord = records.removeFirst();
                        return true;
                    } finally {
                        if(!paused)
                            this.notifyAll();
                        else
                        if(paused && records.size()<RECORD_Threshold_Release)
                            this.notifyAll();
                    }
                }
            }
        }
        return false;
    }

    public Map getCurrent() {
        return this.currentRecord;
    }

    private void createRecord(Object data, VTable node) {
        Map rec = new HashMap();
        for (VColumn c : this.fieldsInQuery) {
            if (c.ofVTable(node)) {
                try {
                    Method m = node.getJavaClassType().getMethod(c.getJavaGetMethodName(), null);
                    Object value = m.invoke(data, null);
                    if (value != null) {
                        rec.put(c.getvColumnName(), value);
                    } else {
                        rec.put(c.getJavaClassName(), "");
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }

            }
        }
    }

    private boolean beenHere(Set<String> beenHereElement, Object element) {
        if (beenHereElement == null) {
            beenHereElement = new HashSet<String>();
        }

        String elementKey = null;

        try {
            elementKey = element.toString();
        } catch (Exception err) {
            elementKey = "Unknown";
        }

        if (beenHereElement.contains(elementKey)) {
            return true;
        }

        beenHereElement.add(elementKey);
        return false;
    }

    public List<JDBCRecord> addRecords(HObject hobject, boolean root) {
        List<JDBCRecord> result = new LinkedList<JDBCRecord>();
        JDBCRecord rec = new JDBCRecord();
        if(insertRecord(hobject, rec)){
            if(addParentData(hobject.getParent(),rec)){
                result.add(rec);
                addRecord(rec.getData(),true);
            }
        }
        return result;
    }

    private String formatValueToString(Object value){
        if(value instanceof byte[]){
            byte[] data = (byte[])value;
            StringBuffer result = new StringBuffer("[");
            for(int i=0;i<data.length;i++){
                result.append(data[i]);
            }
            result.append("]");
            return result.toString();
        }
        return value.toString();
    }

    private boolean insertRecord(HObject hobject, JDBCRecord rec){
        if(checkCriteria(hobject.getObject())){
            switch(collectedDataType){
                case COLLECT_TYPE_RECORDS:
                    VTable table = VSchema.instance.getVTable(Observers.instance.getClassExtractor().getObjectClass(hobject.getObject()));
                    List<VColumn> tableFields = fieldsByTableInQuery.get(table.getName());
                    if(tableFields!=null) {
                        for (VColumn col : tableFields) {
                            Object value = col.get(hobject.getObject());
                            if (value != null) {
                                rec.addValue(col.toString(), formatValueToString(value));
                            }
                        }
                    }
                    break;
                case COLLECT_TYPE_OBJECTS:
                    Class objClass = Observers.instance.getClassExtractor().getObjectClass(hobject.getObject());
                    rec.addValue(objClass.getName(), hobject.getObject());
                    break;
            }
            return true;
        }
        return false;
    }

    public boolean addParentData(HObject parentHObject, JDBCRecord rec){
        if(this.tablesInQueryMap.size()>1){
            VTable parent = VSchema.instance.getVTable(Observers.instance.getClassExtractor().getObjectClass(parentHObject.getObject()));
            if(parent!=null && this.tablesInQueryMap.containsKey(parent.getName())){
                if(!insertRecord(parentHObject, rec)){
                    return false;
                }
                if(parentHObject.getParent()!=null){
                    addParentData(parentHObject.getParent(),rec);
                }
            }
        }
        return true;
    }

    public boolean checkCriteria(Object object){
        VTable vTable = VSchema.instance.getVTable(Observers.instance.getClassExtractor().getObjectClass(object));
        Map<VColumn,List<Criteria>> columnToCriteria = criteria.get(vTable.getName());
        if(columnToCriteria!=null){
            for(Map.Entry<VColumn, List<Criteria>> entry:columnToCriteria.entrySet()){
                Object value = entry.getKey().get(object);
                for(Criteria c:entry.getValue()){
                    if(c.checkValue(value)==0){
                        return false;
                    }
                }
            }
            return true;
        }else{
            return true;
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void afterLast() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeFirst() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearWarnings() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean first() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getConcurrency() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCursorName() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFetchSize() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws SQLException {
        return getObject(columnIndex);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return currentRecord.get(this.fieldsInQuery.get(columnIndex - 1)
                .toString());
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map)
            throws SQLException {
        return getObject(columnLabel);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return currentRecord.get(columnLabel);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRow() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Statement getStatement() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return "Kuku";
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return "Kuku";
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getType() throws SQLException {
        return java.sql.ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAfterLast() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isClosed() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void moveToInsertRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean previous() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void refreshRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean relative(int rows) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x,
            long length) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream,
            long length) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader,
            int length) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateClob(String columnLabel, Reader reader)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNString(int columnIndex, String nString)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNString(String columnLabel, String nString)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean wasNull() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return fieldsInQuery.size();
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return this.fieldsInQuery.get(column - 1).toString();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return 12;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getScale(int column) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public void sortFieldsInQuery(){
        Collections.sort(this.fieldsInQuery,new FieldCompare());
    }

    private class FieldCompare implements Comparator<VColumn>{
        @Override
        public int compare(VColumn o1, VColumn o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
    // //Metadata

}
