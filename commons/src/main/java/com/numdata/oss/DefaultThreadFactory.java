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
package com.numdata.oss;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.jetbrains.annotations.*;

/**
 * A typical implementation of {@link ThreadFactory}. This thread factory can be
 * configured to use a different name prefix, create daemon threads or create
 * threads with a given priority.
 *
 * @author G. Meinders
 */
public class DefaultThreadFactory
implements ThreadFactory
{
	/**
	 * Pool number for the next new factory.
	 */
	private static final AtomicInteger POOL_NUMBER = new AtomicInteger( 1 );

	/**
	 * Thread group for new threads.
	 */
	private final ThreadGroup _group;

	/**
	 * Pool number for this factory.
	 */
	private final int _poolNumber;

	/**
	 * Thread number for the next new thread.
	 */
	private final AtomicInteger _threadNumber;

	/**
	 * Unique name prefix for new threads, i.e. the plain name prefix suffixed
	 * with a unique pool number.
	 */
	private String _uniqueNamePrefix;

	/**
	 * Name prefix for new threads.
	 */
	private String _namePrefix;

	/**
	 * Whether new threads are created as daemon threads.
	 */
	private boolean _daemon;

	/**
	 * Priority for new threads.
	 */
	private int _priority;

	/**
	 * Uncaught exception handler for created threads.
	 */
	private Thread.UncaughtExceptionHandler _uncaughtExceptionHandler = null;

	/**
	 * Constructs a new thread factory.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public DefaultThreadFactory()
	{
		final SecurityManager securityManager = System.getSecurityManager();
		if ( securityManager != null )
		{
			_group = securityManager.getThreadGroup();
		}
		else
		{
			final Thread currentThread = Thread.currentThread();
			_group = currentThread.getThreadGroup();
		}

		_poolNumber = POOL_NUMBER.getAndIncrement();
		_threadNumber = new AtomicInteger( 1 );
		_uniqueNamePrefix = null;
		_namePrefix = null;
		_daemon = false;
		_priority = Thread.NORM_PRIORITY;

		final Class<?> clazz = getClass();
		setNamePrefix( clazz.getName() );
	}

	/**
	 * Returns the name prefix used for new threads.
	 *
	 * @return Name prefix.
	 */
	public String getNamePrefix()
	{
		return _namePrefix;
	}

	/**
	 * Sets the name prefix used for new threads. A pool number will be appended
	 * to the prefix, such that all threads created with factories of this class
	 * are guaranteed to have unique names.
	 *
	 * @param namePrefix Name prefix.
	 */
	public void setNamePrefix( final String namePrefix )
	{
		_namePrefix = namePrefix;
		_uniqueNamePrefix = namePrefix + '-' + _poolNumber + "-thread-";
	}

	/**
	 * Returns whether new threads are created as daemon threads.
	 *
	 * @return {@code true} if new threads are daemon threads.
	 */
	public boolean isDaemon()
	{
		return _daemon;
	}

	/**
	 * Sets whether new threads are created as daemon threads.
	 *
	 * @param daemon {@code true} to create daemon threads.
	 */
	public void setDaemon( final boolean daemon )
	{
		_daemon = daemon;
	}

	/**
	 * Returns the thread priority for new threads.
	 *
	 * @return Thread priority.
	 */
	public int getPriority()
	{
		return _priority;
	}

	/**
	 * Sets the thread priority for new threads.
	 *
	 * @param priority Thread priority.
	 */
	public void setPriority( final int priority )
	{
		_priority = priority;
	}

	/**
	 * Sets the uncaught exception handler for newly created threads. When set
	 * to {@code null}, no uncaught exception handler is set, so the thread will
	 * use the default handler.
	 *
	 * @param uncaughtExceptionHandler Uncaught exception handler.
	 */
	public void setUncaughtExceptionHandler( @Nullable final Thread.UncaughtExceptionHandler uncaughtExceptionHandler )
	{
		_uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	/**
	 * Returns the uncaught exception handler for newly created threads. When
	 * set to {@code null}, no uncaught exception handler is set, so the thread
	 * will use the default handler.
	 *
	 * @return Uncaught exception handler.
	 */
	public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler()
	{
		return _uncaughtExceptionHandler;
	}

	@Override
	public Thread newThread( @NotNull final Runnable runnable )
	{
		final Thread result = new Thread( _group, runnable, _uniqueNamePrefix + _threadNumber.getAndIncrement(), 0L );

		final boolean daemon = isDaemon();
		if ( result.isDaemon() != daemon )
		{
			result.setDaemon( daemon );
		}

		final int priority = getPriority();
		if ( result.getPriority() != priority )
		{
			result.setPriority( priority );
		}

		final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = _uncaughtExceptionHandler;
		if ( uncaughtExceptionHandler != null )
		{
			result.setUncaughtExceptionHandler( uncaughtExceptionHandler );
		}

		return result;
	}
}
