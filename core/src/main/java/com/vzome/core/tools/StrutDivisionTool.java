package com.vzome.core.tools;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.XmlSaveFormat;
import com.vzome.core.construction.Color;
import com.vzome.core.construction.Construction;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.SegmentProportionalDivision;
import com.vzome.core.construction.Transformation;
import com.vzome.core.editor.AbstractToolFactory;
import com.vzome.core.editor.Tool;
import com.vzome.core.editor.ToolsModel;
import com.vzome.core.editor.api.ChangeManifestations;
import com.vzome.core.editor.api.Selection;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Strut;

public class StrutDivisionTool extends TransformationTool {
    private static final int MAX_DIVISIONS = 10; // arbitrary but reasonable
    private static final String CATEGORY = "strut division";
    private static final String LABEL = "Create a strut division tool";
    private static final String TOOLTIP = "<p>" +
            "Each tool will divide selected struts into the specified proportions. <br>" +
            "<br>" +
            "To create a tool, select from 2 to " + MAX_DIVISIONS + " struts in the same orbit (color). <br>" +
            "They define the proportions of the new tool.  The proportions will be the length  <br>" +
            "of each strut to the total of all their lengths.  <br>" +
            "Selection order matters.  The tool will divide all selected struts  <br>" +
            "so that the new struts are sized in the same proportion as the struts  <br>" +
            "used to generate the tool.  The divisions respect strut orientation. <br>" +
        "</p>";

    public static class Factory extends AbstractToolFactory
    {
        public Factory( ToolsModel tools, Symmetry symmetry )
        {
            super( tools, symmetry, CATEGORY, LABEL, TOOLTIP );
        }

        @Override
        protected boolean countsAreValid( int total, int balls, int struts, int panels )
        {
            return ( struts >= 2 && struts <= MAX_DIVISIONS );
        }

        @Override
        public Tool createToolInternal( String id )
        {
            AlgebraicField field = this .getToolsModel() .getEditorModel() .getRealizedModel() .getField();
            
            List<AlgebraicNumber> proportions = null;
            
            final String prefix = CATEGORY + ".builtin/";
            final String recip = "1/"; // or "\u00B9/"; // superscripted "1/"
            String overlayTxt = null;
            if(id.startsWith(prefix)) {
                List<AlgebraicNumber> lengths = new ArrayList<>(MAX_DIVISIONS);
                proportions = new ArrayList<>(MAX_DIVISIONS);
                AlgebraicNumber n = null;
                final String s = id.substring(prefix.length()); 
                switch (s) {
                case "halves":
                    overlayTxt = recip + "2"; // 1/2
                    n = field.createRational(1, 2);
                    proportions.add(n);
                    proportions.add(n);
                    break;
    
                case "thirds":
                    overlayTxt = recip + "3"; // 1/3
                    n = field.createRational(1, 3);
                    proportions.add(n);
                    proportions.add(n);
                    proportions.add(n);
                    break;

                // no need for "fourths", just apply halves twice
                // in fact, we only need prime or irrational divisors for any built-ins

                case "fifths":
                    overlayTxt = recip + "5"; // 1/5
                    n = field.createRational(1, 5);
                    proportions.add(n);
                    proportions.add(n);
                    proportions.add(n);
                    proportions.add(n);
                    proportions.add(n);
                    break;

                case "phi":
                    overlayTxt = "\u03c6"; // phi 
                    lengths.add(getGoldenRatio(field, id)); // phi is first
                    lengths.add(field.one());
                    proportions = getProportionsFromLengths(lengths);
                    break;
    
                case "1/phi":
                    overlayTxt = recip + "\u03c6"; // 1/phi
                    lengths.add(field.one());
                    lengths.add(getGoldenRatio(field, id)); // phi is last
                    proportions = getProportionsFromLengths(lengths);
                    break;
    
                case "increasing size":
                    overlayTxt = "inc";
                    for(int i = 0; i <= field.getNumMultipliers(); i++) {
                        lengths.add(field.getUnitTerm(i));
                    }
                    proportions = getProportionsFromLengths(lengths);
                    break;
    
                case "decreasing size":
                    overlayTxt = "dec";
                    for(int i = field.getNumMultipliers(); i >= 0; i--) {
                        lengths.add(field.getUnitTerm(i));
                    }
                    proportions = getProportionsFromLengths(lengths);
                    break;
    
                default:
                    String[] parts = s.split("/");
                    if(parts.length == 2) {
                        if("#".equals(parts[0])) {
                            n = field.getUnitTerm(Integer.parseInt(parts[1]));
                            lengths.add(n);
                            lengths.add(field.one());
                            overlayTxt = n.toString();
                            proportions = getProportionsFromLengths(lengths);
                            break;
                        } else if("#".equals(parts[1])) {
                            n = field.getUnitTerm(Integer.parseInt(parts[0]));
                            // add them to the list in the opposite order
                            lengths.add(field.one());
                            lengths.add(n);
                            overlayTxt = recip + n.toString();                            
                            proportions = getProportionsFromLengths(lengths);
                            break;
                        }
                    }
                    throw new IllegalStateException("Unknown id: " + id);
                }
//                System.out.println("overlayTxt = " + overlayTxt + "\t\t" + s);
            } else {
                proportions = calculateStrutProportions(this .getToolsModel() .getEditorModel() .getSelection());
            }
            
            return new StrutDivisionTool( id, getToolsModel(), proportions, overlayTxt);
        }
        
