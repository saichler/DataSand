/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cnode;

import java.util.Comparator;

import org.datasand.network.NetUUID;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class NetworkIDComparator implements Comparator<NetUUID>{
    @Override
    public int compare(NetUUID o1, NetUUID o2) {
        if(o1==null && o2==null)
            return 0;
        if(o1==null && o2!=null)
            return 1;
        if(o1!=null && o2==null)
            return -1;
        if(o1.getIPv4Address()<o2.getIPv4Address())
            return -1;
        else
        if(o1.getIPv4Address()>o2.getIPv4Address())
            return 1;
        if(o1.getPort()<o2.getPort())
            return -1;
        else
        if(o1.getPort()>o2.getPort())
            return 1;
        if(o1.getServiceID()<o2.getServiceID())
            return -1;
        else
        if(o1.getServiceID()>o2.getServiceID())
            return 1;
        return 0;
    }
}