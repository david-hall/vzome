package com.vzome.core.algebra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vzome.core.generic.Utilities;
import com.vzome.fields.sqrtphi.SqrtPhiField;

/**
 * @author David Hall
 */
public class ParameterizedFieldTest {
    
    private static final List< AlgebraicField > TEST_FIELDS = new ArrayList<>();
    
    static {
        TEST_FIELDS.add( new PentagonField() );
        TEST_FIELDS.add( new RootTwoField() );
        TEST_FIELDS.add( new RootThreeField() );
        TEST_FIELDS.add( new HeptagonField() );
        TEST_FIELDS.add( new SnubDodecField( AlgebraicNumberImpl.FACTORY ) );
        TEST_FIELDS.add( new SqrtPhiField( AlgebraicNumberImpl.FACTORY ) );
//        TEST_FIELDS.add( new SnubDodecahedronField() );
//        TEST_FIELDS.add( new SqrtField(2) );
//        TEST_FIELDS.add( new SqrtField(3) );
//        TEST_FIELDS.add( new SqrtField(6) );
        TEST_FIELDS.add( new SnubCubeField( AlgebraicNumberImpl.FACTORY ) );
        TEST_FIELDS.add( new PlasticNumberField( AlgebraicNumberImpl.FACTORY ) );
        TEST_FIELDS.add( new PlasticPhiField( AlgebraicNumberImpl.FACTORY ) );
        TEST_FIELDS.add( new SuperGoldenField( AlgebraicNumberImpl.FACTORY ) );
        TEST_FIELDS.add( new EdPeggField( AlgebraicNumberImpl.FACTORY ) );
        for(int nSides = PolygonField.MIN_SIDES; nSides <= PolygonFieldTest.MAX_SIDES; nSides++) {
            TEST_FIELDS.add( new PolygonField(nSides, AlgebraicNumberImpl.FACTORY ) );
        }
    }
    
    @Test
    public void testHaveSameInitialCoefficients() {
        PolygonField polyField = new PolygonField(5, AlgebraicNumberImpl.FACTORY ); 
        PentagonField pentField = new PentagonField(); 
        assertTrue(AlgebraicFields.haveSameInitialCoefficients(polyField, PentagonField.FIELD_NAME));
        assertTrue(AlgebraicFields.haveSameInitialCoefficients(pentField, PolygonField.FIELD_PREFIX + "5"));

        polyField = new PolygonField(7, AlgebraicNumberImpl.FACTORY ); 
        HeptagonField heptField = new HeptagonField(); 
        assertTrue(AlgebraicFields.haveSameInitialCoefficients(polyField, HeptagonField.FIELD_NAME));
        assertTrue(AlgebraicFields.haveSameInitialCoefficients(heptField, PolygonField.FIELD_PREFIX + "7"));
    }


