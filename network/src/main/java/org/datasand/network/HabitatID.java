/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network;

import org.datasand.codec.BytesArray;
import org.datasand.codec.Encoder;
import org.datasand.codec.serialize.ISerializer;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HabitatID implements ISerializer {
    private final int[] address;
    static {
        Encoder.registerSerializer(HabitatID.class,new HabitatID(-1,-1,-1));
    }

    public HabitatID(int address, int port, int _serviceID) {
        this.address = new int[3];
        this.address[0] = address;
        this.address[1] = port;
        this.address[2] = _serviceID;
    }

    public static HabitatID valueOf(String str) {
        String sdata[] = new String[4];
        String port = null;
        String subSystemID = null;

        int index = str.indexOf(".");
        sdata[0] = str.substring(0, index);
        int index1 = str.indexOf(".", index + 1);
        sdata[1] = str.substring(index + 1, index1);
        index = str.indexOf(".", index1 + 1);
        sdata[2] = str.substring(index1 + 1, index);
        index1 = str.indexOf(":");
        sdata[3] = str.substring(index + 1, index1);
        index = str.indexOf(":", index1 + 1);
        port = str.substring(index1 + 1, index);
        subSystemID = str.substring(index + 1);
        int addr = (Integer.parseInt(sdata[0]) * 16777216)
                + (Integer.parseInt(sdata[1]) * 65536)
                + (Integer.parseInt(sdata[2]) * 256)
                + (Integer.parseInt(sdata[3]));
        return new HabitatID(addr, Integer.parseInt(port),
                Integer.parseInt(subSystemID));
    }

    public int getPort() {
        return this.address[this.address.length - 2];
    }

    public int getServiceID() {
        return this.address[this.address.length - 1];
    }

    public int getIPv4Address() {
        return this.address[0];
    }

    public String getIPv4AddressAsString(){
        StringBuffer buff = new StringBuffer();
        byte ipv4[] = new byte[4];
        Encoder.encodeInt32(address[0], ipv4, 0);
        String ipString[] = new String[4];
        if (ipv4[0] < 0) {
            ipString[0] = "" + (256 + ipv4[0]);
        } else {
            ipString[0] = "" + ipv4[0];
        }
        if (ipv4[1] < 0) {
            ipString[1] = "" + (256 + ipv4[1]);
        } else {
            ipString[1] = "" + ipv4[1];
        }
        if (ipv4[2] < 0) {
            ipString[2] = "" + (256 + ipv4[2]);
        } else {
            ipString[2] = "" + ipv4[2];
        }
        if (ipv4[3] < 0) {
            ipString[3] = "" + (256 + ipv4[3]);
        } else {
            ipString[3] = "" + ipv4[3];
        }
        buff.append(ipString[0]).append(".").append(ipString[1])
                .append(".").append(ipString[2]).append(".")
                .append(ipString[3]);
        return buff.toString();
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        // ipv4
        if (address.length == 3) {
            byte ipv4[] = new byte[4];
            Encoder.encodeInt32(address[0], ipv4, 0);
            String ipString[] = new String[4];
            if (ipv4[0] < 0) {
                ipString[0] = "" + (256 + ipv4[0]);
            } else {
                ipString[0] = "" + ipv4[0];
            }
            if (ipv4[1] < 0) {
                ipString[1] = "" + (256 + ipv4[1]);
            } else {
                ipString[1] = "" + ipv4[1];
            }
            if (ipv4[2] < 0) {
                ipString[2] = "" + (256 + ipv4[2]);
            } else {
                ipString[2] = "" + ipv4[2];
            }
            if (ipv4[3] < 0) {
                ipString[3] = "" + (256 + ipv4[3]);
            } else {
                ipString[3] = "" + ipv4[3];
            }
            buff.append(ipString[0]).append(".").append(ipString[1])
                    .append(".").append(ipString[2]).append(".")
                    .append(ipString[3]).append(":")
                    .append(address[address.length - 2]).append(":")
                    .append(address[address.length - 1]);
            return buff.toString();
        } else
            return "Ipv6";
    }

    @Override
    public int hashCode() {
        return this.address[0] & this.address[this.address.length - 1];
    }

    @Override
    public boolean equals(Object obj) {
        HabitatID other = (HabitatID) obj;
        if (other.address.length == this.address.length) {
            for (int i = 0; i < this.address.length; i++) {
                if (this.address[i] != other.address[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    public void encode(HabitatID netID, byte[] data, int location) {
        byte ip[] = new byte[4];
        Encoder.encodeInt32(netID.getIPv4Address(),ip,0);
        System.arraycopy(ip,0,data,location,ip.length);

        byte port[] = new byte[2];
        Encoder.encodeInt16(netID.getPort(),port,0);
        System.arraycopy(port,0,data,location+4,port.length);

        byte subSystem[] = new byte[2];
        Encoder.encodeInt16(netID.getServiceID(),subSystem,0);
        System.arraycopy(subSystem,0,data,location+6,subSystem.length);
    }

    @Override
    public void encode(Object value, BytesArray ba) {
        HabitatID id = (HabitatID)value;
        Encoder.encodeInt32(id.getIPv4Address(), ba);
        Encoder.encodeInt16(id.getPort(), ba);
        Encoder.encodeInt16(id.getServiceID(), ba);
    }


    @Override
    public Object decode(BytesArray ba) {
        int a = Encoder.decodeInt32(ba);
        int b = Encoder.decodeInt16(ba);
        int c = Encoder.decodeInt16(ba);
        return new HabitatID(a, b, c);
    }
}
