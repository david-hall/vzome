package com.vzome.core.construction;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;

public class SegmentProportionalDivision extends Transformation {

    private final AlgebraicNumber startScalar;
    private final AlgebraicNumber offsetScalar;
    
    public SegmentProportionalDivision(AlgebraicNumber startScalar, AlgebraicNumber offsetScalar) {
        super(startScalar.getField());
        this.startScalar = startScalar;
        this.offsetScalar = offsetScalar;
    }

    @Override
    public Construction transform(Construction c) {
        if (c instanceof Segment) {
            Segment in = (Segment) c;
            AlgebraicVector offset = in.getOffset();
            AlgebraicVector start = in.getStart().plus(offset.scale(startScalar));
            Point startPoint = new FreePoint(start);
            if(offsetScalar == null ) {
                return startPoint;
            } else {
                Point endPoint = new FreePoint(start.plus(offset.scale(offsetScalar)));
                return new SegmentJoiningPoints(startPoint, endPoint);
            }
        }
        return null;
    }
    
    @Override
    protected boolean mapParamsToState() {
        // TODO Auto-generated method stub
        return false;
    }

}
