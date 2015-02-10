package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyEnumType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.math.BigDecimal;
import java.util.List;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.AugmentSubList;
import org.opendaylight.yangtools.yang.binding.Augmentation;


public interface SalPersistedDomTest1
    extends
    DataObject,
    Augmentation<SalPersistedDomTest>
{




    /**
     * Test simple string
     * 
     */
    java.lang.String getMainString2();
    
    MyType getAtypedeftest();
    
    MyEnumType getAenumtest();
    
    java.lang.Byte getAtestInt8();
    
    java.lang.Short getAtestInt16();
    
    java.lang.Integer getAtestInt32();
    
    java.lang.Long getAtestInt64();
    
    BigDecimal getAtestDecimal64();
    
    TypedefBits getAbitsTest();
    
    byte[] getAtestBinary();
    
    List<AugmentSubList> getAugmentSubList();

}

