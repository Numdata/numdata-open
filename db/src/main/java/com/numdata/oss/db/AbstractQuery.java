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
import java.util.regex.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Abstract base class for building queries.
 *
 * @param <T> Table record type.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings ( "MismatchedQueryAndUpdateOfStringBuilder" )
public abstract class AbstractQuery<T>
{
	/**
	 * NULL object.
	 */
	public static final Object NULL = null;

	/**
	 * Specifies how a search phrase is matched.
	 *
	 * @see AbstractQuery#andWhereSearchMatch
	 */
	public enum SearchMethod
	{
		/**
		 * Match only when all words are found.
		 */
		ALL_WORDS,

		/**
		 * Match when at least one word is found.
		 */
		ANY_WORD
	}

	/**
	 * Table class.
	 */
	private Class<T> _tableClass = null;

	/**
	 * Table name.
	 */
	private String _tableName = null;

	/**
	 * Alias of the table.
	 */
	@Nullable
	private String _tableAlias = null;

	/**
	 * Free query appended after the table alias.
	 */
	@Nullable
	private CharSequence _tableExtra = null;

	/**
	 * WHERE clause of query.
	 */
	private CharSequence _where = null;

	/**
	 * Parameters for WHERE clause.
	 */
	private final List<Object> _whereParameters = new LinkedList<Object>();

	/**
	 * JOIN clause of query.
	 */
	private CharSequence _join = null;

	/**
	 * Parameters for JOIN clause.
	 */
	private final List<Object> _joinParameters = new LinkedList<Object>();

	/**
	 * Use query parameters (for prepared statement) or inline values.
	 */
	private boolean _usingQueryParameters = true;

	/**
	 * Separator to use between query clauses.
	 */
	private String _separator = " ";

	/**
	 * Create query builder.
	 */
	protected AbstractQuery()
	{
	}

	public boolean isUsingQueryParameters()
	{
		return _usingQueryParameters;
	}

	public void setUsingQueryParameters( final boolean useQueryParameters )
	{
		_usingQueryParameters = useQueryParameters;
	}


	public String getSeparator()
	{
		return _separator;
	}

	public void setSeparator( final String separator )
	{
		_separator = separator;
	}
	/**
	 * Get query that was built. Query parameters, if any, can be retrieved
	 * using {@link #getQueryParameters()}.
	 *
	 * @return Query string.
	 */
	public String getQueryString()
	{
		return getQueryString( getConcreteTableName() );
	}

	/**
	 * Get query that was built. Query parameters, if any, can be retrieved
	 * using {@link #getQueryParameters()}.
	 *
	 * @param tableName Table name to use for query.
	 *
	 * @return Query string.
	 */
	public abstract String getQueryString( @NotNull String tableName );

	/**
	 * Get parameter values used in the query.
	 *
	 * @return Query parameters (may be empty).
	 */
	@NotNull
	public abstract Object[] getQueryParameters();

	/**
	 * Helper method for {@link #getQueryParameters()} to build array with
	 * combined content of specified lists.
	 *
	 * @param lists List of lists.
	 *
	 * @return Array with all list elements.
	 */
	@NotNull
	protected static Object[] toArray( @NotNull final List<Object>... lists )
	{
		int length = 0;
		for ( final List<Object> list : lists )
		{
			length += list.size();
		}

		final Object[] result = new Object[ length ];
		int i = 0;

		for ( final List<Object> list : lists )
		{
			for ( final Object element : list )
			{
				result[ i++ ] = element;
			}
		}

		assert i == length : "Result mismatched!";
		return result;
	}

	@NotNull
	public List<Object> getWhereParameters()
	{
		return new ArrayList<Object>( _whereParameters );
	}

	@NotNull
	public List<Object> getJoinParameters()
	{
		return new ArrayList<Object>( _joinParameters );
	}

	public String getTableName()
	{
		return _tableName;
	}

	public void setTableName( @NotNull final String tableName )
	{
		_tableName = tableName;
	}

	@Nullable
	public String getTableAlias()
	{
		return _tableAlias;
	}

	public void setTableAlias( @Nullable final String tableAlias )
	{
		_tableAlias = tableAlias;
	}

	@Nullable
	public CharSequence getTableExtra()
	{
		return _tableExtra;
	}

	public void setTableExtra( @Nullable final CharSequence tableExtra )
	{
		_tableExtra = tableExtra;
	}

	public Class<T> getTableClass()
	{
		return _tableClass;
	}

	public void setTableClass( @NotNull final Class<T> tableClass )
	{
		_tableClass = tableClass;
	}

