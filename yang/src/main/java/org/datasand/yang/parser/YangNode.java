/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    protected final List<YangNode> children = new ArrayList<>();
    private StringBuilder javaCode = new StringBuilder();
    protected final YangNodeAttributes yangNodeAttributes;

    public YangNode(String data,int startPoint,YangNodeAttributes yangNodeAttributes){
        this.startPoint = startPoint;
        this.data = data;
        this.endPoint = data.length();
        this.valuePoint = data.indexOf("{",startPoint);
        this.yangNodeAttributes = yangNodeAttributes;
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
                YangNodeAttributes yangNodeAttributes = YangNodeAttributes.getNameAndType(data,index1+1);
                yangNodeAttributes.setPackageName(this.yangNodeAttributes.getPackageName());
                yangNodeAttributes.setFilePath(this.yangNodeAttributes.getFilePath());
                YangNode subNode = null;
                switch (yangNodeAttributes.getType()) {
                    case identity:
                        subNode = new IdentityNode(data,index1+1, yangNodeAttributes);
                        break;
                    case augment:
                        subNode = new AugmentNode(data,index1+1, yangNodeAttributes);
                        break;
                    case _enum:
                        subNode = new EnumNode(data,index1+1, yangNodeAttributes);
                        break;
                    case enumeration:
                        subNode = new EnumerationNode(data,index1+1, yangNodeAttributes);
                        break;
                    case typedef:
                        subNode = new TypeDefNode(data,index1+1, yangNodeAttributes);
                        break;
                    case _import:
                        subNode = new ImportNode(data,index1+1, yangNodeAttributes);
                        break;
                    case module:
                        subNode = new ModuleNode(data,index1+1, yangNodeAttributes);
                        break;
                    case grouping:
                        subNode = new GroupingNode(data,index1+1, yangNodeAttributes);
                        break;
                    case list:
                        subNode = new ListNode(data,index1+1, yangNodeAttributes);
                        break;
                    case leaf:
                        subNode = new LeafNode(data,index1+1, yangNodeAttributes);
                        break;
                    case revision:
                        subNode = new RevisionNode(data,index1+1, yangNodeAttributes);
                        break;
                    case container:
                        subNode = new ContainerNode(data,index1+1, yangNodeAttributes);
                        break;
                    case type:
                    case bit:
                        subNode = new TypeNode(data,index1+1, yangNodeAttributes);
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
        StringBuilder sb = new StringBuilder("\nNAME:"+this.yangNodeAttributes.getName() +" Tag Type:"+this.yangNodeAttributes.getType()+"\nVALUE=\n"+this.data.substring(this.valuePoint,this.endPoint+1));
        for(YangNode child:this.children){
            sb.append(child.toString());
        }
        return sb.toString();
    }

    public String getPackageName() {
        return yangNodeAttributes.getPackageName();
    }

    public String getName() {
        return yangNodeAttributes.getName();
    }

    protected void app(String text, int level){
        SerializerGenerator.append(text,level,this.javaCode);
    }

    public StringBuilder getJavaCode() {
        return javaCode;
    }


    public void generateCode(){
        appendPackageName();
        appendImports();
        appendInterfaceName();
        appendChildrenMethods();
        app("\n}",0);
    }

    public void print(){
        switch(this.yangNodeAttributes.getType()){
            case augment:
            case typedef:
            case container:
            case list:
            case grouping:
                System.out.println(this.javaCode);
                File dir = new File(this.yangNodeAttributes.getFilePath());
                if(!dir.exists()){
                    dir.mkdirs();
                }

                File f = new File(dir,this.yangNodeAttributes.getFileName());
                try{
                    FileOutputStream out = new FileOutputStream(f);
                    out.write(this.getJavaCode().toString().getBytes());
                    out.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
        }
        for(YangNode child:this.children){
            child.print();
        }
    }

    protected void appendPackageName(){
        app("package "+this.getPackageName()+";\n",0);
    }

    protected void appendImports() {
        app("import java.util.List;",0);
        app("",0);
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
            switch(child.yangNodeAttributes.getType()){
                case typedef:
                case augment:
                    child.generateCode();
                    break;
                case list:
                    app("public void set"+child.getFormatedName()+"(List<"+child.getFormatedName()+"> "+child.getFormatedName().toLowerCase()+");",1);
                    app("public List<"+child.getFormatedName()+"> get"+child.getFormatedName()+"();",1);
                    app("public void add"+child.getFormatedName()+"("+child.getFormatedName()+" "+child.getFormatedName().toLowerCase()+");",1);
                    app("public "+child.getFormatedName()+" del"+child.getFormatedName()+"();",1);
                    app("",0);
                    child.generateCode();
                    break;
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
                    String type = LeafNode.getType(child);
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
