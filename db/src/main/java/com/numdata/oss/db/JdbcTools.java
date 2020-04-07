/*
 * Copyright (c) 2011-2020, Numdata BV, The Netherlands.
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
import java.sql.*;
import java.util.*;
import javax.sql.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class provides some utility methods for the JDBC API.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "JDBCExecuteWithNonConstantString", "JDBCPrepareStatementWithNonConstantString" } )
public class JdbcTools
{
	/**
	 * Result processor that takes a string result from the first row/column in
	 * the result set.
	 */
	public static final ResultProcessor<String> GET_STRING = resultSet -> resultSet.next() ? resultSet.getString( 1 ) : null;

	/**
	 * Result processor that takes an integer result from the first row/column
	 * in the result set.
	 */
	public static final ResultProcessor<Integer> GET_INTEGER = resultSet -> resultSet.next() ? resultSet.getInt( 1 ) : null;

	/**
	 * Result processor that takes a double precision floating-point result from
	 * the first row/column in the result set.
	 */
	public static final ResultProcessor<Double> GET_DOUBLE = resultSet -> resultSet.next() ? resultSet.getDouble( 1 ) : null;

	/**
	 * Result processor that takes a numeric result from the first row/column in
	 * the result set.
	 */
	public static final ResultProcessor<Number> GET_NUMBER = resultSet -> {
		Number result = null;

		if ( resultSet.next() )
		{
			final Object object = resultSet.getObject( 1 );
			if ( object != null )
			{
				if ( object instanceof Number )
				{
					result = (Number)object;
				}
				else
				{
					result = resultSet.getDouble( 1 );
				}
			}
		}

		return result;
	};

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JdbcTools()
	{
	}

	/**
	 * Returns whether a table exists in the database.
	 *
	 * @param dataSource Data source for database to use.
	 * @param tableName  Name of table.
	 *
	 * @return {@code true} if the table exists; {@code false} if it doesn't or
	 * an error occurred.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static boolean tableExists( @NotNull final DataSource dataSource, @NotNull final String tableName )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( true );
			return tableExists( connection, tableName );
		}
	}

	/**
	 * Returns whether a table exists in the database.
	 *
	 * @param connection Connection to database.
	 * @param tableName  Name of table.
	 *
	 * @return {@code true} if the table exists; {@code false} if it doesn't or
	 * an error occurred.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static boolean tableExists( @NotNull final Connection connection, @NotNull final String tableName )
	throws SQLException
	{
		boolean result;

		try ( final Statement statement = connection.createStatement() )
		{
			try
			{
				final ResultSet resultSet = statement.executeQuery( "SELECT 1 FROM " + tableName + " WHERE 0=1" );
				resultSet.close();
				result = true;
			}
			catch ( final SQLException ignored )
			{
				// exception is expected if table does not exist
				result = false;
			}
		}

		return result;
	}

	/**
	 * Returns whether a table column exists in the database.
	 *
	 * @param dataSource Data source for database to use.
	 * @param tableName  Name of table.
	 * @param column     Column.
	 *
	 * @return {@code true} if the column exists; {@code false} if it doesn't or
	 * an error occurred.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static boolean columnExists( @NotNull final DataSource dataSource, @NotNull final String tableName, @NotNull final String column )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( true );
			return columnExists( connection, tableName, column );
		}
	}

	/**
	 * Returns whether a table column exists in the database.
	 *
	 * @param connection Connection to database.
	 * @param tableName  Name of table.
	 * @param column     Column.
	 *
	 * @return {@code true} if the column exists; {@code false} if it doesn't or
	 * an error occurred.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static boolean columnExists( @NotNull final Connection connection, @NotNull final String tableName, @NotNull final String column )
	throws SQLException
	{
		boolean result;

		try ( final Statement statement = connection.createStatement() )
		{
			try
			{
				final ResultSet resultSet = statement.executeQuery( "SELECT " + column + " FROM " + tableName + " WHERE 0=1" );
				resultSet.close();
				result = true;
			}
			catch ( final SQLException ignored )
			{
				result = false;
			}
		}

		return result;
	}

	/**
	 * Execute delete query.
	 *
	 * @param dataSource  Data source for database to use.
	 * @param deleteQuery {@link DeleteQuery} to execute.
	 *
	 * @return Number of rows that were deleted (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeDelete( @NotNull final DataSource dataSource, @NotNull final DeleteQuery<?> deleteQuery )
	throws SQLException
	{
		return executeUpdate( dataSource, deleteQuery.getQueryString(), deleteQuery.getQueryParameters() );
	}

	/**
	 * Execute delete query.
	 *
	 * @param connection  Connection to database.
	 * @param deleteQuery {@link DeleteQuery} to execute.
	 *
	 * @return Number of rows that were deleted (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeDelete( @NotNull final Connection connection, @NotNull final DeleteQuery<?> deleteQuery )
	throws SQLException
	{
		return executeUpdate( connection, deleteQuery.getQueryString(), deleteQuery.getQueryParameters() );
	}

	/**
	 * Execute update query.
	 *
	 * @param dataSource  Data source for database to use.
	 * @param updateQuery {@link UpdateQuery} to execute.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeUpdate( @NotNull final DataSource dataSource, @NotNull final UpdateQuery<?> updateQuery )
	throws SQLException
	{
		return executeUpdate( dataSource, updateQuery.getQueryString(), updateQuery.getQueryParameters() );
	}

	/**
	 * Execute update query.
	 *
	 * @param connection  Connection to database.
	 * @param updateQuery {@link UpdateQuery} to execute.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeUpdate( @NotNull final Connection connection, @NotNull final UpdateQuery<?> updateQuery )
	throws SQLException
	{
		return executeUpdate( connection, updateQuery.getQueryString(), updateQuery.getQueryParameters() );
	}

	/**
	 * Get numeric result from single-column query.
	 *
	 * @param dataSource  Data source for database to use.
	 * @param selectQuery SELECT query to execute.
	 *
	 * @return Resulting number; {@code null} if query returned no or NULL
	 * result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@Nullable
	public static Number selectNumber( @NotNull final DataSource dataSource, @NotNull final SelectQuery<?> selectQuery )
	throws SQLException
	{
		return executeQuery( dataSource, GET_NUMBER, selectQuery.getQueryString(), selectQuery.getQueryParameters() );
	}

	/**
	 * Get numeric result from single-column query.
	 *
	 * @param connection  Connection to database.
	 * @param selectQuery SELECT query to execute.
	 *
	 * @return Resulting number; {@code null} if query returned no or NULL
	 * result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@Nullable
	public static Number selectNumber( @NotNull final Connection connection, @NotNull final SelectQuery<?> selectQuery )
	throws SQLException
	{
		return executeQuery( connection, GET_NUMBER, selectQuery.getQueryString(), selectQuery.getQueryParameters() );
	}

	/**
	 * Get numeric result from single-column query.
	 *
	 * @param dataSource      Data source for database to use.
	 * @param queryString     SELECT query to execute.
	 * @param queryParameters Query parameters.
	 *
	 * @return Resulting number; {@code null} if query returned no or NULL
	 * result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@Nullable
	public static Number selectNumber( @NotNull final DataSource dataSource, @NotNull final CharSequence queryString, @NotNull final Object... queryParameters )
	throws SQLException
	{
		return executeQuery( dataSource, GET_NUMBER, queryString, queryParameters );
	}

	/**
	 * Get numeric result from single-column query.
	 *
	 * @param connection      Connection to database.
	 * @param queryString     SELECT query to execute.
	 * @param queryParameters Query parameters.
	 *
	 * @return Resulting number; {@code null} if query returned no or NULL
	 * result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@Nullable
	public static Number selectNumber( @NotNull final Connection connection, @NotNull final CharSequence queryString, @NotNull final Object... queryParameters )
	throws SQLException
	{
		return executeQuery( connection, GET_NUMBER, queryString, queryParameters );
	}

	/**
	 * Execute update query.
	 *
	 * @param dataSource Data source to execute query on.
	 * @param query      Update query to execute.
	 * @param arguments  Arguments used in the query.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeUpdate( @NotNull final DataSource dataSource, @NotNull final String query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( false );
			return executeUpdate( connection, query, arguments );
		}
	}

	/**
	 * Execute update query.
	 *
	 * @param connection Connection to database.
	 * @param query      Update query to execute.
	 * @param arguments  Arguments used in the query.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static int executeUpdate( @NotNull final Connection connection, @NotNull final String query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final PreparedStatement statement = connection.prepareStatement( query ) )
		{
			prepareStatement( statement, arguments );
			return statement.executeUpdate();
		}
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 *
	 * @param dataSource Data source to execute query on.
	 * @param processor  Result set processor.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 * @param <R>        Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static <R> R executeQuery( @NotNull final DataSource dataSource, @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( true );
			return executeQuery( connection, processor, query, arguments );
		}
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 *
	 * @param connection Connection to database.
	 * @param processor  Result set processor.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 * @param <R>        Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static <R> R executeQuery( @NotNull final Connection connection, @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final PreparedStatement statement = connection.prepareStatement( query.toString() ) )
		{
			prepareStatement( statement, arguments );
			return processor.process( statement.executeQuery() );
		}
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 * Rows are fetched and processed row-by-row.
	 *
	 * @param dataSource Data source to execute query on.
	 * @param processor  Result set processor.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 * @param <R>        Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static <R> R executeQueryStreaming( @NotNull final DataSource dataSource, @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( true );
			return executeQueryStreaming( connection, processor, query, arguments );
		}
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 * Rows are fetched and processed row-by-row.
	 *
	 * @param connection Connection to database.
	 * @param processor  Result set processor.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 * @param <R>        Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static <R> R executeQueryStreaming( @NotNull final Connection connection, @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		return executeQueryStreaming( connection, 100, processor, query, arguments );
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 * Rows are fetched and processed row-by-row.
	 *
	 * @param connection Connection to database.
	 * @param fetchSize  Fetch size to set. For MySQL/MariaDB, set this to
	 *                   {@link Integer#MIN_VALUE} to really enable streaming.
	 * @param processor  Result set processor.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 * @param <R>        Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static <R> R executeQueryStreaming( @NotNull final Connection connection, final int fetchSize, @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final PreparedStatement statement = connection.prepareStatement( query.toString() ) )
		{
			statement.setFetchSize( fetchSize );
			prepareStatement( statement, arguments );
			return processor.process( statement.executeQuery() );
		}
	}

	/**
	 * Execute query and return its result. The result is returned as a {@link
	 * ResultSetClone} instance which is detached from the database to prevent
	 * any synchronization issues.
	 *
	 * @param dataSource Data source to execute query on.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 *
	 * @return ResultSetClone with query result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@NotNull
	public static ResultSetClone executeQuery( @NotNull final DataSource dataSource, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final Connection connection = dataSource.getConnection() )
		{
			connection.setReadOnly( true );
			return executeQuery( connection, query, arguments );
		}
	}

	/**
	 * Execute query and return its result. The result is returned as a {@link
	 * ResultSetClone} instance which is detached from the database to prevent
	 * any synchronization issues.
	 *
	 * @param connection Connection to database.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 *
	 * @return ResultSetClone with query result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@NotNull
	public static ResultSetClone executeQuery( @NotNull final Connection connection, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		try ( final PreparedStatement statement = connection.prepareStatement( query.toString() ) )
		{
			prepareStatement( statement, arguments );
			return new ResultSetClone( statement.executeQuery() );
		}
	}

	/**
	 * Set arguments of prepared statement.
	 *
	 * @param statement Prepared statement.
	 * @param arguments Arguments to set.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static void prepareStatement( @NotNull final PreparedStatement statement, @NotNull final Object... arguments )
	throws SQLException
	{
		for ( int i = 0; i < arguments.length; i++ )
		{
			final Object object = arguments[ i ];
			statement.setObject( i + 1, ( object instanceof Enum<?> ) ? ( (Enum<?>)object ).name() : object );
		}
	}

	/**
	 * Returns a {@link ResultProcessor result processor} to wrap the given
	 * {@link ResultProcessor result processor} so that it will process the
	 * first row from an incoming result set.
	 *
	 * The following rules are when processing the incoming result set:
	 *
	 * <ol>
	 *
	 * <li>If the result set is empty, {@link NoSuchElementException} is
	 * thrown;</li>
	 *
	 * <li>If the result set returns a single row; that row is processed;</li>
	 *
	 * <li>If the result set returns multiple rows; only the first row is
	 * processed.</li>
	 *
	 * </ol>
	 *
	 * @param processor Processor for single row in {@link ResultSet}.
	 * @param <E>       Result type.
	 *
	 * @return {@link ResultProcessor}.
	 */
	public static <E> ResultProcessor<E> one( @NotNull final ResultProcessor<E> processor )
	{
		return resultSet -> {
			if ( !resultSet.next() )
			{
				throw new NoSuchElementException( "Got no result from query, while at least one result is required" );
			}

			return processor.process( resultSet );
		};
	}

	/**
	 * Returns a {@link ResultProcessor result processor} to wrap the given
	 * {@link ResultProcessor result processor} so that it will process the
	 * first row from an incoming result set.
	 *
	 * The following rules are when processing the incoming result set:
	 *
	 * <ol>
	 *
	 * <li>If the result set is empty, the given {@code defaultValue} is
	 * returned;</li>
	 *
	 * <li>If the result set returns a single row; that row is processed;</li>
	 *
	 * <li>If the result set returns multiple rows; only the first row is
	 * processed.</li>
	 *
	 * </ol>
	 *
	 * @param processor    Processor for single row in {@link ResultSet}.
	 * @param defaultValue Default to return if result set is empty.
	 * @param <E>          Result type.
	 *
	 * @return {@link ResultProcessor}.
	 */
	public static <E> ResultProcessor<E> one( @NotNull final ResultProcessor<E> processor, @Nullable final E defaultValue )
	{
		return resultSet -> resultSet.next() ? processor.process( resultSet ) : defaultValue;
	}

	/**
	 * Returns a {@link ResultProcessor result processor} to wrap the given
	 * {@link ResultProcessor result processor} so that it will process the one
	 * and only row from an incoming result set.
	 *
	 * The following rules are when processing the incoming result set:
	 *
	 * <ol>
	 *
	 * <li>If the result set is empty, {@link NoSuchElementException} is
	 * thrown;</li>
	 *
	 * <li>If the result set returns a single row; that row is processed;</li>
	 *
	 * <li>If the result set returns multiple rows, {@link
	 * IllegalStateException} is thrown;</li>
	 *
	 * </ol>
	 *
	 * @param processor Processor for single row in {@link ResultSet}.
	 * @param <E>       Result type.
	 *
	 * @return {@link ResultProcessor}.
	 */
	public static <E> ResultProcessor<E> only( @NotNull final ResultProcessor<E> processor )
	{
		return resultSet -> {
			if ( !resultSet.next() )
			{
				throw new NoSuchElementException( "Got no result from query, while one and only one result is required" );
			}

			final E result = processor.process( resultSet );

			if ( resultSet.next() )
			{
				throw new IllegalStateException( "Got multiple results from query, while one and only one is required" );
			}

			return result;
		};
	}

	/**
	 * Returns a {@link ResultProcessor result processor} to wrap the given
	 * {@link ResultProcessor result processor} so that it will be invoked for
	 * each row in the result set and its result objects collected.
	 *
	 * @param processor  Processor for row in {@link ResultSet} to produce
	 *                   element to collect.
	 * @param collection Target collection.
	 * @param <E>        Element type.
	 * @param <C>        Collection type.
	 *
	 * @return {@link ResultProcessor}.
	 */
	@NotNull
	public static <E, C extends Collection<? super E>> ResultProcessor<C> collect( @NotNull final ResultProcessor<E> processor, @NotNull final C collection )
	{
		return resultSet -> {
			while ( resultSet.next() )
			{
				collection.add( processor.process( resultSet ) );
			}

			return collection;
		};
	}

	/**
	 * Execute query and dump its result as a text table.
	 *
	 * @param dataSource Data source to execute query on.
	 * @param query      Query to be executed.
	 * @param arguments  Arguments used in the query.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public static void dumpAsTextTable( @NotNull final DataSource dataSource, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		executeQuery( dataSource, (ResultProcessor<Void>)resultSet -> {
			dumpAsTextTable( resultSet, System.out, "" );
			return null;
		}, query, arguments );
	}

	/**
	 * Write {@link ResultSet} as a text table.
	 *
	 * @param resultSet Result set to dump.
	 * @param out       Character stream to write to.
	 * @param indent    Line indent string (empty if not indenting).
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static void dumpAsTextTable( @NotNull final ResultSet resultSet, @NotNull final Appendable out, @NotNull final String indent )
	throws SQLException
	{
		final ResultSetMetaData metaData = resultSet.getMetaData();
		final int columnCount = metaData.getColumnCount();

		final List<String> headers = new ArrayList<>();
		for ( int i = 1; i <= columnCount; i++ )
		{
			headers.add( metaData.getColumnLabel( i ) );
		}

		final List<List<?>> data = new ArrayList<>();

		while ( resultSet.next() )
		{
			final List<Object> row = new ArrayList<>( columnCount );
			for ( int i = 1; i <= columnCount; i++ )
			{
				row.add( resultSet.getObject( i ) );
			}
			data.add( row );
		}

		try
		{
			TextTable.write( out, headers, data, indent, "\n", "NULL" );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Processor that takes a {@link ResultSet} and produces some result.
	 *
	 * @param <R> Result type.
	 */
	public interface ResultProcessor<R>
	{

		/**
		 * Process {@link ResultSet}.
		 *
		 * @param resultSet {@link ResultSet} to process.
		 *
		 * @return Processor result.
		 *
		 * @throws SQLException if an error occurs while accessing the
		 * database.
		 */
		R process( @NotNull ResultSet resultSet )
		throws SQLException;
	}
}
