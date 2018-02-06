package com.vzome.core.algebra;

import static com.vzome.core.algebra.AlgebraicField.DEFAULT_FORMAT;
import static com.vzome.core.algebra.AlgebraicField.EXPRESSION_FORMAT;
import static com.vzome.core.algebra.AlgebraicField.VEF_FORMAT;
import static com.vzome.core.algebra.AlgebraicField.ZOMIC_FORMAT;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

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
																	// multiplying any ulp error
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
			BigRational[] factors1 = field.createRational(0).getFactors();
			factors1[i] = BigRational.ONE;
			AlgebraicNumber n1 = new AlgebraicNumber(field, factors1);
			buf.append("  { ");
			for (int j = 0; j < n; j++) {
				BigRational[] factors2 = field.createRational(0).getFactors();
				factors2[j] = BigRational.ONE;
				AlgebraicNumber n2 = new AlgebraicNumber(field, factors2);
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
		// System.out.println("coefficients" + name + coefficientsToString(field));
		System.out.println("coefficientsMultiplied" + name + coefficientsMultipliedToString(field));
		System.out.println("multiplierMatrix" + name + multiplierMatrixToString(field));
		System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, DEFAULT_FORMAT));
		 System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, EXPRESSION_FORMAT));
		 System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, ZOMIC_FORMAT));
		// VEF_FORMAT order is reversed from other formats
		 System.out.println("factorsMultiplied" + name + factorsMultipliedToString(field, VEF_FORMAT)); 
		 System.out.println("VEF" + name + multiplierMatrixToVefString(field));
		 System.out.println("hull" + name + hullToVefString(field));
		 System.out.println("axes" + name + axesToVefString(field));
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
