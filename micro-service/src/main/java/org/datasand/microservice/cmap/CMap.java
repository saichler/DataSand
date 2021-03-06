/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.microservice.cmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.microservice.Message;
import org.datasand.microservice.cnode.CNode;
import org.datasand.microservice.cnode.CMicroServicePeerEntry;
import org.datasand.codec.Encoder;
import org.datasand.network.NID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class CMap<K, V> extends CNode<Map<K, V>, CMapEntry<K, V>> implements Map<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CMap.class);
    private static final int MAP_ENTRY_CLASS_CODE = 240;
    private static final int PUT = 220;
    private static final int REMOVE = 225;
    private int size = 0;
    private ICMapListener<K, V> listener = null;

    static {
        Encoder.registerSerializer(CMapEntry.class, new CMapEntry());
    }

    public CMap(String clusterMapName, MicroServicesManager m, ICMapListener<K, V> _listener) {
        super(clusterMapName, m);
        this.listener = _listener;
        registerHandler(PUT, new PutHandler<K, V>());
        registerHandler(REMOVE, new RemoveHandler<K, V>());
    }

    public void increaseSize() {
        size++;
    }

    public void decreaseSize() {
        size--;
    }

    public ICMapListener<K, V> getListener() {
        return this.listener;
    }

    @Override
    public Map<K, V> createDataTypeInstance() {
        return new HashMap<K, V>();
    }

    @Override
    public Collection<CMapEntry<K, V>> getDataTypeElementCollection(Map<K, V> data) {
        List<CMapEntry<K, V>> list = new LinkedList<CMapEntry<K, V>>();
        for (Map.Entry<K, V> e : data.entrySet()) {
            list.add(new CMapEntry<K, V>(e.getKey(), e.getValue()));
        }
        return list;
    }

    @Override
    public boolean isLocalPeerCopyContainData(Map<K, V> data) {
        return !data.isEmpty();
    }

    @Override
    public void handleNodeOriginalData(CMapEntry<K, V> ce) {
        if (!this.containsKey(ce.getKey())) {
            increaseSize();
        }
        this.getLocalData().put(ce.getKey(), ce.getValue());
        if (listener != null) {
            listener.peerPut(ce.getKey(), ce.getValue());
        }
    }

    @Override
    public void handlePeerSyncData(CMapEntry<K, V> ce, NID source) {
        CMicroServicePeerEntry<Map<K, V>> peerEntry = getPeerEntry(source);
        if (!this.containsKey(ce.getKey())) {
            increaseSize();
        }
        peerEntry.getPeerData().put((K) ce.getKey(), (V) ce.getValue());
        if (listener != null) {
            listener.peerPut(ce.getKey(), ce.getValue());
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new IllegalStateException("This method is not supported yet");
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<K> keySet = this.keySet();
        Set<Map.Entry<K, V>> eSet = new HashSet<Map.Entry<K, V>>();
        for (K key : keySet) {
            eSet.add(new CMapEntry<K, V>(key, this.get(key)));
        }
        return eSet;
    }

    @Override
    public V get(Object key) {
        for (NID id : getSortedNIDs()) {
            CMicroServicePeerEntry<Map<K, V>> entry = getPeerEntry(id);
            if (entry != null) {
                V o = entry.getPeerData().get(key);
                if (o != null) {
                    return o;
                }
            } else {
                V o = getLocalData().get(key);
                if (o != null) {
                    return o;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        if (!this.getLocalData().isEmpty())
            return false;
        for (NID id : this.getSortedNIDs()) {
            CMicroServicePeerEntry<Map<K, V>> peerEntry = getPeerEntry(id);
            if (!peerEntry.getPeerData().isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public Set<K> keySet() {
        Set<K> result = new HashSet<K>();
        result.addAll(this.getLocalData().keySet());
        for (NID id : this.getSortedNIDs()) {
            CMicroServicePeerEntry<Map<K, V>> peerEntry = getPeerEntry(id);
            if (peerEntry != null) {
                result.addAll(peerEntry.getPeerData().keySet());
            }
        }
        return result;
    }

    @Override
    public V put(K key, V value) {
        long id = -1;
        synchronized (this) {
            id = this.incrementID();
            if (this.isSynchronizing()) {
                LOG.info("Waiting for synchronization...");
                try {
                    this.wait();
                } catch (Exception err) {
                }
                LOG.info("End Waiting...");
            }
        }
        Message putCommand = new Message(id, PUT, new CMapEntry<K, V>(key, value));
        addARPJournal(putCommand, false);
        multicast(putCommand);
        if (!this.containsKey(key)) {
            increaseSize();
        }
        return this.getLocalData().put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        // TODO Auto-generated method stub
    }

    @Override
    public V remove(Object key) {
        long id = -1;
        synchronized (this) {
            id = this.incrementID();
        }
        Message removeCommand = new Message(id, REMOVE, new CMapEntry<K, V>((K) key, null));
        addARPJournal(removeCommand, false);
        multicast(removeCommand);
        V result = this.getLocalData().remove(key);
        if (result != null && !this.containsKey(key)) {
            decreaseSize();
        }
        return result;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Collection<V> values() {
        Set<Map.Entry<K, V>> entrySet = this.entrySet();
        List<V> list = new ArrayList<V>(entrySet.size());
        for (Map.Entry<K, V> e : entrySet) {
            list.add(e.getValue());
        }
        return list;
    }
}
