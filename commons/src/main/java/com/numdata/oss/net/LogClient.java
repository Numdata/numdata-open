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
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;

/**
 * Client for {@link LogServer}.
 *
 * @author  Peter S. Heijnen
 */
public class LogClient
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( LogClient.class );

	/**
	 * Run client application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws  Exception if the application crashes.
	 */
	public static void main( final String[] args )
		throws Exception
	{
		int logLevel = ClassLogger.INFO;
		String host = null;
		int port = LogServer.DEFAULT_PORT;

		final List<String> argList = Arrays.asList( args );
		final Iterator<String> argIterator = argList.iterator();

		while ( argIterator.hasNext() )
		{
			final String arg = argIterator.next();
			if ( "-level".equals( arg ) )
			{
				if ( !argIterator.hasNext() )
				{
					throw new RuntimeException( "No arguments after '" + arg + '\'' );
				}

				logLevel = ClassLogger.parseLevel( argIterator.next() );
			}
			else if ( TextTools.startsWith( arg, '-' ) )
			{
				throw new RuntimeException( "Unrecognized option: " + arg );
			}
			else
			{
				if ( TextTools.isNonEmpty( host ) )
				{
					throw new RuntimeException( "Can't connect to multiple servers" );
				}

				final int colon = arg.indexOf( (int)':' );
				host = ( colon < 0 ) ? arg : arg.substring( 0, colon );
				if ( colon >= 0 )
				{
					port = Integer.parseInt( arg.substring( colon + 1 ) );
				}
			}
		}

		/*
		 * Connect to client and show any incoming messages.
		 */
		final ConsoleTarget consoleTarget = new ConsoleTarget( ClassLogger.TRACE, System.out );

		LOG.info( "Connecting to log server" );
		final LogClient logClient = new LogClient( host, port, logLevel );

		LOG.debug( "Start listening for incoming messages" );
		logClient.addListener( new LogClientListener()
		{
			@Override
			public void logMessageReceived( final LogMessage logMessage )
			{
				consoleTarget.log( logMessage.name, logMessage.level, logMessage.message, logMessage.throwable, logMessage.threadName );
			}
		} );

		logClient._thread.join();
	}

	/**
	 * Log level.
	 */
	private int _logLevel = ClassLogger.INFO;

	/**
	 * Socket connected to server.
	 */
	private final Socket _socket;

	/**
	 * Output stream to connected server.
	 */
	private ObjectOutput _out;

	/**
	 * Thread that receives messages from the server.
	 */
	private Thread _thread;

	/**
	 * Registered listeners.
	 */
	private final List<LogClientListener> _listeners = new ArrayList<LogClientListener>();

	/**
	 * Construct client.
	 *
	 * @param host     Host name or address of server to connect to.
	 * @param port     TCP port to connect to.
	 * @param logLevel Initial log level.
	 *
	 * @throws  IOException if the client could not connect to a log server.
	 */
	public LogClient( final String host, final int port, final int logLevel )
		throws IOException
	{
		LOG.debug( "Connecting to log server " + host + ':' + port );

		final Socket socket = new Socket();
		socket.connect( new InetSocketAddress( host, port ), 10000 );
		LOG.debug( "Connected to log server." );

		final ObjectOutput out = new ObjectOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
		out.writeInt( logLevel );
		out.flush();
		final ObjectInput in = new ObjectInputStream( new BufferedInputStream( socket.getInputStream() ) );

		_logLevel = logLevel;
		_socket = socket;
		_out = out;

		final Thread thread = new Thread( new LogMessageReceiver( in ) );
		thread.setDaemon( true );
		thread.setPriority( Thread.MIN_PRIORITY );
		_thread = thread;
		thread.start();
	}

	/**
	 * Add listener.
	 *
	 * @param listener Listener to add.
	 */
	public void addListener( final LogClientListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Remove listener.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeListener( final LogClientListener listener )
	{
		_listeners.remove( listener );
	}

	public int getLogLevel()
	{
		return _logLevel;
	}

	/**
	 * Set current log level.
	 *
	 * @param logLevel Log level to set.
	 */
	public void setLogLevel( final int logLevel )
	{
		LOG.trace( "setLogLevel( " + logLevel + " )" );

		if ( logLevel != _logLevel )
		{
			_logLevel = logLevel;

			final Thread thread = _thread;
			final ObjectOutput out = _out;
			if ( ( thread != null ) && ( out != null ) )
			{
				LOG.debug( "Switching to log level: " + logLevel );
				synchronized ( out )
				{
					try
					{
						out.writeObject( LogServer.Request.SWITCH_LOG_LEVEL );
						out.writeInt( logLevel );
						out.flush();
					}
					catch ( final Throwable t )
					{
						t.printStackTrace();
						quit();
					}
				}
			}
		}
	}

	/**
	 * Quit client session. This has no effect if the client was already
	 * disconnected.
	 */
	public void quit()
	{
		final ObjectOutput out = _out;
		_out = null;
		if ( out != null )
		{
			synchronized ( out )
			{
				try
				{
					out.writeObject( LogServer.Request.QUIT );
				}
				catch ( final Throwable t )
				{
					/* ignore */
				}
			}
		}

		final Thread thread = _thread;
		_thread = null;
		if ( thread != null )
		{
			thread.interrupt();
		}
	}

	/**
	 * This method is called when a message is received from the server.
	 *
	 * @param logMessage Log message that was received.
	 */
	protected void messageReceived( final LogMessage logMessage )
	{
		if ( logMessage == null )
		{
			throw new NullPointerException( "logMessage" );
		}

		for ( final LogClientListener listener : _listeners )
		{
			listener.logMessageReceived( logMessage );
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		super.finalize();
		quit();
	}

	/**
	 * This receives incoming log messages from the server.
	 */
	private class LogMessageReceiver
		implements Runnable
	{
		/**
		 * Input stream from connected server.
		 */
		private final ObjectInput _in;

		/**
		 * Construct receiver.
		 *
		 * @param in  Input stream from connected server.
		 */
		private LogMessageReceiver( final ObjectInput in )
		{
			if ( in == null )
			{
				throw new NullPointerException( "in" );
			}

			_in = in;
		}

		@Override
		public void run()
		{
			final Socket socket = _socket;
			final ObjectInput in = _in;

			if ( socket != null )
			{
				try
				{
					while ( !Thread.interrupted() )
					{
						LOG.trace( "Waiting for message" );
						final LogMessage logMessage = (LogMessage)in.readObject();
						messageReceived( logMessage );
					}
				}
				catch ( final IOException e )
				{
					LOG.debug( "Unexpected server disconnect: " + e, e );
				}
				catch ( final ClassNotFoundException e )
				{
					LOG.warn( "Bad object received from server: " + e, e );
				}
				finally
				{
					try
					{
						socket.close();
					}
					catch ( final IOException e )
					{
						LOG.debug( "Failed to close socket: " + e, e );
					}
				}

				LOG.debug( "Log server " + socket.getInetAddress() + " disconnected" );
			}
		}
	}

}
