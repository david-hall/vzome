package com.vzome.core.algebra;

import static com.vzome.core.generic.Utilities.getSourceCodeLine;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @author David Hall
 */
public class AlgebraicFieldTest {
    private final static Set<AlgebraicField> fields = new HashSet<>();
    
    static {
        fields.add (new PentagonField());
        fields.add (new RootTwoField());
        fields.add (new RootThreeField());
        fields.add (new HeptagonField());
        fields.add (new SnubDodecField());
        fields.add (new SnubCubeField());
        fields.add (new SqrtPhiField());
        fields.add (new PhiPlusSqrtField(2));
        fields.add (new PhiPlusSqrtField(3));
        fields.add (new PlasticNumberField());
    }
    
    @Test
    public void testEquality() {
        AlgebraicField[] f = fields.toArray( new AlgebraicField[fields.size()] );
        for(int j = 0; j < f.length; j++) {
            for(int k = 0; k < f.length; k++) {
                // TODO: This approach won't work for parameterized fields
                boolean same = (j == k);
                assertEquals( same, f[j].equals(f[k]) );
                assertEquals( same, f[j].hashCode() == f[k].hashCode() );
            }
        }
        // Specifically test the equalities and inequalities described in AlgebraicField.equals()
        compareFields(true, new PolygonField(4), new SqrtField(2));
        compareFields(true, new PolygonField(4), new RootTwoField());
        compareFields(true, new PolygonField(5), new PentagonField());
        compareFields(false, new PolygonField(6), new SqrtField(3));
        compareFields(true, new SqrtField(3), new RootThreeField());
        compareFields(true, new PolygonField(7), new HeptagonField());
        compareFields(true, new SnubDodecField(), new SnubDodecahedronField());
    }
    
    private void compareFields(boolean same, AlgebraicField f1, AlgebraicField f2 ) {
        String msg = "Expected " + f1.toString() + (same ? " == " : " != ") + f2.toString() + " at " + getSourceCodeLine(2);
        if(same) {
            assertEquals( msg, f1.getDefaultStrutScaling(), f2.getDefaultStrutScaling() );
            assertEquals( msg, f1.hashCode(), f2.hashCode() );
            assertEquals( msg, f1, f2 );
        } else {
            assertNotEquals( msg, f1.hashCode(), f2.hashCode() );
            assertNotEquals( msg, f1, f2 );
        }
    }
        
    @Test
    public void testOrder() {
        int pass = 0;
        for(AlgebraicField field : fields) {
            assertTrue(field.getOrder() >= 2);
            pass++;
        }
        assertEquals(fields.size(), pass);
    }    

    @Test
    public void testReciprocal()
    {
    	for( AlgebraicField field : fields ) {
    	    try {
    		field .zero() .reciprocal() .evaluate();
    		fail( "Zero divide should throw an exception" );
    	    } catch ( RuntimeException re ) {
    		assertEquals( "Denominator is zero", re .getMessage() );
    	    }
    	}
    }
}
