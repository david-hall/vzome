package com.vzome.core.math.symmetry;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.vzome.core.algebra.AlgebraicMatrix;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.HeptagonField;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.kinds.PolygonFieldApplication;
import com.vzome.core.math.RealVector;
import com.vzome.fields.heptagon.HeptagonalAntiprismSymmetry;

/**
 * @author David Hall
 */
public class AntiprismSymmetryTest {

    private static final AntiprismSymmetry getAntiprismSymmetry(int nSides) {
        return new AntiprismSymmetry(new PolygonField(nSides), "antiprism").createStandardOrbits("blue");
    }

    @Test
    public void testPermutations() {
        AntiprismSymmetry symm = getAntiprismSymmetry(7);

        Permutation perm7 = symm.getPermutation(3);
        assertEquals(7, perm7.getOrder());
        assertEquals(0, perm7.mapIndex(4));
        assertEquals(8, perm7.mapIndex(12));
        assertEquals(3, symm.getMapping(11, 7));

        Permutation perm2 = symm.getPermutation(9);
        assertEquals(2, perm2.getOrder());
        assertEquals(4, perm2.mapIndex(12));
        assertEquals(10, perm2.mapIndex(6));
        assertEquals(9, symm.getMapping(3, 13));

        Permutation prod = perm2.compose(perm7);
        assertEquals(2, prod.getOrder());
        assertEquals(10, prod.mapIndex(3));
    }

    @Test
    public void testOrientations() {
        AntiprismSymmetry symm = getAntiprismSymmetry(7);

        AlgebraicMatrix m2 = symm.getMatrix(2);
        AlgebraicMatrix m4 = symm.getMatrix(4);
        AlgebraicMatrix m6 = symm.getMatrix(6);

        assertEquals(m2, m6.times(m6.times(m4)));
    }

