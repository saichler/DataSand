package org.datasand.aaa.messages;

import org.datasand.microservice.Message;

/**
 * Created by saichler on 6/7/16.
 */
public class AuthenticateMessage extends Message{
    private final String user;
    private final String password;

    public AuthenticateMessage(String user,String password){
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
