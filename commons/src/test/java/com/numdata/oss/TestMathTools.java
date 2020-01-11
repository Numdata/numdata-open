/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numdata.oss;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for the {@link MathTools} class.
 *
 * @author G. Meinders
 */
public class TestMathTools
{
	/**
	 * Test {@link MathTools#mod} function.
	 */
	@Test
	public void testMod()
	{
		assertEquals( "Unexpected result from mod(     0,    1 )", 0, MathTools.mod( 0, 1 ) );
		assertEquals( "Unexpected result from mod(     0,   -1 )", 0, MathTools.mod( 0, -1 ) );
		assertEquals( "Unexpected result from mod(     0,  360 )", 0, MathTools.mod( 0, 360 ) );
		assertEquals( "Unexpected result from mod(    90,  360 )", 90, MathTools.mod( 90, 360 ) );
		assertEquals( "Unexpected result from mod(   270,  360 )", 270, MathTools.mod( 270, 360 ) );
		assertEquals( "Unexpected result from mod(   360,  360 )", 0, MathTools.mod( 360, 360 ) );
		assertEquals( "Unexpected result from mod(   450,  360 )", 90, MathTools.mod( 450, 360 ) );
		assertEquals( "Unexpected result from mod(  3600,  360 )", 0, MathTools.mod( 3600, 360 ) );
		assertEquals( "Unexpected result from mod(  3690,  360 )", 90, MathTools.mod( 3690, 360 ) );
		assertEquals( "Unexpected result from mod(  3780,  360 )", 180, MathTools.mod( 3780, 360 ) );
		assertEquals( "Unexpected result from mod(  3870,  360 )", 270, MathTools.mod( 3870, 360 ) );
		assertEquals( "Unexpected result from mod(     0,  360 )", 0, MathTools.mod( 0, 360 ) );
		assertEquals( "Unexpected result from mod(   -90,  360 )", 270, MathTools.mod( -90, 360 ) );
		assertEquals( "Unexpected result from mod(  -270,  360 )", 90, MathTools.mod( -270, 360 ) );
		assertEquals( "Unexpected result from mod(  -360,  360 )", 0, MathTools.mod( -360, 360 ) );
		assertEquals( "Unexpected result from mod(  -450,  360 )", 270, MathTools.mod( -450, 360 ) );
		assertEquals( "Unexpected result from mod( -3600,  360 )", 0, MathTools.mod( -3600, 360 ) );
		assertEquals( "Unexpected result from mod( -3690,  360 )", 270, MathTools.mod( -3690, 360 ) );
		assertEquals( "Unexpected result from mod( -3780,  360 )", 180, MathTools.mod( -3780, 360 ) );
		assertEquals( "Unexpected result from mod( -3870,  360 )", 90, MathTools.mod( -3870, 360 ) );
		assertEquals( "Unexpected result from mod(     0, -360 )", 0, MathTools.mod( 0, -360 ) );
		assertEquals( "Unexpected result from mod(   -90, -360 )", -90, MathTools.mod( -90, -360 ) );
		assertEquals( "Unexpected result from mod(  -270, -360 )", -270, MathTools.mod( -270, -360 ) );
		assertEquals( "Unexpected result from mod(  -360, -360 )", 0, MathTools.mod( -360, -360 ) );
		assertEquals( "Unexpected result from mod(  -450, -360 )", -90, MathTools.mod( -450, -360 ) );
		assertEquals( "Unexpected result from mod( -3600, -360 )", 0, MathTools.mod( -3600, -360 ) );
		assertEquals( "Unexpected result from mod( -3690, -360 )", -90, MathTools.mod( -3690, -360 ) );
		assertEquals( "Unexpected result from mod( -3780, -360 )", -180, MathTools.mod( -3780, -360 ) );
		assertEquals( "Unexpected result from mod( -3870, -360 )", -270, MathTools.mod( -3870, -360 ) );
		assertEquals( "Unexpected result from mod(     0, -360 )", 0, MathTools.mod( 0, -360 ) );
		assertEquals( "Unexpected result from mod(    90, -360 )", -270, MathTools.mod( 90, -360 ) );
		assertEquals( "Unexpected result from mod(   270, -360 )", -90, MathTools.mod( 270, -360 ) );
		assertEquals( "Unexpected result from mod(   360, -360 )", 0, MathTools.mod( 360, -360 ) );
		assertEquals( "Unexpected result from mod(   450, -360 )", -270, MathTools.mod( 450, -360 ) );
		assertEquals( "Unexpected result from mod(  3600, -360 )", 0, MathTools.mod( 3600, -360 ) );
		assertEquals( "Unexpected result from mod(  3690, -360 )", -270, MathTools.mod( 3690, -360 ) );
		assertEquals( "Unexpected result from mod(  3780, -360 )", -180, MathTools.mod( 3780, -360 ) );
		assertEquals( "Unexpected result from mod(  3870, -360 )", -90, MathTools.mod( 3870, -360 ) );

		assertEquals( "Unexpected result from mod(     0.0f,    1.0f )", 0.0f, MathTools.mod( 0.0f, 1.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f,    0.5f )", 0.0f, MathTools.mod( 0.5f, 0.5f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.0f,   -1.0f )", 0.0f, MathTools.mod( 0.0f, -1.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f,   -0.5f )", 0.0f, MathTools.mod( 0.5f, -0.5f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f,  360.0f )", 0.5f, MathTools.mod( 0.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(    90.5f,  360.0f )", 90.5f, MathTools.mod( 90.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   270.5f,  360.0f )", 270.5f, MathTools.mod( 270.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   360.5f,  360.0f )", 0.5f, MathTools.mod( 360.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   450.5f,  360.0f )", 90.5f, MathTools.mod( 450.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3600.5f,  360.0f )", 0.5f, MathTools.mod( 3600.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3690.5f,  360.0f )", 90.5f, MathTools.mod( 3690.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3780.5f,  360.0f )", 180.5f, MathTools.mod( 3780.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3870.5f,  360.0f )", 270.5f, MathTools.mod( 3870.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f,  360.0f )", 0.5f, MathTools.mod( 0.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   -90.5f,  360.0f )", 269.5f, MathTools.mod( -90.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -270.5f,  360.0f )", 89.5f, MathTools.mod( -270.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -360.5f,  360.0f )", 359.5f, MathTools.mod( -360.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -450.5f,  360.0f )", 269.5f, MathTools.mod( -450.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3600.5f,  360.0f )", 359.5f, MathTools.mod( -3600.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3690.5f,  360.0f )", 269.5f, MathTools.mod( -3690.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3780.5f,  360.0f )", 179.5f, MathTools.mod( -3780.5f, 360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f, -360.0f )", -359.5f, MathTools.mod( 0.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   -90.5f, -360.0f )", -90.5f, MathTools.mod( -90.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -270.5f, -360.0f )", -270.5f, MathTools.mod( -270.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -360.5f, -360.0f )", -0.5f, MathTools.mod( -360.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  -450.5f, -360.0f )", -90.5f, MathTools.mod( -450.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3600.5f, -360.0f )", -0.5f, MathTools.mod( -3600.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3690.5f, -360.0f )", -90.5f, MathTools.mod( -3690.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3780.5f, -360.0f )", -180.5f, MathTools.mod( -3780.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod( -3870.5f, -360.0f )", -270.5f, MathTools.mod( -3870.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(     0.5f, -360.0f )", -359.5f, MathTools.mod( 0.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(    90.5f, -360.0f )", -269.5f, MathTools.mod( 90.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   270.5f, -360.0f )", -89.5f, MathTools.mod( 270.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   360.5f, -360.0f )", -359.5f, MathTools.mod( 360.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(   450.5f, -360.0f )", -269.5f, MathTools.mod( 450.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3600.5f, -360.0f )", -359.5f, MathTools.mod( 3600.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3690.5f, -360.0f )", -269.5f, MathTools.mod( 3690.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3780.5f, -360.0f )", -179.5f, MathTools.mod( 3780.5f, -360.0f ), 0.0f );
		assertEquals( "Unexpected result from mod(  3870.5f, -360.0f )", -89.5f, MathTools.mod( 3870.5f, -360.0f ), 0.0f );

		assertEquals( "Unexpected result from mod(     0.0,    1.0 )", 0.0, MathTools.mod( 0.0, 1.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5,    0.5 )", 0.0, MathTools.mod( 0.5, 0.5 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.0,   -1.0 )", 0.0, MathTools.mod( 0.0, -1.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5,   -0.5 )", 0.0, MathTools.mod( 0.5, -0.5 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5,  360.0 )", 0.5, MathTools.mod( 0.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(    90.5,  360.0 )", 90.5, MathTools.mod( 90.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   270.5,  360.0 )", 270.5, MathTools.mod( 270.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   360.5,  360.0 )", 0.5, MathTools.mod( 360.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   450.5,  360.0 )", 90.5, MathTools.mod( 450.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3600.5,  360.0 )", 0.5, MathTools.mod( 3600.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3690.5,  360.0 )", 90.5, MathTools.mod( 3690.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3780.5,  360.0 )", 180.5, MathTools.mod( 3780.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3870.5,  360.0 )", 270.5, MathTools.mod( 3870.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5,  360.0 )", 0.5, MathTools.mod( 0.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   -90.5,  360.0 )", 269.5, MathTools.mod( -90.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -270.5,  360.0 )", 89.5, MathTools.mod( -270.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -360.5,  360.0 )", 359.5, MathTools.mod( -360.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -450.5,  360.0 )", 269.5, MathTools.mod( -450.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3600.5,  360.0 )", 359.5, MathTools.mod( -3600.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3690.5,  360.0 )", 269.5, MathTools.mod( -3690.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3780.5,  360.0 )", 179.5, MathTools.mod( -3780.5, 360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5, -360.0 )", -359.5, MathTools.mod( 0.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   -90.5, -360.0 )", -90.5, MathTools.mod( -90.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -270.5, -360.0 )", -270.5, MathTools.mod( -270.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -360.5, -360.0 )", -0.5, MathTools.mod( -360.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  -450.5, -360.0 )", -90.5, MathTools.mod( -450.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3600.5, -360.0 )", -0.5, MathTools.mod( -3600.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3690.5, -360.0 )", -90.5, MathTools.mod( -3690.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3780.5, -360.0 )", -180.5, MathTools.mod( -3780.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod( -3870.5, -360.0 )", -270.5, MathTools.mod( -3870.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(     0.5, -360.0 )", -359.5, MathTools.mod( 0.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(    90.5, -360.0 )", -269.5, MathTools.mod( 90.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   270.5, -360.0 )", -89.5, MathTools.mod( 270.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   360.5, -360.0 )", -359.5, MathTools.mod( 360.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(   450.5, -360.0 )", -269.5, MathTools.mod( 450.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3600.5, -360.0 )", -359.5, MathTools.mod( 3600.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3690.5, -360.0 )", -269.5, MathTools.mod( 3690.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3780.5, -360.0 )", -179.5, MathTools.mod( 3780.5, -360.0 ), 0.0 );
		assertEquals( "Unexpected result from mod(  3870.5, -360.0 )", -89.5, MathTools.mod( 3870.5, -360.0 ), 0.0 );
	}

	/**
	 * Tests the {@link MathTools#rangeDistance} method.
	 */
	@Test
	public void testRangeDistance()
	{
		/*
		 * NOTE: Successive powers of two were chosen as test values, such that
		 * subtracting any two distinct values yields a distinct result.
		 */

		/* Disjoint ranges. */
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 1.0, 2.0, 4.0, 8.0 ), 0.0 );
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 4.0, 8.0, 1.0, 2.0 ), 0.0 );

		/* Parameter sorting. (variations on the first test) */
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 2.0, 1.0, 4.0, 8.0 ), 0.0 );
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 1.0, 2.0, 8.0, 4.0 ), 0.0 );
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 2.0, 1.0, 8.0, 4.0 ), 0.0 );

		/* Touching ranges. */
		assertEquals( "Unexpected result.", 0.0, MathTools.rangeDistance( 1.0, 2.0, 2.0, 4.0 ), 0.0 );
		assertEquals( "Unexpected result.", 0.0, MathTools.rangeDistance( 2.0, 4.0, 1.0, 2.0 ), 0.0 );

		/* Overlapping ranges. */
		assertEquals( "Unexpected result.", -2.0, MathTools.rangeDistance( 1.0, 4.0, 2.0, 8.0 ), 0.0 );
		assertEquals( "Unexpected result.", -2.0, MathTools.rangeDistance( 2.0, 8.0, 1.0, 4.0 ), 0.0 );

		/* Enclosing ranges. */
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 8.0, 2.0, 4.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 2.0, 4.0, 1.0, 8.0 ) ) );

		/* Half-equal ranges. */
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 4.0, 2.0, 4.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 2.0, 4.0, 1.0, 4.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 2.0, 8.0, 2.0, 4.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 2.0, 4.0, 2.0, 8.0 ) ) );

		/* Equal ranges. */
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 2.0, 1.0, 2.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 1.0, 1.0, 1.0 ) ) );

		/* Empty ranges. */
		assertEquals( "Unexpected result.", 1.0, MathTools.rangeDistance( 1.0, 1.0, 2.0, 4.0 ), 0.0 );
		assertEquals( "Unexpected result.", 1.0, MathTools.rangeDistance( 2.0, 4.0, 1.0, 1.0 ), 0.0 );
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 1.0, 2.0, 4.0, 4.0 ), 0.0 );
		assertEquals( "Unexpected result.", 2.0, MathTools.rangeDistance( 4.0, 4.0, 1.0, 2.0 ), 0.0 );
		assertEquals( "Unexpected result.", 1.0, MathTools.rangeDistance( 1.0, 1.0, 2.0, 2.0 ), 0.0 );
		assertEquals( "Unexpected result.", 1.0, MathTools.rangeDistance( 2.0, 2.0, 1.0, 1.0 ), 0.0 );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 1.0, 1.0, 2.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 2.0, 1.0, 1.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 2.0, 2.0, 1.0, 2.0 ) ) );
		assertTrue( "Unexpected result.", Double.isNaN( MathTools.rangeDistance( 1.0, 2.0, 2.0, 2.0 ) ) );
	}

	/**
	 * Tests the {@link MathTools#nearestPowerOfTwo} method.
	 */
	@Test
	public void testNearestPowerOfTwo()
	{
		for ( int i = 0; i < 31; i++ )
		{
			final int powerOfTwo = 1 << i;
			assertEquals( "Power of two must be nearest to itself", powerOfTwo, MathTools.nearestPowerOfTwo( powerOfTwo ) );
		}

		for ( int i = 1; i < 31; i++ )
		{
			// NOTE: ( 1<<30 + 1<<29 ) yields 1<<30, not 1<<31 since that would be a negative value
			final int powerOfTwo = 1 << Math.min( 30, i + 1 );
			final int halfway = ( 1 << i ) | ( 1 << ( i - 1 ) );
			assertEquals( "Halfway point must be rounded up", powerOfTwo, MathTools.nearestPowerOfTwo( halfway ) );
		}

		for ( int i = 1; i < 31; i++ )
		{
			final int powerOfTwo = 1 << i;
			final int halfway = ( 1 << i ) | ( 1 << ( i - 1 ) );
			assertEquals( "Anything below halfway point must be rounded down", powerOfTwo, MathTools.nearestPowerOfTwo( halfway - 1 ) );
		}

		try
		{
			MathTools.nearestPowerOfTwo( -1 );
			fail( "Expected 'IllegalArgumentException'" );
		}
		catch ( IllegalArgumentException e )
		{
			/* Success */
		}

		try
		{
			MathTools.nearestPowerOfTwo( 0 );
			fail( "Expected 'IllegalArgumentException'" );
		}
		catch ( IllegalArgumentException e )
		{
			/* Success */
		}
	}

	@Test
	public void significantCompare()
	{
		assertEquals( "Unexpected result from significantCompare( 1.0f, 2.0f )", -1, MathTools.significantCompare( 1.0f, 2.0f ) );
		assertEquals( "Unexpected result from significantCompare( 1.0f, 2.0f, 0.1f )", -1, MathTools.significantCompare( 1.0f, 2.0f, 0.1f ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 2.0 )", -1, MathTools.significantCompare( 1.0, 2.0 ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 2.0, 0.1 )", -1, MathTools.significantCompare( 1.0, 2.0, 0.1 ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 2.0, 0.1 )", -1, MathTools.significantCompare( 1.0, 2.0, 0.1 ) );

		assertEquals( "Unexpected result from significantCompare( 1.0f, 1.0f )", 0, MathTools.significantCompare( 1.0f, 1.0f ) );
		assertEquals( "Unexpected result from significantCompare( 1.0f, 1.0f, 0.1f )", 0, MathTools.significantCompare( 1.0f, 1.0f, 0.1f ) );
		assertEquals( "Unexpected result from significantCompare( 1.0f, 2.0f, 1.0f )", 0, MathTools.significantCompare( 1.0f, 2.0f, 1.0f ) );
		assertEquals( "Unexpected result from significantCompare( 2.0f, 1.0f, 1.0f )", 0, MathTools.significantCompare( 1.0f, 2.0f, 1.0f ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 1.0 )", 0, MathTools.significantCompare( 1.0, 1.0 ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 1.0, 0.1 )", 0, MathTools.significantCompare( 1.0, 1.0, 0.1 ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 1.0, 0.1 )", 0, MathTools.significantCompare( 1.0, 1.0, 0.1 ) );
		assertEquals( "Unexpected result from significantCompare( 1.0, 2.0, 1.0 )", 0, MathTools.significantCompare( 1.0, 2.0, 1.0 ) );
		assertEquals( "Unexpected result from significantCompare( 2.0, 1.0, 1.0 )", 0, MathTools.significantCompare( 1.0, 2.0, 1.0 ) );

		assertEquals( "Unexpected result from significantCompare( 2.0f, 1.0f )", 1, MathTools.significantCompare( 2.0f, 1.0f ) );
		assertEquals( "Unexpected result from significantCompare( 2.0f, 1.0f, 0.1f )", 1, MathTools.significantCompare( 2.0f, 1.0f, 0.1f ) );
		assertEquals( "Unexpected result from significantCompare( 2.0, 1.0 )", 1, MathTools.significantCompare( 2.0, 1.0 ) );
		assertEquals( "Unexpected result from significantCompare( 2.0, 1.0, 0.1 )", 1, MathTools.significantCompare( 2.0, 1.0, 0.1 ) );
		assertEquals( "Unexpected result from significantCompare( 2.0, 1.0, 0.1 )", 1, MathTools.significantCompare( 2.0, 1.0, 0.1 ) );
	}
}
