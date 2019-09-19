/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Builder for UPDATE queries.
 *
 * @param <T> Table type.
 *
 * @author Peter S. Heijnen
 */
public class UpdateQuery<T>
extends AbstractQuery<T>
{
	/**
	 * SET clause of query.
	 */
	private CharSequence _set = null;

	/**
	 * Parameters for SET clause.
	 */
	private final List<Object> _setParameters = new LinkedList<Object>();

	/**
	 * Create query builder.
	 */
	public UpdateQuery()
	{
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableName Table name.
	 */
	public UpdateQuery( @NotNull final String tableName )
	{
		setTableName( tableName );
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableClass Table record class.
	 */
	public UpdateQuery( @NotNull final Class<T> tableClass )
	{
		setTableClass( tableClass );
	}

	@NotNull
	@Override
	public String getQueryString( @NotNull final String tableName )
	{
		final CharSequence set = _set;
		if ( TextTools.isEmpty( set ) )
		{
			throw new IllegalStateException( "Must have SET" );
		}

		final StringBuilder sb = new StringBuilder();

		sb.append( "UPDATE " );
		sb.append( tableName );

		final String tableAlias = getTableAlias();
		if ( !TextTools.isEmpty( tableAlias ) )
		{
			sb.append( " AS " );
			sb.append( tableAlias );
		}

		final CharSequence join = getJoin();
		if ( !TextTools.isEmpty( join ) )
		{
			sb.append( getSeparator() );
			sb.append( join );
		}

		sb.append( getSeparator() );
		sb.append( "SET " );
		sb.append( set );

		final CharSequence where = getWhere();
		if ( !TextTools.isEmpty( where ) )
		{
			sb.append( getSeparator() );
			sb.append( "WHERE " );
			sb.append( where );
		}

		return sb.toString();
	}

	@Override
	@NotNull
	public Object[] getQueryParameters()
	{
		return toArray( getJoinParameters(), getSetParameters(), getWhereParameters() );
	}

	/**
	 * Get builder for SET clause. A comma is appended to the existing SET clause
	 * when needed.
	 *
	 * @return {@link StringBuilder} for SET clause.
	 */
	public StringBuilder getSetBuilder()
	{
		final StringBuilder result;

		final CharSequence set = _set;

		if ( set instanceof StringBuilder )
		{
			result = (StringBuilder)set;
		}
		else
		{
			result = new StringBuilder();
			_set = result;

			if ( TextTools.isNonEmpty( set ) )
			{
				result.append( set );
			}
		}

		if ( TextTools.isNonEmpty( result ) )
		{
			result.append( ',' );
		}

		return result;
	}

	/**
	 * Append text to the SET clause. A comma is appended to the existing SET
	 * clause when needed.
	 *
	 * @param text Text to append.
	 */
	public void set( @NotNull final CharSequence text )
	{
		final StringBuilder result = getSetBuilder();
		result.append( text );
	}

	/**
	 * Append text with the specified paramter values to the SET clause. A comma is
	 * appended to the existing SET clause when needed.
	 *
	 * @param column Name of column to set.
	 * @param value  Value to assign to column.
	 */
	public void set( @NotNull final CharSequence column, @Nullable final Object value )
	{
		final StringBuilder sb = getSetBuilder();
		sb.append( column );
		sb.append( '=' );
		appendValue( sb, _setParameters, value );
	}

	/**
	 * Append text with the specified paramter values to the SET clause. A comma is
	 * appended to the existing SET clause when needed.
	 *
	 * @param column Name of column to set.
	 * @param value  Value to assign to column.
	 */
	public void set( @NotNull final CharSequence column, final boolean value )
	{
		set( column, Boolean.valueOf( value ) );
	}

	/**
	 * Append text with the specified paramter values to the SET clause. A comma is
	 * appended to the existing SET clause when needed.
	 *
	 * @param column Name of column to set.
	 * @param value  Value to assign to column.
	 */
	public void set( @NotNull final CharSequence column, final double value )
	{
		set( column, Double.valueOf( value ) );
	}

	/**
	 * Append text with the specified paramter values to the SET clause. A comma is
	 * appended to the existing SET clause when needed.
	 *
	 * @param column Name of column to set.
	 * @param value  Value to assign to column.
	 */
	public void set( @NotNull final CharSequence column, final int value )
	{
		set( column, Integer.valueOf( value ) );
	}

	/**
	 * Append WHERE condition for column comparison against a value that is defined
	 * by a SELECT query.
	 *
	 * @param column      Name of column to set.
	 * @param selectQuery SELECT query to provide value.
	 */
	public void set( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		final StringBuilder sb = getSetBuilder();
		sb.append( column );
		sb.append( "=(" );
		sb.append( selectQuery.getQueryString() );
		sb.append( ')' );

		addWhereParameters( selectQuery.getQueryParameters() );
	}

	/**
	 * Get SET clause of query.
	 *
	 * @return SET clause of query.
	 */
	public CharSequence getSet()
	{
		return _set;
	}

	/**
	 * Set SET clause of query.
	 *
	 * @param set SET clause of query.
	 */
	public void setSet( @Nullable final CharSequence set )
	{
		_set = set;
	}

	/**
	 * Get parameter values used in SET clause.
	 *
	 * @return SET query parameters (may be empty).
	 */
	@NotNull
	public List<Object> getSetParameters()
	{
		return new ArrayList<Object>( _setParameters );
	}

	/**
	 * Add parameter value to SET clause of prepared statement.
	 *
	 * @param value Value to add.
	 */
	public void addSetParameter( @NotNull final Object value )
	{
		_setParameters.add( value );
	}

	/**
	 * Add parameter values to SET clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addSetParameters( @NotNull final Object... values )
	{
		for ( final Object value : values )
		{
			addSetParameter( value );
		}
	}

	/**
	 * Add parameter values to SET clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addSetParameters( @NotNull final Iterable<Object> values )
	{
		for ( final Object value : values )
		{
			addSetParameter( value );
		}
	}
}
