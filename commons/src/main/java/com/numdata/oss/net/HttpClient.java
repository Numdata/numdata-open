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
package com.numdata.oss.net;

import java.io.*;
import java.net.*;

import com.numdata.oss.log.*;
import static com.numdata.oss.net.SimpleHttpClient.*;
import org.jetbrains.annotations.*;

/**
 * This class contains a HTTP client implementation.
 *
 * @author Peter S. Heijnen
 * @see ServerServlet
 */
public class HttpClient
extends Client
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( HttpClient.class );

	/**
	 * URL to the server.
	 */
	private final URL _serverURL;

	/**
	 * HTTP client to connect to server.
	 */
	@NotNull
	private final SimpleHttpClient _httpClient;

	/**
	 * Construct client.
	 *
	 * @param httpClient HTTP client to connect to server.
	 * @param serverURL  URL to server.
	 */
	public HttpClient( @NotNull final SimpleHttpClient httpClient, @NotNull final URL serverURL )
	{
		LOG.debug( "HttpClient( '" + serverURL + "' )" );
		_httpClient = httpClient;
		_serverURL = serverURL;
	}

	@Override
	protected Packet transceive( @NotNull final Packet request )
	throws IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.send( baos, request );
		final byte[] requestContent = baos.toByteArray();

		final SimpleHttpClient httpClient = _httpClient;
		final SimpleHttpClient.Connection connection = httpClient.createConnection( SimpleHttpClient.POST, _serverURL, false, false, true );
		connection.sendContent( APPLICATION_OCTET_STREAM, requestContent );
		connection.requireSuccessfulResponse();
		connection.requireResponseContentType( APPLICATION_OCTET_STREAM );

		final int contentLength = connection.getContentLength();
		if ( contentLength < 0 )
		{
			throw new ProtocolException( "missing Content-Length in response" );
		}

		final InputStream in = connection.getInputStream();
		try
		{
			return Protocol.receive( in, contentLength );
		}
		finally
		{
			in.close();
		}
	}

}
