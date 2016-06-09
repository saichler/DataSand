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
import org.datasand.network.HabitatID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 *
 * Message is the vessel which is used to transfer data from one node to another.
 */
public class ServiceInventory extends Message {
    private Map<Integer,List<HabitatID>> services = new HashMap<>();
    private HabitatID habitatID = null;

    public void setHabitatID(HabitatID id){
        this.habitatID = id;
    }

    public void addService(int serviceGroup,HabitatID id){
        List<HabitatID> serviceList = services.get(serviceGroup);
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
        Encoder.encodeObject(si.habitatID,ba);
        Encoder.encodeInt16(si.services.size(),ba);
        for(Map.Entry<Integer,List<HabitatID>> entry:si.services.entrySet()){
            Encoder.encodeInt16(entry.getKey(),ba);
            Encoder.encodeInt16(entry.getValue().size(),ba);
            for(HabitatID id:entry.getValue()){
                Encoder.encodeObject(id,ba);
            }
        }
    }

    @Override
    public Object decode(BytesArray ba) {
        ServiceInventory serviceInventory = new ServiceInventory();
        ServiceInventory si = (ServiceInventory)super.decode(ba);
        si.habitatID = (HabitatID)Encoder.decodeObject(ba);
        int size = Encoder.decodeInt16(ba);
        for(int i=0;i<size;i++){
            int group = Encoder.decodeInt16(ba);
            int groupSize = Encoder.decodeInt16(ba);
            List<HabitatID> ids = new ArrayList<>(groupSize);
            si.services.put(group,ids);
            for(int j=0;j<groupSize;j++){
                ids.add((HabitatID)Encoder.decodeObject(ba));
            }
        }
        return si;
    }
}
