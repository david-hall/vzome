package com.vzome.fields.sqrtphi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vzome.api.Tool;
import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command;
import com.vzome.core.commands.CommandAxialSymmetry;
import com.vzome.core.commands.CommandUniformH4Polytope;
import com.vzome.core.editor.ToolsModel;
import com.vzome.core.kinds.AbstractSymmetryPerspective;
import com.vzome.core.kinds.DefaultFieldApplication;
import com.vzome.core.kinds.IcosahedralSymmetryPerspective;
import com.vzome.core.kinds.OctahedralSymmetryPerspective;
import com.vzome.core.math.symmetry.IcosahedralSymmetry;
import com.vzome.core.math.symmetry.OctahedralSymmetry;
import com.vzome.core.math.symmetry.QuaternionicSymmetry;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.math.symmetry.WythoffConstruction.Listener;
import com.vzome.core.tools.AxialSymmetryToolFactory;
import com.vzome.core.tools.LinearMapTool;
import com.vzome.core.tools.MirrorTool;
import com.vzome.core.tools.RotationTool;
import com.vzome.core.tools.ScalingTool;
import com.vzome.core.tools.StereographicProjectionTool;
import com.vzome.core.tools.SymmetryTool;
import com.vzome.core.tools.TranslationTool;
import com.vzome.core.viewing.AbstractShapes;
import com.vzome.core.viewing.ExportedVEFShapes;
import com.vzome.core.viewing.OctahedralShapes;

/**
 * Everything here is stateless, or at worst, a cache (like Shapes).
 * An instance of this can be shared by many DocumentModels.
 * This is why it does not have tool factories, though it does
 * dictate what tool factories will be present.
 * 
 * @author vorth
 *
 */
public class SqrtPhiFieldApplication extends DefaultFieldApplication
{
	public SqrtPhiFieldApplication()
	{
		super( new SqrtPhiField() );
		AlgebraicField field = this .getField();

		OctahedralSymmetryPerspective octahedralPerspective = (OctahedralSymmetryPerspective) super .getDefaultSymmetryPerspective();
		OctahedralSymmetry symm = octahedralPerspective .getSymmetry();
		
		AlgebraicNumber scale = field .createPower( 6 );
		symm .getDirection( "blue" ) .setUnitLength( scale );
		symm .getDirection( "green" ) .setUnitLength( scale );
		symm .getDirection( "yellow" ) .setUnitLength( scale );
        
		AlgebraicNumber x = field .createAlgebraicNumber( new int[]{ 0, -1, 0, 0 } );
		AlgebraicNumber y = field .createAlgebraicNumber( new int[]{ -1, 0, 0, 0 } );
		AlgebraicNumber z = field .zero();
		AlgebraicNumber unitLength = field .createPower( 4 );
		AlgebraicVector norm = new AlgebraicVector( x, y, z );
		symm .createZoneOrbit( "slate", 0, Symmetry .NO_ROTATION, norm, true, false, unitLength );

	    x = field .createAlgebraicNumber( new int[]{ 0, 1, 0, -1 } );
		y = field .one();
		z = field .one();
		norm = new AlgebraicVector( x, y, z );
		symm .createZoneOrbit( "mauve", 0, Symmetry .NO_ROTATION, norm, true, false, unitLength );
		
	    x = field .createAlgebraicNumber( new int[]{ 1, 0, -1, 0 } );
		y = field .createAlgebraicNumber( new int[]{ 0, -1, 0, 0 } );
		z = field .createAlgebraicNumber( new int[]{ 0, -1, 0, 1 } );
		norm = new AlgebraicVector( x, y, z );
		symm .createZoneOrbit( "ivory", 0, Symmetry .NO_ROTATION, norm, true, false, unitLength );
		
		AbstractShapes defaultShapes = new OctahedralShapes( "octahedral", "octahedra", symm );
		octahedralPerspective .setDefaultGeometry( defaultShapes );
	}
	
    private final IcosahedralSymmetryPerspective icosahedralPerspective = new IcosahedralSymmetryPerspective( 
            new IcosahedralSymmetry( getField() ) ) 
    {
        {
            final IcosahedralSymmetry icosaSymm = this.getSymmetry();
            AbstractShapes tinyIcosaShapes = new ExportedVEFShapes( null, "sqrtPhi/tinyIcosahedra", "tiny icosahedra", null, icosaSymm);
            AbstractShapes icosahedralShapes = new ExportedVEFShapes( null, "sqrtPhi/zome", "solid Zome", icosaSymm, tinyIcosaShapes);
            
            // replace the standard icosa shapes generated by the base class with these new ones
            clearShapes();
    
            // this is the order they will be shown on the dialog
            addShapes(icosahedralShapes);
            addShapes(tinyIcosaShapes);
            setDefaultGeometry(tinyIcosaShapes);
        }
    };
    
