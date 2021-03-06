/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import org.datasand.codec.Encoder;
import org.datasand.codec.VColumn;
import org.datasand.codec.VSchema;
import org.datasand.codec.VTable;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.store.DataStore;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JDBCServer {
    private DataStore database = null;
    private Connection agent = null;

    static {
        Encoder.registerSerializer(JDBCMessage.class, new JDBCMessage());
        Encoder.registerSerializer(JDBCDataContainer.class, new JDBCDataContainer());
    }

    public JDBCServer(DataStore a) {
        this.database = a;
        this.agent = new Connection(new MicroServicesManager(),this.database);
    }

    public void close(){
        try{
            this.agent.close();
        }catch(Exception err){
            err.printStackTrace();
        }
    }

    public void connectToClient(String addr) {
    }

    public static void execute(ResultSet rs, DataStore database, boolean execute) throws SQLException {
        if(rs.getSQL().toLowerCase().trim().equals("select 1")){
            rs.setFinished(true);
            return;
        }
        checkAndBreakSubQueries(rs, database,execute);
        if (rs.getSubQueries().size() == 0) {
            parseTables(rs);
            parseFields(rs);
            parseCriteria(rs);
            if(execute){
                try {
                    database.execute(rs);
                } catch (Exception err) {
                    throw new SQLException("Error", err);
                }
            }
        } else {
            parseExternalQuery(rs);
        }
    }

    public static void parseExternalQuery(ResultSet rs) throws SQLException {
        String sql = rs.getSQL();
        for (Map.Entry<String, ResultSet> entry : rs.getSubQueries().entrySet()) {
            int index = sql.toLowerCase().indexOf(entry.getValue().getSQL());
            String extSql = sql.substring(0, index);
            index = extSql.lastIndexOf("(");
            extSql = extSql.substring(0, index);
            System.out.println("External SQL=" + extSql);
            parseLogicalFields(extSql, rs);
        }
    }

    public static void parseLogicalFields(String sql, ResultSet rs) throws SQLException {
        if(sql.trim().toLowerCase().equals("select * from")){
            for (Map.Entry<String, ResultSet> entry : rs.getSubQueries().entrySet()) {
                for(VTable node:entry.getValue().getTables()){
                    rs.addTableToQuery(node);
                }
                for(VColumn field:entry.getValue().getFieldsInQuery()){
                    rs.addFieldToQuery(field,"");
                }
                while (entry.getValue().next()) {
                    Map rec = entry.getValue().getCurrent();
                    Map newRec = new HashMap();
                    newRec.putAll(rec);
                    rs.addRecord(newRec,true);
                }
            }
            rs.setFinished(true);
            return;
        }

        Map<String, VTable> logicalNameToNode = new HashMap<String, VTable>();
        Map<String, String> origNameToName = new HashMap<String, String>();
        List<VColumn> columnOrder = new ArrayList<>();
        int nextLogField = addNextLogicalField(sql, 0, logicalNameToNode, origNameToName,columnOrder);
        int next = sql.toLowerCase().indexOf(" as ", nextLogField);
        while (next != -1) {
            nextLogField = addNextLogicalField(sql, nextLogField + 1, logicalNameToNode, origNameToName,columnOrder);
            next = sql.toLowerCase().indexOf(" as ", nextLogField + 1);
        }

        for (VTable node : logicalNameToNode.values()) {
            rs.addTableToQuery(node);
        }
        for(VColumn field:columnOrder){
            rs.addFieldToQuery(field,"");
        }
        for (Map.Entry<String, ResultSet> entry : rs.getSubQueries().entrySet()) {
            while (entry.getValue().next()) {
                Map rec = entry.getValue().getCurrent();
                Map newRec = new HashMap();
                for (Iterator iter = rec.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry e = (Map.Entry) iter.next();
                    String key = (String) e.getKey();
                    Object value = e.getValue();
                    String logicalKey = origNameToName.get(key);
                    if (value != null && logicalKey != null) {
                        newRec.put(logicalKey, value);
                    }
                }
                rs.addRecord(newRec,true);
            }
        }
        rs.setFinished(true);
    }

    public static void main(String args[]) {
        String sql = "SELECT DISTINCT"
                + "\"LOGICAL_TABLE_1\".\"nodes/node.id\" AS \"COL0\"\n"
                + ",\"LOGICAL_TABLE_1\".\"nodes/node.id\" AS \"COL1\"\n"
                + ",\"LOGICAL_TABLE_1\".\"nodes/node.id\" AS \"COL2\"\n"
                + "FROM\n"
                + "(select * from nodes/node;) \"LOGICAL_TABLE_1\"\n";
        ResultSet rs = new ResultSet(sql);
        try {
            parseLogicalFields(sql, rs);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static int addNextLogicalField(String sql, int startIndex,
            Map<String, VTable> logicalNameToNode,
            Map<String, String> origNameToName, List<VColumn> columnOrder) {
        int index1 = sql.indexOf("\"", startIndex);
        int index2 = sql.indexOf("\".\"", index1);
        int index3 = sql.indexOf("\"", index2 + 3);
        int index4 = sql.toLowerCase().indexOf(" as ", startIndex);
        int index5 = sql.indexOf("\"", index4);
        int index6 = sql.indexOf("\"", index5 + 1);

        String tblName = sql.substring(index1 + 1, index2);
        String origFieldNameFull = sql.substring(index2 + 3, index3);
        String origTableName = "";
        String origFieldName = "";
        if (origFieldNameFull.indexOf(".") != -1) {
            origTableName = origFieldNameFull.substring(0,origFieldNameFull.indexOf("."));
            origFieldName = origFieldNameFull.substring(origFieldNameFull.indexOf(".") + 1);
        }
        String logicalFieldName = sql.substring(index5 + 1, index6);
        VTable node = logicalNameToNode.get(tblName);
        if (node == null) {
            node = null;//new VTable(tblName, origTableName,null);
            logicalNameToNode.put(tblName, node);
        }
        //columnOrder.add(node.addColumn(logicalFieldName, tblName, origFieldName, origTableName));
        origNameToName.put(origFieldNameFull, tblName + "." + logicalFieldName);
        return index6;
    }

    public static void checkAndBreakSubQueries(ResultSet rs, DataStore database, boolean execute) throws SQLException {
        String sql = rs.getSQL().toLowerCase();
        int index = sql.indexOf("select");
        if (index == -1)
            throw new SQLException("Select statement is missing...");
        int index2 = sql.indexOf("select", index + 6);
        if (index2 != -1) {
            int startSubQuery = index2;
            for (int i = startSubQuery; i >= 0; i--) {
                if (sql.charAt(i) == '(') {
                    startSubQuery = i;
                    break;
                }
            }
            int braketCount = 0;
            int endSubQuery = startSubQuery;
            do {
                if (sql.charAt(endSubQuery) == '(')
                    braketCount++;
                else if (sql.charAt(endSubQuery) == ')')
                    braketCount--;
                endSubQuery++;
            } while (braketCount > 0 || endSubQuery == sql.length());
            String subQuerySQL = sql.substring(startSubQuery + 1,endSubQuery - 1);
            if(rs.getSQL().toLowerCase().substring(0,startSubQuery).trim().equals("select * from")){
                rs.setSQL(subQuerySQL);
                return;
            }
            index = sql.indexOf("\"", endSubQuery);
            index2 = sql.indexOf("\"", index + 1);
            if(index==-1){
                index = endSubQuery;
                index2 = sql.length();
            }
            String logicalName = rs.getSQL().substring(index + 1, index2).trim();
            ResultSet subRS = rs.addSubQuery(subQuerySQL, logicalName);
            JDBCServer.execute(subRS, database,execute);
        }
    }

    public static void parseTables(ResultSet rs) throws SQLException {
        String lowSQL = rs.getSQL().toLowerCase();
        int from = lowSQL.indexOf("from");
        int where = lowSQL.indexOf("where");
        int subQuery = lowSQL.indexOf("select", 2);
        int fromTo = lowSQL.indexOf(";");

        if (where != -1 && subQuery != -1 && where < subQuery) {
            fromTo = where;
        } else if (where != -1 && subQuery != -1 && where > subQuery) {
            fromTo = subQuery;
        } else if (where != -1) {
            fromTo = where;
        } else if (subQuery != -1) {
            fromTo = subQuery;
        }

        if (from == -1) {
            throw new SQLException("Missing \"from\" statement.");
        }

        if (fromTo == -1) {
            throw new SQLException("Missing terminating \";\".");
        }

        String tableNames = rs.getSQL().substring(from + 4, fromTo).trim();
        StringTokenizer tokens = new StringTokenizer(tableNames, ",");
        while (tokens.hasMoreTokens()) {
            String tableName = tokens.nextToken().trim();
            VTable table = VSchema.instance.getVTableByName(tableName);
            if (table == null) {
                throw new SQLException("Unknown table name \"" + tableName
                        + "\"");
            }
            rs.addTableToQuery(table);
        }
    }

    public static void addCriteria(VColumn col, Criteria c, ResultSet rs) {
        Map<VColumn, List<Criteria>> tblCriteria = null;
        if(col.getAugmentedTableName()==null){
            rs.getCriteria().get(col.getvTableName());
        }else{
            rs.getCriteria().get(col.getAugmentedTableName());
        }
        if (tblCriteria == null) {
            tblCriteria = new ConcurrentHashMap<VColumn, List<Criteria>>();
            if(col.getAugmentedTableName()==null){
                rs.getCriteria().put(col.getvTableName(), tblCriteria);
            }else{
                rs.getCriteria().put(col.getAugmentedTableName(), tblCriteria);
            }
        }
        List<Criteria> lstCriteria = tblCriteria.get(col);
        if (lstCriteria == null) {
            lstCriteria = new ArrayList<Criteria>();
            tblCriteria.put(col, lstCriteria);
        }
        lstCriteria.add(c);
    }

    public static void parseFields(ResultSet rs) throws SQLException {
        String lowSQL = rs.getSQL().toLowerCase();
        if (!lowSQL.startsWith("select")) {
            throw new SQLException("Missing 'select' statement.");
        }
        int from = lowSQL.indexOf("from");
        if (from == -1) {
            throw new SQLException("Missing 'from' statement.");
        }
        String fields = rs.getSQL().substring(6, from).trim();
        if(fields.equals("Objects")){
            rs.setCollectedDataType(ResultSet.COLLECT_TYPE_OBJECTS);
            fields = "*";
        }
        StringTokenizer tokens = new StringTokenizer(fields, ",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (token.equals("*")) {
                for (VTable table : rs.getTables()) {
                    for(VColumn col:table.getColumns()){
                        rs.addFieldToQuery(col,table.getName());
                    }
                }
                return;
            }
            if (token.indexOf(".") != -1) {
                VTable tbl = VSchema.instance.getVTableByName(token.substring(0, token.indexOf(".")).trim());
                String p = token.substring(token.indexOf(".") + 1);
                if (p.equals("*")) {
                    for (VColumn c : tbl.getColumns()) {
                        rs.addFieldToQuery(c,tbl.getName());
                    }
                } else {
                    VColumn col = tbl.getColumnByName (p);
                    rs.addFieldToQuery(col,tbl.getName());
                }
            } else {
                VColumn col = null;
                String tableName = null;
                for (VTable table : rs.getTables()) {
                    col = table.getColumnByName(token);
                    tableName = table.getName();
                    if (col != null) {
                        break;
                    }
                }

                if(col == null){

                }

                if (col == null) {
                    throw new SQLException("Unknown field name '" + token+ "'.");
                }
                rs.addFieldToQuery(col,tableName);
            }
        }
    }

    public static void parseCriteria(ResultSet rs) {
        String lowSQL = rs.getSQL().toLowerCase();
        int where = lowSQL.indexOf("where");
        int order = lowSQL.indexOf("order");
        int whereTo = lowSQL.indexOf(";");

        if (where == -1) {
            return;
        }

        if (order != -1) {
            whereTo = order;
        }

        if(whereTo==-1)
            whereTo=lowSQL.length();

        String whereStatement = rs.getSQL().substring(where + 5, whereTo)
                .trim();
        Criteria cr = new Criteria(whereStatement, -1);
        for (VTable tbl : rs.getTables()) {
            for (VColumn col : tbl.getColumns()) {
                String colCriteria = cr.getCriteriaForProperty(col);
                if (colCriteria != null && !colCriteria.trim().equals("")) {
                    addCriteria(col, new Criteria(colCriteria, -1), rs);
                }
            }
        }
        VColumn rowIndex = new VColumn("RowIndex","");
        String rowIndexCriteria = cr.getCriteriaForProperty(rowIndex);
        if(rowIndexCriteria!=null && !rowIndexCriteria.equals("")){
            int fromIndex = 0;
            int toIndex = 1;
            StringTokenizer tokens = new StringTokenizer(rowIndexCriteria,"?");
            String token = tokens.nextToken();
            int index = token.indexOf(">=");
            if(index!=-1){
                int index1 = token.indexOf("and");
                if(index1==-1) index1 = token.length();
                fromIndex = Integer.parseInt(token.substring(index+2,index1).trim());
            }
            if(tokens.hasMoreTokens()){
                token = tokens.nextToken();
                index = token.indexOf("<=");
                if(index!=-1){
                    toIndex = Integer.parseInt(token.substring(index+2).trim());
                }
            }
            rs.fromIndex = fromIndex;
            rs.toIndex = toIndex;
        }
    }
}
