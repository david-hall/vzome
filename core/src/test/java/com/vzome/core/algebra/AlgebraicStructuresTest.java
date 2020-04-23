package com.vzome.core.algebra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.vzome.core.algebra.AlgebraicStructures.FieldElement;
import com.vzome.core.algebra.AlgebraicStructures.RingElement;

public class AlgebraicStructuresTest {

    static Set<RingElement<?>> testedElements = new HashSet<>(); // lets JaCoCo verify hashcode() 
    
    @Test
    public void testNullConstructorArgs() {
        final String failMsg = "Expected NullPointerException"; 
        final String npeMsg = "value cannot be null"; 
        try {
            new LongRingElement(null);
            fail(failMsg);
        } catch(NullPointerException ex) {
            assertEquals(ex.getMessage(), npeMsg); 
        }
        try {
            new IntegerRingElement(null);
            fail(failMsg);
        } catch(NullPointerException ex) {
            assertEquals(ex.getMessage(), npeMsg); 
        }
        try {
            new DoubleFieldElement(null);
            fail(failMsg);
        } catch(NullPointerException ex) {
            assertEquals(ex.getMessage(), npeMsg); 
        }
        try {
            new FloatFieldElement(null);
            fail(failMsg);
        } catch(NullPointerException ex) {
            assertEquals(ex.getMessage(), npeMsg); 
        }
    }

    @Test
    public void testRingElements() {
        AlgebraicField field = new PentagonField();
        testedElements.clear();
        // use the same value for several classes to be sure they hash differently
        final Double n = 321.0987d;
        final int d = 17;
        int qty = 0;
        qty += testNumericRingElement(new LongRingElement(n.longValue()));
        qty += testNumericRingElement(new IntegerRingElement(n.intValue()));
        qty += testNumericFieldElement(new DoubleFieldElement(n.doubleValue()));
        qty += testNumericFieldElement(new FloatFieldElement(n.floatValue()));
        
        qty += testLegacyFieldElement(new BigRational(n.longValue()));
        qty += testLegacyFieldElement(new BigRational(n.longValue(), d));
        qty += testLegacyFieldElement(field.createRational(n.longValue()));
        qty += testLegacyFieldElement(field.createAlgebraicNumber(new int[] {n.intValue(), d} ));
        assertTrue(qty > 0);
        assertEquals(qty, testedElements.size());
    }

    private static <N extends Number, E extends NumericRingElement<N, E>> int testNumericRingElement(E element) {
        System.out.println(element.getClass().getSimpleName() + "(" + element.toString() + ")");
        assertEquals(element, element); // silly looking test lets JaCoCo verify parts of equals() 
        assertNotEquals(element, element.plus(element.one())); // verify parts of equals() 
        assertNotEquals(element, null); // verify parts of equals()
        assertNotEquals(element, new Object()); // verify parts of equals() 
        assertFalse(element.isZero());
        assertFalse(element.isOne());
        assertTrue(element.zero().isZero());
        assertTrue(element.one().isOne());
        E neg = element.negate();
        assertFalse(neg.isOne());
        assertTrue(element.plus(neg).isZero());
        E zmin = element.zero().minus(element);
        assertTrue(neg.equals(zmin));
        assertTrue(zmin.equals(neg));
        E squared = element.times(element);
        assertEquals(-1, element.compareTo(squared));
        assertEquals(-1, element.zero().compareTo(squared)); // positive
        assertEquals(element.evaluate(), element.getValue().doubleValue(), 0d);
        testedElements.add(element);
        return 1;
    }

    private static <N extends Number, E extends NumericRingElement<N, E> & FieldElement<E>> int  testNumericFieldElement(E element) {
        assertFalse(element.isZero());
        assertFalse(element.isOne());
        E reciprocal = element.reciprocal();
        assertTrue(element.times(reciprocal).isOne());
        E one = element.one();
        assertEquals(element, element.dividedBy(one));
        E zero = element.zero();
        try {
            zero.reciprocal();
            fail("Expected divide by zero exception");
        } catch(IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Denominator is zero"); 
        }
        try {
            element.dividedBy(zero);
            fail("Expected divide by zero exception");
        } catch(IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Denominator is zero"); 
        }
        return testNumericRingElement(element);
    }
    
    private static <T extends FieldElement<T> & Comparable<T>> int testLegacyFieldElement(T element) {
        System.out.println(element.getClass().getSimpleName() + "(" + element.toString() + ")");
        assertFalse(element.isZero());
        assertFalse(element.isOne());
        T reciprocal = element.reciprocal();
        assertTrue(element.times(reciprocal).isOne());
        T one = element.one();
        assertEquals(element, element.dividedBy(one));
        T zero = element.zero();
        try {
            zero.reciprocal();
            fail("Expected divide by zero exception");
        } catch(IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Denominator is zero"); 
        }
        try {
            element.dividedBy(zero);
            fail("Expected divide by zero exception");
        } catch(IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Denominator is zero"); 
        }
        testedElements.add(element);
        return 1;
    }

}
