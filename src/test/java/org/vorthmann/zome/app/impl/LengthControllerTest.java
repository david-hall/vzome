package org.vorthmann.zome.app.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.StringTokenizer;

import org.junit.Test;
import org.vorthmann.ui.Controller;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.PentagonField;

public class LengthControllerTest
{
	private void assertStateAfter( Controller controller, String[] actions, String[] propertyValues )
	{
		if ( actions != null )
			try {
				for ( String actionOrChange : actions ) {
					if ( actionOrChange .contains( "=" ) ) {
						StringTokenizer tokens = new StringTokenizer( actionOrChange, "=" );
						String name = tokens .nextToken();
						String value = tokens .nextToken();
						controller .setProperty( name, value );
					}
					else
						controller .doAction( actionOrChange, null );
				}
			} catch (Exception e) {
				fail( e .getMessage() );
			}
		String[] properties = new String[]{ "half", "scale", "unitText", "unitIsCustom", "lengthText" };
		for ( int i = 0; i < properties.length; i++ ) {
			String actual = controller .getProperty( properties[ i ] );
			String expected = propertyValues[ i ];
			assertEquals( expected, actual );
		}
	}
	
	private void assertStateAfter( LengthController controller, String[] actions, String[] propertyValues, AlgebraicNumber expectedValue )
	{
		assertStateAfter( controller, actions, propertyValues );
		AlgebraicNumber result = controller .getValue();
		assertEquals( expectedValue, result );
	}
	
	@Test
	public void testInitialState() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, null, new String[]{ "false", "0", "1", "false", "1" },
							field .createAlgebraicNumber( 1, 0, 1, 3 ) );
	}

	@Test
	public void testSetHalf() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, new String[]{ "half=true" },
							new String[]{ "true", "0", "1", "false", "1" },
							field .createAlgebraicNumber( 1, 0, 2, 3 ) );
	}

	@Test
	public void testToggleHalf() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, new String[]{ "half=true, toggleHalf" },
							new String[]{ "false", "0", "1", "false", "1" },
							field .createAlgebraicNumber( 1, 0, 1, 3 ) );
	}

	@Test
	public void testSetScale() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, new String[]{ "scale=5" },
							new String[]{ "false", "5", "1", "false", "3 +5φ" },
							field .createAlgebraicNumber( 1, 0, 1, 8 ) );
	}

	@Test
	public void testGetCustomUnit() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		Controller units = controller .getSubController( "unit" );
		units .setProperty( "values", "5 8 3" );
		assertStateAfter( controller, new String[]{ "getCustomUnit" },
							new String[]{ "false", "0", "5/3 +8/3φ", "true", "5/3 +8/3φ" },
							field .createAlgebraicNumber( 5, 8, 3, 3 ) );
	}

	@Test
	public void testLong() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, new String[]{ "long" },
							new String[]{ "false", "2", "1", "false", "1 +φ" },
							field .createAlgebraicNumber( 1, 0, 1, 5 ) );
	}

	@Test
	public void testScaling() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		assertStateAfter( controller, new String[]{ "long", "scaleDown", "scaleDown", "scaleDown" },
							new String[]{ "false", "-1", "1", "false", "-1 +φ" },
							field .createPower( 2 ) );
	}

	@Test
	public void testNewZeroScale() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		Controller units = controller .getSubController( "unit" );
		units .setProperty( "values", "3 0 1" );
		assertStateAfter( controller, new String[]{ "getCustomUnit", "scaleUp", "scaleUp", "newZeroScale" },
							new String[]{ "false", "0", "3 +3φ", "true", "3 +3φ" },
							field .createAlgebraicNumber( 3, 0, 1, 5 ) );
	}

	@Test
	public void testReset() 
	{
		AlgebraicField field = new PentagonField();
		LengthController controller = new LengthController( field );
		Controller units = controller .getSubController( "unit" );
		units .setProperty( "values", "5 2 7" );
		assertStateAfter( controller, new String[]{ "getCustomUnit", "scaleUp", "newZeroScale", "reset" },
							new String[]{ "false", "0", "1", "false", "1" },
							field .createAlgebraicNumber( 1, 0, 1, 3 ) );
	}
}
