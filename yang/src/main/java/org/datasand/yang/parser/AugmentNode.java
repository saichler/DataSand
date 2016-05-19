/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.yang.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 5/10/16.
 */
public class AugmentNode extends YangNode {

    private static final Map<String,Integer> augmentationCount = new HashMap<>();
    private final String formattedName;

    public AugmentNode(String data, int startPoint, YangNodeAttributes yangNodeAttributes){
        super(data,startPoint, yangNodeAttributes);
        Integer count = augmentationCount.get(this.getName());
        if(count==null){
            count = new Integer(0);
        }
        count++;
        augmentationCount.put(this.getName(),count);
        String name = this.getName().substring(2,this.getName().length()-1);
        this.formattedName = YangParser.formatElementName(name)+"Augmentation"+count;
        this.yangNodeAttributes.setFileName(this.formattedName+".java");

    }

    @Override
    public String getFormatedName() {
        return this.formattedName;
    }
}
