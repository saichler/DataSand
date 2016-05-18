/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

import java.util.ArrayList;
import java.util.List;
import org.datasand.codec.serialize.SerializerGenerator;

/**
 * @author Sharon Aicler (saichler@gmail.com)
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
    private final String packageName;
    private StringBuilder javaCode = new StringBuilder();


    public YangNode(String data,int startPoint,String packageName,NameAndType nameAndType){
        this.startPoint = startPoint;
        this.data = data;
        this.packageName = packageName;
        this.endPoint = data.length();
        this.name = nameAndType.name;
        this.type = nameAndType.type;
    }

    public int buildElement(String packageName){
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
                NameAndType nameAndType = getNameAndType(data,index1+1);
                YangNode subNode = null;
                switch (nameAndType.type) {
                    case module:
                        subNode = new ModuleNode(data,index1+1,nameAndType);
                        break;
                    case grouping:
                        subNode = new GroupingNode(data,index1+1,packageName,nameAndType);
                        break;
                    case list:
                        subNode = new ListNode(data,index1+1,packageName,nameAndType);
                        break;
                    case leaf:
                        subNode = new LeafNode(data,index1+1,packageName,nameAndType);
                        break;
                    case revision:
                        subNode = new RevisionNode(data,index1+1,packageName,nameAndType);
                        break;
                    case container:
                        subNode = new ContainerNode(data,index1+1,packageName,nameAndType);
                        break;
                }

                children.add(subNode);
                index1 = subNode.buildElement(packageName);
                index2 = data.indexOf("{",index1+1);
                index3 = data.indexOf("}",index1+1);
            }
        }
        return -1;
    }

    public String getFormatedName(){
        return YangParser.formatElementName(this.getName());
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("\nNAME:"+this.name +" Tag Type:"+this.type+"\nVALUE=\n"+this.data.substring(this.valuePoint,this.endPoint+1));
        for(YangNode child:this.children){
            sb.append(child.toString());
        }
        return sb.toString();
    }

    public static final NameAndType getNameAndType(String data,int startPoint){
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

        String str = data.substring(startIndex,index);

        startPoint = -1;
        index = -1;
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

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    protected void app(String text, int level){
        SerializerGenerator.append(text,level,this.javaCode);
    }

    public StringBuilder getJavaCode() {
        return javaCode;
    }

    public static class NameAndType {
        private final String name;
        private final YangTagEnum type;
        public NameAndType(String name, YangTagEnum type){
            this.name = name;
            this.type = type;
        }
    }

    protected void appendPackageName(){
        app("package "+this.getPackageName()+";",0);
    }

    protected void appendInterfaceName(){
        app("public interface "+this.getFormatedName()+" {",0);
        app("}",0);
    }
}
