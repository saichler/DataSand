/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 5/10/16.
 */
public class YangManager {

    private static final Map<String,String> yangType2JavaType = new HashMap<>();

    public static void main(String args[]){
        List<File> yangFiles = new LinkedList<>();
        findYangFiles(new File("./"),yangFiles);
        for(File yangFile:yangFiles){
            try {
                YangParser parser = new YangParser(yangFile);
                parser.build();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static final void findYangFiles(File dir,List<File> yangFiles){
        File files[] = dir.listFiles();
        if(files!=null){
            for(File f:files){
                if(f.isDirectory()){
                    findYangFiles(f,yangFiles);
                }else if(f.getName().endsWith(".yang")){
                    yangFiles.add(f);
                }
            }
        }
    }

    public static void addType(String yangType,String javaType){
        yangType2JavaType.put(yangType.toLowerCase(),javaType);
    }

    public static String getJavaTypeFromYangType(String yangType){
        return yangType2JavaType.get(yangType.toLowerCase());
    }
}
