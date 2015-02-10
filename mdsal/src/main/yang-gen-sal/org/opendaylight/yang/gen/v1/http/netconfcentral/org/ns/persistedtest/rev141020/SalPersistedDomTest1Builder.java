package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyType;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.MyEnumType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.concepts.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.sal.persisted.dom.test.AugmentSubList;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1
 * 
 */
public class SalPersistedDomTest1Builder implements Builder <org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1> {

    private TypedefBits _abitsTest;
    private MyEnumType _aenumtest;
    private byte[] _atestBinary;
    private BigDecimal _atestDecimal64;
    private java.lang.Short _atestInt16;
    private java.lang.Integer _atestInt32;
    private java.lang.Long _atestInt64;
    private java.lang.Byte _atestInt8;
    private MyType _atypedeftest;
    private List<AugmentSubList> _augmentSubList;
    private java.lang.String _mainString2;


    public SalPersistedDomTest1Builder() {
    } 

    public SalPersistedDomTest1Builder(SalPersistedDomTest1 base) {
        this._abitsTest = base.getAbitsTest();
        this._aenumtest = base.getAenumtest();
        this._atestBinary = base.getAtestBinary();
        this._atestDecimal64 = base.getAtestDecimal64();
        this._atestInt16 = base.getAtestInt16();
        this._atestInt32 = base.getAtestInt32();
        this._atestInt64 = base.getAtestInt64();
        this._atestInt8 = base.getAtestInt8();
        this._atypedeftest = base.getAtypedeftest();
        this._augmentSubList = base.getAugmentSubList();
        this._mainString2 = base.getMainString2();
    }


    public TypedefBits getAbitsTest() {
        return _abitsTest;
    }
    
    public MyEnumType getAenumtest() {
        return _aenumtest;
    }
    
    public byte[] getAtestBinary() {
        return _atestBinary == null ? null : _atestBinary.clone();
    }
    
    public BigDecimal getAtestDecimal64() {
        return _atestDecimal64;
    }
    
    public java.lang.Short getAtestInt16() {
        return _atestInt16;
    }
    
    public java.lang.Integer getAtestInt32() {
        return _atestInt32;
    }
    
    public java.lang.Long getAtestInt64() {
        return _atestInt64;
    }
    
    public java.lang.Byte getAtestInt8() {
        return _atestInt8;
    }
    
    public MyType getAtypedeftest() {
        return _atypedeftest;
    }
    
    public List<AugmentSubList> getAugmentSubList() {
        return _augmentSubList;
    }
    
    public java.lang.String getMainString2() {
        return _mainString2;
    }

