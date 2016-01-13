/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.datasand.codec;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class HierarchyBytesArrayTest {

    private static final PojoObject createPojo(){
        PojoObject object = new PojoObject();
        object.setTestBoolean(true);
        object.setTestIndex(123);
        object.setTestLong(System.currentTimeMillis());
        object.setTestShort((short)345);
        object.setTestString("Test String");

        SubPojoObject sub = new SubPojoObject();
        sub.setNumber(12345);
        sub.setString("Some String");
        object.setSubPojo(sub);

        List<SubPojoList> list = new ArrayList<>(2);
        SubPojoList subL1 = new SubPojoList();
        subL1.setName("Sub 1");

        SubPojoList subL2 = new SubPojoList();
        subL1.setName("Sub 2");

        list.add(subL1);
        list.add(subL2);
        object.setList(list);
        return object;
    }

    @Test
    public void testHierarchyEncoding(){
        PojoObject before = createPojo();
        HierarchyBytesArray ba = new HierarchyBytesArray();
        Encoder.encodeObject(before,ba);
        Assert.assertEquals(2,ba.getChildren().size());
        ba.resetLocation();
        PojoObject after = (PojoObject) Encoder.decodeObject(ba);
        Assert.assertEquals(before,after);
    }
}
