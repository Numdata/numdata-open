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
package com.numdata.oss.velocity;

import com.numdata.oss.log.*;
import org.apache.velocity.app.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.runtime.log.*;

/**
 * {@link LogChute} implementation that redirects to a {@link ClassLogger}.
 *
 * @author  Peter S. Heijnen
 */
public class VelocityClassLoggerChute
	implements LogChute
{
	/**
	 * {@link ClassLogger} to which log messages are sent.
	 */
	private ClassLogger _logger = null;

	/**
	 * Construct log chute.
	 */
	public VelocityClassLoggerChute()
	{
	}

	@Override
	public void init( final RuntimeServices rs )
		throws Exception
	{
		_logger = ClassLogger.getFor( VelocityEngine.class );
	}

	@Override
	public void log( final int level, final String message )
	{
		final ClassLogger logger = _logger;

		switch ( level )
		{
			case LogChute.ERROR_ID:
				logger.error( message );
				break;

			case LogChute.WARN_ID:
				logger.warn( message );
				break;

			case LogChute.INFO_ID:
				logger.info( message );
				break;

			case LogChute.DEBUG_ID:
			default:
				logger.debug( message );
				break;

			case LogChute.TRACE_ID:
				logger.trace( message );
				break;
		}
	}

	@Override
	public void log( final int level, final String message, final Throwable t )
	{
		final ClassLogger logger = _logger;

		switch ( level )
		{
			case LogChute.ERROR_ID:
				logger.error( message, t );
				break;

			case LogChute.WARN_ID:
				logger.warn( message, t );
				break;

			case LogChute.INFO_ID:
				logger.info( message, t );
				break;

			case LogChute.DEBUG_ID:
			default:
				logger.debug( message, t );
				break;

			case LogChute.TRACE_ID:
				logger.trace( message, t );
				break;
		}
	}

	@Override
	public boolean isLevelEnabled( final int level )
	{
		final boolean result;

		final ClassLogger logger = _logger;

		switch ( level )
		{
			case LogChute.ERROR_ID:
				result = logger.isErrorEnabled();
				break;

			case LogChute.WARN_ID:
				result = logger.isWarnEnabled();
				break;

			case LogChute.INFO_ID:
				result = logger.isInfoEnabled();
				break;

			case LogChute.DEBUG_ID:
			default:
				result = logger.isDebugEnabled();
				break;

			case LogChute.TRACE_ID:
				result = logger.isTraceEnabled();
				break;
		}

		return result;
	}
}
