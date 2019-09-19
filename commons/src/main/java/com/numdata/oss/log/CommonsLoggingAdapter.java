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
package com.numdata.oss.log;

import java.util.*;

import org.apache.commons.logging.*;

/**
 * This adapter implements the {@link LogFactory} class of the Apache Commons
 * Logging library and forwards all calls to underlying {@link ClassLogger}
 * instances.
 *
 * @author  Peter S. Heijnen
*/
public class CommonsLoggingAdapter
	extends LogFactory
{
	/**
	 * Get the {@link CommonsLoggingAdapter} as {@link LogFactory}.
	 */
	public static void register()
	{
		final String className = CommonsLoggingAdapter.class.getName();
		try
		{
			System.setProperty( FACTORY_PROPERTY, className );
		}
		catch ( SecurityException e )
		{
			System.err.println( "Failed to register " + className + " (" + e + ')' );
		}
	}

	/**
	 * Attributes.
	 */
	final Map<String,Object> _attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute( final String name )
	{
		return _attributes.get( name );
	}

	@Override
	public String[] getAttributeNames()
	{
		final Set<String> keySet = _attributes.keySet();
		return keySet.toArray( new String[ keySet.size() ] );
	}

	@Override
	public Log getInstance( final Class clazz )
			throws LogConfigurationException
	{
		return new LogImpl( ClassLogger.getFor( clazz ) );
	}

	@Override
	public Log getInstance( final String name )
			throws LogConfigurationException
	{
		return new LogImpl( new ClassLogger( name ) );
	}

	@Override
	public void release()
	{
	}

	@Override
	public void removeAttribute( final String name )
	{
		_attributes.remove( name );
	}

	@Override
	public void setAttribute( final String name, final Object value )
	{
		if ( value == null )
		{
			removeAttribute( name );
		}
		else
		{
			_attributes.put( name, value );
		}
	}

	/**
	 * {@link Log} implementation.
	 */
	private static class LogImpl
		implements Log
	{
		/**
		 * {@link ClassLogger} used to do actual logging.
		 */
		private final ClassLogger _classLogger;

		/**
		 * Create {@link Log} implementation.
		 *
		 * @param   classLogger     Underlying logger to use.
		 */
		private LogImpl( final ClassLogger classLogger )
		{
			_classLogger = classLogger;
		}

		@Override
		public boolean isDebugEnabled()
		{
			return _classLogger.isDebugEnabled();
		}

		@Override
		public boolean isErrorEnabled()
		{
			return _classLogger.isErrorEnabled();
		}

		@Override
		public boolean isFatalEnabled()
		{
			return _classLogger.isFatalEnabled();
		}

		@Override
		public boolean isInfoEnabled()
		{
			return _classLogger.isInfoEnabled();
		}

		@Override
		public boolean isTraceEnabled()
		{
			return _classLogger.isTraceEnabled();
		}

		@Override
		public boolean isWarnEnabled()
		{
			return _classLogger.isWarnEnabled();
		}

		@Override
		public void trace( final Object message )
		{
			_classLogger.trace( String.valueOf( message ) );
		}

		@Override
		public void trace( final Object message, final Throwable t )
		{
			_classLogger.trace( String.valueOf( message ), t );
		}

		@Override
		public void debug( final Object message )
		{
			_classLogger.debug( String.valueOf( message ) );
		}

		@Override
		public void debug( final Object message, final Throwable t )
		{
			_classLogger.debug( String.valueOf( message ), t );
		}

		@Override
		public void info( final Object message )
		{
			_classLogger.info( String.valueOf( message ) );
		}

		@Override
		public void info( final Object message, final Throwable t )
		{
			_classLogger.info( String.valueOf( message ), t );
		}

		@Override
		public void warn( final Object message )
		{
			_classLogger.warn( String.valueOf( message ) );
		}

		@Override
		public void warn( final Object message, final Throwable t )
		{
			_classLogger.warn( String.valueOf( message ), t );
		}

		@Override
		public void error( final Object message )
		{
			_classLogger.error( String.valueOf( message ) );
		}

		@Override
		public void error( final Object message, final Throwable t )
		{
			_classLogger.error( String.valueOf( message ), t );
		}

		@Override
		public void fatal( final Object message )
		{
			_classLogger.fatal( String.valueOf( message ) );
		}

		@Override
		public void fatal( final Object message, final Throwable t )
		{
			_classLogger.fatal( String.valueOf( message ), t );
		}
	}
}
