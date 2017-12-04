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

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Builder for SELECT queries.
 *
 * @param <T> Table type.
 *
 * @author Peter S. Heijnen
 */
public class SelectQuery<T>
extends AbstractQuery<T>
{
	/**
	 * Order direction to use in ORDER BY.
	 *
	 * @see SelectQuery#orderBy
	 */
	public enum Ordering
	{
		/**
		 * Ascending.
		 */
		ASC,

		/**
		 * Descending.
		 */
		DESC
	}

	/**
	 * SELECT fields.
	 */
	private CharSequence _select = null;

	/**
	 * Parameters for WHERE clause.
	 */
	private final List<Object> _selectParameters = new LinkedList<Object>();

	/**
	 * GROUP BY clause of query.
	 */
	private CharSequence _groupBy = null;

	/**
	 * ORDER BY clause of query.
	 */
	private CharSequence _orderBy = null;

	/**
	 * Suffix to add to the query string.
	 */
	private CharSequence _suffix = null;

	/**
	 * Create query builder.
	 */
	public SelectQuery()
	{
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableName Table name.
	 */
	public SelectQuery( @NotNull final String tableName )
	{
		setTableName( tableName );
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableClass Table record class.
	 */
	public SelectQuery( @NotNull final Class<T> tableClass )
	{
		setTableClass( tableClass );
	}

	@NotNull
	@Override
	public String getQueryString( @NotNull final String tableName )
	{
		final StringBuilder sb = new StringBuilder();

		sb.append( "SELECT " );

		final String tableAlias = getTableAlias();
		final boolean hasTableAlias = !TextTools.isEmpty( tableAlias );

		final CharSequence select = _select;
		if ( TextTools.isEmpty( select ) )
		{
			if ( hasTableAlias )
			{
				sb.append( tableAlias );
				sb.append( '.' );
			}

			sb.append( '*' );
		}
		else
		{
			sb.append( select );
		}

		sb.append( getSeparator() );
		sb.append( "FROM " );
		sb.append( tableName );

		if ( hasTableAlias )
		{
			sb.append( " AS " );
			sb.append( tableAlias );
		}

		final CharSequence tableExtra = getTableExtra();
		if ( tableExtra != null )
		{
			sb.append( getSeparator() );
			sb.append( tableExtra );
		}

		final CharSequence join = getJoin();
		if ( !TextTools.isEmpty( join ) )
		{
			sb.append( getSeparator() );
			sb.append( join );
		}

		final CharSequence where = getWhere();
		if ( !TextTools.isEmpty( where ) )
		{
			sb.append( getSeparator() );
			sb.append( "WHERE " );
			sb.append( where );
		}

		final CharSequence groupBy = _groupBy;
		if ( !TextTools.isEmpty( groupBy ) )
		{
			sb.append( getSeparator() );
			sb.append( "GROUP BY " );
			sb.append( groupBy );
		}

		final CharSequence orderBy = _orderBy;
		if ( !TextTools.isEmpty( orderBy ) )
		{
			sb.append( getSeparator() );
			sb.append( "ORDER BY " );
			sb.append( orderBy );
		}

		final CharSequence suffix = _suffix;
		if ( !TextTools.isEmpty( suffix ) )
		{
			sb.append( getSeparator() );
			sb.append( suffix );
		}

		return sb.toString();
	}

	@NotNull
	public List<Object> getSelectParameters()
	{
		return new ArrayList<Object>( _selectParameters );
	}

	@Override
	@NotNull
	public Object[] getQueryParameters()
	{
		return toArray( getSelectParameters(), getJoinParameters(), getWhereParameters() );
	}

	@Nullable
	public CharSequence getSelect()
	{
		return _select;
	}

	public void setSelect( @Nullable final CharSequence select )
	{
		_select = select;
	}

	/**
	 * Get {@link StringBuilder} for appending text to the SELECT clause.
	 *
	 * @return String builder for SELECT clause.
	 */
	@NotNull
	public StringBuilder getSelectBuilder()
	{
		final StringBuilder result = getStringBuilder( _select );
		_select = result;
		return result;
	}

	/**
	 * Add parameter value to SELECT clause of prepared statement.
	 *
	 * @param value Value to add.
	 */
	public void addSelectParameter( @NotNull final Object value )
	{
		_selectParameters.add( value );
	}

	/**
	 * Add parameter values to SELECT clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addSelectParameters( @NotNull final Object... values )
	{
		for ( final Object value : values )
		{
			addSelectParameter( value );
		}
	}

	/**
	 * Add parameter values to SELECT clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addSelectParameters( @NotNull final Iterable<Object> values )
	{
		for ( final Object value : values )
		{
			addSelectParameter( value );
		}
	}

	/**
	 * Add column to the SELECT clause. A comma is inserted before the column
	 * when the existing SELECT clause is non-empty.
	 *
	 * @param column Column to add to SELECT clause.
	 */
	public void select( @NotNull final CharSequence column )
	{
		if ( TextTools.isEmpty( column ) )
		{
			throw new IllegalArgumentException( "Trying to add empty column" );
		}

		CharSequence select = getSelect();

		final boolean addAlias = isTableAliasPrependedToColumn( column );
		final boolean emptySelect = TextTools.isEmpty( select );
		if ( emptySelect && !addAlias )
		{
			select = column.toString();
		}
		else
		{
			final StringBuilder sb;

			if ( select instanceof StringBuilder )
			{
				sb = (StringBuilder)select;
				if ( emptySelect )
				{
					sb.setLength( 0 );
				}
			}
			else
			{
				sb = new StringBuilder();

				if ( !emptySelect )
				{
					sb.append( select );
				}
			}

			if ( !emptySelect )
			{
				sb.append( ',' );
			}

			if ( addAlias )
			{
				sb.append( getTableAlias() );
				sb.append( '.' );
			}

			sb.append( column );
			select = sb;
		}

		setSelect( select );
	}

	/**
	 * Add element to the SELECT clause. A comma is inserted before the element
	 * when the existing SELECT clause is non-empty.
	 *
	 * @param function Function that determined the column value.
	 * @param alias    Alias assigned to value.
	 */
	public void select( @NotNull final CharSequence function, @Nullable final CharSequence alias )
	{
		select( ( alias != null ) ? ( function + " AS " + alias ) : function );
	}

	/**
	 * Adds a sub-select to the SELECT clause. A comma is inserted before the element
	 * when the existing SELECT clause is non-empty.
	 *
	 * @param selectQuery Sub-select that provides the value.
	 * @param alias       Alias assigned to value.
	 */
	public void select( @NotNull final SelectQuery<?> selectQuery, @NotNull final CharSequence alias )
	{
		final StringBuilder sb = getSelectBuilder();
		if ( !TextTools.isEmpty( sb ) )
		{
			sb.append( ',' );
		}

		sb.append( '(' );
		sb.append( selectQuery.getQueryString() );
		sb.append( ") AS " );
		sb.append( alias );

		addSelectParameters( selectQuery.getQueryParameters() );
	}

	/**
	 * Add columns to the SELECT clause. A comma is inserted before the columns
	 * when the existing SELECT clause is non-empty.
	 *
	 * @param columns Columns to add to SELECT clause.
	 */
	public void select( final CharSequence[] columns )
	{
		for ( final CharSequence column : columns )
		{
			select( column );
		}
	}

	/**
	 * Add columns to the SELECT clause. A comma is inserted before the columns
	 * when the existing SELECT clause is non-empty.
	 *
	 * @param columns Columns to add to SELECT clause.
	 */
	public void select( final Iterable<? extends CharSequence> columns )
	{
		for ( final CharSequence column : columns )
		{
			select( column );
		}
	}

	@Nullable
	public CharSequence getGroupBy()
	{
		return _groupBy;
	}

	public void setGroupBy( @Nullable final CharSequence groupBy )
	{
		_groupBy = groupBy;
	}

	/**
	 * Add column to the GROUP BY clause. A comma is inserted before the column
	 * when the existing GROUP BY clause is non-empty.
	 *
	 * @param column Column to add to GROUP BY clause.
	 */
	public void groupBy( @NotNull final CharSequence column )
	{
		if ( TextTools.isEmpty( column ) )
		{
			throw new IllegalArgumentException( "Trying to group by empty column" );
		}

		final CharSequence oldValue = getGroupBy();

		if ( ( oldValue == null ) || TextTools.isEmpty( oldValue ) )
		{
			setGroupBy( column );
		}
		else
		{
			final StringBuilder sb;

			if ( oldValue instanceof StringBuilder )
			{
				sb = (StringBuilder)oldValue;
			}
			else
			{
				sb = new StringBuilder( oldValue.length() + 1 + column.length() );
				sb.append( oldValue );
				setGroupBy( sb );
			}

			sb.append( ',' );
			sb.append( column );
		}
	}

	public CharSequence getOrderBy()
	{
		return _orderBy;
	}

	public void setOrderBy( @Nullable final CharSequence orderBy )
	{
		_orderBy = orderBy;
	}

	/**
	 * Add column to the ORDER BY clause. A comma is inserted before the column
	 * when the existing ORDER BY clause is non-empty.
	 *
	 * @param column Column to add to ORDER BY clause.
	 */
	public void orderBy( @NotNull final CharSequence column )
	{
		orderBy( column, null );
	}

	/**
	 * Add column to the ORDER BY clause. A comma is inserted before the column
	 * when the existing ORDER BY clause is non-empty.
	 *
	 * @param column         Column to add to ORDER BY clause.
	 * @param orderDirection Order direction (optional).
	 */
	public void orderBy( @NotNull final CharSequence column, @Nullable final Ordering orderDirection )
	{
		if ( TextTools.isEmpty( column ) )
		{
			throw new IllegalArgumentException( "Trying to order by empty column" );
		}

		final CharSequence orderBy = getOrderBy();

		if ( ( orderBy == null ) && ( orderDirection == null ) )
		{
			setOrderBy( column );
		}
		else
		{
			final String dir = ( orderDirection != null ) ? orderDirection.name() : null;

			final StringBuilder sb;

			if ( orderBy instanceof StringBuilder )
			{
				sb = (StringBuilder)orderBy;

				if ( !TextTools.isEmpty( orderBy ) )
				{
					sb.append( ',' );
				}
			}
			else if ( ( orderBy != null ) && !TextTools.isEmpty( orderBy ) )
			{
				sb = new StringBuilder( orderBy.length() + 1 + column.length() + ( ( dir != null ) ? 1 + dir.length() : 0 ) );
				sb.append( orderBy );
				sb.append( ',' );
				setOrderBy( sb );
			}
			else
			{
				sb = new StringBuilder( column.length() + ( ( dir != null ) ? 1 + dir.length() : 0 ) );
				setOrderBy( sb );
			}

			sb.append( column );

			if ( dir != null )
			{
				sb.append( ' ' );
				sb.append( dir );
			}
		}
	}

	public CharSequence getSuffix()
	{
		return _suffix;
	}

	public void setSuffix( @Nullable final CharSequence suffix )
	{
		_suffix = suffix;
	}
}