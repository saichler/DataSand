package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;sal-persisted-dom-test&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/sal-persisted-dom-test.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * list AugmentSubList {
 *     key     leaf SubAugName {
 *         type string;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;sal-persisted-dom-test/sal-persisted-dom-test/AugmentSubList&lt;/i&gt;
 * 
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.AugmentSubListBuilder}.
 * @see org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.AugmentSubListBuilder
 * 
 * 
 */
public interface AugmentSubList
    extends
    ChildOf<SalPersistedDomTest1>,
    Augmentable<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.AugmentSubList>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("http://netconfcentral.org/ns/persistedtest","2014-10-20","AugmentSubList");;

    java.lang.String getSubAugName();

}

