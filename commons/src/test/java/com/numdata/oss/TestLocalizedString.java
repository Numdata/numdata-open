/*
 * Copyright (c) 2010-2021, Unicon Creation BV, The Netherlands.
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

import static java.util.Arrays.*;
import static java.util.Collections.*;
import org.jetbrains.annotations.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link LocalizedString} class.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "JavaDoc" )
public class TestLocalizedString
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestLocalizedString.class.getName();

	@Test
	public void testConstruct()
	{
		final LocalizedString localizedString = new LocalizedString();
		assertNull( "Expected empty string.", localizedString.get() );
	}

	@Test
	public void testConstructFromLocalizedString()
	{
		final LocalizedString original = LocalizedString.create( "Greetings Earthlings", "en", "Hello world!", "nl", "Hallo wereld!" );
		assertEquals( "Constructed copy should be equal.", original, new LocalizedString( original ) );
	}

	/**
	 * Test {@link LocalizedString#create} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testCreate()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testCreate" );

		final LocalizedString defaultOnly = LocalizedString.create( "Dog" );
		assertEquals( "Bad string", "Dog", defaultOnly.get() );
		assertEquals( "Bad string", "Dog", defaultOnly.get( Locale.US ) );
		assertEquals( "Bad string", "Dog", defaultOnly.get( Locale.GERMAN ) );

		try
		{
			LocalizedString.create( "Dog", "de" );
			fail( "should fail" );
		}
		catch ( final IllegalArgumentException e )
		{
			/* should occur */
		}

		try
		{
			LocalizedString.create( "Dog", "de", "Hund", "nl" );
			fail( "should fail" );
		}
		catch ( final IllegalArgumentException e )
		{
			/* should occur */
		}

		final LocalizedString localizedString = LocalizedString.create( "Dog", "de", "Hund", "nl", "Hond" );
		assertEquals( "Bad string", "Dog", localizedString.get() );
		assertEquals( "Bad string", "Dog", localizedString.get( Locale.ENGLISH ) );
		assertEquals( "Bad string", "Hund", localizedString.get( Locale.GERMAN ) );
		assertEquals( "Bad string", "Hond", localizedString.get( "nl" ) );
	}

	@Test
	public void testGetWithDefaultValue()
	{
		final LocalizedString withExplicitDefault = new LocalizedString();
		withExplicitDefault.set( "", "Greetings Earthlings" );
		withExplicitDefault.set( "en", "Hello world!" );
		withExplicitDefault.set( "nl", "Hallo wereld!" );
		assertEquals( "Unexpected result.", "Greetings Earthlings", withExplicitDefault.get( "zh", "Default greeting" ) );

		final LocalizedString withoutExplicitDefault = new LocalizedString();
		withoutExplicitDefault.set( "en", "Hello world!" );
		withoutExplicitDefault.set( "nl", "Hallo wereld!" );
		assertEquals( "Unexpected result.", "Default greeting", withoutExplicitDefault.get( "zh", "Default greeting" ) );
	}

	@Test
	public void testGetAnyLocale()
	{
		final LocalizedString withEnglish = new LocalizedString();
		withEnglish.set( "en", "Hello world!" );
		withEnglish.set( "nl", "Hallo wereld!" );
		assertEquals( "Unexpected result.", "Hello world!", withEnglish.get( (String)null, "Default greeting" ) );

		final LocalizedString withoutEnglish = new LocalizedString();
		withoutEnglish.set( "de", "Hallo Welt!" );
		withoutEnglish.set( "nl", "Hallo wereld!" );
		assertEquals( "Unexpected result.", "Hallo Welt!", withoutEnglish.get( (Locale)null, "Default greeting" ) );
	}

	@Test
	public void testEquals()
	{
		final LocalizedString localizedString = new LocalizedString();
		localizedString.set( "en", "Hello world!" );
		localizedString.set( "nl", "Hallo wereld!" );
		assertEquals( "Unexpected result.", localizedString, localizedString );

		final LocalizedString otherString = new LocalizedString( localizedString );
		assertEquals( "Unexpected result.", localizedString, otherString );

		otherString.set( "nl", "Hallo andere wereld!" );
		assertNotEquals( "Unexpected result.", localizedString, otherString );

		otherString.set( "nl", "Hallo wereld!" );
		otherString.set( "de", "Hallo Welt!" );
		assertNotEquals( "Unexpected result.", localizedString, otherString );
	}

	@Test
	public void testCompareTo()
	{
		final LocalizedString string1 = LocalizedString.create( "Ambulance", "de", "Krankenwagen", "xx", "Whatever" );
		final LocalizedString string2 = LocalizedString.create( "Fire department", "de", "Feuerwehr", "xx", "Whatever" );
		assertTrue( "Unexpected result.", string1.compareTo( Locale.ENGLISH, string2 ) < 0 );
		assertTrue( "Unexpected result.", string1.compareTo( Locale.GERMAN, string2 ) > 0 );
		assertTrue( "Unexpected result.", string1.compareTo( Locale.GERMANY, string2 ) > 0 );
		assertEquals( "Unexpected result.", 0, string1.compareTo( new Locale( "xx" ), string2 ) );
	}

	@Test
	public void testParse()
	{
		final LocalizedString original = LocalizedString.create( "Hello world!", "nl", "Hallo wereld!", "de", "Hallo Welt!" );
		final String string = original.toString();
		final LocalizedString parsed = LocalizedString.parse( string );
		assertEquals( "Expected original content.", original, parsed );
	}

	@Test
	public void testConvert()
	{
		final LocalizedString original = LocalizedString.create( "Hello world!", "nl", "Hallo wereld!", "de", "Hallo Welt!" );

		assertSame( "Expected original string.", original, LocalizedString.convert( asList( Locale.ROOT, new Locale( "nl" ), Locale.GERMAN ), original ) );
		assertSame( "Expected original string.", original, LocalizedString.convert( emptyList(), original ) );

		final LocalizableString localizableString = new LocalizableString()
		{
			@Override
			public @Nullable String get( @Nullable final Locale locale )
			{
				return original.get( locale );
			}

			@Override
			public @Nullable String get( @Nullable final String locale )
			{
				return original.get( locale );
			}

			@Override
			public LocalizableString concat( final String string )
			{
				return original.concat( string );
			}

			@Override
			public LocalizableString concat( final LocalizableString string )
			{
				return original.concat( string );
			}
		};

		assertEquals( "Expected original content.", original, LocalizedString.convert( asList( Locale.ROOT, new Locale( "nl" ), Locale.GERMAN ), localizableString ) );

		final LocalizedString expected = new LocalizedString( original );
		expected.set( "", "Hallo wereld!" ); // nl becomes the new default (since it's first in the 'locales' parameter)
		expected.remove( "nl" );
		assertEquals( "Expected subset of original content.", expected, LocalizedString.convert( asList( new Locale( "nl" ), Locale.GERMAN ), localizableString ) );
	}
}
