/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link LocalizedString} class.
 *
 * @author Peter S. Heijnen
 */
public class TestLocalizedString
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestLocalizedString.class.getName();

	/**
	 * Test {@link LocalizedString#create} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
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
}
