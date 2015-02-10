package org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020;
import java.io.Serializable;
import java.util.List;
import com.google.common.collect.Lists;


/**
 * The bits built-in type represents a bit set. That is, a bits value is a set of flags identified by small integer position numbers starting at 0.  Each bit number has an assigned name.
 * 
 */
public class TypedefBits
 implements Serializable {
    private static final long serialVersionUID = 5963711052100162391L;
    private final java.lang.Boolean _firstBit;
    private final java.lang.Boolean _secondBit;

    public TypedefBits(java.lang.Boolean _firstBit, java.lang.Boolean _secondBit) {
    
    
        this._firstBit = _firstBit;
        this._secondBit = _secondBit;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public TypedefBits(TypedefBits source) {
        this._firstBit = source._firstBit;
        this._secondBit = source._secondBit;
    }

    public static TypedefBits getDefaultInstance(String defaultValue) {
        List<java.lang.String> properties = Lists.newArrayList("firstBit",
        "secondBit"
        );
        if (!properties.contains(defaultValue)) {
            throw new java.lang.IllegalArgumentException("invalid default parameter");
        }
        int i = 0;
        return new TypedefBits(
        properties.get(i++).equals(defaultValue) ? java.lang.Boolean.TRUE : null,
        properties.get(i++).equals(defaultValue) ? java.lang.Boolean.TRUE : null
        );
    }

    public java.lang.Boolean isFirstBit() {
        return _firstBit;
    }
    
    public java.lang.Boolean isSecondBit() {
        return _secondBit;
    }

    
    public boolean[] getValue() {
        return new boolean[]{
        _firstBit,
        _secondBit
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_firstBit == null) ? 0 : _firstBit.hashCode());
        result = prime * result + ((_secondBit == null) ? 0 : _secondBit.hashCode());
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TypedefBits other = (TypedefBits) obj;
        if (_firstBit == null) {
            if (other._firstBit != null) {
                return false;
            }
        } else if(!_firstBit.equals(other._firstBit)) {
            return false;
        }
        if (_secondBit == null) {
            if (other._secondBit != null) {
                return false;
            }
        } else if(!_secondBit.equals(other._secondBit)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.http.netconfcentral.org.ns.persistedtest.rev141020.TypedefBits.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_firstBit != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_firstBit=");
            builder.append(_firstBit);
         }
        if (_secondBit != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_secondBit=");
            builder.append(_secondBit);
         }
        return builder.append(']').toString();
    }



}

