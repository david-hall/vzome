
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package com.vzome.core.editor;

import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.construction.FreePoint;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.Segment;
import com.vzome.core.construction.SegmentJoiningPoints;
import com.vzome.core.model.Panel;
import com.vzome.core.model.RealizedModel;


public class ShowNormals extends ChangeManifestations
{
    public static final String NAME = "ShowNormals";

    private AlgebraicNumber scale;
    
    @Override
	public void perform() throws Failure
    {
        unselectConnectors();
        unselectStruts();
        
        for ( Panel panel : Manifestations .getPanels( mSelection ) )
        {
            unselect( panel );
            // The length of the winding normal equals the area of the polygon.
            // For example, if the polygon is a 1x1 square, the winding normal will be of length 1 * 1 = 1.
            // Scaling the polygon vertices by a factor of 2 will increase the area 
            // (and thus the length of the winding normal vector), by a factor of 2 * 2 = 4.
            // Since the normal vector grows exponentially, not linearly, 
            // It doesn't really make sense to apply a fixed scaling to it,
            // since that scaling only "looks good" in typical sized models.
            // TODO: Either se the normal without any scaling, or work out some way to scale it 
            // so its length is closer to the avaerage edge length.
            // In the mean time, just use the default hard coded scale for backwards compatability 
            AlgebraicVector cp = panel.getWindingNormal3D() .scale( scale );
            AlgebraicVector centroid = panel .getCentroid();
            AlgebraicVector tip = centroid .plus( cp );
            Point p1 = new FreePoint( centroid );
            select( manifestConstruction( p1 ) );
            Point p2 = new FreePoint( tip );
            select( manifestConstruction( p2 ) );
            Segment s = new SegmentJoiningPoints( p1, p2 );
            select( manifestConstruction( s ) );
        }
        redo();
    }
    
    public ShowNormals( Selection selection, RealizedModel realized )
    {
        this( selection, realized, null );
    }

    public ShowNormals( Selection selection, RealizedModel realized, AlgebraicNumber scale)
    {
        super( selection, realized, false );
        this.scale = scale == null || scale.isZero() ? 
                // scale down 3 powers, and halve, just for backward compatibility
                this .mManifestations .getField().createAlgebraicNumber( 1, 0, 2, -3 )
                : scale;
    }

    @Override
    protected String getXmlElementName()
    {
        return NAME;
    }

    private static final String SCALE_ATTR = "scale";

    @Override
    protected void getXmlAttributes( Element element )
    {
        if ( scale != null ) {
            XmlSaveFormat .serializeNumber(element, SCALE_ATTR, scale );
        }
    }

    @Override
    protected void setXmlAttributes( Element xml, XmlSaveFormat format )
         throws Failure
    {
        AlgebraicNumber xmlScale = format .parseNumber(xml, SCALE_ATTR);
        if(xmlScale != null) {
            scale = xmlScale;
        }
    }

}
