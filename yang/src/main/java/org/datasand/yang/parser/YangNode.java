package org.datasand.yang.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saichler on 5/16/16.
 */
public class YangNode {

    private final int startPoint;
    private int valuePoint=-1;
    private int endPoint=-1;
    private final String data;
    private final String name;
    private final YangTagEnum type;
    private final List<YangNode> children = new ArrayList<>();

    public YangNode(String data,int startPoint){
        this.startPoint = startPoint;
        this.data = data;
        int index = data.indexOf("{",startPoint);
        String prevData = data.substring(0,index);
        int startIndex1 = prevData.lastIndexOf(";");
        int startIndex2 = prevData.lastIndexOf("}");
        int startIndex = 0;
        if(startIndex1!=-1){
            startIndex = startIndex1;
        }

        if(startIndex2>startIndex) {
            startIndex = startIndex2;
        }
        this.endPoint = data.length();
        NameAndType nat = getNameAndType(data.substring(startIndex,index));
        this.name = nat.name;
        this.type = nat.type;
    }

    public int buildElement(){
        int index1 = data.indexOf("{",startPoint);
        int index2 = data.indexOf("{",index1+1);
        int index3 = data.indexOf("}",index1+1);
        this.valuePoint = index1;

        if(index3!=-1 && index2==-1){
            this.endPoint = index3;
            return this.endPoint;
        }

        while(index2!=-1 || index3!=-1) {
            if (index2 == -1 || index3 < index2) {
                this.endPoint = index3;
                return endPoint;
            } else if (index2!=-1 && index2<index3){
                YangNode subNode = new YangNode(data,index1+1);
                children.add(subNode);
                index1 = subNode.buildElement();
                index2 = data.indexOf("{",index1+1);
                index3 = data.indexOf("}",index1+1);
            }
        }
        return -1;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("\nNAME:"+this.name +" Tag Type:"+this.type+"\nVALUE=\n"+this.data.substring(this.valuePoint,this.endPoint+1));
        for(YangNode child:this.children){
            sb.append(child.toString());
        }
        return sb.toString();
    }

    private final NameAndType getNameAndType(String str){
        int startPoint = -1;
        int index = -1;
        YangTagEnum enums[] = YangTagEnum.values();
        for(int i=0;i<enums.length;i++){
            int x = str.lastIndexOf(enums[i].name()+" ");
            if(x>startPoint){
                startPoint = x;
                index = i;
            }
        }
        if(index==-1){
            throw new IllegalArgumentException("Can't figure out node type from "+str);
        }
        return new NameAndType(str.substring(startPoint+enums[index].name().length()).trim(),enums[index]);
    }

    private static class NameAndType {
        private final String name;
        private final YangTagEnum type;
        public NameAndType(String name, YangTagEnum type){
            this.name = name;
            this.type = type;
        }
    }
}
