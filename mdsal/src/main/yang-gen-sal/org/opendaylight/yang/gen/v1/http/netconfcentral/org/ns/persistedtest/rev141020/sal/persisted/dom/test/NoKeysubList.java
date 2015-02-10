package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyEnumType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;sal-persisted-dom-test&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/sal-persisted-dom-test.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * list NoKeysubList {
 *     key     leaf name {
 *         type string;
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
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;sal-persisted-dom-test/sal-persisted-dom-test/NoKeysubList&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.NoKeysubListBuilder}.
 * @see org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.NoKeysubListBuilder
 * 
 * 
 */
public interface NoKeysubList
    extends
    ChildOf<SalPersistedDomTest>,
    Augmentable<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.NoKeysubList>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("http://netconfcentral.org/ns/persistedtest","2014-10-20","NoKeysubList");;

    java.lang.String getName();
    
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

