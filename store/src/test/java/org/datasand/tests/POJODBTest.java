package org.datasand.tests;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.datasand.store.DataStore;
import org.datasand.store.jdbc.DataSandJDBCDriver;
import org.datasand.tests.test.PojoObject;
import org.datasand.tests.test.SubPojoList;
import org.datasand.tests.test.SubPojoObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class POJODBTest {
    private DataStore database = null;
    private long startTime = 0L;
    private long endTime = 0L;
    private static boolean createTestResources = false;
    private static final boolean disableTests = false;

    @Before
    public void setupFlagsAndCreateDB() {
        database = new DataStore();
        //Not a must, but performance is better if you prepare the data file on disk.
        database.prepareTable(PojoObject.class);
    }

    @After
    public void closeDBAndDeleteIT(){
        if(database!=null){
            database.truncateAll();
            database = null;
        }
    }

    public static PojoObject buildPojo(int pojoIndex){
        PojoObject obj = new PojoObject();
        obj.setTestIndex(pojoIndex);
        obj.setTestString("Name-"+pojoIndex);
        obj.setTestBoolean(true);
        obj.setTestLong(12345678L);
        obj.setTestShort((short)44.44);
        SubPojoObject sp = new SubPojoObject();
        obj.setSubPojo(sp);
        SubPojoList l1 = new SubPojoList();
        l1.setName("Item #"+pojoIndex+":1");
        SubPojoList l2 = new SubPojoList();
        l2.setName("Item #"+pojoIndex+":2");
        List<SubPojoList> list = new ArrayList<>();
        list.add(l1);
        list.add(l2);
        obj.setList(list);
        return obj;
    }

    @Test
    public void testOneRecord(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(null,pojo);
        database.commit();
        PojoObject next = (PojoObject) database.getByIndex(index,PojoObject.class);
        Assert.assertEquals(pojo,next);
    }

    @Test
    public void testOneRecordNoCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(null,pojo);
        PojoObject next = (PojoObject) database.getByIndex(index,PojoObject.class);
        Assert.assertEquals(pojo,next);
    }

    @Test
    public void test1000Record(){
        if(disableTests) return;
        Map<Integer,PojoObject> map = new HashMap<>();
        for(int i=0;i<1000;i++) {
            PojoObject pojo = buildPojo(i);
            int index = database.put(null, pojo);
            map.put(index,pojo);
        }
        database.commit();
        for(Map.Entry<Integer,PojoObject> entry:map.entrySet()){
            PojoObject next = (PojoObject) database.getByIndex(entry.getKey(),PojoObject.class);
            Assert.assertEquals(entry.getValue(), next);
        }
    }

    @Test
    public void test1000RecordNoCommit(){
        if(disableTests) return;
        Map<Integer,PojoObject> map = new HashMap<>();
        for(int i=0;i<1000;i++) {
            PojoObject pojo = buildPojo(i);
            int index = database.put(null, pojo);
            map.put(index,pojo);
        }
        for(Map.Entry<Integer,PojoObject> entry:map.entrySet()){
            PojoObject next = (PojoObject) database.getByIndex(entry.getKey(),PojoObject.class);
            Assert.assertEquals(entry.getValue(), next);
        }
    }

    @Test
    public void test1000RecordLoad(){
        if(disableTests) return;
        Map<Integer,PojoObject> map = new HashMap<>();
        for(int i=0;i<1000;i++) {
            PojoObject pojo = buildPojo(i);
            int index = database.put(null, pojo);
            map.put(index,pojo);
        }
        database.close();
        database = new DataStore();
        for(Map.Entry<Integer,PojoObject> entry:map.entrySet()){
            PojoObject next = (PojoObject) database.getByIndex(entry.getKey(),PojoObject.class);
            Assert.assertEquals(entry.getValue(), next);
        }
    }

    @Test
    public void test100000Record(){
        if(disableTests) return;
        long start = System.currentTimeMillis();
        Map<Integer,PojoObject> map = new HashMap<>();
        for(int i=0;i<100000;i++) {
            PojoObject pojo = buildPojo(i);
            int index = database.put(null, pojo);
            map.put(index,pojo);
        }
        database.commit();
        long end = System.currentTimeMillis();
        System.out.println("Time to write 100000 records="+(end-start));
        int i =0;
        for(Map.Entry<Integer,PojoObject> entry:map.entrySet()){
            if(i%10000==0) {
                System.out.println(i);
            }
            PojoObject next = (PojoObject) database.getByIndex(entry.getKey(),PojoObject.class);
            Assert.assertEquals(entry.getValue(), next);
            i++;
        }
    }

    @Test
    public void test100000RecordNoCommit(){
        if(disableTests) return;
        long start = System.currentTimeMillis();
        Map<Integer,PojoObject> map = new HashMap<>();
        for(int i=0;i<100000;i++) {
            PojoObject pojo = buildPojo(i);
            int index = database.put(null, pojo);
            map.put(index,pojo);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time to write 100000 records="+(end-start));
        int i =0;
        for(Map.Entry<Integer,PojoObject> entry:map.entrySet()){
            if(i%10000==0) {
                System.out.println(i);
            }
            PojoObject next = (PojoObject) database.getByIndex(entry.getKey(),PojoObject.class);
            Assert.assertEquals(entry.getValue(), next);
            i++;
        }
    }

    @Test
    public void testOneRecordUpdateNoOverflowWithCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(100);
        int index = database.put(100,pojo);
        database.commit();
        pojo = buildPojo(101);
        index = database.put(100,pojo);
        PojoObject next = (PojoObject) database.getByKey(100,PojoObject.class);
        Assert.assertEquals(pojo,next);
    }

    @Test
    public void testOneRecordUpdateNoOverflowWithoutCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(100);
        int index = database.put(100,pojo);
        pojo = buildPojo(101);
        index = database.put(100,pojo);
        PojoObject next = (PojoObject) database.getByKey(100,PojoObject.class);
        Assert.assertEquals(pojo,next);
    }

    @Test
    public void testOneRecordUpdateOverflowWithCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(100);
        int index = database.put(100,pojo);
        database.commit();
        pojo = buildPojo(1001);
        index = database.put(100,pojo);
        database.commit();
        PojoObject next = (PojoObject) database.getByKey(100,PojoObject.class);
        Assert.assertEquals(pojo,next);
    }

    @Test
    public void testDeleteByIndexNoCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(null,pojo);
        PojoObject next = (PojoObject) database.deleteByIndex(index,PojoObject.class);
        Assert.assertEquals(pojo,next);
        PojoObject deleted = (PojoObject) database.getByIndex(index,PojoObject.class);
        Assert.assertNull(deleted);
    }

    @Test
    public void testDeleteByIndexWithCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(null,pojo);
        database.commit();
        PojoObject next = (PojoObject) database.deleteByIndex(index,PojoObject.class);
        Assert.assertEquals(pojo,next);
        PojoObject deleted = (PojoObject) database.getByIndex(index,PojoObject.class);
        Assert.assertNull(deleted);
    }

    @Test
    public void testDeleteByKeyNoCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(100,pojo);
        PojoObject next = (PojoObject) database.deleteByKey(100,PojoObject.class);
        Assert.assertEquals(pojo,next);
        PojoObject deleted = (PojoObject) database.getByKey(100,PojoObject.class);
        Assert.assertNull(deleted);
    }

    @Test
    public void testDeleteByKeyWithCommit(){
        if(disableTests) return;
        PojoObject pojo = buildPojo(0);
        int index = database.put(100,pojo);
        database.commit();
        PojoObject next = (PojoObject) database.deleteByKey(100,PojoObject.class);
        Assert.assertEquals(pojo,next);
        PojoObject deleted = (PojoObject) database.getByKey(100,PojoObject.class);
        Assert.assertNull(deleted);
    }

    @Test
    public void testJDBCTopLevelObjectQuery() throws SQLException, IOException {
        if(disableTests) return;
        for(int i=0;i<10000;i++){
            Object pojo = buildPojo(i);
            database.put(null,pojo);
        }
        database.commit();
        database.startJDBC();

        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuilder buff = new StringBuilder();

        conn = driver.connect("127.0.0.1", null);
        st = conn.createStatement();
        String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject;";
        rs = st.executeQuery(sql);
        int colCount = rs.getMetaData().getColumnCount();
        buff.append("\"");
        for(int i=1;i<=colCount;i++){
            buff.append(rs.getMetaData().getColumnLabel(i));
            if(i<colCount)
                buff.append("\",\"");
            else
                buff.append("\"\n");
        }
        while(rs.next()){
            buff.append("\"");
            for(int i=1;i<=colCount;i++){
                buff.append(rs.getObject(i));
                if(i<colCount)
                    buff.append("\",\"");
                else
                    buff.append("\"\n");
            }
        }
        checkJDBCQueryResult("./src/test/resources/jdbc-test.csv",buff,true);
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
    }

    @Test
    public void testJDBCTopLevelObjectQueryWithCriteria() throws SQLException, IOException {
        if(disableTests) return;
        for(int i=0;i<10000;i++){
            Object pojo = buildPojo(i);
            database.put(null,pojo);
        }
        database.commit();
        database.startJDBC();

        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuilder buff = new StringBuilder();

        conn = driver.connect("127.0.0.1", null);
        st = conn.createStatement();
        String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject where TestString='Name-657';";
        rs = st.executeQuery(sql);
        int colCount = rs.getMetaData().getColumnCount();
        buff.append("\"");
        for(int i=1;i<=colCount;i++){
            buff.append(rs.getMetaData().getColumnLabel(i));
            if(i<colCount)
                buff.append("\",\"");
            else
                buff.append("\"\n");
        }
        while(rs.next()){
            buff.append("\"");
            for(int i=1;i<=colCount;i++){
                buff.append(rs.getObject(i));
                if(i<colCount)
                    buff.append("\",\"");
                else
                    buff.append("\"\n");
            }
        }
        checkJDBCQueryResult("./src/test/resources/jdbc-test2.csv",buff,true);
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
    }

    @Test
    public void testJDBCSecondLevelObjectQuery() throws SQLException, IOException {
        if(disableTests) return;
        for(int i=0;i<10000;i++){
            Object pojo = buildPojo(i);
            database.put(null,pojo);
        }
        database.commit();
        database.startJDBC();

        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuilder buff = new StringBuilder();

        conn = driver.connect("127.0.0.1", null);
        st = conn.createStatement();
        String sql = "Select Name from SubPojoList;";
        rs = st.executeQuery(sql);
        int colCount = rs.getMetaData().getColumnCount();
        buff.append("\"");
        for(int i=1;i<=colCount;i++){
            buff.append(rs.getMetaData().getColumnLabel(i));
            if(i<colCount)
                buff.append("\",\"");
            else
                buff.append("\"\n");
        }
        while(rs.next()){
            buff.append("\"");
            for(int i=1;i<=colCount;i++){
                buff.append(rs.getObject(i));
                if(i<colCount)
                    buff.append("\",\"");
                else
                    buff.append("\"\n");
            }
        }

        checkJDBCQueryResult("./src/test/resources/jdbc-test1.csv",buff,true);

        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
    }

    public void checkJDBCQueryResult(String fileName,StringBuilder buff,boolean check) throws IOException {
        File f = new File(fileName);
        if(!check) {
            FileOutputStream out = new FileOutputStream(f);
            out.write(buff.toString().getBytes());
            out.close();
        }else {
            FileInputStream in = new FileInputStream(f);
            byte data[] = new byte[(int) f.length()];
            in.read(data);
            in.close();
            String before = new String(data);
            Assert.assertEquals(before, buff.toString());
        }
    }

    /*
    @Test
    public void testPojoPersistency(){
        List<PojoObject> pojos = new ArrayList<PojoObject>(10000);
        for(int i=0;i<10000;i++){
            PojoObject before = buildPojo(123);
            database.put(null, before);
            pojos.add(before);
        }
        for(int i=0;i<pojos.size();i++){
            //PojoObject after = (PojoObject)database.read(PojoObject.class, i);
            //Assert.assertEquals(true, isEqual(pojos.get(i),after,database.getTypeDescriptorsContainer()));
        }
    }

    @Test
    public void testPojoPersistencyCloseDB(){
        List<PojoObject> pojos = new ArrayList<PojoObject>(10000);
        for(int i=0;i<10000;i++){
            PojoObject before = buildPojo(123);
            database.write(before, i);
            pojos.add(before);
        }
        database.close();
        database = null;
        database = new ByteArrayObjectDataStore("POJOStoreTest",true);
        for(int i=0;i<pojos.size();i++){
            PojoObject after = (PojoObject)database.read(PojoObject.class, i);
            Assert.assertEquals(true, isEqual(pojos.get(i),after,database.getTypeDescriptorsContainer()));
        }
    }

    public static boolean isEqual(Object o1,Object o2,TypeDescriptorsContainer container){
        TypeDescriptor td1 = container.getTypeDescriptorByObject(o1);
        TypeDescriptor td2 = container.getTypeDescriptorByObject(o2);
        if(!td1.getTypeClass().equals(td2.getTypeClass()))
            return false;
        for(AttributeDescriptor ad:td1.getAttributes()){
            Object v1 = ad.get(o1, null, td1.getTypeClass());
            Object v2 = ad.get(o2, null, td1.getTypeClass());
            if(v1==null && v2==null)
                continue;
            if(v1==null && v2!=null)
                return false;
            if(v1!=null && v2==null)
                return false;
            if(container.hasTypeDescriptor(v1)){
                if(!isEqual(v1, v2,container))
                    return false;
            }else
            if(!v1.equals(v2))
                return false;
        }
        return true;
    }

    @Test
    public void testJDBC(){
        for(int i=0;i<10000;i++){
            Object pojo = buildPojo(i);
            database.write(pojo, -1);
        }
        database.commit();

        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuffer buff = new StringBuffer();
        try{
            conn = driver.connect("127.0.0.1", null);
            st = conn.createStatement();
            String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject;";
            rs = st.executeQuery(sql);
            int colCount = rs.getMetaData().getColumnCount();
            buff.append("\"");
            for(int i=1;i<=colCount;i++){
                buff.append(rs.getMetaData().getColumnLabel(i));
                if(i<colCount)
                    buff.append("\",\"");
                else
                    buff.append("\"\n");
            }
            while(rs.next()){
                buff.append("\"");
                for(int i=1;i<=colCount;i++){
                    buff.append(rs.getObject(i));
                    if(i<colCount)
                        buff.append("\",\"");
                    else
                        buff.append("\"\n");
                }
            }
            File f = new File("./src/test/resources/jdbc-test.csv");
/*
            FileOutputStream out = new FileOutputStream(f);
            out.write(buff.toString().getBytes());
            out.close();
*//*
            FileInputStream in = new FileInputStream(f);
            byte data[] = new byte[(int)f.length()];
            in.read(data);
            in.close();
            String before = new String(data);
            Assert.assertEquals(before,buff.toString());
        }catch(Exception err){
            err.printStackTrace();
        }
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
    }

    @Test
    public void testJDBCMultipleNodes(){
        database.close();
        System.out.println("Waiting 5 for db to close..");
        try{Thread.sleep(5000);}catch(Exception err){}
        ByteArrayObjectDataStore stores[] = new ByteArrayObjectDataStore[5];
        int recCountPerStore = 10000;
        for(int j=0;j<stores.length;j++){
            stores[j] = new ByteArrayObjectDataStore("POJODB-"+j,true);
            for(int i=0;i<recCountPerStore;i++){
                Object pojo = buildPojo(j*recCountPerStore+i);
                stores[j].write(pojo, -1);
            }
            stores[j].commit();
        }

        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try{
            conn = driver.connect("127.0.0.1", null);
            st = conn.createStatement();
            String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject;";
            rs = st.executeQuery(sql);
            int count = 0;
            while(rs.next()){
                count++;
            }
            System.out.println("Finish");
            Assert.assertEquals(recCountPerStore*5,count);
        }catch(Exception err){
            err.printStackTrace();
        }
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
        for(int i=0;i<stores.length;i++){
            stores[i].close();
            stores[i].deleteDatabase();
        }
    }

    public static void main(String args[]){
    	ByteArrayObjectDataStore database[] = new ByteArrayObjectDataStore[5];
    	for(int j=0;j<5;j++){
    		database[j] = new ByteArrayObjectDataStore("test-"+j,true);
	        for(int i=0;i<10;i++){
	            Object pojo = buildPojo(j*10+i);
	            database[j].write(pojo, -1);
	        }
	        database[j].commit();
    	}
        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuffer buff = new StringBuffer();
        try{
            conn = driver.connect("127.0.0.1", null);
            st = conn.createStatement();
            String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject;";
            rs = st.executeQuery(sql);
            int colCount = rs.getMetaData().getColumnCount();
            buff.append("\"");
            for(int i=1;i<=colCount;i++){
                buff.append(rs.getMetaData().getColumnLabel(i));
                if(i<colCount)
                    buff.append("\",\"");
                else
                    buff.append("\"\n");
            }
            int count=0;
            while(rs.next()){
                buff.append("\"");
                for(int i=1;i<=colCount;i++){
                    buff.append(rs.getObject(i));
                    if(i<colCount)
                        buff.append("\",\"");
                    else
                        buff.append("\"\n");
                }
                count++;
            }
            System.out.println(buff);
            System.out.println("Finish "+count);
        }catch(Exception err){
            err.printStackTrace();
        }
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
        for(int i=0;i<database.length;i++){
            database[i].close();
            database[i].deleteDatabase();
        }                    
    }

    public static void main2(String args[]){
        {
        TypeDescriptor.REGENERATE_SERIALIZERS = false;
        TypeDescriptorsContainer container = new TypeDescriptorsContainer("./JSONTest");
        PojoObject obj = buildPojo(254);
        TypeDescriptor td = container.getTypeDescriptorByObject(obj);
        JsonEncodeDataContainer json = new JsonEncodeDataContainer(td);
        json.getEncoder().encodeObject(obj, json);
        System.out.println(json.toJSON(0));
        }
        {
        TypeDescriptorsContainer container = new TypeDescriptorsContainer("./XMLTest");
        PojoObject obj = buildPojo(254);
        TypeDescriptor td = container.getTypeDescriptorByObject(obj);
        XMLEncodeDataContainer xml = new XMLEncodeDataContainer(td);
        xml.getEncoder().encodeObject(obj, xml);
        System.out.println(xml.toXML(0));
        }
    }*/
}
