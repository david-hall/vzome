package com.vzome.core.algebra;

import static com.vzome.core.generic.Utilities.thisSourceCodeLine;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import java.math.BigInteger;

import org.junit.Test;

/**
 * @author David Hall
 * This class is a collection of static methods to be used for developing and documenting various ParameterizedFields 
 */
public class ParameterizedFields {
	protected final static String VEF_HEADER = "vZome VEF 7 field rational\n\nactual scale 10\n\n";

	public static String axesToVefString(ParameterizedField<?> field) {
		StringBuffer buf = new StringBuffer();
		buf.append(VEF_HEADER);

		buf.append("8\n"); // vertices
		int order = field.getOrder();
		for (int x = 0; x <= order; x += order) {
			for (int y = 0; y <= order; y += order) {
				for (int z = 0; z <= order; z += order) {
					buf.append("0 ").append(x).append(" ").append(y).append(" ").append(z).append("\n");
				}
			}
		}
		buf.append("\n");

		buf.append("3\n"); // struts
		buf.append("0 1\n");
		buf.append("0 2\n");
		buf.append("0 4\n");
		buf.append("\n");

		buf.append("4\n"); // panels
		buf.append("4  0 1 3 2\n");
		buf.append("4  0 1 5 4\n");
		buf.append("4  0 2 6 4\n");
		buf.append("4  0 2 7 5\n"); // mirror plane must be normal to the horizontal plane to match
									// multiplierMatrixToVefString()
		buf.append("\n");

		buf.append("0\n"); // balls
		return buf.toString();
	}

