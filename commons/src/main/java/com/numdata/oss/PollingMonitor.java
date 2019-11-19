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
package com.numdata.oss;

import java.io.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Skeleton implementation of a monitor that polls an entity to monitor it, as
 * opposed to an event-driven (push) approach.
 *
 * @author G. Meinders
 */
@SuppressWarnings( "WeakerAccess" )
public abstract class PollingMonitor
implements ResourceMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( PollingMonitor.class );

	/**
	 * Approximate time between two calls to {@link #poll()}, in milliseconds.
	 */
	private int _pollTime;

	/**
	 * Whether the monitor is running.
	 */
	private boolean _running = false;

	/**
	 * Last exception that occurred.
	 */
	@Nullable
	private Exception _lastException = null;

	/**
	 * Last time that the connection was established.
	 */
	private long _lastPolled = -1;

	/**
	 * Constructs a new monitor that polls an entity at regular intervals.
	 *
	 * @param pollTime Time to wait after polling the entity.
	 */
	protected PollingMonitor( final int pollTime )
	{
		_pollTime = pollTime;
	}

	public int getPollTime()
	{
		return _pollTime;
	}

	public void setPollTime( final int pollTime )
	{
		_pollTime = pollTime;
	}

	public boolean isRunning()
	{
		return _running;
	}

	@Nullable
	public Exception getLastException()
	{
		return _lastException;
	}

	public void setLastException( @Nullable final Exception lastException )
	{
		_lastException = lastException;
	}

	public long getLastPolled()
	{
		return _lastPolled;
	}

	@NotNull
	@Override
	public ResourceStatus getStatus()
	{
		final ResourceStatus result = new ResourceStatus();
		result.setLastOnline( getLastPolled() );

		if ( !isRunning() )
		{
			result.setStatus( ResourceStatus.Status.UNAVAILABLE );
			result.setDetails( "Not running" );
		}
		else
		{
			final Exception lastException = getLastException();
			if ( lastException != null )
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "Error occurred" );
				result.setException( lastException );
			}
			else
			{
				result.setDetails( "Running" );
				result.setStatus( ResourceStatus.Status.AVAILABLE );
			}
		}
		return result;
	}


	/**
	 * Performs any initialization needed for the monitor. If an exception is
	 * thrown, the monitor will shut down.
	 *
	 * @throws Exception if any exception occurs during initialization,
	 * preventing the monitor from being run.
	 */
	protected abstract void initialize()
	throws Exception;

	/**
	 * Polls the monitored entity. If an exception is thrown, the monitor keeps
	 * running and will continue polling normally after the polling time has
	 * passed.
	 *
	 * @throws Exception if any exception occurs while polling.
	 */
	protected abstract void poll()
	throws Exception;

	@Override
	public void run()
	{
		_running = true;
		_lastException = null;

		try
		{
			LOG.info( "Initializing monitor." );
			initialize();

			String lastException = null;
			final int pollTime = getPollTime();
			while ( !Thread.interrupted() )
			{
				try
				{
					poll();
					_lastPolled = System.currentTimeMillis();
					_lastException = null;
				}
				catch ( final Exception e )
				{
					_lastException = e;
					final StringWriter w = new StringWriter();
					e.printStackTrace( new PrintWriter( w, true ) );
					final String exceptionMessage = w.toString();
					if ( TextTools.equals( exceptionMessage, lastException ) )
					{
						LOG.error( "Again: " + e.getClass() + ": " + e.getMessage() );
					}
					else
					{
						LOG.error( "Monitor failed to poll: " + e, e );
						lastException = exceptionMessage;
					}
				}

				try
				{
					Thread.sleep( pollTime );
				}
				catch ( final InterruptedException ignored )
				{
					break;
				}
			}
		}
		catch ( final Exception e )
		{
			LOG.error( "Failed to initialize monitor.", e );
			_lastException = e;
		}
		finally
		{
			_running = false;
			try
			{
				LOG.info( "Shutting down monitor." );
				stop();
			}
			catch ( final Exception e )
			{
				LOG.error( "Failed to (cleanly) shutdown monitor.", e );
			}
		}
	}
}
