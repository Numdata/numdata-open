/*
 * (C) Copyright Numdata BV 2009-2018 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.socket.tool;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import com.numdata.oss.log.*;
import com.numdata.uri.*;
import org.jetbrains.annotations.*;

/**
 * A command-line tool for using TCP sockets.
 *
 * @author G. Meinders
 * @author Peter S. Heijnen
 */
public class SocketTool
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SocketTool.class );

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main( final String[] args )
	throws IOException
	{
		if ( args.length == 0 )
		{
			System.out.println( "Arguments:" );
			System.out.println( "  listen <port>               - Listen on the specified TCP port and write to stdout" );
			System.out.println( "  send <host> <port> [<text>] - Send text or data from stdin to the specified host/port using TCP." );
			System.out.println( "  echo <host> <port> [<text>] - Send text or data from stdin to the specified host/port using TCP and wait for response." );
			System.out.println( "  proxy [<bind address>:]<tcp port> <uri> <...> - Receive data from the specified TCP port and forward to the given URI." );
		}
		else
		{
			final String command = args[ 0 ];

			if ( "listen".equals( command ) )
			{
				doListen( args[ 1 ] );
			}
			else if ( "send".equals( command ) )
			{
				doSend( args );
			}
			else if ( "echo".equals( command ) )
			{
				doEcho( args );
			}
			else if ( "proxy".equals( command ) )
			{
				doProxy( args );
			}
			else
			{
				throw new IllegalArgumentException( "Illegal command: " + command );
			}
		}
	}

	private static void doListen( final String arg )
	throws IOException
	{
		final int port = Integer.parseInt( arg );

		final ServerSocket serverSocket = new ServerSocket( port );
		try
		{
			while ( !Thread.interrupted() )
			{
				final Socket socket = serverSocket.accept();
				try
				{
					final InputStream in = socket.getInputStream();
					int read;
					while ( ( read = in.read() ) != -1 )
					{
						System.out.write( read );
					}
					System.out.println();
				}
				finally
				{
					socket.close();
				}
			}
		}
		finally
		{
			serverSocket.close();
		}
	}

	private static void doSend( final String[] args )
	throws IOException
	{
		final String host = args[ 1 ];
		final int port = Integer.parseInt( args[ 2 ] );

		final Socket socket = new Socket();
		try
		{
			socket.connect( new InetSocketAddress( host, port ), 10000 );

			final OutputStream out = socket.getOutputStream();
			send( out, args.length > 2 ? args[ 3 ] : null );
		}
		finally
		{
			socket.close();
		}
	}

	private static void doEcho( final String[] args )
	throws IOException
	{
		final String host = args[ 1 ];
		final int port = Integer.parseInt( args[ 2 ] );

		final Socket socket = new Socket();
		try
		{
			socket.connect( new InetSocketAddress( host, port ), 10000 );

			final InputStream in = socket.getInputStream();
			while ( in.available() > 0 )
			{
				System.out.print( (char)in.read() );
			}
			System.out.flush();

			final OutputStream out = socket.getOutputStream();
			send( out, args.length > 2 ? args[ 3 ] : null );

			try
			{
				for ( int read = in.read(); read != -1; read = in.read() )
				{
					System.out.print( (char)read );
					System.out.flush();
				}
			}
			catch ( final IOException ignored )
			{
				System.out.println();
				System.out.println( "Aborted: " + ignored );
			}
			System.out.println();
		}
		finally
		{
			socket.close();
		}
	}

	public static void doProxy( final String[] args )
	{
		if ( ( args.length < 2 ) || ( args.length % 2 != 1 ) )
		{
			System.err.println( "Required arguments: " + args[ 0 ] + " [<bind address>:]<tcp port> <uri> <...>" );
		}
		else
		{
			//noinspection ResultOfObjectAllocationIgnored
			final SocketTool tool = new SocketTool();

			for ( int i = 1; i < args.length; i += 2 )
			{
				final String socket = args[ i ];
				final int socketColon = socket.lastIndexOf( ':' );
				final String serverBindAddress = ( socketColon < 0 ) ? null : socket.substring( 0, socketColon );
				final int serverTcpPort = Integer.parseInt( ( socketColon < 0 ) ? socket : socket.substring( socketColon + 1 ) );

				final URI uri = URI.create( args[ i + 1 ] );
				tool.submitTask( tool.new Socket2URI( serverBindAddress, serverTcpPort, uri ) );
			}
		}
	}

	/**
	 * Executes various tasks asynchronously.
	 */
	private ExecutorService _executor = null;

	public void submitTask( @NotNull final Runnable task )
	{
		ExecutorService executor = _executor;
		if ( executor == null )
		{
			executor = Executors.newCachedThreadPool();
			_executor = executor;
		}

		executor.submit( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					task.run();
				}
				catch ( Throwable t )
				{
					LOG.error( "Failed to run task " + task, t );
				}
			}
		} );
	}

	/**
	 * Server.
	 */
	protected class Socket2URI
	implements Runnable
	{
		/**
		 * Address to bind to.
		 */
		@Nullable
		private final String _serverBindAddress;

		/**
		 * TCP port to listen on.
		 */
		private final int _serverTcpPort;

		/**
		 * URI to forward data to.
		 */
		private final URI _uri;

		/**
		 * Construct server.
		 *
		 * @param serverBindAddress Address to bind to.
		 * @param serverTcpPort     TCP on which to receive data.
		 * @param uri               URI to forward data to.
		 */
		public Socket2URI( @Nullable final String serverBindAddress, final int serverTcpPort, @NotNull final URI uri )
		{
			_serverBindAddress = serverBindAddress;
			_serverTcpPort = serverTcpPort;
			_uri = uri;
		}

		@Override
		public void run()
		{
			try
			{
				final ServerSocket serverSocket = new ServerSocket( _serverTcpPort, 50, _serverBindAddress != null ? InetAddress.getByName( _serverBindAddress ) : null );
				try
				{
					System.out.println( "Listen for incoming data from " + serverSocket.getInetAddress() + ':' + serverSocket.getLocalPort() + ", and forward to " + _uri );
					while ( !serverSocket.isClosed() )
					{
						try
						{
							// socket is closed by {@link #receive} method
							//noinspection SocketOpenedButNotSafelyClosed
							final Socket clientSocket = serverSocket.accept();
							clientSocket.setSoTimeout( 10000 );
							LOG.debug( "Receiving data from " + clientSocket.getInetAddress() );
							submitTask( new Runnable()
							{
								@Override
								public void run()
								{
									receive( clientSocket );
								}
							} );
						}
						catch ( final Exception e )
						{
							e.printStackTrace();
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
				LOG.debug( "Server terminated" );
			}
			catch ( final IOException e )
			{
				System.err.println( "Failed to open TCP port " + _serverTcpPort );
			}
		}

		/**
		 * Receive message from socket.
		 *
		 * @param socket Socket connection.
		 */
		protected void receive( final Socket socket )
		{
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			try
			{
				final InputStream in = socket.getInputStream();
				final byte[] buffer = new byte[ 8192 ];
				for ( int read = in.read( buffer ); read >= 0; read = in.read( buffer ) )
				{
					data.write( buffer, 0, read );
				}
			}
			catch ( final IOException e )
			{
				System.out.println( "Error receiving data: " + e );
				data = null;
			}
			finally
			{
				try
				{
					socket.close();
				}
				catch ( final IOException e )
				{
					System.out.println( "Failed to close client socket: " + e );
				}
			}

			if ( data != null )
			{
				received( data.toByteArray() );
			}
		}

		/**
		 * Called when data is received.
		 *
		 * @param data Message to send.
		 */
		protected void received( final byte[] data )
		{
			sendToURI( data );
		}

		/**
		 * Send data to URI.
		 *
		 * @param data Data to send.
		 */
		protected void sendToURI( final byte[] data )
		{
			System.out.println( "Sending " + data.length + " byte(s) to " + _uri );
			try
			{
				URITools.writeData( _uri, data, true );
			}
			catch ( final IOException e )
			{
				System.out.println( "Failed to send data: " + e );
				LOG.debug( "sendToURI failed: " + e, e );
			}
		}
	}

	/**
	 * Send text to socket. The text may contain some common escape codes.
	 *
	 * @param out  Stream to send to.
	 * @param text Text to send; {@code null} to read from stdin.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private static void send( final OutputStream out, @Nullable final String text )
	throws IOException
	{
		if ( text != null )
		{
			int pos = 0;
			while ( pos < text.length() )
			{
				char ch = text.charAt( pos++ );
				if ( ch == '\\' )
				{
					ch = text.charAt( pos++ );
					switch ( ch )
					{
						case 'n':
							ch = '\n';
							break;

						case 'r':
							ch = '\r';
							break;

						case 'b':
							ch = '\b';
							break;

						case 't':
							ch = '\t';
							break;

						case '0':
							ch = (char)Integer.parseInt( text.substring( pos, pos + 2 ), 8 );
							pos += 2;
							break;

						case 'u':
							ch = (char)Integer.parseInt( text.substring( pos, pos + 4 ), 16 );
							pos += 4;
							break;
					}
				}
				out.write( ch );
//				System.out.println( "sent: " + (int)ch );
			}
		}
		else
		{
			for ( int read = System.in.read(); read != -1; read = System.in.read() )
			{
				out.write( read );
			}
		}

		out.flush();
	}
}
