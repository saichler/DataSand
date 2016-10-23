package org.datasand.aaa.service;

import org.datasand.network.nnode.Node;
import org.datasand.network.nnode.auth.AuthenticationProvider;
import org.datasand.security.SecurityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by Homr on 10/23/16.
 */
public class AuthenticationProviderTest {
    private Node n1 = null;

    @Before
    public void before(){
        n1 = new Node(null);
    }

    public void after(){
        n1.shutdown();
    }

    @Test
    public void test() throws NoSuchAlgorithmException, BadPaddingException, IOException, ClassNotFoundException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InterruptedException {
        KeyPair keys = SecurityUtils.generateRSAKeys();
        SecretKey key = AuthenticationProvider.authenticate("admin","admin","127.0.0.1",keys);
        Assert.assertNotNull(key);
    }
}
