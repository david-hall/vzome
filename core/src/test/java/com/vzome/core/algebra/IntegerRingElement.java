package com.vzome.core.algebra;

@SuppressWarnings("serial")
public class IntegerRingElement 
    extends NumericRingElement<Integer, IntegerRingElement>
{
    public static final IntegerRingElement ZERO = new IntegerRingElement(0);
    public static final IntegerRingElement ONE = new IntegerRingElement(1);

    public IntegerRingElement(Integer value) {
        super(value);
    }

    @Override
    public IntegerRingElement zero() {
        return ZERO;
    }

    @Override
    public IntegerRingElement one() {
        return ONE;
    }

    @Override
    public IntegerRingElement plus(IntegerRingElement that) {
        return new IntegerRingElement(StrictMath.addExact(value, that.value));
    }

    @Override
    public IntegerRingElement minus(IntegerRingElement that) {
        return new IntegerRingElement(StrictMath.subtractExact(value, that.value));
    }

    @Override
    public IntegerRingElement negate() {
        return new IntegerRingElement(StrictMath.subtractExact(0, value));
    }

    @Override
    public IntegerRingElement times(IntegerRingElement that) {
        return new IntegerRingElement(StrictMath.multiplyExact(value, that.value));
    }

    @Override
    public int compareTo(IntegerRingElement other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        return (obj instanceof IntegerRingElement) ? compareTo((IntegerRingElement) obj) == 0 : false;
    }

    @Override
    public IntegerRingElement create(Integer value) {
        return this.value == value ? this : new IntegerRingElement(value);
    }

}
