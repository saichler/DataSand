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
    public Class getType(){
        String type = YangParser.extractValue("type",this.startPoint,";",this.data,false).toLowerCase();

        if(type.equals("string")){
            return String.class;
        } else if (type.equals("int32")){
            return int.class;
        } else if (type.equals("int64")){
            return long.class;
        }
        throw new IllegalArgumentException("Unknown type"+type);
    }
}
