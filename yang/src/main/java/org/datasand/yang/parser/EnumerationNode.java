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
public class EnumerationNode extends YangNode {
    public EnumerationNode(String data, int startPoint, YangNodeAttributes yangNodeAttributes){
        super(data,startPoint, yangNodeAttributes);
    }
}
