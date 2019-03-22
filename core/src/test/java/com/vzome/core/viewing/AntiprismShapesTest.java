package com.vzome.core.viewing;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.AlgebraicVectors;
import com.vzome.core.algebra.HeptagonField;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.algebra.SqrtField;
import com.vzome.core.generic.Utilities;
import com.vzome.core.kinds.PolygonFieldApplication;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.Polyhedron.Face;
import com.vzome.core.math.symmetry.AntiprismSymmetry;
import com.vzome.fields.heptagon.HeptagonalAntiprismSymmetry;

/**
 * @author David Hall
 */
public class AntiprismShapesTest {

    private void verifyAntiprism(Polyhedron antiprism, int nSides, boolean print) {
        final boolean isEven = nSides % 2 == 0;
        List<AlgebraicVector> vertexList = antiprism.getVertexList();
        Set<Face> faces = antiprism.getFaceSet();
        assertEquals(nSides * 2, vertexList.size());
        assertEquals((nSides + 1) * 2, faces.size());
        if(print) {
            System.out.println("nSides = " + nSides);
        }
        int polygons = 0;
        int triangles = 0;
        AlgebraicNumber magnitudeSquared = null;
        AlgebraicVector topNormal = null;
        for (Face face : faces) {
            AlgebraicVector normal = computeMidpointNormal(face); // face.getNormal();
            assertFalse(normal.isOrigin());
            int vertexCount = face.size();
            if(print) {
                System.out.println(vertexCount + "-gon normal = " + normal);
                for (int i = 0; i < vertexCount; i++) {
                    int vert = face.getVertex(i);
                    AlgebraicVector vector = vertexList.get(vert);
                    String msg = String.format("%1$8d: %2$8d\t%3$-32s %4$-32s", i, vert,
                            vector.getVectorExpression(AlgebraicField.DEFAULT_FORMAT),
                            vector.getVectorExpression(AlgebraicField.VEF_FORMAT));
                    System.out.println(msg);
                }
            }
            if(vertexCount == 3) {
                triangles++;
                if(isEven) {
                    if(magnitudeSquared == null) {
                        magnitudeSquared = AlgebraicVectors.getMagnitudeSquared(normal);
                    } else {
                        assertEquals(magnitudeSquared, AlgebraicVectors.getMagnitudeSquared(normal));
                    }
                }
            } else if(vertexCount == nSides) {
                polygons++;
                if(topNormal == null) {
                    topNormal = normal;
                } else {
                    assertEquals(topNormal.negate(), normal);
                }
            } else {
                fail("N-gon antiprism should have two N-gon faces and 2*N triangular faces");
            }
        }
        assertEquals("N-gon antiprism should have two N-gon faces", 2, polygons);
        assertEquals("N-gon antiprism should have 2*N triangular faces", 2*nSides, triangles);
    }
    
    private static AlgebraicVector computeMidpointNormal(Face face) {
    	return face.getNormal();
    }

    @Test
    public void testBuildConnectorShape() {
//    	System.out.println("buildConnectorShape " + Utilities.thisSourceCodeLine()
        String name = "polygon antiprism";
        for(int nSides = PolygonField.MIN_SIDES; nSides <= PolygonFieldApplication.MAX_SIDES; nSides++) {
            PolygonField field = new PolygonField(nSides);
            AntiprismSymmetry symm = new AntiprismSymmetry(field, name);
            symm.createStandardOrbits( "blue" );
            AntiprismShapes antiprismShapes = new AntiprismShapes(name, symm);
            Polyhedron antiprism = antiprismShapes.buildConnectorShape(null);
            verifyAntiprism(antiprism, nSides, true);
        }
    }

    @Test
    public void testHeptagonBuildConnectorShape() {
//        System.out.println("heptagonBuildConnectorShape");
        String name = "heptagonal antiprism";
        HeptagonField field = new HeptagonField();
        HeptagonalAntiprismSymmetry symm = new HeptagonalAntiprismSymmetry(field, "blue", name).createStandardOrbits( "blue" );
        final AbstractShapes octahedralShapes = new OctahedralShapes( "octahedral", "triangular antiprism", symm );
    	final AbstractShapes antiprismShapes = new ExportedVEFShapes( null, "heptagon/antiprism", "heptagonal antiprism", symm, octahedralShapes );
        Polyhedron antiprism = antiprismShapes.buildConnectorShape(antiprismShapes.getPackage());

        verifyAntiprism(antiprism, 7, false);
    }

    @Test
    public void printAlgebraicFieldMultiplicationAndDivisionTables() {
        for(int operand = -5; operand <= 25; operand++) {
            AlgebraicField field
                    = operand <  0 ? new SqrtField(-operand)
                    : new PolygonField(operand + 4); // starts at 4
            int order = field.getOrder();
            AlgebraicNumber[] terms = new AlgebraicNumber[order];
            for(int term = 0; term < order; term++) {
                terms[term] = field.getUnitTerm(term);
            }

            String format = "%1$-" + Integer.toString(order * 3) + "s\t";
            System.out.println(field.getName() + " multiplication:");
            for(AlgebraicNumber n : terms) {
                for(AlgebraicNumber d : terms) {
                    System.out.printf(format, n.times(d));
                }
                System.out.println();
            }
            System.out.println();

            System.out.println(field.getName() + " division: (row/column)");
            for(AlgebraicNumber d : terms) {
                System.out.printf(format, d);
            }
            System.out.println();
            for(AlgebraicNumber n : terms) {
                for(AlgebraicNumber d : terms) {
                    System.out.printf(format, n.dividedBy(d));
                }
                System.out.println();
            }
            System.out.println();
        }
    }

}
