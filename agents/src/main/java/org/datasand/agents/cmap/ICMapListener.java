package org.datasand.agents.cmap;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface ICMapListener<K,V> {
    public void peerPut(K key,V value);
    public void peerRemove(K key);
}
