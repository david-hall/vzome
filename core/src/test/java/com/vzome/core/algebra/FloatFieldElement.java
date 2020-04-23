package com.vzome.core.algebra;

import com.vzome.core.algebra.AlgebraicStructures.FieldElement;

@SuppressWarnings("serial")
public class FloatFieldElement extends NumericRingElement<Float, FloatFieldElement>
        implements FieldElement<FloatFieldElement> {
    public static final FloatFieldElement ZERO = new FloatFieldElement(0f);
    public static final FloatFieldElement ONE = new FloatFieldElement(1f);

    public FloatFieldElement(Float value) {
        super(value);
    }

    @Override
    public boolean isZero() {
        return value == 0;
    }

    @Override
    public boolean isOne() {
        return value == 1;
    }

    @Override
    public FloatFieldElement zero() {
        return ZERO;
    }

    @Override
    public FloatFieldElement one() {
        return ONE;
    }

    @Override
    public FloatFieldElement plus(FloatFieldElement that) {
        return new FloatFieldElement(value + that.value);
    }

    @Override
    public FloatFieldElement minus(FloatFieldElement that) {
        return new FloatFieldElement(value - that.value);
    }

    @Override
    public FloatFieldElement negate() {
        return new FloatFieldElement(-value);
    }

    @Override
    public FloatFieldElement times(FloatFieldElement that) {
        return new FloatFieldElement(value * that.value);
    }

    @Override
    public FloatFieldElement reciprocal() {
        if (isZero()) {
            throw new IllegalArgumentException("Denominator is zero");
        }
        return new FloatFieldElement(1f / value);
    }

    @Override
    public int compareTo(FloatFieldElement other) {
        return value.compareTo(other.floatValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        return (obj instanceof FloatFieldElement) ? compareTo((FloatFieldElement) obj) == 0 : false;
    }
}
