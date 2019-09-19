/*
 * Copyright (c) 2006-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.web;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.jsp.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class implements a generator for HTML tables. It is designed to make
 * creation of tables easy for the page developer and also allow a flexible
 * layout by providing hooks for derivative classes.
 *
 * @author Peter S. Heijnen
 */
public class HTMLTable
{
	/**
	 * Format used in column headers to specify column spans.
	 */
	private static final Pattern COLSPAN_PATTERN = Pattern.compile( "(.*)\\*(\\d+)" );

	/**
	 * Horizontal alignment.
	 */
	public enum HorizontalAlignment
	{
		/**
		 * Default.
		 */DEFAULT,
		/**
		 * Left.
		 */LEFT,
		/**
		 * Center.
		 */CENTER,
		/**
		 * Right.
		 */RIGHT
	}

	/**
	 * Internal state: nothing done, no <table> yet.
	 */
	private static final int BEFORE_TABLE = 0;

	/**
	 * Internal state: </tr> done, no <tr>  yet.
	 */
	private static final int BEFORE_ROW = 1;

	/**
	 * Internal state: <tr>/</td> done, no <td>  yet.
	 */
	private static final int BEFORE_COLUMN = 2;

	/**
	 * Internal state: <td>  done, no </td> yet.
	 */
	private static final int IN_COLUMN = 3;

	/**
	 * Index of row currently being written (0 = first).
	 */
	protected int _row = -1;

	/**
	 * Number of columns in table.
	 */
	private final int _columnCount;

	/**
	 * Column titles (singular array to set table title).
	 */
	private final String[] _columnNames;

	/**
	 * Default horizontal alignment of columns.
	 */
	private HorizontalAlignment[] _columnAlignment;

	/**
	 * Column classes.
	 */
	private String[] _columnClasses;

	/**
	 * Index of column currently being written (0 = first).
	 */
	protected int _column;

	/**
	 * Flag to indicate that the column currently being written is a head
	 * column/cell (TH) instead of a data column/cell (TD).
	 */
	private boolean _columnIsHead;

	/**
	 * Column span of column currently being written.
	 */
	private int _colspan;

	/**
	 * State of HTML generator.
	 */
	private int _state;

	/**
	 * Construct a new table without a title and one column.
	 */
	public HTMLTable()
	{
		_columnCount = 1;
		_columnNames = null;
		_columnAlignment = null;
		_columnClasses = null;

		_column = 0;
		_columnIsHead = false;
		_colspan = 1;
		_state = BEFORE_TABLE;
	}

	/**
	 * Construct a new table with the specified (column) title(s) and the
	 * specified number of columns. A title can be set for each column of the
	 * table. If you specify only one title for a table with more than one
	 * column, that title will be used for the table. This is an akward
	 * construction with historical roots and should probably be removed for
	 * during the next update cycle of this class.
	 *
	 * @param columnNames Column titles (singular array to set table title).
	 * @param columnCount Number of columns in table.
	 */
	public HTMLTable( final String[] columnNames, final int columnCount )
	{
		_columnCount = ( columnCount < 1 ) ? 1 : columnCount;
		_columnNames = ( columnNames != null ) ? columnNames.clone() : null;
		_columnAlignment = null;
		_columnClasses = null;

		_column = 0;
		_columnIsHead = false;
		_colspan = 1;
		_state = BEFORE_TABLE;
	}

	/**
	 * Finish output of current column.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter to use for output (possible wrapped).
	 *
	 * @throws IOException if an I/O error occurs while generating data.
	 */
	public IndentingJspWriter endColumn( @NotNull final JspWriter out )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		switch ( _state )
		{
			case IN_COLUMN: // <td>  done, no </td> yet
				_state = BEFORE_COLUMN;
				writeColumnEnd( iw, _columnIsHead );
				_column += _colspan;
				break;

			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
			case BEFORE_ROW: // </tr> done, no <tr>  yet
			case BEFORE_TABLE:
				break;

			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}

