/*
 * Copyright (c) 2010-2020, Numdata BV, The Netherlands.
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
@SuppressWarnings( { "WeakerAccess", "unused" } )
public class PollingMonitor
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
	 * Initial time delay to use after an exception occurred.
	 */
	private int _initialExceptionDelay = 60000;

	/**
	 * Maximum delay after a repeated exception occurred.
	 */
	private int _maximumExceptionMaxDelay = 300000;

	/**
	 * Whether the monitor is running.
	 */
	private boolean _running = false;

	/**
	 * Whether monitor should stop.
	 */
	private boolean _stop = false;

	/**
	 * Last exception that occurred.
	 */
	@Nullable
	private Exception _lastException = null;

	/**
	 * Fail count.
	 */
	private int _failCount = 0;

	/**
	 * Last time that the connection was established.
	 */
	private long _lastPolled = -1;

	/**
	 * Name of this monitor. A default name is generated if set to {@code null}.
	 */
	@Nullable
	private String _name = null;

	/**
	 * Initializer called by {@link #initialize()} method.
	 */
	@Nullable
	private CheckedRunnable<?> _initializer = null;

	/**
	 * Check called by {@link #poll()} method.
	 */
	@Nullable
	private CheckedRunnable<?> _check = null;

	/**
	 * Finalizer called by {@link #stopped()} method.
	 */
	@Nullable
	private Runnable _finalizer = null;

	/**
	 * Constructs a new monitor that polls an entity at regular intervals.
	 *
	 * @param pollTime Time to wait after polling the entity.
	 */
	public PollingMonitor( final int pollTime )
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

	public int getInitialExceptionDelay()
	{
		return _initialExceptionDelay;
	}

	public void setInitialExceptionDelay( final int initialExceptionDelay )
	{
		_initialExceptionDelay = initialExceptionDelay;
	}

	public int getMaximumExceptionMaxDelay()
	{
		return _maximumExceptionMaxDelay;
	}

	public void setMaximumExceptionMaxDelay( final int maximumExceptionMaxDelay )
	{
		_maximumExceptionMaxDelay = maximumExceptionMaxDelay;
	}

	@Nullable
	public CheckedRunnable<?> getInitializer()
	{
		return _initializer;
	}

	public void setInitializer( @Nullable final CheckedRunnable<?> initializer )
	{
		_initializer = initializer;
	}

	@Nullable
	public CheckedRunnable<?> getCheck()
	{
		return _check;
	}

	public void setCheck( @Nullable final CheckedRunnable<?> check )
	{
		_check = check;
	}

	@Nullable
	public Runnable getFinalizer()
	{
		return _finalizer;
	}

	public void setFinalizer( @Nullable final Runnable finalizer )
	{
		_finalizer = finalizer;
	}

	@NotNull
	@Override
	public String getName()
	{
		return ( _name != null ) ? _name : "PollingMonitor (polling time: " + getPollTime() + "ms, " + ( ( getCheck() != null ) ? ", check: " + getCheck() : "" );
	}

	public void setName( @Nullable final String name )
	{
		_name = name;
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

	public int getFailCount()
	{
		return _failCount;
	}

	public long getLastPolled()
	{
		return _lastPolled;
	}

	@NotNull
	@Override
	public ResourceStatus getStatus()
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( '[' + getName() + "] getStatus()" );
		}

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
	protected void initialize()
	throws Exception
	{
		final CheckedRunnable<?> initializer = getInitializer();

		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( '[' + getName() + "] initialize() initializer=" + initializer );
		}

		if ( initializer != null )
		{
			initializer.run();
		}
	}

	/**
	 * Polls the monitored entity. If an exception is thrown, the monitor keeps
	 * running and will continue polling normally after the polling time has
	 * passed.
	 *
	 * @throws Exception if any exception occurs while polling.
	 */
	protected void poll()
	throws Exception
	{
		final CheckedRunnable<?> check = getCheck();

		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( '[' + getName() + "] poll() check=" + check );
		}

		if ( check != null )
		{
			check.run();
		}
	}

	@Override
	public void run()
	{
		_stop = false;
		_running = true;
		_lastException = null;

		try
		{
			LOG.info( '[' + getName() + "] Initializing monitor" );
			initialize();

			String lastException = null;
			while ( !Thread.interrupted() && !_stop )
			{
				try
				{
					poll();
					_lastPolled = System.currentTimeMillis();
					_lastException = null;
					_failCount = 0;
				}
				catch ( final Exception e )
				{
					_lastException = e;
					_failCount++;
					final StringWriter w = new StringWriter();
					e.printStackTrace( new PrintWriter( w, true ) );
					final String exceptionMessage = w.toString();
					if ( TextTools.equals( exceptionMessage, lastException ) )
					{
						LOG.error( '[' + getName() + "] Again: " + e.getClass() + ": " + e.getMessage() );
					}
					else
					{
						LOG.error( '[' + getName() + "] Monitor failed to poll: " + e, e );
						lastException = exceptionMessage;
					}
				}

				final int failCount = getFailCount();
				final int initialDelay;
				final int maximumDelay;
				final int delay;
				if ( failCount > 0 )
				{
					initialDelay = Math.max( 1000, getInitialExceptionDelay() );
					maximumDelay = Math.min( Math.max( 60000, getMaximumExceptionMaxDelay() ), 3600000 );
					delay = Math.min( initialDelay * failCount, maximumDelay );
				}
				else
				{
					delay = getPollTime();
				}

				sleep( delay );
			}
		}
		catch ( final Exception e )
		{
			LOG.error( '[' + getName() + "] Failed to initialize monitor: " + e, e );
			_lastException = e;
		}
		finally
		{
			_running = false;
			_stop = false;
			try
			{
				LOG.info( '[' + getName() + "] Shutting down monitor." );
				stopped();
			}
			catch ( final Exception e )
			{
				LOG.error( "Failed to (cleanly) shutdown monitor.", e );
			}
		}
	}

	/**
	 * Internal helper-method to suspend the current thread for the given
	 * amount of time.
	 *
	 * @param time Time to sleep in milliseconds.
	 *
	 * @return {@code true} if process should continue normally;
	 * {@code false} if the monitor was interrupted/should stop.
	 *
	 * @throws IllegalArgumentException if the value of {@code millis} is negative
	 */
	protected boolean sleep( final int time )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( '[' + getName() + "] sleep( " + time + " )" );
		}

		boolean result = true;
		for ( int remaining = time; ( remaining > 0 ) && !_stop; remaining -= 1000 )
		{
			try
			{
				//noinspection BusyWait
				Thread.sleep( Math.min( 1000, remaining ) );
			}
			catch ( final InterruptedException ignored )
			{
				result = false;
				break;
			}
		}
		return result && !_stop;
	}

	@Override
	public void stop()
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( '[' + getName() + "] stop()" );
		}
		_stop = true;
	}

	/**
	 * This method is called when the monitor has stopped.
	 */
	protected void stopped()
	{
		final Runnable finalizer = getFinalizer();

		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( '[' + getName() + "] stopped() finalizer=" + finalizer );
		}

		if ( finalizer != null )
		{
			try
			{
				finalizer.run();
			}
			catch ( final Exception e )
			{
				LOG.error( "stop() finalizer threw exception: " + e, e );
			}
		}
	}
}
