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
import java.util.zip.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for the {@link ResourcePack} class.
 *
 * @author Gerrit Meinders
 */
public class TestResourcePack
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestResourcePack.class.getName();

	/**
	 * Tests write and read functionality provided by {@link
	 * ResourcePack#write(OutputStream)} and the {@link ResourcePack#ResourcePack(DataInputStream)}
	 * constructor.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testWrite()
	throws Exception
	{
		final String where = CLASS_NAME + ".testWrite()";
		System.out.println( where );

		final ResourcePack resourcePack = new ResourcePack();

		final ListResourceBundle testBundleEN = createBundle( new Object[][] {
		{ "a", "one" },
		{ "b", "two" },
		{ "c", "three" },
		} );

		final ListResourceBundle testBundleDE = createBundle( new Object[][] {
		{ "a", "eins" },
		{ "b", "zwei" },
		{ "c", "drei" },
		} );

		final ListResourceBundle testBundleNL = createBundle( new Object[][] {
		{ "a", "een" },
		{ "b", "twee" },
		{ "c", "drie" },
		} );

		final Locale locale = new Locale( "", "", "" );
		final Locale localeNL = new Locale( "nl", "", "" );
		final Locale localeDE = new Locale( "de", "DE", "" );

		resourcePack.insertBundleData( locale, "test", testBundleEN );
		resourcePack.insertBundleData( localeNL, "test", testBundleNL );
		resourcePack.insertBundleData( localeDE, "test", testBundleDE );

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		resourcePack.write( out );

		final Locale defaultLocale = Locale.getDefault();
		try
		{
			Locale.setDefault( localeNL );
			final DataInputStream in = new DataInputStream( new GZIPInputStream( new ByteArrayInputStream( out.toByteArray() ) ) );
			final ResourcePack actualPack = new ResourcePack( in );

			assertTrue( "Different locales in resource pack.", Arrays.equals( resourcePack.getLocales(), actualPack.getLocales() ) );

			final ResourceBundle bundle = actualPack.getBundle( "test", locale );
			final ResourceBundle bundleNL = actualPack.getBundle( "test", localeNL );
			final ResourceBundle bundleDE = actualPack.getBundle( "test", localeDE );
			final ResourceBundle bundleEN = actualPack.getBundle( "test", new Locale( "en", "US", "" ) );

			assertEquals( "Unexpected resource bundle contents.", ResourceBundleTools.getProperties( testBundleEN ), ResourceBundleTools.getProperties( bundle ) );
			assertEquals( "Unexpected resource bundle contents.", ResourceBundleTools.getProperties( testBundleNL ), ResourceBundleTools.getProperties( bundleNL ) );
			assertEquals( "Unexpected resource bundle contents.", ResourceBundleTools.getProperties( testBundleDE ), ResourceBundleTools.getProperties( bundleDE ) );
			assertEquals( "Unexpected resource bundle contents.", ResourceBundleTools.getProperties( testBundleEN ), ResourceBundleTools.getProperties( bundleEN ) );
		}
		finally
		{
			Locale.setDefault( defaultLocale );
		}
	}

	/**
	 * Creates a resource bundle.
	 *
	 * @param entries Resource bundle entries.
	 *
	 * @return Resource bundle.
	 */
	private static ListResourceBundle createBundle( final Object[][] entries )
	{
		return new ListResourceBundle()
		{
			protected Object[][] getContents()
			{
				return entries;
			}
		};
	}
}
