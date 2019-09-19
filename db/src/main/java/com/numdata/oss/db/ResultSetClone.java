/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.db;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Implements {@code ResultSet} interface based on the contents of an existing
 * {@code ResultSet} instance. All data within that instance is copied, so the
 * old instance can be discarded.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "ReturnOfCollectionOrArrayField", "WeakerAccess", "FinalClass", "override" } )
public final class ResultSetClone
implements ResultSet
{
	/**
	 * Exception message used when trying to access data after the {@code
	 * close()} method was called.
	 *
	 * @see #close
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
	public static final String RESULT_CLOSED = "RESULTSET_CLOSED";

	/**
	 * Exception message used when trying to access data before the first row.
	 *
	 * @see #getObject
	 * @see #updateObject
	 */
	public static final String CURSOR_BEFORE_FIRST = "CURSOR_BEFORE_FIRST";

	/**
	 * Exception message used when trying to access data after the last row.
	 *
	 * @see #getObject
	 * @see #updateObject
	 */
	public static final String CURSOR_AFTER_LAST = "CURSOR_AFTER_LAST";

	/**
	 * Exception message used when a non-existent column name was specified. May
	 * occur for every method taking a column name argument.
	 *
	 * @see #findColumn
	 */
	public static final String NO_SUCH_COLUMN = "NO_SUCH_COLUMN";

	/**
	 * Exception message used when an invalid column index was specified. A
	 * typical example would be specifying column index 0 (first column is 1).
	 *
	 * @see #getObject
	 * @see #updateObject
	 */
	public static final String INVALID_COLUMN_INDEX = "INVALID_COLUMN_INDEX";

	/**
	 * Exception message used when trying to call {@link #unwrap}.
	 */
	private static final String NOT_A_WRAPPER = "not a wrapper";

	/**
	 * Exception message used when trying to write data on a non-writable
	 * result.
	 *
	 * @see #updateObject
	 */
	public static final String NOT_WRITABLE = "NOT_WRITABLE";

	/**
	 * Exception message used when an unsupported JDBC feature is used.
	 */
	public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

	/**
	 * Exception message used when an invalid fetch direction was specified.
	 *
	 * @see #setFetchDirection
	 */
	public static final String INVALID_FETCH_DIRECTION = "INVALID_FETCH_DIRECTION";

	/**
	 * Exception message used when data was requested in a specific format for
	 * which no internal translation was defined to convert the internal data
	 * representation.
	 */
	public static final String INCOMPATIBLE_TYPE = "INCOMPATIBLE_TYPE";

	/**
	 * Result set data being traversed.
	 */
	private final Object[][] _data;

	/**
	 * Cloned meta-data for this result set.
	 */
	private final ResultSetMetaData _metaData;

	/**
	 * Row index in the result set. The following table should explain this
	 * value (there is obviously not a first nor a last row when the result set
	 * is empty): <table> <th><td>row index</td><td>description</td></th>
	 * <tr><td>-1</td><td>before first</td></tr> <tr><td>0</td><td>first
	 * row</td></tr> <tr><td>#rows - 1</td><td>last row</td></tr>
	 * <tr><td>#rows</td><td>after last</td></tr> </table>
	 */
	private int _rowIndex;

	/**
	 * Flag to indicate that {@link #close} was called. Any access to the result
	 * set data is denied when this is set.
	 *
	 * @see #close
	 */
	private boolean _isClosed;

	/**
	 * Fetch direction for this {@link ResultSet} object.
	 */
	private int _fetchDirection;

	/**
	 * Flag to indicate whether the last column read had a value of SQL {@code
	 * null}.
	 *
	 * @see #getObject
	 */
	private boolean _wasNull;

	/**
	 * Constructs a cloned result set.
	 *
	 * @param source Result set to clone.
	 *
	 * @throws SQLException when a problem occurred while cloning the source.
	 */
	public ResultSetClone( final ResultSet source )
	throws SQLException
	{
		_metaData = new ResultSetMetaDataClone( source.getMetaData() );

		final int columnCount = _metaData.getColumnCount();

		final Collection<Object[]> data = new ArrayList<Object[]>( source.getFetchSize() );
		while ( source.next() )
		{
			final Object[] row = new Object[ columnCount ];
			for ( int i = 0; i < columnCount; i++ )
			{
				row[ i ] = source.getObject( i + 1 );
			}

			data.add( row );
		}

		_data = new Object[ data.size() ][];
		data.toArray( _data );

		_rowIndex = -1;
		_isClosed = false;
		_fetchDirection = ResultSet.FETCH_UNKNOWN;
		_wasNull = false;
	}

	@Override
	public Statement getStatement()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public void close()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		_isClosed = true;
	}

	@Override
	public String getCursorName()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public int getType()
	{
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}

	@Override
	public int getConcurrency()
	{
		return ResultSet.CONCUR_READ_ONLY;
	}

	/**
	 * Get all cloned result set data. This is not part of the JDBC
	 * specification.
	 *
	 * @return Cloned result set data.
	 */
	public Object[][] getData()
	{
		return _data;
	}

	@Override
	public ResultSetMetaData getMetaData()
	{
		return _metaData;
	}

	@Override
	public int findColumn( final String columnName )
	throws SQLException
	{
		if ( columnName == null )
		{
			throw new SQLException( NO_SUCH_COLUMN + " null" );
		}

		for ( int i = 1; i <= _metaData.getColumnCount(); i++ )
		{
			if ( columnName.equalsIgnoreCase( _metaData.getColumnLabel( i ) ) ||
			     columnName.equalsIgnoreCase( _metaData.getColumnName( i ) ) )
			{
				return i;
			}
		}

		throw new SQLException( NO_SUCH_COLUMN + " '" + columnName + '\'' );
	}

	@Override
	public int getFetchDirection()
	{
		return _fetchDirection;
	}

	@Override
	public void setFetchDirection( final int direction )
	throws SQLException
	{
		if ( direction != ResultSet.FETCH_FORWARD
		     && direction != ResultSet.FETCH_REVERSE
		     && direction != ResultSet.FETCH_UNKNOWN )
		{
			throw new SQLException( INVALID_FETCH_DIRECTION + " - " + direction );
		}

		_fetchDirection = direction;
	}

	@Override
	public int getFetchSize()
	{
		return _data.length;
	}

	@Override
	public void setFetchSize( final int rows )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Nullable
	@Override
	public SQLWarning getWarnings()
	{
		return null;
	}

	@Override
	public void clearWarnings()
	{
	}

	@Override
	public int getHoldability()
	{
		return HOLD_CURSORS_OVER_COMMIT;
	}

	@Override
	public boolean isClosed()
	{
		return _isClosed;
	}

	@Override
	public boolean isWrapperFor( final Class<?> iface )
	{
		return false;
	}

	@Override
	public <T> T unwrap( final Class<T> iface )
	throws SQLException
	{
		throw new SQLException( NOT_A_WRAPPER );
	}

	/* ********************************************************************* */

	@Override
	public boolean next()
	throws SQLException
	{
		return relative( 1 );
	}

	@Override
	public boolean previous()
	throws SQLException
	{
		return relative( -1 );
	}

	@Override
	public boolean isBeforeFirst()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		return ( _rowIndex < 0 );
	}

	@Override
	public boolean isAfterLast()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		return ( _rowIndex >= _data.length );
	}

	@Override
	public boolean isFirst()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		return ( _rowIndex == 0 ) && ( _data.length > 0 );
	}

	@Override
	public boolean isLast()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		return ( _rowIndex >= 0 ) && ( _rowIndex == _data.length - 1 );
	}

	@Override
	public void beforeFirst()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		_rowIndex = -1;
	}

	@Override
	public void afterLast()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		_rowIndex = _data.length;
	}

	@Override
	public boolean first()
	throws SQLException
	{
		return absolute( 1 );
	}

	@Override
	public boolean last()
	throws SQLException
	{
		return absolute( -1 );
	}

	@Override
	public int getRow()
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		final int index = _rowIndex;
		return ( ( index < 0 ) || ( index >= _data.length ) ) ? 0 : index + 1;

	}

	@Override
	public boolean absolute( final int row )
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		final int nrRows = _data.length;

		final int rowIndex = ( row < 0 ) ? Math.max( nrRows + row, -1 )
		                                 : Math.min( nrRows, row - 1 );
		_rowIndex = rowIndex;

		return ( ( rowIndex >= 0 ) && ( rowIndex < nrRows ) );
	}

	@Override
	public boolean relative( final int rows )
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		final int nrRows = _data.length;

		final int rowIndex = Math.min( Math.max( -1, _rowIndex + rows ), nrRows );
		_rowIndex = rowIndex;

		return ( ( rowIndex >= 0 ) && ( rowIndex < nrRows ) );
	}

	/* ********************************************************************* */

	@Override
	public void insertRow()
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public boolean rowInserted()
	{
		return false;
	}

	@Override
	public void moveToInsertRow()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public void moveToCurrentRow()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public void updateRow()
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public boolean rowUpdated()
	{
		return false;
	}

	@Override
	public void cancelRowUpdates()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public void deleteRow()
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public boolean rowDeleted()
	{
		return false;
	}

	@Override
	public void refreshRow()
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	/* ********************************************************************* */

	@Override
	public Array getArray( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Array getArray( final String columnName )
	throws SQLException
	{
		return getArray( findColumn( columnName ) );
	}

	@Override
	public InputStream getAsciiStream( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public InputStream getAsciiStream( final String columnName )
	throws SQLException
	{
		return getAsciiStream( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public BigDecimal getBigDecimal( final int columnIndex )
	throws SQLException
	{
		final BigDecimal result;
		final Object object = getObject( columnIndex );
		if ( object == null )
		{
			result = null;
		}
		else if ( object instanceof Number )
		{
			result = BigDecimalTools.toBigDecimal( (Number)object );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + object.getClass() );
		}
		return result;
	}

	@Nullable
	@Override
	public BigDecimal getBigDecimal( final int columnIndex, final int scale )
	throws SQLException
	{
		final BigDecimal value = getBigDecimal( columnIndex );
		return value == null ? null : BigDecimalTools.limitScale( value, scale, scale );
	}

	@Nullable
	@Override
	public BigDecimal getBigDecimal( final String columnName )
	throws SQLException
	{
		return getBigDecimal( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public BigDecimal getBigDecimal( final String columnName, final int scale )
	throws SQLException
	{
		return getBigDecimal( findColumn( columnName ), scale );
	}

	@Nullable
	@Override
	public InputStream getBinaryStream( final int columnIndex )
	throws SQLException
	{
		final InputStream result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof byte[] )
		{
			result = new ByteArrayInputStream( (byte[])obj );
		}
		else if ( obj instanceof String )
		{
			result = new ByteArrayInputStream( ( (String)obj ).getBytes() );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public InputStream getBinaryStream( final String columnName )
	throws SQLException
	{
		return getBinaryStream( findColumn( columnName ) );
	}

	@Override
	public Blob getBlob( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Blob getBlob( final String columnName )
	throws SQLException
	{
		return getBlob( findColumn( columnName ) );
	}

	@Override
	public boolean getBoolean( final int columnIndex )
	throws SQLException
	{
		final boolean result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = false;
		}
		else if ( obj instanceof Boolean )
		{
			result = (Boolean)obj;
		}
		else if ( obj instanceof Number )
		{
			result = ( ( (Number)obj ).intValue() > 0 );
		}
		else if ( obj instanceof String )
		{
			result = Boolean.valueOf( (String)obj );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public boolean getBoolean( final String columnName )
	throws SQLException
	{
		return getBoolean( findColumn( columnName ) );
	}

	@Override
	public byte getByte( final int columnIndex )
	throws SQLException
	{
		final byte result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = (byte)0;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).byteValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public byte getByte( final String columnName )
	throws SQLException
	{
		return getByte( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public byte[] getBytes( final int columnIndex )
	throws SQLException
	{
		final byte[] result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof byte[] )
		{
			result = (byte[])obj;
		}
		else if ( obj instanceof String )
		{
			result = ( (String)obj ).getBytes();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public byte[] getBytes( final String columnName )
	throws SQLException
	{
		return getBytes( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public Reader getCharacterStream( final int columnIndex )
	throws SQLException
	{
		final Reader result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof char[] )
		{
			result = new StringReader( new String( (char[])obj ) );
		}
		else if ( obj instanceof String )
		{
			result = new StringReader( (String)obj );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public Reader getCharacterStream( final String columnName )
	throws SQLException
	{
		return getCharacterStream( findColumn( columnName ) );
	}

	@Override
	public Clob getClob( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Clob getClob( final String columnName )
	throws SQLException
	{
		return getClob( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public Date getDate( final int columnIndex )
	throws SQLException
	{
		final Date result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof Date )
		{
			result = (Date)obj;
		}
		else if ( obj instanceof java.util.Date )
		{
			result = new Date( ( (java.util.Date)obj ).getTime() );
		}
		else if ( obj instanceof Number )
		{
			result = new Date( ( (Number)obj ).longValue() );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public Date getDate( final String columnName )
	throws SQLException
	{
		return getDate( findColumn( columnName ) );
	}

	@Override
	public Date getDate( final int columnIndex, final Calendar cal )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Date getDate( final String columnName, final Calendar cal )
	throws SQLException
	{
		return getDate( findColumn( columnName ), cal );
	}

	@Override
	public double getDouble( final int columnIndex )
	throws SQLException
	{
		final double result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = 0.0;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).doubleValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public double getDouble( final String columnName )
	throws SQLException
	{
		return getDouble( findColumn( columnName ) );
	}

	@Override
	public float getFloat( final int columnIndex )
	throws SQLException
	{
		final float result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = 0.0f;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).floatValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public float getFloat( final String columnName )
	throws SQLException
	{
		return getFloat( findColumn( columnName ) );
	}

	@Override
	public int getInt( final int columnIndex )
	throws SQLException
	{
		final int result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = 0;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).intValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public int getInt( final String columnName )
	throws SQLException
	{
		return getInt( findColumn( columnName ) );
	}

	@Override
	public long getLong( final int columnIndex )
	throws SQLException
	{
		final long result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = 0L;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).longValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public long getLong( final String columnName )
	throws SQLException
	{
		return getLong( findColumn( columnName ) );
	}

	@Override
	public Reader getNCharacterStream( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Reader getNCharacterStream( final String columnName )
	throws SQLException
	{
		return getNCharacterStream( findColumn( columnName ) );
	}

	@Override
	public NClob getNClob( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public NClob getNClob( final String columnName )
	throws SQLException
	{
		return getNClob( findColumn( columnName ) );
	}

	@Override
	public String getNString( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public String getNString( final String columnName )
	throws SQLException
	{
		return getNString( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public Object getObject( final int columnIndex )
	throws SQLException
	{
		if ( _isClosed )
		{
			throw new SQLException( RESULT_CLOSED );
		}

		if ( isBeforeFirst() )
		{
			throw new SQLException( CURSOR_BEFORE_FIRST );
		}

		if ( isAfterLast() )
		{
			throw new SQLException( CURSOR_AFTER_LAST );
		}

		final Object[] row = _data[ _rowIndex ];

		if ( ( columnIndex < 1 ) || ( columnIndex > row.length ) )
		{
			throw new SQLException( INVALID_COLUMN_INDEX );
		}

		final Object result = _data[ _rowIndex ][ columnIndex - 1 ];
		_wasNull = ( result == null );
		return result;
	}

	public <T> T getObject( final int columnIndex, final Class<T> type )
	throws SQLException
	{
		return type.cast( getObject( columnIndex ) );
	}

	@Override
	public Object getObject( final int columnIndex, final Map<String, Class<?>> map )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Nullable
	@Override
	public Object getObject( final String columnName )
	throws SQLException
	{
		return getObject( findColumn( columnName ) );
	}

	public <T> T getObject( final String columnName, final Class<T> type )
	throws SQLException
	{
		return getObject( findColumn( columnName ), type );
	}

	@Override
	public Object getObject( final String columnName, final Map<String, Class<?>> map )
	throws SQLException
	{
		return getObject( findColumn( columnName ), map );
	}

	@Override
	public Ref getRef( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Ref getRef( final String columnName )
	throws SQLException
	{
		return getRef( findColumn( columnName ) );
	}

	@Override
	public RowId getRowId( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public RowId getRowId( final String columnName )
	throws SQLException
	{
		return getRowId( findColumn( columnName ) );
	}

	@Override
	public short getShort( final int columnIndex )
	throws SQLException
	{
		final short result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = (short)0;
		}
		else if ( obj instanceof Number )
		{
			result = ( (Number)obj ).shortValue();
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Override
	public short getShort( final String columnName )
	throws SQLException
	{
		return getShort( findColumn( columnName ) );
	}

	@Override
	public SQLXML getSQLXML( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public SQLXML getSQLXML( final String columnName )
	throws SQLException
	{
		return getSQLXML( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public String getString( final int columnIndex )
	throws SQLException
	{
		final Object obj = getObject( columnIndex );
		return ( obj != null ) ? String.valueOf( obj ) : null;
	}

	@Nullable
	@Override
	public String getString( final String columnName )
	throws SQLException
	{
		return getString( findColumn( columnName ) );
	}

	@Nullable
	@Override
	public Time getTime( final int columnIndex )
	throws SQLException
	{
		final Time result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof Time )
		{
			result = (Time)obj;
		}
		else if ( obj instanceof java.util.Date )
		{
			result = new Time( ( (java.util.Date)obj ).getTime() );
		}
		else if ( obj instanceof Number )
		{
			result = new Time( ( (Number)obj ).longValue() );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public Time getTime( final String columnName )
	throws SQLException
	{
		return getTime( findColumn( columnName ) );
	}

	@Override
	public Time getTime( final int columnIndex, final Calendar cal )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Time getTime( final String columnName, final Calendar cal )
	throws SQLException
	{
		return getTime( findColumn( columnName ), cal );
	}

	@Nullable
	@Override
	public Timestamp getTimestamp( final int columnIndex )
	throws SQLException
	{
		final Timestamp result;

		final Object obj = getObject( columnIndex );
		if ( obj == null )
		{
			result = null;
		}
		else if ( obj instanceof Timestamp )
		{
			result = (Timestamp)obj;
		}
		else if ( obj instanceof java.util.Date )
		{
			result = new Timestamp( ( (java.util.Date)obj ).getTime() );
		}
		else if ( obj instanceof Number )
		{
			result = new Timestamp( ( (Number)obj ).longValue() );
		}
		else
		{
			throw new SQLException( INCOMPATIBLE_TYPE + " - " + obj.getClass() );
		}

		return result;
	}

	@Nullable
	@Override
	public Timestamp getTimestamp( final String columnName )
	throws SQLException
	{
		return getTimestamp( findColumn( columnName ) );
	}

	@Override
	public Timestamp getTimestamp( final int columnIndex, final Calendar cal )

	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public Timestamp getTimestamp( final String columnName, final Calendar cal )

	throws SQLException
	{
		return getTimestamp( findColumn( columnName ), cal );
	}

	@Override
	public InputStream getUnicodeStream( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public InputStream getUnicodeStream( final String columnName )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public URL getURL( final int columnIndex )
	throws SQLException
	{
		throw new SQLException( NOT_IMPLEMENTED );
	}

	@Override
	public URL getURL( final String columnName )
	throws SQLException
	{
		return getURL( findColumn( columnName ) );
	}

	@Override
	public void updateArray( final int columnIndex, final Array x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateArray( final String columnName, final Array x )
	throws SQLException
	{
		updateArray( findColumn( columnName ), x );
	}

	@Override
	public void updateAsciiStream( final int columnIndex, final InputStream is )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateAsciiStream( final int columnIndex, final InputStream is, final int length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateAsciiStream( final int columnIndex, final InputStream is, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateAsciiStream( final String columnName, final InputStream is )
	throws SQLException
	{
		updateAsciiStream( findColumn( columnName ), is );
	}

	@Override
	public void updateAsciiStream( final String columnName, final InputStream is, final int length )
	throws SQLException
	{
		updateAsciiStream( findColumn( columnName ), is, length );
	}

	@Override
	public void updateAsciiStream( final String columnName, final InputStream is, final long length )
	throws SQLException
	{
		updateAsciiStream( findColumn( columnName ), is, length );
	}

	@Override
	public void updateBigDecimal( final int columnIndex, final BigDecimal x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBigDecimal( final String columnName, final BigDecimal x )
	throws SQLException
	{
		updateBigDecimal( findColumn( columnName ), x );
	}

	@Override
	public void updateBinaryStream( final int columnIndex, final InputStream is )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBinaryStream( final int columnIndex, final InputStream is, final int length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBinaryStream( final int columnIndex, final InputStream is, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBinaryStream( final String columnName, final InputStream is )
	throws SQLException
	{
		updateBinaryStream( findColumn( columnName ), is );
	}

	@Override
	public void updateBinaryStream( final String columnName, final InputStream is, final int length )
	throws SQLException
	{
		updateBinaryStream( findColumn( columnName ), is, length );
	}

	@Override
	public void updateBinaryStream( final String columnName, final InputStream is, final long length )
	throws SQLException
	{
		updateBinaryStream( findColumn( columnName ), is, length );
	}

	@Override
	public void updateBlob( final int columnIndex, final Blob x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBlob( final int columnIndex, final InputStream is )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBlob( final int columnIndex, final InputStream is, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBlob( final String columnName, final Blob x )
	throws SQLException
	{
		updateBlob( findColumn( columnName ), x );
	}

	@Override
	public void updateBlob( final String columnName, final InputStream is )
	throws SQLException
	{
		updateBlob( findColumn( columnName ), is );
	}

	@Override
	public void updateBlob( final String columnName, final InputStream is, final long length )
	throws SQLException
	{
		updateBlob( findColumn( columnName ), is, length );
	}

	@Override
	public void updateBoolean( final int columnIndex, final boolean x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBoolean( final String columnName, final boolean x )
	throws SQLException
	{
		updateBoolean( findColumn( columnName ), x );
	}

	@Override
	public void updateByte( final int columnIndex, final byte x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateByte( final String columnName, final byte x )
	throws SQLException
	{
		updateByte( findColumn( columnName ), x );
	}

	@Override
	public void updateBytes( final int columnIndex, final byte[] x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateBytes( final String columnName, final byte[] x )
	throws SQLException
	{
		updateBytes( findColumn( columnName ), x );
	}

	@Override
	public void updateCharacterStream( final int columnIndex, final Reader reader )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateCharacterStream( final int columnIndex, final Reader reader, final int length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateCharacterStream( final int columnIndex, final Reader reader, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateCharacterStream( final String columnName, final Reader reader )
	throws SQLException
	{
		updateCharacterStream( findColumn( columnName ), reader );
	}

	@Override
	public void updateCharacterStream( final String columnName, final Reader reader, final int length )
	throws SQLException
	{
		updateCharacterStream( findColumn( columnName ), reader, length );
	}

	@Override
	public void updateCharacterStream( final String columnName, final Reader reader, final long length )
	throws SQLException
	{
		updateCharacterStream( findColumn( columnName ), reader, length );
	}

	@Override
	public void updateClob( final int columnIndex, final Clob x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateClob( final int columnIndex, final Reader reader )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateClob( final int columnIndex, final Reader reader, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateClob( final String columnName, final Clob x )
	throws SQLException
	{
		updateClob( findColumn( columnName ), x );
	}

	@Override
	public void updateClob( final String columnName, final Reader reader )
	throws SQLException
	{
		updateClob( findColumn( columnName ), reader );
	}

	@Override
	public void updateClob( final String columnName, final Reader reader, final long length )
	throws SQLException
	{
		updateClob( findColumn( columnName ), reader, length );
	}

	@Override
	public void updateDate( final int columnIndex, final Date x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateDate( final String columnName, final Date x )
	throws SQLException
	{
		updateDate( findColumn( columnName ), x );
	}

	@Override
	public void updateDouble( final int columnIndex, final double x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateDouble( final String columnName, final double x )
	throws SQLException
	{
		updateDouble( findColumn( columnName ), x );
	}

	@Override
	public void updateFloat( final int columnIndex, final float x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateFloat( final String columnName, final float x )
	throws SQLException
	{
		updateFloat( findColumn( columnName ), x );
	}

	@Override
	public void updateInt( final int columnIndex, final int x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateInt( final String columnName, final int x )
	throws SQLException
	{
		updateInt( findColumn( columnName ), x );
	}

	@Override
	public void updateLong( final int columnIndex, final long x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateLong( final String columnName, final long x )
	throws SQLException
	{
		updateLong( findColumn( columnName ), x );
	}

	@Override
	public void updateNCharacterStream( final int columnIndex, final Reader reader )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNCharacterStream( final int columnIndex, final Reader reader, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNCharacterStream( final String columnName, final Reader reader )
	throws SQLException
	{
		updateNCharacterStream( findColumn( columnName ), reader );
	}

	@Override
	public void updateNCharacterStream( final String columnName, final Reader reader, final long length )
	throws SQLException
	{
		updateNCharacterStream( findColumn( columnName ), reader, length );
	}

	@Override
	public void updateNClob( final int columnIndex, final NClob nClob )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNClob( final String columnName, final NClob nClob )
	throws SQLException
	{
		updateNClob( findColumn( columnName ), nClob );
	}

	@Override
	public void updateNClob( final int columnIndex, final Reader reader )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNClob( final int columnIndex, final Reader reader, final long length )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNClob( final String columnName, final Reader reader )
	throws SQLException
	{
		updateNClob( findColumn( columnName ), reader );
	}

	@Override
	public void updateNClob( final String columnName, final Reader reader, final long length )
	throws SQLException
	{
		updateNClob( findColumn( columnName ), reader, length );
	}

	@Override
	public void updateNString( final int columnIndex, final String nString )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateNString( final String columnName, final String nString )
	throws SQLException
	{
		updateNString( findColumn( columnName ), nString );
	}

	@Override
	public void updateNull( final int columnIndex )
	throws SQLException
	{
		updateObject( columnIndex, null );
	}

	@Override
	public void updateNull( final String columnName )
	throws SQLException
	{
		updateNull( findColumn( columnName ) );
	}

	@Override
	public void updateObject( final int columnIndex, final Object obj, final int scale )

	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateObject( final int columnIndex, @Nullable final Object x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateObject( final String columnName, final Object obj, final int scale )

	throws SQLException
	{
		updateObject( findColumn( columnName ), obj, scale );
	}

	@Override
	public void updateObject( final String columnName, final Object x )
	throws SQLException
	{
		updateObject( findColumn( columnName ), x );
	}

	@Override
	public void updateRef( final int columnIndex, final Ref x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateRef( final String columnName, final Ref x )
	throws SQLException
	{
		updateRef( findColumn( columnName ), x );
	}

	@Override
	public void updateRowId( final int columnIndex, final RowId x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateRowId( final String columnName, final RowId x )
	throws SQLException
	{
		updateRowId( findColumn( columnName ), x );
	}

	@Override
	public void updateShort( final int columnIndex, final short x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateShort( final String columnName, final short x )
	throws SQLException
	{
		updateShort( findColumn( columnName ), x );
	}

	@Override
	public void updateSQLXML( final int columnIndex, final SQLXML x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateSQLXML( final String columnName, final SQLXML x )
	throws SQLException
	{
		updateSQLXML( findColumn( columnName ), x );
	}

	@Override
	public void updateString( final int columnIndex, final String x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateString( final String columnName, final String x )
	throws SQLException
	{
		updateString( findColumn( columnName ), x );
	}

	@Override
	public void updateTime( final int columnIndex, final Time x )
	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateTime( final String columnName, final Time x )
	throws SQLException
	{
		updateTime( findColumn( columnName ), x );
	}

	@Override
	public void updateTimestamp( final int columnIndex, final Timestamp x )

	throws SQLException
	{
		throw new SQLException( NOT_WRITABLE );
	}

	@Override
	public void updateTimestamp( final String columnName, final Timestamp x )
	throws SQLException
	{
		updateTimestamp( findColumn( columnName ), x );
	}

	@Override
	public boolean wasNull()
	{
		return _wasNull;
	}

	/**
	 * Get result set as nicely formatted text table.
	 *
	 * @return User-friendly string representation of result set.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public String toFriendlyString()
	throws SQLException
	{
		final ResultSetMetaData metaData = getMetaData();

		final int columnCount = metaData.getColumnCount();

		final List<String> columnHeaders = new ArrayList<String>( columnCount );
		for ( int i = 1; i <= columnCount; i++ )
		{
			columnHeaders.add( metaData.getColumnLabel( i ) + ':' + metaData.getColumnTypeName( i ) );
		}

		final List<List<String>> tableData = new ArrayList<List<String>>();
		while ( next() )
		{
			final List<String> rowData = new ArrayList<String>( columnCount );
			for ( int i = 1; i <= columnCount; i++ )
			{
				rowData.add( getString( i ) );
			}

			tableData.add( rowData );
		}

		return TextTools.getTableAsText( columnHeaders, tableData );
	}
}
