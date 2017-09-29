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

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides a client implementation.
 *
 * @author Peter S. Heijnen
 * @see Server
 */
public abstract class Client
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( Client.class );

	/**
	 * Ping the server to verify the connection, and keep it alive.
	 *
	 * @throws IOException if a communication/authentication error occurs.
	 */
	public void ping()
	throws IOException
	{
		LOG.debug( "ping()" );
		final Packet request = new Packet( "ping" );
		final String response = request( request, String.class, false );
		if ( !"pong".equals( response ) )
		{
			throw new ProtocolException( "Got '" + response + "' instead of 'pong'." );
		}
	}

	/**
	 * Ping the server to verify the connection, and keep it alive.
	 *
	 * @throws IOException if a communication/authentication error occurs.
	 */
	@SuppressWarnings ( "TypeMayBeWeakened" )
	public void reportError( @Nullable final String title, @Nullable final String message, @Nullable final Throwable cause )
	throws IOException
	{
		LOG.debug( "reportError( " + title + ", " + message + ", " + cause + " )" );

		final Packet request = new Packet( "reportError" );
		request.setAttribute( "title", title );
		request.setAttribute( "message", message );
		request.setAttribute( "cause", ( cause == null ) ? null : ( cause instanceof RemoteException ) ? ((RemoteException)cause) : new RemoteException( cause ) );
		request( request, String.class, false );
	}

	/**
	 * Send a request to the server and returns the object returned by the server
	 * in response, if any.
	 *
	 * @param request    Request packet.
	 * @param returnType Type of returned object ({@code null} => any).
	 * @param allowNull  Allow {@code null} to be returned.
	 *
	 * @return Object sent by the server as a response.
	 *
	 * @throws IOException if an I/O error occurs or if the server throws it.
	 * @throws ProtocolException if the client or server did not follow the
	 * protocol.
	 * @throws RuntimeException if it is thrown by the server.
	 */
	public <T> T request( final Packet request, final Class<T> returnType, final boolean allowNull )
	throws IOException
	{
		final long start = System.nanoTime();
		final Packet response = transceive( request );
		if ( LOG.isDebugEnabled() )
		{
			final long end = System.nanoTime();
			LOG.debug( "request(): message='" + request.getMessage() + "', attributes='" + request.getAttributes() + "', time=" + ( ( end - start ) / 1000000L ) + "ms" );
		}

		/*
		 * Process response code.
		 *
		 * Errors will caused the appropriate exception.
		 */
		final Object result;

		final String responseMessage = response.getMessage();

		if ( "ack".equals( responseMessage ) )
		{
			result = response.getAttribute( "response" );
		}
		else if ( "err".equals( responseMessage ) )
		{
			final Exception exception = response.getAttribute( "exception", Exception.class );

			if ( exception instanceof IOException )
			{
				throw (IOException)exception;
			}

			if ( exception instanceof RuntimeException )
			{
				throw (RuntimeException)exception;
			}
			else
			{
				throw new RuntimeException( exception.getMessage(), exception );
			}
		}
		else
		{
			throw new ProtocolException( "unknown response code: " + responseMessage );
		}

		/*
		 * Check response object.
		 */
		if ( result == null )
		{
			if ( !allowNull )
			{
				throw new ProtocolException( "Got 'null' for '" + request + '\'' );
			}
		}
		else if ( ( returnType != null ) && !returnType.isInstance( result ) )
		{
			final Class<?> resultType = result.getClass();
			throw new ProtocolException( "Expected '" + returnType.getName() + "', but was '" + resultType.getName() + "'." );
		}

		return (T)result;
	}

	/**
	 * Transmit request to server and receive response.
	 *
	 * @param request Request to server.
	 *
	 * @return Response from server.
	 *
	 * @throws IOException if the connection could not be established.
	 */
	protected abstract Packet transceive( final Packet request )
	throws IOException;
}
