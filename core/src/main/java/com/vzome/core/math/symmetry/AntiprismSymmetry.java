package com.vzome.core.math.symmetry;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.vzome.core.algebra.AlgebraicMatrix;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.math.RealVector;

/**
 * @author David Hall
 * This class is a generalized implementation of the HeptagonalAntiprismSymmetry by Scott Vorthmann
 */
public class AntiprismSymmetry extends AbstractSymmetry {

    private final String name;
    private Axis preferredAxis;
    private final boolean useShear;
    private final Matrix3d shearTransform;

    public AntiprismSymmetry(PolygonField field, String defaultStyle) {
        super(field.polygonSides() * 2, field, "blue", defaultStyle,
                field.isEven() ? null   // reflection through origin yields negative zones for even-gons
                : new AlgebraicMatrix(  // reflection in Z (red) will yield the negative zones for odd-gons
                        field.basisVector(3, AlgebraicVector.X), 
                        field.basisVector(3, AlgebraicVector.Y),
                        field.basisVector(3, AlgebraicVector.Z).negate())
                );
        final int nSides = field.polygonSides();
        name = "antiprism";
        double m10 = 0;
        double m11 = 1;
        useShear = field.isOdd();
        if (useShear) {
            m10 = field.getUnitTerm(field.getOrder() - 1).reciprocal().evaluate() / 2.0d;
            m11 = Math.cos(Math.PI / (2.0d * nSides));
        }

        // Shear transformation will only be applied in the Y direction. X and Z will be unchanged.
        shearTransform = new Matrix3d(1, m10, 0, 0, m11, 0, 0, 0, 1);
    }

    @Override
    public PolygonField getField() {
        // This cast to PolygonField is safe because we require a PolygonField in our c'tor
        return (PolygonField) super.getField();
    }

    /**
     * Called by the super constructor.
     */
    @Override
    protected void createInitialPermutations() {
        final int sides = getField().polygonSides();
        mOrientations[0] = new Permutation(this, null);

        // first, define the N-fold rotation
        // for example, when sides == 7, then map looks like this...
        // { 1, 2,  3,  4,  5,  6, 0,
        //   8, 9, 10, 11, 12, 13, 7 };
        int[] map1 = new int[sides * 2];
        for (int i = 0; i < sides; i++) {
            map1[i] = (i + 1) % sides;
            map1[i + sides] = map1[i] + sides;
        }
        mOrientations[1] = new Permutation(this, map1);

        // then, then 2-fold rotation
        // be sure to use a new array, don't just reorder the one used by mOrientations[1] above
        // for example, when sides == 7, then map2 looks like this...
        // { 7, 13, 12, 11, 10, 9, 8,
        //   0,  6,  5,  4,  3, 2, 1 };
        // map2 is just map1 in reverse order
        int[] map2 = new int[map1.length];
        int n = sides * 2;
        for (int i = 0; i < map2.length; i++) {
            n--;
            map2[i] = map1[n];
        }
        mOrientations[sides] = new Permutation(this, map2);
    }

