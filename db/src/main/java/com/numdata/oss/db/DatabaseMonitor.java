/*
 * Copyright (c) 2010-2019, Numdata BV, The Netherlands.
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
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "WeakerAccess", "unused" } )
public abstract class DatabaseMonitor
extends PollingMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( DatabaseMonitor.class );

	/**
	 * Data source for the feedback database.
	 */
	@Nullable
	private DataSource _dataSource = null;

	/**
	 * Initial delay in milliseconds before attempting to reconnect to the
	 * database.
	 */
	private int _initialReconnectDelay = 60000;

	/**
	 * Maximum delay in milliseconds before attempting to reconnect to the
	 * database.
	 */
	private int _maximumReconnectMaxDelay = 600000;

	/**
	 * Fail count.
	 */
	private int _failCount = 0;

	/**
	 * Listeners registered with the monitor.
	 */
	@NotNull
	private final Collection<DatabaseMonitorListener> _listeners = new ArrayList<DatabaseMonitorListener>();

	/**
	 * Constructs a database monitor.
	 *
	 * @param pollTime Time to wait after polling the entity.
	 */
	protected DatabaseMonitor( final int pollTime )
	{
		super( pollTime );
	}

	public int getInitialReconnectDelay()
	{
		return _initialReconnectDelay;
	}

	public void setInitialReconnectDelay( final int initialReconnectDelay )
	{
		_initialReconnectDelay = initialReconnectDelay;
	}

	public int getMaximumReconnectMaxDelay()
	{
		return _maximumReconnectMaxDelay;
	}

	public void setMaximumReconnectMaxDelay( final int maximumReconnectMaxDelay )
	{
		_maximumReconnectMaxDelay = maximumReconnectMaxDelay;
	}

	public int getFailCount()
	{
		return _failCount;
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
	public void stop()
	{
		_dataSource = null;
	}

	/**
	 * Adds a file system monitor listener.
	 *
	 * @param listener Listener to be added.
	 */
	public void addListener( final DatabaseMonitorListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes a file system monitor listener.
	 *
	 * @param listener Listener to be removed.
	 */
	public void removeListener( final DatabaseMonitorListener listener )
	{
		_listeners.remove( listener );
	}

	@Override
	protected void poll()
	throws Exception
	{
		final DataSource dataSource = _dataSource;
		if ( dataSource == null )
		{
			throw new AssertionError( "Data source must be initialized" );
		}

		try
		{
			poll( dataSource );
			_failCount = 0;
		}
		catch ( final Exception e )
		{
			final int initialDelay = Math.max( 1000, getInitialReconnectDelay() );
			final int maximumDelay = Math.min( Math.max( 60000, getMaximumReconnectMaxDelay() ), 3600000 );
			final int failCount = ++_failCount;
			final long delay = Math.min( initialDelay * failCount, maximumDelay );

			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "poll: " + e + " => Delay " + delay + "ms (failCount=" + failCount + ", initialDelay=" + initialDelay + "ms, maximumDelay=" + maximumDelay + "ms)" );
			}
			try
			{
				Thread.sleep( delay );
			}
			catch ( final InterruptedException ignored )
			{
			}
			throw e;
		}
	}

	/**
	 * Polls the given database.
	 *
	 * If an exception is thrown, an extra re-connect delay is inserted before
	 * continuing to prevent overloading the system.
	 *
	 * @param dataSource {@link DataSource} for database to poll.
	 *
	 * @throws Exception if any exception occurs while polling the database.
	 */
	protected abstract void poll( @NotNull final DataSource dataSource )
	throws Exception;

	/**
	 * Notifies listeners that the specified row was added to the database.
	 *
	 * @param rowObject Object representing a row that was added.
	 */
	protected void fireRowAddedEvent( @NotNull final Object rowObject )
	{
		for ( final DatabaseMonitorListener listener : _listeners )
		{
			try
			{
				listener.rowAdded( this, rowObject );
			}
			catch ( final Exception e )
			{
				LOG.error( "Unhandled exception in 'rowAdded' method of " + listener, e );
			}
		}
	}
}
