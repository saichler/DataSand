/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
import org.datasand.network.NID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 *         <p>
 *         Message is the vessel which is used to transfer data from one node to another.
 */
public class ServiceInventory implements ISerializer {

    private final Map<Long, List<Integer>> services = new HashMap<>();
    private final NID NID;

    public ServiceInventory(NID uuid) {
        this.NID = uuid;
    }

    public void addService(long serviceType, int id) {
        List<Integer> serviceList = services.get(serviceType);
        if (serviceList == null) {
            serviceList = new ArrayList<>();
            services.put(serviceType, serviceList);
        }
        serviceList.add(id);
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        ServiceInventory si = (ServiceInventory) value;
        Encoder.encodeObject(si.NID, ba);
        Encoder.encodeInt16(si.services.size(), ba);
        for (Map.Entry<Long, List<Integer>> entry : si.services.entrySet()) {
            Encoder.encodeInt64(entry.getKey(), ba);
            Encoder.encodeInt16(entry.getValue().size(), ba);
            for (Integer id : entry.getValue()) {
                Encoder.encodeInt32(id, ba);
            }
        }
    }

    @Override
    public Object decode(BytesArray ba) {
        ServiceInventory serviceInventory = new ServiceInventory((NID) Encoder.decodeObject(ba));
        int size = Encoder.decodeInt16(ba);
        for (int i = 0; i < size; i++) {
            long serviceType = Encoder.decodeInt64(ba);
            int groupSize = Encoder.decodeInt16(ba);
            for (int j = 0; j < groupSize; j++) {
                serviceInventory.addService(serviceType, Encoder.decodeInt32(ba));
            }
        }
        return serviceInventory;
    }
}