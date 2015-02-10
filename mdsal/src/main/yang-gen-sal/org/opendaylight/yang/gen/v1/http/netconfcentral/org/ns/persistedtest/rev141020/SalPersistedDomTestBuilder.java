package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.SalPersistedSubContainer;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.ListWithKey;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyEnumType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.NoKeysubList;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.concepts.Builder;
import java.math.BigDecimal;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest
 * 
 */
public class SalPersistedDomTestBuilder implements Builder <org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest> {

    private TypedefBits _bitsTest;
    private java.lang.Integer _countID;
    private MyEnumType _enumtest;
    private List<ListWithKey> _listWithKey;
    private java.lang.String _mainString;
    private List<NoKeysubList> _noKeysubList;
    private SalPersistedSubContainer _salPersistedSubContainer;
    private byte[] _testBinary;
    private BigDecimal _testDecimal64;
    private java.lang.Short _testInt16;
    private java.lang.Integer _testInt32;
    private java.lang.Long _testInt64;
    private java.lang.Byte _testInt8;
    private MyType _typedeftest;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> augmentation = new HashMap<>();

    public SalPersistedDomTestBuilder() {
    } 

    public SalPersistedDomTestBuilder(SalPersistedDomTest base) {
        this._bitsTest = base.getBitsTest();
        this._countID = base.getCountID();
        this._enumtest = base.getEnumtest();
        this._listWithKey = base.getListWithKey();
        this._mainString = base.getMainString();
        this._noKeysubList = base.getNoKeysubList();
        this._salPersistedSubContainer = base.getSalPersistedSubContainer();
        this._testBinary = base.getTestBinary();
        this._testDecimal64 = base.getTestDecimal64();
        this._testInt16 = base.getTestInt16();
        this._testInt32 = base.getTestInt32();
        this._testInt64 = base.getTestInt64();
        this._testInt8 = base.getTestInt8();
        this._typedeftest = base.getTypedeftest();
        if (base instanceof SalPersistedDomTestImpl) {
            SalPersistedDomTestImpl _impl = (SalPersistedDomTestImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public TypedefBits getBitsTest() {
        return _bitsTest;
    }
    
    public java.lang.Integer getCountID() {
        return _countID;
    }
    
    public MyEnumType getEnumtest() {
        return _enumtest;
    }
    
    public List<ListWithKey> getListWithKey() {
        return _listWithKey;
    }
    
    public java.lang.String getMainString() {
        return _mainString;
    }
    
    public List<NoKeysubList> getNoKeysubList() {
        return _noKeysubList;
    }
    
    public SalPersistedSubContainer getSalPersistedSubContainer() {
        return _salPersistedSubContainer;
    }
    
    public byte[] getTestBinary() {
        return _testBinary == null ? null : _testBinary.clone();
    }
    
    public BigDecimal getTestDecimal64() {
        return _testDecimal64;
    }
    
    public java.lang.Short getTestInt16() {
        return _testInt16;
    }
    
    public java.lang.Integer getTestInt32() {
        return _testInt32;
    }
    
    public java.lang.Long getTestInt64() {
        return _testInt64;
    }
    
    public java.lang.Byte getTestInt8() {
        return _testInt8;
    }
    
    public MyType getTypedeftest() {
        return _typedeftest;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public SalPersistedDomTestBuilder setBitsTest(TypedefBits value) {
        this._bitsTest = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setCountID(java.lang.Integer value) {
        this._countID = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setEnumtest(MyEnumType value) {
        this._enumtest = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setListWithKey(List<ListWithKey> value) {
        this._listWithKey = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setMainString(java.lang.String value) {
        this._mainString = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setNoKeysubList(List<NoKeysubList> value) {
        this._noKeysubList = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setSalPersistedSubContainer(SalPersistedSubContainer value) {
        this._salPersistedSubContainer = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestBinary(byte[] value) {
        this._testBinary = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestDecimal64(BigDecimal value) {
        this._testDecimal64 = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestInt16(java.lang.Short value) {
        this._testInt16 = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestInt32(java.lang.Integer value) {
        this._testInt32 = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestInt64(java.lang.Long value) {
        this._testInt64 = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTestInt8(java.lang.Byte value) {
        this._testInt8 = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder setTypedeftest(MyType value) {
        this._typedeftest = value;
        return this;
    }
    
    public SalPersistedDomTestBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public SalPersistedDomTestBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public SalPersistedDomTest build() {
        return new SalPersistedDomTestImpl(this);
    }

    private static final class SalPersistedDomTestImpl implements SalPersistedDomTest {

        public java.lang.Class<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest.class;
        }

        private final TypedefBits _bitsTest;
        private final java.lang.Integer _countID;
        private final MyEnumType _enumtest;
        private final List<ListWithKey> _listWithKey;
        private final java.lang.String _mainString;
        private final List<NoKeysubList> _noKeysubList;
        private final SalPersistedSubContainer _salPersistedSubContainer;
        private final byte[] _testBinary;
        private final BigDecimal _testDecimal64;
        private final java.lang.Short _testInt16;
        private final java.lang.Integer _testInt32;
        private final java.lang.Long _testInt64;
        private final java.lang.Byte _testInt8;
        private final MyType _typedeftest;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> augmentation = new HashMap<>();

        private SalPersistedDomTestImpl(SalPersistedDomTestBuilder base) {
            this._bitsTest = base.getBitsTest();
            this._countID = base.getCountID();
            this._enumtest = base.getEnumtest();
            this._listWithKey = base.getListWithKey();
            this._mainString = base.getMainString();
            this._noKeysubList = base.getNoKeysubList();
            this._salPersistedSubContainer = base.getSalPersistedSubContainer();
            this._testBinary = base.getTestBinary();
            this._testDecimal64 = base.getTestDecimal64();
            this._testInt16 = base.getTestInt16();
            this._testInt32 = base.getTestInt32();
            this._testInt64 = base.getTestInt64();
            this._testInt8 = base.getTestInt8();
            this._typedeftest = base.getTypedeftest();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public TypedefBits getBitsTest() {
            return _bitsTest;
        }
        
        @Override
        public java.lang.Integer getCountID() {
            return _countID;
        }
        
        @Override
        public MyEnumType getEnumtest() {
            return _enumtest;
        }
        
        @Override
        public List<ListWithKey> getListWithKey() {
            return _listWithKey;
        }
        
        @Override
        public java.lang.String getMainString() {
            return _mainString;
        }
        
        @Override
        public List<NoKeysubList> getNoKeysubList() {
            return _noKeysubList;
        }
        
        @Override
        public SalPersistedSubContainer getSalPersistedSubContainer() {
            return _salPersistedSubContainer;
        }
        
        @Override
        public byte[] getTestBinary() {
            return _testBinary == null ? null : _testBinary.clone();
        }
        
        @Override
        public BigDecimal getTestDecimal64() {
            return _testDecimal64;
        }
        
        @Override
        public java.lang.Short getTestInt16() {
            return _testInt16;
        }
        
        @Override
        public java.lang.Integer getTestInt32() {
            return _testInt32;
        }
        
        @Override
        public java.lang.Long getTestInt64() {
            return _testInt64;
        }
        
        @Override
        public java.lang.Byte getTestInt8() {
            return _testInt8;
        }
        
        @Override
        public MyType getTypedeftest() {
            return _typedeftest;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_bitsTest == null) ? 0 : _bitsTest.hashCode());
            result = prime * result + ((_countID == null) ? 0 : _countID.hashCode());
            result = prime * result + ((_enumtest == null) ? 0 : _enumtest.hashCode());
            result = prime * result + ((_listWithKey == null) ? 0 : _listWithKey.hashCode());
            result = prime * result + ((_mainString == null) ? 0 : _mainString.hashCode());
            result = prime * result + ((_noKeysubList == null) ? 0 : _noKeysubList.hashCode());
            result = prime * result + ((_salPersistedSubContainer == null) ? 0 : _salPersistedSubContainer.hashCode());
            result = prime * result + ((_testBinary == null) ? 0 : Arrays.hashCode(_testBinary));
            result = prime * result + ((_testDecimal64 == null) ? 0 : _testDecimal64.hashCode());
            result = prime * result + ((_testInt16 == null) ? 0 : _testInt16.hashCode());
            result = prime * result + ((_testInt32 == null) ? 0 : _testInt32.hashCode());
            result = prime * result + ((_testInt64 == null) ? 0 : _testInt64.hashCode());
            result = prime * result + ((_testInt8 == null) ? 0 : _testInt8.hashCode());
            result = prime * result + ((_typedeftest == null) ? 0 : _typedeftest.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest other = (org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest)obj;
            if (_bitsTest == null) {
                if (other.getBitsTest() != null) {
                    return false;
                }
            } else if(!_bitsTest.equals(other.getBitsTest())) {
                return false;
            }
            if (_countID == null) {
                if (other.getCountID() != null) {
                    return false;
                }
            } else if(!_countID.equals(other.getCountID())) {
                return false;
            }
            if (_enumtest == null) {
                if (other.getEnumtest() != null) {
                    return false;
                }
            } else if(!_enumtest.equals(other.getEnumtest())) {
                return false;
            }
            if (_listWithKey == null) {
                if (other.getListWithKey() != null) {
                    return false;
                }
            } else if(!_listWithKey.equals(other.getListWithKey())) {
                return false;
            }
            if (_mainString == null) {
                if (other.getMainString() != null) {
                    return false;
                }
            } else if(!_mainString.equals(other.getMainString())) {
                return false;
            }
            if (_noKeysubList == null) {
                if (other.getNoKeysubList() != null) {
                    return false;
                }
            } else if(!_noKeysubList.equals(other.getNoKeysubList())) {
                return false;
            }
            if (_salPersistedSubContainer == null) {
                if (other.getSalPersistedSubContainer() != null) {
                    return false;
                }
            } else if(!_salPersistedSubContainer.equals(other.getSalPersistedSubContainer())) {
                return false;
            }
            if (_testBinary == null) {
                if (other.getTestBinary() != null) {
                    return false;
                }
            } else if(!Arrays.equals(_testBinary, other.getTestBinary())) {
                return false;
            }
            if (_testDecimal64 == null) {
                if (other.getTestDecimal64() != null) {
                    return false;
                }
            } else if(!_testDecimal64.equals(other.getTestDecimal64())) {
                return false;
            }
            if (_testInt16 == null) {
                if (other.getTestInt16() != null) {
                    return false;
                }
            } else if(!_testInt16.equals(other.getTestInt16())) {
                return false;
            }
            if (_testInt32 == null) {
                if (other.getTestInt32() != null) {
                    return false;
                }
            } else if(!_testInt32.equals(other.getTestInt32())) {
                return false;
            }
            if (_testInt64 == null) {
                if (other.getTestInt64() != null) {
                    return false;
                }
            } else if(!_testInt64.equals(other.getTestInt64())) {
                return false;
            }
            if (_testInt8 == null) {
                if (other.getTestInt8() != null) {
                    return false;
                }
            } else if(!_testInt8.equals(other.getTestInt8())) {
                return false;
            }
            if (_typedeftest == null) {
                if (other.getTypedeftest() != null) {
                    return false;
                }
            } else if(!_typedeftest.equals(other.getTypedeftest())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                SalPersistedDomTestImpl otherImpl = (SalPersistedDomTestImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>>, Augmentation<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("SalPersistedDomTest [");
            boolean first = true;
        
            if (_bitsTest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_bitsTest=");
                builder.append(_bitsTest);
             }
            if (_countID != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_countID=");
                builder.append(_countID);
             }
            if (_enumtest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_enumtest=");
                builder.append(_enumtest);
             }
            if (_listWithKey != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_listWithKey=");
                builder.append(_listWithKey);
             }
            if (_mainString != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainString=");
                builder.append(_mainString);
             }
            if (_noKeysubList != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_noKeysubList=");
                builder.append(_noKeysubList);
             }
            if (_salPersistedSubContainer != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_salPersistedSubContainer=");
                builder.append(_salPersistedSubContainer);
             }
            if (_testBinary != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testBinary=");
                builder.append(Arrays.toString(_testBinary));
             }
            if (_testDecimal64 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testDecimal64=");
                builder.append(_testDecimal64);
             }
            if (_testInt16 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testInt16=");
                builder.append(_testInt16);
             }
            if (_testInt32 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testInt32=");
                builder.append(_testInt32);
             }
            if (_testInt64 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testInt64=");
                builder.append(_testInt64);
             }
            if (_testInt8 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_testInt8=");
                builder.append(_testInt8);
             }
            if (_typedeftest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_typedeftest=");
                builder.append(_typedeftest);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
