/*
 * Copyright (c) 2004-2020, Numdata BV, The Netherlands.
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

import static com.numdata.oss.TextTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link TextTools} class.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "SpellCheckingInspection", "JavaDoc" } )
public class TestTextTools
{
	@Test
	public void testAppendFixed()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "start" );
		TextTools.appendFixed( sb, "abc", 5 );
		sb.append( "end" );
		assertEquals( "Unexpected result.", "startabc  end", sb.toString() );

		sb.setLength( 0 );
		sb.append( "start" );
		TextTools.appendFixed( sb, "abc", 5, true, ' ' );
		sb.append( "end" );
		assertEquals( "Unexpected result.", "start  abcend", sb.toString() );

		for ( int i = 0; i < 2; i++ )
		{
			final boolean rightAligned = i == 1;

			sb.setLength( 0 );
			sb.append( "start" );
			TextTools.appendFixed( sb, "abcdef", 5, rightAligned, ' ' );
			sb.append( "end" );
			assertEquals( "Unexpected result.", "startabcdeend", sb.toString() );

			sb.setLength( 0 );
			sb.append( "start" );
			TextTools.appendFixed( sb, "", 5, rightAligned, ' ' );
			sb.append( "end" );
			assertEquals( "Unexpected result.", "start     end", sb.toString() );

			sb.setLength( 0 );
			sb.append( "start" );
			TextTools.appendFixed( sb, null, 5, rightAligned, ' ' );
			sb.append( "end" );
			assertEquals( "Unexpected result.", "start     end", sb.toString() );
		}

		sb.setLength( 0 );
		sb.append( "start" );
		TextTools.appendFixed( sb, null, -5 );
		sb.append( "end" );
		assertEquals( "Unexpected result.", "startend", sb.toString() );

		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append( "start" );
		TextTools.appendFixed( stringBuffer, "abc", 5 );
		stringBuffer.append( "end" );
		assertEquals( "Unexpected result.", "startabc  end", stringBuffer.toString() );

		stringBuffer.setLength( 0 );
		stringBuffer.append( "start" );
		TextTools.appendFixed( stringBuffer, "abc", 5, false, ' ' );
		stringBuffer.append( "end" );
		assertEquals( "Unexpected result.", "startabc  end", stringBuffer.toString() );
	}

	@Test
	public void testAppendSpace()
	{
		final StringBuilder sb = new StringBuilder();
		TextTools.appendSpace( sb, 5 );
		assertEquals( "Unexpected result.", "     ", sb.toString() );
		TextTools.appendSpace( sb, 7 );
		assertEquals( "Unexpected result.", "            ", sb.toString() );

		final StringBuffer stringBuffer = new StringBuffer();
		TextTools.appendSpace( stringBuffer, 5 );
		assertEquals( "Unexpected result.", "     ", stringBuffer.toString() );
		TextTools.appendSpace( stringBuffer, 7 );
		assertEquals( "Unexpected result.", "            ", stringBuffer.toString() );
	}

	@Test
	public void testGetFixed()
	{
		assertEquals( "Unexpected result.", "aaaaa", TextTools.getFixed( 5, 'a' ) );
		assertEquals( "Unexpected result.", "", TextTools.getFixed( 0, 'a' ) );
		assertEquals( "Unexpected result.", "", TextTools.getFixed( -3, 'a' ) );
		assertEquals( "Unexpected result.", "     ", TextTools.getFixed( null, 5, false, ' ' ) );
		assertEquals( "Unexpected result.", "     ", TextTools.getFixed( null, 5, true, ' ' ) );
		assertEquals( "Unexpected result.", "     ", TextTools.getFixed( "", 5, false, ' ' ) );
		assertEquals( "Unexpected result.", "     ", TextTools.getFixed( "", 5, true, ' ' ) );
		assertEquals( "Unexpected result.", "abc  ", TextTools.getFixed( "abc", 5, false, ' ' ) );
		assertEquals( "Unexpected result.", "  abc", TextTools.getFixed( "abc", 5, true, ' ' ) );
		assertEquals( "Unexpected result.", "abcde", TextTools.getFixed( "abcdef", 5, false, ' ' ) );
		assertEquals( "Unexpected result.", "abcde", TextTools.getFixed( "abcdef", 5, true, ' ' ) );
		assertEquals( "Unexpected result.", "", TextTools.getFixed( "abc", -1, false, ' ' ) );
		assertEquals( "Unexpected result.", "", TextTools.getFixed( "abc", -1, true, ' ' ) );
	}

	@Test
	public void testTrim()
	{
		final StringBuffer sb = new StringBuffer( "abc" );
		assertSame( "Unexpected result.", sb, TextTools.trim( sb ) );
		assertEquals( "Unexpected result.", "abc", TextTools.trim( new StringBuffer( "abc" ) ).toString() );
		assertEquals( "Unexpected result.", "abc", TextTools.trim( new StringBuffer( " abc" ) ).toString() );
		assertEquals( "Unexpected result.", "abc", TextTools.trim( new StringBuffer( "abc " ) ).toString() );
		assertEquals( "Unexpected result.", "abc", TextTools.trim( new StringBuffer( "\tabc \r\n" ) ).toString() );
		assertEquals( "Unexpected result.", "", TextTools.trim( new StringBuffer( "\t \r\n" ) ).toString() );
	}

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
		assertEquals( "Unexpected result.", "abc", getTrimmedSubstring( "  abc  ", 0, 5 ) );

		assertEquals( "Unexpected result.", "abc", getTrimmedSubsequence( "  abc  ", 0, 5 ) );
		assertEquals( "Unexpected result.", "abcde", getTrimmedSubsequence( " abcde ", 1, 6 ) );
		assertEquals( "Unexpected result.", "abcde", getTrimmedSubsequence( "  abcde  ", 2, 7 ) );
		assertEquals( "Unexpected result.", "abcde", getTrimmedSubsequence( "  abcde  ", 1, 8 ) );
		assertEquals( "Unexpected result.", "", getTrimmedSubsequence( "  \t\r\n  ", 1, 6 ) );
		assertEquals( "Unexpected result.", "", getTrimmedSubsequence( " abc ", 3, 3 ) );

		try
		{
			assertEquals( "Unexpected result.", "", getTrimmedSubsequence( " abc ", -1, 2 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", getTrimmedSubsequence( " abc ", 4, -1 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", getTrimmedSubsequence( " abc ", 4, 8 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", getTrimmedSubsequence( " abc ", 4, 2 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( StringIndexOutOfBoundsException ignored )
		{
		}
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

	@Test
	public void testReplace()
	{
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', null, null )", "abcdefghabcdefgh", replace( "abcdefghabcdefgh", null, null ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', null, 'xxx' )", "abcdefghabcdefgh", replace( "abcdefghabcdefgh", null, "xxx" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', 'ABC' )", "ABCdefghABCdefgh", replace( "abcdefghabcdefgh", "abc", "ABC" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', '' )", "defghdefgh", replace( "abcdefghabcdefgh", "abc", "" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', null )", "defghdefgh", replace( "abcdefghabcdefgh", "abc", null ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abcdefghabcdefgh', 'abcdefghabcdefgh' )", "abcdefghabcdefgh", replace( "abcdefghabcdefgh", "abcdefghabcdefgh", "abcdefghabcdefgh" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abcdefgh', 'abcdefgh' )", "abcdefghabcdefgh", replace( "abcdefghabcdefgh", "abcdefgh", "abcdefgh" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'fghi', 'jklm' )", "abcdefghabcdefgh", replace( "abcdefghabcdefgh", "fghi", "jklm" ) );
		assertEquals( "Unexpected result for replace( 'abcdefgh', 'abcdefghabcdefgh', 'xxx' )", "abcdefgh", replace( "abcdefgh", "abcdefghabcdefgh", "xxx" ) );
	}

	@Test
	public void testFormatBinary()
	{
		assertEquals( "Unexpected result.", "1.0 Ki", TextTools.formatBinary( 1024 ) ); // 1024 ^ 1
		assertEquals( "Unexpected result.", "1.0 Mi", TextTools.formatBinary( 1048576 ) ); // 1024 ^ 2
		assertEquals( "Unexpected result.", "1.0 Gi", TextTools.formatBinary( 1073741824 ) ); // 1024 ^ 3
		assertEquals( "Unexpected result.", "1.0 Ti", TextTools.formatBinary( 1099511627776L ) ); // 1024 ^ 4
		assertEquals( "Unexpected result.", "1.0 Pi", TextTools.formatBinary( 1125899906842624L ) ); // 1024 ^ 5

		assertEquals( "Unexpected result.", "1 ", TextTools.formatBinary( 1 ) );
		assertEquals( "Unexpected result.", "10 ", TextTools.formatBinary( 10 ) );
		assertEquals( "Unexpected result.", "100 ", TextTools.formatBinary( 100 ) );
		assertEquals( "Unexpected result.", "1000 ", TextTools.formatBinary( 1000 ) );
		assertEquals( "Unexpected result.", "9.77 Ki", TextTools.formatBinary( 10000 ) );
		assertEquals( "Unexpected result.", "97.7 Ki", TextTools.formatBinary( 100000 ) );
		assertEquals( "Unexpected result.", "977 Ki", TextTools.formatBinary( 1000000 ) );
		assertEquals( "Unexpected result.", "9.54 Mi", TextTools.formatBinary( 10000000 ) );
		assertEquals( "Unexpected result.", "95.4 Mi", TextTools.formatBinary( 100000000 ) );
		assertEquals( "Unexpected result.", "954 Mi", TextTools.formatBinary( 1000000000 ) );
		assertEquals( "Unexpected result.", "9.31 Gi", TextTools.formatBinary( 10000000000L ) );
		assertEquals( "Unexpected result.", "93.1 Gi", TextTools.formatBinary( 100000000000L ) );
		assertEquals( "Unexpected result.", "931 Gi", TextTools.formatBinary( 1000000000000L ) );
		assertEquals( "Unexpected result.", "9.09 Ti", TextTools.formatBinary( 10000000000000L ) );
		assertEquals( "Unexpected result.", "90.9 Ti", TextTools.formatBinary( 100000000000000L ) );
		assertEquals( "Unexpected result.", "909 Ti", TextTools.formatBinary( 1000000000000000L ) );
		assertEquals( "Unexpected result.", "8.88 Pi", TextTools.formatBinary( 10000000000000000L ) );
		assertEquals( "Unexpected result.", "88.8 Pi", TextTools.formatBinary( 100000000000000000L ) );
		assertEquals( "Unexpected result.", "888 Pi", TextTools.formatBinary( 1000000000000000000L ) );

		assertEquals( "Unexpected result.", "1 ", TextTools.formatBinary( 1, 1 ) );
		assertEquals( "Unexpected result.", "10 ", TextTools.formatBinary( 10, 1 ) );
		assertEquals( "Unexpected result.", "100 ", TextTools.formatBinary( 100, 1 ) );
		assertEquals( "Unexpected result.", "1000 ", TextTools.formatBinary( 1000, 1 ) );
		assertEquals( "Unexpected result.", "9.8 Ki", TextTools.formatBinary( 10000, 1 ) );
		assertEquals( "Unexpected result.", "97.7 Ki", TextTools.formatBinary( 100000, 1 ) );
		assertEquals( "Unexpected result.", "977 Ki", TextTools.formatBinary( 1000000, 1 ) );
		assertEquals( "Unexpected result.", "9.5 Mi", TextTools.formatBinary( 10000000, 1 ) );
		assertEquals( "Unexpected result.", "95.4 Mi", TextTools.formatBinary( 100000000, 1 ) );
		assertEquals( "Unexpected result.", "954 Mi", TextTools.formatBinary( 1000000000, 1 ) );
		assertEquals( "Unexpected result.", "9.3 Gi", TextTools.formatBinary( 10000000000L, 1 ) );
		assertEquals( "Unexpected result.", "93.1 Gi", TextTools.formatBinary( 100000000000L, 1 ) );
		assertEquals( "Unexpected result.", "931 Gi", TextTools.formatBinary( 1000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "9.1 Ti", TextTools.formatBinary( 10000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "90.9 Ti", TextTools.formatBinary( 100000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "909 Ti", TextTools.formatBinary( 1000000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "8.9 Pi", TextTools.formatBinary( 10000000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "88.8 Pi", TextTools.formatBinary( 100000000000000000L, 1 ) );
		assertEquals( "Unexpected result.", "888 Pi", TextTools.formatBinary( 1000000000000000000L, 1 ) );

		assertEquals( "Unexpected result.", "1 ", TextTools.formatBinary( 1, 0 ) );
		assertEquals( "Unexpected result.", "10 ", TextTools.formatBinary( 10, 0 ) );
		assertEquals( "Unexpected result.", "100 ", TextTools.formatBinary( 100, 0 ) );
		assertEquals( "Unexpected result.", "1000 ", TextTools.formatBinary( 1000, 0 ) );
		assertEquals( "Unexpected result.", "10 Ki", TextTools.formatBinary( 10000, 0 ) );
		assertEquals( "Unexpected result.", "98 Ki", TextTools.formatBinary( 100000, 0 ) );
		assertEquals( "Unexpected result.", "977 Ki", TextTools.formatBinary( 1000000, 0 ) );
		assertEquals( "Unexpected result.", "10 Mi", TextTools.formatBinary( 10000000, 0 ) );
		assertEquals( "Unexpected result.", "95 Mi", TextTools.formatBinary( 100000000, 0 ) );
		assertEquals( "Unexpected result.", "954 Mi", TextTools.formatBinary( 1000000000, 0 ) );
		assertEquals( "Unexpected result.", "9 Gi", TextTools.formatBinary( 10000000000L, 0 ) );
		assertEquals( "Unexpected result.", "93 Gi", TextTools.formatBinary( 100000000000L, 0 ) );
		assertEquals( "Unexpected result.", "931 Gi", TextTools.formatBinary( 1000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "9 Ti", TextTools.formatBinary( 10000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "91 Ti", TextTools.formatBinary( 100000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "909 Ti", TextTools.formatBinary( 1000000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "9 Pi", TextTools.formatBinary( 10000000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "89 Pi", TextTools.formatBinary( 100000000000000000L, 0 ) );
		assertEquals( "Unexpected result.", "888 Pi", TextTools.formatBinary( 1000000000000000000L, 0 ) );

		assertEquals( "Unexpected result.", "0 ", TextTools.formatBinary( 0 ) );

		assertEquals( "Unexpected result.", "-1 ", TextTools.formatBinary( -1 ) );
		assertEquals( "Unexpected result.", "-10 ", TextTools.formatBinary( -10 ) );
		assertEquals( "Unexpected result.", "-100 ", TextTools.formatBinary( -100 ) );
		assertEquals( "Unexpected result.", "-1000 ", TextTools.formatBinary( -1000 ) );
		assertEquals( "Unexpected result.", "-9.77 Ki", TextTools.formatBinary( -10000 ) );
		assertEquals( "Unexpected result.", "-97.7 Ki", TextTools.formatBinary( -100000 ) );
		assertEquals( "Unexpected result.", "-977 Ki", TextTools.formatBinary( -1000000 ) );
		assertEquals( "Unexpected result.", "-9.54 Mi", TextTools.formatBinary( -10000000 ) );
		assertEquals( "Unexpected result.", "-95.4 Mi", TextTools.formatBinary( -100000000 ) );
		assertEquals( "Unexpected result.", "-954 Mi", TextTools.formatBinary( -1000000000 ) );
		assertEquals( "Unexpected result.", "-9.31 Gi", TextTools.formatBinary( -10000000000L ) );
		assertEquals( "Unexpected result.", "-93.1 Gi", TextTools.formatBinary( -100000000000L ) );
		assertEquals( "Unexpected result.", "-931 Gi", TextTools.formatBinary( -1000000000000L ) );
		assertEquals( "Unexpected result.", "-9.09 Ti", TextTools.formatBinary( -10000000000000L ) );
		assertEquals( "Unexpected result.", "-90.9 Ti", TextTools.formatBinary( -100000000000000L ) );
		assertEquals( "Unexpected result.", "-909 Ti", TextTools.formatBinary( -1000000000000000L ) );
		assertEquals( "Unexpected result.", "-8.88 Pi", TextTools.formatBinary( -10000000000000000L ) );
		assertEquals( "Unexpected result.", "-88.8 Pi", TextTools.formatBinary( -100000000000000000L ) );
		assertEquals( "Unexpected result.", "-888 Pi", TextTools.formatBinary( -1000000000000000000L ) );
	}

	@Test
	public void testIsSecureFilePath()
	{
		//noinspection ConstantConditions
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( null ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "" ) );
		assertEquals( "Test needs to be updated to new 'MAXIMUM_FILENAME_LENGTH' value.", 255, MAXIMUM_FILENAME_LENGTH );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( TextTools.getFixed( 255, 'x' ) ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( TextTools.getFixed( 256, 'x' ) ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( " blabla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla bla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "blabla " ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( ".blabla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla.bla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "blabla." ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "\nblabla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "bla\nbla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "blabla\n" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "bla /bla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla/bla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "/bla/bla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla/bla/" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "/bla/bla/" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla\\bla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "bla//bla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilePath( "bla\\\\bla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilePath( "bla/bla\\bla" ) );
	}

	@Test
	public void testIsSecureFilename()
	{
		//noinspection ConstantConditions
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( null ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( "" ) );
		assertEquals( "Test needs to be updated to new 'MAXIMUM_FILENAME_LENGTH' value.", 255, MAXIMUM_FILENAME_LENGTH );
		assertTrue( "Unexpected result.", TextTools.isSecureFilename( TextTools.getFixed( 255, 'x' ) ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( TextTools.getFixed( 256, 'x' ) ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( " blabla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilename( "bla bla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( "blabla " ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( ".blabla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilename( "bla.bla" ) );
		assertTrue( "Unexpected result.", TextTools.isSecureFilename( "blabla." ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( "\nblabla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( "bla\nbla" ) );
		assertFalse( "Unexpected result.", TextTools.isSecureFilename( "blabla\n" ) );
	}

	@Test
	public void testHexDump()
	throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		TextTools.hexdump( sb, "##Hello world!\n##".getBytes(), 2, 13 );
		assertEquals( "Unexpected output.", "00000: 48 65 6C 6C 6F 20 77 6F 72 6C 64 21 0A           |Hello world!.   |\n", sb.toString() );
		sb.setLength( 0 );
		TextTools.hexdump( sb, "##Hello hexdump(..) world!\n##".getBytes(), 2, 25 );
		assertEquals( "Unexpected output.", "00000: 48 65 6C 6C 6F 20 68 65 78 64 75 6D 70 28 2E 2E  |Hello hexdump(..|\n" +
		                                    "00010: 29 20 77 6F 72 6C 64 21 0A                       |) world!.       |\n", sb.toString() );
	}

	@Test
	public void testGetDurationString()
	{
		assertEquals( "Unexpected result.", "34:18", TextTools.getDurationString( 123456789, false, false ) );
		assertEquals( "Unexpected result.", "34:17:37", TextTools.getDurationString( 123456789, true, false ) );
		assertEquals( "Unexpected result.", "34:17:36.789", TextTools.getDurationString( 123456789, true, true ) );
		assertEquals( "Unexpected result.", "34:17:36.789", TextTools.getDurationString( 123456789, false, true ) );
	}
}
