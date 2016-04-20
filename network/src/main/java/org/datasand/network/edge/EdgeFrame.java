/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.network.edge;

import org.datasand.codec.Encoder;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.UUID;


/**
 * @author - Sharon Aicler (saichler@gmail.com)
 * Created by saichler on 4/20/16.
 */
public class EdgeFrame {
    public static final int B01_LOCATION_UUID                  =  0;
    public static final int B01_SIZE_UUID                      = 16;
    public static final int B02_LOCATION_MESSAGES_IDS          = B01_LOCATION_UUID+B01_SIZE_UUID;
    public static final int B02_SIZE_MESSAGES_IDS              =  8;
    public static final int B03_LOCATION_FRAME_ID              = B02_LOCATION_MESSAGES_IDS+B02_SIZE_MESSAGES_IDS;
    public static final int B03_SIZE_FRAME_ID                  =  4;
    public static final int B04_LOCATION_API_OPERATION         = B03_LOCATION_FRAME_ID+B03_SIZE_FRAME_ID;
    public static final int B04_SIZE_API_OPERATION             = 1;
    public static final int B05_LOCATION_PRIORITY              = B04_LOCATION_API_OPERATION+B04_SIZE_API_OPERATION;
    public static final int B05_SIZE_PRIORITY                  = 1;
    public static final int B06_LOCATION_GROUP_MD5             = B05_LOCATION_PRIORITY+B05_SIZE_PRIORITY;
    public static final int B06_SIZE_GROUP_MD5                 = 16;
    public static final int B07_LOCATION_DATA_SIZE             = B06_LOCATION_GROUP_MD5+B06_SIZE_GROUP_MD5;
    public static final int B07_SIZE_DATA_SIZE                 = 4;
    public static final int  HEADER_SIZE			            = B07_LOCATION_DATA_SIZE+B07_SIZE_DATA_SIZE;
    public static final int  MAX_DATA_SIZE		                = 512;
    public static final int  MAX_FRAME_SIZE                     = HEADER_SIZE+MAX_DATA_SIZE;

    private UUID uuid = null;
    private int messageID = -1;
    private int origMessageID = -1;
    private int frameID = -1;
    private byte apiOperatrion = -1;
    private byte priority = 0;
    private byte[] groupMD5 = null;
    private byte[] data = null;
    private DatagramPacket[] datagramPackets = null;
    private byte[] ackData = null;
    private int totalFrameCount = -1;

    public EdgeFrame(){
    }

