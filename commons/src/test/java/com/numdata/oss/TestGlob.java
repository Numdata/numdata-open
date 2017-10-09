/*
 * (C) Copyright Numdata BV 2017-2017 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */

package com.numdata.oss;import junit.framework.*;

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
	}
}
