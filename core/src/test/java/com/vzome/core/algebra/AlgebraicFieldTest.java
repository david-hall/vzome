package com.vzome.core.algebra;

import static com.vzome.core.generic.Utilities.getSourceCodeLine;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @author David Hall
 */
public class AlgebraicFieldTest {
    private final static Set<AlgebraicField> fields = new LinkedHashSet<>(); // LinkedHashSet preserves insertion order
    
    static {
        fields.add (new PentagonField());
        fields.add (new RootTwoField());
        fields.add (new RootThreeField());
        fields.add (new HeptagonField());
        fields.add (new SnubDodecField());
        fields.add (new SnubDodecahedronField());
        fields.add (new SnubCubeField());
        fields.add (new SqrtPhiField());
        fields.add (new PhiPlusSqrtField(2));
        fields.add (new PhiPlusSqrtField(3));
        fields.add (new PlasticNumberField());
    }
    
    @Test
    public void printParameterizedFieldMatrices() {
        for (AlgebraicField field : fields) {
            if(field instanceof ParameterizedField<?>) {
                ParameterizedFields.printMatrices( (ParameterizedField<?>) field );    
            }
        }
    }
    
    @Test
    public void testEquality() {
        AlgebraicField[] f = fields.toArray( new AlgebraicField[fields.size()] );
        for(int j = 0; j < f.length; j++) {
            for(int k = 0; k < f.length; k++) {
                // TODO: This approach won't work for parameterized fields
                boolean same = (j == k);
                assertEquals( same, f[j].equals(f[k]) );
                assertEquals( same, f[j].hashCode() == f[k].hashCode() );
            }
        }
        // Specifically test the equalities and inequalities described in AlgebraicField.equals()
        compareFields(true, new PolygonField(4), new SqrtField(2));
        compareFields(true, new PolygonField(4), new RootTwoField());
        compareFields(true, new PolygonField(5), new PentagonField());
        compareFields(false, new PolygonField(6), new SqrtField(3));
        compareFields(true, new SqrtField(3), new RootThreeField());
        compareFields(true, new PolygonField(7), new HeptagonField());
        compareFields(true, new SnubDodecField(), new SnubDodecahedronField());
    }
    
    private void compareFields(boolean same, AlgebraicField f1, AlgebraicField f2 ) {
        String msg = "Expected " + f1.toString() + (same ? " == " : " != ") + f2.toString() + " at " + getSourceCodeLine(2);
        if(same) {
            assertEquals( msg, f1.hashCode(), f2.hashCode() );
            assertEquals( msg, f1, f2 );
        } else {
            assertNotEquals( msg, f1.hashCode(), f2.hashCode() );
            assertNotEquals( msg, f1, f2 );
        }
    }
        
    @Test
    public void testOrder() {
        int pass = 0;
        for(AlgebraicField field : fields) {
            assertTrue(field.getOrder() >= 2);
            pass++;
        }
        assertEquals(fields.size(), pass);
    }    

    @Test
    public void testReciprocalOfZero()
    {
        for( AlgebraicField field : fields ) {
            try {
                field .zero() .reciprocal() .evaluate();
                fail( "Zero divide should throw an exception" );
            } catch ( RuntimeException re ) {
                assertEquals( "Denominator is zero", re .getMessage() );
            }
        }
    }
    
    @Test
    public void testReciprocalsOfHackedFields()
    {
        verifyReciprocals( new PolygonField(12) );
        verifyReciprocals( new PolygonField(10) );
        verifyReciprocals( new PolygonField(9) );
        return;
    }
    
    @Test
    public void testReciprocals()
    {
        for( AlgebraicField field : fields ) {
            verifyReciprocals( field );
        }
        for( int i = 1; i < 10; i++) {
            verifyReciprocals( new SqrtField(i) );
        }
        ArrayList<Integer> list = new ArrayList<>();
        for( Integer i = PolygonField.MIN_SIDES; i <= 64; i++) {
            if(verifyReciprocals( new PolygonField(i) )) {
                list.add(i);
                if(i != 9 && i != 10 && i != 12)
                assertFalse(PolygonField.mayBeNonInvertable(i));
            } else {
                assertTrue(PolygonField.mayBeNonInvertable(i));
            }
        }

        String delim = list.size() + " Successful N-gon Fields: ";
        for(int i : list) {
            System.out.print(delim + i);
            delim = ", ";
        }
        System.out.println();
        delim = "    Standard Polygon Sides: ";
        for(int i : PolygonField.getStandardPolygonSides()) {
            System.out.print(delim + i);
            delim = ", ";
        }
        System.out.println();
        System.out.println("They include all where N == 6, N is an even power of two (e.g. 4, 8, 16, 32, 64...), or N is prime.");
        System.out.println("See the series at https://oeis.org/A067133. Note that phi(n) mentioned there refers to Euler's totient function, not the golden ratio.");
        System.out.println("Primes: 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257...");
        
    }
    
