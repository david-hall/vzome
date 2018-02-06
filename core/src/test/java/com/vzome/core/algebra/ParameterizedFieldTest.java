package com.vzome.core.algebra;

import static com.vzome.core.algebra.ParameterizedFields.printMatrices;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import org.junit.Test;

import com.vzome.core.math.RealVector;

/**
 * @author David Hall
 */
public class ParameterizedFieldTest {

    @Test
    public void printPolygonFieldMatrices() {
        for (int i = 4; i <= 16; i++) {
            printMatrices( new PolygonField(i) );
        }
    }

    @Test
    public void printSqrtFieldMatrices() {
        for(int i = 1; i <= 16; i++) {
            printMatrices( new SqrtField(i) );
        }
    }

    @Test
    public void scalingBugTest() {
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

    @Test
    public void testHighOrderPolygonFieldConstructor() {
        PolygonField field = new PolygonField(120);
        assertNotNull(field);
    }

    @Test
    public void testNormalizers() {
    	{
	    	SqrtField field = new SqrtField(9);
	        assertTrue(field.isPerfectSquare());
	        AlgebraicNumber n1 = field.createAlgebraicNumber(7,       4); 
	        AlgebraicNumber n2 = field.createAlgebraicNumber(7+(4*3), 0);
	        assertEquals(n1, n2);
	        assertEquals(n1.toString(), n2.toString());
	        assertEquals("19", n2.toString());
    	}
    	{
	    	PolygonField field = new PolygonField(6);
	        AlgebraicNumber n1 = field.createAlgebraicNumber(3,       5, 7);
	        AlgebraicNumber n2 = field.createAlgebraicNumber(3+(2*7), 5, 0);
	        assertEquals(n1, n2);
	        assertEquals(n1.toString(), n2.toString());
	        assertEquals("17 +5" + "\u221A" + "3", n2.toString());
    	}
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
        for (int i = 2; i <= 3; i++) {
            SqrtField field = new SqrtField(i * i);
            assertTrue(field.isPerfectSquare());
            AlgebraicNumber n = field.createPower(1); 
            Double result = n.evaluate();
            assertTrue(result.toString().endsWith(".0"));
            assertEquals(result.toString(), Integer.toString(i) + ".0");
            assertEquals(result.intValue(), i );
            
            // increment the radicand
            field = new SqrtField(field.radicand() + 1);
            assertFalse(field.isPerfectSquare());
            n = field.createPower(1); 
            result = n.evaluate();
            assertFalse(result.toString().endsWith(".0"));
        }
    }

    @Test
    public void testValidation() {
        // valid values don't throw exceptions
        new SqrtField(Short.MAX_VALUE);
        new SqrtField(1);
            	
    	String failMsg = "Exception should have been thrown";
        int tries = 0;
        int catches = 0;
        try {
        	tries++;
            new SqrtField(-1);
            fail(failMsg);
        } catch (IllegalArgumentException ex) {
            catches++;
        }
        try {
        	tries++;
            new SqrtField(0);
            fail(failMsg);
        } catch (IllegalArgumentException ex) {
            catches++;
        }
        try {
        	tries++;
            new SqrtField(Short.MAX_VALUE + 1);
            fail(failMsg);
        } catch (IllegalArgumentException ex) {
            catches++;
        }
        
        assertEquals(4, PolygonField.MIN_SIDES);
        new PolygonField(PolygonField.MIN_SIDES);
        new PolygonField(128); // giant, but valid
        try {
        	tries++;
            new PolygonField(PolygonField.MIN_SIDES - 1);
            fail(failMsg);
        } catch (IllegalArgumentException ex) {
            catches++;
        }
        
        assertEquals(4, tries);
        assertEquals(catches, tries);
    }

}
