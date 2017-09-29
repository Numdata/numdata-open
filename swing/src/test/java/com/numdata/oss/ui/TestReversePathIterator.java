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
package com.numdata.oss.ui;

import java.awt.geom.*;
import java.text.*;
import java.util.*;

import com.numdata.oss.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Test {@link ReversePathIterator} class.
 *
 * @author Peter S. Heijnen
 */
public class TestReversePathIterator
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestReversePathIterator.class.getName();

	/** {@link PathIterator#SEG_MOVETO} constant. */
	public static final double MOVE = (double)PathIterator.SEG_MOVETO;

	/** {@link PathIterator#SEG_LINETO} constant. */
	public static final double LINE = (double)PathIterator.SEG_LINETO;

	/** {@link PathIterator#SEG_QUADTO} constant. */
	public static final double QUAD = (double)PathIterator.SEG_QUADTO;

	/** {@link PathIterator#SEG_CUBICTO} constant. */
	public static final double CUBIC = (double)PathIterator.SEG_CUBICTO;

	/** {@link PathIterator#SEG_CLOSE} constant. */
	public static final double CLOSE = (double)PathIterator.SEG_CLOSE;

	/** Pseudo segment-type used for end of path. */
	public static final int END_OF_PATH = -1;

	/**
	 * Format used for numbers.
	 */
	private static final NumberFormat NUMBER_FORMAT = TextTools.getNumberFormat( Locale.US, 1, 1, false );

	/**
	 * Test reversal of several open line paths.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testOpenLinePaths()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testOpenLinePaths" );

		final Path2D[][] tests =
		{
			/* sinle line */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0 ),
			  constructPath( MOVE, 700.0, 0.0,
			                 LINE, 0.0, 0.0 ) },

			/* polygon without close segment */
			{ constructPath( MOVE, -350.0, -10.0,
			                 LINE, 768.0, -10.0,
			                 LINE, 768.0, 400.0,
			                 LINE, -350.0, 400.0,
			                 LINE, -350.0, -10.0 ),
			  constructPath( MOVE, -350.0, -10.0,
			                 LINE, -350.0, 400.0,
			                 LINE, 768.0, 400.0,
			                 LINE, 768.0, -10.0,
			                 LINE, -350.0, -10.0 ) },


			/* two closed sub-paths */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 MOVE, 0.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 700.0, 400.0 ),
			  constructPath( MOVE, 700.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 0.0, 400.0,
			                 MOVE, 700.0, 400.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 0.0, 0.0 ) },
			};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final Path2D[] test = tests[ testIndex ];
			final String testName = "Test #" + ( testIndex + 1 );
			final Path2D originalPath = test[ 0 ];
			final Path2D expectedResult = test[ 1 ];

			asserEquals( testName, expectedResult.getPathIterator( null ), new ReversePathIterator( originalPath ) );
		}
	}

	/**
	 * Test reversal of several closed line paths.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testClosedLinePaths()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testClosedLinePaths" );

		final Path2D[][] tests =
		{
			/* closed triangle */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 CLOSE ),
			  constructPath( MOVE, 700.0, 400.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 0.0, 0.0,
			                 CLOSE ) },

			/* two closed sub-paths */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 CLOSE,
			                 MOVE, 0.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 CLOSE ),
			  constructPath( MOVE, 700.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 0.0, 400.0,
			                 CLOSE,
			                 MOVE, 700.0, 400.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 0.0, 0.0,
			                 CLOSE ) },
			};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final Path2D[] test = tests[ testIndex ];
			final String testName = "Test #" + ( testIndex + 1 );
			final Path2D originalPath = test[ 0 ];
			final Path2D expectedResult = test[ 1 ];

			asserEquals( testName, expectedResult.getPathIterator( null ), new ReversePathIterator( originalPath ) );
		}
	}

	/**
	 * Test reversal of several complex paths.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testComplexPaths()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testComplexPaths" );

		final Path2D[][] tests =
		{
			/* open sub-path followed by closed sub-path */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 MOVE, 0.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 CLOSE ),
			  constructPath( MOVE, 700.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 0.0, 400.0,
			                 CLOSE,
			                 MOVE, 700.0, 400.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 0.0, 0.0 ) },

			/* closed sub-path followed by  open sub-path */
			{ constructPath( MOVE, 0.0, 0.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 700.0, 400.0,
			                 CLOSE,
			                 MOVE, 0.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 700.0, 400.0 ),

			  constructPath( MOVE, 700.0, 400.0,
			                 LINE, 0.0, 0.0,
			                 LINE, 0.0, 400.0,
			                 MOVE, 700.0, 400.0,
			                 LINE, 700.0, 0.0,
			                 LINE, 0.0, 0.0,
			                 CLOSE ) },
			};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final Path2D[] test = tests[ testIndex ];
			final String testName = "Test #" + ( testIndex + 1 );
			final Path2D originalPath = test[ 0 ];
			final Path2D expectedResult = test[ 1 ];

			asserEquals( testName, expectedResult.getPathIterator( null ), new ReversePathIterator( originalPath ) );
		}
	}

	/**
	 * Helper method to construct path for tests.
	 *
	 * @param data Data consisting of segment types directly followed by their
	 *             coordinates.
	 *
	 * @return {@link Path2D} that was constructed.
	 */
	public static Path2D constructPath( final double... data )
	{
		final Path2D.Double result = new Path2D.Double();

		int pos = 0;
		while ( pos < data.length )
		{
			final int segmentType = (int)data[ pos++ ];
			switch ( segmentType )
			{
				case PathIterator.SEG_MOVETO:
					result.moveTo( data[ pos++ ], data[ pos++ ] );
					break;

				case PathIterator.SEG_LINETO:
					result.lineTo( data[ pos++ ], data[ pos++ ] );
					break;

				case PathIterator.SEG_QUADTO:
					result.quadTo( data[ pos++ ], data[ pos++ ], data[ pos++ ], data[ pos++ ] );
					break;

				case PathIterator.SEG_CUBICTO:
					result.curveTo( data[ pos++ ], data[ pos++ ], data[ pos++ ], data[ pos++ ], data[ pos++ ], data[ pos++ ] );
					break;

				case PathIterator.SEG_CLOSE:
					result.closePath();
					break;

				default:
					throw new AssertionError( "Unrecognized segment type: " + segmentType );
			}
		}

		return result;
	}

	/**
	 * Assert that both iterators return sequence of elements.
	 *
	 * @param message  Message to output if the iterators differ.
	 * @param expected Expected iteration.
	 * @param actual   Atual iteration.
	 */
	public static void asserEquals( final String message, final PathIterator expected, final PathIterator actual )
	{
		final StringBuilder seenSofar = new StringBuilder();

		final double[] expectedCoords = new double[ 6 ];
		final double[] actualCoords = new double[ 6 ];

		while ( true )
		{
			final int expectedType = expected.isDone() ? END_OF_PATH : expected.currentSegment( expectedCoords );
			final int actualType = actual.isDone() ? END_OF_PATH : actual.currentSegment( actualCoords );

			if ( ( expectedType == END_OF_PATH ) && ( actualType == END_OF_PATH ) )
			{
				break;
			}

			if ( !equals( expectedType, expectedCoords, actualType, actualCoords ) )
			{
				fail( message + " expected " + getSegmentDescription( expectedType, expectedCoords ) + ", but got " + getSegmentDescription( actualType, actualCoords ) + ( ( seenSofar.length() > 0 ) ? " after " + seenSofar : " at first segment" ) );
			}

			seenSofar.append( "\n    " );
			seenSofar.append( getSegmentDescription( expectedType, expectedCoords ) );

			expected.next();
			actual.next();
		}
	}

	/**
	 * Test whether two segments are equal.
	 *
	 * @param segmentType1 First segment type.
	 * @param coords1      Coordinates associated with first segment.
	 * @param segmentType2 Second segment type.
	 * @param coords2      Coordinates associated with second segment.
	 *
	 * @return {@code true} if the two segments are equal; {@code false}
	 * otherwise.
	 */
	public static boolean equals( final int segmentType1, final double[] coords1, final int segmentType2, final double[] coords2 )
	{
		final boolean result;

		if ( segmentType1 == segmentType2 )
		{
			switch ( segmentType1 )
			{
				case PathIterator.SEG_MOVETO:
				case PathIterator.SEG_LINETO:
					result = ( coords1[ 0 ] == coords2[ 0 ] ) && ( coords1[ 1 ] == coords2[ 1 ] );
					break;

				case PathIterator.SEG_QUADTO:
					result = ( coords1[ 0 ] == coords2[ 0 ] ) && ( coords1[ 1 ] == coords2[ 1 ] ) &&
					         ( coords1[ 2 ] == coords2[ 2 ] ) && ( coords1[ 3 ] == coords2[ 3 ] );
					break;

				case PathIterator.SEG_CUBICTO:
					result = ( coords1[ 0 ] == coords2[ 0 ] ) && ( coords1[ 1 ] == coords2[ 1 ] ) &&
					         ( coords1[ 2 ] == coords2[ 2 ] ) && ( coords1[ 3 ] == coords2[ 3 ] ) &&
					         ( coords1[ 4 ] == coords2[ 4 ] ) && ( coords1[ 5 ] == coords2[ 5 ] );
					break;

				case END_OF_PATH:
				case PathIterator.SEG_CLOSE:
					result = true;
					break;

				default:
					throw new AssertionError( "Unrecognized segment type: " + segmentType1 );
			}
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Describe segment.
	 *
	 * @param segmentType Segment type.
	 * @param coords      Coordinates associated with segment.
	 *
	 * @return Description of segment.
	 */
	public static String getSegmentDescription( final int segmentType, final double[] coords )
	{
		final String result;

		final NumberFormat nf = NUMBER_FORMAT;

		switch ( segmentType )
		{
			case END_OF_PATH:
				result = "END_OF_PATH";
				break;

			case PathIterator.SEG_MOVETO:
				result = "SEG_MOVETO( " + nf.format( coords[ 0 ] ) + " , " + nf.format( coords[ 1 ] ) + " )";
				break;

			case PathIterator.SEG_LINETO:
				result = "SEG_LINETO( " + nf.format( coords[ 0 ] ) + " , " + nf.format( coords[ 1 ] ) + " )";
				break;

			case PathIterator.SEG_QUADTO:
				result = "SEG_QUADTO( " + nf.format( coords[ 0 ] ) + " , " + nf.format( coords[ 1 ] ) + "  ,  " +
				         nf.format( coords[ 2 ] ) + " , " + nf.format( coords[ 3 ] ) + " )";
				break;

			case PathIterator.SEG_CUBICTO:
				result = "SEG_CUBICTO( " + nf.format( coords[ 0 ] ) + " , " + nf.format( coords[ 1 ] ) + "  ,  " +
				         nf.format( coords[ 2 ] ) + " , " + nf.format( coords[ 3 ] ) + "  ,  " +
				         nf.format( coords[ 4 ] ) + " , " + nf.format( coords[ 5 ] ) + " )";
				break;

			case PathIterator.SEG_CLOSE:
				result = "SEG_CLOSE";
				break;

			default:
				throw new AssertionError( "Unrecognized segment type: " + segmentType );
		}

		return result;
	}
}
