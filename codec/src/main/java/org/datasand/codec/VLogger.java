/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class VLogger {
    public static final void error(String message,Exception e){
        System.err.println(message);
        if(e!=null){
            e.printStackTrace();
        }
    }

    public static final void info(String message){
        System.out.println(message);
    }
}
