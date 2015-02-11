package org.datasand.agents.cnode;

import org.datasand.agents.PeerEntry;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class CPeerEntry<DataType> extends PeerEntry{
    private DataType peerData;

    public CPeerEntry(NetworkID _netNetworkID,DataType _peerData){
        super(_netNetworkID);
        this.peerData = _peerData;
    }

    public DataType getPeerData() {
        return peerData;
    }
}
