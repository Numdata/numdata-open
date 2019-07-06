/*
 * Copyright (c) 2002-2019, Numdata BV, The Netherlands.
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
import java.util.concurrent.*;
import java.util.logging.*;
import javax.sql.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides database connectivity using a pooling mechanism to reduce
 * resource usage.
 *
 * @author Peter S. Heijnen
 */
public class JdbcDataSource
implements DataSource
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JdbcDataSource.class );

	/**
	 * URL of the database server.
	 */
	private final String _databaseURL;

	/**
	 * Database user on whose behalf the connection is being made.
	 */
	private final String _user;

	/**
	 * User's password.
	 */
	private final String _password;

	/**
	 * Collection of connections to allow multiple connection and provide
	 * reusability.
	 */
	private final Stack<Connection> _connectionPool = new Stack<Connection>();

	/**
	 * Collection of currently used connections.
	 */
	private final List<Connection> _connectionUsed = new ArrayList<Connection>();

	/**
	 * Destination for log messages.
	 */
	private PrintWriter _log = new PrintWriter( System.err );

	/**
	 * Login timeout in seconds.
	 */
	private int _loginTimeout = 1;

	/**
	 * Initialize database services using the specified JDBC driver and URL to
	 * the database. This instance may be shared by multiple connections (a
	 * connection pool is created in order to reuse connections).
	 *
	 * @param jdbcDriver  Class name of JDBC driver ({@code null} may be
	 *                    provided if the required JDBC driver is already
	 *                    registered in some earlier stage).
	 * @param databaseURL URL to the database. The format depends on the JDBC
	 *                    driver (see its documentation for more information).
	 * @param user        Database user on whose behalf the connection is being
	 *                    made.
	 * @param password    User's password.
	 *
	 * @throws SQLException if the specified JDBC driver is not available.
	 */
	public JdbcDataSource( @Nullable final String jdbcDriver, final String databaseURL, @Nullable final String user, @Nullable final String password )
	throws SQLException
	{
		LOG.debug( "JdbcDataSource( " + TextTools.quote( jdbcDriver ) + ", " + TextTools.quote( databaseURL ) + ", " + TextTools.quote( user ) + " )" );

		_databaseURL = databaseURL;
		_user = user;
		_password = password;

		if ( jdbcDriver != null )
		{
			final Class<?> driverClass;
			try
			{
				driverClass = Class.forName( jdbcDriver );
			}
			catch ( final ClassNotFoundException e )
			{
				throw new SQLException( "JDBC driver '" + jdbcDriver + "' not found. Please check your CLASSPATH and configuration file.", e );
			}

			if ( !Driver.class.isAssignableFrom( driverClass ) )
			{
				throw new SQLException( "JDBC driver '" + jdbcDriver + "' is not a valid JDBC driver." );
			}

			final Driver driverInstance;
			try
			{
				driverInstance = (Driver)driverClass.getConstructor().newInstance();
			}
			catch ( final Exception e )
			{
				throw new SQLException( "JDBC driver '" + jdbcDriver + "' failed to initialize (" + e.getMessage() + "'). Please check your configuration.", e );
			}

			DriverManager.registerDriver( driverInstance );
		}
	}

	/**
	 * Clean up before garbage collection.
	 *
	 * Makes sure that all database connections are closed.
	 *
	 * @throws Throwable if finalization failed.
	 */
	@Override
	protected void finalize()
	throws Throwable
	{
		while ( !_connectionUsed.isEmpty() )
		{
			final Connection connection = _connectionUsed.get( 0 );
			_connectionUsed.remove( 0 );

			try
			{
				connection.close();
			}
			catch ( final Exception ignored )
			{
			}
		}

		while ( !_connectionPool.empty() )
		{
			final Connection connection = _connectionPool.pop();

			try
			{
				connection.close();
			}
			catch ( final Exception ignored )
			{
			}
		}

		super.finalize();
	}

	/**
	 * Request a SQL database connection from the connection pool.
	 *
	 * @return Connection object with SQL database connection.
	 *
	 * @throws SQLException if no connection could be established.
	 */
	@SuppressWarnings( { "JDBCResourceOpenedButNotSafelyClosed", "CallToDriverManagerGetConnection" } )
	private Connection allocateConnection()
	throws SQLException
	{
		/*
		 * Sanity check
		 */
		final String url = _databaseURL;
		if ( url == null )
		{
			throw new SQLException( "no database URL is set" );
		}

		/*
		 * Get existing connection from connection pool, or try to create
		 * a new connection. Add the allocated connection to "used
		 * connections" list.
		 */
		Connection result = null;
		synchronized ( _connectionPool )
		{
			while ( ( result == null ) && !_connectionPool.empty() )
			{
				final Connection connection = _connectionPool.pop();
				try
				{
					if ( !connection.isClosed() )
					{
						result = connection;
					}
				}
				catch ( final SQLException ignored )
				{
					/* ignored - connection will be removed */
				}
			}
		}

		if ( result == null )
		{
			// TODO: Better would be to use a 'DataSource' internally as well.
			// That would require different values for 'jdbcDriver' though.
			// And may prove to be more difficult for configuration via web forms.
			DriverManager.setLoginTimeout( _loginTimeout );
			result = DriverManager.getConnection( url, _user, _password );
		}
		else
		{
			result.setReadOnly( false );
		}

		_connectionUsed.add( result );

		return result;
	}

	/**
	 * Release a SQL database connection and make it available again in the
	 * connection pool.
	 *
	 * @param connection Connection object to release.
	 */
	private void releaseConnection( final Connection connection )
	{
		if ( connection != null )
		{
			synchronized ( _connectionUsed )
			{
				if ( _connectionUsed.contains( connection ) )
				{
					_connectionUsed.remove( connection );
					_connectionPool.push( connection );
				}
			}
		}
	}

	@Override
	public Connection getConnection()
	throws SQLException
	{
		return new WrappedConnection( allocateConnection() );
	}

	@Override
	public Connection getConnection( final String username, final String password )
	throws SQLException
	{
		if ( !_user.equals( username ) || !_password.equals( password ) )
		{
			throw new SQLException( "DbPool user/password mismatch!" );
		}

		return getConnection();
	}

	@Override
	public PrintWriter getLogWriter()
	{
		return _log;
	}

	@Override
	public void setLogWriter( final PrintWriter out )
	{
		_log = out;
	}

	@Override
	public void setLoginTimeout( final int seconds )
	{
		_loginTimeout = seconds;
	}

	@Override
	public int getLoginTimeout()
	{
		return _loginTimeout;
	}

	public Logger getParentLogger()
	throws SQLFeatureNotSupportedException
	{
		throw new SQLFeatureNotSupportedException( "We don't use java.util.logging.Logger" );
	}

	@Override
	public <T> T unwrap( final Class<T> iface )
	throws SQLException
	{
		throw new SQLException( "not a wrapper" );
	}

	@Override
	public boolean isWrapperFor( final Class<?> iface )
	{
		return false;
	}

	/**
	 * Wrapper for JDBC connection.
	 */
	@SuppressWarnings( "JDBCPrepareStatementWithNonConstantString" )
	private class WrappedConnection
	implements Connection
	{
		/**
		 * Wrapped connection.
		 */
		private final Connection _realConnection;

		/**
		 * Flag to indicate that the connection is closed.
		 */
		private boolean _closed;

		/**
		 * Construct wrapped connection.
		 *
		 * @param realConnection Connection to wrap.
		 */
		WrappedConnection( final Connection realConnection )
		{
			_realConnection = realConnection;
			_closed = false;
		}

		/**
		 * Internal method to throws a {@link SQLException} if the connection is
		 * closed.
		 *
		 * @throws SQLException if the connection is closed.
		 */
		private void checkClosed()
		throws SQLException
		{
			if ( _closed )
			{
				throw new SQLException( "connection has been closed" );
			}
		}

		@Override
		public Array createArrayOf( final String typeName, final Object[] elements )
		throws SQLException
		{
			checkClosed();
			return _realConnection.createArrayOf( typeName, elements );
		}

		@Override
		public Blob createBlob()
		throws SQLException
		{
			checkClosed();
			return _realConnection.createBlob();
		}

		@Override
		public Clob createClob()
		throws SQLException
		{
			checkClosed();
			return _realConnection.createClob();
		}

		@Override
		public NClob createNClob()
		throws SQLException
		{
			checkClosed();
			return _realConnection.createNClob();
		}

		@Override
		public SQLXML createSQLXML()
		throws SQLException
		{
			checkClosed();
			return _realConnection.createSQLXML();
		}

		@Override
		public Statement createStatement()
		throws SQLException
		{
			checkClosed();
			return _realConnection.createStatement();
		}

		@Override
		public Struct createStruct( final String typeName, final Object[] attributes )
		throws SQLException
		{
			checkClosed();
			return _realConnection.createStruct( typeName, attributes );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql );
		}

		@Override
		public CallableStatement prepareCall( final String sql )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareCall( sql );
		}

		@Override
		public String nativeSQL( final String sql )
		throws SQLException
		{
			checkClosed();
			return _realConnection.nativeSQL( sql );
		}

		@Override
		public void setAutoCommit( final boolean autoCommit )
		throws SQLException
		{
			checkClosed();
			_realConnection.setAutoCommit( autoCommit );
		}

		@Override
		public boolean getAutoCommit()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getAutoCommit();
		}

		@Override
		public void commit()
		throws SQLException
		{
			checkClosed();
			_realConnection.commit();
		}

		@Override
		public void rollback()
		throws SQLException
		{
			checkClosed();
			_realConnection.rollback();
		}

		@Override
		public void close()
		throws SQLException
		{
			if ( !_closed )
			{
				_closed = true;
				if ( !_realConnection.getAutoCommit() )
				{
					_realConnection.rollback();
					_realConnection.setAutoCommit( true );
				}
				releaseConnection( _realConnection );
			}
		}

		@Override
		public boolean isClosed()
		throws SQLException
		{
			return _closed || _realConnection.isClosed();
		}

		@Override
		public DatabaseMetaData getMetaData()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getMetaData();
		}

		@Override
		public void setReadOnly( final boolean readOnly )
		throws SQLException
		{
			checkClosed();
			_realConnection.setReadOnly( readOnly );
		}

		@Override
		public boolean isReadOnly()
		throws SQLException
		{
			checkClosed();
			return _realConnection.isReadOnly();
		}

		@Override
		public void setCatalog( final String catalog )
		throws SQLException
		{
			checkClosed();
			_realConnection.setCatalog( catalog );
		}

		@Override
		public String getCatalog()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getCatalog();
		}

		@Override
		public void setTransactionIsolation( final int level )
		throws SQLException
		{
			checkClosed();
			_realConnection.setTransactionIsolation( level );
		}

		@Override
		public int getTransactionIsolation()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getTransactionIsolation();
		}

		@Override
		public SQLWarning getWarnings()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getWarnings();
		}

		@Override
		public void clearWarnings()
		throws SQLException
		{
			checkClosed();
			_realConnection.clearWarnings();
		}

		@Override
		public Statement createStatement( final int resultSetType, final int resultSetConcurrency )
		throws SQLException
		{
			checkClosed();
			return _realConnection.createStatement( resultSetType, resultSetConcurrency );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql, final int resultSetType, final int resultSetConcurrency )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql, resultSetType, resultSetConcurrency );
		}

		@Override
		public CallableStatement prepareCall( final String sql, final int resultSetType, final int resultSetConcurrency )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareCall( sql, resultSetType, resultSetConcurrency );
		}

		@Override
		public Map<String, Class<?>> getTypeMap()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getTypeMap();
		}

		@Override
		public void setTypeMap( final Map<String, Class<?>> map )
		throws SQLException
		{
			checkClosed();
			_realConnection.setTypeMap( map );
		}

		@Override
		public void setHoldability( final int holdability )
		throws SQLException
		{
			checkClosed();
			_realConnection.setHoldability( holdability );
		}

		@Override
		public int getHoldability()
		throws SQLException
		{
			checkClosed();
			return _realConnection.getHoldability();
		}

		@Override
		public Savepoint setSavepoint()
		throws SQLException
		{
			checkClosed();
			return _realConnection.setSavepoint();
		}

		@Override
		public Savepoint setSavepoint( final String name )
		throws SQLException
		{
			checkClosed();
			return _realConnection.setSavepoint( name );
		}

		@Override
		public void rollback( final Savepoint savepoint )
		throws SQLException
		{
			checkClosed();
			_realConnection.rollback( savepoint );
		}

		@Override
		public void releaseSavepoint( final Savepoint savepoint )
		throws SQLException
		{
			checkClosed();
			_realConnection.releaseSavepoint( savepoint );
		}

		@Override
		public Statement createStatement( final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability )
		throws SQLException
		{
			checkClosed();
			return _realConnection.createStatement( resultSetType, resultSetConcurrency, resultSetHoldability );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
		}

		@Override
		public CallableStatement prepareCall( final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareCall( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql, final int autoGeneratedKeys )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql, autoGeneratedKeys );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql, final int[] columnIndexes )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql, columnIndexes );
		}

		@Override
		public PreparedStatement prepareStatement( final String sql, final String[] columnNames )
		throws SQLException
		{
			checkClosed();
			return _realConnection.prepareStatement( sql, columnNames );
		}

		@Override
		public boolean isValid( final int timeout )
		throws SQLException
		{
			return !_closed && _realConnection.isValid( timeout );
		}

		@Override
		public void setClientInfo( final String name, final String value )
		throws SQLClientInfoException
		{
			_realConnection.setClientInfo( name, value );
		}

		@Override
		public void setClientInfo( final Properties properties )
		throws SQLClientInfoException
		{
			_realConnection.setClientInfo( properties );
		}

		@Override
		public String getClientInfo( final String name )
		throws SQLException
		{
			return _realConnection.getClientInfo( name );
		}

		@Override
		public Properties getClientInfo()
		throws SQLException
		{
			return _realConnection.getClientInfo();
		}

		@Override
		public <T> T unwrap( final Class<T> iface )
		throws SQLException
		{
			final Connection realConnection = _realConnection;
			return iface.isInstance( realConnection ) ? (T)realConnection : realConnection.unwrap( iface );
		}

		@Override
		public boolean isWrapperFor( final Class<?> iface )
		throws SQLException
		{
			final Connection realConnection = _realConnection;
			return iface.isInstance( realConnection ) || realConnection.isWrapperFor( iface );
		}

		public void setSchema( final String schema )
		throws SQLException
		{
			throw new SQLFeatureNotSupportedException( "setSchema" );
		}

		public String getSchema()
		throws SQLException
		{
			throw new SQLFeatureNotSupportedException( "getSchema" );
		}

		public void abort( final Executor executor )
		throws SQLException
		{
			throw new SQLFeatureNotSupportedException( "abort" );
		}

		public void setNetworkTimeout( final Executor executor, final int milliseconds )
		throws SQLException
		{
			throw new SQLFeatureNotSupportedException( "setNetworkTimeout" );
		}

		public int getNetworkTimeout()
		throws SQLException
		{
			throw new SQLFeatureNotSupportedException( "getNetworkTimeout" );
		}
	}
}
