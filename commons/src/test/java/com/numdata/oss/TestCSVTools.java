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

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link CSVTools} class.
 *
 * @author G.B.M. Rupert
 */
@SuppressWarnings( "UnsecureRandomNumberGeneration" )
public class TestCSVTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestCSVTools.class.getName();

	/**
	 * Test {@link CSVTools#readCSV} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadCSV()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadCSV" );

		/*
		 * Create tests.
		 */
		final Object[][] tests =
		{
			/* Test  1 */ { "1\n", new String[] { "1" } },
			/* Test  2 */ { "1", new String[] { "1" } },
			/* Test  3 */ { "1,2,3,4,5\n", new String[] { "1", "2", "3", "4", "5" } },
			/* Test  4 */ { "1,2,3,4,5", new String[] { "1", "2", "3", "4", "5" } },
			/* Test  5 */ { "\"a\"\n", new String[] { "a" } },
			/* Test  6 */ { "\"a\"", new String[] { "a" } },
			/* Test  7 */ { "\"a\",b", new String[] { "a", "b" } },
			/* Test  8 */ { "\"a\",\"b\",\"c\"", new String[] { "a", "b", "c" } },
			/* Test  9 */ { "\"a\",\"\"\"b\"\"\",\"c\"", new String[] { "a", "\"b\"", "c" } },
			/* Test 10 */ { "\"a\",\"\"\",b,\"\"\",\"c\"", new String[] { "a", "\",b,\"", "c" } },
			/* Test 11 */ { "\"a\",\" \"\",b,\"\" \",\"c\"", new String[] { "a", " \",b,\" ", "c" } },
			/* Test 12 */ { " 1 , 2 , 3 ", new String[] { "1", "2", "3" } },
			/* Test 13 */ { "\"a\",\" b \",\"c d\"", new String[] { "a", " b ", "c d" } },
			/* Test 14 */ { "ignore-\"a\",\"b\",\"c\"-ignore", new String[] { "a", "b", "c" } },
			/* Test 15 */ { "a,b c,d", new String[] { "a", "b c", "d" } },
			/* Test 16 */ { "a,x \"b\",c", new String[] { "a", "b", "c" } },
			/* Test 17 */ { "a,x  \"b\",c", new String[] { "a", "b", "c" } },
			};

		/*
		 * Perform tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Object[] test = tests[ i ];

			final List<List<String>> rows = CSVTools.readCSV( new StringReader( (String)test[ 0 ] ) );

			assertEquals( "Test " + ( i + 1 ) + ": One result row is expected.", 1, rows.size() );

			final String[] expected = (String[])test[ 1 ];
			final List<String> actual = rows.get( 0 );

			assertEquals( "Test " + ( i + 1 ) + ": Incorrect values read.", ArrayTools.toString( expected ), ArrayTools.toString( actual ) );
		}
	}

	/**
	 * Test {@link CSVTools#writeCSV} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testWriteCSV()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testWriteCSV" );

		/*
		 * Create tests.
		 */
		final Object[][] tests =
		{
			/* Test  1 */ { new Object[] { "1" }, "1" },
			/* Test  2 */ { new Object[] { "1", "2", 3L, "4", "5" }, "1,2,3,4,5" },
			/* Test  3 */ { new Object[] { "a" }, "a" },
			/* Test  4 */ { new Object[] { "a", "b", "c" }, "a,b,c" },
			/* Test  5 */ { new Object[] { "a", "\"b\"", "c" }, "a,\"\"\"b\"\"\",c" },
			/* Test  6 */ { new Object[] { "a", "\",b,\"", "c" }, "a,\"\"\",b,\"\"\",c" },
			/* Test  7 */ { new Object[] { "a", " b ", "c" }, "a,\" b \",c" },
			};

		/*
		 * Perform tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Object[] test = tests[ i ];
			final Object[] a = (Object[])test[ 0 ];

			final List<Object> input = new ArrayList<Object>();
			input.addAll( Arrays.asList( a ) );

			final Collection<List<Object>> rows = new ArrayList<List<Object>>( 1 );
			rows.add( input );

			final Appendable actual = new StringBuilder();
			CSVTools.writeCSV( actual, rows );

			assertEquals( "Test " + ( i + 1 ), test[ 1 ], actual.toString() );
		}
	}

	/**
	 * Test {@link CSVTools#writeTidyCSV} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testWriteTidyCSV()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testWriteTidyCSV" );

		/*
		 * Create tests.
		 */
		final Object[][] tests =
		{
				/* Test 1: Each column as a constant width across all rows. */
				{
				Arrays.asList(
				Arrays.asList( "1", "1234", "12" ),
				Arrays.asList( "123", "12345" ),
				Arrays.asList( "123", "12345", "12" )
				),
				"1  , 1234 , 12\n" +
				"123, 12345\n" +
				"123, 12345, 12"
				},

				/* Test 2: Comments are not formatted. */
				{
				Arrays.asList(
				Collections.singletonList( "# This is a comment, with a comma." ),
				Arrays.asList( "these", "are" ),
				Arrays.asList( "values", "and", "stuff" ),
				Arrays.asList( "# This", "is", "not", "a comment, because there are multiple columns." )
				),
				"\"# This is a comment, with a comma.\"\n" +
				"these , are\n" +
				"values, and, stuff\n" +
				"# This, is , not  , \"a comment, because there are multiple columns.\""
				}

		};

		/*
		 * Perform tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Object[] test = tests[ i ];

			final Iterable<List<Object>> rows = (Iterable<List<Object>>)test[ 0 ];

			final StringBuilder actual = new StringBuilder();
			CSVTools.writeTidyCSV( actual, rows, ',', "\n" );
			assertEquals( "Test " + ( i + 1 ), test[ 1 ], actual.toString() );

			final List<List<String>> read = CSVTools.readCSV( new StringReader( (String)test[ 1 ] ), ',' );
			actual.setLength( 0 );
			CSVTools.writeTidyCSV( actual, read, ',', "\n" );
			assertEquals( "Test " + ( i + 1 ), test[ 1 ], actual.toString() );
		}
	}

	/**
	 * Test {@link CSVTools#detectSeparator(Reader, String...)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testDetectSeparator()
	throws Exception
	{
		final String where = CLASS_NAME + ".testDetectSeparator()";
		System.out.println( where );

		final char[] separators = { ',', ';', ':', '\t' };

		final List<String> values = Arrays.asList(
		"one, two",
		"something",
		"you two",
		"ö ü",
		"12345",
		"    789   ",
		"-12,456.00",
		"12 june, 2012",
		"12.0",
		"13,0"
		);

		final Random random = new Random( 0L );

		for ( int test = 0; test < 1000; test++ )
		{
			final char separator = separators[ random.nextInt( separators.length ) ];
			final int numberOfRows = 2 + random.nextInt( 100 );
			final int numberOfColumns = 2 + random.nextInt( 100 );

			final StringBuilder out = new StringBuilder();

			for ( int rowIndex = 0; rowIndex < numberOfRows; rowIndex++ )
			{
				for ( int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++ )
				{
					final String value = values.get( random.nextInt( values.size() ) );
					final boolean requireQuotes = ( value.indexOf( separator ) >= 0 );

					if ( columnIndex > 0 )
					{
						out.append( separator );
					}

					if ( requireQuotes )
					{
						out.append( '"' );
						out.append( value.replace( "\"", "\"\"" ) );
						out.append( '"' );
					}
					else
					{
						out.append( value );
					}
				}

				out.append( '\n' );
			}

			final char detected = CSVTools.detectSeparator( new StringReader( out.toString() ) );
			if ( separator != detected )
			{
				System.out.println( "-------------------------------------------------------------------" );
				System.out.println( out.toString() );
				System.out.println( "-------------------------------------------------------------------" );

				assertEquals( "Test " + test + ": Failed to detect separator", separator, detected );
			}
		}

	}

	/**
	 * Test {@link CSVTools#detectSeparator(Reader, String...)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	@Ignore
	public void testDetectSeparator2()
	throws Exception
	{
		final String where = CLASS_NAME + ".testDetectSeparator1()";
		System.out.println( where );

		final char separator = ':';
		final String csv = "12 june, 2012:    789   \n" +
		                   "12 june, 2012:-12,456.00";

		final char detected = CSVTools.detectSeparator( new StringReader( csv ) );
		if ( separator != detected )
		{
			assertEquals( "Failed to detect separator", separator, detected );
		}
	}
}
