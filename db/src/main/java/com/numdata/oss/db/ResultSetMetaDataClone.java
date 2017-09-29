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
package com.numdata.oss.db;

import java.sql.*;

/**
 * Implements {@code ResultSetMetaData} interface based on the contents of an
 * existing {@code ResultSetMetaData} instance. All data within that instance is
 * copied, so the old instance can be discarded.
 *
 * @author Peter S. Heijnen
 */
public class ResultSetMetaDataClone
implements ResultSetMetaData
{
	/** Cloned data. */
	private final int _columnCount;

	/** Cloned data. */
	private final boolean[] _isAutoIncrement;

	/** Cloned data. */
	private final boolean[] _isCaseSensitive;

	/** Cloned data. */
	private final boolean[] _isSearchable;

	/** Cloned data. */
	private final boolean[] _isCurrency;

	/** Cloned data. */
	private final int[] _isNullable;

	/** Cloned data. */
	private final boolean[] _isSigned;

	/** Cloned data. */
	private final int[] _columnDisplaySize;

	/** Cloned data. */
	private final String[] _columnLabel;

	/** Cloned data. */
	private final String[] _columnName;

	/** Cloned data. */
	private final String[] _sSchemaName;

	/** Cloned data. */
	private final int[] _precision;

	/** Cloned data. */
	private final int[] _scale;

	/** Cloned data. */
	private final String[] _tableName;

	/** Cloned data. */
	private final String[] _catalogName;

	/** Cloned data. */
	private final int[] _columnType;

	/** Cloned data. */
	private final String[] _columnTypeName;

	/** Cloned data. */
	private final boolean[] _isReadOnly;

	/** Cloned data. */
	private final boolean[] _isWritable;

	/** Cloned data. */
	private final boolean[] _isDefinitelyWritable;

	/** Cloned data. */
	private final String[] _columnClassName;

	/**
	 * Construct result set meta data clone object.
	 *
	 * @param source Meta data to clone.
	 *
	 * @throws SQLException if the {@code source} object produced one.
	 */
	public ResultSetMetaDataClone( final ResultSetMetaData source )
	throws SQLException
	{
		final int columnCount = source.getColumnCount();

		_columnCount = columnCount;
		_isAutoIncrement = new boolean[ columnCount ];
		_isCaseSensitive = new boolean[ columnCount ];
		_isSearchable = new boolean[ columnCount ];
		_isCurrency = new boolean[ columnCount ];
		_isNullable = new int[ columnCount ];
		_isSigned = new boolean[ columnCount ];
		_columnDisplaySize = new int[ columnCount ];
		_columnLabel = new String[ columnCount ];
		_columnName = new String[ columnCount ];
		_sSchemaName = new String[ columnCount ];
		_precision = new int[ columnCount ];
		_scale = new int[ columnCount ];
		_tableName = new String[ columnCount ];
		_catalogName = new String[ columnCount ];
		_columnType = new int[ columnCount ];
		_columnTypeName = new String[ columnCount ];
		_isReadOnly = new boolean[ columnCount ];
		_isWritable = new boolean[ columnCount ];
		_isDefinitelyWritable = new boolean[ columnCount ];
		_columnClassName = new String[ columnCount ];

		for ( int i = 0; i < columnCount; i++ )
		{
			_isAutoIncrement[ i ] = source.isAutoIncrement( i + 1 );
			try
			{
				_isCaseSensitive[ i ] = source.isCaseSensitive( i + 1 );
			}
			catch ( final SQLException ignored )
			{
				/* Thrown on certain server/driver combinations. Ignore it. */
			}
			_isSearchable[ i ] = source.isSearchable( i + 1 );
			_isCurrency[ i ] = source.isCurrency( i + 1 );
			_isNullable[ i ] = source.isNullable( i + 1 );
			_isSigned[ i ] = source.isSigned( i + 1 );
			_columnDisplaySize[ i ] = source.getColumnDisplaySize( i + 1 );
			_columnLabel[ i ] = source.getColumnLabel( i + 1 );
			_columnName[ i ] = source.getColumnName( i + 1 );
			_sSchemaName[ i ] = source.getSchemaName( i + 1 );
			_precision[ i ] = source.getPrecision( i + 1 );
			_scale[ i ] = source.getScale( i + 1 );
			_tableName[ i ] = source.getTableName( i + 1 );
			_catalogName[ i ] = source.getCatalogName( i + 1 );
			_columnType[ i ] = source.getColumnType( i + 1 );
			_columnTypeName[ i ] = source.getColumnTypeName( i + 1 );
			_isReadOnly[ i ] = source.isReadOnly( i + 1 );
			_isWritable[ i ] = source.isWritable( i + 1 );
			_isDefinitelyWritable[ i ] = source.isDefinitelyWritable( i + 1 );
			_columnClassName[ i ] = source.getColumnClassName( i + 1 );
		}
	}

	@Override
	public int getColumnCount()
	{
		return _columnCount;
	}

	@Override
	public boolean isWrapperFor( final Class<?> iface )
	throws SQLException
	{
		return false;
	}

	@Override
	public <T> T unwrap( final Class<T> iface )
	throws SQLException
	{
		throw new SQLException( "not a wrapper" );
	}

	@Override
	public boolean isAutoIncrement( final int column )
	{
		return _isAutoIncrement[ column - 1 ];
	}

	@Override
	public boolean isCaseSensitive( final int column )
	{
		return _isCaseSensitive[ column - 1 ];
	}

	@Override
	public boolean isSearchable( final int column )
	{
		return _isSearchable[ column - 1 ];
	}

	@Override
	public boolean isCurrency( final int column )
	{
		return _isCurrency[ column - 1 ];
	}

	@Override
	public int isNullable( final int column )
	{
		return _isNullable[ column - 1 ];
	}

	@Override
	public boolean isSigned( final int column )
	{
		return _isSigned[ column - 1 ];
	}

	@Override
	public int getColumnDisplaySize( final int column )
	{
		return _columnDisplaySize[ column - 1 ];
	}

	@Override
	public String getColumnLabel( final int column )
	{
		return _columnLabel[ column - 1 ];
	}

	@Override
	public String getColumnName( final int column )
	{
		return _columnName[ column - 1 ];
	}

	@Override
	public String getSchemaName( final int column )
	{
		return _sSchemaName[ column - 1 ];
	}

	@Override
	public int getPrecision( final int column )
	{
		return _precision[ column - 1 ];
	}

	@Override
	public int getScale( final int column )
	{
		return _scale[ column - 1 ];
	}

	@Override
	public String getTableName( final int column )
	{
		return _tableName[ column - 1 ];
	}

	@Override
	public String getCatalogName( final int column )
	{
		return _catalogName[ column - 1 ];
	}

	@Override
	public int getColumnType( final int column )
	{
		return _columnType[ column - 1 ];
	}

	@Override
	public String getColumnTypeName( final int column )
	{
		return _columnTypeName[ column - 1 ];
	}

	@Override
	public boolean isReadOnly( final int column )
	{
		return _isReadOnly[ column - 1 ];
	}

	@Override
	public boolean isWritable( final int column )
	{
		return _isWritable[ column - 1 ];
	}

	@Override
	public boolean isDefinitelyWritable( final int column )
	{
		return _isDefinitelyWritable[ column - 1 ];
	}

	@Override
	public String getColumnClassName( final int column )
	{
		return _columnClassName[ column - 1 ];
	}
}
