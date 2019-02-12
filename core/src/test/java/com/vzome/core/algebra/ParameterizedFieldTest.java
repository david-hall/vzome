package com.vzome.core.algebra;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

import com.vzome.core.math.RealVector;

/**
 * @author David Hall
 */
public class ParameterizedFieldTest {

    @Test
    public void testHighOrderPolygonFieldConstructor() {
        PolygonField field = new PolygonField(120);
        assertNotNull(field);
    }

    @Test
    public void testParameterizedFieldClassesNotEqual() {
        // differently derived classes with the same operand should not be equal
        int operand = 4;
        SqrtField sqrtField = new SqrtField(operand);
        PolygonField polyField = new PolygonField(operand);
        assertFalse(sqrtField.equals(polyField));
        }

    @Test
    public void testPerfectSquares() {
        for (int i = 1; i <= 10; i++) {
            SqrtField field = new SqrtField(i * i);
//            System.out.println(field.radicand() + " is " + (field.isPerfectSquare ? "" : "NOT") + " a Perfect Square");
            assertTrue(field.isPerfectSquare());

            field = new SqrtField(field.radicand() + 1);
//            System.out.println(field.radicand() + " is " + (field.isPerfectSquare ? "" : "NOT") + " a Perfect Square");
            assertFalse(field.isPerfectSquare());
        }
    }

    @Test
    public void printSqrtFieldMatrices() {
        for(int i = 1; i <= 16; i++) {
            ParameterizedFields.printMatrices( new SqrtField(i) );
        }
    }

    @Test
    public void printPolygonFieldNormalization() {
        for(int i = 22; i <= 22; i++) {
            ParameterizedFields.printNormalization( new PolygonField(i) );
        }
    }

    @Test
    public void printPolygonFieldMatrices() {
        int[] sides = {
                // I have a pattern that seems to work for 3 times any integer power of 2
                // but so far I have only hard coded it for specific cases:
                // TODO: generalize for all applicable cases
//                3 * 2,  // 6
//                3 * 4,  // 12
//                3 * 8,  // 24
//                3 * 16, // 48
                
                // I have a normalizer that works for 10 too... TODO: generalize to other multiples of 5 or 10 
                // or possibly 5 * any integer powers of 2? What's possible?
//                5 * 2, // 10

//                9, 
//                10,
//                12,
                14, 
//                15, 
//                18, 
//                20,         		
//                21, 

                // TODO: Confirm that up thru 21 are all working
                22, // TODO No obvious simple normalization for 22 yet. 
//                24
                25, // TODO? normalization for 25 is not adequate for making it invertible yet.
//                26, 
//                27, 
//                28, 
//                30, 
                }; 
        for (int i : sides) {
            ParameterizedFields.printMatrices( new PolygonField(i) );
        }
        
//        ParameterizedFields.printMatrices( new PlasticNumberField() );
//        ParameterizedFields.printMatrices( new SnubCubeField() );
//        ParameterizedFields.printMatrices( new SuperGoldenField() );
//        ParameterizedFields.printMatrices( new EdPeggField() );
    }
    
    @Test 
    public void printGcdTable() {
        // this is an experiment to see if I can calculate the correct order for a polygonField with nSides
        // so that it is an AlgebraicField, not just an AlgebraicRing.
        // This may or may not indicate which diags to remove, but at least I hope it gets the order correct.
        StringBuilder all = new StringBuilder();
        String delim = "";
        for(long nSides = 2; nSides <= 36; nSides++) {
            System.out.print(nSides);
            int order = 0;
            for(long k = 1; k <= nSides/2; k++) {
                long gcd = BigRational.Gcd.gcd(nSides, k);
                BigRational br = new BigRational(nSides, gcd);
                String marker = " ";
                if (k >= nSides/gcd) {
                    marker = "*";
                } else {
                    order++;
                }
                System.out.print("\t" + marker + k + "/" + br);
            }
            all.append(delim);
            all.append(order);
            delim = ", ";
            if(order != nSides/2) {
                System.out.println("\t\t// order(" + nSides + ") = " + order);
            } else {
                System.out.println("\t\t// prime -----------------"); // prime
            }
        }
        System.out.println("\nOEIS? " + all);
    }