    @Test
    public void testMulDivEvaluate() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            testMulDivEvaluate( field );    
        }
    }
    
    public static void testMulDivEvaluate(AlgebraicField field) {
        // Empirically found the minimum delta that works...
        final double delta = 0.000000000000171d; // twelve 0's between the decimal point and the first non-zero digit 
        int n = field.getOrder();
        for (int i = 0; i < n; i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            double d1 = n1.evaluate();
            for (int j = 0; j < n; j++) {
                AlgebraicNumber n2 = field.getUnitTerm(j);
                double d2 = n2.evaluate();
                AlgebraicNumber product = n1.times(n2);
                AlgebraicNumber quotient = n1.dividedBy(n2);
                double prod = product.evaluate();
                double quot = quotient.evaluate();
//                System.out.println(field.getName() + ": " + n1.toString() + " * " + n2.toString() + " = " + product.toString() + "\t: " + d1 + " * " + d2 + " = " + prod );
//                System.out.println(field.getName() + ": " + n1.toString() + " / " + n2.toString() + " = " + quotient.toString() + "\t: " + d1 + " / " + d2 + " = "+ quot );
                assertEquals(d1 * d2, prod, delta);
                assertEquals(d1 / d2, quot, delta);
            }
        }
    }

    @Test
    public void testSqrtSubFields() {
//        AlgebraicField field = new PolygonField(30, AlgebraicNumberImpl.FACTORY);
        for(AlgebraicField field : TEST_FIELDS) {
            System.out.println(field.getName() + " is of order " + field.getOrder());
            if(field.getOrder() <= 18) { // p60 is 16
                testSqrtSubField(field);
                System.out.println();
            }
        }
    }

    public void testSqrtSubField(AlgebraicField field) {
        List<Integer> termList = new ArrayList<>(field.getOrder());
        qty = 0;
        testSqrtTerms(field, termList);
    }

    public void testSqrtTerms(AlgebraicField field, List<Integer> termList) {
        int nTerms = termList.size(); 
        if(nTerms < field.getOrder()) {
            termList.add(0);
            testSqrtTerms(field, termList);
            termList.set(nTerms, 1);
            testSqrtTerms(field, termList);
            termList.remove(nTerms);
        } else {
            int[] terms = new int[nTerms];
            for(int i = 0; i < nTerms; i++) {
                terms[i] = termList.get(i);
            }
            AlgebraicNumberImpl n1 = (AlgebraicNumberImpl) field.createAlgebraicNumber(terms);
            testSqrtTerms(n1);
        }
    }

    static int qty = 0;
    public void testSqrtTerms(AlgebraicNumberImpl n1) {
        qty++;
        if(!n1.isRational()) {
            AlgebraicNumberImpl n2 = (AlgebraicNumberImpl) n1.times(n1);
            BigRational[] terms1 = n1.getFactors();
            BigRational[] terms2 = n2.getFactors();
            boolean match = true;
            boolean last = true;
            for(int i = 0; i < terms1.length; i++) {
                if(terms1[i].isZero()) {
                    last = false;
                    if(!terms2[i].isZero()) {
                        match = false;
                        break;
                    }
                }
            }
            if(match && !last) {
                System.out.println((last ? " *" : "  ") + qty + "\t" + n1 + "\t===>\t" + n2);
//                System.out.println(qty + "\t" + n1.evaluate() + "\t===>\t" + n2.evaluate());
            }
        }
    }

    @Test
    public void testSqrtGoldenNumber() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            final AlgebraicNumber zero = field.zero();
            final AlgebraicNumber phi = field.getGoldenRatio();
            if(phi == null) {
                boolean thrown = false;
                try {
                    SqrtGoldenNumber.solve(field.createRational(42));
                    fail( field.getName() + " expected an Exception");
                }
                catch(IllegalArgumentException ex) {
                    // success
                    thrown = true;
                }
                assertTrue("thrown ", thrown);
                continue;
            }
            System.out.println();
            System.out.println(field.getName() + ": phi = " + phi);
            // be sure to test some denominators that are multiples of 5 since phi involves sqrt(5)
            for(int i = 0; i < 25; i++) { 
                int range = 100;
                int a = Double.valueOf((0.5d - Math.random()) * range).intValue();
                int b = Double.valueOf((0.5d - Math.random()) * range).intValue();
                int den = i+1;
                String alias = "(" + a + (b < 0 ? " " : " +") + b + "φ)" + "/" + den;
                AlgebraicNumber units = field.createRational(a);
                AlgebraicNumber phis = field.createRational(b).times(phi);
                AlgebraicNumber denom = field.createRational(den);
                AlgebraicNumber number = units.plus(phis).dividedBy(denom);
                if(number.signum() < 0) {
                    number = number.negate();
                }

                AlgebraicNumber numSquared = number.times(number); // numSquared will always be positive or 0
                assertTrue("numSquared is not negative", ((AlgebraicNumberImpl)numSquared).greaterThanOrEqualTo(zero));
                System.out.print("( " + alias + " )  =\t" + number + "\t\t");
                System.out.print("( " + number + " )² =\t" + numSquared + "\t\t√( " + numSquared + " ) =\t");
                AlgebraicNumber sqrt = SqrtGoldenNumber.solve(numSquared);
                System.out.println(sqrt);
                assertTrue("sqrt(numSquared) is not negative", ((AlgebraicNumberImpl)sqrt).greaterThanOrEqualTo(zero));
                assertEquals("sqrt(numSquared)", number, sqrt);
                testNonPerfectSquareRoot(numSquared);
                testNegativeSquareRoot(numSquared);
            }
        }
    }
    
    private static void testNonPerfectSquareRoot(AlgebraicNumber numSquared) {
        AlgebraicField field = numSquared.getField();
        AlgebraicNumber test = numSquared.plus(field.createRational(1,2)); 
        AlgebraicNumber sqrt = SqrtGoldenNumber.solve(test);
        String msg = "sqrt( " + test + " )=" + sqrt + ": Expected null.";
        // normally null but a few perfect squares may get to this point
        if(sqrt != null && !sqrt.times(sqrt).equals(test)) {
            System.out.println(msg);
            fail(msg);
        }
    }
    
    private static void testNegativeSquareRoot(AlgebraicNumber numSquared) {
        if(numSquared.isZero()) 
            return;
        AlgebraicNumber sqrt = SqrtGoldenNumber.solve(numSquared.negate());
        String msg = "sqrt(" + numSquared + ")=" + sqrt + ": sqrt of any negative number should return null.";
        if(sqrt != null) {
            System.out.println(msg);
            fail(msg);
        }
    }
    
    @Test
    public void printMathTables() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            ParameterizedFields.printMathTables( field );
        }
    }

    @Test
    public void printNumberByName() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        String[] names = {
                "sqrt2","sqrt3","sqrt5","sqrt6","sqrt7","sqrt8","sqrt10",
                "phi","xi","rho","sigma",
                "alpha","beta","gamma","delta","epsilon",
                "theta","kappa","lambda","mu","psi"
            };
        for(String name : names) {
            System.out.println(name);
            boolean found = false;
            for(AlgebraicField field : TEST_FIELDS) {
                AlgebraicNumber n = field.getNumberByName(name);
                if(n != null) {
                    found = true;
                    System.out.print("  " + field.getName() + "\t" + n + "\t" + n.evaluate());
                    if(name.startsWith("sqrt")) {
                        String radicand = name.substring(4);
                        // verify the aliases
                        assertEquals(name, n, field.getNumberByName("root" + radicand));
                        assertEquals(name, n, field.getNumberByName("\u221A" + radicand));
                        // now square it
                        AlgebraicNumber sq = n.times(n);
                        System.out.print("^2 = \t" + sq.evaluate());
                        double expected = Double.valueOf(radicand);
                        assertEquals(name, expected, sq.evaluate(), 0.0d);
                    }
                    System.out.println();
                }
            }
            assertTrue("found a match for " + name, found);
        }
    }

    @Test
    public void testGetIrrationalName() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            for(int format = AlgebraicField.DEFAULT_FORMAT; format <= AlgebraicField.EXPRESSION_FORMAT; format++) {
                assertEquals(field.getName() + " 0th irrational name should be a single space", " ", field.getIrrational(0, format));
                for(int i=1; i < field.getOrder(); i++) {
                    String name = field.getIrrational(i, format);
                    AlgebraicNumber n = field.getUnitTerm(i);    
                    AlgebraicNumber q = field.getNumberByName(name);
                    assertEquals(n, q);
                }
                try {
                    int limit = field.getOrder();
                    if(field instanceof PolygonField) {
                        limit = ((PolygonField)field).polygonSides();
                    }
                    field.getIrrational(limit, format);
                    fail(field.getName() + ".getIrrational: expected an ArrayIndexOutOfBoundsException for the INDEX parameter");
                }
                catch(ArrayIndexOutOfBoundsException ex) {
                    // success
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                    fail(field.getName() + ": inconsistent exception class: " + ex.getClass().getSimpleName());
                }
            }
            try {
                field.getIrrational(0, 2);
                fail(field.getName() + ".getIrrational: expected an ArrayIndexOutOfBoundsException for the FORMAT parameter");
            }
            catch(ArrayIndexOutOfBoundsException ex) {
                // success
            }
            catch(Exception ex) {
                ex.printStackTrace();
                fail(field.getName() + ": inconsistent exception class: " + ex.getClass().getSimpleName());
            }

        }
    }

    @Test
    public void printExponentTables() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            ParameterizedFields.printExponentTable( field, 6 );
        }
    }

    @Test
    public void printMultiplicationTensors() {
        System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " " + Utilities.thisSourceCodeLine());
        for(AlgebraicField field : TEST_FIELDS) {
            if(field instanceof ParameterizedField) {
                ParameterizedFields.printMultiplicationTensor( (ParameterizedField)field );    
            }
        }
    }

}
