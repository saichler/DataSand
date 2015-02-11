package org.datasand.agents;

import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class PeerEntry {
    private NetworkID networkID = null;
    private long lastReceivedPing = -1;
    private long lastID = 999;
    private boolean unreachable = false;

    public PeerEntry(NetworkID _netNetworkID){
        this.networkID = _netNetworkID;
        this.lastReceivedPing = System.currentTimeMillis();
    }
    public NetworkID getNetworkID() {
        return networkID;
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