	/**
	 * Get concrete table name. This method also performs the table name
	 * derivation if no explicit table name is set.
	 *
	 * @return Table name. Derived if necessary.
	 */
	@NotNull
	public String getConcreteTableName()
	{
		String tableName = getTableName();
		if ( tableName == null )
		{
			final Class<?> tableClass = getTableClass();
			if ( tableClass == null )
			{
				throw new IllegalArgumentException( "Table name or class must be set" );
			}

			final ClassHandler classHandler = DbServices.getClassHandler( tableClass );
			tableName = classHandler.getTableName();
		}
		return tableName;
	}

	public CharSequence getWhere()
	{
		return _where;
	}

	public void setWhere( @Nullable final CharSequence where )
	{
		_where = where;
	}

	/**
	 * Get {@link StringBuilder} for appending text to the WHERE clause.
	 *
	 * @return String builder for WHERE clause.
	 */
	@NotNull
	public StringBuilder getWhereBuilder()
	{
		final StringBuilder result = getStringBuilder( _where );
		_where = result;
		return result;
	}

	/**
	 * Add parameter value to WHERE clause of prepared statement.
	 *
	 * @param value Value to add.
	 */
	public void addWhereParameter( @NotNull final Object value )
	{
		_whereParameters.add( value );
	}

	/**
	 * Add parameter values to WHERE clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addWhereParameters( @NotNull final Object... values )
	{
		for ( final Object value : values )
		{
			addWhereParameter( value );
		}
	}

	/**
	 * Add parameter values to WHERE clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addWhereParameters( @NotNull final Iterable<Object> values )
	{
		for ( final Object value : values )
		{
			addWhereParameter( value );
		}
	}

	/**
	 * Append text to the WHERE clause.
	 *
	 * @param text Text to append.
	 */
	public void where( @NotNull final CharSequence text )
	{
		final StringBuilder sb = getWhereBuilder();
		sb.append( text );
	}

	/**
	 * Append text with the specified parameter values to the WHERE clause.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void where( @NotNull final CharSequence text, @NotNull final Object... parameters )
	{
		where( text );
		addWhereParameters( parameters );
	}

	/**
	 * Append text with the specified parameter values to the WHERE clause.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void where( @NotNull final CharSequence text, @NotNull final Iterable<Object> parameters )
	{
		where( text );
		addWhereParameters( parameters );
	}

	/**
	 * Get {@link StringBuilder} for appending text to the WHERE clause. The
	 * keyword 'AND' is appended when needed.
	 *
	 * @return String builder for WHERE clause.
	 */
	@NotNull
	public StringBuilder andWhere()
	{
		final StringBuilder result = getWhereBuilder();

		boolean andNeeded = false;
		for ( int i = result.length() - 1; i >= 0; i-- )
		{
			final char c = result.charAt( i );
			if ( !Character.isWhitespace( c ) )
			{
				andNeeded = ( c != '(' );
				break;
			}
		}

		if ( andNeeded )
		{
			result.append( getSeparator() );
			result.append( "AND " );
		}

		return result;
	}

	/**
	 * Append text to the WHERE clause. The keyword 'AND' is appended when the
	 * existing WHERE clause when needed.
	 *
	 * @param text Text to append.
	 */
	public void andWhere( @NotNull final CharSequence text )
	{
		final StringBuilder sb = andWhere();
		sb.append( text );
	}

	/**
	 * Append text with the specified parameter values to the WHERE clause. The
	 * keyword 'AND' is appended when needed.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void andWhere( @NotNull final CharSequence text, @NotNull final Object... parameters )
	{
		andWhere( text );
		addWhereParameters( parameters );
	}

	/**
	 * Append text with the specified parameter values to the WHERE clause. The
	 * keyword 'AND' is appended when needed.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void andWhere( @NotNull final CharSequence text, @NotNull final Iterable<Object> parameters )
	{
		andWhere( text );
		addWhereParameters( parameters );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void whereEqual( @NotNull final CharSequence column, @Nullable final Object value )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );

		if ( value != null )
		{
			sb.append( '=' );
			appendValue( sb, _whereParameters, value );
		}
		else
		{
			sb.append( " IS NULL" );
		}
	}

	/**
	 * Append column name to SQL query. This will also prefix the column with
	 * the table alias if the column name does not contain a dot, angled
	 * bracket, comma, or white space.
	 *
	 * @param sb     Builder for query string.
	 * @param column Column name to append.
	 */
	protected void appendColumn( @NotNull final StringBuilder sb, @NotNull final CharSequence column )
	{
		if ( isTableAliasPrependedToColumn( column ) )
		{
			sb.append( getTableAlias() );
			sb.append( '.' );
		}

		sb.append( column );
	}

