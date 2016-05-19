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
public class TypeDefNode extends YangNode {
    public TypeDefNode(String data, int startPoint, YangNodeAttributes yangNodeAttributes){
        super(data,startPoint, yangNodeAttributes);
        YangManager.addType(yangNodeAttributes.getName(),this.getFormatedName());
    }

    public void generateCode(){
        appendPackageName();
        appendImports();
        appendInterfaceName();
        appendChildrenMethods();
        if(this.children.size()==0){
            String type = LeafNode.getType(this);
            app("public void setValue("+type+" value);",1);
            app("public "+type+" getValue();",1);
        }
        app("\n}",0);
    }

}
