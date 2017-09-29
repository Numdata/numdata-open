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
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.numdata.oss.net.*;
import com.numdata.servlets.test.*;

/**
 * Special {@link Client} for testing purposes, that communicates directly with
 * a {@link Server}.
 *
 * @author  G. Meinders
 */
public class TestClient
	extends Client
{
	/**
	 * Server that handles requests made by the client.
	 */
	private final Server _server;

	/**
	 * Servlet context to use for test.
	 */
	private final ServletContext _servletContext;

	/**
	 * Servlet request to use for test.
	 */
	private final HttpServletRequest _servletRequest;

	/**
	 * Informant about the authenticated user.
	 */
	private final AuthenticationInformant _informant;

	/**
	 * Construct new test client that accesses the specified server.
	 *
	 * @param   server  Server that handles the client's requests.
	 * @param   domain  Domain name (optional).
	 * @param   user    User (login) name (optional).
	 */
	public TestClient( final Server server, final String domain, final String user )
	{
		_server = server;
		final ServletTestContext servletContext = new ServletTestContext( server.getClass() );
		_servletContext = servletContext;
		try
		{
			_servletRequest = new HttpServletTestRequest( servletContext, new URL( "http", domain, 80, "/test/" ) );
		}
		catch ( MalformedURLException e )
		{
			throw new AssertionError( e );
		}
		_informant = new SimpleAuthenticationInformant( domain, user );
	}

	@Override
	protected Packet transceive( final Packet request )
		throws IOException
	{
		/*
		 * Basically, the following is overkill, since we can simply send the
		 * request straight to the server and returns its response, but we use
		 * the {@link Protocol} here anyway to make sure everything works at the
		 * protocol level (where serialization takes place) aswell.
		 */
		final ByteArrayOutputStream requestOut = new ByteArrayOutputStream();
		Protocol.send( requestOut, request );
		final byte[] requestBytes = requestOut.toByteArray();

		final ByteArrayInputStream requestIn = new ByteArrayInputStream( requestBytes );
		final Packet receivedRequest = Protocol.receive( requestIn, requestBytes.length );
		if ( requestIn.available() > 0 )
		{
			throw new AssertionError( "trailing garbage is sent request" );
		}

		final Packet response = _server.handleRequest( _servletContext, _servletRequest, _informant, receivedRequest );

		final ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
		Protocol.send( responseOut, response );
		final byte[] responseBytes = responseOut.toByteArray();

		final ByteArrayInputStream responseIn = new ByteArrayInputStream( responseBytes );
		final Packet receivedResponse = Protocol.receive( responseIn, responseBytes.length );
		if ( responseIn.available() > 0 )
		{
			throw new AssertionError( "trailing garbage is sent request" );
		}

		return receivedResponse;
	}
}