	/**
	 * Determine whether a table alias should be prepended to the given column
	 * Name. This is the case when a table alias is set and the column name does
	 * not contain a dot, angled bracket, comma, or white space.
	 *
	 * @param column Column name to consider.
	 *
	 * @return {@code true} if table alias should be prepended to the column
	 * name; {@code false} if the column name should be used as-is.
	 */
	protected boolean isTableAliasPrependedToColumn( @NotNull final CharSequence column )
	{
		boolean result = false;

		final int length = column.length();
		if ( length > 0 )
		{
			final String tableAlias = getTableAlias();

			result = ( tableAlias != null );
			if ( result )
			{
				for ( int i = 0; i < length; i++ )
				{
					final char ch = column.charAt( i );
					if ( ( ch == '.' ) || ( ch == ',' ) || ( ch == '(' ) || ( ch == ')' ) || Character.isWhitespace( ch ) )
					{
						result = false;
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void andWhereEqual( @NotNull final CharSequence column, @Nullable final Object value )
	{
		andWhere();
		whereEqual( column, value );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void whereEqual( @NotNull final CharSequence column, final int value )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( '=' );
		sb.append( value );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void andWhereEqual( @NotNull final CharSequence column, final int value )
	{
		andWhere();
		whereEqual( column, value );
	}

	/**
	 * Append WHERE condition for column comparison against a value that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value.
	 */
	@SuppressWarnings ( "ClassReferencesSubclass" )
	public void whereEqual( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( "=(" );
		sb.append( selectQuery.getQueryString() );
		sb.append( ')' );

		addWhereParameters( selectQuery.getQueryParameters() );
	}

	/**
	 * Append WHERE condition for column comparison against a value that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value.
	 */
	@SuppressWarnings ( { "ClassReferencesSubclass", "deprecation" } )
	public void andWhereEqual( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		andWhere();
		whereEqual( column, selectQuery );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void whereNotEqual( @NotNull final CharSequence column, @Nullable final Object value )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );

		if ( value != null )
		{
			sb.append( "<>" );
			appendValue( sb, _whereParameters, value );
		}
		else
		{
			sb.append( " IS NOT NULL" );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void andWhereNotEqual( @NotNull final CharSequence column, @Nullable final Object value )
	{
		andWhere();
		whereNotEqual( column, value );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void whereNotEqual( @NotNull final CharSequence column, final int value )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( "<>" );
		sb.append( value );
	}

	/**
	 * Append WHERE condition for column comparison against a value.
	 *
	 * @param column Name of column to compare.
	 * @param value  Value to compare with.
	 */
	public void andWhereNotEqual( @NotNull final CharSequence column, final int value )
	{
		andWhere();
		whereNotEqual( column, value );
	}

	/**
	 * Append WHERE condition to test whether a column is null .
	 *
	 * @param column Name of column to test.
	 */
	public void whereIsNull( @NotNull final CharSequence column )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( " IS NULL" );
	}

	/**
	 * Append WHERE condition to test whether a column is null .
	 *
	 * @param column Name of column to test.
	 */
	public void andWhereIsNull( @NotNull final CharSequence column )
	{
		andWhere();
		whereIsNull( column );
	}

	/**
	 * Append WHERE condition to test whether a column is null .
	 *
	 * @param column Name of column to test.
	 */
	public void whereIsNotNull( @NotNull final CharSequence column )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( " IS NOT NULL" );
	}

	/**
	 * Append WHERE condition to test whether a column is null .
	 *
	 * @param column Name of column to test.
	 */
	public void andWhereIsNotNull( @NotNull final CharSequence column )
	{
		andWhere();
		whereIsNotNull( column );
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified and {@code null} entries are not
	 * allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereIn( @NotNull final CharSequence column, @NotNull final Iterable<?> values )
	{
		final Iterator<?> it = values.iterator();
		if ( !it.hasNext() )
		{
			throw new IllegalArgumentException( "values is empty" );
		}

		final Object first = it.next();

		if ( !it.hasNext() )
		{
			whereEqual( column, first );
		}
		else
		{
			final StringBuilder sb = getWhereBuilder();
			final List<Object> parameters = _whereParameters;

			appendColumn( sb, column );
			sb.append( " IN (" );
			appendValue( sb, parameters, first );

			while ( it.hasNext() )
			{
				sb.append( ',' );
				appendValue( sb, parameters, it.next() );
			}

			sb.append( ')' );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified and {@code null} entries are not
	 * allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereIn( @NotNull final CharSequence column, @NotNull final Iterable<?> values )
	{
		andWhere();
		whereIn( column, values );
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified and {@code null} entries are not
	 * allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereIn( @NotNull final CharSequence column, @NotNull final Object... values )
	{
		if ( values.length == 0 )
		{
			throw new IllegalArgumentException( "values is empty" );
		}

		if ( values.length == 1 )
		{
			whereEqual( column, values[ 0 ] );
		}
		else
		{
			final StringBuilder sb = getWhereBuilder();
			final List<Object> parameters = _whereParameters;

			appendColumn( sb, column );
			sb.append( " IN (" );
			appendValue( sb, parameters, values[ 0 ] );

			for ( int i = 1; i < values.length; i++ )
			{
				sb.append( ',' );
				appendValue( sb, parameters, values[ i ] );
			}

			sb.append( ')' );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified and {@code null} entries are not
	 * allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereIn( @NotNull final CharSequence column, @NotNull final Object... values )
	{
		andWhere();
		whereIn( column, values );
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereIn( @NotNull final CharSequence column, @NotNull final int... values )
	{
		if ( values.length == 0 )
		{
			throw new IllegalArgumentException( "values is empty" );
		}

		if ( values.length == 1 )
		{
			whereEqual( column, values[ 0 ] );
		}
		else
		{
			final StringBuilder sb = getWhereBuilder();
			appendColumn( sb, column );
			sb.append( " IN (" );
			sb.append( values[ 0 ] );

			for ( int i = 1; i < values.length; i++ )
			{
				sb.append( ',' );
				sb.append( values[ i ] );
			}

			sb.append( ')' );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. At
	 * least one value must be specified.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereIn( @NotNull final CharSequence column, @NotNull final int... values )
	{
		andWhere();
		whereIn( column, values );
	}

	/**
	 * Append WHERE condition for column comparison against a value set that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value set.
	 */
	@SuppressWarnings ( "ClassReferencesSubclass" )
	public void whereIn( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( " IN (" );
		sb.append( selectQuery.getQueryString() );
		sb.append( ')' );

		addWhereParameters( selectQuery.getQueryParameters() );
	}

	/**
	 * Append WHERE condition for column comparison against a value set that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value set.
	 */
	@SuppressWarnings ( { "ClassReferencesSubclass", "deprecation" } )
	public void andWhereIn( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		andWhere();
		whereIn( column, selectQuery );
	}

	/**
	 * Append WHERE condition for column comparison against a value set. {@code
	 * null} entries are not allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereNotIn( @NotNull final CharSequence column, @NotNull final Iterable<?> values )
	{
		final Iterator<?> it = values.iterator();
		if ( it.hasNext() )
		{
			final Object first = it.next();

			if ( !it.hasNext() )
			{
				whereNotEqual( column, first );
			}
			else
			{
				final StringBuilder sb = getWhereBuilder();
				final List<Object> parameters = _whereParameters;

				appendColumn( sb, column );
				sb.append( " NOT IN (" );
				appendValue( sb, parameters, first );

				while ( it.hasNext() )
				{
					sb.append( ',' );
					appendValue( sb, parameters, it.next() );
				}

				sb.append( ')' );
			}
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. {@code
	 * null} entries are not allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereNotIn( @NotNull final CharSequence column, @NotNull final Iterable<?> values )
	{
		final Iterator<?> it = values.iterator();
		if ( it.hasNext() )
		{
			andWhere();
			whereNotIn( column, values );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. {@code
	 * null} entries are not allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereNotIn( @NotNull final CharSequence column, @NotNull final Object... values )
	{
		if ( values.length == 1 )
		{
			whereNotEqual( column, values[ 0 ] );
		}
		else if ( values.length > 0 )
		{
			final StringBuilder sb = getWhereBuilder();
			final List<Object> parameters = _whereParameters;

			appendColumn( sb, column );
			sb.append( " NOT IN (" );
			appendValue( sb, parameters, values[ 0 ] );

			for ( int i = 1; i < values.length; i++ )
			{
				sb.append( ',' );
				appendValue( sb, parameters, values[ i ] );
			}

			sb.append( ')' );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set. {@code
	 * null} entries are not allowed.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereNotIn( @NotNull final CharSequence column, @NotNull final Object... values )
	{
		if ( values.length > 0 )
		{
			andWhere();
			whereNotIn( column, values );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void whereNotIn( @NotNull final CharSequence column, @NotNull final int... values )
	{
		if ( values.length == 1 )
		{
			whereNotEqual( column, values[ 0 ] );
		}
		else if ( values.length > 0 )
		{
			final StringBuilder sb = getWhereBuilder();
			appendColumn( sb, column );
			sb.append( " NOT IN (" );
			sb.append( values[ 0 ] );

			for ( int i = 1; i < values.length; i++ )
			{
				sb.append( ',' );
				sb.append( values[ i ] );
			}

			sb.append( ')' );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set.
	 *
	 * @param column Name of column to compare.
	 * @param values Values to compare with.
	 */
	public void andWhereNotIn( @NotNull final CharSequence column, @NotNull final int... values )
	{
		if ( values.length > 0 )
		{
			andWhere();
			whereNotIn( column, values );
		}
	}

	/**
	 * Append WHERE condition for column comparison against a value set that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value set.
	 */
	@SuppressWarnings ( "ClassReferencesSubclass" )
	public void whereNotIn( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		final StringBuilder sb = getWhereBuilder();
		appendColumn( sb, column );
		sb.append( " NOT IN (" );
		sb.append( selectQuery.getQueryString() );
		sb.append( ')' );

		addWhereParameters( selectQuery.getQueryParameters() );
	}

	/**
	 * Append WHERE condition for column comparison against a value set that is
	 * defined by a SELECT query.
	 *
	 * @param column      Name of column to compare.
	 * @param selectQuery SELECT query to provide value set.
	 */
	@SuppressWarnings ( { "ClassReferencesSubclass", "deprecation" } )
	public void andWhereNotIn( @NotNull final CharSequence column, @NotNull final SelectQuery<?> selectQuery )
	{
		andWhere();
		whereNotIn( column, selectQuery );
	}

	/**
	 * Appends a WHERE-condition that matches the given search phrase against
	 * the specified columns. The given search phrase is split into words and
	 * then matched according to the specified {@link SearchMethod}.
	 *
	 * @param searchPhrase Search phrase.
	 * @param method       Specifies how the search phrase is matched.
	 * @param columns      Columns to be searched.
	 */
	public void whereSearchMatch( @NotNull final String searchPhrase, @NotNull final SearchMethod method, @NotNull final String... columns )
	{
		whereSearchMatch( searchPhrase, method, Arrays.asList( columns ), Collections.<ForeignColumn>emptyList() );
	}

	/**
	 * Appends a WHERE-condition that matches the given search phrase against
	 * the specified columns. The given search phrase is split into words and
	 * then matched according to the specified {@link SearchMethod}.
	 *
	 * @param searchPhrase   Search phrase.
	 * @param method         Specifies how the search phrase is matched.
	 * @param columns        Columns to be searched.
	 * @param foreignColumns Foreign columns to be searched.
	 */
	public void whereSearchMatch( @NotNull final String searchPhrase, @NotNull final SearchMethod method, @NotNull final Collection<String> columns, final Collection<ForeignColumn> foreignColumns )
	{
		if ( TextTools.isEmpty( searchPhrase ) )
		{
			throw new IllegalArgumentException( "missing search text" );
		}

		if ( columns.isEmpty() && foreignColumns.isEmpty() )
		{
			throw new IllegalArgumentException( "no columns specified" );
		}

		final String[] words = searchPhrase.split( "\\s+" );
		final String[] patterns = new String[ words.length ];

		for ( int i = 0; i < words.length; i++ )
		{
			patterns[ i ] = createLikePattern( words[ i ] );
		}

		final StringBuilder sb = getWhereBuilder();
		sb.append( '(' );

		String tableName = getTableName();
		if ( tableName == null )
		{
			final ClassHandler classHandler = DbServices.getClassHandler( getTableClass() );
			tableName = classHandler.getTableName();
		}

		for ( int i = 0; i < patterns.length; i++ )
		{
			if ( i > 0 )
			{
				if ( method == SearchMethod.ALL_WORDS )
				{
					sb.append( " AND " );
				}
				else
				{
					sb.append( " OR " );
				}
			}

			sb.append( '(' );

			for ( final Iterator<String> iterator = columns.iterator(); iterator.hasNext(); )
			{
				appendColumn( sb, iterator.next() );
				sb.append( " LIKE ?" );
				addWhereParameter( patterns[ i ] );
				if ( iterator.hasNext() || !foreignColumns.isEmpty() )
				{
					sb.append( " OR " );
				}
			}

			if ( !foreignColumns.isEmpty() )
			{
				for ( final Iterator<ForeignColumn> iterator = foreignColumns.iterator(); iterator.hasNext(); )
				{
					final ForeignColumn foreignColumn = iterator.next();
					sb.append( "EXISTS (SELECT * FROM " );
					sb.append( foreignColumn.getTableName() );
					sb.append( " WHERE " );
					sb.append( foreignColumn.getForeignKey() );
					sb.append( '=' );
					sb.append( tableName );
					sb.append( '.' );
					sb.append( foreignColumn.getReferencedKey() );
					sb.append( " AND " );
					sb.append( foreignColumn.getColumnName() );
					sb.append( " LIKE ?" );
					addWhereParameter( patterns[ i ] );
					sb.append( ')' );
					if ( iterator.hasNext() )
					{
						sb.append( " OR " );
					}
				}
			}

			sb.append( ')' );
		}

		sb.append( ')' );
	}

	/**
	 * Appends a WHERE-condition that matches the given search phrase against
	 * the specified columns. The given search phrase is split into words and
	 * then matched according to the specified {@link SearchMethod}.
	 *
	 * @param searchPhrase Search phrase.
	 * @param method       Specifies how the search phrase is matched.
	 * @param columns      Columns to be searched.
	 */
	public void andWhereSearchMatch( @NotNull final String searchPhrase, @NotNull final SearchMethod method, @NotNull final String... columns )
	{
		andWhere();
		whereSearchMatch( searchPhrase, method, columns );
	}

	/**
	 * Appends a WHERE-condition that matches the given search phrase against
	 * the specified columns. The given search phrase is split into words and
	 * then matched according to the specified {@link SearchMethod}.
	 *
	 * @param searchPhrase   Search phrase.
	 * @param method         Specifies how the search phrase is matched.
	 * @param columns        Columns to be searched.
	 * @param foreignColumns Foreign columns to be searched.
	 */
	public void andWhereSearchMatch( @NotNull final String searchPhrase, @NotNull final SearchMethod method, @NotNull final Collection<String> columns, final Collection<ForeignColumn> foreignColumns )
	{
		andWhere();
		whereSearchMatch( searchPhrase, method, columns, foreignColumns );
	}

	/**
	 * Appends a WHERE-condition that matches the specified date range. The
	 * {@code startDate} and {@code endDate} parameter can be set to {@code
	 * null} to set no start and/or end limit. If both parameters are set to
	 * {@code null}, this method is a no-op.
	 *
	 * @param column    Name of column to compare.
	 * @param startDate Start date, inclusive. Time is ignored.
	 * @param endDate   End date, inclusive. Time is ignored.
	 */
	public void andWhereBetweenDates( @NotNull final String column, @Nullable final Date startDate, @Nullable final Date endDate )
	{
		if ( ( startDate != null ) || ( endDate != null ) )
		{
			andWhere();
			whereBetweenDates( column, startDate, endDate );
		}
	}

	/**
	 * Appends a WHERE-condition that matches the specified date range. The
	 * {@code startDate} and {@code endDate} parameter can be set to {@code
	 * null} to set no start and/or end limit. If both parameters are set to
	 * {@code null}, this method is a no-op.
	 *
	 * @param column    Name of column to compare.
	 * @param startDate Start date, inclusive. Time is ignored.
	 * @param endDate   End date, inclusive. Time is ignored.
	 */
	public void whereBetweenDates( @NotNull final String column, @Nullable final Date startDate, @Nullable final Date endDate )
	{
		if ( ( startDate != null ) || ( endDate != null ) )
		{
			final StringBuilder sb = getWhereBuilder();

			if ( startDate != null )
			{
				appendColumn( sb, column );
				sb.append( " >= ?" );
				addWhereParameter( CalendarTools.getDateInstance( startDate ).getTime() );
			}

			if ( ( startDate != null ) && ( endDate != null ) )
			{
				sb.append( " AND " );
			}

			if ( endDate != null )
			{
				final Calendar calendar = CalendarTools.getDateInstance( endDate );
				calendar.add( Calendar.DATE, 1 );
				final Date date = calendar.getTime();

				appendColumn( sb, column );
				sb.append( " < ?" );
				addWhereParameter( date );
			}
		}
	}

	/**
	 * Test whether specified value is a simple literal that should be embedded
	 * literally in the query without quotes and without the use of query
	 * parameters.
	 *
	 * @param value Value to test.
	 *
	 * @return {@code true} if value is a simple literal; {@code false}
	 * otherwise.
	 */
	protected static boolean isSimpleLiteral( final Object value )
	{
		return ( ( value instanceof Boolean ) || ( value instanceof Enum ) || ( value instanceof Number ) );
	}

	/**
	 * Append value to SQL query. This will write '{@code NULL}' for {@code
	 * null} values, write simple literals as-is, and add a query parameter or
	 * escaped value otherwise.
	 *
	 * A query parameters is only added the value
	 * is not {@code null}, {@link #isSimpleLiteral} returns {@code false} for
	 * the specified {@code value}, the {@code parameters} argument is set, and
	 * the {@link #isUsingQueryParameters()} method returns {@code true}. If a
	 * query parameter is added, a '?' is appended to the query string.
	 *
	 * @param sb         Builder for query string.
	 * @param parameters List of query parameters.
	 * @param value      Value to append.
	 *
	 * @see #isSimpleLiteral
	 * @see #isUsingQueryParameters
	 */
	protected void appendValue( @NotNull final StringBuilder sb, @Nullable final Collection<Object> parameters, @Nullable final Object value )
	{
		if ( value == null )
		{
			sb.append( "NULL" );
		}
		else if ( ( parameters != null ) && isUsingQueryParameters() && !isSimpleLiteral( value ) )
		{
			sb.append( '?' );
			parameters.add( value );
		}
		else
		{
			if ( ( value instanceof Number ) || ( value instanceof Boolean ) )
			{
				sb.append( value );
			}
			else
			{
				sb.append( '\'' );
				TextTools.escape( sb, value.toString() );
				sb.append( '\'' );
			}
		}
	}

	/**
	 * Creates a pattern for use in a {@code LIKE}-clause from the given string.
	 * Any wildcard characters ({@code '%'} and {@code '_'}) in the string are
	 * escaped, while any whitespace is replaced with a wildcard ({@code '%'}).
	 *
	 * The only non-wildcard character that is escaped, is the escape
	 * character ({@code '\'}) itself. <strong>Quotes are NOT escaped.</strong>
	 *
	 * @param string String to create a pattern from.
	 *
	 * @return Pattern for use in a {@code LIKE}-clause.
	 */
	@NotNull
	public static String createLikePattern( @NotNull final CharSequence string )
	{
		final StringBuffer result = new StringBuffer( string.length() + 10 );

		final Pattern pattern = Pattern.compile( "([\\\\%_])|(\\s+)" );
		final Matcher matcher = pattern.matcher( string );

		result.append( '%' );
		while ( matcher.find() )
		{
			if ( matcher.group( 1 ) != null )
			{
				matcher.appendReplacement( result, "\\\\$0" );
			}
			else
			{
				matcher.appendReplacement( result, "%" );
			}
		}
		matcher.appendTail( result );
		result.append( '%' );

		return result.toString();
	}

	public CharSequence getJoin()
	{
		return _join;
	}

	public void setJoin( @Nullable final CharSequence join )
	{
		_join = join;
	}

	/**
	 * Get {@link StringBuilder} for appending text to the JOIN clause.
	 *
	 * @return String builder for JOIN clause.
	 */
	@NotNull
	public StringBuilder getJoinBuilder()
	{
		final StringBuilder result = getStringBuilder( _join );
		_join = result;

		if ( result.length() > 0 )
		{
			result.append( getSeparator() );
		}

		return result;
	}

	/**
	 * Returns a string builder from the given character sequence.
	 *
	 * @param charSequence Character sequence.
	 *
	 * @return String builder.
	 */
	protected StringBuilder getStringBuilder( final CharSequence charSequence )
	{
		final StringBuilder result;
		if ( charSequence instanceof StringBuilder )
		{
			result = (StringBuilder)charSequence;
		}
		else
		{
			result = new StringBuilder();
			if ( TextTools.isNonEmpty( charSequence ) )
			{
				result.append( charSequence );
			}
		}
		return result;
	}

	/**
	 * Append text to the JOIN clause.
	 *
	 * @param text Text to append.
	 */
	public void join( @NotNull final CharSequence text )
	{
		final StringBuilder sb = getJoinBuilder();
		sb.append( text );
	}

	/**
	 * Append text with the specified parameter values to the JOIN clause.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void join( @NotNull final CharSequence text, @NotNull final Object... parameters )
	{
		join( text );
		addJoinParameters( parameters );
	}

	/**
	 * Append text with the specified parameter values to the JOIN clause.
	 *
	 * @param text       Text to append.
	 * @param parameters Parameter values to add.
	 */
	public void join( @NotNull final CharSequence text, @NotNull final Iterable<Object> parameters )
	{
		join( text );
		addJoinParameters( parameters );
	}

	/**
	 * Adds a JOIN to the query.
	 *
	 * @param tableAlias           Alias of the table containing the foreign
	 *                             key.
	 * @param foreignKey           Name of the foreign key column.
	 * @param referencedTableClass Class of the referenced table.
	 * @param referencedTableAlias Alias of the referenced table.
	 * @param referencedKey        Name of the key column in the referenced
	 *                             table.
	 */
	public void join( @Nullable final String tableAlias, @NotNull final String foreignKey, @NotNull final Class<?> referencedTableClass, @Nullable final String referencedTableAlias, @NotNull final String referencedKey )
	{
		join( "JOIN", tableAlias, foreignKey, referencedTableClass, referencedTableAlias, referencedKey );
	}

	/**
	 * Adds a JOIN to the query.
	 *
	 * @param joinType             Type of join (e.g. 'JOIN', 'INNER JOIN',
	 *                             'LEFT OUTER JOIN', etc).
	 * @param tableAlias           Alias of the table containing the foreign
	 *                             key.
	 * @param foreignKey           Name of the foreign key column.
	 * @param referencedTableClass Class of the referenced table.
	 * @param referencedTableAlias Alias of the referenced table.
	 * @param referencedKey        Name of the key column in the referenced
	 *                             table.
	 */
	public void join( @NotNull final String joinType, @Nullable final String tableAlias, @NotNull final String foreignKey, @NotNull final Class<?> referencedTableClass, @Nullable final String referencedTableAlias, @NotNull final String referencedKey )
	{
		final ClassHandler classHandler = DbServices.getClassHandler( referencedTableClass );
		final String referencedTableName = classHandler.getTableName();

		final StringBuilder join = getJoinBuilder();
		join.append( joinType );
		join.append( ' ' );
		join.append( referencedTableName );
		if ( referencedTableAlias != null )
		{
			join.append( " AS " );
			join.append( referencedTableAlias );
		}
		join.append( " ON " );
		join.append( ( tableAlias != null ) ? tableAlias : getConcreteTableName() );
		join.append( '.' );
		join.append( foreignKey );
		join.append( '=' );
		join.append( ( referencedTableAlias != null ) ? referencedTableAlias : referencedTableName );
		join.append( '.' );
		join.append( referencedKey );
	}

	/**
	 * Add parameter value to JOIN clause of prepared statement.
	 *
	 * @param value Value to add.
	 */
	public void addJoinParameter( @NotNull final Object value )
	{
		_joinParameters.add( value );
	}

	/**
	 * Add parameter values to JOIN clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addJoinParameters( @NotNull final Object... values )
	{
		for ( final Object value : values )
		{
			addJoinParameter( value );
		}
	}

	/**
	 * Add parameter values to JOIN clause of prepared statement.
	 *
	 * @param values Values to add.
	 */
	public void addJoinParameters( @NotNull final Iterable<Object> values )
	{
		for ( final Object value : values )
		{
			addJoinParameter( value );
		}
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + "[queryString='" + getQueryString() + "', queryParameters=" + Arrays.toString( getQueryParameters() ) + ']';
	}

	/**
	 * Represents a column in a specific table.
	 */
	public static class ForeignColumn
	{
		/**
		 * Name of the table containing the column.
		 */
		@NotNull
		private final String _tableName;

		/**
		 * Name of the column.
		 */
		@NotNull
		private final String _columnName;

		/**
		 * Name of a foreign key in the same table.
		 */
		@NotNull
		private final String _foreignKey;

		/**
		 * Name of the column referenced by the foreign key.
		 */
		@NotNull
		private final String _referencedKey;

		/**
		 * Constructs a new instance.
		 *
		 * @param tableClass    Class of the table containing the column.
		 * @param columnName    Name of the column.
		 * @param foreignKey    Name of a foreign key in the same table.
		 * @param referencedKey Name of the column referenced by the foreign
		 *                      key.
		 */
		public ForeignColumn( @NotNull final Class<?> tableClass, @NotNull final String columnName, @NotNull final String foreignKey, @NotNull final String referencedKey )
		{
			final ClassHandler classHandler = DbServices.getClassHandler( tableClass );
			_tableName = classHandler.getTableName();
			_columnName = columnName;
			_foreignKey = foreignKey;
			_referencedKey = referencedKey;
		}

		/**
		 * Constructs a new instance.
		 *
		 * @param tableName     Name of the table containing the column.
		 * @param columnName    Name of the column.
		 * @param foreignKey    Name of a foreign key in the same table.
		 * @param referencedKey Name of the column referenced by the foreign
		 *                      key.
		 */
		public ForeignColumn( @NotNull final String tableName, @NotNull final String columnName, @NotNull final String foreignKey, @NotNull final String referencedKey )
		{
			_tableName = tableName;
			_columnName = columnName;
			_foreignKey = foreignKey;
			_referencedKey = referencedKey;
		}

		@NotNull
		public String getTableName()
		{
			return _tableName;
		}

		@NotNull
		public String getColumnName()
		{
			return _columnName;
		}

		@NotNull
		public String getForeignKey()
		{
			return _foreignKey;
		}

		@NotNull
		public String getReferencedKey()
		{
			return _referencedKey;
		}
	}
}