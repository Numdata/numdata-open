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
import java.sql.Date;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.sql.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * In-memory implementation of {@link DbServices} for testing/stand-alone
 * application use based on HSQLDB
 * (see <a href='http://www.hsqldb.org/'>http://www.hsqldb.org/</a>). Each new
 * instance is backed by a newly created database.
 *
 * @author  Peter S. Heijnen
 */
public class HsqlDbServices
	extends DbServices
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( HsqlDbServices.class );

	/**
	 * Keeps track of the ID that is used to uniquely identify the database
	 * backing the next instance of this class.
	 */
	private static final AtomicInteger DATABASE_ID = new AtomicInteger( 0 );

	/**
	 * Database name.
	 */
	@NotNull
	private final String _databaseName;

	/**
	 * Construct in-memory database.
	 */
	public HsqlDbServices()
	{
		this( String.valueOf( DATABASE_ID.getAndIncrement() ), true );
	}

	/**
	 * Construct in-memory database.
	 *
	 * @param   databaseName    Database name.
	 * @param   shutdown        Shutdown database after last client disconnects.
	 */
	public HsqlDbServices( @NotNull final String databaseName, final boolean shutdown )
	{
		super( createDataSource( databaseName, shutdown ) );

		/*
		 * Implement missing SQL functions.
		 */
		try
		{
			createFunctionIfNotExists( "DATE", "CREATE FUNCTION DATE(t TIMESTAMP) RETURNS DATE\n" +
			                                   "LANGUAGE JAVA DETERMINISTIC NO SQL\n" +
			                                   "EXTERNAL NAME 'CLASSPATH:" + HsqlDbServices.class.getName() + ".mysqlDate'" );

			createFunctionIfNotExists( "WEEKOFYEAR", "CREATE FUNCTION WEEKOFYEAR(t TIMESTAMP) RETURNS INT\n" +
			                                         "LANGUAGE JAVA DETERMINISTIC NO SQL\n" +
			                                         "EXTERNAL NAME 'CLASSPATH:" + HsqlDbServices.class.getName() + ".mysqlWeekOfYear'" );
		}
		catch ( SQLException e )
		{
			throw new RuntimeException( "Failed to configure HSQLDB.", e );
		}

		setSqlDialect( SqlDialect.HSQLDB );
		_databaseName = databaseName;
	}

	/**
	 * Creates a function if it doesn't exist yet.
	 *
	 * @param name            Name of the function.
	 * @param createStatement CREATE FUNCTION statement.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	private void createFunctionIfNotExists( final String name, final String createStatement )
	throws SQLException
	{
		final Number count = JdbcTools.selectNumber( _dataSource, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME=? AND ROUTINE_TYPE=?", name, "FUNCTION" );
		if ( ( count == null ) || ( count.intValue() == 0 ) )
		{
			JdbcTools.executeUpdate( _dataSource, createStatement );
		}
	}

	/**
	 * Returns the name of the in-memory database.
	 *
	 * @return Database name.
	 */
	@NotNull
	public String getDatabaseName()
	{
		return _databaseName;
	}

	/**
	 * Sets the transaction concurrency control model for the database.
	 *
	 * @param transactionControl Transaction control to set.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public void setDatabaseTransactionControl( final TransactionControl transactionControl )
	throws SQLException
	{
		JdbcTools.executeUpdate( getDataSource(), "SET DATABASE TRANSACTION CONTROL " + transactionControl );
	}

	/**
	 * Performs a shutdown on the underlying database, freeing any resources
	 * it uses.
	 *
	 * @throws  SQLException if an error occurs while accessing the database.
	 */
	public void shutdown()
		throws SQLException
	{
		JdbcTools.executeUpdate( getDataSource(), "SHUTDOWN" );
	}

	@Override
	public void createTable( final Class<?> tableClass )
		throws SQLException
	{
		final ClassHandler classHandler = getClassHandler( tableClass );
		String createStatement = classHandler.getCreateStatement();
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

		LOG.debug( "Creating HSQLDB table with statement:\n" + createStatement );

		JdbcTools.executeUpdate( getDataSource(), createStatement );
	}

	@Override
	protected long getInsertID( final Connection connection )
		throws SQLException
	{
		final long result;

		final Statement statement = connection.createStatement();
		try
		{
			final ResultSet resultSet = statement.executeQuery( "CALL IDENTITY()" );
			try
			{
				resultSet.next();
				result = resultSet.getLong( 1 );
			}
			finally
			{
				resultSet.close();
			}
		}
		finally
		{
			statement.close();
		}

		return result;
	}

	/**
	 * Get JDBC memory database services (used for test purposes).
	 *
	 *
	 * @param   databaseName    Database name.
	 * @param   shutdown        Shutdown database after last client disconnects.
	 *
	 * @return  Database services (in memory) for JDBC database.
	 */
	private static DataSource createDataSource( @NotNull final String databaseName, final boolean shutdown )
	{
		final JdbcDataSource result;

		try
		{
			result = new JdbcDataSource( "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:" + databaseName + ";shutdown=" + shutdown, "sa", "" );
		}
		catch ( final SQLException e )
		{
			throw new RuntimeException( "Cannot create JDBC connection: " + e.getMessage(), e );
		}

		return result;
	}

	/**
	 * Specifies a transaction concurrency control model.
	 */
	public enum TransactionControl
	{
		/**
		 * Two Phase Locking.
		 */
		LOCKS,

		/**
		 * Two Phase Locking with Snapshot Isolation.
		 */
		MVLOCKS,

		/**
		 * Multi-version concurrency control.
		 */
		MVCC
	}

	/**
	 * Implements DATE compatible with MySQL.
	 *
	 * @param timestamp SQL timestamp.
	 *
	 * @return SQL date.
	 */
	public static Date mysqlDate( final Timestamp timestamp )
	{
		return new Date( timestamp.getTime() );
	}

	/**
	 * Implements WEEKOFYEAR compatible with MySQL (ISO 8601).
	 *
	 * @param timestamp SQL timestamp.
	 *
	 * @return Week of year.
	 */
	public static int mysqlWeekOfYear( final Timestamp timestamp )
	{
		final Calendar calendar = Calendar.getInstance();

		// Weeks according to ISO 8601:
		calendar.setFirstDayOfWeek( Calendar.MONDAY );
		calendar.setMinimalDaysInFirstWeek( 4 );

		calendar.setTime( timestamp );
		return calendar.get( Calendar.WEEK_OF_YEAR );
	}
}
