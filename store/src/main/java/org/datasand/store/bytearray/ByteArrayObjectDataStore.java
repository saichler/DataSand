package org.datasand.store.bytearray;

import org.datasand.store.ObjectDataStore;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ByteArrayObjectDataStore extends ObjectDataStore{
/*
    private JDBCServer server = null;

    public ByteArrayObjectDataStore(String _dataLocation, boolean _shouldSortFields) {
        super(_dataLocation,_shouldSortFields,EncodeDataContainer.ENCODER_TYPE_BYTE_ARRAY);
        server = new JDBCServer(this);
    }

    public void deleteDatabase() {
        File f = new File(this.dataLocation);
        deleteDirectory(f);
    }

    public String getDataLocation(){
        return this.dataLocation;
    }

    public static void deleteDirectory(File dir) {
        File files[] = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    deleteDirectory(file);
                else
                    file.delete();
            }
        }
        dir.delete();
    }

    public void init() {
        AttributeDescriptor.IS_SERVER_SIDE = true;
        File dbDir = new File(this.dataLocation);
        if (dbDir.exists()) {
            File xDirs[] = dbDir.listFiles();
            if (xDirs != null) {
                for (File xDir : xDirs) {
                    int xID = xDir.getName().indexOf("X-");
                    if (xID != -1) {
                        int xx = Integer.parseInt(xDir.getName().substring(
                                xID + 2));
                        if (X_VECTOR < xx)
                            X_VECTOR = xx;
                        File yDirs[] = xDir.listFiles();
                        if (yDirs != null) {
                            for (File yDir : yDirs) {
                                int yID = yDir.getName().indexOf("Y-");
                                if (yID != -1) {
                                    int yy = Integer.parseInt(yDir.getName()
                                            .substring(yID + 2));
                                    if (Y_VECTOR < yy)
                                        Y_VECTOR = yy;
                                    File zDirs[] = yDir.listFiles();
                                    if (zDirs != null) {
                                        for (File zDir : zDirs) {
                                            int zID = zDir.getName().indexOf(
                                                    "Z-");
                                            if (zID != -1) {
                                                int zz = Integer.parseInt(zDir
                                                        .getName().substring(
                                                                zID + 2));
                                                if (Z_VECTOR < zz)
                                                    Z_VECTOR = zz;
                                                File objects[] = zDir
                                                        .listFiles();
                                                if (objects != null) {
                                                    for (File obj : objects) {
                                                        Shard newLoc = new Shard(xx,yy,zz,Shard.readBlockKey(obj.getPath()),this);
                                                        this.location.put(newLoc.getBlockKey(),newLoc);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else{
            dbDir.mkdirs();
        }
    }

    public ResultSet executeSql(String sql,boolean execute){
        ResultSet rs = new ResultSet(sql);
        try{
            JDBCServer.execute(rs, this,execute);
            return rs;
        }catch(Exception err){
            err.printStackTrace();
        }
        return null;
    }

    public void executeSql(String sql, PrintStream out, boolean toCsv) {
        ResultSet rs = new ResultSet(sql);
        try {
            int count = 0;
            JDBCServer.execute(rs, this,true);
            boolean isFirst = true;
            int loc = rs.getFieldsInQuery().size() - 1;
            int totalWidth = 0;
            if(this.shouldSortFields){
                rs.sortFieldsInQuery();
            }
            for (AttributeDescriptor c : rs.getFieldsInQuery()) {
                if (isFirst) {
                    isFirst = false;
                    if (toCsv) {
                        out.print("\"");
                    }
                }

                if (!toCsv) {
                    out.print("|");
                }

                out.print(c.getColumnName());

                if (!toCsv) {
                    int cw = c.getCharWidth();
                    int cnw = c.getColumnName().length();
                    if (cnw > cw) {
                        c.setCharWidth(cnw);
                    }
                    int gap = cw - cnw;
                    for (int i = 0; i < gap; i++) {
                        out.print(" ");
                    }
                }

                totalWidth += c.getCharWidth() + 1;

                if (loc > 0) {
                    if (toCsv) {
                        out.print("\",\"");
                    }
                }
                loc--;
            }

            if (toCsv) {
                out.println("\"");
            } else {
                totalWidth++;
                out.println("|");
                for (int i = 0; i < totalWidth; i++) {
                    out.print("-");
                }
                out.println();
            }

            while (rs.next()) {
                isFirst = true;
                loc = rs.getFieldsInQuery().size() - 1;
                for (AttributeDescriptor c : rs.getFieldsInQuery()) {
                    if (isFirst) {
                        isFirst = false;
                        if (toCsv) {
                            out.print("\"");
                        }
                    }

                    if (!toCsv) {
                        out.print("|");
                    }

                    Object sValue = rs.getObject(c.toString());
                    if (sValue == null) {
                        sValue = "";
                    }
                    if(sValue instanceof byte[]){
                        byte[] data = (byte[])sValue;
                        sValue = "[";
                        for(int i=0;i<data.length;i++){
                            sValue= sValue.toString()+data[i];
                        }
                        sValue = sValue+"]";
                    }
                    out.print(sValue);

                    int cw = c.getCharWidth();
                    int vw = sValue.toString().length();
                    int gap = cw - vw;
                    for (int i = 0; i < gap; i++) {
                        out.print(" ");
                    }

                    if (loc > 0) {
                        if (toCsv) {
                            out.print("\",\"");
                        }
                    }
                    loc--;
                }
                if (toCsv) {
                    out.println("\"");
                } else {
                    out.println("|");
                }
                count++;
            }
            out.println("Total Number Of Records=" + count);
        } catch (Exception err) {
            err.printStackTrace(out);
        }
    }

    public static class NETask implements Runnable {

        private ResultSet rs = null;
        private TypeDescriptor mainTable = null;
        private ByteArrayObjectDataStore db = null;

        public NETask(ResultSet _rs, TypeDescriptor _main, ByteArrayObjectDataStore _db) {
            this.rs = _rs;
            this.mainTable = _main;
            this.db = _db;
        }

        public void run() {
            for (int i = rs.fromIndex; i < rs.toIndex; i++) {
                ObjectWithInfo recInfo = null;
                if(rs.getCollectedDataType()==ResultSet.COLLECT_TYPE_RECORDS){
                    recInfo = db.readNoChildrenWithLocation(mainTable, i);
                }else{
                    recInfo = db.readWithLocation(mainTable, i);
                }
                if(recInfo==null){
                    break;
                }
                rs.addRecords(recInfo, true);
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

    public void execute(ResultSet rs) {
        TypeDescriptor table = rs.getMainTable();
        NETask task = new NETask(rs, table, this);
        rs.numberOfTasks = 1;
        threadpool.addTask(task);
    }

    public void commit() {
        for (Shard bl : this.location.values()) {
            bl.save();
        }
    }

    public void close() {
        this.commit();
        this.closed = true;
        for (Shard bl : this.location.values()) {
            bl.close();
        }
        if(this.server!=null)
        	this.server.close();
    }

    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public Object getEncodedKeyObject(Object dataKey) {
        TypeDescriptor td = this.getTypeDescriptorsContainer().getTypeDescriptorByObject(dataKey);
        BytesArray ba = (BytesArray)EncodeDataContainerFactory.newContainer(null,null,this.getEncoderType(),td);
        td.getSerializer().encode(dataKey, ba);
        MD5ID md5 = MD5ID.createX(ba.getData());
        return md5;
    }
    */
}
