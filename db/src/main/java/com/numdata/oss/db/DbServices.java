/*
 * Copyright (c) 2008-2021, Unicon Creation BV, The Netherlands.
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

import java.lang.reflect.*;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.*;

import com.numdata.oss.*;
import com.numdata.oss.db.JdbcTools.*;
import com.numdata.oss.ensemble.*;
import com.numdata.oss.log.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

/**
 * This class provides a database abstraction layer that should help Java
 * developers with accessing database records through the java introspection
 * mechanism.
 *
 * Database access is provided by a {@link DataSource}.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "JDBCPrepareStatementWithNonConstantString", "JDBCExecuteWithNonConstantString", "unused" } )
public class DbServices
{
	/**
	 * Generate warning in log about a slow query when a query takes longer than
	 * this number of seconds to execute (and process). If this value is zero or
	 * negative, slow query logging is disabled.
	 */
	private static final double SLOW_QUERY_THRESHOLD;

	static
	{
		double slowQueryThreshold = 60.0;
		try
		{
			//noinspection AccessOfSystemProperties
			final String value = System.getProperty( "slow.query.threshold" );
			if ( value != null )
			{
				slowQueryThreshold = Double.parseDouble( value );
			}
		}
		catch ( final RuntimeException e )
		{
			/* ignore no access to system property */
		}
		SLOW_QUERY_THRESHOLD = slowQueryThreshold;
	}

	/**
	 * SQL dialect to use.
	 */
	public enum SqlDialect
	{
		/**
		 * MySQL / MariaDb.
		 */
		MYSQL,

		/**
		 * HSQLDB.
		 */
		HSQLDB,

		/**
		 * Microsoft SQL server.
		 */
		MSSQL,

		/**
		 * Oracle.
		 */
		ORACLE,
	}

	/**
	 * Log used for database related messages.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( DbServices.class );

	/**
	 * This special object is used to store a 'NOW' in the database. This must
	 * be used to set a correct time in the database (the local system time
	 * should not be used!). The value is the earliest possible value for a
	 * {@code Date} object, roughly 300 million years BC.
	 */
	public static final Date NOW = new Date( Long.MIN_VALUE )
	{
		/**
		 * Serialized data version.
		 */
		private static final long serialVersionUID = 8861780300207263703L;

		@Override
		public String toString()
		{
			return "DbServices.NOW";
		}
	};

	/**
	 * Database pool to use for database services.
	 */
	@NotNull
	protected final DataSource _dataSource;

	/**
	 * Connection for the current transaction.
	 */
	@SuppressWarnings( "ThreadLocalNotStaticFinal" )
	@NotNull
	private final ThreadLocal<Connection> _transactionConnection = new ThreadLocal<Connection>();

	/**
	 * Cached/registered {@link ClassHandler} instances.
	 */
	private static final Map<Class<?>, ClassHandler> CLASS_HANDLERS = new HashMap<Class<?>, ClassHandler>();

	/**
	 * Dialect used by the database.
	 */
	@NotNull
	private SqlDialect _sqlDialect;

	/**
	 * Create database services using the specified data source.
	 *
	 * @param dataSource Data source to use.
	 */
	public DbServices( @NotNull final DataSource dataSource )
	{
		this( dataSource, SqlDialect.MYSQL );
	}

	/**
	 * Create database services using the specified data source.
	 *
	 * @param dataSource Data source to use.
	 * @param sqlDialect Dialect used by the database.
	 */
	public DbServices( @NotNull final DataSource dataSource, @NotNull final SqlDialect sqlDialect )
	{
		_dataSource = dataSource;
		_sqlDialect = sqlDialect;

		if ( sqlDialect == SqlDialect.HSQLDB )
		{
			/*
			 * Implement missing SQL functions.
			 */
			try
			{
				createHsqldbFunctionIfNotExists( "DATE", "CREATE FUNCTION DATE(t TIMESTAMP) RETURNS DATE\n" +
				                                         "LANGUAGE JAVA DETERMINISTIC NO SQL\n" +
				                                         "EXTERNAL NAME 'CLASSPATH:" + HsqlDbServices.class.getName() + ".mysqlDate'" );

				createHsqldbFunctionIfNotExists( "WEEKOFYEAR", "CREATE FUNCTION WEEKOFYEAR(t TIMESTAMP) RETURNS INT\n" +
				                                               "LANGUAGE JAVA DETERMINISTIC NO SQL\n" +
				                                               "EXTERNAL NAME 'CLASSPATH:" + HsqlDbServices.class.getName() + ".mysqlWeekOfYear'" );
			}
			catch ( final SQLException e )
			{
				throw new RuntimeException( "Failed to configure HSQLDB.", e );
			}
		}
	}

	/**
	 * Performs a shutdown on the underlying database, freeing any resources it
	 * uses.
	 */
	public void shutdown()
	{
		if ( getSqlDialect() == SqlDialect.HSQLDB )
		{
			try
			{
				final Connection connection = acquireConnection( false );
				try
				{
					JdbcTools.executeUpdate( connection, "SHUTDOWN" );
				}
				finally
				{
					releaseConnection( connection );
				}
			}
			catch ( final SQLException e )
			{
				LOG.error( "Failed to shutdown database: " + e, e );
			}
		}
	}

	@NotNull
	public SqlDialect getSqlDialect()
	{
		return _sqlDialect;
	}

	public void setSqlDialect( @NotNull final SqlDialect sqlDialect )
	{
		_sqlDialect = sqlDialect;
	}

	/**
	 * Get {@link ClassHandler} for a class.
	 *
	 * @param clazz Class to get handler for.
	 *
	 * @return {@link ClassHandler} for class.
	 */
	@NotNull
	protected static ClassHandler getClassHandler( @NotNull final Class<?> clazz )
	{
		ClassHandler result;

		final Map<Class<?>, ClassHandler> classHandlers = CLASS_HANDLERS;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized ( classHandlers )
		{
			result = classHandlers.get( clazz );
			if ( result == null )
			{
				final TableRecord dbClass = clazz.getAnnotation( TableRecord.class );
				if ( dbClass != null )
				{
					final String handlerImpl = dbClass.handlerImpl();
					if ( !handlerImpl.isEmpty() )
					{
						try
						{
							final Class<?> handlerClass = Class.forName( handlerImpl );
							result = (ClassHandler)handlerClass.getConstructor().newInstance();
						}
						catch ( final ClassNotFoundException e )
						{
							throw new RuntimeException( "Handler '" + handlerImpl + "' not found for class '" + clazz.getName() + '\'', e );
						}
						catch ( final InstantiationException | InvocationTargetException e )
						{
							throw new RuntimeException( "Failed to initialize handler '" + handlerImpl + "' for class '" + clazz.getName() + '\'', e );
						}
						catch ( final IllegalAccessException e )
						{
							throw new RuntimeException( "Access denied to handler '" + handlerImpl + "' for class '" + clazz.getName() + '\'', e );
						}
						catch ( final NoSuchMethodException e )
						{
							throw new RuntimeException( "Missing default '" + handlerImpl + "' handler constructor for class '" + clazz.getName() + '\'', e );
						}
					}
					else
					{
						result = new ReflectedClassHandler( clazz );
					}
				}
				else
				{
					result = new ReflectedClassHandler( clazz );
				}

				classHandlers.put( clazz, result );
			}
		}

		return result;
	}

	/**
	 * Get data source.
	 *
	 * @return Data source.
	 */
	@NotNull
	public DataSource getDataSource()
	{
		if ( isTransactionActive() )
		{
			throw new IllegalStateException( "Not allowed during a transaction." );
		}
		return _dataSource;
	}

	/**
	 * Get table name for a query.
	 *
	 * @param query Query to get table name for.
	 *
	 * @return Table name.
	 */
	@NotNull
	protected String getTableName( @NotNull final AbstractQuery<?> query )
	{
		String result = query.getTableName();
		if ( result == null )
		{
			final Class<?> tableClass = query.getTableClass();
			if ( tableClass == null )
			{
				throw new IllegalArgumentException( "Table name or class must be set" );
			}

			result = getTableName( tableClass );
		}
		return result;
	}

	/**
	 * Get table name for a class.
	 *
	 * @param tableClass Table class to get table name for.
	 *
	 * @return Table name.
	 */
	@NotNull
	public String getTableName( @NotNull final Class<?> tableClass )
	{
		final ClassHandler classHandler = getClassHandler( tableClass );
		return classHandler.getTableName();
	}

	/**
	 * Get record ID of the given record object.
	 *
	 * @param record Record to get id from.
	 *
	 * @return Record id; -1 if record has no id (or the id is actually -1).
	 */
	public long getRecordId( @NotNull final Object record )
	{
		final ClassHandler classHandler = getClassHandler( record.getClass() );
		return classHandler.hasRecordId() ? classHandler.getRecordId( record ) : -1L;
	}

	/**
	 * Get SQL function/variable used to get the current date and time. This
	 * depends on the type of SQL server.
	 *
	 * @return String containing SQL function/variable to get the current date
	 * and time.
	 */
	@SuppressWarnings( "WeakerAccess" )
	@NotNull
	public String getCurrentDateTimeFunction()
	{
		return "NOW()";
	}

	/**
	 * Create table in database.
	 *
	 * @param tableClass Class that defines the table.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws NullPointerException if {@code tableClass} is {@code null}.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void createTable( final Class<?> tableClass )
	throws SQLException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "createTable( " + tableClass + " )" );
		}

		final ClassHandler handler = getClassHandler( tableClass );
		String createStatement = handler.getCreateStatement();
		if ( getSqlDialect() == SqlDialect.HSQLDB )
		{
			createStatement = createStatement.replaceAll( "`", "" );
			createStatement = createStatement.replaceAll( "int\\(11\\)", "int" );
			createStatement = createStatement.replaceAll( "tinyint\\(1\\)", "bit" );
			createStatement = createStatement.replaceAll( "tinytext", "varchar(256)" );
			createStatement = createStatement.replaceAll( "mediumtext", "varchar(16777216)" );
			createStatement = createStatement.replaceAll( "['\"](\\d{1,2}:\\d{2})['\"]", "'$1:00'" ); // extend time value HH:MM to HH:MM:00
			createStatement = createStatement.replaceAll( " text([ ,])", " varchar(65536)$1" );
			createStatement = createStatement.replaceAll( "NOT NULL auto_increment", "GENERATED BY DEFAULT AS IDENTITY" );
			createStatement = createStatement.replaceAll( "UNIQUE KEY [^\\(]*(\\([^\\)]*\\))", "UNIQUE $1" );
			createStatement = createStatement.replaceAll( ",?\n\\s+KEY .*,?", "," );
			createStatement = createStatement.replaceAll( ",+\n\\);", "\n);" );
			createStatement = createStatement.replaceAll( "enum\\(.*\\)", "varchar(256)" );
			createStatement = createStatement.replaceAll( "(?i)(NOT NULL) (DEFAULT '[^']*')", "$2 $1" );
			createStatement = createStatement.replaceAll( "\\w*blob", "longvarbinary" );
			createStatement = createStatement.replaceAll( "NOT NULL DEFAULT CURRENT_TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP NOT NULL" );
			createStatement = createStatement.replaceAll( "ON UPDATE CURRENT_TIMESTAMP", "" ); // not supported by Hsqldb
			createStatement = createStatement.replaceAll( "(?i)DEFAULT '(-?\\d[\\d.]*)'", "DEFAULT $1" ); // Hsqldb 2.3.4 is picky default numbers
		}

		LOG.debug( "Creating table with statement:\n" + createStatement );
		final Connection connection = acquireConnection( false );
		try
		{
			JdbcTools.executeUpdate( connection, createStatement );
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Drop table in database.
	 *
	 * @param tableClass Class that defines the table.
	 *
	 * @throws SQLException the table could not be dropped (due to a database
	 * error or invalid query).
	 */
	public void dropTable( final Class<?> tableClass )
	throws SQLException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "dropTable( " + tableClass + " )" );
		}

		final String tableName = getTableName( tableClass );

		final Connection connection = acquireConnection( false );
		try
		{
			JdbcTools.executeUpdate( connection, "DROP TABLE " + tableName );
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Returns whether a table exists in the database for the given table
	 * class.
	 *
	 * @param dbClass Table class.
	 *
	 * @return {@code true} if the table exists; {@code false} if it doesn't or
	 * an error occurred.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public boolean tableExists( @NotNull final Class<?> dbClass )
	throws SQLException
	{
		final String tableName = getTableName( dbClass );

		final Connection connection = acquireConnection( true );
		try
		{
			return JdbcTools.tableExists( connection, tableName );
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Execute delete query.
	 *
	 * @param deleteQuery {@link DeleteQuery} to execute.
	 *
	 * @return Number of rows that were deleted (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public int executeDelete( @NotNull final DeleteQuery<?> deleteQuery )
	throws SQLException
	{
		final String tableName = getTableName( deleteQuery );
		final String queryString = deleteQuery.getQueryString( tableName );
		final Object[] queryParameters = deleteQuery.getQueryParameters();

		final Connection connection = acquireConnection( false );
		try
		{
			final long start = System.nanoTime();
			final int result = JdbcTools.executeUpdate( connection, queryString, queryParameters );
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeDelete() time=" + ( ( System.nanoTime() - start ) / 1000000L ) / 1000.0 + "s, query='" + queryString + "', parameters=" + Arrays.toString( queryParameters ) );
			}
			return result;
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeDelete() FAILED query '" + queryString + "' with parameters " + Arrays.toString( queryParameters ) + " => " + e.getMessage(), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "executeDelete() FAILED query '" + queryString + "' with parameters " + Arrays.toString( queryParameters ) + " => " + e.getMessage(), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Execute update query.
	 *
	 * @param updateQuery {@link UpdateQuery} to execute.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public int executeUpdate( @NotNull final UpdateQuery<?> updateQuery )
	throws SQLException
	{
		final String tableName = getTableName( updateQuery );
		final String queryString = updateQuery.getQueryString( tableName );
		final Object[] queryParameters = updateQuery.getQueryParameters();
		return executeUpdate( queryString, queryParameters );
	}

	/**
	 * Execute update query.
	 *
	 * @param queryString     Query to be executed.
	 * @param queryParameters Query parameters used in the query.
	 *
	 * @return Number of rows that were updated (may be {@code 0}).
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public int executeUpdate( @NotNull final String queryString, @NotNull final Object... queryParameters )
	throws SQLException
	{
		final Connection connection = acquireConnection( false );
		try
		{
			final long start = System.nanoTime();
			final int result = JdbcTools.executeUpdate( connection, queryString, queryParameters );
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeUpdate() time=" + ( ( System.nanoTime() - start ) / 1000000L ) / 1000.0 + "s, result=" + result + ", query='" + queryString + "', parameters=" + Arrays.toString( queryParameters ) );
			}
			return result;
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeUpdate() FAILED query '" + queryString + "' with parameters " + Arrays.toString( queryParameters ) + " => " + e.getMessage(), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "executeUpdate() FAILED query '" + queryString + "' with parameters " + Arrays.toString( queryParameters ) + " => " + e.getMessage(), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Get number result from single-column query.
	 *
	 * @param query SELECT query to execute.
	 *
	 * @return Resulting number; {@code null} if query returned empty or NULL
	 * result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@Nullable
	public Number selectNumber( @NotNull final SelectQuery<?> query )
	throws SQLException
	{
		return executeQuery( JdbcTools.GET_NUMBER, query );
	}

	/**
	 * Get integer result from single-column query.
	 *
	 * @param selectQuery  SELECT query to execute.
	 * @param defaultValue Default value to return.
	 *
	 * @return Resulting number; {@code defaultValue} if query returned empty or
	 * NULL result.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public int selectInt( @NotNull final SelectQuery<?> selectQuery, final int defaultValue )
	throws SQLException
	{
		final Integer result = executeQuery( JdbcTools.GET_INTEGER, selectQuery );
		return result == null ? defaultValue : result;
	}

	/**
	 * Execute a {@link SelectQuery} and return the results as a {@link List}.
	 *
	 * @param selectQuery SELECT query to execute.
	 * @param <DbObject>  Database object type.
	 *
	 * @return List with result set.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@NotNull
	public <DbObject> List<DbObject> retrieveList( @NotNull final SelectQuery<DbObject> selectQuery )
	throws SQLException
	{
		return retrieveList( selectQuery.getTableClass(), selectQuery.getQueryString( getTableName( selectQuery ) ), selectQuery.getQueryParameters() );
	}

	/**
	 * Execute query and specify the result object type. If any arguments are
	 * provided, they are passed to a prepared statement that is then used to
	 * perform the query.
	 *
	 * @param dbClass    Result set record object.
	 * @param query      SQL query to execute.
	 * @param arguments  Arguments used in the query.
	 * @param <DbObject> Database object type.
	 *
	 * @return List with result set.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws NullPointerException if an argument is {@code null}.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	@NotNull
	public <DbObject> List<DbObject> retrieveList( @NotNull final Class<DbObject> dbClass, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "retrieveList() query='" + query + "', arguments: " + Arrays.toString( arguments ) );
		}

		final ResultProcessor<List<DbObject>> processor = new ObjectListConverter<DbObject>( dbClass );

		final Connection connection = acquireConnection( true );
		try
		{
			if ( !isTransactionActive() )
			{
				connection.setReadOnly( true );
			}

			final long start = System.nanoTime();
			final List<DbObject> result = JdbcTools.executeQuery( connection, processor, query, arguments );
			logSlowQuery( start, "retrieveList()", dbClass, result, query, arguments );
			return result;
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "retrieveList() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "retrieveList() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Checks the duration of a query and logs any queries slower than the
	 * {@link #SLOW_QUERY_THRESHOLD}.
	 *
	 * @param start       Start of the query.
	 * @param source      Name of the method that
	 * @param dbClass     Database record class, if known/applicable.
	 * @param queryResult Query result, if any.
	 * @param query       Executed SQL query.
	 * @param arguments   Arguments used in the query.
	 * @param <DbObject>  Database object type.
	 */
	private static <DbObject> void logSlowQuery( final long start, final String source, @Nullable final Class<DbObject> dbClass, @Nullable final Object queryResult, @Nullable final CharSequence query, @NotNull final Object[] arguments )
	{
		final boolean debugEnabled = LOG.isDebugEnabled();
		if ( debugEnabled || ( SLOW_QUERY_THRESHOLD > 0 ) )
		{
			final double seconds = TimeUnit.NANOSECONDS.toMillis( System.nanoTime() - start ) / 1000.0;
			if ( seconds > SLOW_QUERY_THRESHOLD )
			{
				final String message = source + " SLOW QUERY: time=" + seconds + "s, result=" + describeQueryResult( dbClass, queryResult ) + ", query='" + query + "', parameters=" + Arrays.toString( arguments );
				LOG.warn( message, new DatabaseException( message ) );
			}
			else if ( debugEnabled )
			{
				LOG.debug( source + " time=" + seconds + "s, result=" + describeQueryResult( dbClass, queryResult ) + ", query='" + query + "', parameters=" + Arrays.toString( arguments ) );
			}
		}
	}

	/**
	 * Returns a short description of the given query result, for logging.
	 *
	 * @param dbClass     Database record class, if known/applicable.
	 * @param queryResult Query result, if any.
	 * @param <DbObject>  Database object type.
	 *
	 * @return Description of the query result.
	 */
	private static <DbObject> String describeQueryResult( @Nullable final Class<DbObject> dbClass, @Nullable final Object queryResult )
	{
		final String result;
		if ( ( dbClass == null ) && ( queryResult == null ) )
		{
			result = "n/a";
		}
		else if ( queryResult == null )
		{
			result = "(" + dbClass.getSimpleName() + ")null";
		}
		else if ( queryResult instanceof Collection )
		{
			final Collection<?> collection = (Collection<?>)queryResult;
			result = ( dbClass == null ? "" : dbClass.getSimpleName() ) + "[" + collection.size() + "]";
		}
		else if ( queryResult instanceof SingleObjectConverter )
		{
			final SingleObjectConverter<DbObject> singleObjectConverter = (SingleObjectConverter<DbObject>)queryResult;
			result = ( dbClass == null ? "" : dbClass.getSimpleName() ) + "(" + ( singleObjectConverter.getFirst() != null ? ( singleObjectConverter.isMultiple() ? "multiple" : "single" ) : "empty" ) + ")";
		}
		else
		{
			result = ( dbClass == null ? "" : "(" + dbClass.getSimpleName() + ")" ) + queryResult;
		}
		return result;
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 *
	 * @param processor Result set processor.
	 * @param query     Query to be executed.
	 * @param <R>       Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public <R> R executeQuery( @NotNull final ResultProcessor<R> processor, @NotNull final SelectQuery<?> query )
	throws SQLException
	{
		return executeQuery( processor, query.getQueryString( getTableName( query ) ), query.getQueryParameters() );
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 *
	 * @param processor Result set processor.
	 * @param query     Query to be executed.
	 * @param arguments Arguments used in the query.
	 * @param <R>       Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public <R> R executeQuery( @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "executeQuery() query='" + query + "', arguments: " + Arrays.toString( arguments ) );
		}

		final Connection connection = acquireConnection( true );
		try
		{
			final long start = System.nanoTime();
			final R result = JdbcTools.executeQuery( connection, processor, query, arguments );
			logSlowQuery( start, "executeQuery()", null, null, query, arguments );
			return result;
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeQuery() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "executeQuery() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Execute query and feed its result set through a {@link ResultProcessor}.
	 *
	 * @param processor Result set processor.
	 * @param query     Query to be executed.
	 * @param arguments Arguments used in the query.
	 * @param <R>       Result type.
	 *
	 * @return Result of processor.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public <R> R executeQueryStreaming( @NotNull final ResultProcessor<R> processor, @NotNull final CharSequence query, @NotNull final Object... arguments )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "executeQueryStreaming() query='" + query + "', arguments: " + Arrays.toString( arguments ) );
		}

		final Connection connection = acquireConnection( true );
		try
		{
			final long start = System.nanoTime();
			final R result = JdbcTools.executeQueryStreaming( connection, getSqlDialect() == SqlDialect.MYSQL ? Integer.MIN_VALUE : 100, processor, query, arguments );
			logSlowQuery( start, "executeQueryStreaming()", null, null, query, arguments );
			return result;
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "executeQueryStreaming() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "executeQueryStreaming() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Execute a {@link SelectQuery} that returns exactly one result or none at
	 * all.
	 *
	 * @param selectQuery SELECT query to execute.
	 * @param <DbObject>  Database object type.
	 *
	 * @return Result object; {@code null} if the result is empty.
	 *
	 * @throws SQLException if an error occurs while accessing the database or
	 * multiple results were returned).
	 */
	@Nullable
	public <DbObject> DbObject retrieveObject( @NotNull final SelectQuery<DbObject> selectQuery )
	throws SQLException
	{
		final Class<DbObject> dbClass = selectQuery.getTableClass();
		final CharSequence query = selectQuery.getQueryString( getTableName( selectQuery ) );
		final Object[] arguments = selectQuery.getQueryParameters();

		final ResultProcessor<SingleObjectConverter<DbObject>> processor = new SingleObjectConverter<DbObject>( dbClass );

		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "retrieveObject() query='" + query + "', arguments: " + Arrays.toString( arguments ) );
		}

		final SingleObjectConverter<DbObject> result;

		final Connection connection = acquireConnection( true );
		try
		{
			final long start = System.nanoTime();
			result = JdbcTools.executeQuery( connection, processor, query, arguments );
			logSlowQuery( start, "retrieveObject()", dbClass, result, query, arguments );
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "retrieveObject() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "retrieveObject() FAILED query '" + query + "' with parameters " + Arrays.toString( arguments ), e );
			throw e;
		}
		finally
		{
			releaseConnection( connection );
		}

		if ( result.isMultiple() )
		{
			throw new SQLException( "Got multiple results on query: " + selectQuery.getQueryString( getTableName( selectQuery ) ) + " (with arguments " + Arrays.toString( selectQuery.getQueryParameters() ) + ')' );
		}

		return result.getFirst();
	}

	/**
	 * Refresh the state of the instance from the database, overwriting changes
	 * made to the entity, if any.
	 *
	 * @param object     Object to refresh.
	 * @param <DbObject> Database object type.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public <DbObject> void refresh( @NotNull final DbObject object )
	throws SQLException
	{
		final Class<?> dbClass = object.getClass();

		final ClassHandler classHandler = getClassHandler( dbClass );
		if ( !classHandler.hasRecordId() )
		{
			throw new IllegalArgumentException( "Can't refresh object without record id: " + object );
		}

		final SelectQuery<DbObject> select = new SelectQuery<DbObject>( (Class<DbObject>)dbClass );
		select.whereEqual( classHandler.getRecordIdColumn(), classHandler.getRecordId( object ) );
		final String query = select.getQueryString();
		final Object[] arguments = select.getQueryParameters();

		try
		{
			final Connection connection = acquireConnection( true );
			try
			{
				final PreparedStatement statement = connection.prepareStatement( query );

				try
				{
					JdbcTools.prepareStatement( statement, arguments );
					final ResultSet resultSet = statement.executeQuery();

					try
					{
						if ( !resultSet.next() )
						{
							throw new SQLException( "Object not found in database: " + query + " (with arguments " + Arrays.toString( arguments ) + ')' );
						}

						final ResultSetMetaData metaData = resultSet.getMetaData();

						final FieldHandler[] fieldHandlers = new FieldHandler[ metaData.getColumnCount() ];

						for ( int columnIndex = 0; columnIndex < fieldHandlers.length; columnIndex++ )
						{
							final String column = metaData.getColumnLabel( columnIndex + 1 );
							fieldHandlers[ columnIndex ] = classHandler.getFieldHandlerForColumn( column );
						}

						for ( int columnIndex = 0; columnIndex < fieldHandlers.length; columnIndex++ )
						{
							final FieldHandler field = fieldHandlers[ columnIndex ];
							if ( field != null )
							{
								field.getColumnData( object, resultSet, columnIndex + 1 );
							}
						}

						if ( resultSet.next() )
						{
							throw new SQLException( "Got multiple results on query: " + query + " (with arguments " + Arrays.toString( arguments ) + ')' );
						}
					}
					finally
					{
						try
						{
							resultSet.close();
						}
						catch ( final SQLException ignored )
						{
							/* ignored, would hide real exception */
						}
					}
				}
				finally
				{
					try
					{
						statement.close();
					}
					catch ( final SQLException ignored )
					{
						/* ignored, would hide real exception */
					}
				}
			}
			finally
			{
				releaseConnection( connection );
			}
		}
		catch ( final SQLTransientException e )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Failed query: " + query, e );
			}
			throw e;
		}
		catch ( final SQLException e )
		{
			LOG.error( "Failed query: " + query, e );
			throw e;
		}
	}

	/**
	 * Stores a single database record in the database. An INSERT or UPDATE
	 * query is generated depending on the value of the 'ID' field.
	 *
	 * @param object Object to store in the database.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void storeObject( @NotNull final Object object )
	throws SQLException
	{
		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );

		if ( classHandler.hasRecordId() && ( classHandler.getRecordId( object ) >= 0L ) )
		{
			updateObject( object );
		}
		else
		{
			insertObjectImpl( object );
		}
	}

	/**
	 * Updates a single object in the database, setting all fields.
	 *
	 * @param object Object to be updated in the database.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void updateObject( @NotNull final Object object )
	throws SQLException
	{
		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );

		updateObjectImpl( object, classHandler.getFieldHandlers() );
	}

	/**
	 * Updates a single object in the database, setting only the values for the
	 * specified fields. All other field values are left as-is.
	 *
	 * To update all fields of an object, use {@link #updateObject(Object)}
	 * instead.
	 *
	 * @param object     Object to be updated in the database.
	 * @param fieldNames Names of the fields to be updated.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void updateObject( @NotNull final Object object, @NotNull final String... fieldNames )
	throws SQLException
	{
		updateObject( object, Arrays.asList( fieldNames ) );
	}

	/**
	 * Updates a single object in the database, setting only the values for the
	 * specified fields. All other field values are left as-is.
	 *
	 * To update all fields of an object, use {@link #updateObject(Object)}
	 * instead.
	 *
	 * @param object     Object to be updated in the database.
	 * @param fieldNames Names of the fields to be updated.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void updateObject( @NotNull final Object object, @NotNull final Collection<String> fieldNames )
	throws SQLException
	{
		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );

		final List<FieldHandler> fieldHandlers = new ArrayList<FieldHandler>( fieldNames.size() );

		for ( final String fieldName : fieldNames )
		{
			final FieldHandler fieldHandler = classHandler.getFieldHandlerForColumn( fieldName );
			if ( fieldHandler == null )
			{
				throw new IllegalArgumentException( "Unknown field: " + fieldName );
			}

			fieldHandlers.add( fieldHandler );
		}

		updateObjectImpl( object, fieldHandlers );
	}

	/**
	 * Updates a single object in the database, setting only the values for the
	 * specified fields. All other field values are left as-is.
	 *
	 * @param object        Object to be updated in the database.
	 * @param fieldHandlers Fields to be updated.
	 *
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	protected void updateObjectImpl( @NotNull final Object object, @NotNull final List<FieldHandler> fieldHandlers )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "updateObjectImpl( " + object + ", " + fieldHandlers + " )" );
		}

		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );
		final String tableName = getTableName( objectClass );

		final String recordIdColumn = classHandler.getRecordIdColumn();

		final long recordId = classHandler.getRecordId( object );
		if ( recordId < 0L )
		{
			throw new IllegalArgumentException( "recordId: " + recordId );
		}

		final StringBuilder query = new StringBuilder();
		query.append( "UPDATE " );
		query.append( tableName );
		query.append( " SET " );

		final List<FieldHandler> nowFields = new ArrayList<FieldHandler>();

		boolean haveOne = false;
		for ( final FieldHandler fieldHandler : fieldHandlers )
		{
			final String fieldName = fieldHandler.getName();
			if ( !fieldName.equals( recordIdColumn ) )
			{
				if ( haveOne )
				{
					query.append( ',' );
				}

				if ( NOW.equals( fieldHandler.getFieldValue( object ) ) )
				{
					query.append( fieldName );
					query.append( '=' );
					query.append( getCurrentDateTimeFunction() );
					nowFields.add( fieldHandler );
				}
				else
				{
					query.append( fieldName );
					query.append( "=?" );
				}

				haveOne = true;
			}
		}

		query.append( " WHERE " );
		query.append( recordIdColumn );
		query.append( '=' );
		query.append( recordId );

		if ( !haveOne )
		{
			throw new IllegalArgumentException( "Nothing to update: object=" + object + ", fieldHandlers=" + fieldHandlers );
		}

		final Connection connection = acquireConnection( false );
		try
		{
			final String queryString = query.toString();
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "updateObjectImpl() query: " + queryString );
			}

			final PreparedStatement preparedStatement = connection.prepareStatement( queryString );
			try
			{
				int columnIndex = 1;

				for ( final FieldHandler fieldHandler : fieldHandlers )
				{
					final String fieldName = fieldHandler.getName();
					if ( !fieldName.equals( recordIdColumn ) && !NOW.equals( fieldHandler.getFieldValue( object ) ) )
					{
						fieldHandler.setColumnData( object, preparedStatement, columnIndex++ );
					}
				}

				/*
				 * UPDATE should generate exactly 1 update.
				 */
				final int updateCount = preparedStatement.executeUpdate();
				if ( updateCount != 1 )
				{
					throw new SQLException( "updateCount: " + updateCount + ", query: " + queryString );
				}
			}
			finally
			{
				try
				{
					preparedStatement.close();
				}
				catch ( final SQLException ignored )
				{
					/* ignored, would hide real exception */
				}
			}

			if ( !nowFields.isEmpty() )
			{
				updateNowFields( object, nowFields, connection );
			}
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * This internal method is used to prepare a statement to store an object in
	 * the database.
	 *
	 * @param object Object to store.
	 *
	 * @throws SQLException if an error occurred while preparing the statement.
	 * @throws IllegalArgumentException if no table name is defined for the
	 * given object's type.
	 */
	protected void insertObjectImpl( @NotNull final Object object )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "insertObjectImpl( " + object + " )" );
		}

		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );
		final boolean hasRecordId = classHandler.hasRecordId();
		final String skipField = ( hasRecordId && ( classHandler.getRecordId( object ) < 0L ) ) ? classHandler.getRecordIdColumn() : null;

		final List<FieldHandler> fieldHandlers = classHandler.getFieldHandlers();

		final String tableName = getTableName( objectClass );

		final StringBuilder query = new StringBuilder();
		query.append( "INSERT INTO " );
		query.append( tableName );
		query.append( " (" );

		boolean first = true;

		for ( final FieldHandler fieldHandler : fieldHandlers )
		{
			final String fieldName = fieldHandler.getName();
			if ( !fieldName.equals( skipField ) )
			{
				if ( !first )
				{
					query.append( ',' );
				}

				query.append( fieldName );
				first = false;
			}
		}

		query.append( ") VALUES (" );

		first = true;

		final List<FieldHandler> nowFields = new ArrayList<FieldHandler>();

		for ( final FieldHandler fieldHandler : fieldHandlers )
		{
			final String fieldName = fieldHandler.getName();
			if ( !fieldName.equals( skipField ) )
			{
				if ( !first )
				{
					query.append( ',' );
				}

				if ( NOW.equals( fieldHandler.getFieldValue( object ) ) )
				{
					query.append( getCurrentDateTimeFunction() );
					nowFields.add( fieldHandler );
				}
				else
				{
					query.append( '?' );
				}

				first = false;
			}
		}

		query.append( ')' );

		final Connection connection = acquireConnection( false );
		try
		{
			final String queryString = query.toString();
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Prepare/execute: " + queryString );
			}

			final PreparedStatement preparedStatement = connection.prepareStatement( queryString, Statement.RETURN_GENERATED_KEYS );
			try
			{
				int columnIndex = 1;
				for ( final FieldHandler fieldHandler : fieldHandlers )
				{
					final String fieldName = fieldHandler.getName();
					if ( !fieldName.equals( skipField ) && !NOW.equals( fieldHandler.getFieldValue( object ) ) )
					{
						fieldHandler.setColumnData( object, preparedStatement, columnIndex++ );
					}
				}

				/*
				 * INSERT should generate exactly 1 update.
				 */
				final int updateCount = preparedStatement.executeUpdate();
				if ( updateCount != 1 )
				{
					throw new SQLException( "updateCount: " + updateCount + ", query: " + queryString );
				}

				if ( hasRecordId )
				{
					final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
					try
					{
						if ( !generatedKeys.next() )
						{
							throw new SQLException( "Generated record id was not returned by database." );
						}
						classHandler.setRecordId( object, generatedKeys.getLong( 1 ) );
					}
					finally
					{
						generatedKeys.close();
					}
				}
			}
			finally
			{
				try
				{
					preparedStatement.close();
				}
				catch ( final SQLException ignored )
				{
					/* ignored, would hide real exception */

				}
			}

			if ( !nowFields.isEmpty() )
			{
				updateNowFields( object, nowFields, connection );
			}
		}
		finally
		{
			releaseConnection( connection );
		}
	}

	/**
	 * Deletes a single database record in the database. A DELETE query is
	 * generated depending on the value of the 'ID' field. Afterwards the 'ID'
	 * field is set to {@code -1}.
	 *
	 * @param object Object to store in the database.
	 *
	 * @throws IllegalArgumentException if reflection problems occur.
	 * @throws SQLException the query could not be executed (due to a database
	 * error or invalid query).
	 */
	public void deleteObject( @NotNull final Object object )
	throws SQLException
	{
		final Class<?> objectClass = object.getClass();
		final ClassHandler classHandler = getClassHandler( objectClass );

		if ( classHandler.hasRecordId() )
		{
			final long recordId = classHandler.getRecordId( object );
			if ( recordId >= 0L )
			{
				final DeleteQuery<Object> query = new DeleteQuery<Object>( classHandler.getTableName() );
				query.whereEqual( classHandler.getRecordIdColumn(), recordId );
				executeDelete( query );
				classHandler.setRecordId( object, -1L );
			}
		}
	}

	/**
	 * This method is called from the {@link #updateObjectImpl} and {@link
	 * #insertObjectImpl} methods to update the value of {@link #NOW} fields
	 * from the database.
	 *
	 * @param object     Object that was just updated in the database.
	 * @param nowFields  Fields that were set to {@link #NOW}.
	 * @param connection Database connection to use.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	private void updateNowFields( @NotNull final Object object, @NotNull final List<FieldHandler> nowFields, @NotNull final Connection connection )
	throws SQLException
	{
		final Class<?> objectClass = object.getClass();
		final String tableName = getTableName( objectClass );
		final ClassHandler classHandler = getClassHandler( objectClass );
		final long recordId = classHandler.getRecordId( object );
		final String recordIdColumn = classHandler.getRecordIdColumn();

		final StringBuilder query = new StringBuilder();
		query.setLength( 0 );
		query.append( "SELECT " );
		for ( int i = 0; i < nowFields.size(); i++ )
		{
			if ( i > 0 )
			{
				query.append( ',' );
			}
			query.append( nowFields.get( i ).getName() );
		}
		query.append( " FROM " );
		query.append( tableName );
		query.append( " WHERE " );
		query.append( recordIdColumn );
		query.append( '=' );
		query.append( recordId );

		final Statement statement = connection.createStatement();
		try
		{
			statement.execute( query.toString() );
			final ResultSet resultSet = statement.getResultSet();
			if ( ( resultSet != null ) && resultSet.next() )
			{
				for ( int i = 0; i < nowFields.size(); i++ )
				{
					nowFields.get( i ).getColumnData( object, resultSet, i + 1 );

					if ( i > 0 )
					{
						query.append( ',' );
					}
					query.append( nowFields.get( i ).getName() );
				}
			}
		}
		finally
		{
			try
			{
				statement.close();
			}
			catch ( final SQLException e )
			{
				/* ignored, would hide real exception */
				LOG.warn( "Ignored: " + e, e );
			}
		}
	}

	/**
	 * Acquires a connection for the current transaction or a single query (if
	 * no transaction is active).
	 *
	 * @param readOnly Whether to get a read-only connection.
	 *
	 * @return Database connection.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@SuppressWarnings( "JDBCResourceOpenedButNotSafelyClosed" )
	@NotNull
	protected Connection acquireConnection( final boolean readOnly )
	throws SQLException
	{
		Connection result = getTransactionConnection();
		if ( result == null )
		{
			final DataSource dataSource = getDataSource();
			result = dataSource.getConnection();
			result.setReadOnly( readOnly );
		}
		return result;
	}

	/**
	 * Releases a connection for a single query. If a transaction is active, the
	 * connection is not released until the transaction is committed.
	 *
	 * @param connection Database connection.
	 */
	protected void releaseConnection( @NotNull final Connection connection )
	{
		if ( getTransactionConnection() == null )
		{
			try
			{
				connection.close();
			}
			catch ( final SQLException ignored )
			{
				/* ignored, would hide real exception */
			}
		}
	}

	/**
	 * Starts a transaction.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void startTransaction()
	throws SQLException
	{
		LOG.entering( "startTransaction" );

		final Connection transactionConnection = getTransactionConnection();
		if ( transactionConnection != null )
		{
			throw new SQLException( "Another transaction is already active" );
		}

		final Connection connection = _dataSource.getConnection();
		connection.setReadOnly( false );
		connection.setAutoCommit( false );
		_transactionConnection.set( connection );
	}

	/**
	 * Starts a transaction with the specified isolation level.
	 *
	 * @param level Transaction isolation level; see {@link Connection}.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void startTransaction( @MagicConstant( valuesFromClass = Connection.class ) final int level )
	throws SQLException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.entering( "startTransaction", "level=" + level );
		}

		startTransaction();
		final Connection connection = getTransactionConnectionNotNull();
		connection.setTransactionIsolation( level );
	}

	/**
	 * Commits the current transaction.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void commit()
	throws SQLException
	{
		LOG.entering( "commit" );

		final Connection connection = getTransactionConnectionNotNull();
		try
		{
			connection.commit();
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch ( final SQLException ignored )
			{
				/* ignored, would hide real exception */
			}
			_transactionConnection.remove();
		}
	}

	/**
	 * Performs a rollback of the current transaction.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void rollback()
	throws SQLException
	{
		LOG.entering( "rollback" );

		final Connection connection = getTransactionConnectionNotNull();
		try
		{
			connection.rollback();
		}
		finally
		{
			try
			{
				connection.close();
			}
			catch ( final SQLException ignored )
			{
				/* ignored, would hide real exception */
			}
			_transactionConnection.remove();
		}
	}

	/**
	 * Performs a rollback of the current transaction to the given savepoint.
	 *
	 * @param savepoint Savepoint to rollback.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void rollback( final Savepoint savepoint )
	throws SQLException
	{
		LOG.entering( "rollback", savepoint );
		final Connection connection = getTransactionConnectionNotNull();
		connection.rollback( savepoint );
	}

	/**
	 * Creates an unnamed savepoint in the current transaction and returns the
	 * object that represents it.
	 *
	 * @return Created savepoint.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public Savepoint setSavepoint()
	throws SQLException
	{
		LOG.entering( "setSavepoint" );
		return getTransactionConnectionNotNull().setSavepoint();
	}

	/**
	 * Creates a savepoint with the given name in the current transaction and
	 * returns the object that represents it.
	 *
	 * @param name Name of the savepoint.
	 *
	 * @return Created savepoint.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public Savepoint setSavepoint( final String name )
	throws SQLException
	{
		LOG.entering( "setSavepoint", name );
		return getTransactionConnectionNotNull().setSavepoint( name );
	}

	/**
	 * Removes the specified savepoint and subsequent savepoint objects from the
	 * current transaction.
	 *
	 * @param savepoint Savepoint to release.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void releaseSavepoint( @NotNull final Savepoint savepoint )
	throws SQLException
	{
		LOG.entering( "releaseSavepoint", savepoint );
		getTransactionConnectionNotNull().releaseSavepoint( savepoint );
	}

	/**
	 * Returns the database connection for the current transaction.
	 *
	 * @return Database connection.
	 */
	@Nullable
	private Connection getTransactionConnection()
	{
		return _transactionConnection.get();
	}

	/**
	 * Returns the database connection for the current transaction.
	 *
	 * @return Database connection.
	 *
	 * @throws SQLException if there is no current transaction.
	 */
	@NotNull
	private Connection getTransactionConnectionNotNull()
	throws SQLException
	{
		final Connection connection = getTransactionConnection();

		if ( connection == null )
		{
			throw new SQLException( "No transaction in progress" );
		}

		if ( connection.getAutoCommit() )
		{
			throw new SQLException( "Connection is in auto-commit mode" );
		}

		return connection;
	}

	/**
	 * Returns whether a transaction is active.
	 *
	 * @return {@code true} if a transaction is active. v
	 */
	@SuppressWarnings( "WeakerAccess" )
	public boolean isTransactionActive()
	{
		boolean result = false;

		final Connection connection = getTransactionConnection();
		if ( connection != null )
		{
			try
			{
				result = !connection.isClosed();
			}
			catch ( final SQLException ignored )
			{
				/* ignored, would hide real exception */
			}
		}

		return result;
	}

	/**
	 * Executes the given operations within a database transaction.
	 *
	 * @param body Operations to perform within a transaction.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void transaction( final TransactionBody body )
	throws SQLException
	{
		transaction( body, Connection.TRANSACTION_SERIALIZABLE );
	}

	/**
	 * Executes the given operations within a database transaction.
	 *
	 * @param body  Operations to perform within a transaction.
	 * @param level Transaction isolation level; see {@link Connection}.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void transaction( final TransactionBody body, @MagicConstant( valuesFromClass = Connection.class ) final int level )
	throws SQLException
	{
		startTransaction( level );
		try
		{
			body.execute();
			commit();
		}
		finally
		{
			if ( isTransactionActive() )
			{
				try
				{
					rollback();
				}
				catch ( final SQLException e )
				{
					LOG.warn( "Rollback failed: " + e );
				}
			}
		}
	}

	/**
	 * Executes the given operations within a database transaction. If the
	 * transaction fails due to an automatic rollback (indicated by {@link
	 * SQLTransactionRollbackException}), repeated attempts will be made until
	 * the transaction succeeds (or until 100 attempts have failed). There will
	 * be a short random delay (10 to 100 ms) between attempts.
	 *
	 * @param body Operations to perform within a transaction.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@SuppressWarnings( "MethodWithMultipleReturnPoints" )
	public void transactionWithRetry( final TransactionBody body )
	throws SQLException
	{
		final int maximumAttempts = 100;
		for ( int attempt = 1; attempt <= maximumAttempts; attempt++ )
		{
			Exception rollbackException = null;
			try
			{
				transaction( body );
				return;
			}
			catch ( final RuntimeException e )
			{
				for ( Throwable cause = e.getCause(); cause != null; cause = cause.getCause() )
				{
					if ( cause instanceof SQLTransactionRollbackException )
					{
						rollbackException = e;
						break;
					}
				}

				if ( rollbackException == null )
				{
					throw e;
				}
			}
			catch ( final SQLTransactionRollbackException e )
			{
				rollbackException = e;
			}

			if ( attempt == maximumAttempts )
			{
				throw new SQLException( "Transaction failed after trying " + maximumAttempts + " times", rollbackException );
			}

			LOG.trace( "Will retry transaction " + body + " after " + rollbackException );
			try
			{
				//noinspection UnsecureRandomNumberGeneration,BusyWait
				Thread.sleep( (long)( Math.random() * 90.0 ) + 10L );
			}
			catch ( final InterruptedException ignored )
			{
				throw new SQLException( "Interrupted before transaction could be retried.", rollbackException );
			}
		}
	}

	/**
	 * Creates a function if it doesn't exist yet.
	 *
	 * @param name            Name of the function.
	 * @param createStatement CREATE FUNCTION statement.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	private void createHsqldbFunctionIfNotExists( final String name, final String createStatement )
	throws SQLException
	{
		final Number count = JdbcTools.selectNumber( _dataSource, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME=? AND ROUTINE_TYPE=?", name, "FUNCTION" );
		if ( ( count == null ) || ( count.intValue() == 0 ) )
		{
			JdbcTools.executeUpdate( _dataSource, createStatement );
		}
	}

	/**
	 * Implements DATE compatible with MySQL.
	 *
	 * @param timestamp SQL timestamp.
	 *
	 * @return SQL date.
	 */
	@SuppressWarnings( "unused" )
	public static java.sql.Date mysqlDate( final Timestamp timestamp )
	{
		return new java.sql.Date( timestamp.getTime() );
	}

	/**
	 * Implements WEEKOFYEAR compatible with MySQL (ISO 8601).
	 *
	 * @param timestamp SQL timestamp.
	 *
	 * @return Week of year.
	 */
	@SuppressWarnings( "unused" )
	public static int mysqlWeekOfYear( final Timestamp timestamp )
	{
		final Calendar calendar = Calendar.getInstance();

		// Weeks according to ISO 8601:
		calendar.setFirstDayOfWeek( Calendar.MONDAY );
		calendar.setMinimalDaysInFirstWeek( 4 );

		calendar.setTime( timestamp );
		return calendar.get( Calendar.WEEK_OF_YEAR );
	}

	/**
	 * Converter that converts a {@link ResultSet} to tuple objects.
	 *
	 * @param <T> Database record object.
	 */
	public static class ObjectConverter<T>
	{
		/**
		 * Database record class.
		 */
		final Class<T> _dbClass;

		/**
		 * Cache for {@link #getColumnHandlers}.
		 */
		private List<Duet<Integer, FieldHandler>> _columnHandlers = null;

		/**
		 * Prefix to remove from the column names in the result set. This can be
		 * used to separate columns for multiple objects.
		 */
		private String _columnPrefix = null;

		/**
		 * Whether the table name should be checked for each column to see if it
		 * belongs to this database record class.
		 *
		 * The advantage over {@link #setColumnPrefix} is that this method works
		 * with wildcards, e.g. '{@code SELECT t1.*, t2.* FROM Table1 AS t1,
		 * Table2 AS t2}'. The disadvantage is that it can't distinguish between
		 * multiple instances of the same table.
		 */
		private boolean _matchTableName = false;

		/**
		 * Construct processor.
		 *
		 * @param dbClass Database record class.
		 */
		public ObjectConverter( @NotNull final Class<T> dbClass )
		{
			_dbClass = dbClass;
		}

		/**
		 * Convert row from result set to database record object.
		 *
		 * @param resultSet {@link ResultSet} to get record properties from.
		 *
		 * @return Database record object.
		 *
		 * @throws SQLException if an error occurs while accessing the
		 * database.
		 */
		@NotNull
		public T convert( @NotNull final ResultSet resultSet )
		throws SQLException
		{
			final T result = createObject();

			for ( final Duet<Integer, FieldHandler> columnHandler : getColumnHandlers( resultSet ) )
			{
				columnHandler.getValue2().getColumnData( result, resultSet, columnHandler.getValue1() );
			}

			return result;
		}

		/**
		 * Get handlers that for result set columns.
		 *
		 * @param resultSet {@link ResultSet} to get column handlers for.
		 *
		 * @return List of column numbers and their handlers.
		 *
		 * @throws SQLException if an error occurs while accessing the
		 * database.
		 */
		@NotNull
		protected List<Duet<Integer, FieldHandler>> getColumnHandlers( @NotNull final ResultSet resultSet )
		throws SQLException
		{
			List<Duet<Integer, FieldHandler>> result = _columnHandlers;
			if ( result == null )
			{
				final ResultSetMetaData metaData = resultSet.getMetaData();
				final int columnCount = metaData.getColumnCount();

				result = new ArrayList<Duet<Integer, FieldHandler>>( columnCount );

				final ClassHandler classHandler = getClassHandler( _dbClass );
				final boolean matchTableName = _matchTableName;
				final String tableName = matchTableName ? classHandler.getTableName() : null;
				final String columnPrefix = _columnPrefix;

				for ( int column = 1; column <= columnCount; column++ )
				{
					if ( !matchTableName || tableName.equals( metaData.getTableName( column ) ) )
					{
						final String name = metaData.getColumnLabel( column ).toLowerCase();

						FieldHandler handler = null;
						if ( columnPrefix == null )
						{
							handler = classHandler.getFieldHandlerForColumn( name );
						}
						else if ( name.startsWith( columnPrefix ) )
						{
							final String baseName = name.substring( columnPrefix.length() );
							handler = classHandler.getFieldHandlerForColumn( baseName );
						}

						if ( handler != null )
						{
							result.add( new BasicDuet<Integer, FieldHandler>( column, handler ) );
						}
					}
				}

				_columnHandlers = result;
			}
			return result;
		}

		/**
		 * Create database record object.
		 *
		 * @return Database record object.
		 */
		@NotNull
		public T createObject()
		{
			return BeanTools.newInstance( _dbClass );
		}

		public void setColumnPrefix( final String columnPrefix )
		{
			_columnPrefix = columnPrefix;
		}

		public String getColumnPrefix()
		{
			return _columnPrefix;
		}

		public boolean isMatchTableName()
		{
			return _matchTableName;
		}

		public void setMatchTableName( final boolean matchTableName )
		{
			_matchTableName = matchTableName;
		}
	}

	/**
	 * SQL result processor that converts a result set into a list of database
	 * records.
	 *
	 * @param <T> Database record object.
	 */
	public static class ObjectListConverter<T>
	implements ResultProcessor<List<T>>
	{
		/**
		 * Converts {@link ResultSet} to record objects.
		 */
		private final ObjectConverter<T> _converter;

		/**
		 * Construct processor.
		 *
		 * @param dbClass Database record class.
		 */
		public ObjectListConverter( @NotNull final Class<T> dbClass )
		{
			_converter = new ObjectConverter<T>( dbClass );
		}

		@Override
		public List<T> process( @NotNull final ResultSet resultSet )
		throws SQLException
		{
			final List<T> result = new ArrayList<T>();

			int rowCount = 0;
			while ( resultSet.next() )
			{
				JdbcTools.checkMaximumSafeRowCount( ++rowCount );
				result.add( _converter.convert( resultSet ) );
			}

			return result;
		}
	}

	/**
	 * SQL result processor that converts a result set into a single database
	 * record.
	 *
	 * @param <T> Database record object.
	 */
	public static class SingleObjectConverter<T>
	implements ResultProcessor<SingleObjectConverter<T>>
	{
		/**
		 * Database record class.
		 */
		private final Class<T> _dbClass;

		/**
		 * First object from result set. This is {@code null} after processing
		 * if the result set was empty.
		 */
		@Nullable
		private T _first = null;

		/**
		 * Flag to indicate that more than one row was present in the result
		 * set.
		 */
		private boolean _multiple = false;

		/**
		 * Construct processor.
		 *
		 * @param dbClass Database record class.
		 */
		public SingleObjectConverter( @NotNull final Class<T> dbClass )
		{
			_dbClass = dbClass;
		}

		@Override
		public SingleObjectConverter<T> process( @NotNull final ResultSet resultSet )
		throws SQLException
		{
			final T first;
			final boolean multiple;

			if ( resultSet.next() )
			{
				final ObjectConverter<T> converter = new ObjectConverter<T>( _dbClass );
				first = converter.convert( resultSet );
				multiple = resultSet.next();
			}
			else
			{
				first = null;
				multiple = false;
			}

			_first = first;
			_multiple = multiple;

			return this;
		}

		/**
		 * Returns whether more than one row was present in the result set.
		 *
		 * @return {@code true} if more than one row was present in the result
		 * set.
		 */
		public boolean isMultiple()
		{
			return _multiple;
		}

		/**
		 * Returns first object from result set. This returns {@code null} if
		 * the result set was empty.
		 *
		 * @return First object from result; {@code null} if result set was
		 * empty.
		 */
		@Nullable
		public T getFirst()
		{
			return _first;
		}

		/**
		 * Returns first object from result set. This throws a {@link
		 * NoSuchElementException} if the result set was empty.
		 *
		 * @return First object from result.
		 */
		@NotNull
		public T getOne()
		{
			final T result = _first;
			if ( result == null )
			{
				throw new NoSuchElementException( "Got no result from query, while one result is required" );
			}

			return result;
		}

		/**
		 * Returns first object from result set. This throws a {@link
		 * NoSuchElementException} if the result set was empty or a {@link
		 * IllegalStateException} if there were multiple rows in the result
		 * set.
		 *
		 * @return Only object from result.
		 */
		@NotNull
		public T getOnly()
		{
			final T result = _first;
			if ( result == null )
			{
				throw new NoSuchElementException( "Got no result from query, while one and only one result is required" );
			}

			if ( _multiple )
			{
				throw new IllegalStateException( "Got multiple results from query, while one and only one is required" );
			}

			return result;
		}
	}
}
