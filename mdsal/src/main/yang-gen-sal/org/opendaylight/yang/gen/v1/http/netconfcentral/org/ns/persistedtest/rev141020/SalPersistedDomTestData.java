package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest;
import org.opendaylight.yangtools.yang.binding.DataRoot;


/**
 * Persisted Database POC for MDSAL
 * 
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;sal-persisted-dom-test&lt;/b&gt;
 * &lt;br&gt;Source path: &lt;i&gt;META-INF/yang/sal-persisted-dom-test.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * module sal-persisted-dom-test {
 *     yang-version 1;
 *     namespace "http://netconfcentral.org/ns/persistedtest";
 *     prefix "persistedtest";
 * 
 *     import opendaylight-md-sal-binding { prefix "mdsal"; }
 *     
 *     import opendaylight-md-sal-common { prefix "common"; }
 *     
 *     import config { prefix "config"; }
 *     
 *     import opendaylight-md-sal-dom { prefix "sal"; }
 *     
 *     import rpc-context { prefix "rpcx"; }
 *     revision 2014-10-20 {
 *         description "Persisted Database POC for MDSAL
 *         ";
 *     }
 * 
 *     container sal-persisted-dom-test {
 *         leaf MainString {
 *             type string;
 *         }
 *         leaf countID {
 *             type int32;
 *         }
 *         container sal-persisted-sub-container {
 *             leaf SubContainerMainString {
 *                 type string;
 *             }
 *         }
 *         list NoKeysubList {
 *             key     leaf name {
 *                 type string;
 *             }
 *             leaf typedeftest {
 *                 type MyType;
 *             }
 *             leaf enumtest {
 *                 type my-enum-type;
 *             }
 *             leaf testInt8 {
 *                 type int8;
 *             }
 *             leaf testInt16 {
 *                 type int16;
 *             }
 *             leaf testInt32 {
 *                 type int32;
 *             }
 *             leaf testInt64 {
 *                 type int64;
 *             }
 *             leaf testDecimal64 {
 *                 type decimal64;
 *             }
 *             leaf bitsTest {
 *                 type typedef-bits;
 *             }
 *             leaf testBinary {
 *                 type binary;
 *             }
 *         }
 *         list ListWithKey {
 *             key "id"
 *             leaf id {
 *                 type string;
 *             }
 *             leaf name {
 *                 type string;
 *             }
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
 *         augment \(http://netconfcentral.org/ns/persistedtest)sal-persisted-dom-test {
 *             status CURRENT;
 *             leaf MainString2 {
 *                 type string;
 *             }
 *             leaf atypedeftest {
 *                 type MyType;
 *             }
 *             leaf aenumtest {
 *                 type my-enum-type;
 *             }
 *             leaf atestInt8 {
 *                 type int8;
 *             }
 *             leaf atestInt16 {
 *                 type int16;
 *             }
 *             leaf atestInt32 {
 *                 type int32;
 *             }
 *             leaf atestInt64 {
 *                 type int64;
 *             }
 *             leaf atestDecimal64 {
 *                 type decimal64;
 *             }
 *             leaf abitsTest {
 *                 type typedef-bits;
 *             }
 *             leaf atestBinary {
 *                 type binary;
 *             }
 *             list AugmentSubList {
 *                 key     leaf SubAugName {
 *                     type string;
 *                 }
 *             }
 *         }
 *     }
 * 
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
 * 
 *     identity sal-persisted-dom-test {
 *         base "()IdentitySchemaNodeImpl[base=null, qname=(urn:opendaylight:params:xml:ns:yang:controller:config?revision=2013-04-05)module-type]";
 *         status CURRENT;
 *     }
 * }
 * &lt;/pre&gt;
 * 
 */
public interface SalPersistedDomTestData
    extends
    DataRoot
{




    /**
     * Test the MDSAL model serialization and de serialization
     * 
     */
    SalPersistedDomTest getSalPersistedDomTest();

}

