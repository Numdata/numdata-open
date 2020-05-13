/*
 * Copyright (c) 2014-2020, Numdata BV, The Netherlands.
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
package com.numdata.servlets.server;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.net.*;
import org.jetbrains.annotations.*;

/**
 * Handles 'ping' requests.
 *
 * @author Peter S. Heijnen
 */
public class ErrorReportHandler
implements RequestHandler
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( ErrorReportHandler.class );

	@Override
	@Nullable
	public Serializable handleRequest( @NotNull final ServletContext servletContext, @NotNull final HttpServletRequest servletRequest, @NotNull final AuthenticationInformant informant, @NotNull final Packet request )
	throws ProtocolException
	{
		final Serializable result;

		if ( "reportError".equals( request.getMessage() ) )
		{
			final String title = request.getAttribute( "title", String.class );
			final String message = request.getAttribute( "message", String.class, "(no message given)" );
			final RemoteException cause = request.getAttribute( "cause", RemoteException.class, null );
			LOG.info( "Received 'errorReport' from '" + servletRequest.getRemoteUser() + '@' + servletRequest.getRemoteHost() + "' error report with title=" + title + ", message=" + message + ", cause=" + cause, cause );
			result = "logged";
		}
		else
		{
			result = NOT_HANDLED;
		}

		return result;
	}
}
