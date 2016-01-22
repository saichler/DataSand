/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class JDBCRecord {
    private final Map<String,Object> data = new HashMap<>();
    public JDBCRecord(){}
    public void addValue(String columnName,Object value){
        this.data.put(columnName,value);
    }
    public Map<String,Object> getData(){
        return this.data;
    }
}
