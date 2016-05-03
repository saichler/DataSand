/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class ConnectionID {
    private final HabitatID aSide;
    private final HabitatID zSide;
    public ConnectionID(int addr1,int port1,int service1,int addr2,int port2,int service2){
        if(addr1<addr2){
            aSide = new HabitatID(addr1,port1,service1);
            zSide = new HabitatID(addr2,port2,service2);
        } else if(addr1>addr2){
            zSide = new HabitatID(addr1,port1,service1);
            aSide = new HabitatID(addr2,port2,service2);
        } else {
            if(port1<port2){
                aSide = new HabitatID(addr1,port1,service1);
                zSide = new HabitatID(addr2,port2,service2);
            } else if(port1>port2){
                zSide = new HabitatID(addr1,port1,service1);
                aSide = new HabitatID(addr2,port2,service2);
            } else {
                if(service1<service2){
                    aSide = new HabitatID(addr1,port1,service1);
                    zSide = new HabitatID(addr2,port2,service2);
                } else if (service1>service2){
                    zSide = new HabitatID(addr1,port1,service1);
                    aSide = new HabitatID(addr2,port2,service2);
                } else {
                    aSide = new HabitatID(addr1,port1,service1);
                    zSide = new HabitatID(addr2,port2,service2);
                }
            }
        }
    }

    public int hashCode(){
        return aSide.hashCode()+zSide.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        ConnectionID other = (ConnectionID)obj;
        if(other.aSide.equals(this.aSide) && other.zSide.equals(this.zSide))
            return true;
        return false;
    }

    public HabitatID getzSide() {
        return zSide;
    }

    public HabitatID getaSide() {
        return aSide;
    }
}
