package com.vzome.core.edits;

import static com.vzome.xml.DomUtils.addAttribute;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzome.core.commands.Command;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.construction.Color;
import com.vzome.core.editor.api.ChangeManifestations;
import com.vzome.core.editor.api.EditorModel;
import com.vzome.core.editor.api.OrbitSource;
import com.vzome.core.editor.api.SideEffect;
import com.vzome.core.editor.api.SymmetryAware;
import com.vzome.core.edits.ManifestationColorMappers.ManifestationColorMapper;
import com.vzome.core.model.Manifestation;

/**
 * @author David Hall
 */
public class MapToColor extends ChangeManifestations {

    private ManifestationColorMapper colorMapper;
    private final EditorModel editor;

    public MapToColor( EditorModel editor )
    {
        super( editor );
        this.editor = editor;
    }
    
    @Override
    public void configure( Map<String,Object> props ) 
    {
        String colorMapperName = (String) props .get( "mode" );
        OrbitSource symmetry = ((SymmetryAware) this.editor) .getSymmetrySystem();
        if ( colorMapperName != null )
            this .colorMapper = ManifestationColorMappers .getColorMapper( colorMapperName, symmetry );
    }

    /**
     * Either configure() or setXmlAttributes() is always called before perform()
     */
    @Override
	public void perform() throws Failure
    {
        if( colorMapper .requiresOrderedSelection() ) {
            setOrderedSelection( true );
        }
        colorMapper.initialize( getRenderedSelection() );
        for( Manifestation man : getRenderedSelection() ) {
            plan( new ColorMapManifestation( man, colorMapper.apply(man) ) );
            unselect( man, true );
        }
        redo();
    }

    private static final String COLORMAPPER_ATTR_NAME = "colorMapper";

    @Override
    public void getXmlAttributes( Element result )
    {
        result .setAttribute( COLORMAPPER_ATTR_NAME, colorMapper.getName() );
        colorMapper.getXmlAttributes(result);
    }

    @Override
    public void setXmlAttributes( Element xml, XmlSaveFormat format ) throws Command.Failure
    {
        OrbitSource symmetry = ((SymmetryAware) this .editor) .getSymmetrySystem( xml .getAttribute( "symmetry" ) );
        String colorMapperName = xml .getAttribute( COLORMAPPER_ATTR_NAME );
        this .colorMapper = ManifestationColorMappers .getColorMapper( colorMapperName, symmetry );

        // Disabling this warning because colorMapper.getName() fails in Javascript, due to JSweet transpiling limitations
//        if( !colorMapper.getName().equals(colorMapperName) ) {
//            logger.warning("Substituting " + colorMapper.getName() + " for specifed " + COLORMAPPER_ATTR_NAME + ": " + colorMapperName);
//        }
        colorMapper.setXmlAttributes(xml);
    }

    @Override
    protected String getXmlElementName() {
        return "MapToColor";
    }

    private class ColorMapManifestation implements SideEffect
    {
        private final Manifestation mManifestation;

        private final Color oldColor, newColor;

        public ColorMapManifestation( Manifestation manifestation, Color color )
        {
            mManifestation = manifestation;
            this .newColor = color;
            	oldColor = manifestation .getColor();
        }

        @Override
        public void redo()
        {
            mManifestations .setColor( mManifestation, newColor );
        }

        @Override
        public void undo()
        {
            mManifestations .setColor( mManifestation, oldColor );
        }

        @Override
        public Element getXml( Document doc )
        {
            Element result = doc .createElement( "color" );
            addAttribute( result, "rgb", newColor .toString() );
            Element man = mManifestation .getXml( doc );
            result .appendChild( man );
            return result;
        }
    }

}
