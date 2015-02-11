package org.datasand.agents.cmap;

import java.util.Map;

import org.datasand.agents.Message;
import org.datasand.agents.cnode.CNode;
import org.datasand.agents.cnode.CPeerEntry;
import org.datasand.agents.cnode.ICNodeCommandHandler;
import org.datasand.network.NetworkID;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class RemoveHandler<K,V> implements ICNodeCommandHandler<Map<K,V>,CMapEntry<K,V>>{

    @Override
    public void handleMessage(Message cNodeCommand, NetworkID source,NetworkID destination, CPeerEntry<Map<K, V>> peerEntry,CNode<Map<K, V>, CMapEntry<K, V>> node) {
        CMap<K, V> cmap = (CMap<K, V>)node;
        Object o = peerEntry.getPeerData().remove(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey());
        if(o!=null && !cmap.containsKey(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey())){
            cmap.decreaseSize();
        }
        peerEntry.timeStamp();
        peerEntry.setLastID(cNodeCommand.getMessageID());
        node.sendAcknowledge(cNodeCommand, source);
        if(cmap.getListener()!=null){
            cmap.getListener().peerRemove(((CMapEntry<K,V>)cNodeCommand.getMessageData()).getKey());
        }
    }

    @Override
    public void handleUnreachableMessage(Message cNodeCommand,NetworkID unreachableSource, CPeerEntry<Map<K, V>> peerEntry,CNode<Map<K, V>, CMapEntry<K, V>> node) {
    }
}