        private static AlgebraicNumber getGoldenRatio(AlgebraicField field, String id) {
            AlgebraicNumber goldenRatio = field.getGoldenRatio();
            if(goldenRatio == null) {
                String msg = field.getName() + " should not create a '" + id + 
                        "' tool unless getGoldenRatio() is not null." ;
                throw new IllegalStateException(msg);
            }
            return goldenRatio;
        }
        
        private List<AlgebraicNumber> calculateStrutProportions(Selection selection) {
            Symmetry symmetry = getSymmetry();
            if(symmetry == null) {
                // loading from file... we'll have to wait for xml to deserialize proportions
                return null;
            }
            List<AlgebraicNumber> lengths = new ArrayList<>(MAX_DIVISIONS);
            Direction orbit1 = null;
            for (Manifestation man : selection) {
                if(man instanceof Strut) {
                    AlgebraicVector offset = ((Strut) man).getOffset();
                    Axis zone = symmetry .getAxis( offset );
                    if ( zone == null ) {
                        return null;
                    }
                    if ( orbit1 == null ) {
                        orbit1 = zone .getDirection();
                        if ( orbit1 == null ) {
                            return null;
                        }
                    } else if(! orbit1.equals(zone .getDirection())){
                        return null;
                    }
                    AlgebraicNumber length = zone .getLength( offset );
                    lengths.add(length);
                    if(lengths.size() > MAX_DIVISIONS) {
                        return null;
                    }
                }
            }
            if(lengths.size() < 2) {
                return null;
            }
            
            return getProportionsFromLengths(lengths);
        }
        
        public static List<AlgebraicNumber> getProportionsFromLengths(List<AlgebraicNumber> lengths) {
            List<AlgebraicNumber> proportions = new ArrayList<>(lengths.size());
            if(! lengths.isEmpty() ) {
                AlgebraicNumber totalLength = lengths.get(0).getField().zero();

                for(AlgebraicNumber length : lengths) {
                    totalLength = totalLength.plus(length);
                }

                for(AlgebraicNumber length : lengths) {
                    proportions.add(length.dividedBy(totalLength));
                }
            }
            return proportions;
        }
        
        // This is never called for predefined tools, so proportions must be set by createToolInternal
        @Override
        protected boolean bindParameters( Selection selection )
        {
            List<AlgebraicNumber> proportions = calculateStrutProportions(selection);
            return proportions != null && proportions.size() > 1;
        }
    }