    public EdgeFrame(DatagramPacket dp){
        this.decode(dp);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public int getFrameID() {
        return frameID;
    }

    public void setFrameID(int frameID) {
        this.frameID = frameID;
    }

    public int getOrigMessageID() {
        return origMessageID;
    }

    public void setOrigMessageID(int origMessageID) {
        this.origMessageID = origMessageID;
    }

    public byte getApiOperatrion() {
        return apiOperatrion;
    }

    public void setApiOperatrion(byte apiOperatrion) {
        this.apiOperatrion = apiOperatrion;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte[] getGroupMD5() {
        return groupMD5;
    }

    public void setGroupMD5(byte[] groupMD5) {
        this.groupMD5 = groupMD5;
    }

    public void setTotalFrameCount(int totalFrameCount) {
        this.totalFrameCount = totalFrameCount;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        if(data==null && datagramPackets!=null){
            if(this.datagramPackets.length==1){
                int size = Encoder.decodeInt32(datagramPackets[0].getData(), B07_LOCATION_DATA_SIZE)-HEADER_SIZE;
                data = new byte[size];
                System.arraycopy(datagramPackets[0].getData(),HEADER_SIZE, data, 0, size);
            }else{
                int totalSize = 0;
                byte[][] totalData = new byte[this.datagramPackets.length-1][];
                for(int index=1;index<this.datagramPackets.length;index++){
                    int size = Encoder.decodeInt32(this.datagramPackets[index].getData(), B07_LOCATION_DATA_SIZE)-HEADER_SIZE;
                    totalSize+=size;
                    totalData[index-1] = new byte[size];
                    System.arraycopy(this.datagramPackets[index].getData(),HEADER_SIZE, totalData[index-1], 0, size);
                }
                data = new byte[totalSize];
                int dataIndex = 0;
                for(int index=0;index<totalData.length;index++){
                    System.arraycopy(totalData[index], 0, data, dataIndex, totalData[index].length);
                    dataIndex+=totalData[index].length;
                }
            }
        }
        return data;
    }

    public static boolean isAckFrame(DatagramPacket datagramPacket){
        return Encoder.decodeInt32(datagramPacket.getData(), B07_LOCATION_DATA_SIZE)-HEADER_SIZE<0;
    }

    public DatagramPacket[] getDatagramPackets() {
        return this.datagramPackets;
    }

    public int getTotalFrameCount(){
        return this.totalFrameCount;
    }

    public void setFrames(Collection<EdgeFrame> p){
        DatagramPacket packets[] = new DatagramPacket[p.size()];
        for(EdgeFrame dp:p){
            packets[dp.getTotalFrameCount()] = dp.getDatagramPackets()[0];
        }
        this.datagramPackets = packets;
    }

    public void decode(DatagramPacket datagramPacket){
        byte dpData[] = datagramPacket.getData();
        this.ackData = new byte[24];
        this.uuid = new UUID(Encoder.decodeInt64(dpData,B01_LOCATION_UUID),Encoder.decodeInt64(dpData,B01_LOCATION_UUID+8));
        System.arraycopy(dpData, B01_LOCATION_UUID, ackData, 0, 16);
        this.messageID = Encoder.decodeInt32(dpData, B02_LOCATION_MESSAGES_IDS);
        System.arraycopy(dpData, B02_LOCATION_MESSAGES_IDS, ackData, 16, B02_SIZE_MESSAGES_IDS);
        this.origMessageID = Encoder.decodeInt32(dpData, B02_LOCATION_MESSAGES_IDS+4);
        this.frameID = Encoder.decodeInt32(dpData, B03_LOCATION_FRAME_ID);
        System.arraycopy(dpData, B03_LOCATION_FRAME_ID, ackData, 20, 4);
        this.apiOperatrion = dpData[B04_LOCATION_API_OPERATION];
        this.priority = dpData[B05_LOCATION_PRIORITY];
        this.groupMD5 = new byte[B06_SIZE_GROUP_MD5];
        System.arraycopy(dpData, B06_LOCATION_GROUP_MD5, this.groupMD5, 0,B06_SIZE_GROUP_MD5);
        if(this.frameID==0){
            this.totalFrameCount = Encoder.decodeInt32(dpData,B07_LOCATION_DATA_SIZE)-HEADER_SIZE;
        }
        this.datagramPackets = new DatagramPacket[]{datagramPacket};
    }

    public void encode(InetAddress target,int port){
        if(data.length<=MAX_DATA_SIZE){
            byte[] packetData = createDatagramFrameData(data.length, 0, -1);
            datagramPackets = new DatagramPacket[]{new DatagramPacket(packetData,packetData.length,target,port)};
        }else{
            int parts = data.length/MAX_DATA_SIZE+1;
            if(data.length%MAX_DATA_SIZE!=0)
                parts++;
            this.datagramPackets = new DatagramPacket[parts];
            byte messageHeader[] = createFrameHeader(parts);
            this.datagramPackets[0] = new DatagramPacket(messageHeader, messageHeader.length,target,port);
            for(int i=0;i<parts-1;i++){
                if(i<parts-2){
                    byte[] packetData = createDatagramFrameData(MAX_DATA_SIZE, i*MAX_DATA_SIZE, i+1);
                    this.datagramPackets[i+1] = new DatagramPacket(packetData,packetData.length,target,port);
                }else{
                    byte[] packetData = createDatagramFrameData(data.length-MAX_DATA_SIZE*i, i*MAX_DATA_SIZE, i+1);
                    this.datagramPackets[i+1] = new DatagramPacket(packetData,packetData.length,target,port);
                }
            }
        }
        this.totalFrameCount = datagramPackets.length;
    }

    public void setDatagramPacketToNull(int index){
        if(this.datagramPackets[index]!=null){
            this.totalFrameCount--;
            this.datagramPackets[index]=null;
        }
    }

    private byte[] createFrameHeader(int parts){
        byte[] frameHeaderData = new byte[HEADER_SIZE];
        Encoder.encodeInt64(this.uuid.getMostSignificantBits(), frameHeaderData, B01_LOCATION_UUID);
        Encoder.encodeInt64(this.uuid.getLeastSignificantBits(), frameHeaderData, B01_LOCATION_UUID+8);
        Encoder.encodeInt32(this.messageID,frameHeaderData, B02_LOCATION_MESSAGES_IDS);
        Encoder.encodeInt32(this.origMessageID,frameHeaderData, B02_LOCATION_MESSAGES_IDS+4);
        Encoder.encodeInt32(0,frameHeaderData, B03_LOCATION_FRAME_ID);
        frameHeaderData[B04_LOCATION_API_OPERATION] = this.apiOperatrion;
        frameHeaderData[B05_LOCATION_PRIORITY] = this.priority;
        if(this.groupMD5!=null){
            System.arraycopy(this.groupMD5, 0,frameHeaderData, B06_LOCATION_GROUP_MD5, B06_SIZE_GROUP_MD5);
        }
        Encoder.encodeInt32(parts+HEADER_SIZE, frameHeaderData,B07_LOCATION_DATA_SIZE);
        return frameHeaderData;
    }

    private byte[] createDatagramFrameData(int dataSize,int dataLocation,int frameNumber){
        byte[] packetData = new byte[HEADER_SIZE+dataSize];
        Encoder.encodeInt64(this.uuid.getMostSignificantBits(), packetData, B01_LOCATION_UUID);
        Encoder.encodeInt64(this.uuid.getLeastSignificantBits(), packetData, B01_LOCATION_UUID+8);
        Encoder.encodeInt32(this.messageID,packetData, B02_LOCATION_MESSAGES_IDS);
        Encoder.encodeInt32(this.origMessageID,packetData, B02_LOCATION_MESSAGES_IDS+4);
        Encoder.encodeInt32(frameNumber,packetData, B03_LOCATION_FRAME_ID);
        packetData[B04_LOCATION_API_OPERATION] = this.apiOperatrion;
        packetData[B05_LOCATION_PRIORITY] = this.priority;
        if(this.groupMD5!=null){
            System.arraycopy(this.groupMD5, 0,packetData, B06_LOCATION_GROUP_MD5, B06_SIZE_GROUP_MD5);
        }
        Encoder.encodeInt32(dataSize+HEADER_SIZE, packetData,B07_LOCATION_DATA_SIZE);
        System.arraycopy(data, dataLocation, packetData, HEADER_SIZE, dataSize);
        return packetData;
    }

    public EdgeFrame clone(){
        EdgeFrame clone = new EdgeFrame();
        clone.data = this.data;
        clone.datagramPackets = this.datagramPackets;
        clone.messageID = this.messageID;
        clone.apiOperatrion = this.apiOperatrion;
        clone.origMessageID = this.origMessageID;
        clone.frameID = this.frameID;
        clone.priority = this.priority;
        clone.groupMD5 = this.groupMD5;
        clone.uuid = this.uuid;
        return clone;
    }

    public byte[] getAckData(){
        return this.ackData;
    }
}