/*
 * Copyright (c) 2012-2018, Numdata BV, The Netherlands.
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
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Field handler for a collection of strings.
 *
 * @author Gerrit Meinders
 */
public class StringCollectionFieldHandler
extends ReflectedFieldHandler
{
	/**
	 * Separator character.
	 */
	private final char _separator;

	/**
	 * Whether to use {@code null} for an empty collection.
	 */
	private final boolean _nullIfEmpty;

	/**
	 * Constructs a new instance using comma (',') as a separator and the empty
	 * string to represent an empty collection.
	 *
	 * @param recordClass Table record class.
	 * @param fieldName   Field name.
	 */
	public StringCollectionFieldHandler( @NotNull final Class<?> recordClass, @NotNull final String fieldName )
	{
		this( recordClass, fieldName, ',', false );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param recordClass Table record class.
	 * @param fieldName   Field name.
	 * @param separator   Separator character.
	 * @param nullIfEmpty Whether to use {@code null} for an empty collection.
	 */
	public StringCollectionFieldHandler( @NotNull final Class<?> recordClass, @NotNull final String fieldName, final char separator, final boolean nullIfEmpty )
	{
		super( recordClass, fieldName );
		_separator = separator;
		_nullIfEmpty = nullIfEmpty;
	}

	@Override
	public Collection<String> getFieldValue( @NotNull final Object object )
	{
		return (Collection<String>)super.getFieldValue( object );
	}

	@Override
	public void getColumnData( @NotNull final Object object, @NotNull final ResultSet resultSet, final int columnIndex )
	throws SQLException
	{
		final Collection<String> strings = getFieldValue( object );
		strings.clear();
		strings.addAll( TextTools.tokenize( resultSet.getString( columnIndex ), _separator, false ) );
	}

	@Override
	public void setColumnData( @NotNull final Object object, @NotNull final PreparedStatement preparedStatement, final int columnIndex )
	throws SQLException
	{
		final Collection<String> strings = getFieldValue( object );
		preparedStatement.setString( columnIndex, _nullIfEmpty && strings.isEmpty() ? null : TextTools.getList( strings, _separator ) );
	}
}