    public static String coefficientsMultipliedToString(ParameterizedField<?> field) {
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        for (Double c1 : field.coefficients) {
            buf.append("  { ");
            for (Double c2 : field.coefficients) {
                buf.append(String.format("%1$20.14f, ", c1 * c2)); // show one less decimal point because we're also
                                                                    // including any ulp error
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public static String coefficientsDividedToString(ParameterizedField<?> field) {
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        for (Double c1 : field.coefficients) {
            buf.append("  { ");
            for (Double c2 : field.coefficients) {
                buf.append(String.format("%1$20.14f, ", c1 / c2)); // show one less decimal point because we're also
                                                                    // including any ulp error
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public static String coefficientsScaledToString(ParameterizedField<?> field) {
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        for (Double c1 : field.coefficients) {
            buf.append("  { ");
            for (Double c2 = 1.0d; c2 < field.getOrder(); c2++) {
                buf.append(String.format("%1$20.14f, ", c1 * c2)); // show one less decimal point because we're also
                                                                    // including any ulp error
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public static String coefficientsAddedToString(ParameterizedField<?> field) {
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        for (Double c1 : field.coefficients) {
            buf.append("  { ");
            for (Double c2 : field.coefficients) {
                buf.append(String.format("%1$20.14f, ", c1 + c2)); // show one less decimal point because we're also
                                                                    // including any ulp error
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public static String coefficientsSubtractedToString(ParameterizedField<?> field) {
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        for (Double c1 : field.coefficients) {
            buf.append("  { ");
            for (Double c2 : field.coefficients) {
                buf.append(String.format("%1$20.14f, ", c1 - c2)); // show one less decimal point because we're also
                                                                    // including any ulp error
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

	public static String coefficientsToString(ParameterizedField<?> field) {
		StringBuffer buf = new StringBuffer();
		buf.append("{\n");
		for (Double d : field.coefficients) {
			buf.append(String.format("  %1$20.15f,\n", d));
		}
		buf.append("}\n");
		return buf.toString();
	}
	
	// these are all used by printNormalization and the methods it calls
	// they are local instead of being passed as args to reduce the recursuve stack size.
	private static int[] scalars;
	private static PolygonField field;
	private static double candidate;
	
	public static void printNormalization(PolygonField inField) {
		field = inField;
		final int sides = field.polygonSides();
		// positive powers of two
        if ((sides & (sides - 1)) == 0) {
//        	System.out.print(field.getName() + ": Powers of two do not require normalization. " + thisSourceCodeLine());
            return;
        }
        // prime
        final int certainty = 100; // TODO: Determine the min reliable certainty here for any valid Integer nSides 
        if(BigInteger.valueOf(sides).isProbablePrime(certainty)) {
//			System.out.println(field.getName() + ": Prime Numbers do not require normalization. " + thisSourceCodeLine());
			return;
		}
		final int order = field.getOrder();
		if(order == sides/2) {
			int normalizedOrder = getNormalizedOrder();
			if(order > normalizedOrder) {
				System.out.println("\n" + field.getName() + " is not normalized. order = " + order + " should be " + normalizedOrder + ". " + thisSourceCodeLine());
				System.out.println(coefficientsToString(field));
	
				scalars = new int[normalizedOrder+1]; // native types are all initialized to 0.
				for(int q = order-1; q >= normalizedOrder; q--) {
					candidate = field.getCoefficient(q);
					String sq = field.getIrrational(q);
					System.out.println("\n try " + sq + " [" + q + "]: " + candidate);
					if(consider(0)) {
						printScalars();
					}
				}
			}
		}
	}

	private static void printScalars() {
		for(int scalar : scalars) {
			System.out.print((scalar < 0 ? " " : " +") + scalar);
		}
		System.out.println();
	}

	private static boolean testScalars() {
		double total = 0.0d;
		int count = 0;
		int mult = 0;
		for(int i = 0; i < scalars.length; i++) {
			int scalar = scalars[i];
			if(scalar != 0) {
				count++;
				if(scalar != 1) {
					mult++;
				}
				double coefficient = field.getCoefficient(i);
				total += coefficient * scalar;
			}
		}
		if((count > 1 || mult > 0) && nearEqual(candidate, total)) {
			for(int i = scalars.length-1; i >= 0; i--) {
				int scalar = scalars[i];
				if(scalar == 0) {
					System.out.print("    ");
				} else {
					String irr = field.getIrrational(i);
					System.out.print((scalar < 0 ? " " : " +") + scalar + irr);
				}
			}
			System.out.println(" = " + total);
			
//			for(int i = scalars.length-1; i >= 0; i--) {
//				int scalar = scalars[i];
////				if(scalar != 0) {
//					double coefficient = field.getCoefficient(i);
//					System.out.print((scalar < 0 ? " " : " +") + scalar + "(" + coefficient + ")");
////				}
//			}
//			System.out.println(" = " + total);

//			return true;
		}
		return false;
	}

	final private static int[] testScalars = {0, 1, -1, 2, -2}; 
	
	private static boolean consider(int depth) {
		if(depth == scalars.length) {
			// evaluate
			return testScalars();
		} else {
			if(depth == scalars.length - 15) {
				printScalars(); // just so I can be sure it's not hung and judge the time to complete
			}
			for(int scalar : testScalars) {
				scalars[depth] = scalar;
				if(consider(depth+1)) {
					return false;
				}
			}
			return false;
		}
	}
	
	private static boolean nearEqual(double d1, double d2) {
		double delta = 0.000000000001d; // eleven 0's between the decimal point and the 1
		return Math.abs(d1 - d2) < delta;
	}
	
	private static int getNormalizedOrder() {
		switch(field.polygonSides()) {
		case 22:
			return (field.polygonSides() / 2) - 1;
			
		case 25:
			return (field.polygonSides() / 2) - 2;
			
		case 26:
			return (field.polygonSides() / 2) - 2;
			
		case 28:
			return (field.polygonSides() / 2) - 3;
			
		case 34:
			return (field.polygonSides() / 2) - 2;
			
		case 35:
			return (field.polygonSides() / 2) - 5;
			
		case 38:
			return (field.polygonSides() / 2) - 2;
			
		case 39:
			return (field.polygonSides() / 2) - 7;

		case 44:
			return (field.polygonSides() / 2) - 3;

		case 45:
			return 12;

		case 46:
			return (field.polygonSides() / 2) - 2;
			
		case 49:
			return (field.polygonSides() / 2) - 4;

		case 54:
			return 18;

		}
		return (field.polygonSides() / 4)+1;
		// TODO: Optimize this using the number of non-invertible terms
//		int order = field.getOrder();
//		int normalizedOrder = field.getOrder();
//		for(int i = order-1; i > 0; i--) {
//			AlgebraicNumber n1 = field.getUnitTerm(i);
//			BigRational[] factors = n1.getFactors();
//            BigRational[][] representation = new BigRational[ order ][ order ];
//            for ( int j = 0; j < order; j++ ) {
//                BigRational[] column = field .scaleBy( factors, j );
//                System.arraycopy(column, 0, representation[ j ], 0, order);
//            }
//            BigRational[][] reciprocal = new BigRational[ order ][ order ];
//            for (int j = 0; j < order; j++) {
//                for (int k = 0; k < order; k++) {
//                    reciprocal[j][k] = j == k ? BigRational.ONE : BigRational.ZERO;
//                }
//            }
//            int rank = Fields .gaussJordanReduction( representation, reciprocal );
//            if(rank != order) {
//				normalizedOrder--;
//			}
//		}
//		return normalizedOrder;
	}

    public static String wolframAlphaTestString(AlgebraicField field) {
        final int format = AlgebraicField.DEFAULT_FORMAT;
        StringBuffer buf1 = new StringBuffer();
        StringBuffer buf2 = new StringBuffer();
        int order = field.getOrder();
        String delim = "";
        for (int i = 1; i < order; i++) {
            AlgebraicNumber term = field.getUnitTerm(i);
                term.getNumberExpression(buf1, format);
                buf1.append("^2 = ");
                AlgebraicNumber termSquared = term.times(term);
                termSquared.getNumberExpression(buf1, format);
                buf1.append("; ");

                buf2.append(delim);
                delim = "; "; // we don't want to end up with a trailing delimier on buf2
                term.getNumberExpression(buf2, format);
                buf2.append(" > 0");
        }
        buf1.append(buf2.toString());
        buf1.append("\n");
        return buf1.toString();
    }

    public static String factorsMultipliedToString(AlgebraicField field, int format) {
        // int padLen = 0;
        // for( int n = 0; n < field.getOrder(); n++) {
        // padLen += (2 + field.getIrrational(n, format).length());
        // }
        // final String padding = new String(new char[padLen]).replace('\0', ' ');
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        int n = field.getOrder();
        for (int i = 0; i < n; i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            buf.append("  { ");
            for (int j = 0; j < n; j++) {
                AlgebraicNumber n2 = field.getUnitTerm(j);
                AlgebraicNumber product1 = n1.times(n2);
                AlgebraicNumber product2 = n2.times(n1);
                String s = product1.toString(format).replace(" ", "");
                buf.append(s);
                buf.append(",");
                buf.append("\t");
                // buf.append( padding.substring(0, padLen - s.length() ) );
                assertEquals(product1, product2);
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        // TODO: recalc columns with spaces to replace tabs after all has been generated
        // and spaces can be minimized
        return buf.toString();
    }

    public static String factorsDividedToString(AlgebraicField field, int format) {
        // int padLen = 0;
        // for( int n = 0; n < field.getOrder(); n++) {
        // padLen += (2 + field.getIrrational(n, format).length());
        // }
        // final String padding = new String(new char[padLen]).replace('\0', ' ');
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        int n = field.getOrder();
        for (int i = 0; i < n; i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            buf.append("  { ");
            for (int j = 0; j < n; j++) {
                AlgebraicNumber n2 = field.getUnitTerm(j);
                String s = "";
                try {
                    AlgebraicNumber quotient = n1.dividedBy(n2);
                    s = quotient.toString(format).replace(" ", "");
                }
                catch(IllegalArgumentException ex) {
                    s = "\t?";
                }
                buf.append(s);
                buf.append(",");
                buf.append("\t");
                // buf.append( padding.substring(0, padLen - s.length() ) );
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        // TODO: recalc columns with spaces to replace tabs after all has been generated
        // and spaces can be minimized
        return buf.toString();
    }

    public static String factorsEvaluatedDividedToString(AlgebraicField field, int format) {
        // int padLen = 0;
        // for( int n = 0; n < field.getOrder(); n++) {
        // padLen += (2 + field.getIrrational(n, format).length());
        // }
        // final String padding = new String(new char[padLen]).replace('\0', ' ');
        StringBuffer buf = new StringBuffer();
        buf.append("{\n");
        int n = field.getOrder();
        for (int i = 0; i < n; i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            buf.append("  { ");
            for (int j = 0; j < n; j++) {
                AlgebraicNumber n2 = field.getUnitTerm(j);
                String s = "";
                try {
                    AlgebraicNumber quotient = n1.dividedBy(n2);
                    s = Double.toString(quotient.evaluate());
                }
                catch(IllegalArgumentException ex) {
                    s = "\t?";
                }
                buf.append(s);
                buf.append(",");
                buf.append("\t");
                // buf.append( padding.substring(0, padLen - s.length() ) );
            }
            buf.append("},\n");
        }
        buf.append("}\n");
        // TODO: recalc columns with spaces to replace tabs after all has been generated
        // and spaces can be minimized
        return buf.toString();
    }

    public static String factorsReducedToString(AlgebraicField field, int format) {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        int order = field.getOrder();
        for (int i = 0; i < order; i++) {
            AlgebraicNumber n1 = field.getUnitTerm(i);
            BigRational[] factors = n1.getFactors();
            BigRational[][] representation = new BigRational[ order ][ order ];
            for ( int j = 0; j < order; j++ ) {
                BigRational[] column = field .scaleBy( factors, j );
                System.arraycopy(column, 0, representation[ j ], 0, order);
            }
            BigRational[][] reciprocal = new BigRational[ order ][ order ];
            for (int j = 0; j < order; j++) {
                for (int k = 0; k < order; k++) {
                    reciprocal[j][k] = j == k ? BigRational.ONE : BigRational.ZERO;
                }
            }
            int rank = Fields .gaussJordanReduction( representation, reciprocal );
            if(rank != order) {
                buf.append("[");
                buf.append(i);
                buf.append("]-->");
                buf.append(rank);
                buf.append("/");
                buf.append(order);
                buf.append(",  ");
            }    
        }
        buf.append("}\n");
        return buf.toString();
    }

    public static String hullToVefString(ParameterizedField<?> field) {
		StringBuffer buf = new StringBuffer();
		buf.append(VEF_HEADER);

		buf.append("8\n"); // vertices
		int order = field.getOrder();
		for (int x = 0; x < order; x += order - 1) {
			for (int y = 0; y < order; y += order - 1) {
				for (int z = 0; z < order; z += order - 1) {
					buf.append("0 ").append(x).append(" ").append(y).append(" ").append(z).append("\n");
				}
			}
		}
		buf.append("\n");

		buf.append("0\n\n");

		buf.append("7\n"); // panels
		buf.append("3  0 3 5\n");
		buf.append("3  0 3 6\n");
		buf.append("3  0 5 6\n");
		buf.append("3  3 5 6\n");
		buf.append("3  7 3 5\n");
		buf.append("3  7 3 6\n");
		buf.append("3  7 5 6\n");
		buf.append("\n");

		buf.append("0\n"); // balls
		return buf.toString();
	}

	public static String multiplierMatrixToString(ParameterizedField<?> field) {
		StringBuffer buf = new StringBuffer();
		buf.append("{\n");
		for (short[][] outer : field.multiplierMatrix) {
			buf.append("  {\n");
			for (short[] inner : outer) {
				buf.append("    { ");
				for (short i : inner) {
				    if(i < 10) {
				        buf.deleteCharAt(buf.length()-1);
				    }
					buf.append(i).append(", ");
				}
				buf.append("},\n");
			}
			buf.append("  },\n");
		}
		buf.append("}\n");
		return buf.toString();
	}

	public static String multiplierMatrixToVefString(ParameterizedField<?> field) {
		int ballCount = 0;
		for (short[][] outer : field.multiplierMatrix) {
			for (short[] inner : outer) {
				for (short i : inner) {
					if (i != 0) {
						ballCount++;
					}
				}
			}
		}

		StringBuffer buf = new StringBuffer(ballCount * 10);
		buf.append(VEF_HEADER);

		buf.append(ballCount).append("\n"); // vertices
		int order = field.getOrder();
		for (int x = 0; x < order; x++) {
			for (int y = 0; y < order; y++) {
				for (int z = 0; z < order; z++) {
					int multiplier = field.multiplierMatrix[x][y][z];
					if (multiplier != 0) {
						// Note that the x, y, and z are rotated so the reflection plane is shown normal
						// to the horizontal X-Z plane
						// This is only evident when multiplierMatrix doesn't have 3-fold symmetry such
						// as PolygonField(6)
						// or any SqrtField(r) where r is a perfect square such as 1, 4, 9, 16...
						// Since W gets stripped off and ignored when the VEF is imported,
						// we can store multiplier in the W position, just so it's retained for anyone
						// who cares to see it
						buf.append(multiplier).append(" ").append(z).append(" ").append(x).append(" ").append(y)
								.append("\n");
					}
				}
			}
		}
		buf.append("\n");

		buf.append("0\n\n"); // struts

		buf.append("0\n\n"); // panels

		buf.append(ballCount).append("\n"); // balls
		for (int n = 0; n < ballCount; n++) {
			buf.append(n).append(" ");
			if ((n + 1) % 10 == 0) {
				buf.append("\n"); // wrap lines for readability
			}
		}
		buf.append("\n");
		return buf.toString();
	}

	public static void printMatrices(ParameterizedField<?> field) {
        assertNotNull(field);
        String name = "( " + field.toString() + " ) = \n";
//        System.out.println("coefficients" + name + coefficientsToString(field));
//        System.out.println("coefficientsAdded" + name + coefficientsAddedToString(field));
//        System.out.println("coefficientsSubtracted" + name + coefficientsSubtractedToString(field));
//        System.out.println("coefficientsScaled" + name + coefficientsScaledToString(field));
        System.out.println("coefficientsMultiplied" + name + coefficientsMultipliedToString(field));
        System.out.println("coefficientsDivided" + name + coefficientsDividedToString(field));
//        System.out.println("multiplierMatrix" + name + multiplierMatrixToString(field));
//
//        System.out.println( "wolfram alpha test query" + name + ParameterizedFields.wolframAlphaTestString( field ));
//        
//        System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, AlgebraicField.DEFAULT_FORMAT));
//      System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, AlgebraicField.EXPRESSION_FORMAT));
//      System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, AlgebraicField.ZOMIC_FORMAT));
//		// VEF_FORMAT order is reversed from other formats
//        System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, AlgebraicField.VEF_FORMAT));
//		 
        System.out.println("factorsDivided" + name + factorsDividedToString(field, AlgebraicField.DEFAULT_FORMAT));
        System.out.println("factorsEvaluatedDivided" + name + factorsEvaluatedDividedToString(field, AlgebraicField.DEFAULT_FORMAT));
        System.out.println("factorsReduced" + name + factorsReducedToString(field, AlgebraicField.DEFAULT_FORMAT));
//		 
//        System.out.println("VEF" + name + multiplierMatrixToVefString(field));
//        System.out.println("hull" + name + hullToVefString(field));
//        System.out.println("axes" + name + axesToVefString(field));
        System.out.println();
	}

	@Test
	public void printMultiplicationTables() {
		for (int i = 5; i <= 8; i++) {
			ParameterizedField<?> field = new PolygonField(i);
			System.out.println(field.getName() + ".multiplicationTable = " + coefficientsMultipliedToString(field));
		}
	}

/*

coefficientsMultiplied( snubDodecField ) = 
{
  {     1.00000000000000,     1.61803398874990,     1.71556149969737,     2.77583681630108,     2.94315125924388,     4.76211877148865, },
  {     1.61803398874990,     2.61803398874990,     2.77583681630108,     4.49139831599845,     4.76211877148865,     7.70527003073254, },
  {     1.71556149969737,     2.77583681630108,     2.94315125924388,     4.76211877148865,     5.04915698814463,     8.16970762135206, },
  {     2.77583681630108,     4.49139831599845,     4.76211877148865,     7.70527003073254,     8.16970762135206,    13.21886460949669, },
  {     2.94315125924388,     4.76211877148865,     5.04915698814463,     8.16970762135206,     8.66213933478885,    14.01563585897576, },
  {     4.76211877148865,     7.70527003073254,     8.16970762135206,    13.21886460949669,    14.01563585897576,    22.67777519376461, },
}

factorsMultiplied( snubDodecField ) = 
{
  { 1       ,   phi           ,               xi,                  phi*xi,                    xi^2,                        phi*xi^2,    },
  { phi     ,   1 +phi        ,           phi*xi,              xi +phi*xi,                phi*xi^2,                  xi^2 +phi*xi^2,    },
  { xi      ,   phi*xi        ,             xi^2,                phi*xi^2,               phi +2*xi,                1 +phi +2*phi*xi,    },
  { phi*xi  ,   xi +phi*xi    ,         phi*xi^2,          xi^2 +phi*xi^2,        1 +phi +2*phi*xi,           2*phi +2*xi +2*phi*xi,    },
  { xi^2    ,   phi*xi^2      ,        phi +2*xi,        1 +phi +2*phi*xi,          phi*xi +2*xi^2,          xi +phi*xi +2*phi*xi^2,    },
  { phi*xi^2,   xi^2 +phi*xi^2, 1 +phi +2*phi*xi,   2*phi +2*xi +2*phi*xi,  xi +phi*xi +2*phi*xi^2,  xi +phi*xi +2*xi^2 +2*phi*xi^2,    },
}

factorsMultiplied( snubDodecField ) = 
{
  { 1 0 0 0 0 0,    0 1 0 0 0 0,    0 0 1 0 0 0,    0 0 0 1 0 0,    0 0 0 0 1 0,    0 0 0 0 0 1,    },
  { 0 1 0 0 0 0,    1 1 0 0 0 0,    0 0 0 1 0 0,    0 0 1 1 0 0,    0 0 0 0 0 1,    0 0 0 0 1 1,    },
  { 0 0 1 0 0 0,    0 0 0 1 0 0,    0 0 0 0 1 0,    0 0 0 0 0 1,    0 1 2 0 0 0,    1 1 0 2 0 0,    },
  { 0 0 0 1 0 0,    0 0 1 1 0 0,    0 0 0 0 0 1,    0 0 0 0 1 1,    1 1 0 2 0 0,    0 2 2 2 0 0,    },
  { 0 0 0 0 1 0,    0 0 0 0 0 1,    0 1 2 0 0 0,    1 1 0 2 0 0,    0 0 0 1 2 0,    0 0 1 1 0 2,    },
  { 0 0 0 0 0 1,    0 0 0 0 1 1,    1 1 0 2 0 0,    0 2 2 2 0 0,    0 0 1 1 0 2,    0 0 1 1 2 2,    },
}

multiplierMatrix( snubDodecahedron ) = 
{
  {
    { 1, 0, 0, 0, 0, 0, },
    { 0, 1, 0, 0, 0, 0, },
    { 0, 0, 0, 0, 0, 1, },
    { 0, 0, 0, 0, 1, 1, },
    { 0, 0, 0, 1, 0, 0, },
    { 0, 0, 1, 1, 0, 0, },
  },
  {
    { 0, 1, 0, 0, 0, 0, },
    { 1, 1, 0, 0, 0, 0, },
    { 0, 0, 0, 0, 1, 1, },
    { 0, 0, 0, 0, 1, 2, },
    { 0, 0, 1, 1, 0, 0, },
    { 0, 0, 1, 2, 0, 0, },
  },
  {
    { 0, 0, 1, 0, 0, 0, },
    { 0, 0, 0, 1, 0, 0, },
    { 1, 0, 0, 0, 2, 0, },
    { 0, 1, 0, 0, 0, 2, },
    { 0, 0, 2, 0, 0, 1, },
    { 0, 0, 0, 2, 1, 1, },
  },
  {
    { 0, 0, 0, 1, 0, 0, },
    { 0, 0, 1, 1, 0, 0, },
    { 0, 1, 0, 0, 0, 2, },
    { 1, 1, 0, 0, 2, 2, },
    { 0, 0, 0, 2, 1, 1, },
    { 0, 0, 2, 2, 1, 2, },
  },
  {
    { 0, 0, 0, 0, 1, 0, },
    { 0, 0, 0, 0, 0, 1, },
    { 0, 0, 1, 0, 0, 0, },
    { 0, 0, 0, 1, 0, 0, },
    { 1, 0, 0, 0, 2, 0, },
    { 0, 1, 0, 0, 0, 2, },
  },
  {
    { 0, 0, 0, 0, 0, 1, },
    { 0, 0, 0, 0, 1, 1, },
    { 0, 0, 0, 1, 0, 0, },
    { 0, 0, 1, 1, 0, 0, },
    { 0, 1, 0, 0, 0, 2, },
    { 1, 1, 0, 0, 2, 2, },
  },
}

  */

}
