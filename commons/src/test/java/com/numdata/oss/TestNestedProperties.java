/*
 * Copyright (c) 2007-2021, Unicon Creation BV, The Netherlands.
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
 * This class tests the functionality of the {@link NestedProperties} class.
 *
 * @author Peter S. Heijnen
 */
public class TestNestedProperties
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestNestedProperties.class.getName();

	/**
	 * Tests {@link NestedProperties#getBoolean}.
	 */
	@Test
	public void testGetBoolean()
	{
		final NestedProperties properties = new NestedProperties();
		properties.set( "true", true );
		properties.set( "false", false );
		properties.set( "positive", 1.23 );
		properties.set( "zero", 0 );
		properties.set( "negative", -1.23 );
		properties.set( "trueString1", "True" );
		properties.set( "trueString2", "1" );
		properties.set( "trueString3", "y" );
		properties.set( "trueString4", "yeS" );
		properties.set( "falseString1", "any" );
		properties.set( "falseString2", "" );

		assertTrue( "Unexpected result.", properties.getBoolean( "true", false ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "false", true ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "positive", false ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "zero", true ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "negative", true ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "trueString1", false ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "trueString2", false ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "trueString3", false ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "trueString4", false ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "falseString1", true ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "falseString2", true ) );
		assertTrue( "Unexpected result.", properties.getBoolean( "other", true ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "other", false ) );
		assertFalse( "Unexpected result.", properties.getBoolean( "other" ) );
	}

	/**
	 * Tests {@link NestedProperties#getDouble}.
	 */
	@Test
	public void testGetDouble()
	{
		final NestedProperties properties = new NestedProperties();
		properties.set( "true", true );
		properties.set( "false", false );
		properties.set( "positive", 1.23 );
		properties.set( "zero", 0 );
		properties.set( "negative", -1.23 );
		properties.set( "numberString1", "1.23" );
		properties.set( "numberString2", "NaN" );
		properties.set( "invalidString1", "any" );
		properties.set( "invalidString2", "" );

		assertThrows( IllegalArgumentException.class, () -> properties.getDouble( "true", 13.37 ) );
		assertThrows( IllegalArgumentException.class, () -> properties.getDouble( "false", 13.37 ) );
		assertEquals( "Unexpected result.", 1.23, properties.getDouble( "positive", 13.37 ), 0.0 );
		assertEquals( "Unexpected result.", 0, properties.getDouble( "zero", 13.37 ), 0.0 );
		assertEquals( "Unexpected result.", -1.23, properties.getDouble( "negative", 13.37 ), 0.0 );
		assertEquals( "Unexpected result.", 1.23, properties.getDouble( "numberString1", 13.37 ), 0.0 );
		assertEquals( "Unexpected result.", Double.NaN, properties.getDouble( "numberString2", 13.37 ), 0.0 );
		assertThrows( IllegalArgumentException.class, () -> properties.getDouble( "invalidString1", 13.37 ) );
		assertThrows( IllegalArgumentException.class, () -> properties.getDouble( "invalidString2", 13.37 ) );
		assertEquals( "Unexpected result.", 13.37, properties.getDouble( "other", 13.37 ), 0.0 );
		assertEquals( "Unexpected result.", 0.0, properties.getDouble( "other" ), 0.0 );
	}

	/**
	 * Tests {@link NestedProperties#getInt}.
	 */
	@Test
	public void testGetInt()
	{
		final NestedProperties properties = new NestedProperties();
		properties.set( "true", true );
		properties.set( "false", false );
		properties.set( "positive", 1.23 );
		properties.set( "zero", 0 );
		properties.set( "negative", -1.23 );
		properties.set( "numberString1", "123" );
		properties.set( "invalidString1", "1.23" );
		properties.set( "invalidString2", "NaN" );
		properties.set( "invalidString3", "any" );
		properties.set( "invalidString4", "" );

		assertThrows( IllegalArgumentException.class, () -> properties.getInt( "true", 1337 ) );
		assertThrows( IllegalArgumentException.class, () -> properties.getInt( "false", 1337 ) );
		assertEquals( "Unexpected result.", 1, properties.getInt( "positive", 1337 ) );
		assertEquals( "Unexpected result.", 0, properties.getInt( "zero", 1337 ) );
		assertEquals( "Unexpected result.", -1, properties.getInt( "negative", 1337 ) );
		assertEquals( "Unexpected result.", 123, properties.getInt( "numberString1", 1337 ) );
		assertThrows( NumberFormatException.class, () -> properties.getInt( "invalidString1", 1337 ) );
		assertThrows( NumberFormatException.class, () -> properties.getInt( "invalidString2", 1337 ) );
		assertThrows( IllegalArgumentException.class, () -> properties.getInt( "invalidString3", 1337 ) );
		assertThrows( IllegalArgumentException.class, () -> properties.getInt( "invalidString4", 1337 ) );
		assertEquals( "Unexpected result.", 1337, properties.getInt( "other", 1337 ) );
		assertEquals( "Unexpected result.", 0, properties.getInt( "other" ) );
	}

	/**
	 * Tests {@link NestedProperties#getProperty}.
	 */
	@Test
	public void testGetProperty()
	{
		final NestedProperties properties = new NestedProperties();
		properties.set( "true", true );
		properties.set( "false", false );
		properties.set( "positive", 1.23 );
		properties.set( "zero", 0 );
		properties.set( "negative", -1.23 );
		properties.set( "string1", "123" );
		properties.set( "string2", "any" );
		properties.set( "string3", "" );

		assertEquals( "Unexpected result.", "true", properties.getProperty( "true" ) );
		assertEquals( "Unexpected result.", "false", properties.getProperty( "false" ) );
		assertEquals( "Unexpected result.", "1.23", properties.getProperty( "positive" ) );
		assertEquals( "Unexpected result.", "0", properties.getProperty( "zero" ) );
		assertEquals( "Unexpected result.", "-1.23", properties.getProperty( "negative" ) );
		assertEquals( "Unexpected result.", "123", properties.getProperty( "string1" ) );
		assertEquals( "Unexpected result.", "any", properties.getProperty( "string2" ) );
		assertEquals( "Unexpected result.", "", properties.getProperty( "string3" ) );
	}

	/**
	 * Test {@link NestedProperties#getChar} {@link NestedProperties#set(String,
	 * char)} methods.
	 */
	@Test
	public void testGetSetChar()
	{
		System.out.println( CLASS_NAME + ".testGetSetChar()" );

		System.out.println( " - default values" );
		{
			final NestedProperties properties = new NestedProperties();
			final char ch = properties.getChar( "char" );
			assertEquals( "get for default character failed", '\0', ch );
		}

		System.out.println( " - test set" );
		for ( int i = 0; i < 255; i++ )
		{
			final NestedProperties properties = new NestedProperties();
			final char in = (char)i;
			properties.set( "char", in );
			final char out = properties.getChar( "char" );
			assertEquals( "get/set for character #" + i + " ('" + in + "') failed", in, out );
		}
	}
}
