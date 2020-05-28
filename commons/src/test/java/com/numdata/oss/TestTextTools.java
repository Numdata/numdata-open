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
			TextTools.escape( sb, input );
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

	@Test
	public void testGetTrimmedSubstring()
	{
		assertEquals( "Unexpected result.", "abc", TextTools.getTrimmedSubstring( "  abc  ", 0, 5 ) );

		assertEquals( "Unexpected result.", "abc", TextTools.getTrimmedSubsequence( "  abc  ", 0, 5 ) );
		assertEquals( "Unexpected result.", "abcde", TextTools.getTrimmedSubsequence( " abcde ", 1, 6 ) );
		assertEquals( "Unexpected result.", "abcde", TextTools.getTrimmedSubsequence( "  abcde  ", 2, 7 ) );
		assertEquals( "Unexpected result.", "abcde", TextTools.getTrimmedSubsequence( "  abcde  ", 1, 8 ) );
		assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( "  \t\r\n  ", 1, 6 ) );
		assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( " abc ", 3, 3 ) );

		try
		{
			assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( " abc ", -1, 2 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( final StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( " abc ", 4, -1 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( final StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( " abc ", 4, 8 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( final StringIndexOutOfBoundsException ignored )
		{
		}

		try
		{
			assertEquals( "Unexpected result.", "", TextTools.getTrimmedSubsequence( " abc ", 4, 2 ) );
			fail( "Expected 'StringIndexOutOfBoundsException'" );
		}
		catch ( final StringIndexOutOfBoundsException ignored )
		{
		}
	}

	@Test
	public void testWildcardPatternToRegex()
	{
		assertEquals( "Unexpected pattern.", "", TextTools.wildcardPatternToRegex( "" ) );
		assertEquals( "Unexpected pattern.", ".", TextTools.wildcardPatternToRegex( "?" ) );
		assertEquals( "Unexpected pattern.", ".*", TextTools.wildcardPatternToRegex( "*" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E", TextTools.wildcardPatternToRegex( "abc" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E.", TextTools.wildcardPatternToRegex( "abc?" ) );
		assertEquals( "Unexpected pattern.", "\\Qabc\\E..*", TextTools.wildcardPatternToRegex( "abc?*" ) );
		assertEquals( "Unexpected pattern.", ".*\\Q.abc\\E", TextTools.wildcardPatternToRegex( "*.abc" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\E.*\\Qb\\E.\\Qc\\E", TextTools.wildcardPatternToRegex( "a*b?c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa.\\E.*\\Qb[\\E.\\Q]c\\E", TextTools.wildcardPatternToRegex( "a.*b[?]c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\b\\c\\E", TextTools.wildcardPatternToRegex( "a\\b\\c" ) );
		assertEquals( "Unexpected pattern.", "\\Qa\\E\\\\E\\Q\\c\\E", TextTools.wildcardPatternToRegex( "a\\E\\c" ) );
	}

	@Test
	public void testCamelToUpperCase()
	{
		assertEquals( "Unexpected result.", "HELLO_WORLD", TextTools.camelToUpperCase( "helloWorld" ) );
		assertEquals( "Unexpected result.", "HELLO_WORLD", TextTools.camelToUpperCase( "HelloWorld" ) );
		assertEquals( "Unexpected result.", "HELLO_WORLD", TextTools.camelToUpperCase( "HELLO_WORLD" ) );

		assertEquals( "Unexpected result.", "UI_DEFAULTS", TextTools.camelToUpperCase( "UIDefaults" ) );
		assertEquals( "Unexpected result.", "TEST_TEXT_TOOLS", TextTools.camelToUpperCase( "TestTextTools" ) );
		assertEquals( "Unexpected result.", "TEST123", TextTools.camelToUpperCase( "Test123" ) );

		assertEquals( "Unexpected result.", "ABCDEFG", TextTools.camelToUpperCase( "abcdefg" ) );
		assertEquals( "Unexpected result.", "ABCDEFG", TextTools.camelToUpperCase( "Abcdefg" ) );
		assertEquals( "Unexpected result.", "ABCDEFG", TextTools.camelToUpperCase( "ABCDEFG" ) );
		assertEquals( "Unexpected result.", "A_BCD_EFG", TextTools.camelToUpperCase( "ABcdEfg" ) );
		assertEquals( "Unexpected result.", "AB_CD_EFG", TextTools.camelToUpperCase( "ABCdEfg" ) );
		assertEquals( "Unexpected result.", "ABC0EFG", TextTools.camelToUpperCase( "ABC0efg" ) );
		assertEquals( "Unexpected result.", "ABC0E_FG", TextTools.camelToUpperCase( "ABC0eFg" ) );
		assertEquals( "Unexpected result.", "ABC0E_FG", TextTools.camelToUpperCase( "ABC0eFG" ) );
		assertEquals( "Unexpected result.", "ABC0EFG", TextTools.camelToUpperCase( "ABC0Efg" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", TextTools.camelToUpperCase( "ABC_Efg" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", TextTools.camelToUpperCase( "ABC_EFG" ) );
		assertEquals( "Unexpected result.", "A_BC_EFG", TextTools.camelToUpperCase( "aBC_EFG" ) );
		assertEquals( "Unexpected result.", "AB_C_EFG", TextTools.camelToUpperCase( "abC_EFG" ) );
		assertEquals( "Unexpected result.", "ABC_EFG", TextTools.camelToUpperCase( "abc_EFG" ) );
		assertEquals( "Unexpected result.", "ABC_E_FG", TextTools.camelToUpperCase( "abc_eFG" ) );
		assertEquals( "Unexpected result.", "0123ABC", TextTools.camelToUpperCase( "0123abc" ) );
	}

	@Test
	public void testDecapitalize()
	{
		assertEquals( "Unexpected result for decapitalize( 'a' )", "a", TextTools.decapitalize( "a" ) );
		assertEquals( "Unexpected result for decapitalize( 'A' )", "a", TextTools.decapitalize( "A" ) );
		assertEquals( "Unexpected result for decapitalize( 'ab' )", "ab", TextTools.decapitalize( "ab" ) );
		assertEquals( "Unexpected result for decapitalize( 'Ab' )", "ab", TextTools.decapitalize( "Ab" ) );
		assertEquals( "Unexpected result for decapitalize( 'aB' )", "aB", TextTools.decapitalize( "aB" ) );
		assertEquals( "Unexpected result for decapitalize( 'AB' )", "ab", TextTools.decapitalize( "AB" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc' )", "abc", TextTools.decapitalize( "abc" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc' )", "abc", TextTools.decapitalize( "Abc" ) );
		assertEquals( "Unexpected result for decapitalize( 'aBc' )", "aBc", TextTools.decapitalize( "aBc" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABc' )", "aBc", TextTools.decapitalize( "ABc" ) );
		assertEquals( "Unexpected result for decapitalize( 'abC' )", "abC", TextTools.decapitalize( "abC" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbC' )", "abC", TextTools.decapitalize( "AbC" ) );
		assertEquals( "Unexpected result for decapitalize( 'aBC' )", "aBC", TextTools.decapitalize( "aBC" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC' )", "abc", TextTools.decapitalize( "ABC" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcdef' )", "abcdef", TextTools.decapitalize( "abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDef' )", "abcDef", TextTools.decapitalize( "abcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDEf' )", "abcDEf", TextTools.decapitalize( "abcDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'abcDEF' )", "abcDEF", TextTools.decapitalize( "abcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abcdef' )", "abcdef", TextTools.decapitalize( "Abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDef' )", "abcDef", TextTools.decapitalize( "AbcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDEf' )", "abcDEf", TextTools.decapitalize( "AbcDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'AbcDEF' )", "abcDEF", TextTools.decapitalize( "AbcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABcdef' )", "aBcdef", TextTools.decapitalize( "ABcdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABcDef' )", "aBcDef", TextTools.decapitalize( "ABcDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCdef' )", "abCdef", TextTools.decapitalize( "ABCdef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDef' )", "abcDef", TextTools.decapitalize( "ABCDef" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDEf' )", "abcdEf", TextTools.decapitalize( "ABCDEf" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABCDEF' )", "abcdef", TextTools.decapitalize( "ABCDEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1def )'", "abc1def", TextTools.decapitalize( "abc1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1Def )'", "abc1Def", TextTools.decapitalize( "abc1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'abc1DEF )'", "abc1DEF", TextTools.decapitalize( "abc1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1def )'", "abc1def", TextTools.decapitalize( "Abc1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1Def )'", "abc1Def", TextTools.decapitalize( "Abc1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'Abc1DEF )'", "abc1DEF", TextTools.decapitalize( "Abc1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1def )'", "abc1def", TextTools.decapitalize( "ABC1def" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1Def )'", "abc1Def", TextTools.decapitalize( "ABC1Def" ) );
		assertEquals( "Unexpected result for decapitalize( 'ABC1DEF )'", "abc1DEF", TextTools.decapitalize( "ABC1DEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcdef )'", "1abcdef", TextTools.decapitalize( "1abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcDef )'", "1abcDef", TextTools.decapitalize( "1abcDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1abcDEF )'", "1abcDEF", TextTools.decapitalize( "1abcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1Abcdef )'", "1Abcdef", TextTools.decapitalize( "1Abcdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1AbcDef )'", "1AbcDef", TextTools.decapitalize( "1AbcDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1AbcDEF )'", "1AbcDEF", TextTools.decapitalize( "1AbcDEF" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCdef )'", "1ABCdef", TextTools.decapitalize( "1ABCdef" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCDef )'", "1ABCDef", TextTools.decapitalize( "1ABCDef" ) );
		assertEquals( "Unexpected result for decapitalize( '1ABCDEF )'", "1ABCDEF", TextTools.decapitalize( "1ABCDEF" ) );
	}

	@Test
	public void testReplace()
	{
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', null, null )", "abcdefghabcdefgh", TextTools.replace( "abcdefghabcdefgh", null, null ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', null, 'xxx' )", "abcdefghabcdefgh", TextTools.replace( "abcdefghabcdefgh", null, "xxx" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', 'ABC' )", "ABCdefghABCdefgh", TextTools.replace( "abcdefghabcdefgh", "abc", "ABC" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', '' )", "defghdefgh", TextTools.replace( "abcdefghabcdefgh", "abc", "" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abc', null )", "defghdefgh", TextTools.replace( "abcdefghabcdefgh", "abc", null ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abcdefghabcdefgh', 'abcdefghabcdefgh' )", "abcdefghabcdefgh", TextTools.replace( "abcdefghabcdefgh", "abcdefghabcdefgh", "abcdefghabcdefgh" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'abcdefgh', 'abcdefgh' )", "abcdefghabcdefgh", TextTools.replace( "abcdefghabcdefgh", "abcdefgh", "abcdefgh" ) );
		assertEquals( "Unexpected result for replace( 'abcdefghabcdefgh', 'fghi', 'jklm' )", "abcdefghabcdefgh", TextTools.replace( "abcdefghabcdefgh", "fghi", "jklm" ) );
		assertEquals( "Unexpected result for replace( 'abcdefgh', 'abcdefghabcdefgh', 'xxx' )", "abcdefgh", TextTools.replace( "abcdefgh", "abcdefghabcdefgh", "xxx" ) );
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
		assertEquals( "Test needs to be updated to new 'MAXIMUM_FILENAME_LENGTH' value.", 255, TextTools.MAXIMUM_FILENAME_LENGTH );
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
		assertEquals( "Test needs to be updated to new 'MAXIMUM_FILENAME_LENGTH' value.", 255, TextTools.MAXIMUM_FILENAME_LENGTH );
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

	@Test
	public void truncateEscaped()
	{
		final Object[][] tests =
		{

		{ null, false, 100, "null" },
		{ "", false, 100, "" },
		{ "x", false, 100, "x" },
		{ "abc", false, 100, "abc" },
		{ "\t", false, 100, "\\t" },
		{ "\r ", false, 100, "\\r " },
		{ " \n", false, 100, " \\n" },
		{ "Hello \u0248", false, 100, "Hello \\u0248" },
		{ "\u0007", false, 100, "\\u0007" },

		{ null, false, 1, "null" },
		{ "", false, 1, "" },
		{ "x", false, 1, "x" },
		{ "abc", false, 1, "a…" },
		{ "\t", false, 1, "…" },
		{ "\r ", false, 1, "…" },
		{ " \n", false, 1, " …" },
		{ "Hello \u0248", false, 1, "H…" },
		{ "\u0007", false, 1, "…" },

		{ "", false, 2, "" },
		{ "x", false, 2, "x" },
		{ "abc", false, 2, "ab…" },
		{ "\t", false, 2, "\\t" },
		{ "\r ", false, 2, "\\r…" },
		{ " \n", false, 2, " …" },
		{ "Hello \u0248", false, 2, "He…" },
		{ "\u0007", false, 2, "…" },

		{ null, false, 3, "null" },
		{ "", false, 3, "" },
		{ "x", false, 3, "x" },
		{ "abc", false, 3, "abc" },
		{ "\t", false, 3, "\\t" },
		{ "\r ", false, 3, "\\r " },
		{ " \n", false, 3, " \\n" },
		{ "Hello \u0248", false, 3, "Hel…" },
		{ "\u0007", false, 3, "…" },

		{ null, false, 6, "null" },
		{ "", false, 6, "" },
		{ "x", false, 6, "x" },
		{ "abc", false, 6, "abc" },
		{ "\t", false, 6, "\\t" },
		{ "\r ", false, 6, "\\r " },
		{ " \n", false, 6, " \\n" },
		{ "Hello \u0248", false, 6, "Hello …" },
		{ "\u0007", false, 6, "\\u0007" },

		{ null, true, 100, "null" },
		{ "", true, 100, "''" },
		{ "x", true, 100, "'x'" },
		{ "abc", true, 100, "'abc'" },
		{ "\t", true, 100, "'\\t'" },
		{ "\r ", true, 100, "'\\r '" },
		{ " \n", true, 100, "' \\n'" },
		{ "Hello \u0248", true, 100, "'Hello \\u0248'" },
		{ "\u0007", true, 100, "'\\u0007'" },

		{ null, true, 1, "null" },
		{ "", true, 1, "''" },
		{ "x", true, 1, "'x'" },
		{ "abc", true, 1, "'a'…" },
		{ "\t", true, 1, "…" },
		{ "\r ", true, 1, "…" },
		{ " \n", true, 1, "' '…" },
		{ "Hello \u0248", true, 1, "'H'…" },
		{ "\u0007", true, 1, "…" },

		{ "", true, 2, "''" },
		{ "x", true, 2, "'x'" },
		{ "abc", true, 2, "'ab'…" },
		{ "\t", true, 2, "'\\t'" },
		{ "\r ", true, 2, "'\\r'…" },
		{ " \n", true, 2, "' '…" },
		{ "Hello \u0248", true, 2, "'He'…" },
		{ "\u0007", true, 2, "…" },

		{ null, true, 3, "null" },
		{ "", true, 3, "''" },
		{ "x", true, 3, "'x'" },
		{ "abc", true, 3, "'abc'" },
		{ "\t", true, 3, "'\\t'" },
		{ "\r ", true, 3, "'\\r '" },
		{ " \n", true, 3, "' \\n'" },
		{ "Hello \u0248", true, 3, "'Hel'…" },
		{ "\u0007", true, 3, "…" },

		{ null, true, 6, "null" },
		{ "", true, 6, "''" },
		{ "x", true, 6, "'x'" },
		{ "abc", true, 6, "'abc'" },
		{ "\t", true, 6, "'\\t'" },
		{ "\r ", true, 6, "'\\r '" },
		{ " \n", true, 6, "' \\n'" },
		{ "Hello \u0248", true, 6, "'Hello '…" },
		{ "\u0007", true, 6, "'\\u0007'" },

		};

		for ( int testIndex = 0; testIndex < tests.length; testIndex++ )
		{
			final Object[] test = tests[ testIndex ];
			final String input = (String)test[ 0 ];
			final boolean quote = (Boolean)test[ 1 ];
			final int maxLength = (Integer)test[ 2 ];
			final String expected = (String)test[ 3 ];

			final String testDescription = "test[" + testIndex + "] truncateEscaped( " + ( ( input == null ) ? "null" : TextTools.quote( TextTools.escape( input ) ) ) + ", " + quote + ", " + maxLength + " )";
			System.out.println( " - " + testDescription + " => [" + expected + "]" );

			final String result = TextTools.truncateEscaped( input, quote, maxLength );
			assertEquals( "Unexpected result from " + testDescription, expected, result );
		}
	}

}
