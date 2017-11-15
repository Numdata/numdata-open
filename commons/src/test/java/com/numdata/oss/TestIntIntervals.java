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
package com.numdata.oss;

import java.util.*;

import com.numdata.oss.junit.*;
import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * Unit test for {@link IntIntervals}.
 *
 * @author Peter S. Heijnen
 */
public class TestIntIntervals
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestIntIntervals.class.getName();

	/**
	 * Test {@link IntIntervals#add(int, int)} method.
	 */
	@Test
	public void testAdd()
	{
		System.out.println( CLASS_NAME + ".testAdd" );

		final IntIntervals intIntervals = new IntIntervals();

		System.out.println( " - Test #1: add disjunct first" );
		intIntervals.add( 7, 9 );
		assertEquals( intIntervals, new int[][] { { 7, 9 } } );

		System.out.println( " - Test #2: add disjunct after last" );
		intIntervals.add( 10, 14 );
		assertEquals( intIntervals, new int[][] { { 7, 9 }, { 10, 14 } } );

		System.out.println( " - Test #3: add disjunct before first" );
		intIntervals.add( 3, 4 );
		assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );

		System.out.println( " - Test #4: add disjunct after last" );
		intIntervals.add( 18, 20 );
		assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 }, { 18, 20 } } );

		System.out.println( " - Test #5: add disjunct in middle" );
		intIntervals.add( 15, 16 );
		assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 }, { 15, 16 }, { 18, 20 } } );

		System.out.println( " - Test #6: merge touching first" );
		intIntervals.add( 2, 3 );
		assertEquals( intIntervals, new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 }, { 15, 16 }, { 18, 20 } } );

		System.out.println( " - Test #7: merge overlap first" );
		intIntervals.add( 3, 5 );
		assertEquals( intIntervals, new int[][] { { 2, 5 }, { 7, 9 }, { 10, 14 }, { 15, 16 }, { 18, 20 } } );

		System.out.println( " - Test #8: merge connecting" );
		intIntervals.add( 14, 15 );
		assertEquals( intIntervals, new int[][] { { 2, 5 }, { 7, 9 }, { 10, 16 }, { 18, 20 } } );

		System.out.println( " - Test #9: merge multiple overlap" );
		intIntervals.add( 4, 12 );
		assertEquals( intIntervals, new int[][] { { 2, 16 }, { 18, 20 } } );

		System.out.println( " - Test #10: merge extend" );
		intIntervals.add( 19, 21 );
		assertEquals( intIntervals, new int[][] { { 2, 16 }, { 18, 21 } } );

		System.out.println( " - Test #11: merge contain all" );
		intIntervals.add( 2, 21 );
		assertEquals( intIntervals, new int[][] { { 2, 21 } } );

		System.out.println( " - Test #12: add zero length" );
		intIntervals.add( 22, 22 );
		assertEquals( intIntervals, new int[][] { { 2, 21 } } );
	}

	/**
	 * Test {@link IntIntervals#remove(int, int)} method.
	 */
	@Test
	public void testRemove()
	{
		System.out.println( CLASS_NAME + ".testRemove" );

		System.out.println( " - Test #1: disjunct before" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 1, 2 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #2: disjunct touch first" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 1, 3 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #3: disjunct between" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 4, 7 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #4: disjunct touch last" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 14, 20 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #5: disjunct after" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 20, 30 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #6: overlap first start" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 1, 3 );
			assertEquals( intIntervals, new int[][] { { 3, 4 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #7: cover first" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 1, 5 );
			assertEquals( intIntervals, new int[][] { { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #8: overlap first end" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 3, 5 );
			assertEquals( intIntervals, new int[][] { { 2, 3 }, { 7, 9 }, { 10, 14 } } );
		}

		System.out.println( " - Test #9: overlap last start" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 9, 11 );
			assertEquals( intIntervals, new int[][] { { 2, 4 }, { 7, 9 }, { 11, 14 } } );
		}

		System.out.println( " - Test #10: cover last" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 9, 15 );
			assertEquals( intIntervals, new int[][] { { 2, 4 }, { 7, 9 } } );
		}

		System.out.println( " - Test #11: exact match last" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 10, 14 );
			assertEquals( intIntervals, new int[][] { { 2, 4 }, { 7, 9 } } );
		}

		System.out.println( " - Test #12: overlap last end" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 12, 20 );
			assertEquals( intIntervals, new int[][] { { 2, 4 }, { 7, 9 }, { 10, 12 } } );
		}

		System.out.println( " - Test #13: overlap + cover + overlap" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 4 }, { 7, 9 }, { 10, 14 } } );
			intIntervals.remove( 3, 12 );
			assertEquals( intIntervals, new int[][] { { 2, 3 }, { 12, 14 } } );
		}

		System.out.println( " - Test #14: split" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 5 }, { 6, 9 }, { 10, 14 } } );
			intIntervals.remove( 12, 13 );
			assertEquals( intIntervals, new int[][] { { 2, 5 }, { 6, 9 }, { 10, 12 }, { 13, 14 } } );
		}

		System.out.println( " - Test #15: remove zero length" );
		{
			final IntIntervals intIntervals = create( new int[][] { { 2, 5 }, { 6, 9 }, { 10, 14 } } );
			intIntervals.remove( 4, 4 );
			assertEquals( intIntervals, new int[][] { { 2, 5 }, { 6, 9 }, { 10, 14 } } );
		}
	}

	/**
	 * Assert intervals in {@link IntIntervals}.
	 *
	 * @param intervals Intervals.
	 *
	 * @return {@link IntIntervals}.
	 */
	@NotNull
	private static IntIntervals create( @NotNull final int[][] intervals )
	{
		final IntIntervals result = new IntIntervals();
		for ( final int[] interval : intervals )
		{
			result.add( interval[ 0 ], interval[ 1 ] );
		}

		final List<IntIntervals.Interval> resultIntervals = result.getIntervals();
		Assert.assertEquals( "Failed to properly create intervals", intervals.length, resultIntervals.size() );
		for ( int i = 0; i < resultIntervals.size(); i++ )
		{
			final IntIntervals.Interval interval = resultIntervals.get( i );
			Assert.assertEquals( "Unexpected interval[" + i + "].start after creation", intervals[ i ][ 0 ], interval.getStart() );
			Assert.assertEquals( "Unexpected interval[" + i + "].end after creation", intervals[ i ][ 1 ], interval.getEnd() );
		}

		return result;
	}

	/**
	 * Assert intervals in {@link IntIntervals}.
	 *
	 * @param intIntervals      {@link IntIntervals} whose content to check.
	 * @param expectedIntervals Expected intervals.
	 */
	private static void assertEquals( @NotNull final IntIntervals intIntervals, @NotNull final int[][] expectedIntervals )
	{
		final List<IntIntervals.Interval> intervals = intIntervals.getIntervals();
		final int[][] actualsInts = new int[ intervals.size() ][];
		for ( int i = 0; i < intervals.size(); i++ )
		{
			final IntIntervals.Interval interval = intervals.get( i );
			actualsInts[ i ] = new int[] { interval.getStart(), interval.getEnd() };
		}
		System.out.println( "    > expected: " + ArrayTools.toString( expectedIntervals ) );
		System.out.println( "    > actual  : " + ArrayTools.toString( actualsInts ) );
		ArrayTester.assertEquals( "Unexpected periods", "expectedIntervals", "actualIntervals", expectedIntervals, actualsInts );
	}
}
