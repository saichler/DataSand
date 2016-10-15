/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.network.NetUUID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 *
 * Message is the vessel which is used to transfer data from one node to another.
 */
public class ServiceInventory extends Message {
    private final Map<Long,List<NetUUID>> services = new HashMap<>();
    private final NetUUID netUUID;

    public ServiceInventory(int source,int destination, NetUUID uuid, Object data){
        super(source,destination,-1,data);
        this.netUUID = uuid;
    }

    public void addService(long serviceGroup,NetUUID id){
        List<NetUUID> serviceList = services.get(serviceGroup);
        if(serviceList==null){
            serviceList = new ArrayList<>();
            services.put(serviceGroup,serviceList);
        }
        serviceList.add(id);
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        super.encode(value,ba);
        ServiceInventory si = (ServiceInventory)value;
        Encoder.encodeObject(si.netUUID,ba);
        Encoder.encodeInt16(si.services.size(),ba);
        for(Map.Entry<Long,List<NetUUID>> entry:si.services.entrySet()){
            Encoder.encodeInt64(entry.getKey(),ba);
            Encoder.encodeInt16(entry.getValue().size(),ba);
            for(NetUUID id:entry.getValue()){
                Encoder.encodeObject(id,ba);
            }
        }
    }

    @Override
    public Object decode(BytesArray ba) {
        ServiceInventory serviceInventory = new ServiceInventory();
        ServiceInventory si = (ServiceInventory)super.decode(ba);
        si.netUUID = (NetUUID)Encoder.decodeObject(ba);
        int size = Encoder.decodeInt16(ba);
        for(int i=0;i<size;i++){
            long group = Encoder.decodeInt64(ba);
            int groupSize = Encoder.decodeInt16(ba);
            List<NetUUID> ids = new ArrayList<>(groupSize);
            si.services.put(group,ids);
            for(int j=0;j<groupSize;j++){
                ids.add((NetUUID)Encoder.decodeObject(ba));
            }
        }
        return si;
    }
}