    private final SymmetryPerspective pentagonalPerspective = new AbstractSymmetryPerspective(
            new PentagonalAntiprismSymmetry( getField(), null ) ) 
    {
        {
            final PentagonalAntiprismSymmetry pentaSymm = getSymmetry();
            pentaSymm.createStandardOrbits( "blue" );
    
            final AbstractShapes octahedralShapes = new OctahedralShapes( "octahedral", "octahedra", pentaSymm );
            final AbstractShapes kostickShapes = new ExportedVEFShapes( null, "sqrtPhi/fivefold", "Kostick", pentaSymm, octahedralShapes );
            
            // this is the order they will be shown on the dialog
            addShapes(kostickShapes);
            addShapes(octahedralShapes);
            setDefaultGeometry(kostickShapes);

            axialsymm = new CommandAxialSymmetry( pentaSymm );
        }

        @Override
		public PentagonalAntiprismSymmetry getSymmetry()
		{
			return (PentagonalAntiprismSymmetry) super.getSymmetry();
		}
		
		@Override
		public List<Tool.Factory> createToolFactories( Tool.Kind kind, ToolsModel tools )
		{
			List<Tool.Factory> result = new ArrayList<>();
			final PentagonalAntiprismSymmetry pentaSymm = getSymmetry();
			switch ( kind ) {

			case SYMMETRY:
				result .add( new SymmetryTool.Factory( tools, pentaSymm ) );
				result .add( new MirrorTool.Factory( tools ) );
				result .add( new AxialSymmetryToolFactory( tools, pentaSymm ) );
				break;

			case TRANSFORM:
				result .add( new ScalingTool.Factory( tools, pentaSymm ) );
				result .add( new RotationTool.Factory( tools, pentaSymm ) );
				result .add( new TranslationTool.Factory( tools ) );
				result .add( new StereographicProjectionTool.Factory( tools ) );
				break;

			case LINEAR_MAP:
				result .add( new LinearMapTool.Factory( tools, pentaSymm, false ) );
				break;

			default:
				break;
			}
			return result;
		}

		@Override
		public List<Tool> predefineTools( Tool.Kind kind, ToolsModel tools )
		{
			List<Tool> result = new ArrayList<>();
			final PentagonalAntiprismSymmetry pentaSymm = getSymmetry();
			switch ( kind ) {

			case SYMMETRY:
				result .add( new SymmetryTool.Factory( tools, pentaSymm ) .createPredefinedTool( "pentagonal antiprism around origin" ) );
				result .add( new AxialSymmetryToolFactory( tools, pentaSymm ) .createPredefinedTool( "fivefold symmetry through origin" ) );
				result .add( new MirrorTool.Factory( tools ) .createPredefinedTool( "reflection through red plane" ) );
				break;

			case TRANSFORM:
				result .add( new ScalingTool.Factory( tools, pentaSymm ) .createPredefinedTool( "scale down" ) );
				result .add( new ScalingTool.Factory( tools, pentaSymm ) .createPredefinedTool( "scale up" ) );
				result .add( new RotationTool.Factory( tools,pentaSymm ) .createPredefinedTool( "fivefold rotation through origin" ) );
				break;

			default:
				break;
			}
			return result;
		}

        private final Command axialsymm;

		@Override
		public Command getLegacyCommand( String action )
		{
            switch (action) {
            case "axialsymm":
                return axialsymm;

            default:
                return super.getLegacyCommand(action);
            }
		}

		@Override
		public String getModelResourcePath()
		{
			return "org/vorthmann/zome/app/pentagonal.vZome";
		}
	};
    
    private final QuaternionicSymmetry H4 = new QuaternionicSymmetry( "H_4", "com/vzome/core/math/symmetry/H4roots.vef", getField() );

	@Override
	public Collection<SymmetryPerspective> getSymmetryPerspectives()
	{
		return Arrays.asList( this .pentagonalPerspective, super .getDefaultSymmetryPerspective(), this .icosahedralPerspective );
	}

	@Override
	public SymmetryPerspective getDefaultSymmetryPerspective()
	{
		return this .pentagonalPerspective;
	}

	@Override
	public SymmetryPerspective getSymmetryPerspective( String symmName )
	{
		switch ( symmName ) {

		case "pentagonal":
			return this .pentagonalPerspective;

		case "icosahedral":
			return this .icosahedralPerspective;

		default:
			return super .getSymmetryPerspective( symmName );
		}
	}

	@Override
	public QuaternionicSymmetry getQuaternionSymmetry( String name )
	{
		switch ( name ) {

		case "H_4":
			return this .H4;

		default:
			return null;
		}
	}

    private CommandUniformH4Polytope h4Builder = null;
    
	@Override
	public void constructPolytope( String groupName, int index, int edgesToRender, AlgebraicNumber[] edgeScales, Listener listener )
	{
		switch ( groupName ) {

		case "H4":
			if ( this .h4Builder == null ) {
	            QuaternionicSymmetry qsymm = new QuaternionicSymmetry( "H_4", "com/vzome/core/math/symmetry/H4roots.vef", this .getField() );
	            this .h4Builder = new CommandUniformH4Polytope( this .getField(), qsymm, 0 );
			}
			this .h4Builder .generate( index, edgesToRender, edgeScales, listener );
            break;

		default:
			super .constructPolytope( groupName, index, edgesToRender, edgeScales, listener );
			break;
		}
	}
}
