package com.vzome.core.algebra;

import static com.vzome.core.algebra.ParameterizedFields.printMatrices;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

/**
 * @author David Hall
 */
public class PhiPlusSqrtFieldTest {

    @Test
    public void testGetSqrtSubField() {
        System.out.println("testGetSqrtSubField");
        Integer operand = 7; // any positive non-perfect square
        PhiPlusSqrtField field = new PhiPlusSqrtField(operand);
        SqrtField subField = field.getSqrtSubField();
        assertNotNull(subField);
        assertFalse(subField.isPerfectSquare());
        assertEquals( operand, field.operand );
        assertEquals( operand, subField.operand );

        operand *= operand;
        field = new PhiPlusSqrtField(operand);
        subField = field.getSqrtSubField();
        assertNotNull(subField);
        assertTrue(subField.isPerfectSquare());
        assertEquals( operand, field.operand );
        assertEquals( operand, subField.operand );
    }

    @Test
    public void testNormalization() {
        System.out.println("testNormalization");
        final int[] factors = { 3, 5, 7, 11 }; // prefer the factors to be relatively prime
        
        Integer operand = 2; // start with a non-perfect square, preferably not one of the factors
        PhiPlusSqrtField field = new PhiPlusSqrtField(operand);
        assertFalse(field.getSqrtSubField().isPerfectSquare());
        assertEquals(field.getOrder(), factors.length);
        AlgebraicNumber N = field.createAlgebraicNumber(factors);
        System.out.println(field.getName() + ": " + N);
        BigRational[] bigFactors = N.getFactors();
        assertEquals( bigFactors[0], new BigRational(factors[0]) );
        assertEquals( bigFactors[1], new BigRational(factors[1]) );
        assertEquals( bigFactors[2], new BigRational(factors[2]) );
        assertEquals( bigFactors[3], new BigRational(factors[3]) );
        assertFalse( bigFactors[2].isZero() );
        assertFalse( bigFactors[3].isZero() );

        // make operand a perfect square
        operand *= operand;
        field = new PhiPlusSqrtField(operand);
        assertTrue(field.getSqrtSubField().isPerfectSquare());
        assertEquals(field.getOrder(), factors.length);
        N = field.createAlgebraicNumber(factors);
        System.out.println(field.getName() + ": " + N);
        bigFactors = N.getFactors();
        assertEquals(field.getOrder(), bigFactors.length);
        // TODO: verify the first 2 factors without hard coding the values.
        // Derive the expected result from factors[]
        // TODO: Try a few negative numbers and 0's in factors[]
        assertEquals( bigFactors[0], new BigRational(17) );
        assertEquals( bigFactors[1], new BigRational(27) );
        assertTrue( bigFactors[2].isZero() );
        assertTrue( bigFactors[3].isZero() );
    }

    @Test
    public void testMultiplication() {
        System.out.println("testMultiplication");

        Integer operand = 3; // start with a non-perfect square
        PhiPlusSqrtField field = new PhiPlusSqrtField(operand);
//        assertFalse(field.getSqrtSubField().isPerfectSquare);

        AlgebraicNumber one = field.createAlgebraicNumber(new int[] { 1, 0, 0, 0 });
        AlgebraicNumber phi = field.createAlgebraicNumber(new int[] { 0, 1, 0, 0 });
        AlgebraicNumber sqrt = field.createAlgebraicNumber(new int[] { 0, 0, 1, 0 });
        AlgebraicNumber phiSqrt = field.createAlgebraicNumber(new int[] { 0, 0, 0, 1 });
        AlgebraicNumber[] all = { one, phi, sqrt, phiSqrt };

        System.out.println(field.getName());
        System.out.println("Squares:");
        for( AlgebraicNumber num : all ) {
            System.out.println("(" + num + ")\u00b2\t= " + num.times(num));
        }
        System.out.println();

        String tabs = "\t\t";
        System.out.println(field.getName());
        System.out.println("Multiplication Table (Algebraic):");
        System.out.print(tabs);
        for( AlgebraicNumber col : all ) {
            System.out.print(col + tabs);
        }
        System.out.println();
        for( AlgebraicNumber col : all ) {
            System.out.print(col + tabs);
            for (AlgebraicNumber row : all) {
                System.out.print(col.times(row) + tabs);
            }
            System.out.println();
        }
        System.out.println();

//        System.out.println(field.getName());
//        System.out.println("Multiplication Table (Decimal Approximations):");
//        System.out.print(tabs);
//        for( AlgebraicNumber col : all ) {
//            System.out.print(col.evaluate() + tabs);
//        }
//        System.out.println();
//        for( AlgebraicNumber col : all ) {
//            System.out.print(col.evaluate() + tabs);
//            for (AlgebraicNumber row : all) {
//                System.out.print(col.times(row).evaluate() + tabs);
//            }
//            System.out.println();
//        }
//        System.out.println();

//        System.out.println(field.getName());
//        System.out.println("Reciprocals:");
//        for (AlgebraicNumber num : all) {
//            System.out.println("1/(" + num + ") =" + tabs + num.reciprocal());
//        }
//        System.out.println();
    }

    @Test
    public void printPhiPlusSqrtFieldMatrices() {
        System.out.println("printPhiPlusSqrtFieldMatrices");
        printMatrices(new SqrtField(3));
        printMatrices(new SqrtField(4));
        printMatrices(new PhiPlusSqrtField(3));
        printMatrices(new PhiPlusSqrtField(4));
    }

    /*
    This field with operand 3 the basis for the vertices
    of the 4D Triangular Hebesphenorotundaeic Rhombochoron
    See  http://eusebeia.dyndns.org/4d/J92_rhombochoron

    The Cartesian coordinates of the 66 vertices of the J92 rhombochoron, with edge length 2 and centered on the origin, are:

    ±(0, 2/√3, φ2/√3, 0)
    ±(±1, -1/√3, φ2/√3, 0)
    ±(±1, φ3/√3, φ/√3, ±1)
    ±(±φ2, -1/(φ√3), φ/√3, ±1)
    ±(±φ, -(φ+2)/√3, φ/√3, ±1)
    ±(±φ2, φ2/√3, 1/√3, ±φ)
    ±(0, -2φ2/√3, 1/√3, ±φ)
    (±1, ±√3, 0, ±φ2)
    (±2, 0, 0, ±φ2)
    (±φ2, (φ+3)/√3, 1/(φ√3), 0)
    (±φ2, -(φ+3)/√3, -1/(φ√3), 0)
    (±1, (3+2φ)/√3, -1/(φ√3), 0)
    (±1, -(3+2φ)/√3, 1/(φ√3), 0)
    (±(φ+2), φ/√3, 1/(φ√3), 0)
    (±(φ+2), -φ/√3, -1/(φ√3), 0)

    TODO: multiply all of these by φ√3 to get rid of all of the denominators
    then divide all of them by the same amount to restore the original values
    
    */
    @Test
    public void testTriangularHebesphenorotundaeicRhombochoron() {
        System.out.println("testTriangularHebesphenorotundaeicRhombochoron");
        PhiPlusSqrtField field = new PhiPlusSqrtField(3);
        // TODO:
    }

}
