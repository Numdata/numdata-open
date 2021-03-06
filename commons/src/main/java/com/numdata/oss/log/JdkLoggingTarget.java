/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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
 * This class implements {@link LogTarget} to provide java.util.logging
 * support.
 *
 * @author Gerrit Meinders
 */
public final class JdkLoggingTarget
implements LogTarget
{
	public boolean isLevelEnabled( final String name, final int level )
	{
		final boolean result;

		final Logger logger = Logger.getLogger( name );

		switch ( level )
		{
			case ClassLogger.FATAL:
				result = logger.isLoggable( Level.SEVERE );
				break;
			case ClassLogger.ERROR:
				result = logger.isLoggable( Level.SEVERE );
				break;
			case ClassLogger.WARN:
				result = logger.isLoggable( Level.WARNING );
				break;
			case ClassLogger.INFO:
				result = logger.isLoggable( Level.INFO );
				break;
			case ClassLogger.DEBUG:
				result = logger.isLoggable( Level.FINE );
				break;
			case ClassLogger.TRACE:
				result = logger.isLoggable( Level.FINER );
				break;
			default:
				result = false;
		}

		return result;
	}

	public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		final LogRecord record;

		if ( level <= ClassLogger.FATAL )
		{
			record = new LogRecord( Level.SEVERE, message );
		}
		else if ( level == ClassLogger.ERROR )
		{
			record = new LogRecord( Level.SEVERE, message );
		}
		else if ( level == ClassLogger.WARN )
		{
			record = new LogRecord( Level.WARNING, message );
		}
		else if ( level == ClassLogger.INFO )
		{
			record = new LogRecord( Level.INFO, message );
		}
		else if ( level == ClassLogger.DEBUG )
		{
			record = new LogRecord( Level.FINE, message );
		}
		else //if ( level >= ClassLogger.TRACE )
		{
			record = new LogRecord( Level.FINER, message );
		}

		record.setLoggerName( name );
		record.setSourceClassName( name );
		record.setSourceMethodName( null );
		record.setThrown( throwable );

		final Logger logger = Logger.getLogger( name );
		logger.log( record );
	}
}
