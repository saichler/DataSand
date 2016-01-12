/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.util.HashSet;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class VTableTest {
    @Test
    public void test(){
        VTable table = new VTable(PojoObject.class);
        table.analyze(new HashSet<Class<?>>());
        Map<VColumn,VTable> m = table.getChildren();
        Assert.assertEquals(2,m.size());
        Assert.assertEquals(5,table.getColumns().size());
        Assert.assertEquals(0,table.getParents().size());
        Assert.assertNotNull(table.getColumnByName("teststring"));
        for(VTable t:m.values()){
            Assert.assertTrue(t.getName().equals(SubPojoObject.class.getSimpleName().toUpperCase()) || t.getName().equals(SubPojoList.class.getSimpleName().toUpperCase()));
            if(t.getName().equals(SubPojoList.class.getSimpleName().toUpperCase())){
                Assert.assertEquals(1,t.getColumns().size());
            }else
            if(t.getName().equals(SubPojoObject.class.getSimpleName().toUpperCase())){
                Assert.assertEquals(2,t.getColumns().size());
            }
        }
    }
}
