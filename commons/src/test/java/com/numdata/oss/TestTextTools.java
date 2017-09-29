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

import static com.numdata.oss.TextTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link TextTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestTextTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestTextTools.class.getName();

	/**
	 * Test {@link TextTools#escape} method.
	 */
	@Test
	public void testEscape()
	{
		final String[][] tests =
		{
		{ "", "" },
		{ "\t", "\\t" },
		{ "\r ", "\\r " },
		{ " \n", " \\n" },
		{ "Hello \u0248", "Hello \\u0248" },
		{ "\u0007", "\\u0007" },
		};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final String[] test = tests[ testIndex ];
			final String input = test[ 0 ];
			final String expected = test[ 1 ];

			final StringBuffer sb = new StringBuffer();
			TextTools.escape( sb, input );
			final String result = sb.toString();

			assertEquals( "test[" + testIndex + "] escape( '" + input + " ' ) failed", expected, result );
		}
	}

	/**
	 * Test {@link TextTools#unescape} method.
	 */
	@Test
	public void testUnescape()
	{
		final String[][] tests =
		{
		{ "", ",\r\n", "" },
		{ "\\t", ",\r\n", "\t" },
		{ "\\r ", ",\r\n", "\r " },
		{ " \\n", ",\r\n", " \n" },
		{ "Hello \\u0248", ",\r\n", "Hello \u0248" },
		{ "\\u0007", ",\r\n", "\u0007" },
		{ "a\\,b", ",\r\n", "a,b" },
		{ "a,b", ",\r\n", "a,b", "a" },
		{ "a\\,b", ",\r\n", "a,b" },
		{ "a,b", "\"", "a,b" },
		{ "ab\"", "\"", "ab\"", "ab" },
		};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final String[] test = tests[ testIndex ];
			final String separators = test[ 1 ];
			final String input = test[ 0 ];

			{
				final String expected = test[ 2 ];

				final String result = TextTools.unescape( input );
				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' ) failed", expected, result );
			}

			{
				final String expectedString = ( test.length == 4 ) ? test[ 3 ] : test[ 2 ];
				final int expectedPos = ( test.length == 4 ) ? expectedString.length() + 1 : -1;

				final StringBuffer sb = new StringBuffer();
				final int actualPos = TextTools.unescape( input, 0, sb, separators );
				final String actualString = sb.toString();

				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' , <separators> ) returned invalid index", expectedPos, actualPos );
				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' , <separators> ) failed", expectedString, actualString );
			}
		}
	}

	/**
	 * Test {@link TextTools#toHexString} method.
	 */
	@Test
	public void testToHexString()
	{
		assertEquals( "Unexpected result.", "1234abcd", TextTools.toHexString( 0x1234abcd, 8, false ) );
		assertEquals( "Unexpected result.", "1234ABCD", TextTools.toHexString( 0x1234abcd, 8, true ) );
		assertEquals( "Unexpected result.", "fedcba98", TextTools.toHexString( 0xfedcba98, 8, false ) );
		assertEquals( "Unexpected result.", "FEDCBA98", TextTools.toHexString( 0xfedcba98, 8, true ) );
		assertEquals( "Unexpected result.", "34abcd", TextTools.toHexString( 0x1234abcd, 6, false ) );
		assertEquals( "Unexpected result.", "34ABCD", TextTools.toHexString( 0x1234abcd, 6, true ) );
		assertEquals( "Unexpected result.", "dcba98", TextTools.toHexString( 0xfedcba98, 6, false ) );
		assertEquals( "Unexpected result.", "DCBA98", TextTools.toHexString( 0xfedcba98, 6, true ) );

		assertEquals( "Unexpected result.", "000012ab", TextTools.toHexString( 0x12ab, 8, false ) );
		assertEquals( "Unexpected result.", "ffffed55", TextTools.toHexString( -0x12ab, 8, false ) );
		assertEquals( "Unexpected result.", "0000fe98", TextTools.toHexString( 0xfe98, 8, false ) );
		assertEquals( "Unexpected result.", "ffff0168", TextTools.toHexString( -0xfe98, 8, false ) );
		assertEquals( "Unexpected result.", "0012ab", TextTools.toHexString( 0x12ab, 6, false ) );
		assertEquals( "Unexpected result.", "ffed55", TextTools.toHexString( -0x12ab, 6, false ) );
		assertEquals( "Unexpected result.", "00fe98", TextTools.toHexString( 0xfe98, 6, false ) );
		assertEquals( "Unexpected result.", "ff0168", TextTools.toHexString( -0xfe98, 6, false ) );
		assertEquals( "Unexpected result.", "12ab", TextTools.toHexString( 0x12ab, 4, false ) );
		assertEquals( "Unexpected result.", "ed55", TextTools.toHexString( -0x12ab, 4, false ) );
		assertEquals( "Unexpected result.", "fe98", TextTools.toHexString( 0xfe98, 4, false ) );
		assertEquals( "Unexpected result.", "0168", TextTools.toHexString( -0xfe98, 4, false ) );
		assertEquals( "Unexpected result.", "ab", TextTools.toHexString( 0x12ab, 2, false ) );
		assertEquals( "Unexpected result.", "55", TextTools.toHexString( -0x12ab, 2, false ) );
		assertEquals( "Unexpected result.", "98", TextTools.toHexString( 0xfe98, 2, false ) );
		assertEquals( "Unexpected result.", "68", TextTools.toHexString( -0xfe98, 2, false ) );

		assertEquals( "Unexpected result.", "00", TextTools.toHexString( 0, 2, false ) );
		assertEquals( "Unexpected result.", "ffffffff", TextTools.toHexString( -1, 8, false ) );
		assertEquals( "Unexpected result.", "ffffff", TextTools.toHexString( -1, 6, false ) );
		assertEquals( "Unexpected result.", "ffff", TextTools.toHexString( -1, 4, false ) );
		assertEquals( "Unexpected result.", "ff", TextTools.toHexString( -1, 2, false ) );
	}

	/**
	 * Test {@link TextTools#getTrimmedSubstring} method.
	 */
	@Test
	public void testGetTrimmedSubstring()
	{
		assertEquals( "Unexpected result.", "abc", TextTools.getTrimmedSubsequence( "  abc  ", 0, 5 ) );
	}

	/**
	 * Tests the {@link TextTools#wildcardPatternToRegex(String)} method.
	 */
	@Test
	public void testWildcardPatternToRegex()
	{
		assertEquals( "Unexpected pattern.", "", wildcardPatternToRegex( "" ) );
		assertEquals( "Unexpected pattern.", ".", wildcardPatternToRegex( "?" ) );
		assertEquals( "Unexpected pattern.", ".*", wildcardPatternToRegex( "*" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E", wildcardPatternToRegex( "abc" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E.", wildcardPatternToRegex( "abc?" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E..*", wildcardPatternToRegex( "abc?*" ) );
		assertEquals( "Unexpected pattern.", ".*\\Q.abc\\E", wildcardPatternToRegex( "*.abc" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\E.*\\Qb\\E.\\Qc\\E", wildcardPatternToRegex( "a*b?c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa.\\E.*\\Qb[\\E.\\Q]c\\E", wildcardPatternToRegex( "a.*b[?]c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\b\\c\\E", wildcardPatternToRegex( "a\\b\\c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\E\\\\E\\Q\\c\\E", wildcardPatternToRegex( "a\\E\\c" ) );
	}

	/**
	 * Tests the {@link TextTools#camelToUpperCase(CharSequence)} method.
	 */
	@Test
	public void testCamelToUpperCase()
	{
		System.out.println( CLASS_NAME + ".testCamelToUpperCase" );

		assertEquals( "Unexpected result.", "HELLO_WORLD", camelToUpperCase( "helloWorld" ) );
		assertEquals( "Unexpected result.", "HELLO_WORLD", camelToUpperCase( "HelloWorld" ) );
		assertEquals( "Unexpected result.", "HELLO_WORLD", camelToUpperCase( "HELLO_WORLD" ) );

		assertEquals( "Unexpected result.", "UI_DEFAULTS", camelToUpperCase( "UIDefaults" ) );
		assertEquals( "Unexpected result.", "TEST_TEXT_TOOLS", camelToUpperCase( "TestTextTools" ) );
		assertEquals( "Unexpected result.", "TEST123", camelToUpperCase( "Test123" ) );

		assertEquals( "Unexpected result.", "ABCDEFG", camelToUpperCase( "abcdefg" ) );
		assertEquals( "Unexpected result.", "ABCDEFG", camelToUpperCase( "Abcdefg" ) );
		assertEquals( "Unexpected result.", "ABCDEFG", camelToUpperCase( "ABCDEFG" ) );
		assertEquals( "Unexpected result.", "A_BCD_EFG", camelToUpperCase( "ABcdEfg" ) );
		assertEquals( "Unexpected result.", "AB_CD_EFG", camelToUpperCase( "ABCdEfg" ) );
		assertEquals( "Unexpected result.", "ABC0EFG", camelToUpperCase( "ABC0efg" ) );
		assertEquals( "Unexpected result.", "ABC0E_FG", camelToUpperCase( "ABC0eFg" ) );
		assertEquals( "Unexpected result.", "ABC0E_FG", camelToUpperCase( "ABC0eFG" ) );
		assertEquals( "Unexpected result.", "ABC0EFG", camelToUpperCase( "ABC0Efg" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", camelToUpperCase( "ABC_Efg" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", camelToUpperCase( "ABC_EFG" ) );
		assertEquals( "Unexpected result.", "A_BC_EFG", camelToUpperCase( "aBC_EFG" ) );
		assertEquals( "Unexpected result.", "AB_C_EFG", camelToUpperCase( "abC_EFG" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", camelToUpperCase( "abc_EFG" ) );
		assertEquals( "Unexpected result.", "ABC_E_FG", camelToUpperCase( "abc_eFG" ) );
		assertEquals( "Unexpected result.", "0123ABC", camelToUpperCase( "0123abc" ) );
	}
}
