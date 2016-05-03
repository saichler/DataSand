/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents;

import org.datasand.network.HabitatID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PeerEntry {
    private HabitatID habitatID = null;
    private long lastReceivedPing = -1;
    private long lastID = 999;
    private boolean unreachable = false;

    public PeerEntry(HabitatID _netHabitatID){
        this.habitatID = _netHabitatID;
        this.lastReceivedPing = System.currentTimeMillis();
    }
    public HabitatID getHabitatID() {
        return habitatID;
    }
    public long getLastReceivedPing() {
        return lastReceivedPing;
    }
    public long getLastID() {
        return lastID;
    }
    public void setLastID(long id){
        this.lastID = id;
    }
    public void setUnreachable(boolean u){
        this.unreachable = u;
    }
    public boolean isUnreachable() {
        return unreachable;
    }
    public void timeStamp(){
        this.lastReceivedPing = System.currentTimeMillis();
    }
}
