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
package com.numdata.oss;

import java.io.*;

import com.numdata.oss.log.*;

/**
 * Skeleton implementation of a monitor that polls an entity to monitor it, as
 * opposed to an event-driven (push) approach.
 *
 * @author G. Meinders
 */
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
	private final int _polltime;

	/**
	 * Constructs a new monitor that polls an entity at regular intervals.
	 *
	 * @param polltime Time to wait after polling the entity.
	 */
	protected PollingMonitor( final int polltime )
	{
		_polltime = polltime;
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

		try
		{
			LOG.info( "Initializing monitor." );
			initialize();

			String lastException = null;
			final int polltime = _polltime;
			while ( !Thread.interrupted() )
			{
				try
				{
					poll();
				}
				catch ( final Exception e )
				{
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
					Thread.sleep( (long)polltime );
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
		}
		finally
		{
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