    private final String overlayText;
    private final List<AlgebraicNumber> proportions = new ArrayList<>(MAX_DIVISIONS);

    public StrutDivisionTool(String name, ToolsModel tools, List<AlgebraicNumber> proportions, String overlayText) {
        super(name, tools);
        this.overlayText = overlayText;
        if(proportions != null) { // may be null when deserializing from file
            this.proportions.addAll(proportions);
        }
        this.setInputBehaviors(false, true);
//        this.setOrderedSelection(false);
    }

    // Checking selection upon tool creation, not when it's being applied
    @Override
    protected String checkSelection(boolean prepareTool) {
        if (proportions == null || proportions.size() < 2) {
            return "Fewer than 2 struts are selected";
        }
        // mSelection will be empty when this.isPredefined()
        for (Manifestation man : mSelection) {
            if (man instanceof Strut) {
                // save the input parameters so that selectParams will work
                this.addParameter(man.getFirstConstruction());
            }
        }
        return null;
    }
    
    @Override
    public void prepare(ChangeManifestations applyTool) {
        List<Transformation> transformations = new ArrayList<>();
        if(proportions.size() >= 2) {
            AlgebraicField field = proportions.get(0).getField();
            AlgebraicNumber startNext = field.zero();
            for(AlgebraicNumber proportion : proportions) {
                transformations.add(new SegmentProportionalDivision(startNext, null)); // ball at start of each new segment
                transformations.add(new SegmentProportionalDivision(startNext, proportion)); // segment
                startNext = startNext.plus(proportion);
            }
            if(! startNext.isOne() ) {
                throw new IllegalStateException("Proportions should add up to one, but they total " + startNext);
            }
            transformations.add(new SegmentProportionalDivision(startNext, null)); // ball at end of last segment
        }
        this .transforms = transformations.toArray(new Transformation[transformations.size()]);
    }
    
    @Override
    public void performEdit( Construction c, ChangeManifestations applyTool )
    {
        // This is copied from the super class implementation
        // but I've skipped over all of the color copying code for balls.
        for (Transformation transform : transforms) {
            Construction result = transform .transform( c );
            if ( result == null )
                continue;
            Color color = c .getColor();
            if(!(result instanceof Point)) {
                result .setColor( color ); // just for consistency
            }
            Manifestation m = applyTool .manifestConstruction( result );
            if(!(result instanceof Point)) {
                if ( m != null )  // not sure why, but this happens
                    if ( color != null ) // This can be true in the Javascript world
                        applyTool .colorManifestation( m, c .getColor() );
            }
        }
        applyTool .redo();
    }
    
    @Override
    protected void getXmlAttributes( Element element )
    {
        if(proportions != null && proportions.size() > 1) {
            element.setAttribute("nProps", Integer.toString(proportions.size()));
            for(int i = 0; i < proportions.size(); i++ ) {
                AlgebraicNumber proportion = proportions.get(i);
                XmlSaveFormat.serializeNumber(element, "prop" + i, proportion);
            }
        }
        super .getXmlAttributes( element );
    }

    @Override
    protected void setXmlAttributes( Element element, XmlSaveFormat format ) throws Failure
    {
        String nProps = element.getAttribute("nProps");
        if(nProps != null) {
            try {
                int limit = Integer.parseInt(nProps);
                for(int i = 0; i < limit; i++ ) {
                    String key = "prop" + i;
                    if(element.getAttribute(key) == null) {
                        break;
                    }
                    proportions.add(format.parseNumber(element, key));
                }
            }
            catch(NumberFormatException ex) {
                throw new Failure(ex);
            }
        }
        super .setXmlAttributes( element, format );
    }
    
    @Override
    protected String getXmlElementName()
    {
        return "StrutDivisionTool";
    }

    @Override
    public String getCategory()
    {
        return CATEGORY;
    }
    
    @Override
    public String getOverlayText() {
        return overlayText;
    }
}
