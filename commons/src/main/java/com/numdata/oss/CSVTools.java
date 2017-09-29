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
import java.util.regex.*;

/**
 * This class contains utility methods for reading and writing Character
 * Separated Values.
 *
 * @author G.B.M. Rupert
 * @see <a href="http://tools.ietf.org/html/rfc4180">RFC 4180: Common Format and
 * MIME Type for Comma-Separated Values (CSV) Files</a>
 */
public class CSVTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private CSVTools()
	{
	}

	/**
	 * Read values from the specified reader.
	 *
	 * The default separator is used, which is a comma (',').
	 *
	 * @param in Reader to read from.
	 *
	 * @return Values that were read from the reader (per row).
	 *
	 * @throws IOException when reading failed.
	 */
	public static List<List<String>> readCSV( final Reader in )
	throws IOException
	{
		return readCSV( in, ',' );
	}

	/**
	 * Read values from the specified reader.
	 *
	 * This method automatically removes any leading and trailing whitespace,
	 * if not quoted. <em>This behavior is not compliant with RFC 4180.</em>
	 *
	 * @param in        Reader to read from.
	 * @param separator Value separator.
	 *
	 * @return Values that were read from the reader (per row).
	 *
	 * @throws IOException when reading failed.
	 */
	public static List<List<String>> readCSV( final Reader in, final char separator )
	throws IOException
	{
		return readCSV( in, separator, false, false );
	}

	/**
	 * Read values from the specified reader.
	 *
	 * This method automatically removes any leading and trailing whitespace,
	 * if not quoted. <em>This behavior is not compliant with RFC 4180.</em>
	 *
	 * If {@code skipComments} is set, all lines that start with a '#' are
	 * considered to be comments and will be ignored. <em>This behavior is not
	 * compliant with RFC 4180.</em>
	 *
	 * If {@code skipEmptyRows} is set, all rows without any data are
	 * skipped.
	 *
	 * @param in            Reader to read from.
	 * @param separator     Value separator.
	 * @param skipComments  Skip lines that start with a '#'.
	 * @param skipEmptyRows Skip rows that have no non-empty columns.
	 *
	 * @return Values that were read from the reader (per row).
	 *
	 * @throws IOException when reading failed.
	 */
	public static List<List<String>> readCSV( final Reader in, final char separator, final boolean skipComments, final boolean skipEmptyRows )
	throws IOException
	{
		final List<List<String>> result = new ArrayList<List<String>>();

		final List<String> row = new ArrayList<String>();
		final StringBuilder value = new StringBuilder();

		boolean quotedValue = false;
		boolean quotesStarted = false;
		boolean quotesEnded = false;
		boolean valueStarted = false;
		boolean rowStarted = false;

		int whitespace = 0;

		char ch = (char)-1;
		do
		{
			char next = (char)in.read();

			if ( ch != (char)-1 )
			{
				if ( ( ch == '#' ) && skipComments && !rowStarted )
				{
					while ( ( next != '\n' ) && ( next != (char)-1 ) )
					{
						ch = next;
						next = (char)in.read();
					}
				}
				else if ( ch == separator )
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
							next = (char)in.read(); // Ignore next '"'.
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
						valueStarted = false;

						whitespace -= value.length();
						value.setLength( 0 ); // All characters before the quotes are ignored.
					}
					rowStarted = true;
				}
				else if ( Character.isWhitespace( ch ) )
				{
					if ( ch != '\n' )
					{
						if ( quotesStarted ) // Skip unquoted leading whitespace.
						{
							value.append( ch );
							valueStarted = true;
						}
						else if ( valueStarted ) // Include contained whitespace.
						{
							value.append( ch );
							whitespace++;
						}
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

				if ( ( ch == '\n' ) || ( next == (char)-1 ) )
				{
					if ( rowStarted )
					{
						if ( valueStarted )
						{
							if ( whitespace > 0 )
							{
								value.setLength( value.length() - whitespace );
								whitespace = 0;
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
							result.add( new ArrayList<String>( row ) );
						}
					}

					row.clear();
					value.setLength( 0 );
					quotedValue = false;
					quotesStarted = false;
					quotesEnded = false;
					valueStarted = false;
					rowStarted = false;
				}
			}

			ch = next;
		}
		while ( ch != (char)-1 );

		return result;
	}

	/**
	 * Write the specified rows to a string.
	 *
	 * The default separator is used, which is a comma (',').
	 *
	 * @param rows Rows to write.
	 *
	 * @return String with CSV document.
	 */
	public static String writeCSV( final Iterable<? extends Iterable<?>> rows )
	{
		return writeCSV( rows, ',' );
	}

	/**
	 * Write the specified rows to a string.
	 *
	 * @param rows      Rows to write.
	 * @param separator Separator to use.
	 *
	 * @return String with CSV document.
	 */
	public static String writeCSV( final Iterable<? extends Iterable<?>> rows, final char separator )
	{
		final StringBuilder result = new StringBuilder();
		try
		{
			writeCSV( result, rows, separator );
		}
		catch ( final IOException e )
		{
			throw new AssertionError( e.getMessage() );
		}
		return result.toString();
	}

	/**
	 * Write the specified rows to the specified destination.
	 *
	 * The default separator is used, which is a comma (',').
	 *
	 * @param dest Destination to write to.
	 * @param rows Rows to write.
	 *
	 * @throws IOException when writing failed.
	 */
	public static void writeCSV( final Appendable dest, final Iterable<? extends Iterable<?>> rows )
	throws IOException
	{
		writeCSV( dest, rows, ',' );
	}

	/**
	 * Write the specified rows to the specified destination.
	 *
	 * @param dest      Destination to write to.
	 * @param rows      Rows to write.
	 * @param separator Separator to use.
	 *
	 * @throws IOException when writing failed.
	 */
	public static void writeCSV( final Appendable dest, final Iterable<? extends Iterable<?>> rows, final char separator )
	throws IOException
	{
		writeCSV( dest, rows, separator, "\n" );
	}

	/**
	 * Write the specified rows to the specified destination.
	 *
	 * @param dest           Destination to write to.
	 * @param rows           Rows to write.
	 * @param separator      Separator to use.
	 * @param lineTerminator Line terminator to use.
	 *
	 * @throws IOException when writing failed.
	 */
	public static void writeCSV( final Appendable dest, final Iterable<? extends Iterable<?>> rows, final char separator, final String lineTerminator )
	throws IOException
	{
		for ( final Iterator<? extends Iterable<?>> it = rows.iterator(); it.hasNext(); )
		{
			writeRow( dest, it.next(), separator );

			if ( it.hasNext() )
			{
				dest.append( lineTerminator );
			}
		}
	}

	/**
	 * Writes the specified row to the specified destination.
	 *
	 * @param dest      Destination to write to.
	 * @param row       Row to write.
	 * @param separator Separator to use.
	 *
	 * @throws IOException when writing failed.
	 */
	public static void writeRow( final Appendable dest, final Iterable<?> row, final char separator )
	throws IOException
	{
		boolean writeSeparator = false;
		for ( final Object value : row )
		{
			if ( writeSeparator )
			{
				dest.append( separator );
			}

			writeValue( dest, value, separator );
			writeSeparator = true;
		}
	}

	/**
	 * Write the specified rows to the specified destination, using formatting
	 * where possible to make the output more tidy. The tidy output is <em>not
	 * compliant with RFC 4180</em>, due to the extra whitespace that is
	 * introduced. Use {@link #writeCSV} for compatibility.
	 *
	 * Single-column rows that start with a '#' character are considered
	 * comments and do not affect column widths.
	 *
	 * @param dest           Destination to write to.
	 * @param rows           Rows to write.
	 * @param separator      Separator to use.
	 * @param lineTerminator Line terminator to use.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeTidyCSV( final Appendable dest, final Iterable<? extends Iterable<?>> rows, final char separator, final String lineTerminator )
	throws IOException
	{
		final StringBuilder buffer = new StringBuilder();

		// Updated using 'ListIterator.set/add'.
		//noinspection MismatchedQueryAndUpdateOfCollection
		final List<Integer> columnWidths = new ArrayList<Integer>();

		for ( final Iterable<?> row : rows )
		{
			if ( isComment( row ) )
			{
				// comment
			}
			else
			{
				final Iterator<?> valueIterator = row.iterator();
				if ( valueIterator.hasNext() )
				{
					final ListIterator<Integer> columnWidthIterator = columnWidths.listIterator();

					while ( valueIterator.hasNext() )
					{
						buffer.setLength( 0 );
						writeValue( buffer, valueIterator.next(), separator );

						final int valueWidth = buffer.length();
						if ( columnWidthIterator.hasNext() )
						{
							final int columnWidth = columnWidthIterator.next();
							if ( columnWidth < valueWidth )
							{
								columnWidthIterator.set( valueWidth );
							}
						}
						else
						{
							columnWidthIterator.add( valueWidth );
						}
					}
				}
			}
		}

		for ( final Iterator<? extends Iterable<?>> rowIterator = rows.iterator(); rowIterator.hasNext(); )
		{
			final Iterable<?> row = rowIterator.next();

			if ( isComment( row ) )
			{
				buffer.setLength( 0 );
				for ( final Object value : row ) // should be only 1 value
				{
					buffer.append( value );
				}

				writeValue( dest, buffer, separator );
			}
			else
			{
				int columnIndex = 0;

				for ( final Iterator<?> valueIterator = row.iterator(); valueIterator.hasNext(); )
				{
					final Object value = valueIterator.next();

					buffer.setLength( 0 );
					writeValue( buffer, value, separator );
					final int valueLength = buffer.length();
					dest.append( buffer );

					if ( valueIterator.hasNext() )
					{
						final int columnWidth = columnWidths.get( columnIndex++ );
						for ( int i = valueLength; i < columnWidth; i++ )
						{
							dest.append( ' ' );
						}

						dest.append( separator );
						dest.append( ' ' );
					}
				}
			}

			if ( rowIterator.hasNext() )
			{
				dest.append( lineTerminator );
			}
		}
	}

	/**
	 * Returns whether the given row is a comment, in the context of writing
	 * tidy CSV. For compatibility, a comment is defined as a row with only one
	 * value, in the first column, which starts with a '#' character (after
	 * normal quote and whitespace handling).
	 *
	 * @param row Row to be checked.
	 *
	 * @return {@code true} if the rows represents a comment.
	 */
	private static boolean isComment( final Iterable<?> row )
	{
		boolean result = false;

		final Iterator<?> iterator = row.iterator();
		final Object firstValue = iterator.next();
		if ( ( firstValue instanceof CharSequence ) && !iterator.hasNext() )
		{
			final CharSequence firstString = (CharSequence)firstValue;
			if ( ( firstString.length() > 0 ) && ( firstString.charAt( 0 ) == '#' ) )
			{
				result = true;
			}
		}

		return result;
	}

	/**
	 * Write the specified value.
	 *
	 * @param dest      Destination to write to.
	 * @param value     Value to write.
	 * @param separator Separator to use.
	 *
	 * @throws IOException when writing failed.
	 */
	private static void writeValue( final Appendable dest, final Object value, final char separator )
	throws IOException
	{
		if ( value != null )
		{
			final String stringValue = String.valueOf( value );

			if ( writeQuoted( value, separator ) )
			{
				dest.append( '"' );
				dest.append( stringValue.replaceAll( "\"", "\"\"" ) );
				dest.append( '"' );
			}
			else
			{
				dest.append( stringValue );
			}
		}
	}

	/**
	 * Check if the specified value should be quoted before writing.
	 *
	 * RFC 4180 specifies that a value must be quoted if it contains a
	 * carriage return, line feed, double quote or comma. This method complies,
	 * but allows for other separator characters instead of comma. In addition,
	 * values with leading or trailing whitespace are also quoted, even though
	 * RFC 4180 does not allow whitespace to be ignored.
	 *
	 * @param value     Value to check.
	 * @param separator Separator that is being used.
	 *
	 * @return {@code true} if the value should be quoted; {@code false}
	 * otherwise.
	 */
	private static boolean writeQuoted( final Object value, final char separator )
	{
		final String separatorRegex = Pattern.quote( String.valueOf( separator ) );
		final Pattern quotesRequiredPattern = Pattern.compile( "(\\s.*)|(.*\\s)|(.*([\\r\\n\"]|" + separatorRegex + ").*)" );
		final Matcher matcher = quotesRequiredPattern.matcher( String.valueOf( value ) );
		return matcher.matches();
	}

	/**
	 * Detects the separator character used in the given stream. To help with
	 * the auto-detection, expected cell values can be specified. The given
	 * stream is reset to its previous state when the detection process is
	 * completed, using the {@link Reader#mark} and {@link Reader#reset}
	 * operations. To use a stream that doesn't support these operations, simply
	 * wrap the stream in a {@link BufferedReader}.
	 *
	 * @param in             Character stream to read from.
	 * @param expectedValues Expected cell values.
	 *
	 * @return Separator character.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @throws IllegalArgumentException if {@link Reader#markSupported()}
	 * returns {@code false} on the given stream.
	 */
	public static char detectSeparator( final Reader in, final String... expectedValues )
	throws IOException
	{
		if ( !in.markSupported() )
		{
			throw new IllegalArgumentException( "mark() not supported" );
		}

		final int readAheadLength = 4096;
		in.mark( readAheadLength );

		final String startOfFile;
		{
			int pos = 0;
			int lastEol = 0;
			final StringBuilder sb = new StringBuilder();

			int cur = ( pos++ < readAheadLength ) ? in.read() : -1;
			while ( cur >= 0 )
			{
				int next = ( pos++ < readAheadLength ) ? in.read() : -1;

				if ( ( cur == (int)'\r' ) || ( cur == (int)'\n' ) )
				{
					lastEol = sb.length();
					sb.append( (char)cur );

					if ( ( cur != next ) && ( ( next == (int)'\r' ) || ( next == (int)'\n' ) ) )
					{
						next = ( pos++ < readAheadLength ) ? in.read() : -1;
					}
				}
				else
				{
					sb.append( (char)cur );
				}
				cur = next;
			}

			sb.setLength( lastEol );
			startOfFile = sb.toString();
		}

		final char[] separators = { ',', ';', ':', '\t' };

		char result = separators[ 0 ];
		int bestScore = -1;

		for ( final char separator : separators )
		{
			final List<List<String>> rows = readCSV( new StringReader( startOfFile ), separator );

			int score = 0;

			if ( !rows.isEmpty() )
			{
				/*
				 * Add the number of cells in consecutive rows with the same
				 * number of columns to the score.
				 */
				final List<String> firstRow = rows.get( 0 );
				int columnCount = firstRow.size();
				boolean allRowsHaveSameColumnCount = true;

				for ( final List<String> row : rows )
				{
					if ( row.size() == columnCount )
					{
						score += columnCount - 1;
					}
					else
					{
						columnCount = row.size();
						allRowsHaveSameColumnCount = false;
					}

					for ( final String value : expectedValues )
					{
						if ( row.contains( value ) )
						{
							score += 1000;
							break;
						}
					}
				}

				if ( allRowsHaveSameColumnCount )
				{
					score += ( columnCount - 1 ) * rows.size();
				}
			}

			/*
			 * Keep the separator with the best score as result.
			 */
			if ( score > bestScore )
			{
				bestScore = score;
				result = separator;
			}
		}

		in.reset();

		return result;
	}

	/**
	 * This utility method can be used in combination with {@link #readCSV} to
	 * handle CSV files that have a column identifier row. This method converts
	 * each data row to a map that uses the header row values as key.
	 *
	 * @param csvData      CSV data (from {@link #readCSV} method).
	 * @param headerRow    Index of header row (typically 0).
	 * @param firstDataRow First data row (typically 1).
	 *
	 * @return List of data records.
	 */
	public static List<Map<String, String>> getMappedByHeader( final List<List<String>> csvData, final int headerRow, final int firstDataRow )
	{
		final int csvDataRowCount = csvData.size();
		final List<Map<String, String>> result = new ArrayList<Map<String, String>>( csvDataRowCount - firstDataRow );

		final List<String> headers = csvData.get( headerRow );
		final int headerCount = headers.size();

		for ( int rowIndex = firstDataRow; rowIndex < csvDataRowCount; rowIndex++ )
		{
			final List<String> rowData = csvData.get( rowIndex );
			final Map<String, String> rowMap = new HashMap<String, String>();
			final int maxColumn = Math.min( headerCount, rowData.size() );

			for ( int columnIndex = 0; columnIndex < maxColumn; columnIndex++ )
			{
				final String columnHeader = headers.get( columnIndex );
				if ( ( columnHeader != null ) && !columnHeader.isEmpty() )
				{
					final String value = rowData.get( columnIndex );
					if ( value != null )
					{
						if ( rowMap.put( columnHeader, value ) != null )
						{
							throw new IllegalArgumentException( "Duplicate column: " + columnHeader );
						}
					}
				}
			}

			result.add( rowMap );
		}

		return result;
	}
}
