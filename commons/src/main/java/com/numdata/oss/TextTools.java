/*
 * Copyright (c) 2003-2020, Numdata BV, The Netherlands.
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
import java.math.*;
import java.net.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * This class is a text manipulation tool box.
 *
 * @author Sjoerd Bouwman
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "WeakerAccess", "DuplicatedCode", "OverloadedMethodsWithSameNumberOfParameters" } )
public final class TextTools
{
	/**
	 * State used for whitespace by {@link #plainTextToHTML} method.
	 *
	 * @see #plainTextToHTML
	 */
	private static final int ASCII_HTML_SPACE = 0;

	/**
	 * State used for body text by {@link #plainTextToHTML} method.
	 *
	 * @see #plainTextToHTML
	 */
	private static final int ASCII_HTML_TEXT = 1;

	/**
	 * State used for HTML tags by {@link #plainTextToHTML} method.
	 *
	 * @see #plainTextToHTML
	 */
	private static final int ASCII_HTML_TAG = 2;

	/**
	 * Digit characters used by {@link #escape} and {@link #toHexString}.
	 *
	 * @see #escape
	 * @see #toHexString
	 */
	private static final char[] UPPER_DIGITS =
	{ '0', '1', '2', '3', '4', '5', '6', '7',
	  '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Digit characters used by {@link #escape} and {@link #toHexString}.
	 *
	 * @see #escape
	 * @see #toHexString
	 */
	private static final char[] LOWER_DIGITS =
	{ '0', '1', '2', '3', '4', '5', '6', '7',
	  '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * This implementation of {@link Format} produces no output. It may be used
	 * to selectively disable components of a {@link MessageFormat}.
	 */
	public static final Format EMPTY_FORMAT = new Format()
	{
		@Override
		public StringBuffer format( final Object obj, @NotNull final StringBuffer toAppendTo, @NotNull final FieldPosition pos )
		{
			return toAppendTo;
		}

		@Override
		public Object parseObject( final String source, @NotNull final ParsePosition pos )
		{
			return source;
		}
	};

	/**
	 * Single newline pattern.
	 */
	public static final Pattern NEWLINE_PATTERN = Pattern.compile( "\\r\\n?|\\n" );

	/**
	 * One or more newlines pattern.
	 */
	public static final Pattern NEWLINES_PATTERN = Pattern.compile( "[\\r\\n]+" );

	/**
	 * One or more whitespace characters patterns.
	 */
	public static final Pattern WHITESPACE_PATTERN = Pattern.compile( "\\s+" );

	/**
	 * {@code &lt;html&gt;} element pattern.
	 */
	private static final Pattern HTML_TAG_PATTERN = Pattern.compile( "(?i)<html>" );

	/**
	 * Binary prefixes according to IEC 60027-2 A.2 and ISO/IEC 80000 standards
	 * (index = base 1024 exponent).
	 */
	private static final String[] IEC_BINARY_PREFIXES = { "", "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi" };

	/**
	 * Pattern describing which email addresses are valid.
	 */
	private static final Pattern VALID_EMAIL_ADDRESS_PATTERN = Pattern.compile( "[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]+[A-Za-z]" );

	/**
	 * Characters that are illegal for any position in a file name.
	 */
	public static final String ILLEGAL_FILENAME_CHARACTERS = "#%&$<>{}*?!/\\'`\":;@+|=";

	/**
	 * Characters that are illegal as start of a file name.
	 */
	public static final String ILLEGAL_FILENAME_START_CHARACTERS = ILLEGAL_FILENAME_CHARACTERS + "-_.";

	/**
	 * Maximum number of characters in a file name.
	 */
	public static final int MAXIMUM_FILENAME_LENGTH = 255;

	/**
	 * Platform-specific line separator.
	 */
	private static String lineSeparator = null;

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private TextTools()
	{
	}

	/**
	 * Appends a fixed-length string to an {@link Appendable}. Either fills the
	 * missing spaces with the specified character, or truncates the string.
	 *
	 * @param appendable   {@link Appendable} destination.
	 * @param source       String to fix.
	 * @param length       Length of the string to return.
	 * @param rightAligned Align text to right of resulting string.
	 * @param fillChar     Character to fill with.
	 *
	 * @throws IOException from {@link Appendable#append}.
	 */
	public static void appendFixed( @NotNull final Appendable appendable, @Nullable final CharSequence source, final int length, final boolean rightAligned, final char fillChar )
	throws IOException
	{
		if ( length > 0 )
		{
			if ( ( source == null ) || ( source.length() == 0 ) )
			{
				for ( int i = 0; i < length; i++ )
				{
					appendable.append( fillChar );
				}
			}
			else if ( source.length() < length )
			{
				if ( !rightAligned )
				{
					appendable.append( source );
				}

				for ( int i = source.length(); i < length; i++ )
				{
					appendable.append( fillChar );
				}

				if ( rightAligned )
				{
					appendable.append( source );
				}
			}
			else
			{
				for ( int i = 0; i < length; i++ )
				{
					appendable.append( source.charAt( i ) );
				}
			}
		}
	}

	/**
	 * Appends a fixed-length string to a {@link StringBuffer}. Either fills the
	 * missing spaces with spaces, or truncates the string.
	 *
	 * @param sb     {@link StringBuffer} used as destination.
	 * @param source String to fix.
	 * @param length Length of the string to return.
	 */
	public static void appendFixed( @NotNull final StringBuffer sb, @Nullable final CharSequence source, final int length )
	{
		appendFixed( sb, source, length, false, ' ' );
	}

	/**
	 * Appends a fixed-length string to a {@link StringBuffer}. Either fills the
	 * missing spaces with the specified character, or truncates the string.
	 *
	 * @param sb           {@link StringBuffer} used as destination.
	 * @param source       String to fix.
	 * @param length       Length of the string to return.
	 * @param rightAligned Align text to right of resulting string.
	 * @param fillChar     Character to fill with.
	 */
	public static void appendFixed( @NotNull final StringBuffer sb, @Nullable final CharSequence source, final int length, final boolean rightAligned, final char fillChar )
	{
		try
		{
			appendFixed( (Appendable)sb, source, length, rightAligned, fillChar );
		}
		catch ( final IOException e )
		{
			/* impossible */
		}
	}

	/**
	 * Appends a fixed-length string to a {@link StringBuilder}. Either fills
	 * the missing spaces with spaces, or truncates the string.
	 *
	 * @param sb     {@link StringBuilder} used as destination.
	 * @param source String to fix.
	 * @param length Length of the string to return.
	 */
	public static void appendFixed( @NotNull final StringBuilder sb, @Nullable final CharSequence source, final int length )
	{
		appendFixed( sb, source, length, false, ' ' );
	}

	/**
	 * Appends a fixed-length string to a {@link StringBuilder}. Either fills
	 * the missing spaces with the specified character, or truncates the
	 * string.
	 *
	 * @param sb           {@link StringBuilder} used as destination.
	 * @param source       String to fix.
	 * @param length       Length of the string to return.
	 * @param rightAligned Align text to right of resulting string.
	 * @param fillChar     Character to fill with.
	 */
	public static void appendFixed( @NotNull final StringBuilder sb, @Nullable final CharSequence source, final int length, final boolean rightAligned, final char fillChar )
	{
		try
		{
			appendFixed( (Appendable)sb, source, length, rightAligned, fillChar );
		}
		catch ( final IOException e )
		{
			/* impossible */
		}
	}

	/**
	 * Appends spaces to a {@link StringBuffer}.
	 *
	 * @param buffer {@link StringBuffer} used as destination.
	 * @param length Length of the string to return.
	 */
	public static void appendSpace( @NotNull final StringBuffer buffer, final int length )
	{
		appendFixed( buffer, null, length, false, ' ' );
	}

	/**
	 * Appends spaces to a {@link StringBuilder}.
	 *
	 * @param buffer {@link StringBuilder} used as destination.
	 * @param length Length of the string to return.
	 */
	public static void appendSpace( @NotNull final StringBuilder buffer, final int length )
	{
		appendFixed( buffer, null, length, false, ' ' );
	}

	/**
	 * Returns a fixed-length string filled with the specified character.
	 *
	 * @param length   Length of the string to return.
	 * @param fillChar Character to fill with.
	 *
	 * @return Fixed-length {@link String}.
	 */
	public static String getFixed( final int length, final char fillChar )
	{
		return getFixed( null, length, false, fillChar );
	}

	/**
	 * Returns a fixed-length string. Either fills the missing spaces with the
	 * specified character, or truncates the source string.
	 *
	 * @param source       String to fix.
	 * @param length       Length of the string to return.
	 * @param rightAligned Align text to right of resulting string.
	 * @param fillChar     Character to fill with.
	 *
	 * @return Fixed-length {@link String}.
	 */
	@NotNull
	public static String getFixed( @Nullable final String source, final int length, final boolean rightAligned, final char fillChar )
	{
		final String result;

		if ( ( source != null ) && ( source.length() >= length ) || ( length <= 0 ) )
		{
			result = length > 0 ? source.substring( 0, length ) : "";
		}
		else // source is absent or too short => need to add fill chars
		{
			final char[] chars = new char[ length ];
			int pos = 0;

			if ( source != null )
			{
				final int len = source.length();

				if ( rightAligned )
				{
					while ( pos < ( length - len ) )
					{
						chars[ pos++ ] = fillChar;
					}
				}

				for ( int i = 0; i < len; i++ )
				{
					chars[ pos++ ] = source.charAt( i );
				}
			}

			while ( pos < length )
			{
				chars[ pos++ ] = fillChar;
			}

			result = new String( chars );
		}

		return result;
	}

	/**
	 * Trim whitespace from {@link StringBuffer}.
	 *
	 * @param sb {@link StringBuffer} to trim.
	 *
	 * @return {@code sb}.
	 */
	@NotNull
	public static StringBuffer trim( @NotNull final StringBuffer sb )
	{
		final int length = sb.length();

		int start = 0;
		while ( ( start < length ) && Character.isWhitespace( sb.charAt( start ) ) )
		{
			start++;
		}

		int end = length;
		while ( ( end > start ) && Character.isWhitespace( sb.charAt( end - 1 ) ) )
		{
			end--;
		}

		if ( start > 0 )
		{
			sb.delete( 0, start );
		}

		if ( end < length )
		{
			sb.setLength( end - start );
		}

		return sb;
	}

	/**
	 * Get trimmed sub-sequence of a character sequence.
	 *
	 * @param source Char sequence to get sub-sequence of.
	 * @param start  Start index of sub-sequence (inclusive).
	 * @param end    End index of sub-sequence (exclusive).
	 *
	 * @return Trimmed sub-sequence.
	 *
	 * @throws StringIndexOutOfBoundsException if start > end.
	 */
	@NotNull
	public static CharSequence getTrimmedSubsequence( @NotNull final CharSequence source, final int start, final int end )
	{
		if ( start < 0 )
		{
			throw new StringIndexOutOfBoundsException( start );
		}

		final int length = source.length();
		if ( end > length )
		{
			throw new StringIndexOutOfBoundsException( end );
		}

		if ( start > end )
		{
			throw new StringIndexOutOfBoundsException( end - start );
		}

		int trimmedStart = start;
		int trimmedEnd = end;

		if ( trimmedStart < trimmedEnd )
		{
			while ( ( trimmedStart < trimmedEnd ) && Character.isWhitespace( source.charAt( trimmedStart ) ) )
			{
				trimmedStart++;
			}

			while ( ( trimmedStart < trimmedEnd ) && Character.isWhitespace( source.charAt( trimmedEnd - 1 ) ) )
			{
				trimmedEnd--;
			}
		}

		return source.subSequence( trimmedStart, trimmedEnd );
	}

	/**
	 * Get trimmed substring.
	 *
	 * @param source String to get substring of.
	 * @param start  Start index of substring (inclusive).
	 * @param end    End index of substring (exclusive).
	 *
	 * @return Trimmed substring.
	 */
	public static String getTrimmedSubstring( @NotNull final CharSequence source, final int start, final int end )
	{
		return (String)getTrimmedSubsequence( source, start, end );
	}

	/**
	 * Convert a plain text string to HTML formatted string. This is most useful
	 * for use in Swing tool tips (this allows you to use multi-line tool tips,
	 * for example).
	 *
	 * @param string Plain text string to convert.
	 *
	 * @return HTML-formatted text (includes '{@code &lt;html&gt;}' tag); {@code
	 * null} if {@code string} is {@code null}.
	 */
	@Nullable
	@Contract( value = "null -> null; !null -> !null", pure = true )
	public static String plainTextToHTML( @Nullable final String string )
	{
		final String result;

		if ( ( string != null ) && !string.isEmpty() && !HTML_TAG_PATTERN.matcher( string ).matches() )
		{
			final int len = string.length();
			final StringBuilder resultBuffer = new StringBuilder( len + 14 );
			final StringBuilder tokenBuffer = new StringBuilder( Math.min( 40, len / 2 ) );

			resultBuffer.append( "<html>" );

			int state = ASCII_HTML_SPACE;
			boolean inPreFormattedSection = false;

			for ( int pos = 0; pos < len; pos++ )
			{
				final char ch = string.charAt( pos );

				switch ( state )
				{
					case ASCII_HTML_SPACE:
						//noinspection fallthrough
					case ASCII_HTML_TEXT:
						if ( ch == '<' )
						{
							appendHtmlText( resultBuffer, tokenBuffer, inPreFormattedSection );

							tokenBuffer.setLength( 0 );
							tokenBuffer.append( '<' );
							state = ASCII_HTML_TAG;
						}
						else if ( !Character.isWhitespace( ch ) )
						{
							tokenBuffer.append( ch );
							state = ASCII_HTML_TEXT;
						}
						else if ( state == ASCII_HTML_TEXT )
						{
							if ( inPreFormattedSection )
							{
								tokenBuffer.append( ch );
							}
							else
							{
								tokenBuffer.append( ' ' );
								state = ASCII_HTML_SPACE;
							}
						}
						break;

					case ASCII_HTML_TAG:
						if ( ch == '>' )
						{
							tokenBuffer.append( '>' );

							if ( inPreFormattedSection )
							{
								if ( equals( "</pre>", tokenBuffer ) )
								{
									inPreFormattedSection = false;
									resultBuffer.append( tokenBuffer );
									tokenBuffer.setLength( 0 );
									state = ASCII_HTML_TEXT;
								}
								else
								{
									state = ASCII_HTML_TEXT;
								}
							}
							else
							{
								final int tokenLength = tokenBuffer.length();

								if ( equals( "<pre>", tokenBuffer ) )
								{
									inPreFormattedSection = true;
								}
								else if ( tokenBuffer.charAt( tokenLength - 2 ) == '/' )
								{
									tokenBuffer.delete( tokenLength - 2, tokenLength - 1 );
								}

								resultBuffer.append( tokenBuffer );
								tokenBuffer.setLength( 0 );
								state = ASCII_HTML_TEXT;
							}
						}
						else if ( !Character.isWhitespace( ch ) )
						{
							tokenBuffer.append( ch );
						}
						break;
				}
			}

			appendHtmlText( resultBuffer, tokenBuffer, inPreFormattedSection );

//			if ( inPreFormattedSection )
//				resultBuffer.append( "</pre>" );

//			resultBuffer.append( "</html>" );

			result = resultBuffer.toString();
		}
		else
		{
			result = string;
		}

		return result;
	}

	private static void appendHtmlText( final StringBuilder result, final StringBuilder tokenBuffer, final boolean inPreFormattedSection )
	{
		if ( !inPreFormattedSection )
		{
			while ( ( tokenBuffer.length() > 0 ) && Character.isWhitespace( tokenBuffer.charAt( tokenBuffer.length() - 1 ) ) )
			{
				tokenBuffer.setLength( tokenBuffer.length() - 1 );
			}

			while ( tokenBuffer.length() > 66 )
			{
				final int breakAt = tokenBuffer.lastIndexOf( " ", 66 );
				if ( ( breakAt < 0 ) || ( tokenBuffer.indexOf( " ", breakAt + 1 ) < 0 ) )
				{
					break;
				}

				result.ensureCapacity( breakAt + 4 );
				for ( int i = 0; i < breakAt; i++ )
				{
					result.append( tokenBuffer.charAt( i ) );
				}
				result.append( "<br>" );
				tokenBuffer.delete( 0, breakAt + 1 );
			}

			result.append( tokenBuffer );
		}
		else
		{
			final int len = tokenBuffer.length();
			result.ensureCapacity( len );

			for ( int i = 0; i < len; i++ )
			{
				final char c = tokenBuffer.charAt( i );
				switch ( c )
				{
					case '<':
						result.append( "&lt;" );
						break;
					case '>':
						result.append( "&gt;" );
						break;
					default:
						result.append( c );
				}
			}
		}
	}

	/**
	 * Compare if two {@link String}s are equal. Both arguments may be {@code
	 * null}, and {@code null} is always less than non-{@code null}.
	 *
	 * @param s1 First string to compare.
	 * @param s2 Second string to compare.
	 *
	 * @return {@code 0} if the strings both {@code null} or equal; a value less
	 * than {@code 0} if {@code string1} is {@code null} or lexicographically
	 * less than {@code string2}; and a value greater than {@code 0} if {@code
	 * string1} is lexicographically greater than {@code string2} or {@code
	 * string2} is {@code null}.
	 */
	@SuppressWarnings( "TypeMayBeWeakened" )
	public static int compare( @Nullable final String s1, @Nullable final String s2 )
	{
		return ( s1 == null ) ? ( s2 != null ) ? -1 : 0 : ( s2 != null ) ? s1.compareTo( s2 ) : 1;
	}

	/**
	 * Test if two {@link CharSequence}s are equal. Both arguments may be {@code
	 * null}. This will return {@code true} if both objects are {@code null} or
	 * if both character sequences have the same content.
	 *
	 * @param cs1 First {@link CharSequence} (may be {@code null}).
	 * @param cs2 Second {@link CharSequence} (may be {@code null}).
	 *
	 * @return {@code true} if the string objects are equal; {@code false}
	 * otherwise.
	 */
	@Contract( value = "null, null -> true", pure = true )
	public static boolean equals( @Nullable final CharSequence cs1, @Nullable final CharSequence cs2 )
	{
		@SuppressWarnings( "ObjectEquality" ) boolean result = ( cs1 == cs2 );

		if ( !result && ( cs1 != null ) && ( cs2 != null ) )
		{
			int pos = cs1.length();

			result = ( pos == cs2.length() );

			while ( result && ( --pos >= 0 ) )
			{
				result = ( cs2.charAt( pos ) == cs1.charAt( pos ) );
			}
		}

		return result;
	}

	/**
	 * Test if two {@link CharSequence}s are equal. Both arguments may be {@code
	 * null}. This will return {@code true} if both objects are {@code null} or
	 * if both character sequences have the same content.
	 *
	 * @param cs1 First {@link CharSequence} (may be {@code null}).
	 * @param cs2 Second {@link CharSequence} (may be {@code null}).
	 *
	 * @return {@code true} if the string objects are equal; {@code false}
	 * otherwise.
	 */
	@Contract( value = "null, null -> true", pure = true )
	public static boolean equalsIgnoreCase( @Nullable final CharSequence cs1, @Nullable final CharSequence cs2 )
	{
		@SuppressWarnings( "ObjectEquality" ) boolean result = ( cs1 == cs2 );

		if ( !result && ( cs1 != null ) && ( cs2 != null ) )
		{
			int pos = cs1.length();

			result = ( pos == cs2.length() );

			while ( result && ( --pos >= 0 ) )
			{
				final char c1 = cs1.charAt( pos );
				final char c2 = cs2.charAt( pos );

				/* Read {@link String#equalsIgnoreCase} implementation for reason why we test lower AND upper case */
				result = ( Character.toUpperCase( c1 ) == Character.toUpperCase( c2 ) ) &&
				         ( Character.toLowerCase( c1 ) == Character.toLowerCase( c2 ) );
			}
		}

		return result;
	}

	/**
	 * Test whether a string contains another string while ignoring lower/upper
	 * case differences.
	 *
	 * @param text     First {@link CharSequence} (may be {@code null}).
	 * @param sequence Second {@link CharSequence} (may be {@code null}).
	 *
	 * @return {@code true} if the string objects are equal; {@code false}
	 * otherwise.
	 */
	@Contract( value = "null, _ -> false; _, null -> false", pure = true )
	public static boolean containsIgnoreCase( @Nullable final CharSequence text, @Nullable final CharSequence sequence )
	{
		return ( indexOfIgnoreCase( text, sequence ) >= 0 );
	}

	/**
	 * Test whether a string contains another string while ignoring lower/upper
	 * case differences.
	 *
	 * @param text     First {@link CharSequence} (may be {@code null}).
	 * @param sequence Second {@link CharSequence} (may be {@code null}).
	 *
	 * @return {@code true} if the string objects are equal; {@code false}
	 * otherwise.
	 */
	@Contract( pure = true )
	public static int indexOfIgnoreCase( @Nullable final CharSequence text, @Nullable final CharSequence sequence )
	{
		int result = -1;

		if ( ( text != null ) && ( sequence != null ) )
		{
			final int textLength = text.length();
			final int sequenceLength = sequence.length();

			final int maxIndex = textLength - sequenceLength;
			for ( int index = 0; index <= maxIndex && result < 0; index++ )
			{
				result = index;
				for ( int j = 0; j < sequenceLength; j++ )
				{
					final char c1 = sequence.charAt( j );
					final char c2 = text.charAt( index + j );

					/* Read {@link String#equalsIgnoreCase} implementation for reason why we test lower AND upper case */
					if ( ( Character.toUpperCase( c1 ) != Character.toUpperCase( c2 ) ) ||
					     ( Character.toLowerCase( c1 ) != Character.toLowerCase( c2 ) ) )
					{
						result = -1;
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * This method tests whether the specified string is {@code null}, is zero
	 * length, or consists only of whitespace characters (as classified per
	 * {@link Character#isWhitespace(char)}.
	 *
	 * @param string String to examine.
	 *
	 * @return {@code true} if the string is empty; {@code false} otherwise.
	 */
	@Contract( value = "null -> true", pure = true )
	public static boolean isEmpty( @Nullable final CharSequence string )
	{
		boolean result = ( string == null );
		if ( !result )
		{
			result = true;
			for ( int i = string.length(); --i >= 0; )
			{
				if ( !Character.isWhitespace( string.charAt( i ) ) )
				{
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * This method tests whether the specified string is not {@code null}, has a
	 * length larger than {@code 0}, and does not only contain whitespace
	 * characters (identified by {@link Character#isWhitespace(char)}.
	 *
	 * @param string String to examine.
	 *
	 * @return {@code true} if the string is not empty; {@code false} if the
	 * string is {@code null} or contains no non-whitespace characters.
	 */
	@Contract( value = "null -> false", pure = true )
	public static boolean isNonEmpty( @Nullable final CharSequence string )
	{
		return !isEmpty( string );
	}

	/**
	 * Tests if the specified string starts with the specified prefix
	 * character.
	 *
	 * @param string String to examine.
	 * @param prefix Prefix character to test for.
	 *
	 * @return {@code true} if the string starts with the specified prefix;
	 * {@code false} if the string is {@code null} or does not start with the
	 * specified prefix.
	 */
	@Contract( value = "null, _ -> false", pure = true )
	public static boolean startsWith( @Nullable final CharSequence string, final char prefix )
	{
		boolean result = ( string != null );
		if ( result )
		{
			final int length = string.length();
			result = ( length > 0 ) && ( string.charAt( 0 ) == prefix );
		}

		return result;
	}

	/**
	 * Tests if the specified string starts with the specified prefix ignoring
	 * character case.
	 *
	 * @param string String to examine.
	 * @param prefix Prefix to test for.
	 *
	 * @return {@code true} if the string starts with the specified prefix;
	 * {@code false} if either argument is {@code null} or the {@code string}
	 * does not start with the specified prefix.
	 */
	@Contract( value = "null, _ -> false; _, null -> false", pure = true )
	public static boolean startsWithIgnoreCase( @Nullable final String string, @Nullable final String prefix )
	{
		return ( string != null ) && ( prefix != null ) && string.regionMatches( true, 0, prefix, 0, prefix.length() );
	}

	/**
	 * Tests if the specified string ends with the specified suffix character.
	 *
	 * @param string String to examine.
	 * @param suffix Suffix character to test for.
	 *
	 * @return {@code true} if the string ends with the specified suffix; {@code
	 * false} if the string is {@code null} or does not end with the specified
	 * suffix.
	 */
	@Contract( value = "null, _ -> false", pure = true )
	public static boolean endsWith( @Nullable final CharSequence string, final char suffix )
	{
		boolean result = ( string != null );
		if ( result )
		{
			final int length = string.length();
			result = ( length > 0 ) && ( string.charAt( length - 1 ) == suffix );
		}

		return result;
	}

	/**
	 * Tests if the specified string ends with the specified suffix ignoring
	 * character case.
	 *
	 * @param string String to examine.
	 * @param suffix Suffix to test for.
	 *
	 * @return {@code true} if the string ends with the specified suffix; {@code
	 * false} if either argument is {@code null} or the {@code string} does not
	 * end with the specified suffix.
	 */
	@Contract( value = "null, _ -> false; _, null -> false", pure = true )
	public static boolean endsWithIgnoreCase( @Nullable final String string, @Nullable final String suffix )
	{
		return ( string != null ) && ( suffix != null ) && string.regionMatches( true, string.length() - suffix.length(), suffix, 0, suffix.length() );
	}

	/**
	 * Add escapes to a string (e.g. to use in SQL queries).
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. See {@link
	 * #escape(Appendable, char)} for details.
	 *
	 * @param source String to escape.
	 *
	 * @return Escaped string.
	 *
	 * @see #unescape
	 */
	@NotNull
	public static String escape( @NotNull final CharSequence source )
	{
		final StringBuilder sb = new StringBuilder();
		escape( sb, source );
		return sb.toString();
	}

	/**
	 * Add escapes to a string (e.g. to use in SQL queries) and appends the
	 * result to the specified string buffer.
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. See {@link
	 * #escape(Appendable, char)} for details.
	 *
	 * @param buffer {@link Appendable} destination.
	 * @param source String to escape.
	 *
	 * @throws IOException from {@link Appendable#append}.
	 * @see #unescape
	 */
	public static void escape( @NotNull final Appendable buffer, @NotNull final CharSequence source )
	throws IOException
	{
		final int len = source.length();

		for ( int index = 0; index < len; index++ )
		{
			escape( buffer, source.charAt( index ) );
		}
	}

	/**
	 * Append character to an {@link Appendable}. Escape codes are added when
	 * necessary.
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. The
	 * following characters are escape: ISO control characters, characters not
	 * in ISO-8859-1, single quote {@code '\''}, double quote {@code '"'} and
	 * backslash {@code '\\'}.
	 *
	 * @param buffer {@link Appendable} destination.
	 * @param ch     Character to append.
	 *
	 * @throws IOException from {@link Appendable#append}.
	 */
	public static void escape( @NotNull final Appendable buffer, final char ch )
	throws IOException
	{
		switch ( ch )
		{
			case '\0':
				buffer.append( '\\' );
				buffer.append( '0' );
				break;

			case '\b':
				buffer.append( '\\' );
				buffer.append( 'b' );
				break;

			case '\f':
				buffer.append( '\\' );
				buffer.append( 'f' );
				break;

			case '\n':
				buffer.append( '\\' );
				buffer.append( 'n' );
				break;

			case '\r':
				buffer.append( '\\' );
				buffer.append( 'r' );
				break;

			case '\t':
				buffer.append( '\\' );
				buffer.append( 't' );
				break;

			case '\\':
				//noinspection fallthrough
			case '\'':
				//noinspection fallthrough
			case '"':
				buffer.append( '\\' );
				buffer.append( ch );
				break;

			default:
				if ( ch > '\u00FF' || Character.isISOControl( ch ) )
				{
					final char[] upperDigits = UPPER_DIGITS;

					buffer.append( '\\' );
					buffer.append( 'u' );
					buffer.append( upperDigits[ ( ch >> 12 ) & 0x0F ] );
					buffer.append( upperDigits[ ( ch >> 8 ) & 0x0F ] );
					buffer.append( upperDigits[ ( ch >> 4 ) & 0x0F ] );
					buffer.append( upperDigits[ (int)ch & 0x0F ] );
				}
				else
				{
					buffer.append( ch );
				}
		}
	}

	/**
	 * Add escapes to a string (e.g. to use in SQL queries) and appends the
	 * result to the specified string buffer.
	 *
	 * @param sb     String buffer destination.
	 * @param source String to escape.
	 *
	 * @see #unescape
	 */
	public static void escape( @NotNull final StringBuffer sb, @NotNull final CharSequence source )
	{
		final int len = source.length();

		for ( int index = 0; index < len; index++ )
		{
			escape( sb, source.charAt( index ) );
		}
	}

	/**
	 * Append character to string. Escape codes are added when necessary.
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. See {@link
	 * #escape(Appendable, char)} for details.
	 *
	 * @param sb String buffer destination.
	 * @param c  Character to append.
	 */
	public static void escape( @NotNull final StringBuffer sb, final char c )
	{
		try
		{
			escape( (Appendable)sb, c );
		}
		catch ( final Exception e )
		{
			/* impossible */
		}
	}

	/**
	 * Add escapes to a string (e.g. to use in SQL queries) and appends the
	 * result to the specified string builder.
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. See {@link
	 * #escape(Appendable, char)} for details.
	 *
	 * @param sb     String builder destination.
	 * @param source String to escape.
	 *
	 * @see #unescape
	 */
	public static void escape( @NotNull final StringBuilder sb, @NotNull final CharSequence source )
	{
		final int len = source.length();

		for ( int index = 0; index < len; index++ )
		{
			escape( sb, source.charAt( index ) );
		}
	}

	/**
	 * Append character to string. Escape codes are added when necessary.
	 *
	 * This method escapes characters for use in a string or character literal,
	 * using the syntax defined in the Java Language Specification. See {@link
	 * #escape(Appendable, char)} for details.
	 *
	 * @param sb String builder destination.
	 * @param ch Character to append.
	 */
	public static void escape( @NotNull final StringBuilder sb, final char ch )
	{
		try
		{
			escape( (Appendable)sb, ch );
		}
		catch ( final Exception e )
		{
			/* impossible */
		}
	}

	/**
	 * Get localized currency format.
	 *
	 * @param locale Locale to get currency format of.
	 * @param symbol Currency symbol to use.
	 *
	 * @return {@link NumberFormat} representing the localized currency format.
	 */
	public static DecimalFormat getCurrencyFormat( @NotNull final Locale locale, @NotNull final String symbol )
	{
		final DecimalFormat format = (DecimalFormat)NumberFormat.getCurrencyInstance( locale );
		format.setRoundingMode( RoundingMode.HALF_UP );
		final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
		symbols.setCurrencySymbol( symbol );
		symbols.setInternationalCurrencySymbol( symbol );
		format.setDecimalFormatSymbols( symbols );
		return format;
	}

	/**
	 * Get localized currency format.
	 *
	 * @param locale   Locale to get currency format of.
	 * @param currency Currency to use.
	 *
	 * @return {@link NumberFormat} representing the localized currency format.
	 */
	public static DecimalFormat getCurrencyFormat( @NotNull final Locale locale, @NotNull final Currency currency )
	{
		final DecimalFormat result = (DecimalFormat)NumberFormat.getCurrencyInstance( locale );
		result.setRoundingMode( RoundingMode.HALF_UP );
		result.setCurrency( currency );

		final int fractionDigits = currency.getDefaultFractionDigits();
		if ( fractionDigits >= 0 )
		{
			result.setMinimumFractionDigits( fractionDigits );
			result.setMaximumFractionDigits( fractionDigits );
		}

		return result;
	}

	/**
	 * Get localized date (not including time) format.
	 *
	 * @param locale Locale to use for localized formatting.
	 *
	 * @return {@link DateFormat} representing the localized date format.
	 */
	public static SimpleDateFormat getDateFormat( @NotNull final Locale locale )
	{
		return (SimpleDateFormat)DateFormat.getDateInstance( DateFormat.SHORT, locale );
	}

	/**
	 * Get localized date/time format.
	 *
	 * @param locale Locale to use for localized formatting.
	 *
	 * @return {@link DateFormat} representing the localized date/time format.
	 */
	public static SimpleDateFormat getDateTimeFormat( @NotNull final Locale locale )
	{
		return (SimpleDateFormat)DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, locale );
	}

	/**
	 * Get line separator for the current platform.
	 *
	 * @return Platform-specific line separator.
	 */
	public static String getLineSeparator()
	{
		String result = lineSeparator;
		if ( result == null )
		{
			final StringWriter sw = new StringWriter();
			//noinspection IOResourceOpenedButNotSafelyClosed
			final PrintWriter pw = new PrintWriter( sw );
			pw.println();
			result = sw.toString();

			lineSeparator = result;
		}
		return result;
	}

	/**
	 * Get string with list of objects separated by the specified separator.
	 * {@code null}-elements are removed from the result.
	 *
	 * @param separator Separator character between objects.
	 * @param objects   Objects.
	 *
	 * @return String with list of objects (may be empty, never {@code null}).
	 */
	@NotNull
	public static String getList( @NotNull final char separator, @NotNull final Object... objects )
	{
		return getList( Arrays.asList( objects ), separator );
	}

	/**
	 * Get string with list of objects separated by the specified separator
	 * character. {@code null}-elements are removed from the result.
	 *
	 * @param list      List/array of objects.
	 * @param separator Separator character between objects.
	 *
	 * @return String with list of objects (may be empty, never {@code null}).
	 */
	@NotNull
	public static String getList( @Nullable final Iterable<?> list, final char separator )
	{
		StringBuilder sb = null;
		String first = null;

		if ( list != null )
		{
			for ( final Object object : list )
			{
				if ( object != null )
				{
					final String string = String.valueOf( object );
					if ( sb == null )
					{
						if ( first == null )
						{
							first = string;
						}
						else
						{
							sb = new StringBuilder( first.length() + 1 + string.length() );
							sb.append( first );
							sb.append( separator );
							sb.append( string );
						}
					}
					else
					{
						sb.ensureCapacity( sb.length() + 1 + string.length() );
						sb.append( separator );
						sb.append( string );
					}
				}
			}
		}

		return ( sb != null ) ? sb.toString() : ( first != null ) ? first : "";
	}

	/**
	 * Get string with list of objects separated by the specified separator.
	 * {@code null}-elements are removed from the result.
	 *
	 * @param separator Separator character between objects.
	 * @param objects   Objects.
	 *
	 * @return String with list of objects (may be empty, never {@code null}).
	 */
	@NotNull
	public static String getList( @NotNull final CharSequence separator, @NotNull final Object... objects )
	{
		return getList( Arrays.asList( objects ), separator );
	}

	/**
	 * Get string with list of objects separated by the specified separator.
	 * {@code null}-elements are removed from the result.
	 *
	 * @param list      List/array of objects.
	 * @param separator Separator character between objects.
	 *
	 * @return String with list of objects (may be empty, never {@code null}).
	 */
	@NotNull
	public static String getList( @Nullable final Iterable<?> list, @NotNull final CharSequence separator )
	{
		StringBuilder sb = null;
		String first = null;

		if ( list != null )
		{
			for ( final Object object : list )
			{
				if ( object != null )
				{
					final String string = String.valueOf( object );
					if ( sb == null )
					{
						if ( first == null )
						{
							first = string;
						}
						else
						{
							sb = new StringBuilder( first.length() + separator.length() + string.length() );
							sb.append( first );
							sb.append( separator );
							sb.append( string );
						}
					}
					else
					{
						sb.ensureCapacity( sb.length() + separator.length() + string.length() );
						sb.append( separator );
						sb.append( string );
					}
				}
			}
		}

		return ( sb != null ) ? sb.toString() : ( first != null ) ? first : "";
	}

	/**
	 * Get list of value names for the given enumeration type.
	 *
	 * @param enumClass Enumeration type to get names of.
	 *
	 * @return Names of enumeration values.
	 */
	@NotNull
	public static List<String> getEnumNames( @NotNull final Class<? extends Enum<?>> enumClass )
	{
		final Enum<?>[] enumConstants = enumClass.getEnumConstants();
		final List<String> result = new ArrayList<String>( enumConstants.length );
		for ( final Enum<?> enumConstant : enumConstants )
		{
			result.add( enumConstant.name() );
		}
		return result;
	}

	/**
	 * Get format to use for numbers. Grouping is disabled.
	 *
	 * @param locale Locale to use for localized formatting.
	 *
	 * @return Number format.
	 */
	@NotNull
	public static NumberFormat getNumberFormat( @NotNull final Locale locale )
	{
		final NumberFormat result = NumberFormat.getNumberInstance( locale );
		result.setRoundingMode( RoundingMode.HALF_UP );
		result.setGroupingUsed( false );
		return result;
	}

	/**
	 * Get format to use for numbers with the specified amount of fraction
	 * digits. Grouping is disabled.
	 *
	 * @param locale                Locale to use for localized formatting.
	 * @param minimumFractionDigits Minimum number of fraction digits.
	 * @param maximumFractionDigits Maximum number of fraction digits.
	 * @param groupingUsed          Use grouping of digits or not.
	 *
	 * @return Number format.
	 */
	@NotNull
	public static NumberFormat getNumberFormat( @NotNull final Locale locale, final int minimumFractionDigits, final int maximumFractionDigits, final boolean groupingUsed )
	{
		final NumberFormat result;

		if ( minimumFractionDigits > maximumFractionDigits )
		{
			throw new IllegalArgumentException( minimumFractionDigits + " > " + maximumFractionDigits );
		}

		if ( maximumFractionDigits == 0 )
		{
			result = NumberFormat.getIntegerInstance( locale );
		}
		else
		{
			result = getNumberFormat( locale );
			result.setMinimumFractionDigits( minimumFractionDigits );
			result.setMaximumFractionDigits( maximumFractionDigits );
		}

		result.setRoundingMode( RoundingMode.HALF_UP );
		result.setGroupingUsed( groupingUsed );

		return result;
	}

	/**
	 * Get localized percent format.
	 *
	 * @param locale Locale to use for localized formatting.
	 *
	 * @return {@link NumberFormat} representing the localized percent format.
	 */
	@NotNull
	public static NumberFormat getPercentFormat( @NotNull final Locale locale )
	{
		final NumberFormat result = NumberFormat.getPercentInstance( locale );
		result.setRoundingMode( RoundingMode.HALF_UP );
		return result;
	}

	/**
	 * Read lines from a resource references by an URL. This will properly
	 * handle all common line separators (CR+LF, LF only, or CR only).
	 *
	 * @param url URL to load text from.
	 *
	 * @return Lines.
	 *
	 * @throws IOException if a read error occurs.
	 */
	@NotNull
	public static List<String> readLines( @NotNull final URL url )
	throws IOException
	{
		final URLConnection connection = url.openConnection();
		final String contentEncoding = connection.getContentEncoding();

		final InputStream is = connection.getInputStream();
		try
		{
			final Reader reader = ( contentEncoding != null ) ? new InputStreamReader( is, contentEncoding ) : new InputStreamReader( is );
			return readLines( reader );
		}
		finally
		{
			is.close();
		}
	}

	/**
	 * Read lines from a file. This will properly handle all common line
	 * separators (CR+LF, LF only, or CR only).
	 *
	 * @param file File to read from.
	 *
	 * @return Lines.
	 *
	 * @throws IOException if a read error occurs.
	 */
	@NotNull
	public static List<String> readLines( @NotNull final File file )
	throws IOException
	{
		final Reader is = new FileReader( file );
		try
		{
			return readLines( is );
		}
		finally
		{
			is.close();
		}
	}

	/**
	 * Read lines from an input stream. This will properly handle all common
	 * line separators (CR+LF, LF only, or CR only).
	 *
	 * @param is          Stream to read from.
	 * @param charsetName Name of a supported character set.
	 *
	 * @return Lines.
	 *
	 * @throws IOException if a read error occurs.
	 */
	@NotNull
	public static List<String> readLines( @NotNull final InputStream is, @NotNull final String charsetName )
	throws IOException
	{
		try
		{
			return readLines( new InputStreamReader( is, charsetName ) );
		}
		catch ( final UnsupportedEncodingException e )
		{
			throw new IllegalArgumentException( "Unsupported encoding '" + charsetName + '\'', e );
		}
	}

	/**
	 * Read lines from a character stream. This will properly handle all common
	 * line separators (CR+LF, LF only, or CR only).
	 *
	 * @param reader Character stream to read lines from.
	 *
	 * @return Lines.
	 *
	 * @throws IOException if a read error occurs.
	 */
	@NotNull
	public static List<String> readLines( @NotNull final Reader reader )
	throws IOException
	{
		final List<String> result = new ArrayList<String>();

		final StringBuilder sb = new StringBuilder();

		int i = reader.read();
		while ( i >= 0 )
		{
			if ( ( i == (int)'\r' ) || ( i == (int)'\n' ) )
			{
				result.add( sb.toString() );
				sb.setLength( 0 );

				final int n = reader.read();
				i = ( ( i != n ) && ( ( n == (int)'\r' ) || ( n == (int)'\n' ) ) ) ? reader.read() : n;
			}
			else
			{
				sb.append( (char)i );
				i = reader.read();
			}
		}

		if ( sb.length() > 0 )
		{
			result.add( sb.toString() );
		}

		return result;
	}

	/**
	 * Count number of line in a string. Every line separator in the string is
	 * counted as a line; and, if there are any characters after the last line
	 * separator or if a non-empty string has no separators at all, that is also
	 * counted as one line.
	 *
	 * @param string Character to count number of text lines in.
	 *
	 * @return Number of text lines.
	 */
	public static int getLineCount( @NotNull final CharSequence string )
	{
		final int stringLength = string.length();

		int result = 0;
		int pos = 0;
		boolean inLine = false;

		while ( pos < stringLength )
		{
			final char ch = string.charAt( pos++ );

			if ( ( ch == '\r' ) || ( ch == '\n' ) )
			{
				result++;
				inLine = false;

				if ( pos < stringLength )
				{
					final char next = string.charAt( pos );
					if ( ( ch != next ) && ( ( next == '\r' ) || ( next == '\n' ) ) )
					{
						pos++;
					}
				}
			}
			else
			{
				inLine = true;
			}
		}

		if ( inLine )
		{
			result++;
		}

		return result;
	}

	/**
	 * Get maximum line length in a string.
	 *
	 * @param string Character with text to analyze.
	 *
	 * @return Maximum line length in the specified string.
	 */
	public static int getMaximumLineLength( @NotNull final CharSequence string )
	{
		final int stringLength = string.length();

		int result = 0;
		int lineLength = 0;

		for ( int pos = 0; pos < stringLength; pos++ )
		{
			final char c = string.charAt( pos );

			if ( ( c == '\r' ) || ( c == '\n' ) )
			{
				result = Math.max( result, lineLength );
				lineLength = 0;
			}
			else
			{
				lineLength++;
			}
		}

		result = Math.max( result, lineLength );

		return result;
	}

	/**
	 * Loads the contents of a text file. This method should be used to quickly
	 * load a text file without to much ding dong.
	 *
	 * @param url URL to load text from.
	 *
	 * @return Contents of the file; {@code null} if there was a problem reading
	 * the file.
	 */
	@Nullable
	public static String loadText( @NotNull final URL url )
	{
		String result = null;

		try
		{
			final InputStream is = url.openStream();
			try
			{
				result = loadText( is );
			}
			finally
			{
				is.close();
			}
		}
		catch ( final IOException e )
		{ /* ignore, will return null */ }

		return result;
	}

	/**
	 * Loads the contents of a text file. This method should be used to quickly
	 * load a text file without to much ding dong.
	 *
	 * @param url     URL to load text from.
	 * @param charset Charset to use.
	 *
	 * @return Contents of the file; {@code null} if there was a problem reading
	 * the file.
	 */
	@Nullable
	public static String loadText( final URL url, final Charset charset )
	{
		String result = null;

		try
		{
			final InputStream is = url.openStream();
			try
			{
				result = loadText( new InputStreamReader( is, charset ) );
			}
			finally
			{
				is.close();
			}
		}
		catch ( final IOException e )
		{ /* ignore, will return null */ }

		return result;
	}

	/**
	 * Loads the contents of a text file. This method should be used to quickly
	 * load a text file without to much ding dong.
	 *
	 * @param file File to load text from.
	 *
	 * @return Contents of the file; {@code null} if there was a problem reading
	 * the file.
	 */
	@Nullable
	public static String loadText( @NotNull final File file )
	{
		String result = null;
		try
		{
			final FileReader reader = new FileReader( file );
			try
			{
				result = loadText( reader );
			}
			finally
			{
				reader.close();
			}
		}
		catch ( final FileNotFoundException e )
		{
			/* ignore, will return null */
		}
		catch ( final IOException e )
		{
			/* ignore, will return null */
		}

		return result;
	}

	/**
	 * Loads the contents of a text document into a string. This method should
	 * be used to quickly load a text file without to much ding dong.
	 *
	 * @param in Stream to read from.
	 *
	 * @return Contents of the file.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static String loadText( @NotNull final InputStream in )
	throws IOException
	{
		final String result;

		int i = in.read();
		if ( i < 0 )
		{
			result = "";
		}
		else
		{
			final StringBuilder sb = new StringBuilder( Math.min( 10, in.available() ) );

			while ( i >= 0 )
			{
				sb.append( (char)( ( i == (int)'\r' ) ? (int)'\n' : i ) );
				final int next = in.read();
				i = ( ( i == (int)'\r' && next == (int)'\n' ) ||
				      ( i == (int)'\n' && next == (int)'\r' ) ) ? in.read() : next;
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Loads the contents of a text document into a string. This method should
	 * be used to quickly load a text file without to much ding dong.
	 *
	 * @param reader Source to read from.
	 *
	 * @return Contents of the file.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static String loadText( @NotNull final Reader reader )
	throws IOException
	{
		final String result;

		int i = reader.read();
		if ( i < 0 )
		{
			result = "";
		}
		else
		{
			final StringBuilder sb = new StringBuilder();

			while ( i >= 0 )
			{
				sb.append( (char)( ( i == (int)'\r' ) ? (int)'\n' : i ) );
				final int nc = reader.read();
				i = ( ( i == (int)'\r' && nc == (int)'\n' ) ||
				      ( i == (int)'\n' && nc == (int)'\r' ) ) ? reader.read() : nc;
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Quote an object.
	 *
	 * @param source Char sequence to quote.
	 *
	 * @return String with quotes around it; "null" if {@code source} is {@code
	 * null}.
	 */
	@NotNull
	public static String quote( @Nullable final Object source )
	{
		return ( source != null ) ? "'" + source + '\'' : "null";
	}

	/**
	 * Replaces all occurrences of 'find' in 'source' with 'replace'.
	 *
	 * @param source  Source text.
	 * @param find    Search character.
	 * @param replace Replacement string.
	 *
	 * @return Result text.
	 */
	@Nullable
	@Contract( value = "null, _, _ -> null; _, _, null -> null; !null, _, !null -> !null", pure = true )
	public static String replace( @Nullable final String source, final char find, @Nullable final String replace )
	{
		final String result;

		if ( ( source == null ) || ( replace == null ) || ( source.indexOf( find ) < 0 ) )
		{
			result = source;
		}
		else
		{
			final int len = source.length();
			final StringBuilder sb = new StringBuilder( len );

			for ( int pos = 0; pos < len; pos++ )
			{
				final char ch = source.charAt( pos );
				if ( ch == find )
				{
					sb.append( replace );
				}
				else
				{
					sb.append( ch );
				}
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Replaces all occurrences of 'find' in 'source' with 'replace'.
	 *
	 * @param source  Source text.
	 * @param find    Search string.
	 * @param replace Replacement string.
	 *
	 * @return Result text.
	 */
	@Nullable
	@Contract( value = "null, _, _ -> null; !null, _, _ -> !null", pure = true )
	public static String replace( @Nullable final String source, @Nullable final String find, @Nullable final String replace )
	{
		final String result;

		if ( ( source == null ) || ( find == null ) )
		{
			result = source;
		}
		else
		{
			final int sourceLength = source.length();
			final int findLength = find.length();
			if ( ( findLength == 0 ) || ( findLength > sourceLength ) )
			{
				result = source;
			}
			else
			{
				final int replaceLength = ( replace != null ) ? replace.length() : 0;
				if ( ( findLength == replaceLength ) && find.equals( replace ) )
				{
					result = source;
				}
				else
				{
					int pos = source.indexOf( find );
					if ( pos < 0 )
					{
						result = source;
					}
					else
					{
						final boolean haveReplacement = ( replaceLength > 0 );
						final StringBuilder sb = new StringBuilder( sourceLength + ( ( replaceLength > findLength ) ? 16 * ( replaceLength - findLength ) : 0 ) );

						int start = 0;
						do
						{
							sb.append( source, start, pos );
							if ( haveReplacement )
							{
								sb.append( replace );
							}

							start = pos + findLength;
							pos = source.indexOf( find, start );
						}
						while ( pos > 0 );

						if ( start < sourceLength )
						{
							sb.append( source, start, sourceLength );
						}

						result = sb.toString();
					}
				}
			}
		}

		return result;
	}

	/**
	 * Convert an integer value to a string with its hexadecimal representation
	 * and the specified length and case.
	 *
	 * @param value     Value to convert.
	 * @param digits    Fixed number of digits.
	 * @param upperCase Return value using upper vs. lower case letters.
	 *
	 * @return String with hexadecimal representation of {@code value}.
	 *
	 * @see Integer#toHexString
	 */
	public static String toHexString( final int value, final int digits, final boolean upperCase )
	{
		final char[] buf = new char[ digits ];
		final char[] hexDigits = upperCase ? UPPER_DIGITS : LOWER_DIGITS;

		int remainder = value;

		for ( int charPos = digits; --charPos >= 0; )
		{
			final int digit = remainder & 0x0f;
			remainder >>= 4;
			buf[ charPos ] = hexDigits[ digit ];
		}

		return new String( buf );
	}

	/**
	 * Append hexadecimal representation of integer value to the given character
	 * stream.
	 *
	 * @param out       Character stream to write to.
	 * @param value     Value to convert.
	 * @param digits    Fixed number of digits.
	 * @param upperCase Use upper vs. lower case letters.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void appendHexString( @NotNull final Appendable out, final int value, final int digits, final boolean upperCase )
	throws IOException
	{
		final char[] hexDigits = upperCase ? UPPER_DIGITS : LOWER_DIGITS;
		for ( int shift = digits * 4; ( shift -= 4 ) >= 0; )
		{
			out.append( hexDigits[ ( value >> shift ) & 0x0f ] );
		}
	}

	/**
	 * Convert an integer value to a string with its hexadecimal representation
	 * and the specified length and case.
	 *
	 * @param value     Value to convert.
	 * @param digits    Fixed number of digits.
	 * @param upperCase Return value using upper vs. lower case letters.
	 *
	 * @return String with hexadecimal representation of {@code value}.
	 *
	 * @see Integer#toHexString
	 */
	public static String toHexString( final long value, final int digits, final boolean upperCase )
	{
		final char[] buf = new char[ digits ];
		final char[] hexDigits = upperCase ? UPPER_DIGITS : LOWER_DIGITS;

		long remainder = value;

		for ( int charPos = digits; --charPos >= 0; )
		{
			final int digit = (int)remainder & 0x0f;
			remainder >>= 4;
			buf[ charPos ] = hexDigits[ digit ];
		}

		return new String( buf );
	}

	/**
	 * Append hexadecimal representation of integer value to the given character
	 * stream.
	 *
	 * @param out       Character stream to write to.
	 * @param value     Value to convert.
	 * @param digits    Fixed number of digits.
	 * @param upperCase Use upper vs. lower case letters.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void appendHexString( @NotNull final Appendable out, final long value, final int digits, final boolean upperCase )
	throws IOException
	{
		final char[] hexDigits = upperCase ? UPPER_DIGITS : LOWER_DIGITS;
		for ( int shift = digits * 4; ( shift -= 4 ) >= 0; )
		{
			out.append( hexDigits[ (int)( value >> shift ) & 0x0f ] );
		}
	}

	/**
	 * Tokenize the input string using the specified separator.
	 *
	 * @param source    Source string to be tokenized.
	 * @param separator Separator character.
	 *
	 * @return String array with result.
	 */
	@NotNull
	public static String[] tokenize( @Nullable final String source, final char separator )
	{
		final String[] result;

		/*
		 * Handle empty source string.
		 */
		if ( ( source == null ) || source.isEmpty() )
		{
			result = new String[ 0 ];
		}
		else
		{
			/*
			 * Count tokens
			 */
			int numberOfSeparators = 0;
			for ( int pos = -1; ( ( pos = source.indexOf( separator, pos + 1 ) ) >= 0 ); )
			{
				numberOfSeparators++;
			}

			/*
			 * Create result
			 */
			result = new String[ numberOfSeparators + 1 ];

			/*
			 * Get tokens into result.
			 */
			int start = 0;
			for ( int index = 0; index < numberOfSeparators; index++ )
			{
				final int end = source.indexOf( separator, start );
				result[ index ] = source.substring( start, end );
				start = end + 1;
			}

			result[ numberOfSeparators ] = ( start > 0 ) ? source.substring( start ) : source;
		}

		return result;
	}

	/**
	 * Tokenize the input string using the specified separator and return the
	 * resulting tokens as a list. If desired, leading and trailing whitespace
	 * will be trimmed off.
	 *
	 * @param source    Source string to be tokenized.
	 * @param separator Separator character.
	 * @param trim      Trim off leading and trailing whitespace.
	 *
	 * @return Empty if source is empty.
	 */
	@NotNull
	public static List<String> tokenize( @Nullable final String source, final char separator, final boolean trim )
	{
		List<String> result;

		/*
		 * Handle empty source string.
		 */
		if ( ( source == null ) || source.isEmpty() )
		{
			result = Collections.emptyList();
		}
		else
		{
			result = null;

			final int length = source.length();

			int separatorPos = -1;
			do
			{
				int start = separatorPos + 1;
				if ( trim )
				{
					while ( ( start < length ) && Character.isWhitespace( source.charAt( start ) ) )
					{
						start++;
					}
				}

				separatorPos = source.indexOf( separator, start );

				int end = ( separatorPos >= 0 ) ? separatorPos : length;
				if ( trim )
				{
					while ( ( end > start ) && Character.isWhitespace( source.charAt( end - 1 ) ) )
					{
						end--;
					}
				}

				final boolean empty = ( start == end );
				final String token = empty ? "" : source.substring( start, end );

				// first token ?
				if ( result == null )
				{
					// one and only token?
					if ( separatorPos < 0 )
					{
						// totally empty?
						if ( empty )
						{
							result = Collections.emptyList();
						}
						else
						{
							result = Collections.singletonList( token );
						}
						break;
					}
					// at least 2 tokens
					result = new ArrayList<String>();
				}

				result.add( token );
			}
			while ( separatorPos >= 0 );
		}

		return result;
	}

	/**
	 * Remove escapes from a string that was previously processed by the
	 * escape() method (or came from another source that supports this
	 * encoding).
	 *
	 * This method partially supports the syntax of the Java Language
	 * Specification. See {@link #unescape(CharSequence, int, Appendable,
	 * String)} for details.
	 *
	 * @param source String to unescape.
	 *
	 * @return String with escapes removed.
	 *
	 * @see #escape
	 */
	@Nullable
	@Contract( value = "null -> null; !null -> !null", pure = true )
	public static String unescape( @Nullable final CharSequence source )
	{
		final String result;

		if ( source == null )
		{
			result = null;
		}
		else if ( source.length() == 0 )
		{
			result = "";
		}
		else
		{
			final int len = source.length();
			final StringBuilder dest = new StringBuilder( len );

			int index = 0;
			while ( index >= 0 )
			{
				index = unescape( source, index, dest, null );
			}

			result = dest.toString();
		}

		return result;
	}

	/**
	 * Remove escapes from a string that was previously processed by the {@link
	 * #escape(Appendable, char)} method (or came from another source that
	 * supports this encoding).
	 *
	 * This method supports the syntax of the Java Language Specification for
	 * Unicode escapes and escape sequences for string and character literals,
	 * but excludes support for octal escapes.
	 *
	 * @param source      String to unescape.
	 * @param index       Start index in source string.
	 * @param destination Destination {@link Appendable}.
	 * @param terminators Characters that terminate the string; {@code null} to
	 *                    unescape only 1 character.
	 *
	 * @return Index of next substring in source string (separator is skipped);
	 * -1 if there are no more substrings in the source string.
	 *
	 * @throws IOException from {@link Appendable#append}.
	 * @see #escape
	 */
	public static int unescape( @NotNull final CharSequence source, final int index, @NotNull final Appendable destination, @Nullable final String terminators )
	throws IOException
	{
		final int len = source.length();

		int nextIndex = index;
		while ( nextIndex >= 0 )
		{
			if ( nextIndex >= len )
			{
				nextIndex = -1;
				break;
			}

			char c = source.charAt( nextIndex++ );
			if ( ( c == '\\' ) && ( nextIndex < len ) )
			{
				c = source.charAt( nextIndex++ );

				switch ( c )
				{
					case '0':
						c = '\0';
						break;

					case 'b':
						c = '\b';
						break;

					case 'f':
						c = '\f';
						break;

					case 'n':
						c = '\n';
						break;

					case 'r':
						c = '\r';
						break;

					case 't':
						c = '\t';
						break;

					case 'u':
					{
						int value = 0;

						for ( int i = 0; i < 4; i++ )
						{
							if ( nextIndex >= len )
							{
								//noinspection SpellCheckingInspection
								throw new IllegalArgumentException( "Malformed \\uxxxx encoding." );
							}

							c = source.charAt( nextIndex++ );
							value <<= 4;

							if ( c >= '0' && c <= '9' )
							{
								value += (int)c - (int)'0';
							}
							else if ( c >= 'a' && c <= 'f' )
							{
								value += (int)c - (int)'a' + 10;
							}
							else if ( c >= 'A' && c <= 'F' )
							{
								value += (int)c - (int)'A' + 10;
							}
							else
							{
								//noinspection SpellCheckingInspection
								throw new IllegalArgumentException( "Malformed \\uxxxx encoding." );
							}
						}
						c = (char)value;
					}
					break;
				}
			}
			else // no backslash
			{
				if ( terminators == null )
				{
					destination.append( c );
					break;
				}
				else if ( terminators.indexOf( c ) >= 0 )
				{
					break;
				}
			}
			destination.append( c );
		}

		return nextIndex;
	}

	/**
	 * Remove escapes from a string that was previously processed by the {@link
	 * #escape(Appendable, char)} method (or came from another source that
	 * supports this encoding).
	 *
	 * This method partially supports the syntax of the Java Language
	 * Specification. See {@link #unescape(CharSequence, int, Appendable,
	 * String)} for details.
	 *
	 * @param source      String to unescape.
	 * @param index       Start index in source string.
	 * @param destination Destination builder for result string.
	 * @param terminators Characters that terminate the string; {@code null} to
	 *                    unescape only 1 character.
	 *
	 * @return Index of next substring in source string (separator is skipped);
	 * -1 if there are no more substrings in the source string.
	 *
	 * @see #escape
	 */
	public static int unescape( @NotNull final CharSequence source, final int index, @NotNull final StringBuilder destination, @Nullable final String terminators )
	{
		try
		{
			return unescape( source, index, (Appendable)destination, terminators );
		}
		catch ( final IOException e )
		{
			/* impossible */
			throw new RuntimeException( e );
		}
	}

	/**
	 * Remove escapes from a string that was previously processed by the {@link
	 * #escape(Appendable, char)} method (or came from another source that
	 * supports this encoding).
	 *
	 * This method partially supports the syntax of the Java Language
	 * Specification. See {@link #unescape(CharSequence, int, Appendable,
	 * String)} for details.
	 *
	 * @param source      String to unescape.
	 * @param index       Start index in source string.
	 * @param destination Destination buffer for result string.
	 * @param terminators Characters that terminate the string; {@code null} to
	 *                    unescape only 1 character.
	 *
	 * @return Index of next substring in source string (separator is skipped);
	 * -1 if there are no more substrings in the source string.
	 *
	 * @see #escape
	 */
	public static int unescape( @NotNull final CharSequence source, final int index, @NotNull final StringBuffer destination, @Nullable final String terminators )
	{
		try
		{
			return unescape( source, index, (Appendable)destination, terminators );
		}
		catch ( final IOException e )
		{
			/* impossible */
			throw new RuntimeException( e );
		}
	}

	/**
	 * This method returns the input character sequence as a string with the
	 * first character in upper case.
	 *
	 * @param cs Character sequence.
	 *
	 * @return Capitalized string ({@code null} if {@code cs} was {@code null},
	 * same as {@code cs} if it's already a capitalized {@link String}).
	 */
	@Nullable
	@Contract( value = "null -> null; !null -> !null", pure = true )
	public static String capitalize( @Nullable final CharSequence cs )
	{
		return ( cs == null ) ? null : ( cs.length() == 0 ) ? "" : setFirstChar( cs, Character.toUpperCase( cs.charAt( 0 ) ) );
	}

	private static String setFirstChar( final CharSequence cs, final char ch )
	{
		final String result;

		final int length = cs.length();
		final char first = cs.charAt( 0 );

		if ( first == ch )
		{
			result = cs.toString();
		}
		else if ( length == 1 )
		{
			result = String.valueOf( ch );
		}
		else
		{
			final char[] chars = new char[ length ];
			chars[ 0 ] = ch;
			for ( int i = 1; i < length; i++ )
			{
				chars[ i ] = cs.charAt( i );
			}

			result = new String( chars );
		}

		return result;
	}

	/**
	 * This method returns the input character sequence as a string with the
	 * upper case prefix sequence converted to lower case. This supports 'camel
	 * humps', so that the last upper-case character is preserved (i.e.
	 * 'CAMELHump' becomes 'CamelHump').
	 *
	 * @param cs Character sequence.
	 *
	 * @return De-capitalized string ({@code null} if {@code cs} was {@code
	 * null}, same as {@code cs} if it's already a de-capitalized {@link
	 * String}).
	 */
	@Nullable
	@Contract( value = "null -> null; !null -> !null", pure = true )
	public static String decapitalize( @Nullable final CharSequence cs )
	{
		final String result;

		if ( cs == null )
		{
			result = null;
		}
		else
		{
			final int len = cs.length();
			if ( len == 0 )
			{
				result = "";
			}
			else
			{
				char ch = cs.charAt( 0 );
				char lower = Character.toLowerCase( ch );

				if ( ch == lower )
				{
					// do nothing if not even starting with upper case
					result = cs.toString();
				}
				else if ( len == 1 )
				{
					// border-case: single character
					result = String.valueOf( lower );
				}
				else
				{
					// implementation note: could support 'StringBuilder' here for efficiency (no new character array + String allocation)
					final char[] chars = new char[ len ];
					chars[ 0 ] = lower;
					int upCount = 1;

					for ( int i = 1; i < len; i++ )
					{
						ch = cs.charAt( i );
						if ( upCount > 0 )
						{
							lower = Character.toLowerCase( ch );
							if ( lower != ch )
							{
								upCount++;
							}
							else
							{
								// camel hump
								if ( ( upCount > 1 ) && ( Character.toUpperCase( ch ) != ch ) )
								{
									chars[ i - 1 ] = cs.charAt( i - 1 );
								}
								upCount = 0;
							}
							chars[ i ] = lower;
						}
						else
						{
							chars[ i ] = ch;
						}
					}

					result = new String( chars );
				}
			}
		}

		return result;
	}

	/**
	 * Generate a textual representation of a table.
	 *
	 * @param headers List of table headers.
	 * @param data    Contents of table.
	 *
	 * @return Textual representation of table.
	 */
	@NotNull
	public static String getTableAsText( @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data )
	{
		return getTableAsText( headers, data, false );
	}

	/**
	 * Generate a textual representation of a table.
	 *
	 * @param headers          List of table headers.
	 * @param data             Contents of table.
	 * @param linesBetweenRows Insert line between each row in the table.
	 *
	 * @return Textual representation of table.
	 */
	@NotNull
	public static String getTableAsText( @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, final boolean linesBetweenRows )
	{
		final Appendable result = new StringBuilder();
		try
		{
			writeTableAsText( result, headers, data, linesBetweenRows, "\n" );
		}
		catch ( final IOException e )
		{
			throw new AssertionError( e );
		}
		return result.toString();
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out              Character stream to write output to.
	 * @param headers          List of table headers.
	 * @param data             Contents of table.
	 * @param linesBetweenRows Insert line between each row in the table.
	 * @param lineSep          Line separator to use (typically '\n').
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void writeTableAsText( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, final boolean linesBetweenRows, @NotNull final CharSequence lineSep )
	throws IOException
	{
		writeTableAsText( out, headers, data, linesBetweenRows, lineSep, "null" );
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out              Character stream to write output to.
	 * @param headers          List of table headers.
	 * @param data             Contents of table.
	 * @param linesBetweenRows Insert line between each row in the table.
	 * @param lineSep          Line separator to use (typically '\n').
	 * @param nullValue        Value to use instead of null data.
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void writeTableAsText( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, final boolean linesBetweenRows, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
	throws IOException
	{
		writeTableAsText( out, headers, data, linesBetweenRows, "", lineSep, nullValue );
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out              Character stream to write output to.
	 * @param headers          List of table headers.
	 * @param data             Contents of table.
	 * @param linesBetweenRows Insert line between each row in the table.
	 * @param indent           Line indent string (empty if not indenting).
	 * @param lineSep          Line separator to use (typically '\n').
	 * @param nullValue        Value to use instead of null data.
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void writeTableAsText( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, final boolean linesBetweenRows, @NotNull final CharSequence indent, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
	throws IOException
	{
		int columnCount = ( headers != null ) ? headers.size() : 0;

		int dataLines = 0;
		if ( data != null )
		{
			dataLines = data.size();
			for ( final List<?> dataRow : data )
			{
				if ( dataRow != null )
				{
					columnCount = Math.max( columnCount, dataRow.size() );
				}
			}
		}

		int headerLines = 0;
		if ( headers != null )
		{
			for ( final String header : headers )
			{
				if ( header != null )
				{
					final List<String> lines = tokenize( header, '\n', true );
					headerLines = Math.max( headerLines, lines.size() );
				}
			}
		}

		final int rowCount = headerLines + dataLines;
		final String[][] strings = new String[ rowCount + 1 ][ columnCount ];

		final boolean[] columnRightAligned = new boolean[ columnCount ];
		final int[] columnWidths = new int[ columnCount ];

		if ( headers != null )
		{
			for ( int columnIndex = 0; columnIndex < headers.size(); columnIndex++ )
			{
				final String string = headers.get( columnIndex );
				if ( string != null )
				{
					final boolean rightAligned = startsWith( string, '>' );
					columnRightAligned[ columnIndex ] = rightAligned;

					final List<String> lines = tokenize( rightAligned ? string.substring( 1 ) : string, '\n', true );
					for ( int i = 0; i < lines.size(); i++ )
					{
						final String line = lines.get( i );
						strings[ i ][ columnIndex ] = line;
						columnWidths[ columnIndex ] = Math.max( columnWidths[ columnIndex ], line.length() );
					}
				}
			}
		}

		if ( data != null )
		{

			for ( int dataRowIndex = 0; dataRowIndex < data.size(); dataRowIndex++ )
			{
				final List<?> contentRow = data.get( dataRowIndex );
				if ( contentRow != null )
				{
					final String[] stringRow = strings[ headerLines + dataRowIndex ];

					for ( int columnIndex = 0; columnIndex < contentRow.size(); columnIndex++ )
					{
						final Object value = contentRow.get( columnIndex );
						final String string = ( value == null ) ? nullValue : String.valueOf( value ).trim();
						if ( string != null )
						{
							columnWidths[ columnIndex ] = Math.max( columnWidths[ columnIndex ], string.length() );
							stringRow[ columnIndex ] = string;
						}
					}
				}
			}
		}

		for ( int rowIndex = -1; rowIndex < rowCount; rowIndex++ )
		{
			if ( rowIndex >= 0 )
			{
				out.append( indent );
				for ( int columnIndex = 0; columnIndex < columnCount; columnIndex++ )
				{
					out.append( "| " );
					appendFixed( out, strings[ rowIndex ][ columnIndex ], columnWidths[ columnIndex ], columnRightAligned[ columnIndex ], ' ' );
					out.append( ' ' );
				}
				out.append( "|" );
				out.append( lineSep );
			}

			if ( ( rowIndex == -1 ) || ( rowIndex == headerLines - 1 ) || linesBetweenRows || ( rowIndex == ( rowCount - 1 ) ) )
			{
				out.append( indent );
				for ( int columnIndex = 0; columnIndex < columnCount; columnIndex++ )
				{
					out.append( "+" );
					appendFixed( out, null, columnWidths[ columnIndex ] + 2, false, '-' );
				}

				out.append( "+" );
				out.append( lineSep );
			}
		}
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out              Print stream to write output to.
	 * @param headers          List of table headers.
	 * @param data             Contents of table.
	 * @param linesBetweenRows Insert line between each row in the table.
	 * @param indent           Line indent string (empty if not indenting).
	 * @param lineSep          Line separator to use (typically '\n').
	 * @param nullValue        Value to use instead of null data.
	 */
	public static void writeTableAsText( @NotNull final PrintStream out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, final boolean linesBetweenRows, @NotNull final CharSequence indent, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
	{
		try
		{
			writeTableAsText( (Appendable)out, headers, data, linesBetweenRows, indent, lineSep, nullValue );
		}
		catch ( final IOException e )
		{
			// should never happen when PrintStream is used, therefore throw AssertionError
			throw new AssertionError( e );
		}
	}

	/**
	 * Parses the given wildcard pattern and converts it into an equivalent
	 * regular expression pattern. Wildcards are '*' for zero or more characters
	 * and '?' for a single character.
	 *
	 * @param wildcardPattern Wildcard pattern to be parsed.
	 *
	 * @return Equivalent regular expression pattern.
	 */
	@NotNull
	public static String wildcardPatternToRegex( @NotNull final String wildcardPattern )
	{
		final Pattern wildcardRegex = Pattern.compile( "[*?]" );
		final Matcher wildcardMatcher = wildcardRegex.matcher( wildcardPattern );

		final StringBuilder resultPattern = new StringBuilder();

		int lastWildcardPosition = 0;

		while ( wildcardMatcher.find() )
		{
			if ( lastWildcardPosition < wildcardMatcher.start() )
			{
				resultPattern.append( Pattern.quote( wildcardPattern.substring( lastWildcardPosition, wildcardMatcher.start() ) ) );
			}

			lastWildcardPosition = wildcardMatcher.start() + 1;

			final String wildcard = wildcardMatcher.group();
			switch ( wildcard.charAt( 0 ) )
			{
				case '*':
					resultPattern.append( ".*" );
					break;
				case '?':
					resultPattern.append( '.' );
					break;
			}
		}

		if ( lastWildcardPosition < wildcardPattern.length() )
		{
			resultPattern.append( Pattern.quote( wildcardPattern.substring( lastWildcardPosition ) ) );
		}

		return resultPattern.toString();
	}

	/**
	 * Converts a string in camel case to upper case (with underscores to
	 * separate words). For example, 'helloWorld' converts to 'HELLO_WORLD', as
	 * does 'HelloWorld'.
	 *
	 * @param value Camel case string to be converted.
	 *
	 * @return Upper case equivalent.
	 */
	@NotNull
	public static String camelToUpperCase( @NotNull final CharSequence value )
	{
		final String result;

		final int length = value.length();
		if ( length == 0 )
		{
			result = "";
		}
		else
		{
			int pos = 0;
			while ( ( pos < length ) && !Character.isLowerCase( value.charAt( pos ) ) )
			{
				pos++;
			}

			if ( pos == length )
			{
				result = value.toString();
			}
			else
			{
				final StringBuilder sb = new StringBuilder( length + 1 );

				if ( pos > 0 )
				{
					pos--;
					sb.append( value, 0, pos );
				}

				boolean lastIsLower = ( pos > 0 ) && Character.isUpperCase( value.charAt( pos - 1 ) );

				while ( pos < length )
				{
					final char ch = value.charAt( pos++ );

					if ( Character.isUpperCase( ch ) && lastIsLower )
					{
						sb.append( '_' );
					}

					lastIsLower = Character.isLowerCase( ch );
					sb.append( lastIsLower ? Character.toUpperCase( ch ) : ch );
				}

				result = sb.toString();
			}
		}

		return result;
	}

	/**
	 * Converts a string in upper case with underscores to separate words to
	 * camel case. For example, 'HELLO_WORLD' converts to 'helloWorld'.
	 *
	 * @param value Upper case string to be converted.
	 *
	 * @return Camel case equivalent.
	 */
	@NotNull
	public static String upperToCamelCase( @NotNull final CharSequence value )
	{
		final String result;

		final int length = value.length();
		if ( length == 0 )
		{
			result = "";
		}
		else
		{
			final StringBuilder sb = new StringBuilder( length );
			boolean keepNextUpper = false;
			for ( int pos = 0; pos < length; pos++ )
			{
				final char ch = value.charAt( pos );
				if ( ch == '_' )
				{
					if ( sb.length() > 0 )
					{
						keepNextUpper = true;
					}
				}
				else if ( keepNextUpper )
				{
					sb.append( ch );
					keepNextUpper = false;
				}
				else
				{
					sb.append( Character.toLowerCase( ch ) );
				}
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Truncates the given string to the specified length. If the string is
	 * already shorter than {@code length} the string is returned as-is.
	 *
	 * @param string String to be truncated.
	 * @param length Maximum length of the result.
	 *
	 * @return A string of at most {@code length} characters; {@code null} if
	 * the input was {@code null}.
	 */
	@Nullable
	@Contract( value = "null, _ -> null; !null, _ -> !null", pure = true )
	public static String truncate( @Nullable final String string, final int length )
	{
		return ( string == null ) ? null : ( string.length() > length ) ? string.substring( 0, length ) : string;
	}

	/**
	 * Format number using a binary prefix according to IEC 60027-2 A.2 and
	 * ISO/IEC 80000 standards.
	 *
	 * @param number Number to format.
	 *
	 * @return Formatted number.
	 */
	@NotNull
	public static String formatBinary( final long number )
	{
		return formatBinary( number, 3 );
	}

	/**
	 * Format number using a binary prefix according to IEC 60027-2 A.2 and
	 * ISO/IEC 80000 standards.
	 *
	 * @param number            Number to format.
	 * @param maxFractionDigits Maximum fraction digits (0-2).
	 *
	 * @return Formatted number.
	 */
	@NotNull
	public static String formatBinary( final long number, final int maxFractionDigits )
	{
		long quotient = number;
		int remainder = 0;
		int scale = 0;

		while ( quotient > 1024L )
		{
			remainder = (int)( quotient % 1024L );
			quotient /= 1024L;
			scale++;
		}

		String fraction = "";
		if ( ( scale != 0 ) && ( maxFractionDigits > 0 ) && ( quotient < 100L ) )
		{
			final int fractionScale = ( quotient > 10L ) ? 1 : Math.min( 2, maxFractionDigits );
			fraction = String.valueOf( (double)remainder / 1024.0 );
			fraction = '.' + fraction.substring( 2, Math.min( fraction.length(), 2 + fractionScale ) );
		}

		return quotient + fraction + ' ' + IEC_BINARY_PREFIXES[ scale ];
	}

	/**
	 * This function can be used to test if a string seems to represent a valid
	 * E-mail address.
	 *
	 * @param email E-mail address string.
	 *
	 * @return {@code true} if the E-mail address seems to be valid; {@code
	 * false} otherwise.
	 */
	@Contract( value = "null -> false", pure = true )
	public static boolean isValidEmail( @Nullable final String email )
	{
		return ( ( email != null ) && !email.isEmpty() && VALID_EMAIL_ADDRESS_PATTERN.matcher( email ).matches() );
	}

	/**
	 * Test whether the given file path is secure. This is true if the file path
	 * is a slash- or backslash-separated list of secure file names (see {@link
	 * #isSecureFilename}). An absolute path (with a leading slash or backslash)
	 * is not considered secure.
	 *
	 * @param filePath File path to check.
	 *
	 * @return {@code true} if file name is valid.
	 */
	@Contract( value = "null -> false", pure = true )
	public static boolean isSecureFilePath( @Nullable final CharSequence filePath )
	{
		boolean result;

		if ( filePath == null )
		{
			result = false;
		}
		else
		{
			final int length = filePath.length();

			result = ( length > 0 );
			int namePartLength = 0;
			char previousCh = '\0';

			for ( int i = 0; i < length; i++ )
			{
				final char ch = filePath.charAt( i );
				if ( ( ch == '/' ) || ( ch == '\\' ) )
				{
					// empty file name
					if ( namePartLength == 0 )
					{
						result = false;
						break;
					}

					// filename ends with whitespace
					if ( Character.isWhitespace( previousCh ) )
					{
						result = false;
						break;
					}

					// start new file
					namePartLength = 0;
				}
				else if ( Character.isISOControl( ch ) || ( ( ( namePartLength == 0 ) ? ILLEGAL_FILENAME_START_CHARACTERS : ILLEGAL_FILENAME_CHARACTERS ).indexOf( ch ) >= 0 ) )
				{
					// illegal character
					result = false;
					break;
				}
				else
				{
					// filename starts with whitespace
					if ( ( namePartLength == 0 ) && Character.isWhitespace( ch ) )
					{
						result = false;
						break;
					}

					// filename is too long
					if ( ++namePartLength > MAXIMUM_FILENAME_LENGTH )
					{
						result = false;
						break;
					}
				}

				previousCh = ch;
			}

			// filename ends with whitespace
			if ( Character.isWhitespace( previousCh ) )
			{
				result = false;
			}
		}

		return result;
	}

	/**
	 * Test whether the given file name is secure. This is true if the file
	 * name:
	 *
	 * <ul>
	 *
	 * <li>Has an acceptable length (1 to 255)</li>
	 *
	 * <li>Contains no ISO control characters (see {@link
	 * Character#isISOControl})</li>
	 *
	 * <li>Contains no characters with a known possible special meaning:
	 *
	 * <table>
	 *
	 * <tr><td>!</td><td>exclamation mark</td></tr>
	 *
	 * <tr><td>"</td><td>quotation mark</td></tr>
	 *
	 * <tr><td>#</td><td>number sign</td></tr>
	 *
	 * <tr><td>$</td><td>dollar sign</td></tr>
	 *
	 * <tr><td>%</td><td>percent sign</td></tr>
	 *
	 * <tr><td>&amp;</td><td>ampersand</td></tr>
	 *
	 * <tr><td>'</td><td>apostrophe</td></tr>
	 *
	 * <tr><td>*</td><td>asterisk</td></tr>
	 *
	 * <tr><td>+</td><td>plus sign</td></tr>
	 *
	 * <tr><td>/</td><td>slash</td></tr>
	 *
	 * <tr><td>:</td><td>colon</td></tr>
	 *
	 * <tr><td>;</td><td>semicolon</td></tr>
	 *
	 * <tr><td>&lt;</td><td>less-than</td></tr>
	 *
	 * <tr><td>=</td><td>equals to</td></tr>
	 *
	 * <tr><td>&gt;</td><td>greater-than</td></tr>
	 *
	 * <tr><td>?</td><td>question mark</td></tr>
	 *
	 * <tr><td>@</td><td>at sign</td></tr>
	 *
	 * <tr><td>\</td><td>backslash</td></tr>
	 *
	 * <tr><td>`</td><td>grave accent</td></tr>
	 *
	 * <tr><td>{</td><td>left curly brace</td></tr>
	 *
	 * <tr><td>|</td><td>vertical bar</td></tr>
	 *
	 * <tr><td>}</td><td>right curly brace</td></tr>
	 *
	 * </table></li>
	 *
	 * <li>Does not start with a characters with a known possible special
	 * meaning:
	 *
	 * <table>
	 *
	 * <tr><td>-</td><td>hyphen</td></tr>
	 *
	 * <tr><td>.</td><td>period</td></tr>
	 *
	 * <tr><td>_</td><td>underscore</td></tr>
	 *
	 * </table></li>
	 *
	 * <li>Does not start or end with whitespace</li>
	 *
	 * </ul>
	 *
	 * @param fileName File name to check.
	 *
	 * @return {@code true} if file name is valid.
	 */
	@Contract( value = "null -> false", pure = true )
	public static boolean isSecureFilename( @Nullable final CharSequence fileName )
	{
		boolean result;

		if ( fileName == null )
		{
			result = false;
		}
		else
		{
			final int length = fileName.length();
			if ( ( length == 0 ) || ( length > MAXIMUM_FILENAME_LENGTH ) )
			{
				result = false;
			}
			else
			{
				result = true;

				for ( int i = 0; i < length; i++ )
				{
					final char ch = fileName.charAt( i );
					if ( Character.isISOControl( ch ) || ( ( ( i == 0 ) ? ILLEGAL_FILENAME_START_CHARACTERS : ILLEGAL_FILENAME_CHARACTERS ).indexOf( ch ) >= 0 ) )
					{
						result = false;
						break;
					}
					else if ( ( i == 0 ) || ( i == length - 1 ) )
					{
						if ( Character.isWhitespace( ch ) )
						{
							result = false;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Generate "hex dump" output of the buffer at src like the following:
	 * <pre>
	 * 00000: 04 d2 29 00 00 01 00 00 00 00 00 01 20 45 47 |..)......... EG|
	 * 00010: 43 45 46 45 45 43 41 43 41 43 41 43 41 43 41 |CEFEECACACACACA|
	 * 00020: 41 43 41 43 41 43 41 43 41 43 41 41 44 00 00 |ACACACACACAAD..|
	 * 00030: 00 01 c0 0c 00 20 00 01 00 00 00 00 00 06 20 |..... ........ |
	 * 00040: ac 22 22 e1                                  |."".           |
	 * </pre>
	 *
	 * @param out      Target character stream.
	 * @param src      Data to dump.
	 * @param srcIndex Index of first byte to dump.
	 * @param length   Number of bytes to dump.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
	public static void hexdump( @NotNull final Appendable out, @NotNull final byte[] src, final int srcIndex, final int length )
	throws IOException
	{
		for ( int pointer = 0; pointer < length; pointer += 16 )
		{
			appendHexString( out, pointer, 5, true );
			out.append( ": " );

			final int rowLength = Math.min( 16, length - pointer );

			for ( int i = 0; i < 16; i++ )
			{
				if ( i < rowLength )
				{
					appendHexString( out, src[ srcIndex + pointer + i ], 2, true );
					out.append( ' ' );
				}
				else
				{
					out.append( "   " );
				}
			}

			out.append( " |" );

			for ( int i = 0; i < 16; i++ )
			{
				if ( i < rowLength )
				{
					final byte b = src[ srcIndex + pointer + i ];
					out.append( ( b < 31 ) ? '.' : (char)b );
				}
				else
				{
					out.append( ' ' );
				}
			}

			out.append( "|\n" );
		}
	}

	/**
	 * Get duration string.
	 *
	 * @param millis       Number of milliseconds.
	 * @param seconds      Whether to include seconds in the result.
	 * @param milliseconds Whether to include seconds and milliseconds in the result.
	 *
	 * @return Duration string.
	 */
	@NotNull
	public static String getDurationString( final long millis, final boolean seconds, final boolean milliseconds )
	{
		final String result;

		if ( milliseconds )
		{
			final int totalSeconds = (int)( millis / 1000L );
			final int hours = totalSeconds / 3600;
			final int min = ( totalSeconds / 60 ) % 60;
			final int sec = totalSeconds % 60;

			result = hours + ":" + ( min / 10 ) + ( min % 10 ) + ':' + ( sec / 10 ) + ( sec % 10 ) + '.' + ( millis % 1000 );
		}
		else if ( seconds )
		{
			final int totalSeconds = Math.round( millis / 1000.0f );
			final int hours = totalSeconds / 3600;
			final int min = ( totalSeconds / 60 ) % 60;
			final int sec = totalSeconds % 60;

			result = hours + ":" + ( min / 10 ) + ( min % 10 ) + ':' + ( sec / 10 ) + ( sec % 10 );
		}
		else
		{
			final int totalMinutes = Math.round( millis / 60000.0f );
			final int hours = totalMinutes / 60;
			final int min = totalMinutes % 60;

			result = hours + ":" + ( min / 10 ) + ( min % 10 );
		}

		return result;
	}
}
