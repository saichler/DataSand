/*
 * Copyright (c) 2016 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.datasand.security;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class SecurityUtilTest {
    @Test
    public void testEncryptionDecruption() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {
        String input = "Hello World Security Test 1...2...3...4...5...6....7...8....9....10";
        SecretKey key = SecurityUtils.generateSecretKey();
        byte[] enryptedData = SecurityUtils.encrypt(input.getBytes(), key);
        byte[] decruptedData = SecurityUtils.decrypt(enryptedData, key);
        String output = new String(decruptedData).trim();
        Assert.assertEquals(input, output);
    }

    @Test
    public void testEncryptionDecruptionRSA() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
    	String input = "Hello World Security Test 1...2...3...4...5...6....7...8....9....10";
        KeyPair keys = SecurityUtils.generateRSAKeys();
        byte[] enryptedData = SecurityUtils.encryptRSA(input.getBytes(),keys.getPublic());
        byte[] decruptedData = SecurityUtils.decryptRSA(enryptedData, keys.getPrivate());
        String output = new String(decruptedData).trim();
        Assert.assertEquals(input, output);
    }

    @Test
    public void testRSAKeysToBytes() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair keys = SecurityUtils.generateRSAKeys();
        byte[] p = SecurityUtils.getRSAPublicKeyBytes(keys.getPublic());
        PublicKey pKey = SecurityUtils.getRSAPublicKeyFromBytes(p);
        Assert.assertNotNull(pKey);

        p = SecurityUtils.getRSAPrivateKeyBytes(keys.getPrivate());
        PrivateKey prKey = SecurityUtils.getRSAPrivateKeyFromBytes(p);
        Assert.assertNotNull(prKey);

    }
}
