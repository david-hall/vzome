package com.vzome.core.algebra;

import static com.vzome.core.algebra.PentagonField.PHI_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.sin;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author David Hall
 */
public class PolygonField extends ParameterizedField<Integer> {
    
    /**
     * 
     * @param polygonSides
     * @return the coefficients of a PolygonField given the same parameter. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     */
    public static double[] getFieldCoefficients(int polygonSides) {
        if(polygonSides < polygonSides) {
            return new double[] {}; // empty array
        }
        int order = polygonSides/2;
        double[] coefficients = new double[order]; 
        double unitLength = sin(PI / polygonSides);

        // The units position should always be exactly 1.0d.
        // We avoid any trig or rounding errors by specifically assigning it that value.
        coefficients[0] = 1.0d;
        // now initialize the rest, starting from i = 1
        for (int i = 1; i < order; i++) {
            coefficients[i] = sin((i+1) * PI / polygonSides) / unitLength;
        }

        // I discovered that a few significant values don't appear to be calculated "correctly" at first glance.
        // I found a great explanation at https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
        switch(polygonSides) {
        case 6:
            // Since PI is irrational and cannot be exactly represented in a double,
            // the trig functions may not produce the exact result we expect.
            // Specifically, for a hexagon, the calculated value of coefficients[2] is 2.0000000000000004
            // I want to have the exact correct value, so I'm going to hard code it.
            // I'm pretty sure that Niven's theorem https://en.wikipedia.org/wiki/Niven%27s_theorem
            // implies that this will be the only case where we'll get a rational result,
            // although I have not thought through other cases where polygonSides() may be some multiple of 6
            // Emperically, I can see that it doesn't happen when polygonSides() == 12
            // If there is found to be some other case where we get a rational coefficient,
            // (notice that I say rational coefficient, not just integer coefficient)
            // then it should be checked here, and normalize() should reflect that case as well.
            coefficients[2] = 2.0d;
            // Similarly, the calculated value of coefficients[1] is 1.7320508075688774 and should exactly equal sqrt(3) which is 1.73205080756887729...
            coefficients[1] = Math.sqrt(3);
            break;

        case 5:
            // Similarly, for pentagons, the trig calculation for coefficients[1] differs from PHI_VALUE by 0.0000000000000002220446049250313
            // PHI_VALUE       = 1.618033988749895
            // coefficients[1] = 1.618033988749897
            // WolframAlpha says 1.618033988749894848204586834365...
            // I want to have the same value in either case, so I'm going to hard code it.
            coefficients[1] = PHI_VALUE;
            break;

//        case 4:
//            // No difference found between sqrt(2) and coefficients[1]
//            System.out.println("sqrt(2) = " + Math.sqrt(2.0d));
//              break;

//        case 7:
//            // For heptagons, the trig calculation for coefficients[1] and coefficients[2]
//            // had more significant digits than RHO_VALUE and SIGMA_VALUE,
//            // so I updated the constant definitions to use more digits calculated on WolframAlpha
//            // the values are now equal:
//            // coefficients[1]  = 1.80193773580483825d;
//            // RHO_VALUE        = 1.80193773580483825d;   // root of x^3 - x^2 -2x +1
//            // coefficients[2]  = 2.24697960371746706d;
//            // SIGMA_VALUE      = 2.24697960371746706d;   // root of x^3 -2x^2 - x +1
//            System.out.printf("  RHO_VALUE = %1.16f\n", HeptagonField.RHO_VALUE);
//            System.out.printf("SIGMA_VALUE = %1.16f\n", HeptagonField.SIGMA_VALUE);
//                      break;
        }
        return coefficients;
    }
    
    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients(polygonSides());
    }
    
    public static final String FIELD_PREFIX = "polygon";
    
    /**
     * 
     * @return a pre-sorted array of the standard number of sides to be used 
     * as parameters to the PolygonField c'tor. This is intended to be a 
     * convenient pre-calculated way for applications to know which values 
     * should commonly be used without having to check individual values.
     * This array should be sorted and should include all values up to an 
     * arbitrary limit for which mayBeNonInvertable() will return false.
     * Initially, we'll use all values up to and incuding 64.
     */
    public static int[] getStandardPolygonSides() {
        final int[] std = new int[] {4, 5, 6, 7, 8, 11, 13, 16, 17, 19, 23, 29, 31, 32, 37, 41, 43, 47, 53, 59, 61, 64};
        return Arrays.copyOf(std, std.length); // return a copy so the original array is immutable
    }

    private final boolean isEven;
    private final boolean mayBeNonInvertable;
    
    public PolygonField(int polygonSides) {
        this( FIELD_PREFIX + polygonSides, polygonSides);
    }

    // this protected c'tor is intended to allow PentagonField and HeptagonField classes to be refactored
    // so they are derived from PolygonField and still maintain their original legacy names
    protected PolygonField(String name, int polygonSides) {
        super( name, 
//                polygonSides == 6 ? 2 : // TODO: Make this work for hexagon field ... 
                polygonSides == 9 ? 3 : 
                polygonSides == 10 ? 4 : 
                polygonSides == 12 ? 4 : 
                polygonSides/2, polygonSides );
        isEven = operand % 2 == 0;
        mayBeNonInvertable = mayBeNonInvertable(polygonSides);
    }

    /**
     * 
     * @param nSides
     * @return true if the terms of an AlgebraicNumber in the PolygonField with 
     * specified nSides could possibly be non-invertable, hence no reciprocal.
     * Even in fields where the terms could possibly be non-invertable, 
     * it's possible that some specific AlgebraicNumbers could be invertable.
     * This property allows the field to still be used for those specific AlgebraicNumbers    
     * Reciprocals are always valid for polygon fields where nSides == 6, 
     * where nSides is prime, or where nSides is an exact power of 2. 
     * This method will return false for all of these values.
     * See the series at https://oeis.org/A067133. 
     * Note that phi(n) mentioned there refers to Euler's totient function, not the golden ratio.
     */
    public static boolean mayBeNonInvertable(int nSides) {
        if (nSides < MIN_SIDES) {
            String msg = "polygon sides = " + nSides + ". It must be at least " + MIN_SIDES + ".";
            throw new IllegalArgumentException(msg);
        }
        if (nSides == 6) {
            return false; // no validation required
        }
        // positive powers of two
        if ((nSides & (nSides - 1)) == 0) {
            return false; // no validation required
        }
        // prime
        final int certainty = 100; // TODO: Determine the min reliable certainty here for any valid Integer nSides 
        return ! BigInteger.valueOf(nSides).isProbablePrime(certainty);
    }
    
    public boolean mayBeNonInvertable() {
        return mayBeNonInvertable;
    }

    public final static int MIN_SIDES = 4;

    @Override
    protected void validate() {
        if (polygonSides() < MIN_SIDES) {
            String msg = "polygon sides = " + polygonSides() + ". It must be at least " + MIN_SIDES + ".";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected void initializeLabels() {
        // odd-gons are labeled with the same lower case Greek letters as
        // Peter Steinbach uses in "Sections Beyond Golden"
        // See http://archive.bridgesmathart.org/2000/bridges2000-35.pdf
        switch(polygonSides()) {
            case 4:
                irrationalLabels[1] = new String[]{ "\u221A" + "2", "sqrtTwo" };
                break;

            case 5:
                irrationalLabels[1] = new String[]{ "\u03C6", "phi" };
                break;

            case 6:
                irrationalLabels[1] = new String[]{ "\u221A" + "3", "sqrtThree" };
                if(getOrder() > 2)
                irrationalLabels[2] = new String[]{ "Two", "two" };
                break;

            case 7:
                irrationalLabels[1] = new String[]{ "\u03C1", "rho" };
                irrationalLabels[2] = new String[]{ "\u03C3", "sigma" };
                break;

            case 9:
                irrationalLabels[1] = new String[]{ "\u03B1", "alpha" };
                irrationalLabels[2] = new String[]{ "\u03B2", "beta" };
                if(getOrder() > 3)
                irrationalLabels[3] = new String[]{ "\u03B3", "gamma" };
                break;

            case 11:
                irrationalLabels[1] = new String[]{ "\u03B8", "theta"  };
                irrationalLabels[2] = new String[]{ "\u03BA", "kappa"  };
                irrationalLabels[3] = new String[]{ "\u03BB", "lambda" };
                irrationalLabels[4] = new String[]{ "\u03BC", "mu"     };
                break;

            default:
                final String alphabet = "abcdefghijklmnopqrstuvwxyz";
                int order = getOrder();
                if(order -1 <= alphabet.length()) {
                    for(int i = 1; i < order; i++) {
                        String name = alphabet.substring(i-1, i);
                        irrationalLabels[i] = new String[]{ name, name };
                    }
                }
                else {
                    // The article "Proof by Picture: Products and Reciprocals of Diagonal Length Ratios in the Regular Polygon"
                    // at http://forumgeom.fau.edu/FG2006volume6/FG200610.pdf uses one-based indexing for the diagonals,
                    // but I am going to use zero-based indexing so it corresponds to our coefficients and multiplierMatrix indices.
                    // irrationalLabels[0] should never be needed, so I'll leave it blank.
                    for(int i = 1; i < order; i++) {
                        irrationalLabels[i] = new String[]{ "d" + subscriptString(i), "d[" + i + "]" };
                    }
                }
                break;
        }
    }

    private static String subscriptString(int i) {
        // https://stackoverflow.com/questions/17908593/how-to-find-the-unicode-of-the-subscript-alphabet
        return Integer.toString(i)
                .replace("0", "\u2080")
                .replace("1", "\u2081")
                .replace("2", "\u2082")
                .replace("3", "\u2083")
                .replace("4", "\u2084")
                .replace("5", "\u2085")
                .replace("6", "\u2086")
                .replace("7", "\u2087")
                .replace("8", "\u2088")
                .replace("9", "\u2089")
                .replace("-", "\u208B")
                ;
    }

    @Override
    protected void initializeCoefficients() {
        double[] temp = getCoefficients();
        int i = 0;
        for(double coefficient : temp) {
            if(i < coefficients.length)
            coefficients[i++] = coefficient;
        }
    }

    @Override
    protected void initializeMultiplierMatrix() {
        // <editor-fold defaultstate="collapsed">
        // the editor-fold tag is used in NetBeans to collapse these comments within the editor by default 
/*
        multiplierMatrix( polygon4 ) =
        {
          {
            { 1, 0, },
            { 0, 2, },
          },
          {
            { 0, 1, },
            { 1, 0, },
          },
        }


        multiplierMatrix( polygon5 ) =
        {
          {
            { 1, 0, },
            { 0, 1, },
          },
          {
            { 0, 1, },
            { 1, 1, },
          },
        }


        // hexagon is a special case as described below
        multiplierMatrix( polygon6 ) =
        {
          {
            { 1, 0, 2, },
            { 0, 3, 0, },
            { 2, 0, 4, },
          },
          {
            { 0, 1, 0, },
            { 1, 0, 2, },
            { 0, 2, 0, },
          },
          {
            { 0, 0, 0, },
            { 0, 0, 0, },
            { 0, 0, 0, },
          },
        }


        multiplierMatrix( polygon7 ) =
        {
          {
            { 1, 0, 0, },
            { 0, 1, 0, },
            { 0, 0, 1, },
          },
          {
            { 0, 1, 0, },
            { 1, 0, 1, },
            { 0, 1, 1, },
          },
          {
            { 0, 0, 1, },
            { 0, 1, 1, },
            { 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon8 ) =
        {
          {
            { 1, 0, 0, 0, },
            { 0, 1, 0, 0, },
            { 0, 0, 1, 0, },
            { 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, },
            { 1, 0, 1, 0, },
            { 0, 1, 0, 2, },
            { 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, },
            { 0, 1, 0, 2, },
            { 1, 0, 2, 0, },
            { 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, },
            { 0, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 1, 0, 1, 0, },
          },
        }


        multiplierMatrix( polygon9 ) =
        {
          {
            { 1, 0, 0, 0, },
            { 0, 1, 0, 0, },
            { 0, 0, 1, 0, },
            { 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, },
            { 1, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 1, 0, 1, 1, },
            { 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, },
            { 0, 0, 1, 1, },
            { 0, 1, 1, 1, },
            { 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon10 ) =
        {
          {
            { 1, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 2, },
            { 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 2, },
            { 0, 1, 0, 2, 0, },
            { 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 2, },
            { 0, 1, 0, 2, 0, },
            { 1, 0, 2, 0, 2, },
            { 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, },
          },
        }


        multiplierMatrix( polygon11 ) =
        {
          {
            { 1, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 1, },
            { 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 1, },
            { 1, 0, 1, 1, 1, },
            { 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, },
            { 0, 0, 0, 1, 1, },
            { 0, 0, 1, 1, 1, },
            { 0, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon12 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 1, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 2, },
            { 0, 0, 1, 0, 2, 0, },
            { 0, 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 2, },
            { 1, 0, 1, 0, 2, 0, },
            { 0, 1, 0, 2, 0, 2, },
            { 0, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 2, },
            { 0, 0, 1, 0, 2, 0, },
            { 0, 1, 0, 2, 0, 2, },
            { 1, 0, 2, 0, 2, 0, },
            { 0, 2, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 1, 0, 1, 0, 1, 0, },
          },
        }


        multiplierMatrix( polygon13 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 1, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 1, },
            { 0, 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 1, 0, 1, 0, 1, 1, },
            { 0, 1, 0, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 1, },
            { 0, 1, 0, 1, 1, 1, },
            { 1, 0, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 1, 1, },
            { 0, 0, 0, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon14 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 2, },
            { 0, 0, 0, 1, 0, 2, 0, },
            { 0, 0, 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 2, },
            { 0, 1, 0, 1, 0, 2, 0, },
            { 0, 0, 1, 0, 2, 0, 2, },
            { 0, 0, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 2, },
            { 0, 1, 0, 1, 0, 2, 0, },
            { 1, 0, 1, 0, 2, 0, 2, },
            { 0, 1, 0, 2, 0, 2, 0, },
            { 0, 0, 2, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 1, 0, 2, 0, },
            { 0, 0, 1, 0, 2, 0, 2, },
            { 0, 1, 0, 2, 0, 2, 0, },
            { 1, 0, 2, 0, 2, 0, 2, },
            { 0, 2, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 1, },
          },
        }


        multiplierMatrix( polygon15 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 1, },
            { 0, 0, 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 1, },
            { 0, 0, 1, 0, 1, 1, 1, },
            { 0, 0, 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 1, },
            { 1, 0, 1, 0, 1, 1, 1, },
            { 0, 1, 0, 1, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 1, },
            { 0, 0, 1, 0, 1, 1, 1, },
            { 0, 1, 0, 1, 1, 1, 1, },
            { 1, 0, 1, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 1, },
            { 0, 0, 0, 0, 1, 1, 1, },
            { 0, 0, 0, 1, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, 1, 1, },
          },
        }
*/
        // </editor-fold>
        if(polygonSides() == 9 && getOrder() == 3) {
            multiplierMatrix = new short[][][] 
                {
                  {
                    { 1, 0, 0, },
                    { 0, 1, 1, },
                    { 0, 1, 2, },
                  },
                  {
                    { 0, 1, 0, },
                    { 1, 0, 2, },
                    { 0, 2, 1, },
                  },
                  {
                    { 0, 0, 1, },
                    { 0, 1, 0, },
                    { 1, 0, 1, },
                  },
                };
            return;
        }
        
        if(polygonSides() == 10 && getOrder() == 4) {
            multiplierMatrix = new short[][][] // D = -2 + 2B
                    {                       // { // D is truncated ... $ = to be modified
                      { // units            //   {  // units
                        { 1, 0, 0, 0, },    //     { 1, 0, 0, 0, },
                        { 0, 1, 0,-2, },    //     { 0, 1, 0,$0, },
                        { 0, 0,-1, 0, },    //     { 0, 0,$1, 0, },
                        { 0,-2, 0,-1, },    //     { 0,$0, 0,$1, },
                      },                    //   },
                      { // A                //   {  // A = no change
                        { 0, 1, 0, 0, },    //     { 0, 1, 0, 0, },
                        { 1, 0, 1, 0, },    //     { 1, 0, 1, 0, },
                        { 0, 1, 0, 1, },    //     { 0, 1, 0, 1, },
                        { 0, 0, 1, 0, },    //     { 0, 0, 1, 0, },
                      },                    //   },
                      { // B                //   {  // B
                        { 0, 0, 1, 0, },    //     { 0, 0, 1, 0, },
                        { 0, 1, 0, 3, },    //     { 0, 1, 0,$1, },
                        { 1, 0, 3, 0, },    //     { 1, 0,$1, 0, },
                        { 0, 3, 0, 4, },    //     { 0,$1, 0,$2, },
                      },                    //   },
                      { // C                //   {  // C = no change
                        { 0, 0, 0, 1, },    //     { 0, 0, 0, 1, },
                        { 0, 0, 1, 0, },    //     { 0, 0, 1, 0, },
                        { 0, 1, 0, 2, },    //     { 0, 1, 0, 2, },
                        { 1, 0, 2, 0, },    //     { 1, 0, 2, 0, },
                      },                    //   },
                    };                      // };
            return;
        }

        if(polygonSides() == 12 && getOrder() == 4) {
            multiplierMatrix = new short[][][] // D = 1 + B; E = 2A
              {                       // { // D & E  are truncated ... $ = to be modified
                { // units
                    { 1, 0, 0, 0, },
                    { 0, 1, 0, 1, },
                    { 0, 0, 2, 0, },
                    { 0, 1, 0, 3, },
                  },
                  {
                    { 0, 1, 0, 0, },
                    { 1, 0, 1, 0, },
                    { 0, 1, 0, 3, },
                    { 0, 0, 3, 0, },
                  },
                  {
                    { 0, 0, 1, 0, },
                    { 0, 1, 0, 2, },
                    { 1, 0, 2, 0, },
                    { 0, 2, 0, 3, },
                  },
                  {
                    { 0, 0, 0, 1, },
                    { 0, 0, 1, 0, },
                    { 0, 1, 0, 3, },
                    { 1, 0, 3, 0, },
                  },
                };
            return;
        }

        int order = getOrder();

        // initialize everything to 0
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                for (int k = 0; k < order; k++) {
                    multiplierMatrix[i][j][k] = 0;
                }
            }
        }
            
        // initialize all of the \<->\ SouthEasterly diagonal paths
        for (int layer = 0; layer < order; layer++) {
            int midWay = layer/2;
            for (int bx = layer, by = 0; bx > midWay || bx == by; bx--, by++) {
                for (int x = bx, y = by; x < order && y < order; x++, y++) {
                    // Simple assignment would work here 
                    // but incrementing the value identifies unwanted duplicates. Ditto for the mirror.
                    multiplierMatrix[layer][y][x] += 1;
                    if(x != y) {
                        multiplierMatrix[layer][x][y] += 1; // mirror around x == y
                    }
                }
            }
        }

        // initialize the remaining /<->/ SouthWesterly diagonal paths
        int box = polygonSides() - 2;
        int parity = (polygonSides()+1) % 2;
        for (int layer = 0; layer < order-parity; layer++) {
            int base = box - layer;
            for (int xb = base, yb = 0; xb >= 0; xb--, yb++) {
                int x=xb;
                int y=yb;
                while(x<order && y<order) {
                    multiplierMatrix[layer][y][x] += 1;
                    x++;
                    y++;
                }
            }
        }

        // Hexagons are a special case because the length of the 2nd diagonal 
        // is an integer multiple of the unit edge,
        // so that "carry" must be transferred to the units position
        // much like the situation for perfect squares in SqrtField.
        // We could hard code the values, but the code below makes the reasoning a little clearer.
        if(polygonSides() == 6) {
            for(int x=0; x<order; x++) {
                for(int y=0; y<order; y++) {
                    int xfer = 2 * multiplierMatrix[2][x][y];
                    multiplierMatrix[0][x][y] += xfer;
                    multiplierMatrix[2][x][y] = 0;
                }
            }

//            multiplierMatrix = new short[][][] {
//                {
//                  { 1, 0, 2, },
//                  { 0, 3, 0, },
//                  { 2, 0, 4, },
//                },
//                {
//                  { 0, 1, 0, },
//                  { 1, 0, 2, },
//                  { 0, 2, 0, },
//                },
//                {
//                  { 0, 0, 0, },
//                  { 0, 0, 0, },
//                  { 0, 0, 0, },
//                },
//              };
        }
    }

    @Override
        protected void initializeNormalizer() {
        if(polygonSides() == 6 && getOrder() > 2) {
                normalizer = PolygonField::normalizeHexagon; 
        }
    }
    
    private static void normalizeHexagon(AlgebraicField field, BigRational[] factors) {
        if(!factors[2].isZero()) {
            factors[0] = factors[0].plus(factors[2].times(2));
            factors[2] = BigRational.ZERO;
        }
    }

    public Integer polygonSides() {
        return operand;
    }

    public final boolean isEven() {
        return isEven;
    }

    public final boolean isOdd() {
        return !isEven;
    }
    
    @Override
    protected BigRational[] reciprocal( BigRational[] terms )
    {
        BigRational[] reciprocalTerms = super.reciprocal( terms );
        if(mayBeNonInvertable) {
            AlgebraicNumber num = createAlgebraicNumber( terms ); 
            AlgebraicNumber recip = createAlgebraicNumber( reciprocalTerms ); 
            if(! num.times(recip) .isOne()) {
                String msg = "The AlgebraicNumber '" + num.toString() + "' is non-invertable in the " + getName() + " field.";
                throw new IllegalArgumentException(msg);
            }
        }
        return reciprocalTerms;
    }

    @Override
    public void defineMultiplier(StringBuffer buf, int i) {
        if (i >= 2) {
            buf.append( getIrrational(i, EXPRESSION_FORMAT) )
                    .append(" = ")
                    .append(coefficients[i - 1]);
        } else {
            super.defineMultiplier(buf, i);
        }
    }

    private AlgebraicNumber defaultStrutScaling;
    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        if(defaultStrutScaling == null) {
            switch(polygonSides()) {
            case 5: // legacy PentagonField
                defaultStrutScaling = createAlgebraicNumber(-1, 1, 2, 0);
                break;

            case 4:
            case 6:
                // we start with this value just because we did in RootTwoField and RootThreeField
                defaultStrutScaling = createAlgebraicNumber(1, 0, 2, -3);
                break;
                
            default:
                defaultStrutScaling = super.getDefaultStrutScaling();
                break;
            }
        }
        return defaultStrutScaling;
    }

//  protected static final String[] GREEK_ALPHABET = {
//  "\u03B1", // alpha
//  "\u03B2", // beta
//  "\u03B3", // gamma
//  "\u03B4", // delta
//  "\u03B5", // epsilon
//  "\u03B6", // zeta
//  "\u03B7", // eta
//  "\u03B8", // theta
//  "\u03B9", // iota
//  "\u03BA", // kappa
//  "\u03BB", // lambda
//  "\u03BC", // mu
//  "\u03BD", // nu
//  "\u03BE", // xi
//  "\u03BF", // omicron
////  "\u03C0", // pi            // To avoid confusion, don't use pi (3.1415...) as the name of an irrational factor
//  "\u03C1", // rho
////  "\u03C2", // 'final_sigma' // Not to be confused with the actual lower case letter 'stigma' (with a 't' in it) @ "\u03DB".
//  "\u03C3", // sigma
//  "\u03C4", // tau
//  "\u03C5", // upsilon
//  "\u03C6", // phi
//  "\u03C7", // chi
//  "\u03C8", // psi
//  "\u03C9", // omega
//};

}
