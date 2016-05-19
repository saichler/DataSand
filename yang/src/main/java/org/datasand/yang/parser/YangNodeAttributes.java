/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 5/19/16.
 */
public class YangNodeAttributes {
    private final String name;
    private final YangTagEnum type;
    private String packageName = null;
    private String filePath = null;
    private String fileName;

    public YangNodeAttributes(String name, YangTagEnum type){
        this.name = name;
        this.type = type;
        this.fileName = YangParser.formatElementName(this.name)+".java";
    }

    public static final YangNodeAttributes getNameAndType(String data, int startPoint){
        int index = data.indexOf("{",startPoint);
        String prevData = data.substring(0,index);
        int startIndex1 = prevData.lastIndexOf(";");
        int startIndex2 = prevData.lastIndexOf("}");
        int startIndex3 = prevData.lastIndexOf("{");
        int startIndex = 0;
        if(startIndex1!=-1){
            startIndex = startIndex1;
        }

        if(startIndex2>startIndex) {
            startIndex = startIndex2;
        }

        if(startIndex3>startIndex) {
            startIndex = startIndex3;
        }

        String str = data.substring(startIndex,index);

        index = -1;
        YangTagEnum enums[] = YangTagEnum.values();

        boolean found = false;
        int x = -1;
        int y = -1;
        for(int i=0;i<enums.length;i++){
            String enumName = enums[i].name();
            if(enumName.startsWith("_")){
                enumName = enumName.substring(1);
            }
            x = str.lastIndexOf(enumName);
            if(x==-1){
                continue;
            }
            y = str.indexOf(enumName);
            if(y<x){
                x = y;
            }
            if(x+startIndex>=startPoint){
                found = true;
                startPoint = x;
                index = i;
                break;
            }
        }

        if(index==-1){
            throw new IllegalArgumentException("Can't figure out node type from "+str);
        }
        return new YangNodeAttributes(str.substring(x+enums[index].name().length()).trim(),enums[index]);
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
