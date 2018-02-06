package com.vzome.core.algebra;

import static com.vzome.core.algebra.AlgebraicField.VEF_FORMAT;
import static com.vzome.core.algebra.ParameterizedFields.printMatrices;

import org.junit.Test;

/**
 * @author David Hall
 */
public class SnubPolyhedronFieldTest {

	@Test
	public void printSnubPolyhedronFieldMatrices() {
		// printMatrices(new SnubCubeField());
		printMatrices(new SnubDodecahedronField());
	}

	@Test
	public void testSnubDodecahedronFieldReciprocal() {
		AlgebraicField[] fields = { 
			new SnubDodecField(),
			new SnubDodecahedronField()
		};

		int format = VEF_FORMAT;
		for (AlgebraicField field : fields) {
			AlgebraicNumber unitTerm = field.getUnitTerm(field.getOrder()-1);
			System.out.println(field.getName());
			System.out.println("reciprocal of " + unitTerm.toString(format) + " = " + unitTerm.reciprocal().toString(format) + "\n");
		}
	}

}
