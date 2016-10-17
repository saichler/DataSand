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
import org.datasand.codec.VLogger;
import org.datasand.microservice.Message;
import org.datasand.network.NID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class QueryContainer {
    private final Map<NID,Boolean> destToFinish = new HashMap<NID,Boolean>();
    private final NID source;
    private final Message msg;
    private final Connection connection;
    public QueryContainer(Connection connection, NID _source, ResultSet _rs){
        if(_source==null){
            VLogger.error("Source Cannot be null",null);
        }
        this.connection = connection;
        this.source = _source;
        msg = new JDBCMessage(_rs,0,0);
        connection.multicast(msg);
        connection.addARPJournal(msg,true);
    }

    public NID getSource() {
        return source;
    }

    public Message getMsg() {
        return msg;
    }
}
