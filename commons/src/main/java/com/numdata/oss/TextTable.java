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
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class provides functionality to create tables as plain text.
 *
 * This is especially suitable for console applications and tests.
 *
 * @author Peter S. Heijnen
 */
public class TextTable
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private TextTable()
	{
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
	public static String getText( @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data )
	{
		final Appendable result = new StringBuilder();
		try
		{
			write( result, headers, data, "\n" );
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
	 * @param out     Character stream to write output to.
	 * @param headers List of table headers.
	 * @param data    Contents of table.
	 * @param lineSep Line separator to use (typically '\n').
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void write( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, @NotNull final CharSequence lineSep )
	throws IOException
	{
		write( out, headers, data, lineSep, "null" );
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out       Character stream to write output to.
	 * @param headers   List of table headers.
	 * @param data      Contents of table.
	 * @param lineSep   Line separator to use (typically '\n').
	 * @param nullValue Value to use instead of null data.
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void write( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
	throws IOException
	{
		write( out, headers, data, "", lineSep, nullValue );
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out       Print stream to write output to.
	 * @param headers   List of table headers.
	 * @param data      Contents of table.
	 * @param indent    Line indent string (empty if not indenting).
	 * @param lineSep   Line separator to use (typically '\n').
	 * @param nullValue Value to use instead of null data.
	 */
	public static void write( @NotNull final PrintStream out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, @NotNull final CharSequence indent, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
	{
		try
		{
			write( (Appendable)out, headers, data, indent, lineSep, nullValue );
		}
		catch ( final IOException e )
		{
			// should never happen when PrintStream is used, therefore throw AssertionError
			throw new AssertionError( e );
		}
	}

	/**
	 * Write textual representation of a table to a character stream.
	 *
	 * @param out       Character stream to write output to.
	 * @param headers   List of table headers.
	 * @param data      Contents of table.
	 * @param indent    Line indent string (empty if not indenting).
	 * @param lineSep   Line separator to use (typically '\n').
	 * @param nullValue Value to use instead of null data.
	 *
	 * @throws IOException when writing of output fails.
	 */
	public static void write( @NotNull final Appendable out, @Nullable final List<String> headers, @Nullable final List<? extends List<?>> data, @NotNull final CharSequence indent, @NotNull final CharSequence lineSep, @Nullable final String nullValue )
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

		final int headerLines = getHeaderLineCount( headers );

		final int rowCount = headerLines + dataLines;
		final String[][] strings = new String[ rowCount + 1 ][ columnCount ];
		final boolean[] columnRightAligned = new boolean[ columnCount ];
		final int[] columnWidths = new int[ columnCount ];

		if ( headers != null )
		{
			addHeaderData( strings, headers, columnWidths, columnRightAligned );
		}

		if ( data != null )
		{
			for ( int dataRowIndex = 0; dataRowIndex < data.size(); dataRowIndex++ )
			{
				final List<?> contentRow = data.get( dataRowIndex );
				if ( contentRow != null )
				{
					addDataRow( strings, headerLines + dataRowIndex, contentRow, columnWidths, nullValue );
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
					TextTools.appendFixed( out, strings[ rowIndex ][ columnIndex ], columnWidths[ columnIndex ], columnRightAligned[ columnIndex ], ' ' );
					out.append( ' ' );
				}
				out.append( "|" ).append( lineSep );
			}

			if ( ( rowIndex == -1 ) || ( rowIndex == headerLines - 1 ) || ( rowIndex == ( rowCount - 1 ) ) )
			{
				out.append( indent );
				for ( int columnIndex = 0; columnIndex < columnCount; columnIndex++ )
				{
					out.append( "+" );
					TextTools.appendFixed( out, null, columnWidths[ columnIndex ] + 2, false, '-' );
				}

				out.append( "+" ).append( lineSep );
			}
		}
	}

	/**
	 * Returns number of header lines required for the given headers.
	 *
	 * @param headers List of table headers.
	 *
	 * @return Number of header lines.
	 */
	private static int getHeaderLineCount( @Nullable final Iterable<String> headers )
	{
		int headerLines = 0;
		if ( headers != null )
		{
			for ( final String header : headers )
			{
				if ( header != null )
				{
					final List<String> lines = TextTools.tokenize( header, '\n', true );
					headerLines = Math.max( headerLines, lines.size() );
				}
			}
		}
		return headerLines;
	}

	/**
	 * Add header row to the table.
	 *
	 * @param result             Resulting table data.
	 * @param headers            List of table headers.
	 * @param columnWidths       Width of each column (updated).
	 * @param columnRightAligned Whether each column is right-aligned.
	 */
	private static void addHeaderData( @NotNull final String[][] result, @NotNull final List<String> headers, @NotNull final int[] columnWidths, @NotNull final boolean[] columnRightAligned )
	{
		for ( int columnIndex = 0; columnIndex < headers.size(); columnIndex++ )
		{
			final String string = headers.get( columnIndex );
			if ( string != null )
			{
				final boolean rightAligned = TextTools.startsWith( string, '>' );
				columnRightAligned[ columnIndex ] = rightAligned;

				final List<String> lines = TextTools.tokenize( rightAligned ? string.substring( 1 ) : string, '\n', true );
				for ( int i = 0; i < lines.size(); i++ )
				{
					final String line = lines.get( i );
					result[ i ][ columnIndex ] = line;
					columnWidths[ columnIndex ] = Math.max( columnWidths[ columnIndex ], line.length() );
				}
			}
		}
	}

	/**
	 * Add data row to the table.
	 *
	 * @param result       Resulting table data.
	 * @param rowIndex     Index in result.
	 * @param rowData      Data to add to table.
	 * @param columnWidths Width of each column (updated).
	 * @param nullValue    Value to use instead of null data.
	 */
	private static void addDataRow( @NotNull final String[][] result, final int rowIndex, @NotNull final List<?> rowData, @NotNull final int[] columnWidths, final @Nullable String nullValue )
	{
		final String[] resultRow = result[ rowIndex ];

		for ( int columnIndex = 0; columnIndex < rowData.size(); columnIndex++ )
		{
			final Object value = rowData.get( columnIndex );
			final String string = ( value == null ) ? nullValue : String.valueOf( value ).trim();
			if ( string != null )
			{
				columnWidths[ columnIndex ] = Math.max( columnWidths[ columnIndex ], string.length() );
				resultRow[ columnIndex ] = string;
			}
		}
	}
}
