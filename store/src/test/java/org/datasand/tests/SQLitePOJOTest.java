package org.datasand.tests;

import java.util.ArrayList;
import java.util.List;

import org.datasand.store.fortest.PojoObject;
import org.datasand.store.fortest.SubPojoList;
import org.datasand.store.fortest.SubPojoObject;
import org.datasand.store.sqlite.SQLiteObjectStore;

public class SQLitePOJOTest {

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

    public static void main(String args[]){
        SQLiteObjectStore db = new SQLiteObjectStore();
        PojoObject obj = buildPojo(0);
        db.write(obj, -1);
    }
}
