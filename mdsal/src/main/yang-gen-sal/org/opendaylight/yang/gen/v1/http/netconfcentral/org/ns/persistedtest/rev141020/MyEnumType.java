package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;


/**
 * The enumeration built-in type represents values from a set of assigned names.
 * 
 */
public enum MyEnumType {
    TestEnum1(1),
    
    TestEnum2(2),
    
    TestEnum3(3)
    ;


    int value;
    private static final java.util.Map<java.lang.Integer, MyEnumType> VALUE_MAP;

    static {
        final com.google.common.collect.ImmutableMap.Builder<java.lang.Integer, MyEnumType> b = com.google.common.collect.ImmutableMap.builder();
        for (MyEnumType enumItem : MyEnumType.values())
        {
            b.put(enumItem.value, enumItem);
        }

        VALUE_MAP = b.build();
    }

    private MyEnumType(int value) {
        this.value = value;
    }
    
    /**
     * @return integer value
     */
    public int getIntValue() {
        return value;
    }

    /**
     * @param valueArg
     * @return corresponding MyEnumType item
     */
    public static MyEnumType forValue(int valueArg) {
        return VALUE_MAP.get(valueArg);
    }
}
