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

import static com.numdata.oss.TextTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link TextTools} class.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "SpellCheckingInspection" )
public class TestTextTools
{
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
			escape( sb, input );
			final String result = sb.toString();

			assertEquals( "test[" + testIndex + "] escape( '" + input + " ' ) failed", expected, result );
		}
	}

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

				final String result = unescape( input );
				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' ) failed", expected, result );
			}

			{
				final String expectedString = ( test.length == 4 ) ? test[ 3 ] : test[ 2 ];
				final int expectedPos = ( test.length == 4 ) ? expectedString.length() + 1 : -1;

				final StringBuffer sb = new StringBuffer();
				final int actualPos = unescape( input, 0, sb, separators );
				final String actualString = sb.toString();

				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' , <separators> ) returned invalid index", expectedPos, actualPos );
				assertEquals( "test[" + testIndex + "] unescape( '" + input + " ' , <separators> ) failed", expectedString, actualString );
			}
		}
	}

	@Test
	public void testToHexString()
	{
		assertEquals( "Unexpected result.", "1234abcd", toHexString( 0x1234abcd, 8, false ) );
		assertEquals( "Unexpected result.", "1234ABCD", toHexString( 0x1234abcd, 8, true ) );
		assertEquals( "Unexpected result.", "fedcba98", toHexString( 0xfedcba98, 8, false ) );
		assertEquals( "Unexpected result.", "FEDCBA98", toHexString( 0xfedcba98, 8, true ) );
		assertEquals( "Unexpected result.", "34abcd", toHexString( 0x1234abcd, 6, false ) );
		assertEquals( "Unexpected result.", "34ABCD", toHexString( 0x1234abcd, 6, true ) );
		assertEquals( "Unexpected result.", "dcba98", toHexString( 0xfedcba98, 6, false ) );
		assertEquals( "Unexpected result.", "DCBA98", toHexString( 0xfedcba98, 6, true ) );

		assertEquals( "Unexpected result.", "000012ab", toHexString( 0x12ab, 8, false ) );
		assertEquals( "Unexpected result.", "ffffed55", toHexString( -0x12ab, 8, false ) );
		assertEquals( "Unexpected result.", "0000fe98", toHexString( 0xfe98, 8, false ) );
		assertEquals( "Unexpected result.", "ffff0168", toHexString( -0xfe98, 8, false ) );
		assertEquals( "Unexpected result.", "0012ab", toHexString( 0x12ab, 6, false ) );
		assertEquals( "Unexpected result.", "ffed55", toHexString( -0x12ab, 6, false ) );
		assertEquals( "Unexpected result.", "00fe98", toHexString( 0xfe98, 6, false ) );
		assertEquals( "Unexpected result.", "ff0168", toHexString( -0xfe98, 6, false ) );
		assertEquals( "Unexpected result.", "12ab", toHexString( 0x12ab, 4, false ) );
		assertEquals( "Unexpected result.", "ed55", toHexString( -0x12ab, 4, false ) );
		assertEquals( "Unexpected result.", "fe98", toHexString( 0xfe98, 4, false ) );
		assertEquals( "Unexpected result.", "0168", toHexString( -0xfe98, 4, false ) );
		assertEquals( "Unexpected result.", "ab", toHexString( 0x12ab, 2, false ) );
		assertEquals( "Unexpected result.", "55", toHexString( -0x12ab, 2, false ) );
		assertEquals( "Unexpected result.", "98", toHexString( 0xfe98, 2, false ) );
		assertEquals( "Unexpected result.", "68", toHexString( -0xfe98, 2, false ) );

		assertEquals( "Unexpected result.", "00", toHexString( 0, 2, false ) );
		assertEquals( "Unexpected result.", "ffffffff", toHexString( -1, 8, false ) );
		assertEquals( "Unexpected result.", "ffffff", toHexString( -1, 6, false ) );
		assertEquals( "Unexpected result.", "ffff", toHexString( -1, 4, false ) );
		assertEquals( "Unexpected result.", "ff", toHexString( -1, 2, false ) );
	}

	@Test
	public void testGetTrimmedSubstring()
	{
		assertEquals( "Unexpected result.", "abc", getTrimmedSubsequence( "  abc  ", 0, 5 ) );
	}

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

	@Test
	public void testCamelToUpperCase()
	{
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

	@Test
	public void testDecapitalize()
	{
		assertEquals( "Unexpected result for decapitalize( 'a' )", "a", decapitalize( "a" ) );
		assertEquals( "Unexpected result for decapitalize( 'A' )", "a", decapitalize( "A" ) );
		assertEquals( "Unexpected result for decapitalize( 'ab' )", "ab", decapitalize( "ab" ) );
		assertEquals( "Unexpected result for decapitalize( 'Ab' )", "ab", decapitalize( "Ab" ) );
		assertEquals( "Unexpected result for decapitalize( 'aB' )", "aB", decapitalize( "aB" ) );
		assertEquals( "Unexpected result for decapitalize( 'AB' )", "ab", decapitalize( "AB" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc' )", "abc", decapitalize( "abc" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc' )", "abc", decapitalize( "Abc" ) );
		assertEquals( "Unexpected result for decapitalize( 'aBc' )", "aBc", decapitalize( "aBc" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABc' )", "aBc", decapitalize( "ABc" ) );
		assertEquals( "Unexpected result for decapitalize( 'abC' )", "abC", decapitalize( "abC" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbC' )", "abC", decapitalize( "AbC" ) );
		assertEquals( "Unexpected result for decapitalize( 'aBC' )", "aBC", decapitalize( "aBC" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC' )", "abc", decapitalize( "ABC" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcdef' )", "abcdef", decapitalize( "abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDef' )", "abcDef", decapitalize( "abcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDEf' )", "abcDEf", decapitalize( "abcDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDEF' )", "abcDEF", decapitalize( "abcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abcdef' )", "abcdef", decapitalize( "Abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDef' )", "abcDef", decapitalize( "AbcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDEf' )", "abcDEf", decapitalize( "AbcDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDEF' )", "abcDEF", decapitalize( "AbcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABcdef' )", "aBcdef", decapitalize( "ABcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABcDef' )", "aBcDef", decapitalize( "ABcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCdef' )", "abCdef", decapitalize( "ABCdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDef' )", "abcDef", decapitalize( "ABCDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDEf' )", "abcdEf", decapitalize( "ABCDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDEF' )", "abcdef", decapitalize( "ABCDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1def )'", "abc1def", decapitalize( "abc1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1Def )'", "abc1Def", decapitalize( "abc1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1DEF )'", "abc1DEF", decapitalize( "abc1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1def )'", "abc1def", decapitalize( "Abc1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1Def )'", "abc1Def", decapitalize( "Abc1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1DEF )'", "abc1DEF", decapitalize( "Abc1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1def )'", "abc1def", decapitalize( "ABC1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1Def )'", "abc1Def", decapitalize( "ABC1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1DEF )'", "abc1DEF", decapitalize( "ABC1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcdef )'", "1abcdef", decapitalize( "1abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcDef )'", "1abcDef", decapitalize( "1abcDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcDEF )'", "1abcDEF", decapitalize( "1abcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1Abcdef )'", "1Abcdef", decapitalize( "1Abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1AbcDef )'", "1AbcDef", decapitalize( "1AbcDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1AbcDEF )'", "1AbcDEF", decapitalize( "1AbcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCdef )'", "1ABCdef", decapitalize( "1ABCdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCDef )'", "1ABCDef", decapitalize( "1ABCDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCDEF )'", "1ABCDEF", decapitalize( "1ABCDEF" ) );
	}
}
