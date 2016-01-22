/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.fortest;

import java.io.File;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/22/16.
 */
public class CleanTargetDirectory {
    public static void main(String args[]){
        File f = new File("/root");
        scanAndDeleteTargetDirectory(f);
    }

    private static void scanAndDeleteTargetDirectory(File f){
        if(f.getName().equals("target")){
            System.out.println("Deleting "+f.getAbsolutePath());
            deleteDirectory(f);
        }else
        if(f.isDirectory()){
            File files[] = f.listFiles();
            for(File file:files){
                if(file.isDirectory()){
                    scanAndDeleteTargetDirectory(file);
                }
            }
        }
    }

    private static void deleteDirectory(File dir){
        File files[] = dir.listFiles();
        if(files!=null){
            for(File file:files){
                if(file.isDirectory()){
                    deleteDirectory(file);
                }else{
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
