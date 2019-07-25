/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
 * Parses character-separated value (CSV) data.
 *
 * <p>The current implementation is not strictly RFC 4180 compliant:
 *
 * <ul>
 *
 * <li>Leading and trailing whitespace is removed if not quoted.</li>
 *
 * <li>Only '\n' is recognized as a line ending.</li>
 *
 * <li>Line endings can not be quoted.</li>
 *
 * </ul>
 *
 * @author Gerrit Meinders
 * @author G.B.M. Rupert
 * @see <a href="http://tools.ietf.org/html/rfc4180">RFC 4180: Common Format and
 * MIME Type for Comma-Separated Values (CSV) Files</a>
 */
public class CSVParser
{
	/**
	 * Value separator.
	 */
	private char _separator = ',';

	/**
	 * Skip lines that start with a '#'.
	 */
	private boolean _skipComments = false;

	/**
	 * Skip rows that have no non-empty columns.
	 */
	private boolean _skipEmptyRows = false;

	public char getSeparator()
	{
		return _separator;
	}

	public void setSeparator( final char separator )
	{
		_separator = separator;
	}

	public boolean isSkipComments()
	{
		return _skipComments;
	}

	public void setSkipComments( final boolean skipComments )
	{
		_skipComments = skipComments;
	}

	public boolean isSkipEmptyRows()
	{
		return _skipEmptyRows;
	}

	public void setSkipEmptyRows( final boolean skipEmptyRows )
	{
		_skipEmptyRows = skipEmptyRows;
	}

	/**
	 * Reads all rows from the given stream.
	 *
	 * @param reader Character stream to read from.
	 *
	 * @return Rows of CSV data.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@NotNull
	public List<List<String>> readAll( @NotNull final Reader reader )
	throws IOException
	{
		final List<List<String>> result = new ArrayList<List<String>>();

		final BufferedReader bufferedReader = ( reader instanceof BufferedReader ) ? (BufferedReader)reader : new BufferedReader( reader );
		for ( String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine() )
		{
			final List<String> row = parseLine( line );
			if ( row != null )
			{
				result.add( row );
			}
		}
		return result;
	}

	/**
	 * Reads next rom from the given stream.
	 *
	 * @param reader Character stream to read from.
	 *
	 * @return Next from from CSV file; {@code null} if no more data is
	 * available.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Nullable
	public List<String> readRow( @NotNull final BufferedReader reader )
	throws IOException
	{
		List<String> result = null;

		for ( String line = reader.readLine(); line != null; line = reader.readLine() )
		{
			final List<String> row = parseLine( line );
			if ( row != null )
			{
				result = row;
				break;
			}
		}

		return result;
	}

	/**
	 * Parses all the given lines.
	 *
	 * @param lines Lines to parse.
	 *
	 * @return Rows of CSV data.
	 */
	@NotNull
	public List<List<String>> parseLines( @NotNull final Iterable<? extends CharSequence> lines )
	{
		final List<List<String>> result = new ArrayList<List<String>>( ( lines instanceof Collection<?> ) ? ( (Collection<CharSequence>)lines ).size() : 0 );

		for ( final CharSequence line : lines )
		{
			final List<String> row = parseLine( line );
			if ( row != null )
			{
				result.add( row );
			}
		}

		return result;
	}

	/**
	 * Reads the next row of CSV data from the given line.
	 *
	 * @param line Text line.
	 *
	 * @return Row of CSV data; {@code null} if the input yielded no CSV data.
	 */
	@Nullable
	public List<String> parseLine( @NotNull final CharSequence line )
	{
		final char separator = _separator;
		final boolean skipComments = _skipComments;
		final boolean skipEmptyRows = _skipEmptyRows;

		final List<String> row = new ArrayList<String>();
		final StringBuilder value = new StringBuilder();

		boolean quotedValue = false;
		boolean quotesStarted = false;
		boolean quotesEnded = false;
		boolean valueStarted = false;
		boolean rowStarted = false;

		int whitespace = 0;

		final int length = line.length();
		int pos = 0;
		while ( pos < length )
		{
			final char ch = line.charAt( pos++ );
			final char next = ( pos < length ) ? line.charAt( pos ) : (char)-1;

			if ( ch != (char)-1 )
			{
				if ( ( ch == '#' ) && skipComments && !rowStarted )
				{
					break;
				}

				if ( ch == separator )
				{
					if ( quotesStarted )
					{
						value.append( ch );
						valueStarted = true;
					}
					else
					{
						if ( whitespace > 0 )
						{
							value.setLength( value.length() - whitespace );
							whitespace = 0;
						}

						row.add( value.toString() );

						value.setLength( 0 );
						quotedValue = false;
						quotesStarted = false;
						quotesEnded = false;
						valueStarted = false;
					}
					rowStarted = true;
				}
				else if ( ch == '"' )
				{
					if ( quotesStarted )
					{
						if ( next == '"' )
						{
							pos++;
							value.append( ch );
							valueStarted = true;
						}
						else
						{
							quotesEnded = true;
							quotesStarted = false;
						}
					}
					else
					{
						quotedValue = true;
						quotesStarted = true;
						quotesEnded = false;
						valueStarted = true; // Because empty values can be quoted too.

						whitespace -= value.length();
						value.setLength( 0 ); // All characters before the quotes are ignored.
					}
					rowStarted = true;
				}
				else if ( Character.isWhitespace( ch ) )
				{
					if ( quotesStarted ) // Skip unquoted leading whitespace.
					{
						value.append( ch );
						valueStarted = true;
					}
					else if ( valueStarted && !quotesEnded ) // Include contained whitespace.
					{
						value.append( ch );
						whitespace++;
					}
				}
				else
				{
					if ( quotedValue )
					{
						if ( !quotesEnded )
						{
							value.append( ch );
							valueStarted = true;
						}
					}
					else
					{
						value.append( ch );
						valueStarted = true;
						whitespace = 0;
					}
					rowStarted = true;
				}
			}
		}

		List<String> result = null;

		if ( rowStarted )
		{
			if ( valueStarted )
			{
				if ( whitespace > 0 )
				{
					value.setLength( value.length() - whitespace );
				}

				row.add( value.toString() );
			}

			boolean addRow = true;
			if ( skipEmptyRows )
			{
				addRow = false;

				for ( final String column : row )
				{
					if ( !column.isEmpty() )
					{
						addRow = true;
						break;
					}
				}
			}

			if ( addRow )
			{
				result = row;
			}
		}

		return result;
	}
}
