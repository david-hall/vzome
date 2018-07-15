package com.vzome.core.algebra;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * A collection of static helper methods for the AlgebraicVector class
 */
public class AlgebraicVectors {
    private AlgebraicVectors() {}

    public static AlgebraicVector getNormal(final AlgebraicVector v0, final AlgebraicVector v1, final AlgebraicVector v2) {
        return v1.minus(v0).cross(v2.minus(v0));
    }
    
    /**
     * 
     * @return a 3D normal vector based on all vertices. 
     * The absolute value of the vector's length is the area of the polygon.
     * The sign of the vector indicates the the polygon's winding direction,
     * with a negative value indicating a clockwise winding direction. 
     * A self intersecting polygon will have the sign of the area with the larger winding direction.
     * 
     * The Newell algorithm works with convex as well as non-convex polygons
     * including those with self-intersections or consecutive collinear vertices.
     * 
     * If the vertices are not coplanar (which should never happen with a polygon or panel),
     * then the algorithm implicitly returns a normal vector to a "best-fit" plane. 
     * The sign and length of the vector is then based on the projection of the vertices onto that plane.
     * No special test or code is required to handle this condition.
     * It is a natural behavior of the algorithm.   
     * 
     * See https://blog.element84.com/polygon-winding-post.html
     *  and http://geomalgorithms.com/a01-_area.html#3D%20Polygons
     */
    public static AlgebraicVector get3DNormal(final List<AlgebraicVector> vertices)
    {
        if(vertices.size() < 3) {
            throw new IllegalArgumentException("3 vertices are required to calculate a normal. Found " + vertices.size());
        }
        AlgebraicNumber xNormal = null;
        AlgebraicNumber yNormal = null;
        AlgebraicNumber zNormal = null;
        AlgebraicNumber x0 = null;
        AlgebraicNumber y0 = null;
        AlgebraicNumber z0 = null;
        AlgebraicNumber xBeg = null;
        AlgebraicNumber yBeg = null;
        AlgebraicNumber zBeg = null;
        
        for(AlgebraicVector vertex : vertices) {
            vertex = vertex.projectTo3d(true); // just to be safe
            // get the 3 individual coordinates
            AlgebraicNumber xEnd = vertex.getComponent( AlgebraicVector.X );
            AlgebraicNumber yEnd = vertex.getComponent( AlgebraicVector.Y );
            AlgebraicNumber zEnd = vertex.getComponent( AlgebraicVector.Z );

            if(xNormal == null) {
                // first time around so initialize the accumulators
                xNormal = yNormal = zNormal = vertex.getField().zero();
                // save these first coordinates for use after we finish the loop
                x0 = xEnd;
                y0 = yEnd;
                z0 = zEnd;
            } else {
                // accumulate the projections of the current edge onto each axis
                xNormal = xNormal .plus( yBeg.minus(yEnd) .times ( zBeg.plus(zEnd ) ) );
                yNormal = yNormal .plus( zBeg.minus(zEnd) .times ( xBeg.plus(xEnd ) ) );
                zNormal = zNormal .plus( xBeg.minus(xEnd) .times ( yBeg.plus(yEnd ) ) );                
            }
            
            // prep for next iteration
            xBeg = xEnd;
            yBeg = yEnd;
            zBeg = zEnd;
        }

        // don't forget the final edge which is between the last and first vertex
        xNormal = xNormal .plus( yBeg.minus(y0) .times ( zBeg.plus(z0 ) ) );
        yNormal = yNormal .plus( zBeg.minus(z0) .times ( xBeg.plus(x0 ) ) );
        zNormal = zNormal .plus( xBeg.minus(x0) .times ( yBeg.plus(y0 ) ) );                
        
        // scaling by 1/2 results in the absolute value of the length equaling the polygon's area
        return new AlgebraicVector(xNormal, yNormal, zNormal).scale( xNormal.getField().createRational(1, 2));
    }

    public static boolean areCollinear(final AlgebraicVector v0, final AlgebraicVector v1, final AlgebraicVector v2) {
        return getNormal(v0, v1, v2).isOrigin();
    }
    
    public static AlgebraicVector getLinePlaneIntersection( AlgebraicVector lineStart, AlgebraicVector lineDirection,
                                                            AlgebraicVector planeCenter, AlgebraicVector planeNormal )
    {
        AlgebraicNumber denom = planeNormal .dot( lineDirection );
        if ( denom .isZero() )
            return null;

        AlgebraicVector p1p3 = planeCenter .minus( lineStart );
        AlgebraicNumber numerator = planeNormal .dot( p1p3 );
        AlgebraicNumber u = numerator .dividedBy( denom );
        return lineStart .plus( lineDirection .scale( u ) );
    }

    public static AlgebraicVector calculateCentroid(Collection<AlgebraicVector> vectors) {
        return getCentroid(vectors.toArray(new AlgebraicVector[vectors.size()]));
    }

    public static AlgebraicVector getCentroid(AlgebraicVector[] vectors) {
        // Assert that vectors is neither null nor empty.
        AlgebraicField field = vectors[0].getField();
        // Start with 0 as when calculating the average of any set of numbers...
        AlgebraicVector sum = new AlgebraicVector(field, vectors[0].dimension()); // preinitialized to 0
        for (AlgebraicVector vector : vectors) {
            // add them all together
            sum = sum.plus(vector);
        }
        // then divide by the number of items we added (to divide is to scale by the reciprocal)
        return sum.scale(field.createRational(1, vectors.length));
    }

    public static AlgebraicNumber getMagnitudeSquared(AlgebraicVector v) {
        return v.dot(v);
    }

    /**
     * @param vector
     * @return the greater of {@code vector} and its inverse. 
     * The comparison is based on a canonical (not mathematical) comparison as implemented in {@code AlgebraicVector.compareTo()}. 
     * There is no reasonable mathematical sense of ordering vectors, 
     * but this provides a way to map a vector and its inverse to a common vector for such purposes as sorting and color mapping.    
     */
    public static AlgebraicVector getCanonicalOrientation( AlgebraicVector vector ) {
        AlgebraicVector negate = vector.negate();
        return vector.compareTo(negate) > 0 ? vector : negate;
    }

    /**
     * getMostDistantFromOrigin() is is used by a few ColorMapper classes, but I think it can eventually be useful elsewhere as well, for example, a zoom-to-fit command or in deriving a convex hull. I've made it a static method of the AlgebraicVector class to encourage such reuse.
     *
     * @param vectors A collection of vectors to be evaluated.
     * @return A canonically sorted subset (maybe all) of the {@code vectors} collection. All of the returned vectors will be the same distance from the origin. That distance will be the maximum distance from the origin of any of the vectors in the original collection. If the original collection contains only the origin then so will the result.
     */
    public static TreeSet<AlgebraicVector> getMostDistantFromOrigin(Collection<AlgebraicVector> vectors) {
        TreeSet<AlgebraicVector> mostDistant = new TreeSet<>();
        double maxDistanceSquared = 0D;
        for (AlgebraicVector vector : vectors) {
            double magnitudeSquared = AlgebraicVectors.getMagnitudeSquared(vector).evaluate();
            if (magnitudeSquared >= maxDistanceSquared) {
                if (magnitudeSquared > maxDistanceSquared) {
                    mostDistant.clear();
                }
                maxDistanceSquared = magnitudeSquared;
                mostDistant.add(vector);
            }
        }
        return mostDistant;
    }

}
