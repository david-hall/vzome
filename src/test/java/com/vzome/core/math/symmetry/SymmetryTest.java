
//(c) Copyright 2015, Scott Vorthmann.

package com.vzome.core.math.symmetry;

import java.util.Arrays;

import junit.framework.TestCase;

import com.vzome.core.algebra.PentagonField;
import com.vzome.core.math.RealVector;

public class SymmetryTest extends TestCase
{
    public void testAxisIncidence()
    {
        IcosahedralSymmetry symm = new IcosahedralSymmetry( new PentagonField(), null );
        
        assertTrue( Arrays.equals( new int[]{ 22, 50, 19 }, symm .getIncidentOrientations( 10 ) ) );
        assertTrue( Arrays.equals( new int[]{ 20, 52, 17 }, symm .getIncidentOrientations( 44 ) ) );
    }
    
    public void testGetAxis()
    {
        RealVector vector = new RealVector( 0.1, 0.1, 3.0 ); // should be orientation -47
        IcosahedralSymmetry symm = new IcosahedralSymmetry( new PentagonField(), null );
        
        Direction orbit = symm .getDirection( "red" );
        Axis axis = orbit .getAxis( vector );
        Axis expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "blue" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "yellow" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "green" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "orange" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "purple" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "black" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "turquoise" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "rose" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "maroon" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "olive" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "lavender" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "navy" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "sulfur" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "spruce" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "cinnamon" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "apple" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "coral" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "sand" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );

        orbit = symm .getDirection( "brown" );
        axis = orbit .getAxis( vector );
        expected = orbit .getAxisBruteForce( vector );
        assertEquals( expected, axis );
    }
}
