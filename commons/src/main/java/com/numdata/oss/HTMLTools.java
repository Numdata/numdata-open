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

import org.jetbrains.annotations.*;

/**
 * This class contains utility methods used to generate HTML documents.
 *
 * @author Peter S. Heijnen
 */
public class HTMLTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private HTMLTools()
	{
	}

	/**
	 * This method converts a {@link Properties} into a string containing the
	 * properties suitable as attributes of an HTML element.
	 *
	 * @param attributes Properties to assign to HTML element.
	 *
	 * @return Properties as string for a HTML element (may be empty, but is
	 * never {@code null}).
	 */
	public static String getAttributes( final Properties attributes )
	{
		final String result;

		if ( ( attributes != null ) && !attributes.isEmpty() )
		{
			final Appendable sb = new StringBuffer();

			try
			{
				writeAttributes( sb, attributes );
			}
			catch ( final IOException e )
			{
				e.printStackTrace(); /* should never happen */
			}

			result = sb.toString();
		}
		else
		{
			result = "";
		}

		return result;
	}

	/**
	 * Add class(es) to the {@code class} attribute of an HTML element. The
	 * given classes are combined with any pre-existing classes.
	 *
	 * @param elementAttributes Target attributes.
	 * @param classes           Class(es) to set.
	 */
	public static void addClasses( @NotNull final Properties elementAttributes, @Nullable final String classes )
	{
		if ( ( classes != null ) && !TextTools.isEmpty( classes ) )
		{
			elementAttributes.setProperty( "class", combineClasses( elementAttributes.getProperty( "class" ), classes ) );
		}
	}

	/**
	 * Add class(es) to the {@code class} attribute of an HTML element. The
	 * given classes are combined with any pre-existing classes.
	 *
	 * @param elementAttributes Target attributes.
	 * @param classes           Class(es) to set.
	 */
	public static void addClasses( @NotNull final Map<String, String> elementAttributes, @Nullable final String classes )
	{
		if ( ( classes != null ) && !TextTools.isEmpty( classes ) )
		{
			elementAttributes.put( "class", combineClasses( elementAttributes.get( "class" ), classes ) );
		}
	}

	/**
	 * Combine class values.
	 *
	 * @param classes1 First class value.
	 * @param classes2 Second class value;
	 *
	 * @return Combined class value.
	 */
	@Nullable
	public static String combineClasses( @Nullable final String classes1, @Nullable final String classes2 )
	{
		final String result;

		if ( TextTools.equals( classes1, classes2 ) || ( classes2 == null ) || TextTools.isEmpty( classes2 ) )
		{
			result = classes1;
		}
		else if ( ( classes1 == null ) || TextTools.isEmpty( classes1 ) )
		{
			result = classes2;
		}
		else
		{
			final String trimmed1 = classes1.trim();
			final String trimmed2 = classes2.trim();
			final String[] parts1 = trimmed1.split( "\\s+" );
			final String[] parts2 = trimmed2.split( "\\s+" );

			final Collection<String> combinedClasses = new LinkedHashSet<String>( parts1.length + parts2.length );
			combinedClasses.addAll( Arrays.asList( parts1 ) );
			combinedClasses.addAll( Arrays.asList( parts2 ) );

			final StringBuilder sb = new StringBuilder( classes1.length() + 1 + classes2.length() );

			for ( final String element : combinedClasses )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( element );
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Add text to {@code style} attribute of an HTML element. The given style
	 * is appenden to any pre-existing style attribute (a semi-colon is
	 * automatically inserted when needed).
	 *
	 * @param elementAttributes Target attributes.
	 * @param style             Style to set.
	 */
	public static void addStyle( @NotNull final Properties elementAttributes, @Nullable final String style )
	{
		if ( ( style != null ) && !TextTools.isEmpty( style ) )
		{
			elementAttributes.setProperty( "style", combineStyles( elementAttributes.getProperty( "style" ), style ) );
		}
	}

	/**
	 * Add text to {@code style} attribute of an HTML element. The given style
	 * is appenden to any pre-existing style attribute (a semi-colon is
	 * automatically inserted when needed).
	 *
	 * @param elementAttributes Target attributes.
	 * @param style             Style to set.
	 */
	public static void addStyle( @NotNull final Map<String, String> elementAttributes, @Nullable final String style )
	{
		if ( ( style != null ) && !TextTools.isEmpty( style ) )
		{
			elementAttributes.put( "style", combineStyles( elementAttributes.get( "style" ), style ) );
		}
	}

	/**
	 * Combine style values. Simply concatenates styles, appending {@code
	 * style2} to {@code style1}. This automatically inserts a semi-colon if
	 * {@code style1} does not end with one.
	 *
	 * @param style1 First style value.
	 * @param style2 Second style value.
	 *
	 * @return Combined style value.
	 */
	@Nullable
	public static String combineStyles( @Nullable final String style1, @Nullable final String style2 )
	{
		final String result;

		if ( TextTools.equals( style1, style2 ) || ( style2 == null ) || TextTools.isEmpty( style2 ) )
		{
			result = style1;
		}
		else if ( ( style1 == null ) || TextTools.isEmpty( style1 ) )
		{
			result = style2;
		}
		else
		{
			result = TextTools.endsWith( style1, ';' ) ? style1 + style2 : style1 + "; " + style2;
		}

		return result;
	}

	/**
	 * Replace HTML code (ISO 8879) with Java character (unicode).
	 *
	 * @param source Source string.
	 * @param pos    Position of code in source string (should point at '&').
	 * @param len    Length of code (without trailing ';', if any).
	 *
	 * @return Character for HTML code; 0 if no character was found for the HTML
	 * code.
	 */
	public static char getCharForHtmlCode( final String source, final int pos, final int len )
	{
		char ch = '\0';

		if ( ( source.charAt( pos ) == '&' ) && ( len > 1 ) )
		{
			if ( ( len > 2 ) && ( source.charAt( pos + 1 ) == '#' ) ) // numeric character references
			{
				int value = 0;
				for ( int i = 2; ( value >= 0 ) && ( i < len ); i++ )
				{
					final int digit = Character.digit( source.charAt( pos + i ), 10 );
					value = ( digit < 0 ) ? -1 : ( value * 10 + digit );
				}

				ch = ( value < 0 ) ? '\0' : (char)value;
			}
			else
			{
				switch ( len ) // character entity references
				{
					case 3:
						if ( source.regionMatches( true, pos + 1, "lt", 0, 2 ) )
						{
							ch = '\u003C';
						}
						else if ( source.regionMatches( true, pos + 1, "gt", 0, 2 ) )
						{
							ch = '\u003E';
						}
						break;

					case 4:
						if ( source.regionMatches( true, pos + 1, "uml", 0, 3 ) )
						{
							ch = '\u00A8';
						}
						else if ( source.regionMatches( true, pos + 1, "amp", 0, 3 ) )
						{
							ch = '\u0026';
						}
						else if ( source.regionMatches( true, pos + 1, "deg", 0, 3 ) )
						{
							ch = '\u00B0';
						}
						else if ( source.regionMatches( true, pos + 1, "yen", 0, 3 ) )
						{
							ch = '\u00A5';
						}
						else if ( source.regionMatches( true, pos + 1, "reg", 0, 3 ) )
						{
							ch = '\u00AE';
						}
						else if ( source.regionMatches( true, pos + 1, "not", 0, 3 ) )
						{
							ch = '\u00AC';
						}
						else if ( source.regionMatches( true, pos + 1, "shy", 0, 3 ) )
						{
							ch = '\u00AE';
						}
						break;

					case 5:
						if ( source.regionMatches( true, pos + 2, "uml", 0, 3 ) )
						{
						/*
						 * uml
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C4';
									break;
								case 'a':
									ch = '\u00E4';
									break;
								case 'E':
									ch = '\u00CB';
									break;
								case 'e':
									ch = '\u00EB';
									break;
								case 'I':
									ch = '\u00CF';
									break;
								case 'i':
									ch = '\u00EF';
									break;
								case 'O':
									ch = '\u00D6';
									break;
								case 'o':
									ch = '\u00F6';
									break;
								case 'U':
									ch = '\u00DC';
									break;
								case 'u':
									ch = '\u00FC';
									break;
								case 'y':
									ch = '\u00FF';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 1, "quot", 0, 4 ) )
						{
							ch = '\'';
						}
						else if ( source.regionMatches( true, pos + 1, "nbsp", 0, 4 ) )
						{
							ch = '\u00A0';
						}
						else if ( source.regionMatches( true, pos + 1, "sect", 0, 4 ) )
						{
							ch = '\u00A7';
						}
						else if ( source.regionMatches( true, pos + 1, "macr", 0, 4 ) )
						{
							ch = '\u00AF';
						}
						else if ( source.regionMatches( true, pos + 1, "cent", 0, 4 ) )
						{
							ch = '\u00A2';
						}
						else if ( source.regionMatches( true, pos + 1, "para", 0, 4 ) )
						{
							ch = '\u00B6';
						}
						else if ( source.regionMatches( true, pos + 1, "copy", 0, 4 ) )
						{
							ch = '\u00A9';
						}
						else if ( source.regionMatches( true, pos + 1, "euro", 0, 4 ) )
						{
							ch = '\u20AC';
						}
						else if ( source.regionMatches( true, pos + 1, "sip1", 0, 4 ) )
						{
							ch = '\u00B9';
						}
						else if ( source.regionMatches( true, pos + 1, "sup2", 0, 4 ) )
						{
							ch = '\u00B2';
						}
						else if ( source.regionMatches( true, pos + 1, "sup3", 0, 4 ) )
						{
							ch = '\u00B3';
						}
						break;

					case 6:
						if ( source.regionMatches( true, pos + 2, "circ", 0, 4 ) )
						{
						/*
						 * circumflex
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C2';
									break;
								case 'a':
									ch = '\u00E2';
									break;
								case 'E':
									ch = '\u00CA';
									break;
								case 'e':
									ch = '\u00EA';
									break;
								case 'I':
									ch = '\u00CE';
									break;
								case 'i':
									ch = '\u00EE';
									break;
								case 'O':
									ch = '\u00D4';
									break;
								case 'o':
									ch = '\u00F4';
									break;
								case 'U':
									ch = '\u00DB';
									break;
								case 'u':
									ch = '\u00FB';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "elig", 0, 4 ) )
						{
						/*
						 * elig
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00E6';
									break;
								case 'a':
									ch = '\u00C6';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "ring", 0, 4 ) )
						{
						/*
						 * ring
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C5';
									break;
								case 'a':
									ch = '\u00E5';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 1, "szlig", 0, 5 ) )
						{
							ch = '\u00DF';
						}
						else if ( source.regionMatches( true, pos + 1, "iexcl", 0, 5 ) )
						{
							ch = '\u00A1';
						}
						else if ( source.regionMatches( true, pos + 1, "laquo", 0, 5 ) )
						{
							ch = '\u00AB';
						}
						else if ( source.regionMatches( true, pos + 1, "acute", 0, 5 ) )
						{
							ch = '\u00B4';
						}
						else if ( source.regionMatches( true, pos + 1, "times", 0, 5 ) )
						{
							ch = '\u00D7';
						}
						else if ( source.regionMatches( true, pos + 1, "micro", 0, 5 ) )
						{
							ch = '\u00B5';
						}
						else if ( source.regionMatches( true, pos + 1, "cedil", 0, 5 ) )
						{
							ch = '\u00B8';
						}
						else if ( source.regionMatches( true, pos + 1, "pound", 0, 5 ) )
						{
							ch = '\u00A3';
						}
						else if ( source.regionMatches( true, pos + 1, "trade", 0, 5 ) )
						{
							ch = '\u2122';
						}
						else if ( source.regionMatches( true, pos + 1, "raquo", 0, 5 ) )
						{
							ch = '\u00BB';
						}
						break;

					case 7:
						if ( source.regionMatches( true, pos + 2, "acute", 0, 5 ) )
						{
						/*
						 * acute
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C1';
									break;
								case 'a':
									ch = '\u00E1';
									break;
								case 'E':
									ch = '\u00C9';
									break;
								case 'e':
									ch = '\u00E9';
									break;
								case 'I':
									ch = '\u00CD';
									break;
								case 'i':
									ch = '\u00ED';
									break;
								case 'O':
									ch = '\u00D3';
									break;
								case 'o':
									ch = '\u00F3';
									break;
								case 'U':
									ch = '\u00DA';
									break;
								case 'u':
									ch = '\u00FA';
									break;
								case 'Y':
									ch = '\u00DD';
									break;
								case 'y':
									ch = '\u00FD';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "cedil", 0, 5 ) )
						{
						/*
						 * cedil
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'C':
									ch = '\u00C7';
									break;
								case 'c':
									ch = '\u00E7';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "grave", 0, 5 ) )
						{
						/*
						 * grave
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C0';
									break;
								case 'a':
									ch = '\u00E0';
									break;
								case 'E':
									ch = '\u00C8';
									break;
								case 'e':
									ch = '\u00E8';
									break;
								case 'I':
									ch = '\u00CC';
									break;
								case 'i':
									ch = '\u00EC';
									break;
								case 'O':
									ch = '\u00D2';
									break;
								case 'o':
									ch = '\u00F2';
									break;
								case 'U':
									ch = '\u00D9';
									break;
								case 'u':
									ch = '\u00F9';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "slash", 0, 5 ) )
						{
						/*
						 * slash
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'O':
									ch = '\u00D8';
									break;
								case 'o':
									ch = '\u00F8';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 2, "tilde", 0, 5 ) )
						{
						/*
						 * tilde
						 */
							switch ( source.charAt( pos + 1 ) )
							{
								case 'A':
									ch = '\u00C3';
									break;
								case 'a':
									ch = '\u00E3';
									break;
								case 'N':
									ch = '\u00D1';
									break;
								case 'n':
									ch = '\u00F1';
									break;
								case 'O':
									ch = '\u00D5';
									break;
								case 'o':
									ch = '\u00F5';
									break;
							}
						}
						else if ( source.regionMatches( true, pos + 1, "middot", 0, 6 ) )
						{
							ch = '\u00B7';
						}
						else if ( source.regionMatches( true, pos + 1, "iquest", 0, 6 ) )
						{
							ch = '\u00BF';
						}
						else if ( source.regionMatches( true, pos + 1, "brvbar", 0, 6 ) )
						{
							ch = '\u00A6';
						}
						else if ( source.regionMatches( true, pos + 1, "plusmn", 0, 6 ) )
						{
							ch = '\u00B1';
						}
						else if ( source.regionMatches( true, pos + 1, "divide", 0, 6 ) )
						{
							ch = '\u00F7';
						}
						else if ( source.regionMatches( true, pos + 1, "frac14", 0, 6 ) )
						{
							ch = '\u00BC';
						}
						else if ( source.regionMatches( true, pos + 1, "frac12", 0, 6 ) )
						{
							ch = '\u00BD';
						}
						else if ( source.regionMatches( true, pos + 1, "frac34", 0, 6 ) )
						{
							ch = '\u00BE';
						}
						else if ( source.regionMatches( true, pos + 1, "curren", 0, 6 ) )
						{
							ch = '\u00A4';
						}
						break;
				}
			}
		}

		return ch;
	}

	/**
	 * Assemble URI from context path and servlet path. Basically this appends
	 * the servlet path to the context path.
	 *
	 * The result is never {@code null}, but may be an empty string if both
	 * paths are empty.
	 *
	 * @param contextPath Context path (typically empty string or
	 *                    "/{contextName}", may be {@code null}).
	 * @param servletPath Servlet path (typically "/servlet/{servletName}" or
	 *                    "/path/to/page.jsp").
	 *
	 * @return Assembled URI (e.g. "/{contextName}/servlet/{servletName}").
	 */
	public static String getURI( final String contextPath, final String servletPath )
	{
		final String result;

		if ( TextTools.isNonEmpty( servletPath ) )
		{
			if ( TextTools.startsWith( servletPath, '/' ) && TextTools.isNonEmpty( contextPath ) )
			{
				result = contextPath + servletPath.substring( TextTools.endsWith( contextPath, '/' ) ? 1 : 0 );
			}
			else
			{
				result = servletPath;
			}
		}
		else
		{
			result = TextTools.isNonEmpty( contextPath ) ? contextPath : "";
		}

		return result;
	}

	/**
	 * Write heading of the specified {@code level} with the specified {@code
	 * text} as content to an HTML document. This will output the following
	 * code:
	 * <pre>
	 *   &lt;h{@code level}&gt;{@code text}&lt;/h{@code level}&gt;
	 * </pre>
	 *
	 * @param out   Writer to use for output.
	 * @param level Heading level (between 1 and 6, inclusive).
	 * @param text  Text body to write.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static void writeHeading( final Writer out, final int level, final String text )
	throws IOException
	{
		if ( ( level < 1 ) || ( level > 6 ) )
		{
			throw new IllegalArgumentException( "level=" + level );
		}

		out.append( "<h" );
		out.append( String.valueOf( level ) );
		out.append( '>' );
		out.append( text );
		out.append( "</h" );
		out.append( String.valueOf( level ) );
		out.append( ">\n" );
	}

	/**
	 * Write heading of the specified {@code level} with the specified {@code
	 * text} as content to an HTML document, optionally with an anchor tag to
	 * allow references to it. This will output the following code:
	 * <pre>
	 *   &lt;h{@code level}&gt;&lt;a name='{@code name}'&gt;{@code
	 * text}&lt;/a&gt;&lt;/h{@code level}&gt;
	 * </pre>
	 *
	 * @param out   Writer to use for output.
	 * @param level Heading level (between 1 and 6, inclusive).
	 * @param text  Text body to write.
	 * @param name  Anchor name; {@code null} to write a heading only.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static void writeHeading( final Writer out, final int level, final String text, final String name )
	throws IOException
	{
		if ( ( level < 1 ) || ( level > 6 ) )
		{
			throw new IllegalArgumentException( "level=" + level );
		}

		out.append( "<h" );
		out.append( String.valueOf( level ) );
		out.append( '>' );

		if ( name != null )
		{
			out.append( "<a name='" );
			out.append( name );
			out.append( "'>" );
			out.append( "</a>" );
		}

		out.append( text );

		out.append( "</h" );
		out.append( String.valueOf( level ) );
		out.append( ">\n" );
	}

	/**
	 * Writes text to an HTML document. Characters with special meaning in HTML
	 * are escaped and newlines are replaced with '&lt;br&gt;' tags.
	 *
	 * @param result Character sequence to append the result to.
	 * @param source Text to write.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static void writeText( @NotNull final Appendable result, @NotNull final CharSequence source )
	throws IOException
	{
		final int length = source.length();
		for ( int i = 0; i < length; i++ )
		{
			final char c = source.charAt( i );
			switch ( c )
			{
				case '<':
					result.append( "&lt;" );
					break;
				case '>':
					result.append( "&gt;" );
					break;
				case '&':
					result.append( "&amp;" );
					break;
				case '\r':
					break;
				case '\n':
					result.append( "<br>" );
					break;
				default:
					result.append( c );
					break;
			}
		}
	}

	/**
	 * Writes text to an HTML document. Characters with special meaning in HTML
	 * are escaped and newlines are replaced with '&lt;br&gt;' tags.
	 *
	 * @param result Character sequence to append the result to.
	 * @param source Text to write.
	 */
	public static void appendText( @NotNull final StringBuilder result, @NotNull final CharSequence source )
	{
		appendText( result, source, "<br>" );
	}

	/**
	 * Writes text to an HTML document. Characters with special meaning in HTML
	 * are escaped and newlines are replaced with '&lt;br&gt;' tags.
	 *
	 * @param result             Character sequence to append the result to.
	 * @param source             Text to write.
	 * @param newLineReplacement Text to replace newline characters with (e.g.
	 *                           '&lt;br&gt;').
	 */
	public static void appendText( @NotNull final StringBuilder result, @NotNull final CharSequence source, final String newLineReplacement )
	{
		final int length = source.length();
		for ( int i = 0; i < length; i++ )
		{
			final char c = source.charAt( i );
			switch ( c )
			{
				case '<':
					result.append( "&lt;" );
					break;

				case '>':
					result.append( "&gt;" );
					break;

				case '&':
					result.append( "&amp;" );
					break;

				case '\r':
					break;

				case '\n':
					result.append( newLineReplacement );
					break;

				default:
					result.append( c );
					break;
			}
		}
	}

	/**
	 * Escapes the source string, replacing characters that are illegal in an
	 * attribute value with a character reference to the character. This
	 * includes the greater than sign, single and double quotes. The ampersand
	 * character ('&amp;'), though illegal on its own, is not escaped as it is
	 * *assumed* to be part of a valid entity reference.
	 *
	 * @param source String to be escaped.
	 *
	 * @return Result of escaping {@code source}.
	 */
	public static String escapeAttributeValue( @NotNull final CharSequence source )
	{
		final StringBuilder result = new StringBuilder();
		escapeAttributeValue( result, source );
		return result.toString();
	}

	/**
	 * Escapes the given string for use as character data in an HTML document.
	 * Angled brackets ('<' and '>') and the ampersand ('&') are replaced with
	 * entity references.
	 *
	 * @param builder Builder to append the result to.
	 * @param source  String to be escaped.
	 */
	public static void escapeAttributeValue( @NotNull final StringBuilder builder, @NotNull final CharSequence source )
	{
		final int length = source.length();
		for ( int i = 0; i < length; i++ )
		{
			final char c = source.charAt( i );
			switch ( c )
			{
				case '<':
					builder.append( "&lt;" );
					break;
				case '>':
					builder.append( "&gt;" );
					break;
				case '&':
					builder.append( "&amp;" );
					break;
				case '"':
					builder.append( "&quot;" );
					break;
				case '\'':
					builder.append( "&#39;" );
					break;
				default:
					builder.append( c );
					break;
			}
		}
	}

	/**
	 * Escapes the given string for use as character data in an HTML document.
	 * Angled brackets ('<' and '>') and the ampersand ('&') are replaced with
	 * entity references.
	 *
	 * @param result Character sequence to append the result to.
	 * @param source String to be escaped.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void escapeAttributeValue( @NotNull final Appendable result, @NotNull final CharSequence source )
	throws IOException
	{
		final int length = source.length();
		for ( int i = 0; i < length; i++ )
		{
			final char c = source.charAt( i );
			switch ( c )
			{
				case '<':
					result.append( "&lt;" );
					break;
				case '>':
					result.append( "&gt;" );
					break;
				case '&':
					result.append( "&amp;" );
					break;
				case '"':
					result.append( "&quot;" );
					break;
				case '\'':
					result.append( "&#39;" );
					break;
				default:
					result.append( c );
					break;
			}
		}
	}

	/**
	 * Escapes the given string for use as character data in an HTML document.
	 * Angled brackets ('<' and '>') and the ampersand ('&') are replaced with
	 * entity references.
	 *
	 * @param source String to be escaped.
	 *
	 * @return Escaped string.
	 */
	public static String escapeCharacterData( @NotNull final CharSequence source )
	{
		final Appendable result = new StringBuilder();
		try
		{
			escapeCharacterData( result, source );
		}
		catch ( final IOException ignored )
		{
			throw new AssertionError( "StringBuilder must not throw IOException" );
		}
		return result.toString();
	}

	/**
	 * Escapes the given string for use as character data in an HTML document.
	 * Angled brackets ('<' and '>') and the ampersand ('&') are replaced with
	 * entity references.
	 *
	 * @param result Character sequence to append the result to.
	 * @param source String to be escaped.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void escapeCharacterData( @NotNull final Appendable result, @NotNull final CharSequence source )
	throws IOException
	{
		final int length = source.length();
		for ( int i = 0; i < length; i++ )
		{
			final char c = source.charAt( i );
			switch ( c )
			{
				case '<':
					result.append( "&lt;" );
					break;
				case '>':
					result.append( "&gt;" );
					break;
				case '&':
					result.append( "&amp;" );
					break;
				default:
					result.append( c );
					break;
			}
		}
	}

	/**
	 * Writes plain text as HTML, escaping any characters that have special
	 * meaning in HTML.
	 *
	 * @param in  Provides the text to be written
	 * @param out Receives an escaped version of the input text.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writePlainText( @NotNull final Reader in, @NotNull final Appendable out )
	throws IOException
	{
		int read;
		while ( ( read = in.read() ) != -1 )
		{
			switch ( read )
			{
				case '<':
					out.append( "&lt;" );
					break;
				case '>':
					out.append( "&gt;" );
					break;
				case '&':
					out.append( "&amp;" );
					break;
				default:
					out.append( (char)read );
					break;
			}
		}
	}

	/**
	 * Writes plain text as HTML, escaping any characters that have special
	 * meaning in HTML.
	 *
	 * @param in  Provides the text to be written
	 * @param out Receives an escaped version of the input text.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writePlainText( @NotNull final CharSequence in, @NotNull final Appendable out )
	throws IOException
	{
		for ( int i = 0; i < in.length(); i++ )
		{
			final char c = in.charAt( i );
			switch ( c )
			{
				case '<':
					out.append( "&lt;" );
					break;
				case '>':
					out.append( "&gt;" );
					break;
				case '&':
					out.append( "&amp;" );
					break;
				default:
					out.append( c );
					break;
			}
		}
	}

	/**
	 * Write element attributes to the specified writer.
	 *
	 * @param out        Destination for attributes.
	 * @param attributes Properties representing attributes (optional).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeAttributes( @NotNull final Appendable out, @Nullable final Properties attributes )
	throws IOException
	{
		if ( attributes != null )
		{
			for ( final String name : attributes.stringPropertyNames() )
			{
				final String value = attributes.getProperty( name );

				out.append( ' ' );
				out.append( name );
				out.append( '=' );
				out.append( '"' );
				escapeAttributeValue( out, value );
				out.append( '"' );
			}
		}
	}

	/**
	 * Merge two sets of attributes. If an attribute exists in both sets, the
	 * attribute in the second set overrides the first, except for the 'class'
	 * and 'style' attributes, which are combined.
	 *
	 * @param attributes1 First set of attributes.
	 * @param attributes2 Second set of attributes.
	 *
	 * @return Combined set of attributes.
	 */
	@Nullable
	public static Map<String, String> mergeAttributes( @Nullable final Map<String, String> attributes1, @Nullable final Map<String, String> attributes2 )
	{
		final Map<String, String> result;

		if ( ( attributes1 == null ) || attributes1.isEmpty() )
		{
			result = attributes2;
		}
		else if ( ( attributes2 == null ) || attributes2.isEmpty() )
		{
			result = attributes1;
		}
		else
		{
			result = new HashMap<String, String>();
			result.putAll( attributes1 );

			for ( final Map.Entry<String, String> entry : attributes2.entrySet() )
			{
				final String name = entry.getKey();
				final String value2 = entry.getValue();
				final String value1 = result.put( name, value2 );
				if ( value1 != null )
				{
					if ( "class".equals( name ) )
					{
						result.put( name, combineClasses( value1, value2 ) );
					}
					else if ( "style".equals( name ) )
					{
						result.put( name, combineStyles( value1, value2 ) );
					}
				}
			}
		}

		return result;
	}

	/**
	 * Write element attributes to the specified writer.
	 *
	 * @param out        Destination for attributes.
	 * @param attributes Properties representing attributes (optional).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeAttributes( @NotNull final Appendable out, @NotNull final Map<String, String> attributes )
	throws IOException
	{
		for ( final Map.Entry<String, String> entry : attributes.entrySet() )
		{
			out.append( ' ' );
			out.append( entry.getKey() );
			out.append( '=' );
			out.append( '"' );
			escapeAttributeValue( out, entry.getValue() );
			out.append( '"' );
		}
	}
}
