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
public abstract class SocketMonitor
	implements ResourceMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SocketMonitor.class );

	/**
	 * Host name or IP address.
	 */
	private final String _host;

	/**
	 * Remote port.
	 */
	private final int _port;

	/**
	 * Stop.
	 */
	private boolean _stopped;

	/**
	 * Currently connected socket.
	 */
	private Socket _socket;

	/**
	 * Connect timeout for the socket, in milliseconds.
	 */
	private int _connectTimeout;

	/**
	 * Delay in milliseconds before attempting to reconnect, after losing the
	 * connection.
	 */
	private long _reconnectDelay;

	/**
	 * Constructs a new instance.
	 *
	 * @param   host    Host name or IP address.
	 * @param   port    Remote port.
	 */
	protected SocketMonitor( final String host, final int port )
	{
		LOG.debug( "SocketMonitor( host=" + TextTools.quote( host ) + ", port=" + port + " )" );

		_host = host;
		_port = port;
		_socket = null;
		_stopped = false;
		_connectTimeout = 10000;
		_reconnectDelay = 10000L;
	}

	/**
	 * Returns the connect timeout for the socket.
	 *
	 * @return Connect timeout, in milliseconds.
	 */
	public int getConnectTimeout()
	{
		return _connectTimeout;
	}

	/**
	 * Sets the connect timeout for the socket.
	 *
	 * @param connectTimeout Connect timeout, in milliseconds.
	 */
	public void setConnectTimeout( final int connectTimeout )
	{
		_connectTimeout = connectTimeout;
	}

	/**
	 * Returns the delay in milliseconds before attempting to reconnect, after
	 * losing the connection.
	 *
	 * @return  Delay until reconnection, in milliseconds.
	 */
	public long getReconnectDelay()
	{
		return _reconnectDelay;
	}

	/**
	 * Sets the delay in milliseconds before attempting to reconnect, after
	 * losing the connection.
	 *
	 * @param   reconnectDelay  Delay until reconnection, in milliseconds.
	 */
	public void setReconnectDelay( final long reconnectDelay )
	{
		_reconnectDelay = reconnectDelay;
	}

	@Override
	@NotNull
	public String getName()
	{
		return _host + ':' + _port;
	}

	@Override
	public boolean isAvailable()
	{
		return ( _socket != null ) && _socket.isConnected();
	}

	@Override
	public void run()
	{
		LOG.debug( "Starting socket monitor to '" + _host + ':' + _port + '\'' );

		while ( !Thread.interrupted() && !_stopped )
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

					handleConnection( in, out );
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
				if ( !_stopped )
				{
					//noinspection InstanceofCatchParameter
					if ( ( e instanceof ConnectException ) || ( e instanceof SocketTimeoutException ) )
					{
						LOG.warn( getName() + ": " + e.getMessage() );
					}
					else
					{
						LOG.warn( getName() + ": " + e.getMessage(), e );
					}

					if ( _reconnectDelay > 0 )
					{
						try
						{
							Thread.sleep( _reconnectDelay );
						}
						catch ( final InterruptedException ignored )
						{
							break;
						}
					}
				}
			}
		}

		if ( _stopped )
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

	/**
	 * Called when a socket connection is established to handle communication
	 * with the remote host. The given streams are automatically closed when
	 * this method returns.
	 *
	 * @param   in      Input stream.
	 * @param   out     Output stream.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	protected abstract void handleConnection( final InputStream in, final OutputStream out )
		throws IOException;
}
