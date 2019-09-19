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

import java.sql.*;
import java.util.*;

import javax.sql.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * A {@link PollingMonitor} implementation for a database.
 *
 * @author  Peter S. Heijnen
 */
public abstract class DatabaseMonitor
	extends PollingMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( DatabaseMonitor.class );

	/**
	 * JDBC driver.
	 */
	protected String _jdbcDriver;

	/**
	 * JDBC database URL.
	 */
	protected String _databaseURL;

	/**
	 * JDBC database user.
	 */
	protected String _user;

	/**
	 * JDBC database password.
	 */
	protected String _password;

	/**
	 * Data source for the feedback database.
	 */
	protected DataSource _dataSource = null;

	/**
	 * Listeners registered with the monitor.
	 */
	private final List<DatabaseMonitorListener> _listeners;

	/**
	 * Constructs a database monitor.
	 *
	 * @param   jdbcDriver      JDBC driver.
	 * @param   databaseURL     Database URL.
	 * @param   user            Database user.
	 * @param   password        Database password.
	 * @param   polltime        Time to wait after polling the entity.
	 */
	protected DatabaseMonitor( final String jdbcDriver, final String databaseURL, final String user, final String password, final int polltime )
	{
		super( polltime );
		_jdbcDriver = jdbcDriver;
		_databaseURL = databaseURL;
		_user = user;
		_password = password;
		_listeners = new ArrayList<DatabaseMonitorListener>();
	}

	@NotNull
	@Override
	public String getName()
	{
		return _databaseURL;
	}

	@Override
	protected void initialize()
		throws SQLException
	{
		if ( _dataSource == null )
		{
			_dataSource = createDataSource();
		}
	}

	/**
	 * Creates a data source.
	 *
	 * @return Created data source.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	protected abstract DataSource createDataSource()
	throws SQLException;

	@Override
	public boolean isAvailable()
	{
		boolean result;

		try
		{
			initialize();
			final Connection connection = _dataSource.getConnection();
			connection.close();
			result = true;
		}
		catch ( SQLException e )
		{
			result = false;
		}

		return result;
	}

	@Override
	public void stop()
	{
		_dataSource = null;
	}

	/**
	 * Adds a file system monitor listener.
	 *
	 * @param   listener    Listener to be added.
	 */
	public void addListener( final DatabaseMonitorListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes a file system monitor listener.
	 *
	 * @param   listener    Listener to be removed.
	 */
	public void removeListener( final DatabaseMonitorListener listener )
	{
		_listeners.remove( listener );
	}

	/**
	 * Notifies listeners that the specified row was added to the database.
	 *
	 * @param   rowObject   Object representing a row that was added.
	 */
	protected void fireRowAddedEvent( final Object rowObject )
	{
		for ( final DatabaseMonitorListener listener : _listeners )
		{
			try
			{
				listener.rowAdded( this, rowObject );
			}
			catch ( Exception e )
			{
				LOG.error( "Unhandled exception in 'rowAdded' method of " + listener, e );
			}
		}
	}
}
