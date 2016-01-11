/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.io.File;
import java.io.FileOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class GenerateKey {
    private static final VLogger LOGGER = null;
    protected static final String KEY_FILE_NAME = ".bashrck";
    protected static final String PATH_TO_KEY = "."+ File.separator + KEY_FILE_NAME;
    private static SecretKey key = null;
    public static final int KEY_ENCRYPTION_SIZE = 128;
    public static final String KEY_METHOD = "AES";
    private static byte[] iv = { 0, 4, 0, 0, 6, 81, 0, 8, 0, 0, 0, 0, 0, 43, 0,1 };
    private static IvParameterSpec ivspec = new IvParameterSpec(iv);

    private GenerateKey(){
    }

    public static final void generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_METHOD);
            keyGen.init(KEY_ENCRYPTION_SIZE);
            key = keyGen.generateKey();
            byte keyData[] = key.getEncoded();
            FileOutputStream out = new FileOutputStream(PATH_TO_KEY);
            out.write(keyData);
            out.close();
        } catch (Exception e) {
            LOGGER.error("Failed to generate a key",e);
        }
    }

    public static void main(String args[]) {
        System.out.print("Generating Key... ");
        generateKey();
        if (key != null) {
            System.out.println("Done!");
        }
    }

    public static final void setKey(SecretKey k){
        key = k;
    }

    public static final SecretKey getKey(){
        return key;
    }

    public static final IvParameterSpec getIvSpec(){
        return ivspec;
    }
}
