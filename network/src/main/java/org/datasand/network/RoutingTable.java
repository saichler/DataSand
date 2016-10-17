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
    private final Map<Integer,Map<Long,Map<Long,ConnectionID>>> routingTree = new HashMap<>();

    public void add(NID NID, ConnectionID connID){
        Map<Long,Map<Long,ConnectionID>> network = routingTree.get(NID.getNetwork());
        if(network==null){
            network = new HashMap<>();
            routingTree.put(NID.getNetwork(),network);
        }

        Map<Long,ConnectionID> uuidAMap = network.get(NID.getUuidA());
        if(uuidAMap==null){
            uuidAMap = new HashMap<>();
            network.put(NID.getUuidA(),uuidAMap);
        }

        uuidAMap.put(NID.getUuidB(),connID);
    }

    public ConnectionID get(NID NID) {
        Map<Long,Map<Long,ConnectionID>> network = routingTree.get(NID.getNetwork());
        if(network!=null){
            Map<Long,ConnectionID> uuidAMap = network.get(NID.getUuidA());
            if(uuidAMap!=null){
                return uuidAMap.get(NID.getUuidB());
            }
        }
        return null;
    }
}