    @Test
    public void testRequiresReciprocalValidation() {
        assertFalse(PolygonField.mayBeNonInvertable(6));
        int[] primes = { // 2, 3, // these are unused because they are less than PolygonField.MIN_SIDES    
                5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 
                67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 
                137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 
                199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 
                277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 
                359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 
                439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 
                521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 
                607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 
                683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 
                773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 
                863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 
                967, 971, 977, 983, 991, 997        
        };
        ArrayList<Integer> tested = new ArrayList<>(primes.length);
        for(Integer prime : primes) {
            assertTrue(prime.toString(), BigInteger.valueOf(prime).isProbablePrime(100));
            if(prime > PolygonField.MIN_SIDES) {
                assertFalse(PolygonField.mayBeNonInvertable(prime));
                tested.add(prime);
            }
        }
        for(long i=4; i<=Integer.MAX_VALUE; i*=2 ) {
            int powerOfTwo = (int) i;
            assertFalse(PolygonField.mayBeNonInvertable(powerOfTwo));
            tested.add(powerOfTwo);
        }
        final int six = 6;
        assertFalse(PolygonField.mayBeNonInvertable(six));
        tested.add(six);

        // all untested integer values less than the max prime listed above should return true
        for(Integer n=PolygonField.MIN_SIDES; n<=primes[primes.length-1]; n++ ) {
            if(! tested.contains(n)) {
                assertTrue(n.toString(), PolygonField.mayBeNonInvertable(n));
            }
        }
    }
    
    public boolean verifyReciprocals(AlgebraicField field)
    {
        boolean success = true;
        System.out.println(field.getName() + " (order " + field.getOrder() + "):");
        for(int i = 0; i < field.getOrder(); i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            for(int j = i; j < field.getOrder(); j++) {
                AlgebraicNumber n2 = field.getUnitTerm(j);
                AlgebraicNumber n = n1.times(n2);
                success &= verifyReciprocal( n );
                if(!success) {
//                    break;  // comment this line to see all non-invertable unit terms for the given field
                }
            }
            i=1000;
        }
        if(success) {
            verifyReciprocal( field.getUnitPolynomial());
        }
        System.out.println("====================================");
        return success;
    }

    private boolean verifyReciprocal(AlgebraicNumber n)
    {
        boolean success = false;
        try {
            AlgebraicNumber r = n.reciprocal();
            AlgebraicNumber product = n.times(r); 
            success = product.isOne();
            if(! success ) {
                System.out.println(n + " * " + r + " = " + product);
                System.out.println(n.evaluate() + " * " + r.evaluate() + " = " + product.evaluate());
            }
        }
        catch(IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
        return success;
    }
    
    @Test
    public void testCreatePower()
    {
        for(int parity = 0; parity <= 0; parity++) {
            System.out.println((parity == 0 ? "EVEN " : "ODD  ") + "...................");
            for(int i = 4+parity; i <=64; i+=2) {
                if(PolygonField.mayBeNonInvertable(i)) {
                    continue;
                }
                String s = "i = " + i;
                PolygonField field = new PolygonField(i);
                final int n = 1;
                AlgebraicNumber small = field.createPower(-n);
                AlgebraicNumber large = field.createPower(n);
                Double sm = small.evaluate();
                Double lg = large.evaluate();
                String ss = small.toString().startsWith("-") ? "" : " ";
                System.out.print(s + "\tcreatePower(1) = " + large + "\treciprocal = " + ss + small + "\t");
                System.out.print(s + "\tlg = " + lg + "\tsm = " + sm);
                if(sm >= 1.0d || sm <-0.5d) {
                    System.out.println(" <-------------------------------------------- Huh?");
                } else {
                    System.out.println();
                }
//                assertEquals(s, small, large.reciprocal() );
//                assertEquals(s, field.one, small.times(large) );
//                assertTrue(s, lg > 1.0d);
//                assertTrue(s, lg < 2.0d);
//                assertTrue(s, sm < 1.0d);
//                assertTrue(s, sm > 0.5d);
            }
        }
    }
    
    
}
