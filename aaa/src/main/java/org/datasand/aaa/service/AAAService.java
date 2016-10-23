package org.datasand.aaa.service;

import org.datasand.microservice.Message;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.network.NID;

/**
 * Created by saichler on 6/7/16.
 */
public class AAAService extends MicroService{

    private static final String AAA_SERVICE_TYPE = "AAA";

    public AAAService(MicroServicesManager manager){
        super(AAA_SERVICE_TYPE,manager);
    }

    @Override
    public void processDestinationUnreachable(Message message, NID unreachableSource) {

    }

    @Override
    public void processMessage(Message message, NID source, NID destination) {

    }

    @Override
    public void start() {

    }

    @Override
    public String getName() {
        return null;
    }
}
