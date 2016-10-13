/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saichler on 10/13/16.
 */
public class RoutingTable {
    private final Map<NetUUID,ConnectionID> netUUID2ConnectionID = new HashMap<>();

    public void add(NetUUID uuid,ConnectionID connID){
        this.netUUID2ConnectionID.put(uuid,connID);
    }

    public ConnectionID get(NetUUID netUUID){
        return this.netUUID2ConnectionID.get(netUUID);
    }
}
