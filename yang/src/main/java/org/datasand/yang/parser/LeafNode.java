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
 * Created by saichler on 5/10/16.
 */
public class LeafNode extends YangNode {
    public LeafNode(String data, int startPoint, NameAndType nameAndType){
        super(data,startPoint,nameAndType);
    }
    public String getType(){
        String type = YangParser.extractValue("type ",this.startPoint,";",this.data,false).toLowerCase();
        if(type.equals("binary")){
            return boolean.class.getSimpleName();
        }else
        if(type.startsWith("decimal64")){
            return double.class.getSimpleName();
        }else
        if(type.equals("string")){
            return String.class.getSimpleName();
        } else if (type.equals("int32")){
            return int.class.getSimpleName();
        } else if (type.equals("int16")){
            return short.class.getSimpleName();
        } else if (type.equals("int8")){
            return short.class.getSimpleName();
        } else if (type.equals("int64")){
            return long.class.getSimpleName();
        } else if (YangManager.getJavaTypeFromYangType(type)!=null){
            return YangManager.getJavaTypeFromYangType(type);
        }
        throw new IllegalArgumentException("Unknown type "+type);
    }
}
