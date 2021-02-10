/*
 * Copyright (c) 2009-2021, Unicon Creation BV, The Netherlands.
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
import java.util.regex.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Client for {@link LogServer}.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "UseOfSystemOutOrSystemErr" )
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
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		final LogClient logClient = new LogClient();
		boolean hostSet = false;
		final List<Pattern> exclusions = new ArrayList<>();

		final List<String> argList = Arrays.asList( args );
		final Iterator<String> argIterator = argList.iterator();

		while ( argIterator.hasNext() )
		{
			final String arg = argIterator.next();
			if ( "-l".equals( arg ) ||
			     "-level".equals( arg ) )
			{
				if ( !argIterator.hasNext() )
				{
					throw new RuntimeException( "No arguments after '" + arg + '\'' );
				}

				final String levels = argIterator.next();
				final int length = levels.length();
				int levelsStart = 0;

				for ( int i = 0; i <= length; i++ )
				{
					final char ch = ( i < length ) ? levels.charAt( i ) : ',';
					if ( ch == ',' )
					{
						logClient.setLogLevel( ClassLogger.parseLevel( levels.substring( 0, i ).trim() ) );
						levelsStart = i + 1;
						break;
					}
					else if ( ch == '=' )
					{
						break;
					}
				}

				if ( levelsStart < length )
				{
					logClient.setLogLevels( levels.substring( levelsStart ).trim() );
				}
			}
			else if ( "-e".equals( arg ) ||
			          "-exclude".equals( arg ) )
			{
				if ( !argIterator.hasNext() )
				{
					throw new RuntimeException( "No arguments after '" + arg + '\'' );
				}

				exclusions.add( Pattern.compile( argIterator.next() ) );
			}
			else if ( TextTools.startsWith( arg, '-' ) )
			{
				throw new RuntimeException( "Unrecognized option: " + arg );
			}
			else
			{
				if ( hostSet )
				{
					throw new RuntimeException( "Can't connect to multiple servers" );
				}

				final int colon = arg.indexOf( ':' );
				logClient.setHost( ( colon < 0 ) ? arg : arg.substring( 0, colon ) );
				if ( colon >= 0 )
				{
					logClient.setPort( Integer.parseInt( arg.substring( colon + 1 ) ) );
				}
				hostSet = true;
			}
		}

		if ( TextTools.isEmpty( logClient.getHost() ) )
		{
			System.out.println( "Command-line: " + LogClient.class.getName() + " -level <levels> -exclude <regex> <host>[:<port>]" );
			System.out.println();
			System.out.println( "    -l[evel] <level>[,<pattern>=<level>]..." );
			System.out.println( "        Set log level(s)." );
			System.out.println();
			System.out.println( "    -e[xclude] <regex>" );
			System.out.println( "        Exclude log messages that match this pattern (multiple allowed)" );
			System.out.println();
			System.out.println( "    <host>[:<port>]>" );
			System.out.println( "        Host name and optional port number to connect to." );
			System.exit( 0 );
		}

		/*
		 * Connect to client and show any incoming messages.
		 */
		System.out.println( "Connecting to log server" );
		final LogTarget consoleTarget = new ConsoleTarget( ClassLogger.TRACE, System.out );
		logClient.addListener( logMessage -> {
			if ( exclusions.stream().noneMatch( regex -> regex.matcher( logMessage.message ).find() ) )
			{
				consoleTarget.log( logMessage.name, logMessage.level, logMessage.message, logMessage.throwable, logMessage.threadName );
			}
		} );
		logClient.connect().run();
	}

	/**
	 * Host name or address of server to connect to (default: localhost).
	 */
	@NotNull
	private String _host = "localhost";

	/**
	 * TCP port to connect to (default: {@link LogServer#DEFAULT_PORT 7709}).
	 */
	private int _port = LogServer.DEFAULT_PORT;

	/**
	 * Log level (default: {@link ClassLogger#INFO}).
	 */
	private int _logLevel = ClassLogger.INFO;

	/**
	 * Log levels string (default: none).
	 *
	 * @see AbstractLeveledLogTarget#setLevels(String)
	 */
	@NotNull
	private String _logLevels = "";

	/**
	 * Registered listeners.
	 */
	@NotNull
	private final List<LogClientListener> _listeners = new ArrayList<>();

	/**
	 * Open connection to server.
	 *
	 * @return Connection to server.
	 *
	 * @throws IOException if a connection could not be established.
	 */
	public Connection connect()
	throws IOException
	{
		return new Connection();
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

	@NotNull
	public String getHost()
	{
		return _host;
	}

	public void setHost( @NotNull final String host )
	{
		_host = host;
	}

	public int getPort()
	{
		return _port;
	}

	public void setPort( final int port )
	{
		_port = port;
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
		_logLevel = logLevel;
	}

	@NotNull
	public String getLogLevels()
	{
		return _logLevels;
	}

	/**
	 * Set current log levels.
	 *
	 * @param logLevels Log levels to set.
	 */
	public void setLogLevels( @NotNull final String logLevels )
	{
		_logLevels = logLevels;
	}

	/**
	 * This method is called when a message is received from the server.
	 *
	 * @param logMessage Log message that was received.
	 */
	protected void messageReceived( @NotNull final LogMessage logMessage )
	{
		for ( final LogClientListener listener : _listeners )
		{
			listener.logMessageReceived( logMessage );
		}
	}

	/**
	 * This receives incoming log messages from the server.
	 */
	public class Connection
	implements Runnable
	{
		/**
		 * Socket connected to server.
		 */
		@NotNull
		private final Socket _socket;

		/**
		 * Input stream to connected server.
		 */
		@NotNull
		private final ObjectInput _in;

		/**
		 * Output stream to connected server.
		 */
		@NotNull
		private final ObjectOutput _out;

		/**
		 * Thread on which the connection is handled.
		 *
		 * @see #run()
		 */
		@Nullable
		private Thread _thread = null;

		/**
		 * Open connection to server.
		 *
		 * @throws IOException if a connection could not be established.
		 */
		public Connection()
		throws IOException
		{
			LOG.debug( "Connecting to log server " + getHost() + ':' + getPort() );

			final Socket socket = new Socket();
			socket.connect( new InetSocketAddress( getHost(), getPort() ), 10000 );
			LOG.debug( "Connected to log server." );

			final ObjectOutput out = new ObjectOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
			out.writeInt( getLogLevel() );
			out.writeUTF( getLogLevels() );
			out.flush();
			final ObjectInput in = new ObjectInputStream( new BufferedInputStream( socket.getInputStream() ) );

			_socket = socket;
			_out = out;
			_in = in;
		}

		@Override
		public void run()
		{
			_thread = Thread.currentThread();

			try
			{
				while ( !Thread.interrupted() )
				{
					LOG.trace( "Waiting for message" );
					final LogMessage logMessage = (LogMessage)_in.readObject();
					messageReceived( logMessage );
				}
			}
			catch ( final InterruptedIOException e )
			{
				LOG.info( "Log client connection was interrupted: " + e, e );
			}
			catch ( final IOException e )
			{
				LOG.info( "Unexpected log server disconnect: " + e, e );
			}
			catch ( final ClassNotFoundException e )
			{
				LOG.warn( "Bad object received from log server: " + e, e );
			}
			finally
			{
				try
				{
					_socket.close();
				}
				catch ( final IOException e )
				{
					LOG.debug( "Failed to close socket: " + e, e );
				}
			}

			LOG.debug( "Log server " + _socket.getInetAddress() + " disconnected" );
			_thread = null;
		}

		/**
		 * Set current log level.
		 *
		 * @param logLevel Log level to set.
		 */
		public void setLogLevel( final int logLevel )
		{
			LOG.debug( "setLogLevel( " + logLevel + " )" );
			synchronized ( _out )
			{
				try
				{
					_out.writeObject( LogServer.Request.SWITCH_LOG_LEVEL );
					_out.writeInt( logLevel );
					_out.flush();
				}
				catch ( final Throwable t )
				{
					t.printStackTrace();
					quit();
				}
			}
		}

		/**
		 * Set current log levels.
		 *
		 * @param logLevels Log levels to set.
		 */
		public void setLogLevels( @NotNull final String logLevels )
		{
			LOG.debug( "setLogLevels( " + logLevels + " )" );
			synchronized ( _out )
			{
				try
				{
					_out.writeObject( LogServer.Request.SWITCH_LOG_LEVELS );
					_out.writeUTF( logLevels );
					_out.flush();
				}
				catch ( final Throwable t )
				{
					t.printStackTrace();
					quit();
				}
			}
		}

		/**
		 * Quit client session. This has no effect if the client was already
		 * disconnected.
		 */
		public void quit()
		{
			synchronized ( _out )
			{
				try
				{
					_out.writeObject( LogServer.Request.QUIT );
				}
				catch ( final Throwable t )
				{
					/* ignore */
				}
			}

			final Thread thread = _thread;
			if ( thread != null )
			{
				thread.interrupt();
			}
		}

		@Override
		protected void finalize()
		throws Throwable
		{
			super.finalize();
			quit();
		}
	}

}
