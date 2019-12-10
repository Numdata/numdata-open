/*
 * Copyright (c) 2012-2019, Numdata BV, The Netherlands.
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
 * Monitors a resource that is accessed using a (client) socket connection.
 * If the connection is broken, the monitor will automatically attempt to
 * reconnect.
 *
 * @author G. Meinders
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public class SocketMonitor
implements ResourceMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SocketMonitor.class );

	/**
	 * Host name or IP address.
	 */
	@NotNull
	private final String _host;

	/**
	 * Remote port.
	 */
	private final int _port;

	/**
	 * Stop.
	 */
	private boolean _stopped = false;

	/**
	 * Currently connected socket.
	 */
	@Nullable
	private Socket _socket = null;

	/**
	 * Connection handler.
	 */
	@Nullable
	private ConnectionHandler _handler = null;

	/**
	 * Connect timeout for the socket, in milliseconds.
	 */
	private int _connectTimeout = 10000;

	/**
	 * Delay in milliseconds before attempting to reconnect, after losing the
	 * connection.
	 */
	private int _reconnectDelay = 10000;

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
	 * Constructs a new instance.
	 *
	 * @param host Host name or IP address.
	 * @param port Remote port.
	 */
	public SocketMonitor( @NotNull final String host, final int port )
	{
		LOG.debug( "SocketMonitor( host=" + TextTools.quote( host ) + ", port=" + port + " )" );

		_host = host;
		_port = port;
	}

	@NotNull
	public String getHost()
	{
		return _host;
	}

	public int getPort()
	{
		return _port;
	}

	public int getConnectTimeout()
	{
		return _connectTimeout;
	}

	public void setConnectTimeout( final int connectTimeout )
	{
		_connectTimeout = connectTimeout;
	}

	public int getReconnectDelay()
	{
		return _reconnectDelay;
	}

	public void setReconnectDelay( final int reconnectDelay )
	{
		_reconnectDelay = reconnectDelay;
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

	@Override
	@NotNull
	public String getName()
	{
		return _host + ':' + _port;
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
			final Socket socket = _socket;
			if ( socket == null )
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "Not connected" );
			}
			else if ( socket.isConnected() )
			{
				result.setStatus( ResourceStatus.Status.AVAILABLE );
				result.setDetails( "Connected (" + socket + ')' );
			}
			else
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "Disconnected" );
			}
		}
		return result;
	}

	@Override
	public void run()
	{
		_socket = null;
		_lastException = null;

		LOG.debug( "Starting socket monitor to '" + _host + ':' + _port + '\'' );
		while ( !Thread.interrupted() && !isStopped() )
		{
			try
			{
				if ( LOG.isTraceEnabled() )
				{
					LOG.trace( "Connect to '" + _host + ':' + _port + '\'' );
				}

				final Socket socket = new Socket();
				try
				{
					socket.connect( new InetSocketAddress( _host, _port ), _connectTimeout );
					_socket = socket;
					_lastException = null;
					_lastConnected = System.currentTimeMillis();

					if ( LOG.isTraceEnabled() )
					{
						LOG.trace( "Connected to '" + _host + ':' + _port + '\'' );
					}

					final OutputStream out = socket.getOutputStream();
					final InputStream in = socket.getInputStream();

					if ( LOG.isTraceEnabled() )
					{
						LOG.trace( "Handle connection to " + socket.getInetAddress() );
					}

					handleConnection( socket );
				}
				finally
				{
					socket.close(); // Also closes 'in' and 'out'.
					_socket = null;
				}
			}
			catch ( final IOException e )
			{
				/*
				 * Wait a while, then try again.
				 */
				if ( !isStopped() )
				{
					_lastException = e;
					//noinspection InstanceofCatchParameter
					if ( ( e instanceof ConnectException ) || ( e instanceof SocketTimeoutException ) )
					{
						LOG.warn( getName() + ": " + e.getMessage() );
					}
					else
					{
						LOG.warn( getName() + ": " + e.getMessage(), e );
					}

					for ( int pollTime = getReconnectDelay(); ( pollTime > 0 ) && !_stopped; pollTime -= 1000 )
					{
						try
						{
							//noinspection BusyWait
							Thread.sleep( Math.min( 1000, pollTime ) );
						}
						catch ( final InterruptedException ignored )
						{
							break;
						}
					}
				}
			}
		}

		if ( isStopped() )
		{
			LOG.info( "Socket monitor to '" + _host + ':' + _port + "' was stopped" );
		}
		else
		{
			LOG.info( "Socket monitor to '" + _host + ':' + _port + "' was interrupted" );
		}
	}

	@Override
	public void stop()
	{
		LOG.trace( "stop()" );

		_stopped = true;

		final Socket socket = _socket;
		if ( socket != null )
		{
			try
			{
				socket.close();
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
	 * Called when a socket connection is established. The socket is
	 * automatically closed after this method is called.
	 *
	 * @param socket Connected client socket.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void handleConnection( @NotNull final Socket socket )
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
		 * Called when a socket connection is established. The socket is
		 * automatically closed after this method is called.
		 *
		 * @param socket Connected client socket.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		void handleConnection( @NotNull final Socket socket )
		throws IOException;
	}
}
