package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.ListWithKey;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.SalPersistedSubContainer;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTestData;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyEnumType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.NoKeysubList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import java.util.List;


/**
 * Test the MDSAL model serialization and de serialization
 * 
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;sal-persisted-dom-test&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/sal-persisted-dom-test.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * container sal-persisted-dom-test {
 *     leaf MainString {
 *         type string;
 *     }
 *     leaf countID {
 *         type int32;
 *     }
 *     container sal-persisted-sub-container {
 *         leaf SubContainerMainString {
 *             type string;
 *         }
 *     }
 *     list NoKeysubList {
 *         key     leaf name {
 *             type string;
 *         }
 *         leaf typedeftest {
 *             type MyType;
 *         }
 *         leaf enumtest {
 *             type my-enum-type;
 *         }
 *         leaf testInt8 {
 *             type int8;
 *         }
 *         leaf testInt16 {
 *             type int16;
 *         }
 *         leaf testInt32 {
 *             type int32;
 *         }
 *         leaf testInt64 {
 *             type int64;
 *         }
 *         leaf testDecimal64 {
 *             type decimal64;
 *         }
 *         leaf bitsTest {
 *             type typedef-bits;
 *         }
 *         leaf testBinary {
 *             type binary;
 *         }
 *     }
 *     list ListWithKey {
 *         key "id"
 *         leaf id {
 *             type string;
 *         }
 *         leaf name {
 *             type string;
 *         }
 *     }
 *     leaf typedeftest {
 *         type MyType;
 *     }
 *     leaf enumtest {
 *         type my-enum-type;
 *     }
 *     leaf testInt8 {
 *         type int8;
 *     }
 *     leaf testInt16 {
 *         type int16;
 *     }
 *     leaf testInt32 {
 *         type int32;
 *     }
 *     leaf testInt64 {
 *         type int64;
 *     }
 *     leaf testDecimal64 {
 *         type decimal64;
 *     }
 *     leaf bitsTest {
 *         type typedef-bits;
 *     }
 *     leaf testBinary {
 *         type binary;
 *     }
 *     leaf MainString2 {
 *         type string;
 *     }
 *     leaf atypedeftest {
 *         type MyType;
 *     }
 *     leaf aenumtest {
 *         type my-enum-type;
 *     }
 *     leaf atestInt8 {
 *         type int8;
 *     }
 *     leaf atestInt16 {
 *         type int16;
 *     }
 *     leaf atestInt32 {
 *         type int32;
 *     }
 *     leaf atestInt64 {
 *         type int64;
 *     }
 *     leaf atestDecimal64 {
 *         type decimal64;
 *     }
 *     leaf abitsTest {
 *         type typedef-bits;
 *     }
 *     leaf atestBinary {
 *         type binary;
 *     }
 *     list AugmentSubList {
 *         key     leaf SubAugName {
 *             type string;
 *         }
 *     }
 *     augment \(http://netconfcentral.org/ns/persistedtest)sal-persisted-dom-test {
 *         status CURRENT;
 *         leaf MainString2 {
 *             type string;
 *         }
 *         leaf atypedeftest {
 *             type MyType;
 *         }
 *         leaf aenumtest {
 *             type my-enum-type;
 *         }
 *         leaf atestInt8 {
 *             type int8;
 *         }
 *         leaf atestInt16 {
 *             type int16;
 *         }
 *         leaf atestInt32 {
 *             type int32;
 *         }
 *         leaf atestInt64 {
 *             type int64;
 *         }
 *         leaf atestDecimal64 {
 *             type decimal64;
 *         }
 *         leaf abitsTest {
 *             type typedef-bits;
 *         }
 *         leaf atestBinary {
 *             type binary;
 *         }
 *         list AugmentSubList {
 *             key     leaf SubAugName {
 *                 type string;
 *             }
 *         }
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;sal-persisted-dom-test/sal-persisted-dom-test&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTestBuilder}.
 * @see org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTestBuilder
 * 
 */
public interface SalPersistedDomTest
    extends
    ChildOf<SalPersistedDomTestData>,
    Augmentable<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("http://netconfcentral.org/ns/persistedtest","2014-10-20","sal-persisted-dom-test");;

    /**
     * Test simple string
     * 
     */
    java.lang.String getMainString();
    
    java.lang.Integer getCountID();
    
    SalPersistedSubContainer getSalPersistedSubContainer();
    
    List<NoKeysubList> getNoKeysubList();
    
    List<ListWithKey> getListWithKey();
    
    MyType getTypedeftest();
    
    MyEnumType getEnumtest();
    
    java.lang.Byte getTestInt8();
    
    java.lang.Short getTestInt16();
    
    java.lang.Integer getTestInt32();
    
    java.lang.Long getTestInt64();
    
    BigDecimal getTestDecimal64();
    
    TypedefBits getBitsTest();
    
    byte[] getTestBinary();

}

