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
    private final NetUUID aSide;
    private final NetUUID zSide;

    public ConnectionID(long a, long b, long a1, long b1) {
        if (a < a1) {
            aSide = new NetUUID(a, b);
            zSide = new NetUUID(a1, b1);
        } else if (a > a1) {
            aSide = new NetUUID(a1, b1);
            zSide = new NetUUID(a, b);
        } else {
            if (b < b1) {
                aSide = new NetUUID(a, b);
                zSide = new NetUUID(a1, b1);
            } else if (b > b1) {
                aSide = new NetUUID(a1, b1);
                zSide = new NetUUID(a, b);
            } else {
                throw new IllegalArgumentException("Connection ID cannot have same aside & zside");
            }
        }
    }

    public NetUUID getAdjacentNetUUID(NetUUID me){
        if(this.aSide.equals(me)){
            return this.zSide;
        } else if (this.zSide.equals(me)) {
            return this.aSide;
        } else {
            throw new IllegalArgumentException("This connectionID does not contain this NetUUID");
        }
    }

    public int hashCode() {
        return aSide.hashCode() + zSide.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        ConnectionID other = (ConnectionID) obj;
        if (other.aSide.equals(this.aSide) && other.zSide.equals(this.zSide))
            return true;
        return false;
    }

    public NetUUID getzSide() {
        return zSide;
    }

    public NetUUID getaSide() {
        return aSide;
    }

    public String toString() {
        return this.aSide + "<-->" + this.zSide;
    }
}
