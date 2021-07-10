

package com.vzome.core.commands;


import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.construction.ConstructionChanges;
import com.vzome.core.construction.ConstructionList;
import com.vzome.core.construction.FreePoint;
import com.vzome.core.construction.Point;

/**
 * @author Scott Vorthmann
 */
public class CommandFreePoint extends AbstractCommand
{
    private static Object[][] PARAMS = new Object[][]{};
        
    @Override
    public Object[][] getParameterSignature()
    {
        return PARAMS;
    }

    @Override
    public Object[][] getAttributeSignature()
    {
        return null;
    }

    @Override
    public ConstructionList apply( ConstructionList parameters, AttributeMap attributes,
            ConstructionChanges effects ) throws Failure
    {
        ConstructionList result = new ConstructionList();
        AlgebraicVector loc = (AlgebraicVector) attributes .get( "where" );
        
        Point pt2 = new FreePoint( loc );
        effects .constructionAdded( pt2 );
        result .addConstruction( pt2 );
    
        return result;
    }

}
