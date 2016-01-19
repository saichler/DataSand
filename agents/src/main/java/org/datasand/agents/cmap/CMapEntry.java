/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.agents.cmap;

import java.util.Map;
import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class CMapEntry<K,V> implements Map.Entry,ISerializer{
    private K key = null;
    private V value = null;
    public CMapEntry(){}
    public CMapEntry(K _key,V _value){
        this.key = _key;
        this.value = _value;
    }
    public K getKey() {
        return key;
    }
    public V getValue() {
        return value;
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        CMapEntry<K,V> me = (CMapEntry<K,V>)value;
        Encoder.encodeObject(me.getKey(), ba);
        Encoder.encodeObject(me.getValue(), ba);
    }

    @Override
    public Object decode(BytesArray ba) {
        Object key = Encoder.decodeObject(ba);
        Object value = Encoder.decodeObject(ba);
        return new CMapEntry<K,V>((K)key,(V)value);
    }

    @Override
    public Object setValue(Object value) {
        // TODO Auto-generated method stub
        return null;
    }
}