    @Test
    public void testGetAxisUncorrected() {
        AntiprismSymmetry symm = getAntiprismSymmetry(7);

        RealVector v1 = new RealVector(0.1, 0.1, 3.0);
        RealVector v2 = new RealVector(0.1, 0.1, -3.0);
        RealVector v3 = new RealVector(0.1, -0.1, 3.0);
        RealVector v4 = new RealVector(0.1, -0.1, -3.0);
        RealVector v5 = new RealVector(-0.1, 0.1, 3.0);
        RealVector v6 = new RealVector(-0.1, 0.1, -3.0);
        RealVector v7 = new RealVector(-0.1, -0.1, 3.0);
        RealVector v8 = new RealVector(-0.1, -0.1, -3.0);

        Direction redOrbit = symm.getDirection("red");

        Axis axis = redOrbit.getAxis(v1);
        Axis expected = redOrbit.getAxis(Axis.PLUS, 1); // these numbers are pretty arbitrary, for red...
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v2);
        expected = redOrbit.getAxis(Axis.PLUS, 7); // since there are really only "up" and "down"
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v3);
        expected = redOrbit.getAxis(Axis.MINUS, 11);
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v4);
        expected = redOrbit.getAxis(Axis.PLUS, 13);
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v5);
        expected = redOrbit.getAxis(Axis.PLUS, 3);
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v6);
        expected = redOrbit.getAxis(Axis.PLUS, 8);
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v7);
        expected = redOrbit.getAxis(Axis.MINUS, 8);
        assertEquals(expected, axis);

        axis = redOrbit.getAxis(v8);
        expected = redOrbit.getAxis(Axis.PLUS, 9);
        assertEquals(expected, axis);
    }

    @Test
    public void testGetAxisCorrected() {
        AntiprismSymmetry symm = getAntiprismSymmetry(7);

        RealVector v1 = new RealVector(0.5, 0.1, 0.1);
        RealVector v2 = new RealVector(0.5, 0.1, -0.1);
        RealVector v3 = new RealVector(0.5, -0.1, 0.1);
        RealVector v4 = new RealVector(0.5, -0.1, -0.1);
        RealVector v5 = new RealVector(-0.5, 0.1, 0.1);
        RealVector v6 = new RealVector(-0.5, 0.1, -0.1);
        RealVector v7 = new RealVector(-0.5, -0.1, 0.1);
        RealVector v8 = new RealVector(-0.5, -0.1, -0.1);

        Direction blueOrbit = symm.getDirection("blue");

        Axis axis = blueOrbit.getAxis(v1);
        Axis expected = blueOrbit.getAxis(Axis.PLUS, 0, true);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v2);
        expected = blueOrbit.getAxis(Axis.MINUS, 0, true);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v3);
        expected = blueOrbit.getAxis(Axis.MINUS, 7, true);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v4);
        expected = blueOrbit.getAxis(Axis.PLUS, 7, true);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v5);
        expected = blueOrbit.getAxis(Axis.PLUS, 7, false);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v6);
        expected = blueOrbit.getAxis(Axis.MINUS, 7, false);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v7);
        expected = blueOrbit.getAxis(Axis.MINUS, 0, false);
        assertEquals(expected, axis);

        axis = blueOrbit.getAxis(v8);
        expected = blueOrbit.getAxis(Axis.PLUS, 0, false);
        assertEquals(expected, axis);
    }

    @Test
    public void testGetRotationMatrix() {
//        System.out.println("getRotationMatrix");
        for(int parity = 0; parity <=1; parity++ ) {
//            System.out.println("\n" + (parity == 0 ? "Even" : "Odd") + "...");
            for (int nSides = PolygonField.MIN_SIDES + parity; nSides <= PolygonFieldApplication.MAX_SIDES; nSides+=2) {
                if(nSides != 9 && PolygonField.mayBeNonInvertable(nSides)) {
                    continue;
                }
                AntiprismSymmetry symm = getAntiprismSymmetry(nSides);
                PolygonField field = symm.getField();
                AlgebraicMatrix rotationMatrix = symm.getRotationMatrix();
//                System.out.println("nSides = " + nSides + " ");
//                System.out.println("rm = " + rotationMatrix);
                final AlgebraicVector posX = field.basisVector(3, AlgebraicVector.X);
                final AlgebraicVector posY = field.basisVector(3, AlgebraicVector.Y);
                final AlgebraicVector negX = field.basisVector(3, AlgebraicVector.X).negate();
                final AlgebraicVector negY = field.basisVector(3, AlgebraicVector.Y).negate();
                AlgebraicVector v = posX;
                int isX = 0;
                int isY = 0;
                int i = 0;
                for (i = 0; i < nSides; i++) {
//                    System.out.println(i + "\t: " + v);
                    v = rotationMatrix.timesColumn(v);
                    if (i % nSides == nSides - 1) {
                        if(nSides != 9)
                        assertEquals(posX, v);
                    } else {
                        assertNotEquals(posX, v);
                    }
                    if (v.equals(posX) || v.equals(negX)) {
                        isX++;
                    } else if (v.equals(posY) || v.equals(negY)) {
                        isY++;
                    }
                }
//                System.out.println(i++ + "\t: " + v);
                if(nSides != 9) {
                assertEquals(posX, v);
                if (field.isOdd()) {
                    assertEquals(1, isX);
                    assertEquals(1, isY);
                } else {
                    assertEquals(2, isX);
                    assertEquals((nSides % 4 == 0) ? 2 : 0, isY);
                }
                }
            }
        }
    }

    @Test
    public void compareHeptagonRotationMatrices() {
//        System.out.println("compareHeptagonRotationMatrix");
        HeptagonalAntiprismSymmetry hSymm = new HeptagonalAntiprismSymmetry(
                new HeptagonField(), "blue", "heptagonal antiprism", true )
                .createStandardOrbits("blue");
        AntiprismSymmetry aSymm = getAntiprismSymmetry(7);
        for(int i = 0; i < 14; i++) {
            AlgebraicMatrix hMatrix = hSymm.getMatrix(i);
            AlgebraicMatrix aMatrix = aSymm.getMatrix(i);
            assertEquals(hMatrix, aMatrix);
        }
    }

    @Test
    public void testEmbedInR3() {
        // System.out.println("embedInR3");
        final RealVector[] expected = { new RealVector(1, 0, 0), new RealVector(0, 1, 0), new RealVector(0, 0, 1) };
        for (int nSides = PolygonField.MIN_SIDES; nSides <= PolygonFieldApplication.MAX_SIDES; nSides++) {
            if(nSides != 9 && PolygonField.mayBeNonInvertable(nSides)) {
                continue;
            }
            AntiprismSymmetry symm = getAntiprismSymmetry(nSides);
            PolygonField field = symm.getField();
            int dims = 3;
            double lastX = 0.5d;
            double lastY = 0.5d;
            for (int axis = AlgebraicVector.X; axis <= AlgebraicVector.Z; axis++) {
                AlgebraicVector v = field.basisVector(dims, axis);
                RealVector result = symm.embedInR3(v);
                // System.out.println(v + " ---> " + result);
                if (axis == AlgebraicVector.Y && field.isOdd()) {
                    assertNotEquals(expected[axis].x, result.x);
                    assertNotEquals(expected[axis].y, result.y);
                    assertEquals(expected[axis].z, result.z);
                    assertTrue(lastX > result.x);
                    assertTrue(lastY < result.y);
                    lastX = result.x;
                    lastY = result.y;
                } else {
                    assertEquals(expected[axis], result);
                }
            }
        }
    }

    @Test
    public void testHeptagonEmbedInR3() {
        // System.out.println("HeptagonEmbedInR3");
        HeptagonField hField = new HeptagonField();
        HeptagonalAntiprismSymmetry hSymm = new HeptagonalAntiprismSymmetry(hField, "blue",
                "heptagonal antiprism corrected", true).createStandardOrbits("blue");
        AntiprismSymmetry pSymm = getAntiprismSymmetry(7);
        PolygonField pField = pSymm.getField();
        int dims = 3;
        for (int axis = AlgebraicVector.X; axis <= AlgebraicVector.Z; axis++) {
            AlgebraicVector hv = hField.basisVector(dims, axis);
            AlgebraicVector pv = pField.basisVector(dims, axis);
            RealVector hResult = hSymm.embedInR3(hv);
            RealVector pResult = pSymm.embedInR3(pv);
            if (axis == AlgebraicVector.Y) {
                // Math differs in the last decimal position for this one case...
                // but that's close enough since one is calculated with sin() and the other with
                // cos().
                assertEquals(hResult.x, pResult.x, 0.00000000000000006d);
                assertEquals(hResult.y, pResult.y);
                assertEquals(hResult.z, pResult.z);
            } else {
                assertEquals(hResult, pResult);
            }
        }
    }

}
