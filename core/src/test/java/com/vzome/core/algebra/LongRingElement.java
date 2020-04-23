package com.vzome.core.algebra;

@SuppressWarnings("serial")
public class LongRingElement extends NumericRingElement<Long, LongRingElement> {
    public static final LongRingElement ZERO = new LongRingElement(0L);
    public static final LongRingElement ONE = new LongRingElement(1L);

    public LongRingElement(Long value) {
        super(value);
    }

    @Override
    public LongRingElement zero() {
        return ZERO;
    }

    @Override
    public LongRingElement one() {
        return ONE;
    }

    @Override
    public LongRingElement plus(LongRingElement that) {
        return new LongRingElement(StrictMath.addExact(value, that.value));
    }

    @Override
    public LongRingElement minus(LongRingElement that) {
        return new LongRingElement(StrictMath.subtractExact(value, that.value));
    }

    @Override
    public LongRingElement negate() {
        return new LongRingElement(StrictMath.subtractExact(0, value));
    }

    @Override
    public LongRingElement times(LongRingElement that) {
        return new LongRingElement(StrictMath.multiplyExact(value, that.value));
    }

    @Override
    public int compareTo(LongRingElement other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        return (obj instanceof LongRingElement) ? compareTo((LongRingElement) obj) == 0 : false;
    }
}
