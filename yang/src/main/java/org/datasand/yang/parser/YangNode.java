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

    protected final int startPoint;
    protected int valuePoint=-1;
    protected int endPoint=-1;
    protected final String data;
    private final List<YangNode> children = new ArrayList<>();
    private StringBuilder javaCode = new StringBuilder();
    private final NameAndType nameAndType;

    public YangNode(String data,int startPoint,NameAndType nameAndType){
        this.startPoint = startPoint;
        this.data = data;
        this.endPoint = data.length();
        this.valuePoint = data.indexOf("{",startPoint);
        this.nameAndType = nameAndType;
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
                nameAndType.setPackageName(this.nameAndType.getPackageName());
                nameAndType.setFilePath(this.nameAndType.getFilePath());
                YangNode subNode = null;
                switch (nameAndType.type) {
                    case identity:
                        subNode = new IdentityNode(data,index1+1,nameAndType);
                        break;
                    case augment:
                        subNode = new AugmentNode(data,index1+1,nameAndType);
                        break;
                    case _enum:
                        subNode = new EnumNode(data,index1+1,nameAndType);
                        break;
                    case enumeration:
                        subNode = new EnumerationNode(data,index1+1,nameAndType);
                        break;
                    case typedef:
                        subNode = new TypeDefNode(data,index1+1,nameAndType);
                        break;
                    case _import:
                        subNode = new ImportNode(data,index1+1,nameAndType);
                        break;
                    case module:
                        subNode = new ModuleNode(data,index1+1,nameAndType);
                        break;
                    case grouping:
                        subNode = new GroupingNode(data,index1+1,nameAndType);
                        break;
                    case list:
                        subNode = new ListNode(data,index1+1,nameAndType);
                        break;
                    case leaf:
                        subNode = new LeafNode(data,index1+1,nameAndType);
                        break;
                    case revision:
                        subNode = new RevisionNode(data,index1+1,nameAndType);
                        break;
                    case container:
                        subNode = new ContainerNode(data,index1+1,nameAndType);
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
        StringBuilder sb = new StringBuilder("\nNAME:"+this.nameAndType.name +" Tag Type:"+this.nameAndType.type+"\nVALUE=\n"+this.data.substring(this.valuePoint,this.endPoint+1));
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
            String enumName = enums[i].name();
            if(enumName.startsWith("_")){
                enumName = enumName.substring(1);
            }
            int x = str.lastIndexOf(enumName+" ");
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
        return nameAndType.getPackageName();
    }

    public String getName() {
        return nameAndType.getName();
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
        private String packageName = null;
        private String filePath = null;
        private final String fileName;

        public NameAndType(String name, YangTagEnum type){
            this.name = name;
            this.type = type;
            this.fileName = YangParser.formatElementName(this.name)+".java";
        }

        public String getName() {
            return name;
        }

        public YangTagEnum getType() {
            return type;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public void generateCode(){
        appendPackageName();
        appendInterfaceName();
        appendChildrenMethods();
        app("\n}",0);
    }

    public void print(){
        System.out.println(this.javaCode);
        for(YangNode child:this.children){
            child.print();
        }
    }

    protected void appendPackageName(){
        app("package "+this.getPackageName()+";",0);
    }

    protected void appendInterfaceName(){
        String uses = getUses();
        if(uses==null) {
            app("public interface " + this.getFormatedName() + " {", 0);
        } else {
            app("public interface " + this.getFormatedName() + " extends "+YangParser.formatElementName(uses)+" {", 0);
        }
    }

    protected void appendChildrenMethods(){
        app("",1);
        for(YangNode child:this.children){
            switch(child.nameAndType.getType()){
                case list:
                    app("public void set"+child.getFormatedName()+"(List<"+child.getFormatedName()+"> "+child.getFormatedName().toLowerCase()+");",1);
                    app("public List<"+child.getFormatedName()+"> get"+child.getFormatedName()+"();",1);
                    app("public void add"+child.getFormatedName()+"("+child.getFormatedName()+" "+child.getFormatedName().toLowerCase()+");",1);
                    app("public "+child.getFormatedName()+" del"+child.getFormatedName()+"();",1);
                    app("",0);
                    child.generateCode();
                case grouping:
                    child.generateCode();
                    break;
                case container:
                    app("public void set"+child.getFormatedName()+"("+child.getFormatedName()+" "+child.getFormatedName().toLowerCase()+");",1);
                    app("public "+child.getFormatedName()+" get"+child.getFormatedName()+"();",1);
                    app("",0);
                    child.generateCode();
                    break;
                case leaf:
                    String type = ((LeafNode)child).getType();
                    app("public void set"+child.getFormatedName()+"("+type+" "+child.getFormatedName().toLowerCase()+");",1);
                    app("public "+type+" get"+child.getFormatedName()+"();",1);
                    app("",0);
                    break;
            }
        }
    }

    protected String getUses(){
        int valueEndPoint = -1;
        if(this.children.isEmpty()){
            valueEndPoint = this.endPoint;
        }else{
            valueEndPoint = this.children.get(0).valuePoint;
        }
        String uses = YangParser.extractValue("uses",this.startPoint,valueEndPoint,";",this.data,false);
        return uses;
    }
}