    @Override
    protected void createFrameOrbit(String frameColor) {
        // The following drawing uses the 7-gon as an example of the general case for an odd-gon.
        //
        //                                                  Y
        //                                               (0,1)
        //                       +---+------ [2] ------- [f] ---------+--------+---+
        //                      /   /        /           /           /        /   /
        //                     +- [g] ------+-----------+--------- [1] ------+---+
        //                    /   /        /           /           /        /   /
        //                   /   /        /           /           /        /   /
        //                  /   /        /           /           /        /   /
        //         (-1,s) [3] -+--------+-----------+-----------+------ [e] -+
        //                /   /        /           /           /        /   /
        //               /   /        /           /           /        /   /
        //              /   /        /           /           /        /   /
        //             /   /        /           /           /        /   /
        //           [a] -+--------+---------- 0 ----------+--------+- [0] (1,0) X
        //           /   /        /           /           /        /   /
        //          /   /        /           /<--- s --->/        /   /
        //         /   /        /           /<---- 1 --------------->/
        //        /   /        /           /           /        /   /
        //       +- [4| ------+-----------+-----------+--------+- [d]
        //      /   /        /           /           /        /   /
        //     /   /        /           /           /        /   /
        //    /   /        /           /           /        /   /
        //   +---+------ [b] ---------+-----------+------ [6] -+
        //  /   /        /           /           /        /   /
        // +---+--------+--------- [5] ------- [c] ------+---+
        //                       (0,-1)
        //
        //
        // The rotation will map v[0] to v[1]. v[0].x = 1 and v[0].y = 0, so this is the X unit vector.
        // The rotation will map v[f] to v[g]. v[f].x = 0 and v[f].y = 1, so this is the Y unit vector.
        // Components of v[1] and v[g] can therefore be used directly to generate the rotation matrix.

        final PolygonField field = getField();
        final int nSides = field.polygonSides();
        final int order = field.getOrder();
        final AlgebraicMatrix rotationMatrix = getRotationMatrix();
        // rvNeg will be indexed with the modulus operator (%) to calculate the second half of mMatrices
        final AlgebraicVector[] rvNeg = new AlgebraicVector[nSides];
        {
            final AlgebraicNumber num = field.getUnitTerm(order - 2); //(field.isEven() ? 3 : 2)); // TODO: Is this correct when field.isEven()?
            final AlgebraicNumber den = field.getUnitTerm(order - 1);
            AlgebraicVector vNeg = field.origin(3)
                    .setComponent(AlgebraicVector.X, num.dividedBy(den))
                    .setComponent(AlgebraicVector.Y, den.reciprocal());
            for (int i = 0; i < nSides; i++) {
                rvNeg[i] = vNeg;
                vNeg = rotationMatrix.timesColumn(vNeg);
            }
        }

        // All mMatrices are mappings of the 3D standard basis unit vectors.
        // e.g. [X,Y,Z] = field .identityMatrix( 3 );
        final AlgebraicVector zAxis = field.basisVector(3, AlgebraicVector.Z);
        final AlgebraicVector zNeg = zAxis.negate();
        AlgebraicVector vx = field.basisVector(3, AlgebraicVector.X);
        AlgebraicVector vy = field.basisVector(3, AlgebraicVector.Y);
        for (int iLo = 0; iLo < nSides; iLo++) {
            // Low  half of mMatrices rotate around the Z axis starting from the X axis
            // High half of mMatrices rotate around the Z axis starting from the second to last rotation in rvn
            // I think that any vector in rvn could have been the starting point, 
            // but using the second from the last makes it identical to the original HeptagonalAntiprismSymmetry  
            int iHi = iLo + nSides;
            int n = (iHi - 2) % nSides;
            mMatrices[iLo] = new AlgebraicMatrix(vx, vy,      zAxis);
            mMatrices[iHi] = new AlgebraicMatrix(vx, rvNeg[n], zNeg);
            vx = rotationMatrix.timesColumn(vx);
            vy = rotationMatrix.timesColumn(vy);
        }
    }

    public AlgebraicMatrix getRotationMatrix() {
        final PolygonField field = getField();
        final int order = field.getOrder();
        // getUnitTerm() returns zero when its argument is negative 
        final AlgebraicNumber p_x = field.getUnitTerm(order - 3);
        final AlgebraicNumber q_y = field.getUnitTerm(order - (field.isEven() ? 3 : 2));
        final AlgebraicNumber den = field.getUnitTerm(order - 1);
        final AlgebraicNumber num = field.getUnitTerm(1);

        final AlgebraicVector p = field.origin(3)
                .setComponent(AlgebraicVector.X, p_x.dividedBy(den))
                .setComponent(AlgebraicVector.Y, num.dividedBy(den));
        final AlgebraicVector q = field.origin(3)
                .setComponent(AlgebraicVector.X, num.dividedBy(den).negate())
                .setComponent(AlgebraicVector.Y, q_y.dividedBy(den));
        final AlgebraicVector zAxis = field.basisVector(3, AlgebraicVector.Z);

        return new AlgebraicMatrix(p, q, zAxis);
    }

    @Override
    protected void createOtherOrbits() {
        // Breaking the bad pattern of orbit initialization in the AbstractSymmetry constructor
    }

    public AntiprismSymmetry createStandardOrbits(String frameColor) {
        Direction redOrbit = createZoneOrbit("red", 0, 1, this.mField.basisVector(3, AlgebraicVector.Z), true);
        redOrbit.setDotLocation(1d, 0d);
        this.preferredAxis = redOrbit.getAxis(Symmetry.PLUS, 0);

        Direction blueOrbit = createZoneOrbit(frameColor, 0, getField().polygonSides(),
                this.mField.basisVector(3, AlgebraicVector.X), true);
        blueOrbit.setDotLocation(0d, 1d);

        return this;
    }

    @Override
    public Axis getPreferredAxis() {
        return this.preferredAxis;
    }

    @Override
    public RealVector embedInR3(AlgebraicVector v) {
        RealVector rv = super.embedInR3(v);
        if (useShear) {
            Vector3d v3d = new Vector3d(rv.x, rv.y, rv.z);
            shearTransform.transform(v3d);
            return new RealVector(v3d.x, v3d.y, v3d.z);
        }
        return rv;
    }

    @Override
    public boolean isTrivial() {
        return false; // signals the POV-Ray exporter to generate the tranform
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int[] subgroup(String name) {
        return null; // TODO
    }

    @Override
    public Direction getSpecialOrbit(Symmetry.SpecialOrbit which) {
        switch (which) {

        case BLUE:
            return this.getDirection("blue");

        case RED:
            return this.getDirection("red");

        case YELLOW:
            return this.getDirection("blue");

        default:
            return null;
        }
    }
}
