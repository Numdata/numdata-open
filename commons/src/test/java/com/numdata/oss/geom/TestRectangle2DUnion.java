/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.geom;

import com.numdata.oss.geom.Rectangle2DUnion.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link Rectangle2DUnion} class.
 *
 * @author G. Meinders
 */
public class TestRectangle2DUnion
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestRectangle2DUnion.class.getName();

	private static void assertSurfaceArea( final double expected, final Rectangle[] rectangles )
	{
		final Rectangle2DUnion union = new Rectangle2DUnion();
		for ( final Rectangle rectangle : rectangles )
		{
			union.add( rectangle );
		}
		assertEquals( "Unexpected surface area", expected, union.getSurfaceArea(), 0.0 );
	}

	/**
	 * Test the {@link Rectangle2DUnion#getSurfaceArea} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetSurfaceArea()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetSurfaceArea()" );

		// single rectangle
		assertSurfaceArea( 100.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 )
		} );

		/*
		 * Complete overlap
		 */

		// identical rectangles
		assertSurfaceArea( 100.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 10.0, 10.0, 10.0, 10.0 )
		} );

		// disjunct rectangles
		assertSurfaceArea( 200.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 0.0, 0.0, 10.0, 10.0 )
		} );
		assertSurfaceArea( 200.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 20.0, 10.0, 10.0, 10.0 )
		} );

		// completely overlapping existing rectangle
		assertSurfaceArea( 225.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 10.0, 10.0, 15.0, 15.0 )
		} );
		assertSurfaceArea( 225.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 15.0, 15.0 )
		} );
		assertSurfaceArea( 400.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 20.0, 20.0 )
		} );

		// completely overlapped by existing rectangle
		assertSurfaceArea( 100.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 10.0, 10.0, 5.0, 5.0 )
		} );
		assertSurfaceArea( 100.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 15.0, 5.0, 5.0 )
		} );
		assertSurfaceArea( 100.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 12.0, 12.0, 5.0, 5.0 )
		} );

		/*
		 * Corners
		 */

		// overlapping bottom-left corner of existing rectangle
		assertSurfaceArea( 175.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 10.0, 10.0 )
		} );

		// overlapping top-left corner of existing rectangle
		assertSurfaceArea( 175.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 15.0, 10.0, 10.0 )
		} );

		// overlapping bottom-right corner of existing rectangle
		assertSurfaceArea( 175.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 5.0, 10.0, 10.0 )
		} );

		// overlapping top-right corner of existing rectangle
		assertSurfaceArea( 175.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 15.0, 10.0, 10.0 )
		} );

		/*
		 * Sides
		 */

		// overlapping entire left side
		assertSurfaceArea( 150.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 10.0, 10.0, 10.0 )
		} );

		// overlapping entire left side (with margins)
		assertSurfaceArea( 250.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 10.0, 20.0 )
		} );

		// overlapping part of left side (but no corner)
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 12.0, 10.0, 5.0 )
		} );

		// overlapping entire right side
		assertSurfaceArea( 150.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 10.0, 10.0, 10.0 )
		} );

		// overlapping entire right side (with margins)
		assertSurfaceArea( 250.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 5.0, 10.0, 20.0 )
		} );

		// overlapping part of right side (but no corner)
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 12.0, 10.0, 5.0 )
		} );

		// overlapping entire bottom side
		assertSurfaceArea( 150.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 10.0, 5.0, 10.0, 10.0 )
		} );

		// overlapping entire bottom side (with margins)
		assertSurfaceArea( 250.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 20.0, 10.0 )
		} );

		// overlapping part of bottom side (but no corner)
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 12.0, 5.0, 5.0, 10.0 )
		} );

		// overlapping entire top side
		assertSurfaceArea( 150.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 10.0, 15.0, 10.0, 10.0 )
		} );

		// overlapping entire top side (with margins)
		assertSurfaceArea( 250.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 5.0, 15.0, 20.0, 10.0 )
		} );

		// overlapping part of top side (but no corner)
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 12.0, 15.0, 5.0, 10.0 )
		} );

		/*
		 * Intersections
		 */

		// intersection through left/right sides
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 8.0, 12.0, 15.0, 5.0 )
		} );

		// intersection through bottom/top sides
		assertSurfaceArea( 125.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 12.0, 8.0, 5.0, 15.0 )
		} );

		/*
		 * Multiple rectangles
		 */
		assertSurfaceArea( 500.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 15.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 5.0, 10.0, 25.0 ),
		new Rectangle( 5.0, 15.0, 25.0, 10.0 ),
		new Rectangle( 5.0, 5.0, 20.0, 20.0 )
		} );
		assertSurfaceArea( 950.0, new Rectangle[] {
		new Rectangle( 10.0, 10.0, 10.0, 10.0 ),
		new Rectangle( 15.0, 15.0, 10.0, 10.0 ),
		new Rectangle( 0.0, 0.0, 10.0, 10.0 ),
		new Rectangle( 0.0, 0.0, 25.0, 25.0 ),
		new Rectangle( -5.0, -5.0, 35.0, 20.0 ),
		new Rectangle( 12.0, 12.0, 10.0, 10.0 )
		} );
	}

	/**
	 * Unit test for possible infinite loop due to rounding, which was the cause
	 * of issue 2442.
	 */
	@Test
	public void testRoundingErrorInfiniteLoop()
	{
		final Rectangle2DUnion union = new Rectangle2DUnion();

		// These lines do not appear to affect the outcome.
//		union.add( new Rectangle( 0.019, 0.019, 0.6365, 0.283 ) );
//		union.add( new Rectangle( 0.019, 0.019, 0.6365, 0.132 ) );
//		union.add( new Rectangle( 0.019, 0.17, 0.6365, 0.13199999999999998 ) );
//		union.add( new Rectangle( 0.6745, 0.019, 0.6365, 0.283 ) );
//		union.add( new Rectangle( 0.6745, 0.019, 0.6365, 0.132 ) );
//		union.add( new Rectangle( 0.6745, 0.17, 0.6365, 0.13199999999999998 ) );
//		union.add( new Rectangle( 0.0195, 0.151, 0.6355, 0.019 ) );
//		union.add( new Rectangle( 0.675, 0.151, 0.6355, 0.019 ) );

		// Without this line, the infinite loop does not occur.
		union.add( new Rectangle( 0.0, 0.019, 0.019, 0.283 ) );

		// These lines do not appear to affect the outcome.
//		union.add( new Rectangle( 0.6555, 0.019, 0.019, 0.283 ) );
//		union.add( new Rectangle( 1.311, 0.019, 0.019, 0.283 ) );
//		union.add( new Rectangle( 0.019, 0.019, 1.292, 0.283 ) );

		// An infinite loop occurred at this line.
		union.add( new Rectangle( 0.002, 0.002, 1.326, 0.298 ) );
	}
}
