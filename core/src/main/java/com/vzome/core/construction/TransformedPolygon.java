

package com.vzome.core.construction;

import com.vzome.core.algebra.AlgebraicVector;


/**
 * @author Scott Vorthmann
 */
public class TransformedPolygon extends Polygon
{
    private final Transformation mTransform;
    
    private final Polygon mPrototype;

    public TransformedPolygon( Transformation transform, Polygon prototype )
    {
        super( prototype .field );
        mTransform = transform;
        mPrototype = prototype;
        mapParamsToState();
    }

    @Override
    protected final boolean mapParamsToState()
    {
        // TODO implement impossibility
//      if ( mStart .isImpossible() || mEnd .isImpossible() )
//          return setStateVariables( null, null, true );
        AlgebraicVector [] protoLocs = mPrototype .getVertices();
        AlgebraicVector [] locs = new AlgebraicVector[ protoLocs .length ];
        // TODO: Fix calculation of normal to a panel so the first three verticees can be collinear
        // or else this may fail to reverse the panel when the last three vertices of the original are collinear
        int j = locs.length - 1;
        boolean reverseVertexOrder = !mTransform .preservesChirality();
        for ( int i = 0; i < locs .length; i++ ) {
            locs[ reverseVertexOrder ? j : i ] = mTransform .transform( protoLocs[ i ] );
            j--;
        }
        return setStateVariable( locs, false );
    }
}
