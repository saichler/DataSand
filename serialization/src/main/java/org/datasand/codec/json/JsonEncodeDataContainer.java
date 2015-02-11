package org.datasand.codec.json;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.datasand.codec.EncodeDataContainer;
import org.datasand.codec.TypeDescriptor;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JsonEncodeDataContainer extends EncodeDataContainer {

    private List<JSONEntry> properties = new LinkedList<JSONEntry>();

    public JsonEncodeDataContainer(TypeDescriptor _typeDescriptor) {
        super(_typeDescriptor, EncodeDataContainer.ENCODER_TYPE_JSON);
    }

    @Override
    public void resetLocation() {
    }

    public void addEntry(String name,Object obj){
        this.properties.add(new JSONEntry(name,obj));
    }

    private class JSONEntry{
        public String name;
        public String value;
        public JSONEntry(String _name,Object _value){
            this.name = _name;
            if(_value!=null)
                this.value = _value.toString();
            else
                this.value="";
        }
        public String toJSON(int level){
            if(this.name.equals("PP")) return "";
            StringBuffer buff = new StringBuffer();
            appendTab(buff, level);
            buff.append("\"");
            buff.append(this.name).append("\": ");
            buff.append(this.value);
            return buff.toString();
        }
    }

    public static void appendTab(StringBuffer b,int level){
        for(int i=0;i<level;i++){
            b.append("  ");
        }
    }

    public String toJSON(int level){
        StringBuffer buff = new StringBuffer();
        if(level>0)
            appendTab(buff, level+1);
        buff.append("{\n");
        Map<Integer, List<EncodeDataContainer>> subElements = this.getSubElementsData();
        int count = 0;
        for(JSONEntry entry:properties){
            if(level==0)
                buff.append(entry.toJSON(1));
            else
                buff.append(entry.toJSON(level+2));
            count++;
            if(count<properties.size() || !subElements.isEmpty())
                buff.append(",\n");
            else
                buff.append("\n");
        }
        count = 0;
        for(Map.Entry<Integer,List<EncodeDataContainer>> entry:subElements.entrySet()){
            appendTab(buff, level+1);
            buff.append("\"");
            String attributeName = this.getTypeDescriptorContainer().getTypeDescriptorByCode(entry.getKey()).getTypeClassShortName();
            buff.append(attributeName).append("\": [\n");
            int subCount = 0;
            for(EncodeDataContainer edc:entry.getValue()){
                buff.append(((JsonEncodeDataContainer)edc).toJSON(level+1));
                subCount++;
                if(subCount<entry.getValue().size())
                    buff.append(",");
                buff.append("\n");
            }
            appendTab(buff, level+1);
            buff.append("]");
            count++;
            if(count<subElements.size())
                buff.append(",");
            buff.append("\n");
        }
        if(level>0)
            appendTab(buff, level+1);
        buff.append("}");
        return buff.toString();
    }
}
