package org.datasand.microservice;

import java.util.UUID;
import org.datasand.network.NetUUID;

/**
 * Created by saichler on 10/13/16.
 */
public class MicroServiceNetUUID extends NetUUID{

    private final int microServiceID;

    public MicroServiceNetUUID(UUID address,int microServiceID) {
        super(address);
        this.microServiceID = microServiceID;
    }

    public MicroServiceNetUUID(long a, long b,int microServiceID) {
        super(a, b);
        this.microServiceID = microServiceID;
    }

    public int getMicroServiceID(){
        return this.microServiceID;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)){
            MicroServiceNetUUID other = (MicroServiceNetUUID)obj;
            if(this.microServiceID==other.microServiceID)
                return true;
        }
        return false;
    }
}
