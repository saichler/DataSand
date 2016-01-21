/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.store;

import org.datasand.codec.Encoder;

/**
 * @author Sharon Aicler (saichler@gmail.com)
 * Created on 1/20/16.
 */
public class DataKey {

    private final byte[] keyData;

    public DataKey(byte[] data){
        if(data==null){
            throw new IllegalArgumentException("Key data can not be null");
        }else
        if(data.length!=4 && data.length!=8 && data.length!=16){
            throw new IllegalArgumentException("Key data can only be 4,8 & 16 and cannot be "+data.length);
        }
        this.keyData = data;
    }

    public DataKey(int key){
        this.keyData = new byte[4];
        Encoder.encodeInt32(key,keyData,0);
    }

    public DataKey(long key){
        this.keyData = new byte[8];
        Encoder.encodeInt64(key,keyData,0);
    }

    public DataKey(long mostSegnificant,long lessSegnificant){
        this.keyData = new byte[16];
        Encoder.encodeInt64(mostSegnificant,keyData,0);
        Encoder.encodeInt64(lessSegnificant,keyData,8);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;

        DataKey dataKey = (DataKey) o;

        if(dataKey.keyData.length!=this.keyData.length) return false;

        if(dataKey.keyData.length==16 &&
                dataKey.keyData[0]==this.keyData[0] &&
                dataKey.keyData[1]==this.keyData[1] &&
                dataKey.keyData[2]==this.keyData[2] &&
                dataKey.keyData[3]==this.keyData[3] &&
                dataKey.keyData[4]==this.keyData[4] &&
                dataKey.keyData[5]==this.keyData[5] &&
                dataKey.keyData[6]==this.keyData[6] &&
                dataKey.keyData[7]==this.keyData[7] &&
                dataKey.keyData[8]==this.keyData[8] &&
                dataKey.keyData[9]==this.keyData[9] &&
                dataKey.keyData[10]==this.keyData[10] &&
                dataKey.keyData[11]==this.keyData[11] &&
                dataKey.keyData[12]==this.keyData[12] &&
                dataKey.keyData[13]==this.keyData[13] &&
                dataKey.keyData[14]==this.keyData[14] &&
                dataKey.keyData[15]==this.keyData[15]){
            return true;
        }else
        if(dataKey.keyData.length==8 &&
                dataKey.keyData[0]==this.keyData[0] &&
                dataKey.keyData[1]==this.keyData[1] &&
                dataKey.keyData[2]==this.keyData[2] &&
                dataKey.keyData[3]==this.keyData[3] &&
                dataKey.keyData[4]==this.keyData[4] &&
                dataKey.keyData[5]==this.keyData[5] &&
                dataKey.keyData[6]==this.keyData[6] &&
                dataKey.keyData[7]==this.keyData[7] ) {
            return true;
        }else
        if(dataKey.keyData.length==4 &&
                dataKey.keyData[0]==this.keyData[0] &&
                dataKey.keyData[1]==this.keyData[1] &&
                dataKey.keyData[2]==this.keyData[2] &&
                dataKey.keyData[3]==this.keyData[3] ) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (keyData == null)
            return 0;
        int result = 1;
        if(keyData.length==16){
            result = 31 * result+this.keyData[0];
            result = 31 * result+this.keyData[1];
            result = 31 * result+this.keyData[2];
            result = 31 * result+this.keyData[3];
            result = 31 * result+this.keyData[4];
            result = 31 * result+this.keyData[5];
            result = 31 * result+this.keyData[6];
            result = 31 * result+this.keyData[7];
            result = 31 * result+this.keyData[8];
            result = 31 * result+this.keyData[9];
            result = 31 * result+this.keyData[10];
            result = 31 * result+this.keyData[11];
            result = 31 * result+this.keyData[12];
            result = 31 * result+this.keyData[13];
            result = 31 * result+this.keyData[14];
            result = 31 * result+this.keyData[15];
        }else
        if(keyData.length==4){
            result = 31 * result+this.keyData[0];
            result = 31 * result+this.keyData[1];
            result = 31 * result+this.keyData[2];
            result = 31 * result+this.keyData[3];
        }else
        if(keyData.length==8){
            result = 31 * result+this.keyData[0];
            result = 31 * result+this.keyData[1];
            result = 31 * result+this.keyData[2];
            result = 31 * result+this.keyData[3];
            result = 31 * result+this.keyData[4];
            result = 31 * result+this.keyData[5];
            result = 31 * result+this.keyData[6];
            result = 31 * result+this.keyData[7];
            result = 31 * result+this.keyData[8];
        }
        return result;
    }
}