    @Test
    public void scalingBugTest() {
        System.out.println("scalingBugTest");
        AlgebraicField field = new PolygonField(8);

        AlgebraicNumber x = field.createAlgebraicNumber(new int[] { 0, 1, 0, 0} );
        Double dx = x.evaluate();

        AlgebraicNumber y = field.createAlgebraicNumber(new int[] { -1, 1, 1, -1} );
        Double dy = y.evaluate();

        assertEquals(x.times(y), y.times(x));

        // This is what we're really testing
        AlgebraicNumber z = y.times(x);
        Double dz = z.evaluate();

        System.out.print(dx);
        System.out.print("  *  ");
        System.out.print(dy);
        System.out.print("\t=  ");
        System.out.print(dx * dy);
        System.out.print("\t==  ");
        System.out.print(dz);
        System.out.println(" ???");

        int format = 1;
        System.out.print(x.toString(format));
        System.out.print("  *  ");
        System.out.print(y.toString(format));
        System.out.print("\t=  ");
        System.out.print(z.toString(format));
        System.out.println();

        assertEquals(dx * dy, dz, 0.00000000000001d);
    }

    @Test
    public void scalingBugTest2() {
        System.out.println("scalingBugTest2");
        AlgebraicField field = new PolygonField(8);
        AlgebraicNumber scalar = field .createPower( -1 );
        Double dScalar = scalar.evaluate();
        assertTrue ( dScalar < 1.0d );

//        AlgebraicNumber x = field.createAlgebraicNumber(new int[] { -17,   6,  15, -11} );
        AlgebraicNumber x = field.createAlgebraicNumber(new int[] { -1,   1,  1, -1} );
        AlgebraicNumber y = field.createAlgebraicNumber(new int[] { -10,   4,   9, - 7} );
        AlgebraicNumber z = field.createAlgebraicNumber(new int[] {  32, -11, -28,  21} );

        Double dx = x.evaluate();
        Double dy = y.evaluate();
        Double dz = z.evaluate();

        // This is what we're really testing
        AlgebraicNumber x2 = x.times(scalar);
        AlgebraicNumber y2 = y.times(scalar);
        AlgebraicNumber z2 = z.times(scalar);
        Double dx2 = x2.evaluate();
        Double dy2 = y2.evaluate();
        Double dz2 = z2.evaluate();

        System.out.print(dScalar);
        System.out.print("  *  ");
        System.out.print(dx);
        System.out.print("\t=  ");
        System.out.print(dx2);
        System.out.println();

        System.out.print(dScalar);
        System.out.print("  *  ");
        System.out.print(dy);
        System.out.print("\t=  ");
        System.out.print(dy2);
        System.out.println();

        System.out.print(dScalar);
        System.out.print("  *  ");
        System.out.print(dz);
        System.out.print("\t=  ");
        System.out.print(dz2);
        System.out.println();

        double delta = 0.00000000000001d;
        assertEquals(dx * dScalar, dx2, delta);
//        assertEquals(dy * dScalar, dy2, delta);
//        assertEquals(dz * dScalar, dz2, delta);

        AlgebraicVector longVector = new AlgebraicVector(x, y, z);

        RealVector longRealVector =  longVector .toRealVector();
        Double longLen =  longRealVector.length();
        System.out.print(longLen);
        System.out.print("\t");
//        assertTrue ( longLen > 2.0d);

        RealVector scaledReal = longRealVector.scale(dScalar);
        Double scaledRealLength = scaledReal.length();
        assertTrue ( scaledRealLength < longLen);
//        assertTrue ( scaledRealLength < 2.0d);

        AlgebraicVector shortVector = longVector .scale( scalar );

        RealVector shortRealVector =  shortVector.toRealVector();
        Double shortLen =  shortRealVector.length();
        System.out.println(shortLen);
        
        assertTrue ( shortLen < longLen );
    }

}
