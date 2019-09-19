/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
import com.numdata.oss.net.*;

/**
 * This servlet handles requests from a {@link HttpClient}.
 *
 * @author  Peter S. Heijnen
 */
public abstract class ServerServlet
	extends HttpServlet
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -2473197530726216732L;

	/**
	 * Server that handles requests.
	 */
	private Server _server;

	/**
	 * Construct server.
	 */
	protected ServerServlet()
	{
		_server = new Server();
	}

	/**
	 * Get server that handles requests.
	 *
	 * @return  Server that handles requests.
	 */
	public Server getServer()
	{
		return _server;
	}

	/**
	 * Set server to handle requests.
	 *
	 * @param   server  Server to handle requests.
	 *
	 * @throws IllegalArgumentException if {@code server} is {@code null}.
	 */
	public void setServer( final Server server )
	{
		if ( server == null )
		{
			throw new IllegalArgumentException( "null" );
		}

		_server = server;
	}

	/**
	 * Create {@link AuthenticationInformant} for the given request.
	 *
	 * @param request Request being processed.
	 *
	 * @return {@link AuthenticationInformant} for request.
	 */
	protected AuthenticationInformant createInformant( final HttpServletRequest request )
	{
		return new ServletRequestAuthenticationInformant( request );
	}

	/**
	 * Handle servlet service request.
	 *
	 * @param   servletRequest      Request to the servlet.
	 * @param   servletResponse     Response from the servlet.
	 *
	 * @throws  IOException if an I/O exception has occurred.
	 * @throws  ServletException if a servlet exception has occurred.
	 */
	@Override
	public void service( final HttpServletRequest servletRequest, final HttpServletResponse servletResponse )
		throws ServletException, IOException
	{
		/*
		 * Receive request.
		 *
		 * There seems to be a bug in the Tomcat 5.1 / Apache 2.x / JK  module
		 * chain, causing the {@link HttpServletRequest#getContentLength()} method
		 * to return {@code 0} while the request headers indicate a non-zero
		 * 'Content-Length' value. We work around this problem by using the
		 * {@link HttpServletRequest#getIntHeader(String)} method.
		 */
		final int contentLength = servletRequest.getIntHeader( "Content-Length" );
		if ( contentLength < 0 )
		{
			throw new ProtocolException( "Received request without Content-Length" );
		}

		final ServletInputStream in = servletRequest.getInputStream();
		final Packet request = Protocol.receive( in, contentLength );
		in.close();

		final String requestName = request.getMessage();
		if ( TextTools.isEmpty( requestName ) )
		{
			throw new IOException( "empty request name" );
		}

		final Class<? extends ServerServlet> thisClass = getClass();
		log( thisClass.getSimpleName() + ": " + getLogMessageForRequest( servletRequest ) + ": " + requestName );

		final ServletContext servletContext = getServletContext();
		final AuthenticationInformant informant = createInformant( servletRequest );

		/*
		 * Handle request and prepare response.
		 */
		final Packet response;
		try
		{
			final Server server = getServer();
			response = server.handleRequest( servletContext, servletRequest, informant, request );
		}
		catch ( Throwable t )
		{
			throw new ServletException( "Unexpected exception during request: " + t, t );
		}

		/*
		 * Send response.
		 */
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.send( baos, response );
		final byte[] data = baos.toByteArray();

		servletResponse.setContentType( "application/octet-stream" );
		servletResponse.setContentLength( data.length );

		final ServletOutputStream out = servletResponse.getOutputStream();
		out.write( data );
		out.close();
	}

	/**
	 * Get log message for servlet request.
	 *
	 * @param   servletRequest  Servlet request to log.
	 *
	 * @return  Log message for specified request.
	 */
	public static String getLogMessageForRequest( final HttpServletRequest servletRequest )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( servletRequest.getProtocol() );
		sb.append( ' ' );
		sb.append( servletRequest.getMethod() );
		sb.append( " (" );
		sb.append( servletRequest.getRemoteUser() );
		sb.append( ") " );
		sb.append( servletRequest.getRequestURL() );

		final String queryString = servletRequest.getQueryString();
		if ( TextTools.isNonEmpty( queryString ) )
		{
			sb.append( '?' );
			sb.append( queryString );
		}

		return sb.toString();
	}
}
