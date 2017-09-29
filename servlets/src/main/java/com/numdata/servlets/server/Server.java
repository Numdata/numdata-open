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
package com.numdata.servlets.server;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.net.*;
import org.jetbrains.annotations.*;

/**
 * This class provides a server implementation.
 *
 * @author Peter S. Heijnen
 */
public class Server
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( Server.class );

	/**
	 * Request handlers.
	 */
	private final List<RequestHandler> _requestHandlers;

	/**
	 * Construct server.
	 *
	 * @param defaultHandlers Default request handlers.
	 */
	public Server( final RequestHandler... defaultHandlers )
	{
		_requestHandlers = new ArrayList<RequestHandler>();
		addRequestHandler( new PingRequestHandler() );

		boolean haveErrorReportHandler = false;
		if ( defaultHandlers != null )
		{
			for ( final RequestHandler requestHandler : defaultHandlers )
			{
				if ( requestHandler instanceof ErrorReportHandler )
				{
					haveErrorReportHandler = true;
				}
				addRequestHandler( requestHandler );
			}
		}

		if ( !haveErrorReportHandler )
		{
			addRequestHandler( new ErrorReportHandler() );
		}
	}

	/**
	 * Add request handler to this server.
	 *
	 * @param requestHandler Request handler to add.
	 */
	public void addRequestHandler( @NotNull final RequestHandler requestHandler )
	{
		LOG.entering( "addRequestHandler", "requestHandler", requestHandler );

		_requestHandlers.add( 0, requestHandler );

		LOG.exiting( "addRequestHandler" );
	}

	/**
	 * Handle request from client and prepare response.
	 *
	 * @param servletContext Servlet context for server.
	 * @param servletRequest Servlet request being handled.
	 * @param informant      Informant about the authenticated user.
	 * @param request        Request packet.
	 *
	 * @return Response packet.
	 */
	@NotNull
	protected Packet handleRequest( @NotNull final ServletContext servletContext, @NotNull final HttpServletRequest servletRequest, @NotNull final AuthenticationInformant informant, @NotNull final Packet request )
	{
		LOG.entering( "handleRequest", "servletContext", servletContext, "servletRequest", servletRequest, "informant", informant, "request", request );

		Packet result = null;

		try
		{
			for ( final RequestHandler requestHandler : _requestHandlers )
			{
				final Serializable response = requestHandler.handleRequest( servletContext, servletRequest, informant, request );
				if ( response != RequestHandler.NOT_HANDLED )
				{
					result = new Packet( "ack" );
					result.setAttribute( "response", response );
					break;
				}
			}

		}
		catch ( Exception e )
		{
			LOG.info( "Failed '" + request.getMessage() + "' request for " + informant.getUserName() + '@' + informant.getDomainName() + ": " + e, e );
			result = new Packet( "err" );
			result.setAttribute( "exception", new RemoteException( e ) );
		}

		if ( result == null )
		{
			LOG.info( "Received unsupported '" + request.getMessage() + "' request from " + informant.getUserName() + '@' + informant.getDomainName() );
			result = new Packet( "err" );
			result.setAttribute( "exception", new UnsupportedRequestException( request.getMessage() ) );
		}

		return LOG.exiting( "handleRequest", result );
	}
}
