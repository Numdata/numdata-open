/*
 * Copyright (c) 2017-2018, Numdata BV, The Netherlands.
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

import junit.framework.*;

/**
 * Unit test for {@link Glob}.
 *
 * @author Gerrit Meinders
 */
public class TestGlob
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestGlob.class.getName();

	/**
	 * Tests {@link Glob#matchPattern}.
	 */
	public void testMatchPattern()
	{
		final String where = CLASS_NAME + ".testMatchPattern()";
		System.out.println( where );

		final Glob glob = new Glob();

		// Empty/null.
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( null, null ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( "", null ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( null, "" ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( "", "" ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( "value", null ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( null, "pattern" ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( "value", "" ) );
		assertTrue( "Null/empty values or patterns always match.", glob.matchPattern( "", "pattern" ) );

		// Plain
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello World" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "ello World!" ) );

		// Asterisk
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "*" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "*!" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "*rld!" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "*lo Wo*" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello*Glob" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Glob*World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "*World" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "ello*" ) );

		// Question mark
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello World?" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "?ello World!" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "He?lo??orld!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello Worl?" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "?llo World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello ?World!" ) );

		// Pipe
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*|Hello Worl?" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*Glob|Hello World?" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*|Hello Worl?|Hello World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*Glob|Hello World?|Hello World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello World!", "Hello*Glob|Hello Worl?|Hello World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello*Glob|Hello Worl?" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello*Glob|Hello Worl?|Hello World" ) );

		// Character escapes
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello . World", "Hello . World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello $ World", "Hello $ World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello ( World", "Hello ( World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello ) World", "Hello ) World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello + World", "Hello + World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello [ World", "Hello [ World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello ] World", "Hello ] World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello ? World", "Hello \\? World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello * World", "Hello \\* World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Hello \\ World", "Hello \\\\ World" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello.World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello World!$" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "(Hello World!)" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello+ World!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "[H]ello [W]orld!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello W\\?rld!" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello World!", "Hello \\*!" ) );

		// Excludes
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello . World", "!Hello*|Hello . World" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "Hello . World", "!Hello*|*World" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "Wonderful World", "!Hello*|*World" ) );
		assertFalse( "Pattern should not match.", glob.matchPattern( "test123a", "!*a|test*" ) );
		assertTrue( "Pattern should match.", glob.matchPattern( "test123b", "!*a|test*" ) );
	}
}
