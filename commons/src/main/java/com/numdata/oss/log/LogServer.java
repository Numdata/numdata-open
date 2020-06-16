/*
 * Copyright (c) 2009-2020, Numdata BV, The Netherlands.
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
package com.numdata.oss.log;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Log target that allows network clients to view logs of a running application.
 *
 * @author Peter S. Heijnen
 */
public class LogServer
implements LogTarget
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( LogServer.class );

	/**
	 * Name of system property through which the TCP/IP port can be set. If set
	 * to 'default' or an empty string, the {@link #DEFAULT_PORT} is used.
	 */
	public static final String PORT_SYSTEM_PROPERTY = "log.server.port";

	/**
	 * Default port for log server.
	 */
	public static final int DEFAULT_PORT = 7709;

	/**
	 * Request from client to server.
	 */
	public enum Request
	{
		/**
		 * Quit log session.
		 *
		 * Server will disconnect.
		 */
		QUIT,

		/**
		 * Switch log level.
		 *
		 * Should be followed by log level written using
		 * {@link DataOutputStream#writeInt(int)}.
		 */
		SWITCH_LOG_LEVEL,

		/**
		 * Switch {@link AbstractLeveledLogTarget#setLevels(String) log levels}.
		 *
		 * Should be followed by log levels string written using
		 * {@link ObjectOutputStream#writeUTF(String)}.
		 */
		SWITCH_LOG_LEVELS
	}

	/**
	 * List of active client connection handlers.
	 */
	private final List<ConnectionHandler> _connectionHandlers = new ArrayList<ConnectionHandler>();

	/**
	 * Log message buffer. Used for history playback.
	 */
	private final LinkedList<LogMessage> _messageBuffer = new LinkedList<LogMessage>();

	/**
	 * Estimated size (in bytes) of log message in message buffer.
	 */
	private int _bufferSize = 0;

	/**
	 * Get default {@link LogServer} if the {@link #PORT_SYSTEM_PROPERTY} is set.
	 *
	 * @return Default {@link LogServer} instance;
	 * {@code null} if no default instance is available.
	 */
	@SuppressWarnings( "AccessOfSystemProperties" )
	@Nullable
	static LogServer getDefaultInstance()
	{
		LogServer result = null;

		try
		{
			final String port = System.getProperty( PORT_SYSTEM_PROPERTY );
			if ( port != null )
			{
				result = new LogServer( ( TextTools.isNonEmpty( port ) && !"default".equals( port ) ) ? Integer.parseInt( port ) : DEFAULT_PORT );
			}
		}
		catch ( final SecurityException e )
		{
			/* ignore no access to system property */
		}
		catch ( final Throwable t )
		{
			/* ignore all other problems, but do print them on the console */
			t.printStackTrace();
		}

		return result;
	}

	/**
	 * Construct server.
	 *
	 * @param port TCP/IP port to listen to.
	 */
	@SuppressWarnings( { "CallToThreadStartDuringObjectConstruction", "SocketOpenedButNotSafelyClosed", "IOResourceOpenedButNotSafelyClosed" } )
	public LogServer( final int port )
	{
		ServerSocket serverSocket = null;
		try
		{
			serverSocket = new ServerSocket( port );
		}
		catch ( final IOException e )
		{
			LOG.warn( "Failed to open log server socket: " + e.getMessage(), e );
		}

		if ( serverSocket != null )
		{
			final DefaultThreadFactory defaultThreadFactory = new DefaultThreadFactory();
			defaultThreadFactory.setNamePrefix( getClass().getSimpleName() );
			defaultThreadFactory.setPriority( Thread.MIN_PRIORITY );
			defaultThreadFactory.setDaemon( true );

			final Thread serverThread = defaultThreadFactory.newThread( new SocketMonitor( serverSocket ) );
			serverThread.start();
		}
	}

	@Override
	public boolean isLevelEnabled( final String name, final int level )
	{
		boolean result = false;

		synchronized ( _connectionHandlers )
		{
			for ( final LogTarget connectionHandler : _connectionHandlers )
			{
				if ( connectionHandler.isLevelEnabled( name, level ) )
				{
					result = true;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		final LogMessage logMessage = new LogMessage( name, level, message, throwable, threadName );

		bufferMessage( logMessage );

		final List<ConnectionHandler> connectionHandlers;
		synchronized ( _connectionHandlers )
		{
			connectionHandlers = new ArrayList<ConnectionHandler>( _connectionHandlers.size() );
			for ( final ConnectionHandler connectionHandler : _connectionHandlers )
			{
				if ( connectionHandler.isLevelEnabled( name, level ) )
				{
					connectionHandlers.add( connectionHandler );
				}
			}
		}

		for ( final ConnectionHandler connectionHandler : connectionHandlers )
		{
			connectionHandler.send( logMessage );
		}
	}

	/**
	 * Place log message in buffer.
	 *
	 * @param logMessage Log message to
	 */
	protected void bufferMessage( final LogMessage logMessage )
	{
		synchronized ( _messageBuffer )
		{
			final LinkedList<LogMessage> messageBuffer = _messageBuffer;
			int bufferSize = _bufferSize + getMessageSize( logMessage );

			while ( bufferSize > 100000 )
			{
				if ( messageBuffer.isEmpty() )
				{
					bufferSize = 0;
					break;
				}

				final LogMessage removedMessage = messageBuffer.removeFirst();
				bufferSize -= getMessageSize( removedMessage );
			}

			_bufferSize = bufferSize;
			messageBuffer.add( logMessage );
		}
	}

	/**
	 * Get log messages from buffer.
	 *
	 * @param logLevel Only get log messages upto this level.
	 *
	 * @return Buffered log messages.
	 */
	protected List<LogMessage> getBufferedMessages( final int logLevel )
	{
		final List<LogMessage> result = new ArrayList<LogMessage>();

		synchronized ( _messageBuffer )
		{
			for ( final LogMessage logMessage : _messageBuffer )
			{
				if ( ( logMessage.level <= logLevel ) )
				{
					result.add( logMessage );
				}
			}
		}

		return result;
	}

	/**
	 * Get size of log message in bytes. This only makes a best effort to
	 * estimate the size based on the total number of characters in the message.
	 * The actual number of bytes is always more than the result of this method.
	 *
	 * @param logMessage Log message to get size of.
	 *
	 * @return Log message size in bytes.
	 */
	public static int getMessageSize( @NotNull final LogMessage logMessage )
	{
		int stringLength = 0;

		final String name = logMessage.name;
		if ( name != null )
		{
			stringLength += name.length();
		}

		final String message = logMessage.message;
		if ( message != null )
		{
			stringLength += message.length();
		}

		for ( RemoteException exception = logMessage.throwable; exception != null; exception = exception.getCause() )
		{
			final String exceptionMessage = exception.getMessage();
			if ( exceptionMessage != null )
			{
				stringLength += exceptionMessage.length();
			}

			for ( final StackTraceElement stackTraceElement : exception.getStackTrace() )
			{
				final String className = stackTraceElement.getClassName();
				stringLength += className.length();

				final String fileName = stackTraceElement.getFileName();
				if ( fileName != null )
				{
					stringLength += fileName.length();
				}

				final String methodName = stackTraceElement.getMethodName();
				stringLength += methodName.length();
			}
		}

		return 2 * stringLength;
	}

	/**
	 * Listens to incoming connections.
	 */
	private class SocketMonitor
	implements Runnable
	{
		/**
		 * Server socket to accept connections from.
		 */
		private final ServerSocket _serverSocket;

		/**
		 * Construct socket connection listener.
		 *
		 * @param serverSocket Server socket to accept connections from.
		 */
		private SocketMonitor( final ServerSocket serverSocket )
		{
			_serverSocket = serverSocket;
		}

		@Override
		public void run()
		{
			final ServerSocket serverSocket = _serverSocket;
			LOG.info( "Log server started, listening to " + serverSocket.getInetAddress() + ':' + serverSocket.getLocalPort() );

			try
			{
				while ( !serverSocket.isClosed() )
				{
					try
					{
						final Socket clientSocket = serverSocket.accept();
						try
						{
							LOG.debug( "Log client connected from " + clientSocket.getInetAddress() );
							final ConnectionHandler connectionHandler = new ConnectionHandler( clientSocket );
							synchronized ( _connectionHandlers )
							{
								_connectionHandlers.add( connectionHandler );
							}

							final Thread thread = new Thread( connectionHandler );
							thread.setDaemon( true );
							//noinspection CallToThreadSetPriority
							thread.setPriority( Thread.MIN_PRIORITY );
							thread.start();
						}
						catch ( final Exception e )
						{
							e.printStackTrace();
							clientSocket.close();
						}
					}
					catch ( final IOException e )
					{
						if ( !serverSocket.isClosed() )
						{
							e.printStackTrace();
						}
					}
				}
			}
			finally
			{
				try
				{
					serverSocket.close();
				}
				catch ( final IOException e )
				{
					/* ignore socket closing problems */
				}
			}

			LOG.debug( "Log server terminated" );
		}
	}

	/**
	 * Handles a client connection.
	 */
	private class ConnectionHandler
	extends AbstractLeveledLogTarget
	implements Runnable
	{
		/**
		 * Socket connected to client.
		 */
		private final Socket _socket;

		/**
		 * Output stream to connected client.
		 */
		@SuppressWarnings( "FieldAccessedSynchronizedAndUnsynchronized" )
		private ObjectOutput _out;

		/**
		 * Thread on which the client is handled.
		 */
		@SuppressWarnings( "FieldAccessedSynchronizedAndUnsynchronized" )
		private Thread _thread = null;

		/**
		 * Constructs a new handler for the given socket.
		 *
		 * @param socket Socket connected to a client.
		 */
		ConnectionHandler( @NotNull final Socket socket )
		{
			super( ClassLogger.INFO, null );
			_socket = socket;
			_out = null;
		}

		@SuppressWarnings( "IOResourceOpenedButNotSafelyClosed" )
		@Override
		public void run()
		{
			_thread = Thread.currentThread();
			final Socket socket = _socket;
			try
			{
				final ObjectOutput out = new ObjectOutputStream( socket.getOutputStream() );
				_out = out;

				final ObjectInput in = new ObjectInputStream( socket.getInputStream() );
				final int initialLogLevel = in.readInt();
				final String initialLogLevels = in.readUTF();
				setLevel( initialLogLevel );
				setLevels( initialLogLevels );
				for ( final LogMessage logMessage : getBufferedMessages( initialLogLevel ) )
				{
					if ( isLevelEnabled( logMessage.name, logMessage.level ) )
					{
						out.writeObject( logMessage );
					}
				}
				out.flush();

				while ( !Thread.interrupted() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown() )
				{
					final Object request = in.readObject();
					LOG.debug( "Log client request: " + request );

					if ( request == LogServer.Request.QUIT )
					{
						break;
					}
					else if ( request == LogServer.Request.SWITCH_LOG_LEVEL )
					{
						final int level = in.readInt();
						if ( ( level < ClassLogger.FATAL ) || ( level > ClassLogger.TRACE ) )
						{
							break;
						}

						setLevel( level );
					}
					else if ( request == LogServer.Request.SWITCH_LOG_LEVELS )
					{
						setLevels( in.readUTF() );
					}
				}
			}
			catch ( final IOException e )
			{
				LOG.debug( "Unexpected client disconnect: " + e, e );
			}
			catch ( final ClassNotFoundException e )
			{
				LOG.warn( "Bad object received from client: " + e, e );
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

			LOG.debug( "Log client " + socket.getInetAddress() + " disconnected" );
			_thread = null;
		}

		@Override
		public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
		{
			send( new LogMessage( name, level, message, throwable, threadName ) );
		}

		/**
		 * Send object to client.
		 *
		 * @param object Object to send.
		 */
		public void send( final Object object )
		{
			final Thread thread = _thread;
			final ObjectOutput out = _out;
			if ( ( thread != null ) && ( out != null ) )
			{
				//noinspection SynchronizationOnLocalVariableOrMethodParameter
				synchronized ( out )
				{
					try
					{
						out.writeObject( object );
						out.flush();
					}
					catch ( final Throwable t )
					{
						_out = null;
						t.printStackTrace();
						thread.interrupt();
						_thread = null;
					}
				}
			}
		}
	}
}
