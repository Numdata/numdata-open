/*
 * Copyright (c) 2011-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.io.*;
import java.net.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Opens a server socket to accept incoming connections. If the socket is
 * closed, the monitor will automatically attempt to reopen it.
 *
 * @author Peter S. Heijnen.
 */
@SuppressWarnings( "WeakerAccess" )
public class ServerSocketMonitor
implements ResourceMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( ServerSocketMonitor.class );

	/**
	 * TCP port to listen on for incoming connections.
	 */
	private int _port;

	/**
	 * Server socket.
	 */
	@Nullable
	private ServerSocket _serverSocket = null;

	/**
	 * Monitor is stopped.
	 */
	private boolean _stopped = false;

	/**
	 * Last exception that occurred.
	 */
	@Nullable
	private Exception _lastException = null;

	/**
	 * Last time that the connection was established.
	 */
	private long _lastConnected = -1;

	/**
	 * Connection handler.
	 */
	@Nullable
	private ConnectionHandler _handler = null;

	/**
	 * Constructs a new barcode monitor for the specified CNC machine.
	 *
	 * @param port TCP port to listen for incoming connections.
	 */
	public ServerSocketMonitor( final int port )
	{
		_port = port;
	}

	public int getPort()
	{
		return _port;
	}

	public void setPort( final int port )
	{
		_port = port;
	}

	@Nullable
	public ConnectionHandler getHandler()
	{
		return _handler;
	}

	public void setHandler( @Nullable final ConnectionHandler handler )
	{
		_handler = handler;
	}

	@Nullable
	public Exception getLastException()
	{
		return _lastException;
	}

	public long getLastConnected()
	{
		return _lastConnected;
	}

	@NotNull
	@Override
	public String getName()
	{
		String host = null;
		int port = -1;

		final ServerSocket serverSocket = _serverSocket;
		if ( serverSocket != null )
		{
			final InetAddress inetAddress = serverSocket.getInetAddress();
			if ( inetAddress != null )
			{
				host = inetAddress.getHostName();
				if ( host == null )
				{
					host = inetAddress.getHostAddress();
				}
			}

			port = serverSocket.getLocalPort();
		}

		if ( host == null )
		{
			host = "0.0.0.0";
		}

		if ( port < 0 )
		{
			port = _port;
		}

		return "socket://" + host + ':' + port;
	}

	@NotNull
	@Override
	public ResourceStatus getStatus()
	{
		final ResourceStatus result = new ResourceStatus();
		result.setException( getLastException() );
		result.setLastOnline( getLastConnected() );

		if ( isStopped() )
		{
			result.setStatus( ResourceStatus.Status.UNAVAILABLE );
			result.setDetails( "Stopped" );
		}
		else
		{
			final ServerSocket serverSocket = _serverSocket;
			if ( serverSocket == null )
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "Not started yet" );
			}
			else if ( serverSocket.isClosed() )
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "Closed (" + serverSocket + ')' );
			}
			else
			{
				result.setStatus( ResourceStatus.Status.AVAILABLE );
				result.setDetails( "Listening (" + serverSocket + ')' );
				result.setLastOnline( _lastConnected = System.currentTimeMillis() );
			}
		}
		return result;
	}

	@Override
	public void run()
	{
		_stopped = false;
		_lastException = null;

		while ( !isStopped() )
		{
			try
			{
				final ServerSocket serverSocket = new ServerSocket( _port );
				try
				{
					_serverSocket = serverSocket;

					LOG.info( "Listening on TCP port " + _port );
					while ( !isStopped() && !serverSocket.isClosed() )
					{
						try
						{
							_lastException = null;
							final Socket socket = serverSocket.accept();
							try
							{
								_lastConnected = System.currentTimeMillis();
								LOG.debug( "Accepted connection from " + socket.getRemoteSocketAddress() + '.' );
								handleConnection( socket );
								LOG.debug( "Connection handled." );
							}
							catch ( final RuntimeException e )
							{
								_lastException = e;
								LOG.error( "Unhandled exception while processing client request: " + e, e );
							}
							finally
							{
								try
								{
									socket.close();
								}
								catch ( final IOException ignored )
								{
								}
							}
						}
						catch ( final IOException e )
						{
							_lastException = e;
							if ( serverSocket.isClosed() )
							{
								LOG.debug( "Exception while socket was closed: " + e, e );
							}
							else
							{
								LOG.warn( "Failed to accept client connection.", e );
							}
						}
					}
				}
				finally
				{
					if ( !serverSocket.isClosed() )
					{
						try
						{
							serverSocket.close();
							LOG.info( "Server socket closed." );
						}
						catch ( final IOException e )
						{
							LOG.warn( "Failed to close server socket.", e );
						}
					}
				}
				_serverSocket = null;
			}
			catch ( final IOException e )
			{
				LOG.error( "Failed to open server socket.", e );

			}
			catch ( final Throwable e )
			{
				LOG.error( "Fatal exception!", e );
			}
		}

		LOG.info( "ServerSocketMonitor stopped." );
	}

	@Override
	public void stop()
	{
		LOG.trace( "stop()" );

		_stopped = true;

		final ServerSocket serverSocket = _serverSocket;
		if ( serverSocket != null )
		{
			try
			{
				serverSocket.close();
			}
			catch ( final Exception e )
			{
				/* ignore socket close problems */
			}
		}
	}

	public boolean isStopped()
	{
		return _stopped;
	}

	/**
	 * Handles an incoming connection.
	 *
	 * @param socket Connected client socket.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void handleConnection( @NotNull final Socket socket )
	throws IOException
	{
		final ConnectionHandler handler = getHandler();
		if ( handler == null )
		{
			throw new IllegalStateException( "No handler installed" );
		}

		handler.handleConnection( socket );
	}

	/**
	 * Handler for an incoming connection.
	 */
	public interface ConnectionHandler
	{
		/**
		 * Handles an incoming connection.
		 *
		 * @param socket Connected client socket.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		void handleConnection( @NotNull Socket socket )
		throws IOException;
	}
}
