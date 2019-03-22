package com.vzome.core.algebra;

import static com.vzome.core.algebra.PentagonField.PHI_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.sin;

import com.vzome.core.generic.Utilities;

/**
 * @author David Hall
 */
public class PolygonField extends ParameterizedField<Integer> {
    
    /**
     * 
     * @param nSides
     * @return the coefficients of a PolygonField given the same parameter. 
     * This can be used to determine when two fields have compatible coefficients 
     * without having to generate an instance of the class. 
     */
    public static double[] getFieldCoefficients(int nSides) {
        int order = getOrder(nSides);
        double[] coefficients = new double[order]; 
        double[] diagLengths = getDiagonalLengths(nSides);

        // if nSides is prime or a power of two then all diagLengths are returned
        // otherwise, order is less than diagLengths.length, so not all are returned
        for (int i = 0; i < order; i++) {
            coefficients[i] = diagLengths[i];
        }
        return coefficients;
    }
    
    /**
     * 
     * @param nSides
     * @return an array with the unique lengths in increasing order 
     * of the diagonals of a regular N-gon having a unit edge length. 
     */
    public static double[] getDiagonalLengths(int nSides) {
        int count = diagonalCount(nSides);
        double[] diagLengths = new double[count]; 
        double unitLength = sin(PI / nSides);

        // The units position should always be exactly 1.0d.
        // We avoid any trig or rounding errors by specifically assigning it that value.
        diagLengths[0] = 1.0d;
        // now initialize the rest, starting from i = 1
        for (int i = 1; i < count; i++) {
            diagLengths[i] = sin((i+1) * PI / nSides) / unitLength;
        }

        // I discovered that a few significant values don't appear to be calculated "correctly" at first glance.
        // I found a great explanation at https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
        switch(nSides) {
        case 6:
            // Since PI is irrational and cannot be exactly represented in a double,
            // the trig functions may not produce the exact result we expect.
            // Specifically, for a hexagon, the calculated value of coefficients[2] is 2.0000000000000004
            // I want to have the exact correct value, so I'm going to hard code it.
            // I'm pretty sure that Niven's theorem https://en.wikipedia.org/wiki/Niven%27s_theorem
            // implies that this will be the only case where we'll get a rational result.
            diagLengths[2] = 2.0d;
            // Similarly, the calculated value of coefficients[1] is 1.7320508075688774 but should exactly equal sqrt(3) which is 1.73205080756887729...
            diagLengths[1] = Math.sqrt(3);
            break;

        case 5:
            // Similarly, for pentagons, the trig calculation for coefficients[1] differs from PHI_VALUE by 0.0000000000000002220446049250313
            // PHI_VALUE       = 1.618033988749895
            // coefficients[1] = 1.618033988749897
            // WolframAlpha says 1.618033988749894848204586834365...
            // I want to have the same value in either case, so I'm going to hard code it.
            diagLengths[1] = PHI_VALUE;
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
        return diagLengths;
    }

    @Override
    public double[] getCoefficients() {
        return getFieldCoefficients(polygonSides());
    }
    
    public static final String FIELD_PREFIX = "polygon";
    
    public static int getOrder(int nSides) {
    	return primaryDiagonalCount(nSides);
    }
    
    public static int diagonalCount(int nSides) {
    	return nSides/2;
    }

    public int diagonalCount() {
    	return diagonalCount(polygonSides());
    }

    // generates the sequence described at https://oeis.org/A055034
    public static int primaryDiagonalCount(int nSides) {
    	// as long as nSides is an int, the result can safely be cast down to an int
    	// but we need a long to hold 2 * nSides without overflowing
    	return (int) (eulerTotient(2L * nSides) / 2L);
    }
    
    public static int secondaryDiagonalCount(int nSides) {
    	return diagonalCount(nSides) - primaryDiagonalCount(nSides);
    }
    
    // returns the number of positive integers <= n and coprime to n
    // The result will always be less than or equal to n/2 
    // This function is also known as eulerPhi() or simpy phi(),
    // but since we use phi for the golden ratio, I'll call it eulerTotient()
    // It generates the sequence described at https://oeis.org/A000010
    public static long eulerTotient(long n) {
       long result = n; 
       for(long i=2; i*i <= n; i++) { 
            if (n % i == 0) result -= result / i; 
            while (n % i == 0) {
            	n /= i;
            }
       } 
       if (n > 1) {
    	   result -= result / n; 
       }
       return result; 
    }
    
    public static short[][][] getNormalizedMultiplicationHolor(int nSides) {
    	short[][][] holor = getExtendedMultiplicationHolor(nSides);
    	if(Utilities.isPrime(nSides) || Utilities.isPowerOfTwo(nSides)) {
    		return holor;
    	}
    	// copy the truncated holor to result
    	int length = primaryDiagonalCount(nSides);
    	short[][][] result = new short[length][length][length];
    	for(int i = 0; i < length; i++) {
        	for(int j = 0; j < length; j++) {
    			for(int k = 0; k < length; k++) {
    				result[i][j][k] = holor[i][j][k];
        		}
        	}
    	}
    	// apply normalizer matrix to result
    	short[][] normalizerMatrix = getNormalizerMatrix(nSides);
    	int n = 0;
    	for(int term = length; term < diagonalCount(nSides); term++) {
    		for(int r = 0; r < length; r++) {
	    		for(int c = 0; c < length; c++) {
        			short omit = holor[term][r][c];
        			if(omit != 0) {
        				for(int t = 0; t < length; t++) {
        					short alt = normalizerMatrix[n][t];
    						if(alt != 0) {
            					int adjust = omit * alt;
    							// This is the same as using 
            					// result[t][r][c] += adjust; 
    							// except that when using the += operator, the cast to short is implicit and thus may be overlooked.
    							result[t][r][c] = (short)(result[t][r][c] + adjust); // cast assumes no overflow or underflow
	        				}
        				}
        			}
        		}
    		}
    		n++;
    	}
    	return result;
    }
    
    public static short[][] getNormalizerMatrix(int nSides) {
    	if(nSides < MIN_SIDES) {
    		throw new IllegalArgumentException("nSides = " + nSides + " but must be greater than or equal to " + MIN_SIDES);
    	}
    	if(PolygonField.secondaryDiagonalCount(nSides) == 0) { // same as isPrime(nSides) || isPowerOfTwo(nSides)
        	return null;
    	}
    	// TODO: calculate normalizerMatrix for any valid nSides 
    	// rather than using this hard-coded switch for specific known values
    	switch(nSides) {
			case 6:
			return new short[][] {
			    // 1  a
			    {  2, 0, },    //  b =  +2       |
			};
			
			case 9:
			return new short[][] {
			    // 1  a  b
			    {  1, 1, 0, },    //  c =  +1   +1a      |
			};
			
			case 10:
			return new short[][] {
			    // 1  a  b  c
			    { -2, 0, 2, 0, },    //  d =  -2        +2b      |
			};
			
			case 12:
			return new short[][] {
			    // 1  a  b  c
			    {  1, 0, 1, 0, },    //  d =  +1        +1b      |
			    {  0, 2, 0, 0, },    //  e =       +2a           |
			};
			
			case 14:
			return new short[][] {
			    // 1  a  b  c  d  e
			    {  2, 0,-2, 0, 2, 0, },    //  f =  +2        -2b       +2d      |
			};
			
			case 15:
			return new short[][] {
			    // 1  a  b  c
			    {  1, 2, 1,-1, },    //  d =  +1   +2a  +1b  -1c |
			    {  1, 0, 0, 1, },    //  e =  +1             +1c |
			    {  0, 1, 1, 0, },    //  f =       +1a  +1b      |
			};
			
			case 18:
			return new short[][] {
			    // 1  a  b  c  d  e
			    {  1, 0, 0, 0, 1, 0, },    //  f =  +1                  +1d      |
			    {  0, 1, 0, 1, 0, 0, },    //  g =       +1a       +1c           |
			    {  0, 0, 2, 0, 0, 0, },    //  h =            +2b                |
			};
			
			case 20:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g
			    { -1, 0,-1, 0, 1, 0, 1, 0, },    //  h =  -1        -1b       +1d       +1f      |
			    {  0,-2, 0, 0, 0, 2, 0, 0, },    //  i =       -2a                 +2e           |
			};
			
			case 21:
			return new short[][] {
			    // 1  a  b  c  d  e
			    { -2,-1, 1, 2, 1,-1, },    //  f =  -2   -1a  +1b  +2c  +1d  -1e |
			    {  1, 0, 0, 0, 0, 1, },    //  g =  +1                       +1e |
			    {  0, 1, 0, 0, 1, 0, },    //  h =       +1a            +1d      |
			    {  0, 0, 1, 1, 0, 0, },    //  i =            +1b  +1c           |
			};
			
			case 22:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i
			    {  2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  j =  +2        -2b       +2d       -2f       +2h      |
			};
			
			case 24:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g
			    {  1, 0, 0, 0, 0, 0, 1, 0, },    //  h =  +1                            +1f      |
			    {  0, 1, 0, 0, 0, 1, 0, 0, },    //  i =       +1a                 +1e           |
			    {  0, 0, 1, 0, 1, 0, 0, 0, },    //  j =            +1b       +1d                |
			    {  0, 0, 0, 2, 0, 0, 0, 0, },    //  k =                 +2c                     |
			};
			
			case 25:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i
			    { -1, 0, 0,-1, 0, 1, 0, 0, 1, 0, },    //  j =  -1             -1c       +1e            +1h      |
			    {  0,-1,-1, 0, 0, 0, 1, 1, 0, 0, },    //  k =       -1a  -1b                 +1f  +1g           |
			};
			
