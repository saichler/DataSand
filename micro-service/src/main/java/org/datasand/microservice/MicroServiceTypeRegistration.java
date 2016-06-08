/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class MicroServiceTypeRegistration {

    private static final MicroServiceTypeRegistration instance = new MicroServiceTypeRegistration();
    private final Map<Integer,String> serviceTypes = new ConcurrentHashMap<>();

    private MicroServiceTypeRegistration(){
    }

    public static final MicroServiceTypeRegistration getInstance(){
        return instance;
    }

    public void registerServiceType(int type,String description){
        if(serviceTypes.containsKey(type)){
            throw new IllegalArgumentException("Service type "+type+" is already registered for "+this.serviceTypes.get(type));
        }
        this.serviceTypes.put(type,description);
    }
}
