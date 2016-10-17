package org.datasand.aaa.service;

import org.datasand.aaa.messages.AuthenticateMessage;
import org.datasand.microservice.Message;
import org.datasand.microservice.MicroService;
import org.datasand.microservice.MicroServiceTypeRegistration;
import org.datasand.microservice.MicroServicesManager;
import org.datasand.network.NID;

/**
 * Created by saichler on 6/7/16.
 */
public class AAAService extends MicroService{

    private static final int AAA_SERVICE_TYPE = 111;

    public AAAService(MicroServicesManager manager){
        super(AAA_SERVICE_TYPE,manager);
        MicroServiceTypeRegistration.getInstance().registerServiceType(AAA_SERVICE_TYPE,"AAA Service");
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

    public void authenticate(String user,String password){
        AuthenticateMessage authMsg = new AuthenticateMessage(user,password);
        this.multicast(authMsg);
    }
}