    public SalPersistedDomTest1Builder setAbitsTest(TypedefBits value) {
        this._abitsTest = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAenumtest(MyEnumType value) {
        this._aenumtest = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestBinary(byte[] value) {
        this._atestBinary = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestDecimal64(BigDecimal value) {
        this._atestDecimal64 = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestInt16(java.lang.Short value) {
        this._atestInt16 = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestInt32(java.lang.Integer value) {
        this._atestInt32 = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestInt64(java.lang.Long value) {
        this._atestInt64 = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtestInt8(java.lang.Byte value) {
        this._atestInt8 = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAtypedeftest(MyType value) {
        this._atypedeftest = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setAugmentSubList(List<AugmentSubList> value) {
        this._augmentSubList = value;
        return this;
    }
    
    public SalPersistedDomTest1Builder setMainString2(java.lang.String value) {
        this._mainString2 = value;
        return this;
    }

    public SalPersistedDomTest1 build() {
        return new SalPersistedDomTest1Impl(this);
    }

    private static final class SalPersistedDomTest1Impl implements SalPersistedDomTest1 {

        public java.lang.Class<org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1.class;
        }

        private final TypedefBits _abitsTest;
        private final MyEnumType _aenumtest;
        private final byte[] _atestBinary;
        private final BigDecimal _atestDecimal64;
        private final java.lang.Short _atestInt16;
        private final java.lang.Integer _atestInt32;
        private final java.lang.Long _atestInt64;
        private final java.lang.Byte _atestInt8;
        private final MyType _atypedeftest;
        private final List<AugmentSubList> _augmentSubList;
        private final java.lang.String _mainString2;


        private SalPersistedDomTest1Impl(SalPersistedDomTest1Builder base) {
            this._abitsTest = base.getAbitsTest();
            this._aenumtest = base.getAenumtest();
            this._atestBinary = base.getAtestBinary();
            this._atestDecimal64 = base.getAtestDecimal64();
            this._atestInt16 = base.getAtestInt16();
            this._atestInt32 = base.getAtestInt32();
            this._atestInt64 = base.getAtestInt64();
            this._atestInt8 = base.getAtestInt8();
            this._atypedeftest = base.getAtypedeftest();
            this._augmentSubList = base.getAugmentSubList();
            this._mainString2 = base.getMainString2();
        }

        @Override
        public TypedefBits getAbitsTest() {
            return _abitsTest;
        }
        
        @Override
        public MyEnumType getAenumtest() {
            return _aenumtest;
        }
        
        @Override
        public byte[] getAtestBinary() {
            return _atestBinary == null ? null : _atestBinary.clone();
        }
        
        @Override
        public BigDecimal getAtestDecimal64() {
            return _atestDecimal64;
        }
        
        @Override
        public java.lang.Short getAtestInt16() {
            return _atestInt16;
        }
        
        @Override
        public java.lang.Integer getAtestInt32() {
            return _atestInt32;
        }
        
        @Override
        public java.lang.Long getAtestInt64() {
            return _atestInt64;
        }
        
        @Override
        public java.lang.Byte getAtestInt8() {
            return _atestInt8;
        }
        
        @Override
        public MyType getAtypedeftest() {
            return _atypedeftest;
        }
        
        @Override
        public List<AugmentSubList> getAugmentSubList() {
            return _augmentSubList;
        }
        
        @Override
        public java.lang.String getMainString2() {
            return _mainString2;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_abitsTest == null) ? 0 : _abitsTest.hashCode());
            result = prime * result + ((_aenumtest == null) ? 0 : _aenumtest.hashCode());
            result = prime * result + ((_atestBinary == null) ? 0 : Arrays.hashCode(_atestBinary));
            result = prime * result + ((_atestDecimal64 == null) ? 0 : _atestDecimal64.hashCode());
            result = prime * result + ((_atestInt16 == null) ? 0 : _atestInt16.hashCode());
            result = prime * result + ((_atestInt32 == null) ? 0 : _atestInt32.hashCode());
            result = prime * result + ((_atestInt64 == null) ? 0 : _atestInt64.hashCode());
            result = prime * result + ((_atestInt8 == null) ? 0 : _atestInt8.hashCode());
            result = prime * result + ((_atypedeftest == null) ? 0 : _atypedeftest.hashCode());
            result = prime * result + ((_augmentSubList == null) ? 0 : _augmentSubList.hashCode());
            result = prime * result + ((_mainString2 == null) ? 0 : _mainString2.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1 other = (org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.SalPersistedDomTest1)obj;
            if (_abitsTest == null) {
                if (other.getAbitsTest() != null) {
                    return false;
                }
            } else if(!_abitsTest.equals(other.getAbitsTest())) {
                return false;
            }
            if (_aenumtest == null) {
                if (other.getAenumtest() != null) {
                    return false;
                }
            } else if(!_aenumtest.equals(other.getAenumtest())) {
                return false;
            }
            if (_atestBinary == null) {
                if (other.getAtestBinary() != null) {
                    return false;
                }
            } else if(!Arrays.equals(_atestBinary, other.getAtestBinary())) {
                return false;
            }
            if (_atestDecimal64 == null) {
                if (other.getAtestDecimal64() != null) {
                    return false;
                }
            } else if(!_atestDecimal64.equals(other.getAtestDecimal64())) {
                return false;
            }
            if (_atestInt16 == null) {
                if (other.getAtestInt16() != null) {
                    return false;
                }
            } else if(!_atestInt16.equals(other.getAtestInt16())) {
                return false;
            }
            if (_atestInt32 == null) {
                if (other.getAtestInt32() != null) {
                    return false;
                }
            } else if(!_atestInt32.equals(other.getAtestInt32())) {
                return false;
            }
            if (_atestInt64 == null) {
                if (other.getAtestInt64() != null) {
                    return false;
                }
            } else if(!_atestInt64.equals(other.getAtestInt64())) {
                return false;
            }
            if (_atestInt8 == null) {
                if (other.getAtestInt8() != null) {
                    return false;
                }
            } else if(!_atestInt8.equals(other.getAtestInt8())) {
                return false;
            }
            if (_atypedeftest == null) {
                if (other.getAtypedeftest() != null) {
                    return false;
                }
            } else if(!_atypedeftest.equals(other.getAtypedeftest())) {
                return false;
            }
            if (_augmentSubList == null) {
                if (other.getAugmentSubList() != null) {
                    return false;
                }
            } else if(!_augmentSubList.equals(other.getAugmentSubList())) {
                return false;
            }
            if (_mainString2 == null) {
                if (other.getMainString2() != null) {
                    return false;
                }
            } else if(!_mainString2.equals(other.getMainString2())) {
                return false;
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("SalPersistedDomTest1 [");
            boolean first = true;
        
            if (_abitsTest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_abitsTest=");
                builder.append(_abitsTest);
             }
            if (_aenumtest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_aenumtest=");
                builder.append(_aenumtest);
             }
            if (_atestBinary != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestBinary=");
                builder.append(Arrays.toString(_atestBinary));
             }
            if (_atestDecimal64 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestDecimal64=");
                builder.append(_atestDecimal64);
             }
            if (_atestInt16 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestInt16=");
                builder.append(_atestInt16);
             }
            if (_atestInt32 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestInt32=");
                builder.append(_atestInt32);
             }
            if (_atestInt64 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestInt64=");
                builder.append(_atestInt64);
             }
            if (_atestInt8 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atestInt8=");
                builder.append(_atestInt8);
             }
            if (_atypedeftest != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_atypedeftest=");
                builder.append(_atypedeftest);
             }
            if (_augmentSubList != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_augmentSubList=");
                builder.append(_augmentSubList);
             }
            if (_mainString2 != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mainString2=");
                builder.append(_mainString2);
             }
            return builder.append(']').toString();
        }
    }

}
