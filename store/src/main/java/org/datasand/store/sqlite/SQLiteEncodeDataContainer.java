package org.datasand.store.sqlite;

import java.util.HashMap;
import java.util.Map;

import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SQLiteEncodeDataContainer extends EncodeDataContainer{

    static{
        EncodeDataContainer.registerEncoder(EncodeDataContainer.ENCODER_TYPE_SQLITE, new SQLiteEncoder());
    }

    private Map<String,Entry> entries = new HashMap<String,Entry>();

    public SQLiteEncodeDataContainer(TypeDescriptor _typeDescrptor){
        super(_typeDescrptor,EncodeDataContainer.ENCODER_TYPE_SQLITE);
    }

    public void addEntry(String name,Object obj){
        this.entries.put(name,new Entry(name,obj));
    }

    public Object getEntryValue(EncodeDataContainer ba){
        Entry e = entries.get(ba.getCurrentAttributeName());
        if(e!=null)
            return e.value;
        else
            return null;
    }

    @Override
    public void resetLocation() {
    }

    private static class Entry {
        private String name;
        private Object value;
        public Entry(String _name,Object _value){
            this.name = _name;
            this.value = _value;
        }
    }
}
