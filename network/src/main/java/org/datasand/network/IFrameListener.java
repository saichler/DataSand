package org.datasand.network;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public interface IFrameListener {
    public void process(Packet frame);

    public void processDestinationUnreachable(Packet frame);

    public void processBroadcast(Packet frame);

    public void processMulticast(Packet frame);
}
