/*
 * Copyright (c) 2004-2017, Numdata BV, The Netherlands.
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
 * This class tests the {@link PropertyTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestPropertyTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestPropertyTools.class.getName();

	/**
	 * Test {@link PropertyTools#getBoolean} / {@link PropertyTools#set(Properties, String, boolean)}
	 * methods.
	 */
	@Test
	public void testGetBoolean()
	{
		System.out.println( CLASS_NAME + ".testGetBoolean()" );

		final Properties p = new Properties();

		System.out.println( " - Null-pointer handling" );

		assertTrue( "<empty> getBoolean( null , 'test' , 1.1 ) failed", PropertyTools.getBoolean( null, "test", true ) );

		System.out.println( " - Non-existing property" );

		try
		{
			PropertyTools.getBoolean( p, "test" );
			fail( "<empty> getBoolean( p , 'test' ) expected 'NoSuchElementException'" );
		}
		catch ( final NoSuchElementException e )
		{
			/* should occur */
		}

		assertTrue( "<empty> getBoolean( p , 'test' , true ) failed", PropertyTools.getBoolean( p, "test", true ) );

		System.out.println( " - Malformed property value" );
		p.setProperty( "test", "malformed" );

		try
		{
			PropertyTools.getBoolean( p, "test" );
			fail( "<malformed> getBoolean( p , 'test' ) expected 'IllegalArgumentException'" );
		}
		catch ( final IllegalArgumentException e )
		{
			/* should occur */
		}

		assertTrue( "<malformed> getBoolean( p , 'test' , true ) failed", PropertyTools.getBoolean( p, "test", true ) );

		System.out.println( " - Normal operation" );
		p.setProperty( "test", "false" );
		assertFalse( "<false> getBoolean( p , 'test' )", PropertyTools.getBoolean( p, "test" ) );
		assertFalse( "<false> getBoolean( p , 'test' , true ) failed", PropertyTools.getBoolean( p, "test", true ) );

		p.setProperty( "test", "true" );
		assertTrue( "<true> getBoolean( p , 'test' )", PropertyTools.getBoolean( p, "test" ) );
		assertTrue( "<true> getBoolean( p , 'test' , true ) failed", PropertyTools.getBoolean( p, "test", true ) );
	}

	/**
	 * Test {@link PropertyTools#getFloat} / {@link PropertyTools#set(Properties, String, float)}
	 * methods.
	 */
	@Test
	public void testGetFloat()
	{
		System.out.println( CLASS_NAME + ".testGetFloat()" );

		final Properties p = new Properties();

		System.out.println( " - Null-pointer handling" );

		assertEquals( "<empty> getFloat( null , 'test' , 1.1 ) failed", 1.1f, PropertyTools.getFloat( null, "test", 1.1f ), 0.0f );

		System.out.println( " - Non-existing property" );

		try
		{
			PropertyTools.getFloat( p, "test" );
			fail( "<empty> getFloat( p , 'test' ) expected 'NoSuchElementException'" );
		}
		catch ( final NoSuchElementException e )
		{
			/* should occur */
		}

		assertEquals( "<empty> getFloat( p , 'test' , 1.1f ) failed", 1.1f, PropertyTools.getFloat( p, "test", 1.1f ), 0.0f );

		System.out.println( " - Malformed property value" );
		p.setProperty( "test", "malformed" );

		try
		{
			PropertyTools.getFloat( p, "test" );
			fail( "<malformed> getFloat( p , 'test' ) expected 'NumberFormatException'" );
		}
		catch ( final NumberFormatException e )
		{
			/* should occur */
		}

		assertEquals( "<malformed> getFloat( p , 'test' , 1.1f ) failed", 1.1f, PropertyTools.getFloat( p, "test", 1.1f ), 0.0f );

		System.out.println( " - Normal operation" );
		p.setProperty( "test", "2.2" );

		assertEquals( "<ok> getFloat( p , 'test' )", 2.2f, PropertyTools.getFloat( p, "test" ), 0.0f );
		assertEquals( "<ok> getFloat( p , 'test' , 1.1f ) failed", 2.2f, PropertyTools.getFloat( p, "test", 1.1f ), 0.0f );
	}

	/**
	 * Test {@link PropertyTools#getInt} / {@link PropertyTools#set(Properties, String, int)} methods.
	 */
	@Test
	public void testGetInt()
	{
		System.out.println( CLASS_NAME + ".testGetInt()" );

		final Properties p = new Properties();

		System.out.println( " - Null-pointer handling" );

		assertEquals( "<empty> getInt( null , 'test' , 1 ) failed", 1, PropertyTools.getInt( null, "test", 1 ) );

		System.out.println( " - Non-existing property" );

		try
		{
			PropertyTools.getInt( p, "test" );
			fail( "<empty> getInt( p , 'test' ) expected 'NoSuchElementException'" );
		}
		catch ( final NoSuchElementException e )
		{
			/* should occur */
		}

		assertEquals( "<empty> getInt( p , 'test' , 1 ) failed", 1, PropertyTools.getInt( p, "test", 1 ) );

		System.out.println( " - Malformed property value" );
		p.setProperty( "test", "malformed" );

		try
		{
			PropertyTools.getInt( p, "test" );
			fail( "<malformed> getInt( p , 'test' ) expected 'NumberFormatException'" );
		}
		catch ( final NumberFormatException e )
		{
			/* should occur */
		}

		assertEquals( "<malformed> getInt( p , 'test' , 1 ) failed", 1, PropertyTools.getInt( p, "test", 1 ) );

		System.out.println( " - Normal operation" );
		p.setProperty( "test", "2" );

		assertEquals( "<ok> getInt( p , 'test' )", 2, PropertyTools.getInt( p, "test" ) );
		assertEquals( "<ok> getInt( p , 'test' , 1 ) failed", 2, PropertyTools.getInt( p, "test", 1 ) );
	}

	/**
	 * Test {@link PropertyTools#getLong} / {@link PropertyTools#set(Properties, String, long)} methods.
	 */
	@Test
	public void testGetLong()
	{
		System.out.println( CLASS_NAME + ".testGetLong()" );

		final Properties p = new Properties();

		System.out.println( " - Null-pointer handling" );

		assertEquals( "<empty> getLong( null , 'test' , 1L ) failed", 1L, PropertyTools.getLong( null, "test", 1L ) );

		System.out.println( " - Non-existing property" );

		try
		{
			PropertyTools.getLong( p, "test" );
			fail( "<empty> getLong( p , 'test' ) expected 'NoSuchElementException'" );
		}
		catch ( final NoSuchElementException e )
		{
			/* should occur */
		}

		assertEquals( "<empty> getLong( p , 'test' , 1L ) failed", 1L, PropertyTools.getLong( p, "test", 1L ) );

		System.out.println( " - Malformed property value" );
		p.setProperty( "test", "malformed" );

		try
		{
			PropertyTools.getLong( p, "test" );
			fail( "<malformed> getLong( p , 'test' ) expected 'NumberFormatException'" );
		}
		catch ( final NumberFormatException e )
		{
			/* should occur */
		}

		assertEquals( "<malformed> getLong( p , 'test' , 1L ) failed", 1L, PropertyTools.getLong( p, "test", 1L ) );

		System.out.println( " - Normal operation" );
		p.setProperty( "test", "2" );

		assertEquals( "<ok> getLong( p , 'test' )", 2L, PropertyTools.getLong( p, "test" ) );
		assertEquals( "<ok> getLong( p , 'test' , 1L ) failed", 2L, PropertyTools.getLong( p, "test", 1L ) );
	}

	private enum TestEnum
	{
		ALPHA,
		BETA,
		GAMMA,
		DELTA
	}

	/**
	 * Tests the {@link PropertyTools#getEnumList} method.
	 */
	@Test
	public void testGetEnumList()
	{
		System.out.println( CLASS_NAME + ".testGetEnumList" );

		final TestEnum[] empty = {};
		final TestEnum[] nullValue = { null };
		final TestEnum[] nullValues = { null, null };
		final TestEnum[] alpha = { TestEnum.ALPHA };
		final TestEnum[] alphaBeta = { TestEnum.ALPHA, TestEnum.BETA };
		final TestEnum[] alphaNullBeta = { TestEnum.ALPHA, null, TestEnum.BETA };
		final TestEnum[] nullBeta = { null, TestEnum.BETA };
		final TestEnum[] alphaNull = { TestEnum.ALPHA, null };

		final Properties properties = new Properties();

		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", alphaBeta, PropertyTools.getEnumList( properties, "test", alphaBeta ) );

		properties.setProperty( "test", "ALPHA" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", alpha, PropertyTools.getEnumList( properties, "test", empty ) );
		properties.setProperty( "test", "ALPHA,BETA" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", alphaBeta, PropertyTools.getEnumList( properties, "test", empty ) );
		properties.setProperty( "test", "ALPHA,,BETA" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", alphaNullBeta, PropertyTools.getEnumList( properties, "test", empty ) );
		properties.setProperty( "test", ",BETA" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", nullBeta, PropertyTools.getEnumList( properties, "test", empty ) );
		properties.setProperty( "test", "ALPHA," );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", alphaNull, PropertyTools.getEnumList( properties, "test", empty ) );

		properties.setProperty( "test", "" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", empty, PropertyTools.getEnumList( properties, "test", alphaBeta ) );
		properties.setProperty( "test", "null" );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", nullValue, PropertyTools.getEnumList( properties, "test", alphaBeta ) );
		properties.setProperty( "test", "," );
		ArrayTester.assertEquals( "Unexpected result.", "expected", "actual", nullValues, PropertyTools.getEnumList( properties, "test", alphaBeta ) );
	}

	/**
	 * Test {@link PropertyTools#getProperties(Properties, String, boolean)} / {@link PropertyTools#toString} methods.
	 */
	@Test
	public void testToString()
	{
		System.out.println( CLASS_NAME + ".testGetProperties" );

		//noinspection SpellCheckingInspection
		final String[][][] tests =
		{
			/*  0 */ { { "" } },
			/*  1 */ { { "", "key=\"\"" }, { "key", "" } },
			/*  2 */ { { "", "b=\"\",a=\"\"" }, { "a", "" }, { "b", "" } },
			/*  3 */ { { "key=\" \"" }, { "key", " " } },
			/*  4 */ { { "key=value" }, { "key", "value" } },
			/*  5 */ { { "key=\"value \"" }, { "key", "value " } },
			/*  6 */ { { "key=\"pre,post\"" }, { "key", "pre,post" } },
			/*  7 */ { { "key=\"pre=post\"" }, { "key", "pre=post" } },
			/*  8 */ { { "key=\"pre'post\"" }, { "key", "pre'post" } },
			/*  9 */ { { "key='pre\"post'" }, { "key", "pre\"post" } },
			/* 10 */ { { "key=pre\\npost" }, { "key", "pre\npost" } },
			/* 11 */ { { "key=pre\\rpost" }, { "key", "pre\rpost" } },
			/* 12 */ { { "key='\"a\\'b\"'" }, { "key", "\"a'b\"" } },
			/* 13 */ { { "b=2,a=1" }, { "a", "1" }, { "b", "2" } },
			/* 14 */ { { "b=2,a=\",\"" }, { "a", "," }, { "b", "2" } },
			/* 15 */ { { "b=\"2 \",a=\"'\"" }, { "a", "'" }, { "b", "2 " } },
			/* 16 */ { { "b=' \"',a=\"'\"" }, { "a", "'" }, { "b", " \"" } },
			/* 17 */ { { "a=1", "b=\"\",a=1" }, { "a", "1" }, { "b", "" } },
			/* 18 */ { { "b=2", "b=2,a=\"\"" }, { "a", "" }, { "b", "2" } },
			/* 19 */ { { "k\\ ey=value" }, { "k ey", "value" } },
			/* 20 */ { { "k\\rey=value" }, { "k\rey", "value" } },
			/* 21 */ { { "k\\ney=value" }, { "k\ney", "value" } },
			};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final String[][] test = tests[ testIndex ];

			for ( int filterEmptyIndex = 0; filterEmptyIndex < 2; filterEmptyIndex++ )
			{
				final boolean filterEmpty = ( filterEmptyIndex == 0 );
				System.out.println( " - test[ " + testIndex + " ], filterEmpty=" + filterEmpty );

				final String expected = test[ 0 ][ ( filterEmpty || ( test[ 0 ].length == 1 ) ) ? 0 : 1 ];

				// build Properties object from test
				final Properties properties = new Properties();
				for ( int propertyIndex = 1; propertyIndex < test.length; propertyIndex++ )
				{
					properties.setProperty( test[ propertyIndex ][ 0 ], test[ propertyIndex ][ 1 ] );
				}
				//System.out.println( "   > test set: " + properties );

				// test Properties -> String conversion
				final String actual = PropertyTools.toString( properties, filterEmpty );
				System.out.println( "   > to string: " + actual );
				assertEquals( "test[ " + testIndex + " ] (filterEmpty=" + filterEmpty + ") failed", expected, actual );

				// check if we can convert the String back to a Properties object
				final Properties readBack = PropertyTools.fromString( actual );
				System.out.println( "   > to Properties: " + readBack );
				assertNotNull( "failed to read back test[ " + testIndex + " ] (filterEmpty=" + filterEmpty + ") result", readBack );

				// remove empty entries from properties (these can not be read back)
				if ( filterEmpty )
				{
					for ( int propertyIndex = 1; propertyIndex < test.length; propertyIndex++ )
					{
						if ( test[ propertyIndex ][ 1 ].isEmpty() )
						{
							properties.remove( test[ propertyIndex ][ 0 ] );
						}
					}
				}

				// test if original and new Properties are the same
				assertEquals( "properties changed during test[ " + testIndex + " ] (filterEmpty=" + filterEmpty + ')', properties, readBack );
			}
		}
	}
}
