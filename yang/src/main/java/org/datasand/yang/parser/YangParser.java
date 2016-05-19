/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 5/10/16.
 */
public class YangParser {

    private final File yangFile;
    private final String fileContent;

    public YangParser(File yangFile) throws IOException{
        this.yangFile = yangFile;
        byte data[] = new byte[(int)this.yangFile.length()];
        FileInputStream in = new FileInputStream(this.yangFile);
        in.read(data);
        in.close();
        this.fileContent = new String(data);
    }

    public static final String extractValue(String lookFor,int startingAt, String terminator,String fileContent,boolean removeQuate){
        int index1 = fileContent.indexOf(lookFor,startingAt);
        int index2 = fileContent.indexOf(terminator,index1+1);
        String result = fileContent.substring(index1+lookFor.length(),index2);
        result = result.trim();
        if(removeQuate){
            return result.substring(1,result.length()-1);
        }
        return result;
    }

    public static final String extractValue(String lookFor,int startingAt, int endPoint, String terminator,String fileContent,boolean removeQuate){
        int index1 = fileContent.indexOf(lookFor,startingAt);
        int index2 = fileContent.indexOf(terminator,index1+1);
        if(index1==-1) {
            return null;
        }
        if(index2==-1) {
            return null;
        }
        if(index1>endPoint){
            return null;
        }
        String result = fileContent.substring(index1+lookFor.length(),index2);
        result = result.trim();
        if(removeQuate){
            return result.substring(1,result.length()-1);
        }
        return result;
    }

    public static final String extractFormatedValue(String lookFor,int startingAt, String terminator,String fileContent){
        int index1 = fileContent.indexOf(lookFor,startingAt);
        int index2 = fileContent.indexOf(terminator,index1+1);
        String result = fileContent.substring(index1+lookFor.length(),index2);
        return formatElementName(result);
    }

    public static final String formatElementName(String element){
        element = element.trim();
        StringBuilder sb = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(element,"-");
        while(tokens.hasMoreTokens()){
            String token = tokens.nextToken();
            token = token.toLowerCase();
            token = token.substring(0,1).toUpperCase()+token.substring(1);
            sb.append(token);
        }
        return sb.toString();
    }

    public void build(){
        YangNodeAttributes yangNodeAttributes = YangNodeAttributes.getNameAndType(this.fileContent,0);
        ModuleNode.setPackageName(yangNodeAttributes,this.fileContent,0);
        ModuleNode node = new ModuleNode(this.fileContent,0, yangNodeAttributes);
        node.buildElement(node.getPackageName());
        node.generateCode();
        node.print();
    }
}
