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
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@code ArrayTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestArrayTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestArrayTools.class.getName();

	/**
	 * Array with no elements.
	 */
	private static final int[] EMPTY_ARRAY = new int[ 0 ];

	/**
	 * Array with 3 elements.
	 */
	private static final char[] XYZ_ARRAY = { 'X', 'Y', 'Z' };

	/**
	 * Empty {@code List} instance.
	 */
	private static final List<?> EMPTY_LIST = Collections.emptyList();

	/**
	 * A {@code List} instance with 3 elements.
	 */
	private static final List<String> ABC_LIST = new ArrayList<String>( Arrays.asList( "A", "B", "C" ) );

	/**
	 * Test the {@link ArrayTools#clear} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testClear()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testClear()" );

		class Test
		{
			final Object array;

			final Object out;

			private Test( final Object in, final Object out )
			{
				array = in;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null ),
			/* Test #2  */ new Test( new boolean[] { true, false, true }, new boolean[] { false, false, false } ),
			/* Test #3  */ new Test( new byte[] {}, new byte[] {} ),
			/* Test #4  */ new Test( new char[] { 1 }, new char[] { 0 } ),
			/* Test #5  */ new Test( new double[] { 2, 1 }, new double[] { 0, 0 } ),
			/* Test #6  */ new Test( new float[] { 3, 2, 1 }, new float[] { 0, 0, 0 } ),
			/* Test #7  */ new Test( new int[] { 4, 3, 2, 1 }, new int[] { 0, 0, 0, 0 } ),
			/* Test #8  */ new Test( new long[] { 5, 4, 3, 2, 1 }, new long[] { 0, 0, 0, 0, 0 } ),
			/* Test #9  */ new Test( new Object[] { "hello" }, new Object[] { null } ),
			/* Test #10 */ new Test( new String[] { "hello" }, new String[] { null } ),
			/* Test #11 */ new Test( new TestArrayTools[] {}, new TestArrayTools[] {} ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			ArrayTools.clear( test.array );
			ArrayTester.assertEquals( description, "expected", "actual", test.out, test.array );
		}
	}

	/**
	 * Test the {@link ArrayTools#clone} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testClone()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testClone()" );

		class Test
		{
			final Object array;

			final Object out;

			private Test( final Object in, final Object out )
			{
				array = in;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null ),
			/* Test #2  */ new Test( new boolean[] { true, false, true }, new boolean[] { true, false, true } ),
			/* Test #3  */ new Test( new byte[] {}, new byte[] {} ),
			/* Test #4  */ new Test( new char[] { 1 }, new char[] { 1 } ),
			/* Test #5  */ new Test( new double[] { 2, 1 }, new double[] { 2, 1 } ),
			/* Test #6  */ new Test( new float[] { 3, 2, 1 }, new float[] { 3, 2, 1 } ),
			/* Test #7  */ new Test( new int[] { 4, 3, 2, 1 }, new int[] { 4, 3, 2, 1 } ),
			/* Test #8  */ new Test( new long[] { 5, 4, 3, 2, 1 }, new long[] { 5, 4, 3, 2, 1 } ),
			/* Test #9  */ new Test( new Object[] { "hello" }, new Object[] { "hello" } ),
			/* Test #10 */ new Test( new String[] { "hello" }, new String[] { "hello" } ),
			/* Test #11 */ new Test( new TestArrayTools[] {}, new TestArrayTools[] {} ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			ArrayTools.clone( test.array );
			ArrayTester.assertEquals( description, "expected", "actual", test.out, test.array );
		}
	}

	/**
	 * Test the {@link ArrayTools#equals} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testEquals()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testEquals()" );

		assertEquals( "Test #1", true, ArrayTools.equals( null, null ) );
		assertEquals( "Test #2", true, ArrayTools.equals( new boolean[] { true, false, true }, new boolean[] { true, false, true } ) );
		assertEquals( "Test #3", false, ArrayTools.equals( new boolean[] { true, false, true }, new boolean[] { true, false } ) );
		assertEquals( "Test #4", false, ArrayTools.equals( new boolean[] { true, false, true }, new boolean[] { true, false, false } ) );
		assertEquals( "Test #5", true, ArrayTools.equals( new byte[] {}, new byte[] {} ) );
		assertEquals( "Test #6", false, ArrayTools.equals( new byte[] {}, new byte[] { (byte)0 } ) );
		assertEquals( "Test #7", true, ArrayTools.equals( new char[] { 1 }, new char[] { 1 } ) );
		assertEquals( "Test #8", false, ArrayTools.equals( new char[] {}, new char[] { 1 } ) );
		assertEquals( "Test #9", true, ArrayTools.equals( new double[] { 2, 1 }, new double[] { 2, 1 } ) );
		assertEquals( "Test #10", false, ArrayTools.equals( null, new double[] { 2, 1 } ) );
		assertEquals( "Test #11", true, ArrayTools.equals( new float[] { 3, 2, 1 }, new float[] { 3, 2, 1 } ) );
		assertEquals( "Test #12", false, ArrayTools.equals( new float[] { 0, 2, 1 }, new float[] { 3, 2, 1 } ) );
		assertEquals( "Test #13", true, ArrayTools.equals( new int[] { 4, 3, 2, 1 }, new int[] { 4, 3, 2, 1 } ) );
		assertEquals( "Test #14", false, ArrayTools.equals( new int[] { 4, 3, 2, 1 }, new int[] { 0, 3, 2, 1 } ) );
		assertEquals( "Test #15", true, ArrayTools.equals( new long[] { 5, 4, 3, 2, 1 }, new long[] { 5, 4, 3, 2, 1 } ) );
		assertEquals( "Test #16", false, ArrayTools.equals( new long[] { 5, 4, 3, 2, 0 }, new long[] { 5, 4, 3, 2, 1 } ) );
		assertEquals( "Test #17", false, ArrayTools.equals( new long[] { 5, 4, 3, 2, 1 }, new long[] { 5, 4, 3, 2, 0 } ) );
		assertEquals( "Test #18", true, ArrayTools.equals( new Object[] { "hello" }, new Object[] { "hello" } ) );
		assertEquals( "Test #19", false, ArrayTools.equals( new Object[] { "hello" }, new Object[] { null } ) );
		assertEquals( "Test #20", true, ArrayTools.equals( new String[] { "hello" }, new String[] { "hello" } ) );
		assertEquals( "Test #21", false, ArrayTools.equals( new String[] { null }, new String[] { "hello" } ) );
		assertEquals( "Test #22", false, ArrayTools.equals( new String[] { "hello" }, new String[] { "bye" } ) );
		assertEquals( "Test #23", true, ArrayTools.equals( new TestArrayTools[] {}, new TestArrayTools[] {} ) );
		assertEquals( "Test #24", true, ArrayTools.equals( new TestArrayTools[] { null }, new TestArrayTools[] { null } ) );
		assertEquals( "Test #25", true, ArrayTools.equals( new int[][] { {}, { 1 }, { 2, 3 }, { 4 } }, new int[][] { {}, { 1 }, { 2, 3 }, { 4 } } ) );
		assertEquals( "Test #26", false, ArrayTools.equals( new int[][] { {}, { 1 }, { 2, 3 }, { 4 } }, new int[][] { {}, { 1 }, { 2 }, { 4 } } ) );
		assertEquals( "Test #27", false, ArrayTools.equals( new int[][] { {}, { 1 }, { 2, 3 }, { 4 } }, new int[][] { {}, { 1 }, null, { 4 } } ) );
		assertEquals( "Test #28", false, ArrayTools.equals( new int[][] { {}, { 1 }, { 2, 3 }, { 4 } }, new int[][] { {}, { 1 }, { 2, 3 }, {} } ) );
		assertEquals( "Test #29", false, ArrayTools.equals( new int[][] { {}, { 1 }, { 2, 3 }, { 4 } }, new int[][] { { 0 }, { 1 }, { 2, 3 }, { 4 } } ) );
	}

	/**
	 * Test the {@link ArrayTools#getLength} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetLength()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetLength()" );

		assertEquals( "Test #1", 0, ArrayTools.getLength( null ) );
		assertEquals( "Test #2", 0, ArrayTools.getLength( new Object[] {} ) );
		assertEquals( "Test #3", 1, ArrayTools.getLength( new Object[] { null } ) );
		assertEquals( "Test #4", 3, ArrayTools.getLength( new int[][] { { 1 }, { 2, 3 }, { 4 } } ) );
	}

	/**
	 * Test the {@link ArrayTools#setLength(Object, Class, int)} method. for
	 * static arrays.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testSetLength_static()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testSetLength_static()" );

		class Test
		{
			final Object array;

			final Class<?> componentType;

			final int length;

			final Object out;

			private Test( final Object array, final Class<?> componentType, final int length, final Object out )
			{
				this.array = array;
				this.componentType = componentType;
				this.length = length;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null, 0, new Object[ 0 ] ),
			/* Test #2  */ new Test( null, null, 1, new Object[ 1 ] ),
			/* Test #3  */ new Test( null, null, 2, new Object[ 2 ] ),
			/* Test #4  */ new Test( null, int.class, 0, new int[ 0 ] ),
			/* Test #5  */ new Test( null, int.class, 1, new int[ 1 ] ),
			/* Test #6  */ new Test( null, int.class, 2, new int[ 2 ] ),
			/* Test #7  */ new Test( new int[ 0 ], null, 0, new int[ 0 ] ),
			/* Test #8  */ new Test( new int[ 0 ], null, 1, new int[ 1 ] ),
			/* Test #9  */ new Test( new int[ 0 ], null, 2, new int[ 2 ] ),
			/* Test #10 */ new Test( new int[ 0 ], int.class, 0, new int[ 0 ] ),
			/* Test #11 */ new Test( new int[ 0 ], int.class, 1, new int[ 1 ] ),
			/* Test #12 */ new Test( new int[ 0 ], int.class, 2, new int[ 2 ] ),
			/* Test #13 */ new Test( new int[] { 1, 2 }, null, 0, new int[] {} ),
			/* Test #14 */ new Test( new int[] { 1, 2 }, null, 1, new int[] { 1 } ),
			/* Test #15 */ new Test( new int[] { 1, 2 }, null, 2, new int[] { 1, 2 } ),
			/* Test #16 */ new Test( new int[] { 1, 2 }, null, 3, new int[] { 1, 2, 0 } ),
			/* Test #17 */ new Test( new int[] { 1, 2 }, int.class, 0, new int[] {} ),
			/* Test #18 */ new Test( new int[] { 1, 2 }, int.class, 1, new int[] { 1 } ),
			/* Test #19 */ new Test( new int[] { 1, 2 }, int.class, 2, new int[] { 1, 2 } ),
			/* Test #20 */ new Test( new int[] { 1, 2 }, int.class, 3, new int[] { 1, 2, 0 } ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Object result = ArrayTools.setLength( test.array, test.componentType, test.length );
			ArrayTester.assertEquals( description, "expected", "actual", test.out, result );
		}
	}

	/**
	 * Test the {@link ArrayTools#setLength(Object, Class, int, int, int)}
	 * method. for dynamic arrays.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testSetLength_dynamic()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testSetLength_dynamic()" );

		class Test
		{
			final Object array;

			final Class<?> componentType;

			final int elementCount;

			final int incrementSize;

			final int length;

			final Object out;

			private Test( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final int length, final Object out )
			{
				this.array = array;
				this.componentType = componentType;
				this.elementCount = elementCount;
				this.incrementSize = incrementSize;
				this.length = length;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null, 0, 4, 0, null ),
			/* Test #2  */ new Test( null, null, 0, 4, 1, new Object[ 4 ] ),
			/* Test #3  */ new Test( null, null, 0, 4, 2, new Object[ 4 ] ),
			/* Test #4  */ new Test( null, int.class, 0, 4, 0, null ),
			/* Test #5  */ new Test( null, int.class, 0, 4, 1, new int[ 4 ] ),
			/* Test #6  */ new Test( null, int.class, 0, 4, 2, new int[ 4 ] ),
			/* Test #7  */ new Test( new int[ 0 ], null, 0, 4, 0, new int[ 0 ] ),
			/* Test #8  */ new Test( new int[ 0 ], null, 0, 4, 1, new int[ 4 ] ),
			/* Test #9  */ new Test( new int[ 0 ], null, 0, 4, 2, new int[ 4 ] ),
			/* Test #10 */ new Test( new int[ 0 ], int.class, 0, 4, 0, new int[ 0 ] ),
			/* Test #11 */ new Test( new int[ 0 ], int.class, 0, 4, 1, new int[ 4 ] ),
			/* Test #12 */ new Test( new int[ 0 ], int.class, 0, 4, 2, new int[ 4 ] ),
			/* Test #13 */ new Test( new int[] { 1, 2 }, null, 2, 4, 0, new int[] { 0, 0 } ),
			/* Test #14 */ new Test( new int[] { 1, 2 }, null, 2, 4, 1, new int[] { 1, 0 } ),
			/* Test #15 */ new Test( new int[] { 1, 2 }, null, 2, 4, 2, new int[] { 1, 2 } ),
			/* Test #16 */ new Test( new int[] { 1, 2 }, null, 2, 4, 3, new int[] { 1, 2, 0, 0 } ),
			/* Test #17 */ new Test( new int[] { 1, 2 }, int.class, 2, 4, 0, new int[] { 0, 0 } ),
			/* Test #18 */ new Test( new int[] { 1, 2 }, int.class, 2, 4, 1, new int[] { 1, 0 } ),
			/* Test #19 */ new Test( new int[] { 1, 2 }, int.class, 2, 4, 2, new int[] { 1, 2 } ),
			/* Test #20 */ new Test( new int[] { 1, 2 }, int.class, 2, 4, 3, new int[] { 1, 2, 0, 0 } ),
			/* Test #21 */ new Test( new int[] { 1, 2, 0, 0 }, null, 2, 4, 0, new int[] { 0, 0, 0, 0 } ),
			/* Test #22 */ new Test( new int[] { 1, 2, 0, 0 }, null, 2, 4, 1, new int[] { 1, 0, 0, 0 } ),
			/* Test #23 */ new Test( new int[] { 1, 2, 0, 0 }, null, 2, 4, 2, new int[] { 1, 2, 0, 0 } ),
			/* Test #24 */ new Test( new int[] { 1, 2, 0, 0 }, null, 2, 4, 3, new int[] { 1, 2, 0, 0 } ),
			/* Test #25 */ new Test( new int[] { 1, 2, 3, 4 }, null, 2, 4, 0, new int[] { 0, 0, 3, 4 } ),
			/* Test #26 */ new Test( new int[] { 1, 2, 3, 4 }, null, 2, 4, 1, new int[] { 1, 0, 3, 4 } ),
			/* Test #27 */ new Test( new int[] { 1, 2, 3, 4 }, null, 2, 4, 2, new int[] { 1, 2, 3, 4 } ),
			/* Test #28 */ new Test( new int[] { 1, 2, 3, 4 }, null, 2, 4, 3, new int[] { 1, 2, 0, 4 } ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Object result = ArrayTools.setLength( test.array, test.componentType, test.elementCount, test.incrementSize, test.length );
			ArrayTester.assertEquals( description, "expected", "actual", test.out, result );
		}
	}

	/**
	 * Test the {@link ArrayTools#ensureLength} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testEnsureLength()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testEnsureLength()" );

		class Test
		{
			final Object array;

			final Class<?> componentType;

			final int incrementSize;

			final int minimumLength;

			final Object out;

			private Test( final Object array, final Class<?> componentType, final int incrementSize, final int minimumLength, final Object out )
			{
				this.array = array;
				this.componentType = componentType;
				this.incrementSize = incrementSize;
				this.minimumLength = minimumLength;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null, 4, 0, null ),
			/* Test #2  */ new Test( null, null, 4, 1, new Object[ 4 ] ),
			/* Test #3  */ new Test( null, null, 4, 2, new Object[ 4 ] ),
			/* Test #4  */ new Test( null, int.class, 1, 0, null ),
			/* Test #5  */ new Test( null, int.class, 1, 1, new int[ 1 ] ),
			/* Test #6  */ new Test( null, int.class, 1, 2, new int[ 2 ] ),
			/* Test #7  */ new Test( null, int.class, 1, 3, new int[ 3 ] ),
			/* Test #8  */ new Test( null, int.class, 1, 4, new int[ 4 ] ),
			/* Test #9  */ new Test( null, int.class, 1, 5, new int[ 5 ] ),
			/* Test #10 */ new Test( null, int.class, 1, 6, new int[ 6 ] ),
			/* Test #11 */ new Test( null, int.class, 1, 7, new int[ 7 ] ),
			/* Test #12 */ new Test( null, int.class, 1, 8, new int[ 8 ] ),
			/* Test #13 */ new Test( null, int.class, 1, 9, new int[ 9 ] ),
			/* Test #14 */ new Test( null, int.class, 1, 10, new int[ 10 ] ),
			/* Test #15 */ new Test( null, int.class, 1, 20, new int[ 20 ] ),
			/* Test #16 */ new Test( null, int.class, 1, 30, new int[ 30 ] ),
			/* Test #17 */ new Test( null, int.class, 1, 40, new int[ 40 ] ),
			/* Test #18 */ new Test( null, int.class, 1, 50, new int[ 50 ] ),
			/* Test #19 */ new Test( null, int.class, 1, 100, new int[ 100 ] ),
			/* Test #20 */ new Test( null, int.class, 1, 1000, new int[ 1000 ] ),
			/* Test #21 */ new Test( null, int.class, 4, 0, null ),
			/* Test #22 */ new Test( null, int.class, 4, 1, new int[ 4 ] ),
			/* Test #23 */ new Test( null, int.class, 4, 2, new int[ 4 ] ),
			/* Test #24 */ new Test( null, int.class, 4, 3, new int[ 4 ] ),
			/* Test #25 */ new Test( null, int.class, 4, 4, new int[ 4 ] ),
			/* Test #26 */ new Test( null, int.class, 4, 5, new int[ 8 ] ),
			/* Test #27 */ new Test( null, int.class, 4, 6, new int[ 8 ] ),
			/* Test #28 */ new Test( null, int.class, 4, 7, new int[ 8 ] ),
			/* Test #29 */ new Test( null, int.class, 4, 8, new int[ 8 ] ),
			/* Test #30 */ new Test( null, int.class, 4, 9, new int[ 12 ] ),
			/* Test #31 */ new Test( null, int.class, 4, 10, new int[ 12 ] ),
			/* Test #32 */ new Test( null, int.class, 4, 20, new int[ 20 ] ),
			/* Test #33 */ new Test( null, int.class, 4, 30, new int[ 32 ] ),
			/* Test #34 */ new Test( null, int.class, 4, 40, new int[ 40 ] ),
			/* Test #35 */ new Test( null, int.class, 4, 50, new int[ 52 ] ),
			/* Test #36 */ new Test( null, int.class, 4, 100, new int[ 100 ] ),
			/* Test #37 */ new Test( null, int.class, 4, 1000, new int[ 1000 ] ),
			/* Test #38 */ new Test( null, int.class, -2, 0, null ),
			/* Test #39 */ new Test( null, int.class, -2, 1, new int[ 1 ] ),
			/* Test #40 */ new Test( null, int.class, -2, 2, new int[ 2 ] ),
			/* Test #41 */ new Test( null, int.class, -2, 3, new int[ 4 ] ),
			/* Test #42 */ new Test( null, int.class, -2, 4, new int[ 4 ] ),
			/* Test #43 */ new Test( null, int.class, -2, 5, new int[ 8 ] ),
			/* Test #44 */ new Test( null, int.class, -2, 6, new int[ 8 ] ),
			/* Test #45 */ new Test( null, int.class, -2, 7, new int[ 8 ] ),
			/* Test #46 */ new Test( null, int.class, -2, 8, new int[ 8 ] ),
			/* Test #47 */ new Test( null, int.class, -2, 9, new int[ 16 ] ),
			/* Test #48 */ new Test( null, int.class, -2, 10, new int[ 16 ] ),
			/* Test #49 */ new Test( null, int.class, -2, 20, new int[ 32 ] ),
			/* Test #50 */ new Test( null, int.class, -2, 30, new int[ 32 ] ),
			/* Test #51 */ new Test( null, int.class, -2, 40, new int[ 64 ] ),
			/* Test #52 */ new Test( null, int.class, -2, 50, new int[ 64 ] ),
			/* Test #53 */ new Test( null, int.class, -2, 100, new int[ 128 ] ),
			/* Test #54 */ new Test( null, int.class, -2, 1000, new int[ 1024 ] ),
			/* Test #55 */ new Test( null, int.class, -3, 0, null ),
			/* Test #56 */ new Test( null, int.class, -3, 1, new int[ 1 ] ),
			/* Test #57 */ new Test( null, int.class, -3, 2, new int[ 3 ] ),
			/* Test #58 */ new Test( null, int.class, -3, 3, new int[ 3 ] ),
			/* Test #59 */ new Test( null, int.class, -3, 4, new int[ 9 ] ),
			/* Test #60 */ new Test( null, int.class, -3, 5, new int[ 9 ] ),
			/* Test #61 */ new Test( null, int.class, -3, 6, new int[ 9 ] ),
			/* Test #62 */ new Test( null, int.class, -3, 7, new int[ 9 ] ),
			/* Test #63 */ new Test( null, int.class, -3, 8, new int[ 9 ] ),
			/* Test #64 */ new Test( null, int.class, -3, 9, new int[ 9 ] ),
			/* Test #65 */ new Test( null, int.class, -3, 10, new int[ 27 ] ),
			/* Test #66 */ new Test( null, int.class, -3, 20, new int[ 27 ] ),
			/* Test #67 */ new Test( null, int.class, -3, 30, new int[ 81 ] ),
			/* Test #68 */ new Test( null, int.class, -3, 40, new int[ 81 ] ),
			/* Test #69 */ new Test( null, int.class, -3, 50, new int[ 81 ] ),
			/* Test #70 */ new Test( null, int.class, -3, 100, new int[ 243 ] ),
			/* Test #71 */ new Test( null, int.class, -3, 1000, new int[ 2187 ] ),
			/* Test #72 */ new Test( new int[ 0 ], null, 4, 0, new int[ 0 ] ),
			/* Test #73 */ new Test( new int[ 0 ], null, 4, 1, new int[ 4 ] ),
			/* Test #74 */ new Test( new int[ 0 ], null, 4, 2, new int[ 4 ] ),
			/* Test #75 */ new Test( new int[] { 1, 2 }, null, 4, 0, new int[] { 1, 2 } ),
			/* Test #76 */ new Test( new int[] { 1, 2 }, null, 4, 1, new int[] { 1, 2 } ),
			/* Test #77 */ new Test( new int[] { 1, 2 }, null, 4, 2, new int[] { 1, 2 } ),
			/* Test #78 */ new Test( new int[] { 1, 2 }, null, 4, 3, new int[] { 1, 2, 0, 0 } ),
			/* Test #79 */ new Test( new int[] { 1, 2 }, null, 4, 4, new int[] { 1, 2, 0, 0 } ),
			/* Test #80 */ new Test( new int[] { 1, 2, 3, 4 }, null, 4, 0, new int[] { 1, 2, 3, 4 } ),
			/* Test #81 */ new Test( new int[] { 1, 2, 3, 4 }, null, 4, 1, new int[] { 1, 2, 3, 4 } ),
			/* Test #82 */ new Test( new int[] { 1, 2, 3, 4 }, null, 4, 2, new int[] { 1, 2, 3, 4 } ),
			/* Test #83 */ new Test( new int[] { 1, 2, 3, 4 }, null, 4, 3, new int[] { 1, 2, 3, 4 } ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Object result = ArrayTools.ensureLength( test.array, test.componentType, test.incrementSize, test.minimumLength );
			ArrayTester.assertEquals( description, "expected", "actual", test.out, result );
		}
	}

	/**
	 * Test the {@link ArrayTools#get} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGet()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGet()" );

		class Test
		{
			final Object array;

			final int elementCount;

			final int index;

			final Object out;

			private Test( final Object array, final int elementCount, final int index, final Object out )
			{
				this.array = array;
				this.elementCount = elementCount;
				this.index = index;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, 0, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #2  */ new Test( null, 1, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #3  */ new Test( EMPTY_ARRAY, -1, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #4  */ new Test( EMPTY_ARRAY, 0, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #5  */ new Test( EMPTY_ARRAY, 1, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #6  */ new Test( EMPTY_LIST, -1, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #7  */ new Test( EMPTY_LIST, 0, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #8  */ new Test( EMPTY_LIST, 1, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #9  */ new Test( XYZ_ARRAY, -1, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #10 */ new Test( XYZ_ARRAY, -1, 0, 'X' ),
			/* Test #11 */ new Test( XYZ_ARRAY, -1, 1, 'Y' ),
			/* Test #12 */ new Test( XYZ_ARRAY, -1, 2, 'Z' ),
			/* Test #13 */ new Test( XYZ_ARRAY, -1, 3, ArrayIndexOutOfBoundsException.class ),
			/* Test #14 */ new Test( XYZ_ARRAY, 0, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #15 */ new Test( XYZ_ARRAY, 0, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #16 */ new Test( XYZ_ARRAY, 0, 1, ArrayIndexOutOfBoundsException.class ),
			/* Test #17 */ new Test( XYZ_ARRAY, 1, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #18 */ new Test( XYZ_ARRAY, 1, 0, 'X' ),
			/* Test #19 */ new Test( XYZ_ARRAY, 1, 1, ArrayIndexOutOfBoundsException.class ),
			/* Test #20 */ new Test( ABC_LIST, -1, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #21 */ new Test( ABC_LIST, -1, 0, "A" ),
			/* Test #22 */ new Test( ABC_LIST, -1, 1, "B" ),
			/* Test #23 */ new Test( ABC_LIST, -1, 2, "C" ),
			/* Test #24 */ new Test( ABC_LIST, -1, 3, ArrayIndexOutOfBoundsException.class ),
			/* Test #25 */ new Test( ABC_LIST, 0, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #26 */ new Test( ABC_LIST, 0, 0, ArrayIndexOutOfBoundsException.class ),
			/* Test #27 */ new Test( ABC_LIST, 0, 1, ArrayIndexOutOfBoundsException.class ),
			/* Test #28 */ new Test( ABC_LIST, 1, -1, ArrayIndexOutOfBoundsException.class ),
			/* Test #29 */ new Test( ABC_LIST, 1, 0, "A" ),
			/* Test #30 */ new Test( ABC_LIST, 1, 1, ArrayIndexOutOfBoundsException.class ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			Class<?> expectedException = null;
			if ( ( test.out instanceof Class ) && Exception.class.isAssignableFrom( (Class<?>)test.out ) )
			{
				expectedException = (Class<?>)test.out;
			}

			try
			{
				final Object result = ArrayTools.get( test.array, test.elementCount, test.index );
				if ( expectedException != null )
				{
					fail( description + " should have thrown exception" );
				}

				assertEquals( description, test.out, result );
			}
			catch ( final Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception", expectedException.getName(), e.getClass().getName() );
			}
		}
	}

	/**
	 * Test the {@link ArrayTools#getSafely} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetSafely()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetSafely()" );

		assertEquals( "Test #1", null, ArrayTools.getSafely( null, 0, 0 ) );
		assertEquals( "Test #2", null, ArrayTools.getSafely( null, 1, 0 ) );
		assertEquals( "Test #3", null, ArrayTools.getSafely( EMPTY_ARRAY, -1, 0 ) );
		assertEquals( "Test #4", null, ArrayTools.getSafely( EMPTY_ARRAY, 0, 0 ) );
		assertEquals( "Test #5", null, ArrayTools.getSafely( EMPTY_ARRAY, 1, 0 ) );
		assertEquals( "Test #6", null, ArrayTools.getSafely( EMPTY_LIST, -1, 0 ) );
		assertEquals( "Test #7", null, ArrayTools.getSafely( EMPTY_LIST, 0, 0 ) );
		assertEquals( "Test #8", null, ArrayTools.getSafely( EMPTY_LIST, 1, 0 ) );
		assertEquals( "Test #9", null, ArrayTools.getSafely( XYZ_ARRAY, -1, -1 ) );
		assertEquals( "Test #10", 'X', ArrayTools.getSafely( XYZ_ARRAY, -1, 0 ) );
		assertEquals( "Test #11", 'Y', ArrayTools.getSafely( XYZ_ARRAY, -1, 1 ) );
		assertEquals( "Test #12", 'Z', ArrayTools.getSafely( XYZ_ARRAY, -1, 2 ) );
		assertEquals( "Test #13", null, ArrayTools.getSafely( XYZ_ARRAY, -1, 3 ) );
		assertEquals( "Test #14", null, ArrayTools.getSafely( XYZ_ARRAY, 0, -1 ) );
		assertEquals( "Test #15", null, ArrayTools.getSafely( XYZ_ARRAY, 0, 0 ) );
		assertEquals( "Test #16", null, ArrayTools.getSafely( XYZ_ARRAY, 0, 1 ) );
		assertEquals( "Test #17", null, ArrayTools.getSafely( XYZ_ARRAY, 1, -1 ) );
		assertEquals( "Test #18", 'X', ArrayTools.getSafely( XYZ_ARRAY, 1, 0 ) );
		assertEquals( "Test #19", null, ArrayTools.getSafely( XYZ_ARRAY, 1, 1 ) );
		assertEquals( "Test #20", null, ArrayTools.getSafely( ABC_LIST, -1, -1 ) );
		assertEquals( "Test #21", "A", ArrayTools.getSafely( ABC_LIST, -1, 0 ) );
		assertEquals( "Test #22", "B", ArrayTools.getSafely( ABC_LIST, -1, 1 ) );
		assertEquals( "Test #23", "C", ArrayTools.getSafely( ABC_LIST, -1, 2 ) );
		assertEquals( "Test #24", null, ArrayTools.getSafely( ABC_LIST, -1, 3 ) );
		assertEquals( "Test #25", null, ArrayTools.getSafely( ABC_LIST, 0, -1 ) );
		assertEquals( "Test #26", null, ArrayTools.getSafely( ABC_LIST, 0, 0 ) );
		assertEquals( "Test #27", null, ArrayTools.getSafely( ABC_LIST, 0, 1 ) );
		assertEquals( "Test #28", null, ArrayTools.getSafely( ABC_LIST, 1, -1 ) );
		assertEquals( "Test #29", "A", ArrayTools.getSafely( ABC_LIST, 1, 0 ) );
		assertEquals( "Test #30", null, ArrayTools.getSafely( ABC_LIST, 1, 1 ) );
	}

	/**
	 * Test the {@link ArrayTools#getElementCount} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetElementCount()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetElementCount()" );

		class Test
		{
			final Object array;

			final int elementCount;

			final int out;

			private Test( final Object array, final int elementCount, final int out )
			{
				this.array = array;
				this.elementCount = elementCount;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( null, -1, 0 ),
			/* Test #2  */ new Test( null, 0, 0 ),
			/* Test #3  */ new Test( null, 1, 0 ),
			/* Test #4  */ new Test( EMPTY_ARRAY, -1, 0 ),
			/* Test #5  */ new Test( EMPTY_ARRAY, 0, 0 ),
			/* Test #6  */ new Test( EMPTY_ARRAY, 1, 0 ),
			/* Test #7  */ new Test( XYZ_ARRAY, -1, 3 ),
			/* Test #8  */ new Test( XYZ_ARRAY, 0, 0 ),
			/* Test #9  */ new Test( XYZ_ARRAY, 1, 1 ),
			/* Test #10 */ new Test( XYZ_ARRAY, 2, 2 ),
			/* Test #11 */ new Test( XYZ_ARRAY, 3, 3 ),
			/* Test #12 */ new Test( XYZ_ARRAY, 4, 3 ),
			/* Test #13 */ new Test( ABC_LIST, -1, 3 ),
			/* Test #14 */ new Test( ABC_LIST, 0, 0 ),
			/* Test #15 */ new Test( ABC_LIST, 1, 1 ),
			/* Test #16 */ new Test( ABC_LIST, 2, 2 ),
			/* Test #17 */ new Test( ABC_LIST, 3, 3 ),
			/* Test #18 */ new Test( ABC_LIST, 4, 3 ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final int result = ArrayTools.getElementCount( test.array, test.elementCount );
			assertEquals( description, test.out, result );
		}
	}

	/**
	 * Test the {@link ArrayTools#append} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testAppend()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testAppend()" );

		class Test
		{
			final Object array;

			final Class<?> componentType;

			final int elementCount;

			final int incrementSize;

			final Object element;

			final Object out;

			private Test( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final Object element, final Object out )
			{
				this.array = array;
				this.componentType = componentType;
				this.elementCount = elementCount;
				this.incrementSize = incrementSize;
				this.element = element;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Integer six = 6;

		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null, -1, -1, six, new Object[] { six } ),
			/* Test #2  */ new Test( null, int.class, -1, -1, six, new int[] { 6 } ),
			/* Test #3  */ new Test( new short[ 0 ], int.class, -1, -1, six, new int[] { 6 } ),
			/* Test #4  */ new Test( new short[ 1 ], int.class, -1, -1, six, ArrayStoreException.class ),
			/* Test #5  */ new Test( new int[] {}, int.class, -1, -1, six, new int[] { 6 } ),
			/* Test #6  */ new Test( new int[] {}, short.class, -1, -1, six, IllegalArgumentException.class ),
			/* Test #7  */ new Test( new int[] {}, null, -1, -1, six, new int[] { 6 } ),
			/* Test #8  */ new Test( new int[] { 0 }, null, -1, -1, six, new int[] { 0, 6 } ),
			/* Test #9  */ new Test( new int[] { 1, 2 }, null, -1, -1, six, new int[] { 1, 2, 6 } ),
			/* Test #10 */ new Test( new int[] {}, null, -1, 4, six, new int[] { 6, 0, 0, 0 } ),
			/* Test #11 */ new Test( new int[] { 0 }, null, -1, 4, six, new int[] { 0, 6, 0, 0 } ),
			/* Test #12 */ new Test( new int[] { 1, 2 }, null, -1, 4, six, new int[] { 1, 2, 6, 0 } ),
			/* Test #13 */ new Test( new int[] {}, null, 0, 4, six, new int[] { 6, 0, 0, 0 } ),
			/* Test #14 */ new Test( new int[] { 0 }, null, 0, 4, six, new int[] { 6 } ),
			/* Test #15 */ new Test( new int[] { 1, 2 }, null, 0, 4, six, new int[] { 6, 2 } ),
			/* Test #16 */ new Test( new int[] {}, null, 1, 4, six, new int[] { 6, 0, 0, 0 } ),
			/* Test #17 */ new Test( new int[] { 0 }, null, 1, 4, six, new int[] { 0, 6, 0, 0 } ),
			/* Test #18 */ new Test( new int[] { 1, 2 }, null, 1, 4, six, new int[] { 1, 6 } ),
			/* Test #19 */ new Test( new int[] {}, null, -1, -2, six, new int[] { 6 } ),
			/* Test #20 */ new Test( new int[] { 0 }, null, -1, -2, six, new int[] { 0, 6 } ),
			/* Test #21 */ new Test( new int[] { 1, 2 }, null, -1, -2, six, new int[] { 1, 2, 6, 0 } ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			Class<?> expectedException = null;
			if ( ( test.out instanceof Class ) && Exception.class.isAssignableFrom( (Class<?>)test.out ) )
			{
				expectedException = (Class<?>)test.out;
			}
			try
			{
				final Object result = ArrayTools.append( test.array, test.componentType, test.elementCount, test.incrementSize, test.element );
				if ( expectedException != null )
				{
					fail( description + " should have thrown exception" );
				}

				ArrayTester.assertEquals( description, "expected", "actual", test.out, result );
			}
			catch ( final Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception", expectedException.getName(), e.getClass().getName() );
			}
		}

		/*
		 * Test overloaded methods for appending basic types.
		 */
		final boolean[] boolean1 = { false };
		final boolean[] boolean2 = { false, true };
		ArrayTester.assertEquals( "Test 'boolean' append() #1", "expected", "actual", boolean1, ArrayTools.append( null, boolean1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'boolean' append() #2", "expected", "actual", boolean2, ArrayTools.append( boolean1, null, -1, -1, boolean2[ 1 ] ) );

		final byte[] byte1 = { (byte)1 };
		final byte[] byte2 = { (byte)1, (byte)2 };
		ArrayTester.assertEquals( "Test 'byte' append() #1", "expected", "actual", byte1, ArrayTools.append( null, byte1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'byte' append() #2", "expected", "actual", byte2, ArrayTools.append( byte1, null, -1, -1, byte2[ 1 ] ) );

		final char[] char1 = { '2' };
		final char[] char2 = { '2', '3' };
		ArrayTester.assertEquals( "Test 'char' append() #1", "expected", "actual", char1, ArrayTools.append( null, char1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'char' append() #2", "expected", "actual", char2, ArrayTools.append( char1, null, -1, -1, char2[ 1 ] ) );

		final double[] double1 = { 3 };
		final double[] double2 = { 3, 4 };
		ArrayTester.assertEquals( "Test 'double' append() #1", "expected", "actual", double1, ArrayTools.append( null, double1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'double' append() #2", "expected", "actual", double2, ArrayTools.append( double1, null, -1, -1, double2[ 1 ] ) );

		final float[] float1 = { 4.0f };
		final float[] float2 = { 4, 5 };
		ArrayTester.assertEquals( "Test 'float' append() #1", "expected", "actual", float1, ArrayTools.append( null, float1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'float' append() #2", "expected", "actual", float2, ArrayTools.append( float1, null, -1, -1, float2[ 1 ] ) );

		final int[] int1 = { 5 };
		final int[] int2 = { 5, 6 };
		ArrayTester.assertEquals( "Test 'int' append() #1", "expected", "actual", int1, ArrayTools.append( null, int1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'int' append() #2", "expected", "actual", int2, ArrayTools.append( int1, null, -1, -1, int2[ 1 ] ) );

		final long[] long1 = { 6 };
		final long[] long2 = { 6, 7 };
		ArrayTester.assertEquals( "Test 'long' append() #1", "expected", "actual", long1, ArrayTools.append( null, long1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'long' append() #2", "expected", "actual", long2, ArrayTools.append( long1, null, -1, -1, long2[ 1 ] ) );

		final short[] short1 = { (short)7 };
		final short[] short2 = { (short)7, (short)8 };
		ArrayTester.assertEquals( "Test 'short' append() #1", "expected", "actual", short1, ArrayTools.append( null, short1[ 0 ] ) );
		ArrayTester.assertEquals( "Test 'short' append() #2", "expected", "actual", short2, ArrayTools.append( short1, null, -1, -1, short2[ 1 ] ) );
	}

	/**
	 * Test the {@link ArrayTools#appendMultiple} method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testAppendMultiple()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testAppendMultiple()" );

		class Test
		{
			final Object array;

			final Class<?> componentType;

			final int elementCount;

			final int incrementSize;

			final Object elements;

			final Object out;

			private Test( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final Object elements, final Object out )
			{
				this.array = array;
				this.componentType = componentType;
				this.elementCount = elementCount;
				this.incrementSize = incrementSize;
				this.elements = elements;
				this.out = out;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final char nul = '\0';
		final Character x = 'X';
		final Character y = 'Y';
		final Character z = 'Z';

		final Test[] tests =
		{
			/* Test #1  */ new Test( null, null, -1, -1, x, IllegalArgumentException.class ),
			/* Test #2  */ new Test( XYZ_ARRAY, null, -1, -1, ABC_LIST, IllegalArgumentException.class ),
			/* Test #3  */ new Test( null, null, -1, -1, null, null ),
			/* Test #4  */ new Test( x, null, -1, -1, null, IllegalArgumentException.class ),
			/* Test #5  */ new Test( null, null, -1, -1, x, IllegalArgumentException.class ),
			/* Test #6  */ new Test( null, null, -1, -1, new Object[] {}, null ),
			/* Test #7  */ new Test( null, null, -1, -1, new Object[] { "A" }, new Object[] { "A" } ),
			/* Test #8  */ new Test( null, null, -1, -1, new Object[] { "A", "B" }, new Object[] { "A", "B" } ),
			/* Test #9  */ new Test( new Object[] {}, null, -1, -1, null, new Object[] {} ),
			/* Test #10 */ new Test( new Object[] {}, null, -1, -1, new Object[] {}, new Object[] {} ),
			/* Test #11 */ new Test( new Object[] {}, null, -1, -1, new Object[] { "A" }, new Object[] { "A" } ),
			/* Test #12 */ new Test( new Object[] {}, null, -1, -1, new Object[] { "A", "B" }, new Object[] { "A", "B" } ),
			/* Test #13 */ new Test( new Object[] { "1" }, null, -1, -1, null, new Object[] { "1" } ),
			/* Test #14 */ new Test( new Object[] { "1" }, null, -1, -1, new Object[] {}, new Object[] { "1" } ),
			/* Test #15 */ new Test( new Object[] { "1" }, null, -1, -1, new Object[] { "A" }, new Object[] { "1", "A" } ),
			/* Test #16 */ new Test( new Object[] { "1" }, null, -1, -1, new Object[] { "A", "B" }, new Object[] { "1", "A", "B" } ),
			/* Test #17 */ new Test( new Object[] { "1", "2" }, null, -1, -1, null, new Object[] { "1", "2" } ),
			/* Test #18 */ new Test( new Object[] { "1", "2" }, null, -1, -1, new Object[] {}, new Object[] { "1", "2" } ),
			/* Test #19 */ new Test( new Object[] { "1", "2" }, null, -1, -1, new Object[] { "A" }, new Object[] { "1", "2", "A" } ),
			/* Test #20 */ new Test( new Object[] { "1", "2" }, null, -1, -1, new Object[] { "A", "B" }, new Object[] { "1", "2", "A", "B" } ),
			/* Test #21 */ new Test( XYZ_ARRAY, null, -1, -1, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z', 'X', 'Y', 'Z' } ),
			/* Test #22 */ new Test( XYZ_ARRAY, null, 0, -1, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z' } ),
			/* Test #23 */ new Test( XYZ_ARRAY, null, 1, -1, XYZ_ARRAY, new char[] { 'X', 'X', 'Y', 'Z' } ),
			/* Test #24 */ new Test( XYZ_ARRAY, null, 2, -1, XYZ_ARRAY, new char[] { 'X', 'Y', 'X', 'Y', 'Z' } ),
			/* Test #25 */ new Test( XYZ_ARRAY, null, 3, -1, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z', 'X', 'Y', 'Z' } ),
			/* Test #26 */ new Test( XYZ_ARRAY, null, 4, -1, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z', 'X', 'Y', 'Z' } ),
			/* Test #27 */ new Test( XYZ_ARRAY, null, -1, 4, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z', 'X', 'Y', 'Z', nul, nul } ),
			/* Test #28 */ new Test( XYZ_ARRAY, null, 0, 4, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z' } ),
			/* Test #29 */ new Test( XYZ_ARRAY, null, 1, 4, XYZ_ARRAY, new char[] { 'X', 'X', 'Y', 'Z' } ),
			/* Test #30 */ new Test( XYZ_ARRAY, null, 2, 4, XYZ_ARRAY, new char[] { 'X', 'Y', 'X', 'Y', 'Z', nul, nul, nul } ),
			/* Test #31 */ new Test( XYZ_ARRAY, null, 3, 4, XYZ_ARRAY, new char[] { 'X', 'Y', 'Z', 'X', 'Y', 'Z', nul, nul } ),
			/* Test #33 */ new Test( XYZ_ARRAY, Object.class, -1, -1, ABC_LIST, new Object[] { x, y, z, "A", "B", "C" } ),
			/* Test #34 */ new Test( XYZ_ARRAY, Object.class, 0, -1, ABC_LIST, IllegalArgumentException.class ),
			/* Test #35 */ new Test( XYZ_ARRAY, Object.class, 1, -1, ABC_LIST, new Object[] { x, "A", "B", "C" } ),
			/* Test #36 */ new Test( XYZ_ARRAY, Object.class, 2, -1, ABC_LIST, new Object[] { x, y, "A", "B", "C" } ),
			/* Test #37 */ new Test( XYZ_ARRAY, Object.class, 3, -1, ABC_LIST, new Object[] { x, y, z, "A", "B", "C" } ),
			/* Test #38 */ new Test( XYZ_ARRAY, Object.class, 4, -1, ABC_LIST, new Object[] { x, y, z, "A", "B", "C" } ),
			};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			Class<?> expectedException = null;
			if ( ( test.out instanceof Class ) && Exception.class.isAssignableFrom( (Class<?>)test.out ) )
			{
				expectedException = (Class<?>)test.out;
			}

			try
			{
				final Object result = ArrayTools.appendMultiple( test.array, test.componentType, test.elementCount, test.incrementSize, test.elements );
				if ( expectedException != null )
				{
					fail( description + " should have thrown exception" );
				}

				ArrayTester.assertEquals( description, "expected", "actual", test.out, result );
			}
			catch ( final Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception", expectedException.getName(), e.getClass().getName() );
			}
		}
	}

	/**
	 * Test the {@link ArrayTools#subtract} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testSubtract()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testSubtract()" );

		final String[] x = {};
		final String[] a____f = { "a", "f" };
		final String[] abc = { "a", "b", "c" };
		final String[] abcdef = { "a", "b", "c", "d", "e", "f" };
		final String[] bcde = { "b", "c", "d", "e" };
		final String[] def = { "d", "e", "f" };

		ArrayTester.assertEquals( "Test #1", "expected", "actual", x, ArrayTools.subtract( String.class, null, abcdef ) );
		ArrayTester.assertEquals( "Test #2", "expected", "actual", x, ArrayTools.subtract( String.class, x, abcdef ) );
		ArrayTester.assertEquals( "Test #3", "expected", "actual", x, ArrayTools.subtract( String.class, a____f, abcdef ) );
		ArrayTester.assertEquals( "Test #4", "expected", "actual", x, ArrayTools.subtract( String.class, abc, abcdef ) );
		ArrayTester.assertEquals( "Test #5", "expected", "actual", x, ArrayTools.subtract( String.class, abcdef, abcdef ) );
		ArrayTester.assertEquals( "Test #6", "expected", "actual", x, ArrayTools.subtract( String.class, bcde, abcdef ) );
		ArrayTester.assertEquals( "Test #7", "expected", "actual", x, ArrayTools.subtract( String.class, def, abcdef ) );
		ArrayTester.assertEquals( "Test #8", "expected", "actual", a____f, ArrayTools.subtract( String.class, abcdef, bcde ) );
		ArrayTester.assertEquals( "Test #9", "expected", "actual", abc, ArrayTools.subtract( String.class, abcdef, def ) );
		ArrayTester.assertEquals( "Test #10", "expected", "actual", abcdef, ArrayTools.subtract( String.class, abcdef, null ) );
		ArrayTester.assertEquals( "Test #11", "expected", "actual", abcdef, ArrayTools.subtract( String.class, abcdef, x ) );
		ArrayTester.assertEquals( "Test #12", "expected", "actual", bcde, ArrayTools.subtract( String.class, abcdef, a____f ) );
		ArrayTester.assertEquals( "Test #13", "expected", "actual", def, ArrayTools.subtract( String.class, abcdef, abc ) );
	}

	/**
	 * Test the {@link ArrayTools#concatenate(Object[][])} method.
	 */
	@Test
	public void testConcatenate()
	{
		final String[][] arrays = {
		{ "hello", "world" },
		{ "foo" },
		{},
		{ "bar", "baz" }
		};

		final String[] expected = { "hello", "world", "foo", "bar", "baz" };

		ArrayTester.assertEquals( "Test concatenate", "expected", "actual", expected, ArrayTools.concatenate( arrays ) );
	}

	/**
	 * Unit test for {@link ArrayTools#hexDump}.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testHexDump()
	throws Exception
	{
		final byte[] first = "Hello world! This is a simple test.".getBytes();
		final byte[] second = "Hello world! This is a rather complicated test, sometimes.".getBytes();
		final byte[] third = "Sometimes, maybe.".getBytes();

		final StringBuilder buffer = new StringBuilder();
		ArrayTools.hexDump( buffer, 16, true, first, second, third );

		//noinspection SpellCheckingInspection
		final String expected =
		"00000000 [48|65|6C|6C|6F|20|77|6F|72|6C|64|21|20|54|68|69] |Hello world! Thi| [48|65|6C|6C|6F|20|77|6F|72|6C|64|21|20|54|68|69] |Hello world! Thi| [53|6F|6D|65|74|69|6D|65|73|2C|20|6D|61|79|62|65] |Sometimes, maybe|\n" +
		"00000010 [73|20|69|73|20|61|20|73|69|6D|70|6C|65|20|74|65] |s is a simple te| [73|20|69|73|20|61|20|72|61|74|68|65|72|20|63|6F] |s is a rather co| [2E|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  ] |.               |\n" +
		"00000020 [73|74|2E|  |  |  |  |  |  |  |  |  |  |  |  |  ] |st.             | [6D|70|6C|69|63|61|74|65|64|20|74|65|73|74|2C|20] |mplicated test, | [  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  ]                   \n" +
		"00000030 [  |  |  |  |  |  |  |  |  |  ]                                      [73|6F|6D|65|74|69|6D|65|73|2E]                   |sometimes.      | [  |  |  |  |  |  |  |  |  |  ]                                     \n";

		assertEquals( "Unexpected output.", expected, buffer.toString() );
	}
}
