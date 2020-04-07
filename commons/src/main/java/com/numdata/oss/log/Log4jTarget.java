/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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

import org.apache.logging.log4j.*;

/**
 * This class implements {@link LogTarget} to provide Log4j support.
 *
 * @author Peter S. Heijnen
 * @see ClassLogger
 * @see LogTarget
 */
public class Log4jTarget
implements LogTarget
{
	@Override
	public boolean isLevelEnabled( final String name, final int level )
	{
		final boolean result;

		final Logger logger = LogManager.getLogger( name );

		switch ( level )
		{
			case ClassLogger.FATAL:
				result = logger.isFatalEnabled();
				break;

			case ClassLogger.ERROR:
				result = logger.isErrorEnabled();
				break;

			case ClassLogger.WARN:
				result = logger.isWarnEnabled();
				break;

			case ClassLogger.INFO:
				result = logger.isInfoEnabled();
				break;

			case ClassLogger.DEBUG:
				result = logger.isDebugEnabled();
				break;

			case ClassLogger.TRACE:
				result = logger.isTraceEnabled();
				break;

			default:
				result = false;
		}

		return result;
	}

	@Override
	public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		final Logger logger = LogManager.getLogger( name );

		switch ( level )
		{
			case ClassLogger.FATAL:
				logger.fatal( message, throwable );
				break;

			case ClassLogger.ERROR:
				logger.error( message, throwable );
				break;

			case ClassLogger.WARN:
				logger.warn( message, throwable );
				break;

			case ClassLogger.INFO:
				logger.info( message, throwable );
				break;

			case ClassLogger.DEBUG:
				logger.debug( message, throwable );
				break;

			case ClassLogger.TRACE:
				logger.trace( message, throwable );
				break;
		}
	}
}
