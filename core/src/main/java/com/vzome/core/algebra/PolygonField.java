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
            // Empirically, I can see that it doesn't happen when polygonSides() == 12
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
//                polygonSides == 9 ? 3 : 
//                polygonSides == 10 ? 4 : 
//                polygonSides == 12 ? 4 : 
//                polygonSides == 15 ? 4 : 
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
//        if (nSides == 6) {
//            return false; // no validation required
//        }
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
                // TODO: Move this default behavior into the base class, possibly with an option for the subscripted variable name
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
        if(polygonSides() == 6 && getOrder() == 2) {
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
        if(polygonSides() / 2 == getOrder()) {
            switch(polygonSides()) {
            case 6: 
                normalizer = PolygonField::normalize6;
                break;
            case 9:
                normalizer = PolygonField::normalize9;
                break;
            case 10:
                normalizer = PolygonField::normalize10;
                break;
            case 12:
                normalizer = PolygonField::normalize12;
                break;
            case 14:
            	normalizer = PolygonField::normalize14;
            	break;
            case 15:
            	normalizer = PolygonField::normalize15;
            	break;
            case 18:
            	normalizer = PolygonField::normalize18;
            	break;
            case 20:
          	  	normalizer = PolygonField::normalize20;
            	break;
            case 21:
	            normalizer = PolygonField::normalize21;
	            break;
            case 24:
            	normalizer = PolygonField::normalize24;
            	break;
            case 22:
            	normalizer = PolygonField::normalize22;
            	break;
            case 25:
            	normalizer = PolygonField::normalize25;
            	break;
            case 26:
            	normalizer = PolygonField::normalize26;
            	break;
            case 27:
	            normalizer = PolygonField::normalize27;
	            break;
            case 28:
	            normalizer = PolygonField::normalize28;
	            break;
            case 30:
                normalizer = PolygonField::normalize30;
                break;
            case 33:
                normalizer = PolygonField::normalize33;
                break;
            case 34:
                normalizer = PolygonField::normalize34;
                break;
            case 35:
                normalizer = PolygonField::normalize35;
                break;
            case 36:
                normalizer = PolygonField::normalize36;
                break;
            case 38:
            	normalizer = PolygonField::normalize38;
            	break;
            case 46:
                normalizer = PolygonField::normalize46;
                break;
            case 48:
            	normalizer = PolygonField::normalize48;
            	break;
            case 58:
                normalizer = PolygonField::normalize58;
                break;
            default:
                normalizer = ParameterizedField::doNothing; // unchanged
                break;
            }
        }
    }
    
    // this pattern seems to work for 3 times any even power of 2 (e.g. 3 * 2 = 6)
    private static void normalize6(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 2, 0, 0); // B = 1 + 1
    }

    private static void normalize9(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 3, 0, 1); // C = 1 + A
    }

    private static void normalize10(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 4, -2L, 0, 2L, 2); // D = -2(1) + 2B
    }

    // this pattern seems to work for 3 times any even power of 2 (e.g. 3 * 4 = 12)
    private static void normalize12(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 4, 0, 2); // D = 1 + B
        normalizeFactor(factors, 5, 1, 1); // E = A + A
    }

    private static void normalize14(AlgebraicField field, BigRational[] factors) {
    	int F  = 6;
    	BigRational factor = factors[F]; // F = 2 - 2B + 2D
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// units
            factors[0] = factors[0].plus(factor); 	// units again = 2
            factors[2] = factors[2].minus(factor); 	// -B
            factors[2] = factors[2].minus(factor); 	// -B again = -2B
            factors[4] = factors[4].plus(factor); 	// D
            factors[4] = factors[4].plus(factor); 	// D again = 2D
            // zero
            factors[F] = BigRational.ZERO;
        }
    }

    private static void normalize15(AlgebraicField field, BigRational[] factors) {
    	int D = 4;
    	BigRational factor = factors[D]; // D = 1 + 2A + B - C 
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// units
            factors[1] = factors[1].plus(factor); 	// A
            factors[1] = factors[1].plus(factor); 	// A again = 2A
            factors[2] = factors[2].plus(factor); 	// B
            factors[3] = factors[3].minus(factor);	// C
            // zero
            factors[D] = BigRational.ZERO;
        }
        normalizeFactor(factors, 5, 0, 3); // E = 1 + C
        normalizeFactor(factors, 6, 1, 2); // F = A + B
    }

    private static void normalize18(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 6, 0, 4); // F = 1 + D
        normalizeFactor(factors, 7, 1, 3); // G = A + C
        normalizeFactor(factors, 8, 2, 2); // H = B + B = 2B
    }
    
    private static void normalize20(AlgebraicField field, BigRational[] factors) {
    	int I = 9;
    	BigRational factor = factors[I]; // I = 2E - 2A
        if(!factor.isZero()) {
            factors[1] = factors[1].minus(factor); 	// A
            factors[1] = factors[1].minus(factor); 	// A again = -2A
            factors[5] = factors[5].plus(factor); 	// E
            factors[5] = factors[5].plus(factor);	// again = 2E
            // zero
            factors[I] = BigRational.ZERO;
        }
    	int H = 8; 
    	factor = factors[H]; // H = F + D -B -1
        if(!factor.isZero()) {
            factors[0] = factors[0].minus(factor); 	// -units
            factors[2] = factors[2].minus(factor); 	// -B
            factors[4] = factors[4].plus(factor); 	// D
            factors[6] = factors[6].minus(factor);	// F
            // zero
            factors[H] = BigRational.ZERO;
        }
    }

    private static void normalize21(AlgebraicField field, BigRational[] factors) {
    	int F = 6; 
    	BigRational factor = factors[F]; // F = -E +D +2C +B -A -2
        if(!factor.isZero()) {
            factors[0] = factors[0].minus(factor); 	// -units
            factors[0] = factors[0].minus(factor); 	// -units again = 2
            factors[1] = factors[1].minus(factor); 	// -A
            factors[2] = factors[2].plus(factor); 	// B
            factors[3] = factors[3].plus(factor); 	// C
            factors[3] = factors[3].plus(factor); 	// C again = 2C
            factors[4] = factors[4].plus(factor); 	// D
            factors[5] = factors[5].minus(factor);	// -E
            // zero
            factors[F] = BigRational.ZERO;
        }	 
    	normalizeFactor(factors, 7, 0, 5); // G = 1 + E
    	normalizeFactor(factors, 8, 1, 4); // H = A + D
    	normalizeFactor(factors, 9, 2, 3); // I = B + C
    }

    private static void normalize22(AlgebraicField field, BigRational[] factors) {
    	int J = 10; 
    	BigRational factor = factors[J]; // J = 2H -2F +2D -2B +2
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// -units
            factors[0] = factors[0].plus(factor); 	// -units again = 2
            factors[2] = factors[2].minus(factor); 	// -B
            factors[2] = factors[2].minus(factor); 	// -B again - 2B
            factors[4] = factors[4].plus(factor); 	// D
            factors[4] = factors[4].plus(factor); 	// D again = 2D
            factors[6] = factors[6].minus(factor); 	// -F
            factors[6] = factors[6].minus(factor);	// -F again = -2F
            factors[8] = factors[8].plus(factor); 	// H
            factors[8] = factors[8].plus(factor); 	// H again = 2H
            // zero
            factors[J] = BigRational.ZERO;
        }	 
    }

    // this pattern seems to work for 3 times any even power of 2 (e.g. 3 * 8 = 24)
    private static void normalize24(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors,  8, 0, 6); // H = 1 + F
        normalizeFactor(factors,  9, 1, 5); // I = A + E
        normalizeFactor(factors, 10, 2, 4); // J = B + D
        normalizeFactor(factors, 11, 3, 3); // K = C + C
    }

    private static void normalize25(AlgebraicField field, BigRational[] factors) {
    	int K = 11; 
    	BigRational factor = factors[K]; // K = G + F - B - A
        if(!factor.isZero()) {
            factors[1] = factors[1].minus(factor); 	// -A
            factors[2] = factors[2].minus(factor); 	// -B
            factors[6] = factors[6].plus(factor); 	// F
            factors[7] = factors[7].plus(factor); 	// G
            // zero
            factors[K] = BigRational.ZERO;
        }
        int J = 10;
        factor = factors[J]; // J = H + E - C - 1
        if(!factor.isZero()) {
            factors[0] = factors[0].minus(factor); 	// -1
            factors[3] = factors[3].minus(factor); 	// -C
            factors[5] = factors[5].plus(factor); 	// E
            factors[8] = factors[8].plus(factor); 	// H
            // zero
            factors[J] = BigRational.ZERO;
        }
    }

    private static void normalize26(AlgebraicField field, BigRational[] factors) {
        int L = 12; 
    	BigRational factor = factors[L]; // L = 2J -2H +2F -2D +2B -2
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].minus(factor); 	// -units
            factors[ 0] = factors[ 0].minus(factor); 	// -units again = -2
            factors[ 2] = factors[ 2].plus(factor); 	// B
            factors[ 2] = factors[ 2].plus(factor); 	// B again = 2B
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 4] = factors[ 4].minus(factor); 	// -D again = -2D
            factors[ 6] = factors[ 6].plus(factor); 	// F
            factors[ 6] = factors[ 6].plus(factor); 	// F again = 2F
            factors[ 8] = factors[ 8].minus(factor); 	// -H
            factors[ 8] = factors[ 8].minus(factor); 	// -H again = -2H
            factors[10]= factors[10].plus(factor); 		// J
            factors[10]= factors[10].plus(factor); 		// J again = 2J
            // zero
            factors[L] = BigRational.ZERO;
        }
    }
    
    private static void normalize27(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 12, 3, 4); // L = C + D
        normalizeFactor(factors, 11, 2, 5); // K = B + E
        normalizeFactor(factors, 10, 1, 6); // J = A + F
        normalizeFactor(factors,  9, 0, 7); // I = 1 + G
    }
    
    private static void normalize28(AlgebraicField field, BigRational[] factors) {
        int M = 13;
    	BigRational factor = factors[M]; // M = 2I -2E +2A
        if(!factor.isZero()) {
            factors[1] = factors[1].plus(factor); 	// A
            factors[1] = factors[1].plus(factor); 	// A again = 2A
            factors[5] = factors[5].minus(factor); 	// -E
            factors[5] = factors[5].minus(factor); 	// -E again = -2E
            factors[9] = factors[9].plus(factor);	// I
            // zero
            factors[M] = BigRational.ZERO;
        }
    	int L = 12; 
    	factor = factors[L]; // L = J +H -F -D +B +1
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].plus(factor); 	// units
            factors[ 2] = factors[ 2].plus(factor); 	// B
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 6] = factors[ 6].minus(factor); 	// -F
            factors[ 8] = factors[ 8].plus(factor); 	// H
            factors[10] = factors[10].plus(factor); 	// J
            // zero
            factors[L] = BigRational.ZERO;
        }
    }

    private static void normalize30(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors,  8, 4, 2); // H = D + B
        int I = 9;
    	BigRational factor = factors[I]; // I = -G +E +2C +A
        if(!factor.isZero()) {
            factors[1] = factors[1].plus(factor); 	// A
            factors[3] = factors[3].plus(factor); 	// C
            factors[3] = factors[3].plus(factor); 	// C again = 2C
            factors[5] = factors[5].plus(factor); 	// E
            factors[7] = factors[7].minus(factor);	// -G
            // zero
            factors[I] = BigRational.ZERO;
        }
    	int J = 10; 
    	factor = factors[J]; // J = H + 1 = D + B + 1
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// units
            factors[2] = factors[2].plus(factor); 	// B
            factors[4] = factors[4].plus(factor); 	// D
            // zero
            factors[J] = BigRational.ZERO;
        }
        normalizeFactor(factors, 11, 7, 1); // K = G + A
        normalizeFactor(factors, 12, 6, 2); // L = F + B
        normalizeFactor(factors, 13, 5, 3); // M = E + C
        normalizeFactor(factors, 14, 4, 4); // N = D + D = 2D
    }

    private static void normalize33(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 15, 5, 4); // O = E + D
        normalizeFactor(factors, 14, 6, 3); // N = F + C
        normalizeFactor(factors, 13, 7, 2); // M = G + B
        normalizeFactor(factors, 12, 8, 1); // L = H + A
        normalizeFactor(factors, 11, 9, 0); // K = I + 1
        int J = 10;
    	BigRational factor = factors[J]; 	// J = -I +H +2G +F -E -2D -C +B +2A +1
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// -units            
            factors[1] = factors[1].plus(factor); 	// A
            factors[1] = factors[1].plus(factor); 	// A again = 2A
            factors[2] = factors[2].minus(factor); 	// B
            factors[3] = factors[3].minus(factor); 	// -C
            factors[4] = factors[4].plus(factor); 	// -D
            factors[4] = factors[4].plus(factor); 	// -D again = -2D
            factors[5] = factors[5].minus(factor); 	// -E
            factors[6] = factors[6].minus(factor); 	// F
            factors[7] = factors[7].plus(factor); 	// G
            factors[7] = factors[7].plus(factor); 	// G again = 2G
            factors[8] = factors[8].minus(factor); 	// H
            factors[9] = factors[9].plus(factor); 	// -I
            // zero
            factors[J] = BigRational.ZERO;
        }
    }
    
    private static void normalize34(AlgebraicField field, BigRational[] factors) {
        int P = 16; 
    	BigRational factor = factors[P]; // P = 2N -2L +2J -2H +2F -2D +2B -2
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].minus(factor); 	// -units
            factors[ 0] = factors[ 0].minus(factor); 	// -units again = -2
            factors[ 2] = factors[ 2].plus(factor); 	// B
            factors[ 2] = factors[ 2].plus(factor); 	// B again = 2B
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 4] = factors[ 4].minus(factor); 	// -D again = -2D
            factors[ 6] = factors[ 6].plus(factor); 	// F
            factors[ 6] = factors[ 6].plus(factor); 	// F again = 2F
            factors[ 8] = factors[ 8].minus(factor); 	// -H
            factors[ 8] = factors[ 8].minus(factor); 	// -H again = -2H
            factors[10] = factors[10].plus(factor); 	// J
            factors[10] = factors[10].plus(factor); 	// J again = 2J
            factors[12] = factors[12].minus(factor); 	// -L
            factors[12] = factors[12].minus(factor); 	// -L again = -2L
            factors[14] = factors[14].plus(factor); 	// N
            factors[14] = factors[14].plus(factor); 	// N again = 2N
            // zero
            factors[P] = BigRational.ZERO;
        }
    }
    
    private static void normalize35(AlgebraicField field, BigRational[] factors) {
        int P = 16; 
    	BigRational factor = factors[P]; 	// P = J + I -C -B
        if(!factor.isZero()) {
            factors[ 2] = factors[ 2].minus(factor); 	// -B
            factors[ 3] = factors[ 3].minus(factor); 	// -C
            factors[ 9] = factors[ 9].plus(factor); 	// I
            factors[10] = factors[10].plus(factor); 	// J
            // zero
            factors[P] = BigRational.ZERO;
        }
        int o = 15; 
    	factor = factors[o]; 				// O = K + H -D -A
        if(!factor.isZero()) {
            factors[ 1] = factors[ 1].minus(factor); 	// -A
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 8] = factors[ 8].plus(factor); 	// H
            factors[11] = factors[11].plus(factor); 	// K
            // zero
            factors[o] = BigRational.ZERO;
        }
        int N = 14; 
    	factor = factors[N]; 				// N = -K +J +I +2G +F -E -C -2B -A -1
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].minus(factor); 	// -1
            factors[ 1] = factors[ 1].minus(factor); 	// -A
            factors[ 2] = factors[ 2].minus(factor); 	// -B
            factors[ 2] = factors[ 2].minus(factor); 	// -B again 
            factors[ 3] = factors[ 3].minus(factor); 	// -C
            factors[ 5] = factors[ 5].minus(factor); 	// -E
            factors[ 6] = factors[ 6].plus(factor); 	// F
            factors[ 7] = factors[ 7].plus(factor); 	// G
            factors[ 7] = factors[ 7].plus(factor); 	// G again
            factors[ 9] = factors[ 9].plus(factor); 	// I
            factors[10] = factors[10].plus(factor); 	// J
            factors[11] = factors[11].minus(factor); 	// -K
            // zero
            factors[N] = BigRational.ZERO;
        }
        int M = 13;
    	factor = factors[M]; 					// M = K -J +2H +E -D -C -A -1
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].minus(factor); 	// -1
            factors[ 1] = factors[ 1].minus(factor); 	// -A
            factors[ 3] = factors[ 3].minus(factor); 	// -C
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 5] = factors[ 5].plus(factor); 	// E
            factors[ 8] = factors[ 8].plus(factor); 	// H
            factors[ 8] = factors[ 8].plus(factor); 	// H again
            factors[10] = factors[10].minus(factor); 	// -J
            factors[11] = factors[11].plus(factor); 	// K
            // zero
            factors[M] = BigRational.ZERO;
        }
    	int L = 12; 
    	factor = factors[L]; // L = -K +J +I +G +F -C -2B -A
        if(!factor.isZero()) {
            factors[ 1] = factors[ 1].minus(factor); 	// -A
            factors[ 2] = factors[ 2].minus(factor); 	// -B
            factors[ 2] = factors[ 2].minus(factor); 	// -B again
            factors[ 3] = factors[ 3].minus(factor); 	// -C
            factors[ 6] = factors[ 6].plus(factor); 	// F
            factors[ 7] = factors[ 7].plus(factor); 	// G
            factors[ 9] = factors[ 9].plus(factor); 	// I
            factors[10] = factors[10].plus(factor); 	// J
            factors[11] = factors[11].minus(factor); 	// -K
            // zero
            factors[L] = BigRational.ZERO;
        }
    }
    
    // this pattern seems to work for 3 to any power times any even power of 2 (e.g. 3 * 3 * 4 = 36)
    private static void normalize36(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 12, 0, 10); // L = 1 + J
        normalizeFactor(factors, 13, 1,  9); // M = A + I
        normalizeFactor(factors, 14, 2,  8); // N = B + H
        normalizeFactor(factors, 15, 3,  7); // O = C + G
        normalizeFactor(factors, 16, 4,  6); // P = D + F
        normalizeFactor(factors, 17, 5,  5); // Q = E + E
    }

    private static void normalize38(AlgebraicField field, BigRational[] factors) {
    	int R = 18; 
    	BigRational factor = factors[R]; // R = +2 -2B +2D -2F +2H -2J +2L -2N +2P
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// -units
            factors[0] = factors[0].plus(factor); 	// -units again = 2
            factors[2] = factors[2].minus(factor); 	// -B
            factors[2] = factors[2].minus(factor); 	// -B again - 2B
            factors[4] = factors[4].plus(factor); 	// D
            factors[4] = factors[4].plus(factor); 	// D again = 2D
            factors[6] = factors[6].minus(factor); 	// -F
            factors[6] = factors[6].minus(factor);	// -F again = -2F
            factors[8] = factors[8].plus(factor); 	// H
            factors[8] = factors[8].plus(factor); 	// H again = 2H
            factors[10] = factors[10].minus(factor); 	// -J
            factors[10] = factors[10].minus(factor); 	// -J again - 2J
            factors[12] = factors[12].plus(factor); 	// L
            factors[12] = factors[12].plus(factor); 	// L again = 2L
            factors[14] = factors[14].minus(factor); 	// -N
            factors[14] = factors[14].minus(factor);	// -N again = -2N
            factors[16] = factors[16].plus(factor); 	// P
            factors[16] = factors[16].plus(factor); 	// P again = 2P
            // zero
            factors[R] = BigRational.ZERO;
        }	 
    }

    private static void normalize46(AlgebraicField field, BigRational[] factors) {
    	int V = 22; 
    	BigRational factor = factors[V]; // V = +2 -2B +2D -2F +2H -2J +2L -2N +2P -2R +2T
        if(!factor.isZero()) {
            factors[0] = factors[0].plus(factor); 	// -units
            factors[0] = factors[0].plus(factor); 	// -units again = 2
            factors[2] = factors[2].minus(factor); 	// -B
            factors[2] = factors[2].minus(factor); 	// -B again - 2B
            factors[4] = factors[4].plus(factor); 	// D
            factors[4] = factors[4].plus(factor); 	// D again = 2D
            factors[6] = factors[6].minus(factor); 	// -F
            factors[6] = factors[6].minus(factor);	// -F again = -2F
            factors[8] = factors[8].plus(factor); 	// H
            factors[8] = factors[8].plus(factor); 	// H again = 2H
            factors[10] = factors[10].minus(factor); 	// -J
            factors[10] = factors[10].minus(factor); 	// -J again - 2J
            factors[12] = factors[12].plus(factor); 	// L
            factors[12] = factors[12].plus(factor); 	// L again = 2L
            factors[14] = factors[14].minus(factor); 	// -N
            factors[14] = factors[14].minus(factor);	// -N again = -2N
            factors[16] = factors[16].plus(factor); 	// P
            factors[16] = factors[16].plus(factor); 	// P again = 2P
            factors[18] = factors[18].minus(factor); 	// -R
            factors[18] = factors[18].minus(factor);	// -R again = -2R
            factors[20] = factors[20].plus(factor); 	// T
            factors[20] = factors[20].plus(factor); 	// T again = 2T
            // zero
            factors[V] = BigRational.ZERO;
        }	 
    }

    // this pattern seems to work for 3 times any even power of 2 (e.g. 3 * 16 = 48)
    private static void normalize48(AlgebraicField field, BigRational[] factors) {
        normalizeFactor(factors, 16, 0, 14); // P = 1 + N
        normalizeFactor(factors, 17, 1, 13); // Q = A + M
        normalizeFactor(factors, 18, 2, 12); // R = B + L
        normalizeFactor(factors, 19, 3, 11); // S = C + K
        normalizeFactor(factors, 20, 4, 10); // T = D + J
        normalizeFactor(factors, 21, 5,  9); // U = E + I
        normalizeFactor(factors, 22, 6,  8); // V = F + H
        normalizeFactor(factors, 23, 7,  7); // W = G + G
    }

    private static void normalize58(AlgebraicField field, BigRational[] factors) {
        int d28 = 28; 
    	BigRational factor = factors[d28]; // d28 = -2 +2B -2D +2F -2H +2J -2L +2N -2P +2R -2T +2V -2X +2Z
        if(!factor.isZero()) {
            factors[ 0] = factors[ 0].minus(factor); 	// -units
            factors[ 0] = factors[ 0].minus(factor); 	// -units again = -2
            factors[ 2] = factors[ 2].plus(factor); 	// B
            factors[ 2] = factors[ 2].plus(factor); 	// B again = 2B
            factors[ 4] = factors[ 4].minus(factor); 	// -D
            factors[ 4] = factors[ 4].minus(factor); 	// -D again = -2D
            factors[ 6] = factors[ 6].plus(factor); 	// F
            factors[ 6] = factors[ 6].plus(factor); 	// F again = 2F
            factors[ 8] = factors[ 8].minus(factor); 	// -H
            factors[ 8] = factors[ 8].minus(factor); 	// -H again = -2H
            factors[10] = factors[10].plus(factor); 	// J
            factors[10] = factors[10].plus(factor); 	// J again = 2J
            factors[12] = factors[12].minus(factor); 	// -L
            factors[12] = factors[12].minus(factor); 	// -L again = -2L
            factors[14] = factors[14].plus(factor); 	// N
            factors[14] = factors[14].plus(factor); 	// N again = 2N
            factors[16] = factors[16].minus(factor); 	// -P
            factors[16] = factors[16].minus(factor); 	// -P again = -2P
            factors[18] = factors[18].plus(factor); 	// R
            factors[18] = factors[18].plus(factor); 	// R again = 2R
            factors[20] = factors[20].minus(factor); 	// -T
            factors[20] = factors[20].minus(factor); 	// -T again = -2T
            factors[22] = factors[22].plus(factor); 	// V
            factors[22] = factors[22].plus(factor); 	// V again = 2V
            factors[24] = factors[24].minus(factor); 	// -X
            factors[24] = factors[24].minus(factor); 	// -X again = -2X
            factors[26] = factors[26].plus(factor); 	// Z
            factors[26] = factors[26].plus(factor); 	// Z again = 2Z
            // zero
            factors[d28] = BigRational.ZERO;
        }
    }
    
    // TODO: Refactor this method into the base class, then use it in SqrtField.normalizePerfectSquare()
    private static void normalizeFactor(BigRational[] factors, int i, int j, int k) {
        BigRational factor = factors[i]; 
        if(!factor.isZero()) {
            factors[j] = factors[j].plus(factor);
            factors[k] = factors[k].plus(factor);
            factors[i] = BigRational.ZERO;
        }        
    }
    
    // TODO: Refactor this method into the base class, then use it in SqrtField.normalizePerfectSquare()
    private static void normalizeFactor(BigRational[] factors, int i, long nj, int j, long nk, int k) {
        BigRational factor = factors[i]; 
        if(!factor.isZero()) {
            factors[j] = factors[j].plus(factor.times(new BigRational(nj)));
            factors[k] = factors[k].plus(factor.times(new BigRational(nk)));
            factors[i] = BigRational.ZERO;
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
