package com.vzome.core.algebra;

import com.vzome.core.algebra.AlgebraicStructures.RingElement;

@SuppressWarnings("serial")
public abstract class NumericRingElement<N extends Number, E extends RingElement<E>> extends Number
        implements RingElement<E>, Comparable<E> {
    
    protected final N value;
    
    private final boolean isZero;
    private final boolean isOne;

    public NumericRingElement(N value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = value;
        String s = value.toString();
        isZero = s.equals("0") || s.equals("0.0");
        isOne = s.equals("1") || s.equals("1.0");
    }

    @Override
    public boolean isZero() {
        return isZero;
    }

    @Override
    public boolean isOne() {
        return isOne;
    }

    public N getValue() {
        return value;
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public double evaluate() {
        return doubleValue();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
