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
package com.numdata.oss.log;

import java.util.logging.*;

/**
 * This adapter provides redirect log messages from the JDK logging API to our
 * own {@link ClassLogger}. This is done by installing our own implementation of
 * the JDK's log {@link Handler}.
 *
 * @author  Peter S. Heijnen
*/
public class JdkLoggingAdapter
{
	/**
	 * Make our own handler the only handler of the JDK's root logger.
	 */
	public static void register()
	{
		try
		{
			System.setProperty( "java.util.logging.config.class", Object.class.getName() );
		}
		catch ( Throwable t )
		{
			System.err.println( "Unable to override JDK logging configuration class: " + t );
			t.printStackTrace();
		}

		try
		{
			final Logger rootLogger = Logger.getLogger( "" );
			for ( final Handler handler : rootLogger.getHandlers() )
			{
				rootLogger.removeHandler( handler );
			}
			rootLogger.addHandler( new HandlerImpl() );

			final LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration();
		}
		catch ( Throwable t )
		{
			System.err.println( "Unable to set install our JDK logging handler: " + t );
			t.printStackTrace();
		}
	}

	/**
	 * This class is not supposed to be instantiated.
	 */
	private JdkLoggingAdapter()
	{
	}

	private static class HandlerImpl
		extends Handler
	{
		@Override
		public void publish( final LogRecord record )
		{
			final Level jdkLevel = record.getLevel();
			final int jdkIntLevel = jdkLevel.intValue();

			final int level = ( jdkIntLevel <= Level.FINER.intValue() ) ? ClassLogger.TRACE :
			                     ( jdkIntLevel <= Level.FINE.intValue() ) ? ClassLogger.DEBUG :
			                     ( jdkIntLevel <= Level.INFO.intValue() ) ? ClassLogger.INFO :
			                     ( jdkIntLevel <= Level.WARNING.intValue() ) ? ClassLogger.WARN : ClassLogger.FATAL;

			final Throwable throwable = record.getThrown();
			final String message = record.getMessage();
			final String name = record.getLoggerName();

			ClassLogger.log( name, level, message, throwable );
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void close()
		{
		}
	}
}
