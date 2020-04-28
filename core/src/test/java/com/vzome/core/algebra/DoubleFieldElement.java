package com.vzome.core.algebra;

import com.vzome.core.algebra.AlgebraicStructures.FieldElement;

@SuppressWarnings("serial")
public class DoubleFieldElement 
    extends NumericRingElement<Double, DoubleFieldElement>
    implements FieldElement<DoubleFieldElement>
{
    public static final DoubleFieldElement ZERO = new DoubleFieldElement(0d);
    public static final DoubleFieldElement ONE = new DoubleFieldElement(1d);

    public DoubleFieldElement(Double value) {
        super(value);
    }

    @Override
    public DoubleFieldElement zero() {
        return ZERO;
    }

    @Override
    public DoubleFieldElement one() {
        return ONE;
    }

    @Override
    public DoubleFieldElement plus(DoubleFieldElement that) {
        return new DoubleFieldElement(value + that.value);
    }

    @Override
    public DoubleFieldElement minus(DoubleFieldElement that) {
        return new DoubleFieldElement(value - that.value);
    }

    @Override
    public DoubleFieldElement negate() {
        return new DoubleFieldElement(-value);
    }

    @Override
    public DoubleFieldElement times(DoubleFieldElement that) {
        return new DoubleFieldElement(value * that.value);
    }

    @Override
    public DoubleFieldElement reciprocal() {
        if (isZero()) {
            throw new IllegalArgumentException("Denominator is zero");
        }
        return new DoubleFieldElement(1d / value);
    }

    @Override
    public int compareTo(DoubleFieldElement other) {
        return value.compareTo(other.doubleValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        return (obj instanceof DoubleFieldElement) ? compareTo((DoubleFieldElement) obj) == 0 : false;
    }

    public DoubleFieldElement create(Double value) {
        return this.value == value ? this : new DoubleFieldElement(value);
    }

    @Override
    public DoubleFieldElement create(Integer value) {
        return create(Double.parseDouble(value.toString()));
    }

}