			case 26:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    { -2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  l =  -2        +2b       -2d       +2f       -2h       +2j      |
			};
			
			case 27:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h
			    {  1, 0, 0, 0, 0, 0, 0, 1, 0, },    //  i =  +1                                 +1g      |
			    {  0, 1, 0, 0, 0, 0, 1, 0, 0, },    //  j =       +1a                      +1f           |
			    {  0, 0, 1, 0, 0, 1, 0, 0, 0, },    //  k =            +1b            +1e                |
			    {  0, 0, 0, 1, 1, 0, 0, 0, 0, },    //  l =                 +1c  +1d                     |
			};
			
			case 28:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    {  1, 0, 1, 0,-1, 0,-1, 0, 1, 0, 1, 0, },    //  l =  +1        +1b       -1d       -1f       +1h       +1j      |
			    {  0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0, },    //  m =       +2a                 -2e                 +2i           |
			};
			
			case 30:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g
			    {  0, 0, 1, 0, 1, 0, 0, 0, },    //  h =            +1b       +1d                |
			    {  0, 1, 0, 2, 0, 1, 0,-1, },    //  i =       +1a       +2c       +1e       -1g |
			    {  1, 0, 1, 0, 1, 0, 0, 0, },    //  j =  +1        +1b       +1d                |
			    {  0, 1, 0, 0, 0, 0, 0, 1, },    //  k =       +1a                           +1g |
			    {  0, 0, 1, 0, 0, 0, 1, 0, },    //  l =            +1b                 +1f      |
			    {  0, 0, 0, 1, 0, 1, 0, 0, },    //  m =                 +1c       +1e           |
			    {  0, 0, 0, 0, 2, 0, 0, 0, },    //  n =                      +2d                |
			};
			
			case 33:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i
			    {  1, 2, 1,-1,-2,-1, 1, 2, 1,-1, },    //  j =  +1   +2a  +1b  -1c  -2d  -1e  +1f  +2g  +1h  -1i |
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 1, },    //  k =  +1                                           +1i |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 1, 0, },    //  l =       +1a                                +1h      |
			    {  0, 0, 1, 0, 0, 0, 0, 1, 0, 0, },    //  m =            +1b                      +1g           |
			    {  0, 0, 0, 1, 0, 0, 1, 0, 0, 0, },    //  n =                 +1c            +1f                |
			    {  0, 0, 0, 0, 1, 1, 0, 0, 0, 0, },    //  o =                      +1d  +1e                     |
			};
			
			case 34:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o
			    { -2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  p =  -2        +2b       -2d       +2f       -2h       +2j       -2l       +2n      |
			};
			
			case 35:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    {  0,-1,-2,-1, 0, 0, 1, 1, 0, 1, 1,-1, },    //  l =       -1a  -2b  -1c            +1f  +1g       +1i  +1j  -1k |
			    { -1,-1, 0,-1,-1, 1, 0, 0, 2, 0,-1, 1, },    //  m =  -1   -1a       -1c  -1d  +1e            +2h       -1j  +1k |
			    { -1,-1,-2,-1, 0,-1, 1, 2, 0, 1, 1,-1, },    //  n =  -1   -1a  -2b  -1c       -1e  +1f  +2g       +1i  +1j  -1k |
			    {  0,-1, 0, 0,-1, 0, 0, 0, 1, 0, 0, 1, },    //  o =       -1a            -1d                 +1h            +1k |
			    {  0, 0,-1,-1, 0, 0, 0, 0, 0, 1, 1, 0, },    //  p =            -1b  -1c                           +1i  +1j      |
			};
			
			case 36:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  l =  +1                                                +1j      |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, },    //  m =       +1a                                     +1i           |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, },    //  n =            +1b                           +1h                |
			    {  0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, },    //  o =                 +1c                 +1g                     |
			    {  0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, },    //  p =                      +1d       +1f                          |
			    {  0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, },    //  q =                           +2e                               |
			};
			
			case 38:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q
			    {  2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  r =  +2        -2b       +2d       -2f       +2h       -2j       +2l       -2n       +2p      |
			};
			
			case 39:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    { -2,-1, 1, 2, 1,-1,-2,-1, 1, 2, 1,-1, },    //  l =  -2   -1a  +1b  +2c  +1d  -1e  -2f  -1g  +1h  +2i  +1j  -1k |
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, },    //  m =  +1                                                     +1k |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  n =       +1a                                          +1j      |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, },    //  o =            +1b                                +1i           |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, },    //  p =                 +1c                      +1h                |
			    {  0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, },    //  q =                      +1d            +1g                     |
			    {  0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, },    //  r =                           +1e  +1f                          |
			};
			
			case 40:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o
			    { -1, 0, 0, 0, 0, 0,-1, 0, 1, 0, 0, 0, 0, 0, 1, 0, },    //  p =  -1                            -1f       +1h                           +1n      |
			    {  0,-1, 0, 0, 0,-1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, },    //  q =       -1a                 -1e                 +1i                 +1m           |
			    {  0, 0,-1, 0,-1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, },    //  r =            -1b       -1d                           +1j       +1l                |
			    {  0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, },    //  s =                 +2c                                     +2k                     |
			};
			
			case 42:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    { -1, 0,-1, 0, 0, 0, 1, 0, 1, 0, 0, 0, },    //  l =  -1        -1b                 +1f       +1h                |
			    {  0,-2, 0,-1, 0, 1, 0, 2, 0, 1, 0,-1, },    //  m =       -2a       -1c       +1e       +2g       +1i       -1k |
			    {  0, 0,-1, 0, 0, 0, 1, 0, 1, 0, 0, 0, },    //  n =            -1b                 +1f       +1h                |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, },    //  o =       +1a                                               +1k |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  p =            +1b                                     +1j      |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, },    //  q =                 +1c                           +1i           |
			    {  0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, },    //  r =                      +1d                 +1h                |
			    {  0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, },    //  s =                           +1e       +1g                     |
			    {  0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, },    //  t =                                +2f                          |
			};
			
			case 44:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s
			    {  1, 0, 1, 0,-1, 0,-1, 0, 1, 0, 1, 0,-1, 0,-1, 0, 1, 0, 1, 0, },    //  t =  +1        +1b       -1d       -1f       +1h       +1j       -1l       -1n       +1p       +1r      |
			    {  0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0, },    //  u =       +2a                 -2e                 +2i                 -2m                 +2q           |
			};
			
			case 45:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k
			    {  1,-1, 0, 1, 0, 0, 0, 1, 0,-1, 1, 0, },    //  l =  +1   -1a       +1c                 +1g       -1i  +1j      |
			    { -1, 1, 0, 0, 1, 0, 1, 0, 0, 1,-1, 0, },    //  m =  -1   +1a            +1d       +1f            +1i  -1j      |
			    {  0, 0, 1, 0, 0, 2, 0, 0, 1, 0, 0,-1, },    //  n =            +1b            +2e            +1h            -1k |
			    {  0, 1, 0, 0, 1, 0, 1, 0, 0, 1,-1, 0, },    //  o =       +1a            +1d       +1f            +1i  -1j      |
			    {  1, 0, 0, 1, 0, 0, 0, 1, 0,-1, 1, 0, },    //  p =  +1             +1c                 +1g       -1i  +1j      |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, },    //  q =            +1b                                          +1k |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, },    //  r =                 +1c                                +1j      |
			    {  0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, },    //  s =                      +1d                      +1i           |
			    {  0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, },    //  t =                           +1e            +1h                |
			    {  0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, },    //  u =                                +1f  +1g                     |
			};
			
			case 46:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u
			    {  2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  v =  +2        -2b       +2d       -2f       +2h       -2j       +2l       -2n       +2p       -2r       +2t      |
			};
			
			case 48:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  p =  +1                                                                    +1n      |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, },    //  q =       +1a                                                         +1m           |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, },    //  r =            +1b                                               +1l                |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, },    //  s =                 +1c                                     +1k                     |
			    {  0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, },    //  t =                      +1d                           +1j                          |
			    {  0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, },    //  u =                           +1e                 +1i                               |
			    {  0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, },    //  v =                                +1f       +1h                                    |
			    {  0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, },    //  w =                                     +2g                                         |
			};
			
			case 49:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t
			    {  1, 0, 0, 0, 0, 1, 0,-1, 0, 0, 0, 0,-1, 0, 1, 0, 0, 0, 0, 1, 0, },    //  u =  +1                       +1e       -1g                      -1l       +1n                      +1s      |
			    {  0, 1, 0, 0, 1, 0, 0, 0,-1, 0, 0,-1, 0, 0, 0, 1, 0, 0, 1, 0, 0, },    //  v =       +1a            +1d                 -1h            -1k                 +1o            +1r           |
			    {  0, 0, 1, 1, 0, 0, 0, 0, 0,-1,-1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, },    //  w =            +1b  +1c                           -1i  -1j                           +1p  +1q                |
			};
			
			case 50:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s
			    { -1, 0, 0, 0, 0, 0, 0, 0,-1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  t =  -1                                      -1h       +1j                                     +1r      |
			    {  0,-1, 0, 0, 0, 0, 0,-1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, },    //  u =       -1a                           -1g                 +1k                           +1q           |
			    {  0, 0,-1, 0, 0, 0,-1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, },    //  v =            -1b                 -1f                           +1l                 +1p                |
			    {  0, 0, 0,-1, 0,-1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, },    //  w =                 -1c       -1e                                     +1m       +1o                     |
			    {  0, 0, 0, 0,-2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, },    //  x =                      -2d                                               +2n                          |
			};
			
			case 51:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o
			    {  1, 2, 1,-1,-2,-1, 1, 2, 1,-1,-2,-1, 1, 2, 1,-1, },    //  p =  +1   +2a  +1b  -1c  -2d  -1e  +1f  +2g  +1h  -1i  -2j  -1k  +1l  +2m  +1n  -1o |
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, },    //  q =  +1                                                                         +1o |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  r =       +1a                                                              +1n      |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, },    //  s =            +1b                                                    +1m           |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, },    //  t =                 +1c                                          +1l                |
			    {  0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, },    //  u =                      +1d                                +1k                     |
			    {  0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, },    //  v =                           +1e                      +1j                          |
			    {  0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, },    //  w =                                +1f            +1i                               |
			    {  0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, },    //  x =                                     +1g  +1h                                    |
			};
			
			case 52:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v  w
			    { -1, 0,-1, 0, 1, 0, 1, 0,-1, 0,-1, 0, 1, 0, 1, 0,-1, 0,-1, 0, 1, 0, 1, 0, },    //  x =  -1        -1b       +1d       +1f       -1h       -1j       +1l       +1n       -1p       -1r       +1t       +1v      |
			    {  0,-2, 0, 0, 0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0, 0,-2, 0, 0, 0, 2, 0, 0, },    //  y =       -2a                 +2e                 -2i                 +2m                 -2q                 +2u           |
			};
			
			case 54:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q
			    {  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, },    //  r =  +1                                                                              +1p      |
			    {  0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, },    //  s =       +1a                                                                   +1o           |
			    {  0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, },    //  t =            +1b                                                         +1n                |
			    {  0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, },    //  u =                 +1c                                               +1m                     |
			    {  0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, },    //  v =                      +1d                                     +1l                          |
			    {  0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, },    //  w =                           +1e                           +1k                               |
			    {  0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, },    //  x =                                +1f                 +1j                                    |
			    {  0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, },    //  y =                                     +1g       +1i                                         |
			    {  0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, },    //  z =                                          +2h                                              |
			};
			
			case 55:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s
			    { -1, 1, 0,-2, 0, 1,-1, 0, 2, 0,-1, 0,-1,-1, 1, 1, 0, 1, 1,-1, },    //  t =  -1   +1a       -2c       +1e  -1f       +2h       -1j       -1l  -1m  +1n  +1o       +1q  +1r  -1s |
			    {  2,-2,-1, 2,-1,-2, 2, 1,-2, 1, 1,-2, 0, 1,-1, 0, 2, 0,-1, 1, },    //  u =  +2   -2a  -1b  +2c  -1d  -2e  +2f  +1g  -2h  +1i  +1j  -2k       +1m  -1n       +2p       -1r  +1s |
			    { -2, 1, 0,-2, 0, 1,-1, 0, 2,-1,-1, 1,-1,-1, 1, 1, 0, 1, 1,-1, },    //  v =  -2   +1a       -2c       +1e  -1f       +2h  -1i  -1j  +1k  -1l  -1m  +1n  +1o       +1q  +1r  -1s |
			    {  0,-1, 0, 0, 0, 0, 0, 0,-1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, },    //  w =       -1a                                -1h                 +1l                                +1s |
			    {  0, 0,-1, 0, 0, 0, 0,-1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, },    //  x =            -1b                      -1g                           +1m                      +1r      |
			    {  0, 0, 0,-1, 0, 0,-1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, },    //  y =                 -1c            -1f                                     +1n            +1q           |
			    {  0, 0, 0, 0,-1,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, },    //  z =                      -1d  -1e                                               +1o  +1p                |
			};
			
			case 58:
			return new short[][] {
			    // 1  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v  w  x  y  z  A
			    { -2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0,-2, 0, 2, 0, },    //  B =  -2        +2b       -2d       +2f       -2h       +2j       -2l       +2n       -2p       +2r       -2t       +2v       -2x       +2z      |
			};
    	}
    	// TODO: No need to throw this exception once I figure out how to calculate any normalizerMatrix
    	throw new IllegalArgumentException("Unable to determine Normalizer Matrix for nSides = " + nSides);
    }
    
    // It seems that the 3D multiplierMatrix should be called multiplicationTensor or multiplicationHolor
    // Since Holor seems more generic, though less commonly used, I will use multiplicationHolor
    // rather than multiplicationTensor since I don't know if they actually qualify as Tensors
    // which have additional characteristic requirements beyond being a multi-dimentional array.
    // If in fact, they are found to qualify as Tensors, then they should be renamed appropriately.
    // TODO: change applicable method and variable names throughout...
    //
    // See https://en.wikipedia.org/wiki/Tensor#Holors, which says:
    // The term holor is not in widespread use, and unfortunately the word "tensor" is often misused 
    // when referring to the multidimensional array representation of a holor, 
    // causing confusion regarding the strict meaning of tensor.
    public static short[][][] getExtendedMultiplicationHolor(int nSides) {
        int order = diagonalCount(nSides);
        short[][][] holor = new short[order][order][order];
    	
        // initialize everything to 0
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                for (int k = 0; k < order; k++) {
                    holor[i][j][k] = 0;
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
                    holor[layer][y][x] += 1;
                    if(x != y) {
                        holor[layer][x][y] += 1; // mirror around x == y
                    }
                }
            }
        }

        // initialize the remaining /<->/ SouthWesterly diagonal paths
        int box = nSides - 2;
        int parity = (nSides + 1) % 2;
        for (int layer = 0; layer < order-parity; layer++) {
            int base = box - layer;
            for (int xb = base, yb = 0; xb >= 0; xb--, yb++) {
                int x=xb;
                int y=yb;
                while(x<order && y<order) {
                    holor[layer][y][x] += 1;
                    x++;
                    y++;
                }
            }
        }
        return holor;
    }

    private final boolean isEven;
    
    public PolygonField(int polygonSides) {
        this( FIELD_PREFIX + polygonSides, polygonSides);
    }

    // this protected c'tor is intended to allow PentagonField and HeptagonField classes to be refactored
    // so they are derived from PolygonField and still maintain their original legacy names
    protected PolygonField(String name, int polygonSides) {
        super( name, getOrder(polygonSides) /* diagonalCount(polygonSides)*/, polygonSides);
        isEven = operand % 2 == 0;
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
    	int nSides = polygonSides();
    	if(irrationalLabels.length != diagonalCount(nSides)) {
    		String[] unitLabels = irrationalLabels[0];
    		irrationalLabels = new String[diagonalCount(nSides)][unitLabels.length];
    		irrationalLabels[0] = unitLabels; // retain the default labels for units
    	}
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
                if(irrationalLabels.length > 2)
                irrationalLabels[2] = new String[]{ "\u03B2", "beta" };
                break;

            case 7:
                irrationalLabels[1] = new String[]{ "\u03C1", "rho" };
                irrationalLabels[2] = new String[]{ "\u03C3", "sigma" };
                break;

            case 9:
                irrationalLabels[1] = new String[]{ "\u03B1", "alpha" };
                irrationalLabels[2] = new String[]{ "\u03B2", "beta" };
                if(irrationalLabels.length > 3)
                irrationalLabels[3] = new String[]{ "\u03B3", "gamma" };
                break;

            case 11:
                irrationalLabels[1] = new String[]{ "\u03B8", "theta"  };
                irrationalLabels[2] = new String[]{ "\u03BA", "kappa"  };
                irrationalLabels[3] = new String[]{ "\u03BB", "lambda" };
                irrationalLabels[4] = new String[]{ "\u03BC", "mu"     };
                break;

            case 13:
            	// See https://nbviewer.jupyter.org/github/vorth/ipython/blob/master/triskaidecagons/Triskaidecagons.ipynb
                irrationalLabels[1] = new String[]{ "\u03B1", "alpha" };
                irrationalLabels[2] = new String[]{ "\u03B2", "beta" };
                irrationalLabels[3] = new String[]{ "\u03B3", "gamma" };
                irrationalLabels[4] = new String[]{ "\u03B4", "delta" };
                irrationalLabels[5] = new String[]{ "\u03B5", "epsilon" };
                break;

            default:
                // TODO: Move this default behavior into the base class
                final String alphabet = "abcdefghijklmnopqrstuvwxyz";
                int length = irrationalLabels.length;
                if(length -1 <= alphabet.length()) {
                    for(int i = 1; i < length; i++) {
                        String name = alphabet.substring(i-1, i);
                        irrationalLabels[i] = new String[]{ name, "d[" + i + "]" };
                    }
                }
                else {
                    // The article "Proof by Picture: Products and Reciprocals of Diagonal Length Ratios in the Regular Polygon"
                    // at http://forumgeom.fau.edu/FG2006volume6/FG200610.pdf uses one-based indexing for the diagonals,
                    // but I am going to use zero-based indexing so it corresponds to our coefficients and multiplierMatrix indices.
                    // irrationalLabels[0] remains unchanged from the default (blank).
                    for(int i = 1; i < irrationalLabels.length; i++) {
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
                .replace("+", "\u208A")
                .replace("-", "\u208B")
                ;
    }
    
    /**
     * getUnitTerm(n) expects n < getOrder().
     * This method handles normalized diagonal lengths 
     * where getOrder() <= n < diagonalCount()
     * In these cases, the resulting AlgebraicNumber will not have just the nth factor set to 1,
     * but rather, will have the normalized equivalent.
     * For example, since a normalized PolygonField(6) is of order 2, but diagonalCount() == 3,
     *  PolygonField(6).getUnitTerm(2) would return factors of {2,0} rather than {0,0,1},   
     */
    public AlgebraicNumber getUnitDiagonal(int n) {
        if(n >= getOrder() && n < diagonalCount()) {
            // This is safe since AlgebraicNumber is immutable 
            // and getFactors() returns a copy of its factors rather than the actual array
            BigRational[] factors = zero.getFactors();
            int row = n - getOrder();
            for(int i = 0; i < getOrder(); i++) {
            	int term = normalizerMatrix[row][i];
            	if(term != 0) {
            		factors[i] = new BigRational(term);
            	}
            }
            return createAlgebraicNumber(factors);
        }
        return super.getUnitTerm(n);
    }

    @Override
    protected void initializeCoefficients() {
        double[] temp = getCoefficients();
        int i = 0;
        for(double coefficient : temp) {
        	// TODO: This shouldn't be necessary here
        	// although we do want to get only the diagonal lengths that correspond to the actual normalized order.
            if(i < coefficients.length) {
            	coefficients[i++] = coefficient;
            } else {
            	continue;
            }
        }
    }

    @Override
    protected void initializeMultiplierMatrix() {
    	multiplierMatrix = getNormalizedMultiplicationHolor(polygonSides());
    }

    @Override
    protected void initializeNormalizer() {
    	normalizerMatrix = getNormalizerMatrix(polygonSides());
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
    
//    @Override
//    protected BigRational[] reciprocal( BigRational[] terms )
//    {
//        BigRational[] reciprocalTerms = super.reciprocal( terms );
//        if(mayBeNonInvertable) {
//            AlgebraicNumber num = createAlgebraicNumber( terms ); 
//            AlgebraicNumber recip = createAlgebraicNumber( reciprocalTerms ); 
//            if(! num.times(recip) .isOne()) {
//                String msg = "The AlgebraicNumber '" + num.toString() + "' is non-invertable in the " + getName() + " field.";
//                throw new IllegalArgumentException(msg);
//            }
//        }
//        return reciprocalTerms;
//    }

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