		return iw;
	}

	/**
	 * Finish output of current row.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter to use for output (possible wrapped).
	 *
	 * @throws IOException if an I/O error occurs while generating data.
	 */
	@SuppressWarnings( "FieldRepeatedlyAccessedInMethod" )
	public IndentingJspWriter endRow( @NotNull final JspWriter out )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		switch ( _state )
		{
			case IN_COLUMN: // <td>  done, no </td> yet
				_state = BEFORE_COLUMN;
				writeColumnEnd( iw, _columnIsHead );
				_column += _colspan;
				writeRowEnd( iw );
				_state = BEFORE_ROW;
				break;

			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
				writeRowEnd( iw );
				_state = BEFORE_ROW;
				break;

			case BEFORE_ROW: // </tr> done, no <tr>  yet
			case BEFORE_TABLE:
				break;

			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}

		return iw;
	}

	/**
	 * Finish output of table.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter to use for output (possible wrapped).
	 *
	 * @throws IOException if an I/O error occurs while generating data.
	 */
	@SuppressWarnings( "FieldRepeatedlyAccessedInMethod" )
	public IndentingJspWriter endTable( @NotNull final JspWriter out )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		switch ( _state )
		{
			case IN_COLUMN: // <td>  done, no </td> yet
				_state = BEFORE_COLUMN;
				writeColumnEnd( iw, _columnIsHead );
				_column += _colspan;
				writeRowEnd( iw );
				writeTableEnd( iw );
				_state = BEFORE_TABLE;
				break;

			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
				writeRowEnd( iw );
				writeTableEnd( iw );
				_state = BEFORE_TABLE;
				break;

			case BEFORE_ROW: // </tr> done, no <tr>  yet
				writeTableEnd( iw );
				_state = BEFORE_TABLE;
				break;

			case BEFORE_TABLE:
				break;

			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}

		return iw;
	}

	/**
	 * Get index of row currently being written. The first row has index {@code
	 * 0}. Before the start of the first row, the result is {@code -1}.
	 *
	 * @return Index of row currently being written (0 = first).
	 */
	public int getRowIndex()
	{
		return _row;
	}

	/**
	 * Returns the number of rows currently in the table.
	 *
	 * @return Number of rows.
	 */
	public int getRowCount()
	{
		return _row + 1;
	}

	/**
	 * Get index of column currently being written (0 = first).
	 *
	 * @return Index of column currently being written (0 = first).
	 */
	public int getColumn()
	{
		return _column;
	}

	/**
	 * Get the number of columns in this table. This is the number of columns
	 * set at construction time and in no way guarantees that the application
	 * doesn't write more or fewer columns in a row. It is up to the application
	 * to set the correct number of columns.
	 *
	 * @return Number of columns in this table.
	 */
	public int getColumnCount()
	{
		return Math.max( 1, _columnCount );
	}

	/**
	 * Get title of the specified columnIndex.
	 *
	 * @param columnIndex Column number.
	 *
	 * @return Title of columnIndex ({@code null} for none).
	 */
	public String getColumnTitle( final int columnIndex )
	{
		final String[] names = _columnNames;
		return ( ( names == null ) || ( columnIndex < 0 ) || ( columnIndex >= names.length ) ) ? null : names[ columnIndex ];
	}

	/**
	 * Get alignment of the given column.
	 *
	 * @param columnIndex Column number.
	 *
	 * @return Horizontal alignment.
	 */
	public HorizontalAlignment getColumnAlignment( final int columnIndex )
	{
		final HorizontalAlignment[] align = _columnAlignment;
		return ( ( align == null ) || ( columnIndex < 0 ) || ( columnIndex >= align.length ) ) ? HorizontalAlignment.DEFAULT : align[ columnIndex ];
	}

	/**
	 * Set default horizontal alignment of columns.
	 *
	 * @param columnIndex Column number.
	 * @param alignment   Horizontal column alignment.
	 */
	public void setColumnAlignment( final int columnIndex, @NotNull final HorizontalAlignment alignment )
	{
		final int columnCount = getColumnCount();
		if ( ( columnIndex < 0 ) || ( columnIndex >= columnCount ) )
		{
			throw new IllegalArgumentException( "columnIndex: " + columnIndex + ", columnCount: " + columnCount );
		}

		HorizontalAlignment[] columnAlignment = _columnAlignment;
		if ( columnAlignment == null )
		{
			columnAlignment = new HorizontalAlignment[ columnCount ];
			Arrays.fill( columnAlignment, HorizontalAlignment.DEFAULT );
			_columnAlignment = columnAlignment;
		}

		columnAlignment[ columnIndex ] = alignment;
	}

	/**
	 * Set default horizontal alignment of columns.
	 *
	 * @param alignment Horionztal alignment of columns.
	 */
	public void setColumnAlignment( @NotNull final HorizontalAlignment... alignment )
	{
		_columnAlignment = alignment.clone();
	}

	/**
	 * Set default horizontal alignment of columns.
	 *
	 * @param alignment Horizontal alignment of each column.
	 */
	public void setColumnAlignment( @NotNull final List<HorizontalAlignment> alignment )
	{
		setColumnAlignment( alignment.toArray( new HorizontalAlignment[ alignment.size() ] ) );
	}

	/**
	 * Add column class(es).
	 *
	 * @param columnIndex Column number.
	 * @param columnClass Column class(es).
	 */
	public void addColumnClass( final int columnIndex, @NotNull final String columnClass )
	{
		setColumnClass( columnIndex, HTMLTools.combineClasses( getColumnClass( columnIndex ), columnClass ) );
	}

	/**
	 * Get column class(es).
	 *
	 * @param columnIndex Column number.
	 *
	 * @return Column class; {@code null} if column has no class(es).
	 */
	public String getColumnClass( final int columnIndex )
	{
		final String[] columnClasses = _columnClasses;
		return ( ( columnClasses == null ) || ( columnIndex < 0 ) || ( columnIndex >= columnClasses.length ) ) ? null : columnClasses[ columnIndex ];
	}

	/**
	 * Set column class(es).
	 *
	 * @param columnIndex Column number.
	 * @param columnClass Column class(es).
	 */
	public void setColumnClass( final int columnIndex, final String columnClass )
	{
		final int columnCount = getColumnCount();
		if ( ( columnIndex < 0 ) || ( columnIndex >= columnCount ) )
		{
			throw new IllegalArgumentException( "columnIndex: " + columnIndex + ", columnCount: " + columnCount );
		}

		String[] columnClasses = _columnClasses;
		if ( columnClasses == null )
		{
			columnClasses = new String[ columnCount ];
			_columnClasses = columnClasses;
		}

		columnClasses[ columnIndex ] = columnClass;
	}

	/**
	 * Get title of table (if any). If the table has column titles, or no title
	 * was set, {@code null} is returned.
	 *
	 * @return Table table; {@code null} if the table has no title (or column
	 * titles).
	 */
	public String getTableTitle()
	{
		final String[] names = _columnNames;
		return ( ( names == null ) || ( names.length != 1 ) ) ? null : names[ 0 ];
	}

	/**
	 * Return {@code true} if the table has a title string for each of the
	 * columns in the table.
	 *
	 * @return {@code true} if table has a title string for each of the columns.
	 */
	public boolean hasColumnTitles()
	{
		return ( ( _columnNames != null ) && ( _columnNames.length > 1 ) );
	}

	/**
	 * Return {@code true} if the table has a title string for the whole table.
	 *
	 * @return {@code true} if table has a title string for the whole table.
	 */
	public boolean hasTableTitle()
	{
		return ( ( _columnNames != null ) && ( _columnNames.length == 1 ) );
	}

	/**
	 * Return {@code true} if the table has a title row (either singular or per
	 * column.
	 *
	 * @return {@code true} if table has a title row.
	 */
	public boolean hasTitleRow()
	{
		return ( ( _columnNames != null ) && ( _columnNames.length > 0 ) );
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out )
	throws IOException
	{
		return newColumn( out, false, 1, HorizontalAlignment.DEFAULT, null );
	}

	/**
	 * Start a new column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param isHead     {@code true} to write 'TH' tag; {@code false} to write
	 *                   'TD' tag.
	 * @param colspan    Column span to use (1 or less for unspecified).
	 * @param align      Horizontal alignment (DEFAULT, LEFT, CENTER, or
	 *                   RIGHT).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	@SuppressWarnings( "FieldRepeatedlyAccessedInMethod" )
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, final boolean isHead, final int colspan, @NotNull final HorizontalAlignment align, @Nullable final Properties attributes )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		/*
		 * Do state preparation.
		 *
		 * STATE GUARANTEED AFTER THIS SWITCH: BEFORE_COLUMN
		 */
		switch ( _state )
		{
			case BEFORE_TABLE: // nothing done, no <table> yet
				_state = BEFORE_ROW;
				writeTableBegin( iw, null );
				_column = 0;
				_state = BEFORE_COLUMN;
				writeRowBegin( iw, null );
				break;

			case BEFORE_ROW: // </tr> done, no <tr>  yet
				_column = 0;
				_state = BEFORE_COLUMN;
				writeRowBegin( iw, null );
				break;

			case IN_COLUMN: // <td>  done, no </td> yet
				_state = BEFORE_COLUMN;
				writeColumnEnd( iw, _columnIsHead );
				_column += _colspan;
				break;

			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
				break;

			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}

		/*
		 * Process column HTML arguments.
		 */
		final Properties actualAttributes = new Properties( attributes );

		if ( ( colspan > 1 ) && ( actualAttributes.getProperty( "colspan" ) == null ) )
		{
			actualAttributes.setProperty( "colspan", String.valueOf( colspan ) );
		}

		if ( actualAttributes.getProperty( "align" ) == null )
		{
			switch ( ( align == HorizontalAlignment.DEFAULT ) ? getColumnAlignment( _column ) : align )
			{
				case LEFT:
					HTMLTools.addStyle( actualAttributes, "text-align: left" );
					break;
				case RIGHT:
					HTMLTools.addStyle( actualAttributes, "text-align: right" );
					break;
				case CENTER:
					HTMLTools.addStyle( actualAttributes, "text-align: center" );
			}
		}

		HTMLTools.addClasses( actualAttributes, getColumnClass( _column ) );

		/*
		 * Update column info, write column begin en set state to 'IN_COLUMN'.
		 */
		_columnIsHead = isHead;
		_colspan = ( colspan < 1 ) ? 1 : colspan;
		_state = IN_COLUMN;
		writeColumnBegin( iw, _column, _columnIsHead, actualAttributes );

		return iw;
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out     JspWriter to use for output.
	 * @param colspan Column span to use (1 or less for unspecified).
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, final int colspan )
	throws IOException
	{
		return newColumn( out, false, colspan, HorizontalAlignment.DEFAULT, null );
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out     JspWriter to use for output.
	 * @param colspan Column span to use (1 or less for unspecified).
	 * @param align   Horizontal alignment (DEFAULT, LEFT, CENTER, or RIGHT).
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, final int colspan, @NotNull final HorizontalAlignment align )
	throws IOException
	{
		return newColumn( out, false, colspan, align, null );
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param colspan    Column span to use (1 or less for unspecified).
	 * @param align      Horizontal alignment (DEFAULT, LEFT, CENTER, or
	 *                   RIGHT).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, final int colspan, @NotNull final HorizontalAlignment align, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, false, colspan, align, attributes );
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param colspan    Column span to use (1 or less for unspecified).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, final int colspan, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, false, colspan, HorizontalAlignment.DEFAULT, attributes );
	}

	/**
	 * Start a new data column. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newColumn( @NotNull final JspWriter out, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, false, 1, HorizontalAlignment.DEFAULT, attributes );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out )
	throws IOException
	{
		return newColumn( out, true, 1, HorizontalAlignment.DEFAULT, null );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out     JspWriter to use for output.
	 * @param colspan Column span to use (1 or less for unspecified).
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out, final int colspan )
	throws IOException
	{
		return newColumn( out, true, colspan, HorizontalAlignment.DEFAULT, null );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out     JspWriter to use for output.
	 * @param colspan Column span to use (1 or less for unspecified).
	 * @param align   Horizontal alignment (DEFAULT, LEFT, CENTER, or RIGHT).
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out, final int colspan, @NotNull final HorizontalAlignment align )
	throws IOException
	{
		return newColumn( out, true, colspan, align, null );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param colspan    Column span to use (1 or less for unspecified).
	 * @param align      Horizontal alignment (DEFAULT, LEFT, CENTER, or
	 *                   RIGHT).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out, final int colspan, @NotNull final HorizontalAlignment align, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, true, colspan, align, attributes );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param colspan    Column span to use (1 or less for unspecified).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out, final int colspan, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, true, colspan, HorizontalAlignment.DEFAULT, attributes );
	}

	/**
	 * Start a new column head. If a column is already being written, it is
	 * terminated. Also, a new table or row is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newHead( @NotNull final JspWriter out, @Nullable final Properties attributes )
	throws IOException
	{
		return newColumn( out, true, 1, HorizontalAlignment.DEFAULT, attributes );
	}

	/**
	 * Start a new row. If a column or row is being written, both are
	 * terminated. Also, a new table is started if needed.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newRow( @NotNull final JspWriter out )
	throws IOException
	{
		return newRow( out, 1, null );
	}

	/**
	 * Start a new row. If a column or row is being written, both are
	 * terminated. Also, a new table is started if needed.
	 *
	 * @param out     JspWriter to use for output.
	 * @param rowspan Row span to use (1 or less for unspecified).
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newRow( @NotNull final JspWriter out, final int rowspan )
	throws IOException
	{
		return newRow( out, rowspan, null );
	}

	/**
	 * Start a new row. If a column or row is being written, both are
	 * terminated. Also, a new table is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newRow( @NotNull final JspWriter out, @Nullable final Properties attributes )
	throws IOException
	{
		return newRow( out, 1, attributes );
	}

	/**
	 * Start a new row. If a column or row is being written, both are
	 * terminated. Also, a new table is started if needed.
	 *
	 * @param out        JspWriter to use for output.
	 * @param rowspan    Row span to use (1 or less for unspecified).
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	@SuppressWarnings( "FieldRepeatedlyAccessedInMethod" )
	public IndentingJspWriter newRow( @NotNull final JspWriter out, final int rowspan, @Nullable final Properties attributes )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		/*
		 * Do state preparation.
		 *
		 * STATE GUARANTEEED AFTER THIS SWITCH: BEFORE_ROW
		 */
		switch ( _state )
		{
			case BEFORE_TABLE: // nothing done, no <table> yet
				_state = BEFORE_ROW;
				writeTableBegin( iw, null );
				_state = BEFORE_ROW;
				break;

			case BEFORE_ROW: // </tr> done, no <tr>  yet
				break;

			case IN_COLUMN: // <td>  done, no </td> yet
				_state = BEFORE_COLUMN;
				writeColumnEnd( iw, _columnIsHead );
				_column += _colspan;
				writeRowEnd( iw );
				_state = BEFORE_ROW;
				break;

			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
				writeRowEnd( iw );
				_state = BEFORE_ROW;
				break;

			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}

		/*
		 * Process column HTML arguments.
		 */
		final Properties actualAttributes = new Properties( attributes );

		if ( ( rowspan > 1 ) && ( actualAttributes.getProperty( "rowspan" ) == null ) )
		{
			actualAttributes.setProperty( "rowspan", String.valueOf( rowspan ) );
		}

		/*
		 * Generate row begin and set state to 'BEFORE_COLUMN'.
		 */
		_column = 0;
		_row++;
		_state = BEFORE_COLUMN;
		writeRowBegin( iw, actualAttributes );

		return iw;
	}

	/**
	 * Insert a 'separator' row in the table. If a column or row is being
	 * written, both are terminated. A new row is started (a new table is also
	 * started if needed) and a data column with a span matching the number of
	 * columns is written with a horizontal line tag. Finally, the create row is
	 * terminated.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter writeSeparator( @NotNull final JspWriter out )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		endRow( iw );
		newRow( iw );
		newColumn( iw, getColumnCount() );
		iw.print( "<hr />" );
		endRow( iw );
		return iw;
	}

	/**
	 * Start a new table. If a table was already started, an exception will be
	 * thrown. Note that calling this method is generally not required.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newTable( @NotNull final JspWriter out )
	throws IOException
	{
		return newTable( out, null );
	}

	/**
	 * Start a new table. If a table was already started, an exception will be
	 * thrown. Note that calling this method is generally not required unless
	 * special tag attributes need to be specified.
	 *
	 * @param out        JspWriter to use for output.
	 * @param attributes HTML element attributes.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newTable( @NotNull final JspWriter out, @Nullable final Properties attributes )
	throws IOException
	{
		return newTable( out, attributes, null, null );
	}

	/**
	 * Start a new table. If a table was already started, an exception will be
	 * thrown. Note that calling this method is generally not required unless
	 * special tag attributes need to be specified.
	 *
	 * @param out             JspWriter to use for output.
	 * @param tableAttributes Extra attributes for {@code &lt;table&gt;}
	 *                        element.
	 * @param theadAttributes Extra attributes for {@code &lt;thead&gt;}
	 *                        element.
	 * @param tbodyAttributes Extra attributes for {@code &lt;tbody&gt;}
	 *                        element.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter newTable( @NotNull final JspWriter out, @Nullable final Properties tableAttributes, @Nullable final Properties theadAttributes, @Nullable final Properties tbodyAttributes )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 0 );

		switch ( _state )
		{
			case BEFORE_TABLE: // nothing done, no <table> yet
				_state = BEFORE_ROW;
				_column = 0;
				_columnIsHead = false;
				_colspan = 1;
				writeTableBegin( iw, tableAttributes, theadAttributes, tbodyAttributes );
				return iw;

			case BEFORE_ROW: // </tr> done, no <tr>  yet
			case IN_COLUMN: // <td>  done, no </td> yet
			case BEFORE_COLUMN: // <tr>/</td>  done, no <td>  yet
			default:
				throw new IllegalStateException( String.valueOf( _state ) );
		}
	}

	/**
	 * This method is called to write the start of a new column. It basically
	 * just writes the 'TD' or 'TH' tag, but derivative classes may enhance this
	 * to create more layouts.
	 *
	 * @param iw          IndentingJspWriter to use for output.
	 * @param columnIndex Number of column being terminated.
	 * @param isHead      {@code true} for head columns (TH); {@code false} for
	 *                    data columns (TD).
	 * @param attributes  Properties for 'TD' or 'TH' tag.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeColumnBegin( @NotNull final IndentingJspWriter iw, final int columnIndex, final boolean isHead, @Nullable final Properties attributes )
	throws IOException
	{
		iw.print( isHead ? "<th" : "<td" );
		HTMLTools.writeAttributes( iw, attributes );
		iw.print( '>' );
		iw.indentIn();
	}

	/**
	 * This method is called to write the end of a column. It basically just
	 * writes the '/TD' or '/TH' tag, but derivative classes may enhance this to
	 * create more complex layouts.
	 *
	 * @param iw     IndentingJspWriter to use for output.
	 * @param isHead {@code true} for head columns (&lt;/TH&gt;); {@code false}
	 *               for data columns (&lt;/TD&gt;).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeColumnEnd( @NotNull final IndentingJspWriter iw, final boolean isHead )
	throws IOException
	{
		iw.indentOut();
		iw.println( isHead ? "</th>" : "</td>" );
	}

	/**
	 * This convenience method can be used to write an entire row of a table
	 * using a single method call. If the number of values is less the column
	 * count of the table, the last column's span is set to fill the rest of the
	 * row.
	 *
	 * @param out    JspWriter to use for output.
	 * @param values Values to write in each column.
	 *
	 * @return IndentingJspWriter actually used for output (possibly wrapped).
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	public IndentingJspWriter writeRow( @NotNull final JspWriter out, final String... values )
	throws IOException
	{
		final IndentingJspWriter iw = newRow( out );

		for ( int i = 0; i < values.length; i++ )
		{
			int colspan = 1;
			if ( ( i == ( values.length - 1 ) ) && ( i < ( _columnCount - 1 ) ) )
			{
				colspan = _columnCount - values.length + 1;
			}

			newColumn( iw, colspan );
			iw.print( values[ i ] );
		}

		return endRow( iw );
	}

	/**
	 * This method is called to write the start of a new row. It basically just
	 * writes the 'TR' tag, but derivative classes may enhance this to create
	 * more complex layouts.
	 *
	 * @param iw         IndentingJspWriter to use for output.
	 * @param attributes Extra attributes for {@code TR} element.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeRowBegin( @NotNull final IndentingJspWriter iw, @Nullable final Properties attributes )
	throws IOException
	{
		iw.print( "<tr" );
		HTMLTools.writeAttributes( iw, attributes );
		iw.println( ">" );
		iw.indentIn();
	}

	/**
	 * This method is called to write the end of a row. It basically just writes
	 * the '/TR' tag, but derivative classes may enhance this to create more
	 * complex layouts.
	 *
	 * @param iw IndentingJspWriter to use for output.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeRowEnd( @NotNull final IndentingJspWriter iw )
	throws IOException
	{
		iw.indentOut();
		iw.println( "</tr>" );
	}

	/**
	 * This method is called to write the start of a new table. It basically
	 * just writes the 'TABLE' tag and table/column titles (is defined), but
	 * derivative classes may enhance this to create more complex layouts.
	 *
	 * @param out             IndentingJspWriter to use for output.
	 * @param tableAttributes Extra attributes for {@code TABLE} element.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeTableBegin( @NotNull final IndentingJspWriter out, @Nullable final Properties tableAttributes )
	throws IOException
	{
		writeTableBegin( out, tableAttributes, null, null );
	}

	/**
	 * This method is called to write the start of a new table. It basically
	 * just writes the 'TABLE' tag and table/column titles (is defined), but
	 * derivative classes may enhance this to create more complex layouts.
	 *
	 * @param out             IndentingJspWriter to use for output.
	 * @param tableAttributes Extra attributes for {@code &lt;table&gt;}
	 *                        element.
	 * @param theadAttributes Extra attributes for {@code &lt;thead&gt;}
	 *                        element.
	 * @param tbodyAttributes Extra attributes for {@code &lt;tbody&gt;}
	 *                        element.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeTableBegin( @NotNull final IndentingJspWriter out, @Nullable final Properties tableAttributes, @Nullable final Properties theadAttributes, @Nullable final Properties tbodyAttributes )
	throws IOException
	{
		out.print( "<table" );
		HTMLTools.writeAttributes( out, tableAttributes );
		out.println( ">" );
		out.indentIn();

		final int columnCount = getColumnCount();
		if ( hasTableTitle() )
		{
			out.print( "<thead" );
			HTMLTools.writeAttributes( out, theadAttributes );
			out.println( ">" );
			out.indentIn();
			writeTableTitle( out, getTableTitle() );
			out.indentOut();
			out.println( "</thead>" );
		}
		else if ( hasColumnTitles() )
		{
			out.print( "<thead" );
			HTMLTools.writeAttributes( out, theadAttributes );
			out.println( ">" );
			out.indentIn();

			boolean twoRows = false;
			for ( int columnIndex = 0; columnIndex < columnCount; columnIndex++ )
			{
				final String name = getColumnTitle( columnIndex );
				if ( ( name != null ) && ( name.indexOf( '\0' ) >= 0 ) )
				{
					twoRows = true;
					break;
				}
			}

			if ( twoRows )
			{
				final List<Integer> secondRowColumns = new ArrayList<Integer>( columnCount );
				final List<HorizontalAlignment> secondRowAlignment = new ArrayList<HorizontalAlignment>( columnCount );
				final List<String> secondRowTexts = new ArrayList<String>( columnCount );
				final List<Properties> secondRowAttributes = new ArrayList<Properties>( columnCount );

				startColumnTitlesRow( out );

				int columnIndex = 0;
				while ( columnIndex < columnCount )
				{
					final String columnTitle = getColumnTitle( columnIndex );

					final int thColspan;
					final HorizontalAlignment thAlign;
					final Properties thAttributes;
					final String thText;

					final int separatorPos = ( columnTitle != null ) ? columnTitle.indexOf( '\0' ) : -1;
					if ( separatorPos < 0 )
					{
						thColspan = ( columnIndex == columnCount - 1 ) ? columnCount - columnIndex : 1;
						thText = columnTitle;
						thAlign = getColumnAlignment( columnIndex );
						thAttributes = PropertyTools.create( "rowspan", "2", "class", "twoRows singular" );
					}
					else
					{
						final String rowText = columnTitle.substring( 0, separatorPos );
						final Matcher colspanMatcher = COLSPAN_PATTERN.matcher( rowText );
						if ( colspanMatcher.matches() )
						{
							thText = colspanMatcher.group( 1 );
							thColspan = Integer.valueOf( colspanMatcher.group( 2 ) );
						}
						else
						{
							thText = rowText;
							thColspan = ( columnIndex == columnCount - 1 ) ? columnCount - columnIndex : 1;
						}

						thAlign = ( thColspan > 1 ) ? HorizontalAlignment.LEFT : getColumnAlignment( columnIndex );
						thAttributes = PropertyTools.create( "class", "twoRows top" );

						for ( int j = 0; j < thColspan; j++ )
						{
							secondRowColumns.add( Integer.valueOf( columnIndex + j ) );
							secondRowAlignment.add( getColumnAlignment( columnIndex + j ) );
							secondRowTexts.add( ( j == 0 ) ? columnTitle.substring( separatorPos + 1 ) : getColumnTitle( columnIndex + j ) );
							secondRowAttributes.add( PropertyTools.create( "class", ( thColspan == 1 ) ? "twoRows bottom only first last" : ( j == 0 ) ? "twoRows bottom first" : ( j == thColspan - 1 ) ? "twoRows bottom last" : "twoRows bottom" ) );
						}
					}

					_column = columnIndex;
					writeTableColumnHeader( out, thColspan, thAlign, thAttributes, thText );
					columnIndex += thColspan;
				}

				endColumnTitlesRow( out );

				if ( !secondRowTexts.isEmpty() )
				{
					startColumnTitlesRow( out );

					for ( int i = 0; i < secondRowTexts.size(); i++ )
					{
						_column = secondRowColumns.get( i );
						writeTableColumnHeader( out, 1, secondRowAlignment.get( i ), secondRowAttributes.get( i ), secondRowTexts.get( i ) );
					}

					endColumnTitlesRow( out );
				}
			}
			else
			{
				startColumnTitlesRow( out );

				for ( int columnIndex = 0; columnIndex < columnCount; columnIndex++ )
				{
					final int colspan = ( columnIndex == columnCount - 1 ) ? columnCount - columnIndex : 1;
					final String columnText = getColumnTitle( columnIndex );
					_column = columnIndex;
					writeTableColumnHeader( out, colspan, getColumnAlignment( columnIndex ), null, columnText );
				}

				endColumnTitlesRow( out );
			}

			out.indentOut();
			out.println( "</thead>" );
		}
	}

	/**
	 * Write table title.
	 *
	 * @param out        JspWriter to use for output.
	 * @param tableTitle Table title to write.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeTableTitle( @NotNull final IndentingJspWriter out, @NotNull final String tableTitle )
	throws IOException
	{
		newRow( out );
		newHead( out, getColumnCount(), HorizontalAlignment.CENTER, null );
		out.print( getTableTitle() );
		endColumn( out );
		endRow( out );
	}

	/**
	 * Start table header row.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void startColumnTitlesRow( @NotNull final IndentingJspWriter out )
	throws IOException
	{
		newRow( out );
	}

	/**
	 * Writabe table column header.
	 *
	 * @param out        JspWriter to use for output.
	 * @param colspan    Column span to use.
	 * @param align      Horizontal alignment.
	 * @param attributes HTML element attributes.
	 * @param text       Table column header text.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeTableColumnHeader( @NotNull final IndentingJspWriter out, final int colspan, @NotNull final HorizontalAlignment align, @Nullable final Properties attributes, @Nullable final String text )
	throws IOException
	{
		newHead( out, colspan, align, attributes );
		out.print( TextTools.isEmpty( text ) ? "&nbsp" : text );
		endColumn( out );
	}

	/**
	 * End table header row.
	 *
	 * @param out JspWriter to use for output.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void endColumnTitlesRow( final IndentingJspWriter out )
	throws IOException
	{
		endRow( out );
	}

	/**
	 * This method is called to write the end of a table. It basically just
	 * writes the '/TABLE', but derivative classes may enhance this to create
	 * more complex layouts.
	 *
	 * @param iw IndentingJspWriter to use for output.
	 *
	 * @throws IOException if an error occurred while writing content.
	 */
	protected void writeTableEnd( @NotNull final IndentingJspWriter iw )
	throws IOException
	{
		iw.indentOut();
		iw.println( "</table>" );
	}
}
