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
package com.numdata.ssh;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;

import com.numdata.oss.*;
import com.numdata.oss.db.*;
import com.numdata.oss.ensemble.*;
import com.numdata.oss.log.*;
import com.numdata.oss.net.*;
import com.trilead.ssh2.Connection;
import org.jetbrains.annotations.*;

/**
 * Some tools for using SSH connections.
 *
 * @author Peter S. Heijnen
 */
public class SshTools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SshTools.class );

	/**
	 * Connect to SSH server.
	 *
	 * @param server SSH server specification (&lt;user&gt;:&lt;pass&gt;@&lt;host&gt;[':'&lt;port&gt;]).
	 *
	 * @return Open connection.
	 *
	 * @throws IOException if the connection could not be established.
	 * @throws IllegalArgumentException if the specification is badly
	 * formatted.
	 */
	public static Connection connect( @Nullable final String server )
	throws IOException
	{
		final Connection result;

		final Quartet<String, Integer, String, String> options = parseOptions( server );
		if ( ( options != null ) && ( options.getValue1() != null ) )
		{
			result = connect( options.getValue1(), options.getValue2(), options.getValue3(), options.getValue4() );
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Parse SSH server specification. Syntax:
	 * <pre>&lt;user&gt;:&lt;pass&gt;@&lt;host&gt;[':'&lt;port&gt;]</pre>.
	 *
	 * @param server SSH server specification.
	 *
	 * @return {@link Quartet} with SSH server host, server port, user name, and
	 * password; {@code null} if {@code server} is empty.
	 *
	 * @throws IllegalArgumentException if the specification is badly
	 * formatted.
	 */
	@Nullable
	public static Quartet<String, Integer, String, String> parseOptions( @Nullable final String server )
	{
		final Quartet<String, Integer, String, String> result;

		if ( ( server == null ) || TextTools.isEmpty( server ) )
		{
			result = null;
		}
		else
		{
			final URI uri;
			try
			{
				uri = new URI( "ssh://" + server + '/' );
			}
			catch ( final URISyntaxException e )
			{
				throw new IllegalArgumentException( "Bad server specification: " + server, e );
			}

			final String sshHost = uri.getHost();
			if ( TextTools.isEmpty( sshHost ) )
			{
				throw new IllegalArgumentException( "Missing hostname in '" + server + '\'' );
			}

			final int sshPort = ( uri.getPort() < 0 ) ? 22 : uri.getPort();

			final String userInfo = uri.getUserInfo();
			if ( TextTools.isEmpty( userInfo ) )
			{
				throw new IllegalArgumentException( "Missing user info in '" + server + '\'' );
			}

			final int colon = userInfo.indexOf( (int)':' );
			if ( colon < 0 )
			{
				throw new IllegalArgumentException( "Missing user password in '" + server + '\'' );
			}

			final String sshUsername = userInfo.substring( 0, colon );
			final String sshPassword = userInfo.substring( colon + 1 );

			result = new BasicQuartet<String, Integer, String, String>( sshHost, Integer.valueOf( sshPort ), sshUsername, sshPassword );
		}

		return result;
	}

	/**
	 * Connect to SSH server.
	 *
	 * @param host     SSH server host.
	 * @param port     SSH server port.
	 * @param username User name for authentication.
	 * @param password Password for authentication.
	 *
	 * @return Open connection
	 *
	 * @throws IOException if the connection could not be established.
	 */
	@NotNull
	public static Connection connect( @NotNull final String host, final int port, @NotNull final String username, @NotNull final String password )
	throws IOException
	{
		LOG.debug( "Establishing SSH connection to '" + host + '\'' );

		final Connection connection = new Connection( host, port );
		connection.connect( null, 10000, 10000 );

		LOG.trace( "Connected, authenticating as '" + username + '\'' );
		if ( !connection.authenticateWithPassword( username, password ) )
		{
			LOG.debug( "Authentication failed" );
			connection.close();
			throw new AuthenticationException( "Failed to authenticate" );
		}

		LOG.trace( "Authentication successful" );
		return connection;
	}

	/**
	 * Create {@link JdbcDataSource}.
	 *
	 * @param sshServer    Optional SSH server specification (&lt;user&gt;:&lt;pass&gt;@&lt;host&gt;[':'&lt;port&gt;]).
	 * @param dbServer     Database server host.
	 * @param dbPort       TCP port of database server.
	 * @param jdbcDriver   JDBC driver name.
	 * @param jdbcUrl      JDBC URL format ({0}=host, {1}=port).
	 * @param jdbcUser     JDBC user name.
	 * @param jdbcPassword JDBC password.
	 *
	 * @return {@link JdbcDataSource}.
	 *
	 * @throws IllegalArgumentException if the specification is badly
	 * formatted.
	 * @throws IOException if the SSH connection could not be established.
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	public static JdbcDataSource createJdbcDataSource( @Nullable final String sshServer, @NotNull final String dbServer, final int dbPort, final String jdbcDriver, @NotNull final String jdbcUrl, final String jdbcUser, final String jdbcPassword )
	throws IOException, SQLException
	{
		final String actualHost;
		final int actualPort;

		final Connection connection = connect( sshServer );
		if ( connection != null )
		{
			actualHost = "127.0.0.1";
			actualPort = createLocalPortForwarder( connection, dbServer, dbPort );
		}
		else
		{
			actualHost = dbServer;
			actualPort = dbPort;
		}

		final String actualJdbcUrl = MessageFormat.format( jdbcUrl, actualHost, String.valueOf( actualPort ) );

		return new JdbcDataSource( jdbcDriver, actualJdbcUrl, jdbcUser, jdbcPassword );
	}

	/**
	 * Create port forwarding tunnel to the specified target host and port and
	 * return the local TCP port through which it can be accessed.
	 *
	 * @param connection Connection to create port forwarding on.
	 * @param targetHost Target host to connect to.
	 * @param targetPort TCP port to connect to.
	 *
	 * @return Local TCP port.
	 *
	 * @throws IOException if the tunnel could not be created.
	 */
	public static int createLocalPortForwarder( @NotNull final Connection connection, final String targetHost, final int targetPort )
	throws IOException
	{
		final int localPort = getFreeLocalPort();
		LOG.trace( "createLocalPortForwarder( " + TextTools.quote( targetHost ) + ", " + targetPort + " ) => Forward local 0.0.0.0:" + localPort + " to remote " + targetHost + ':' + targetPort );
		connection.createLocalPortForwarder( localPort, targetHost, targetPort );
		return localPort;
	}

	/**
	 * Create local port forwarding based on the specified {@link
	 * PortForwardingProperties}.
	 *
	 * @param connection Connection to create port forwardings on.
	 * @param properties Port forwarding properties.
	 *
	 * @return Local TCP port.
	 *
	 * @throws IOException port forwarding failed.
	 */
	public static int createLocalPortForwarder( @NotNull final Connection connection, @NotNull final PortForwardingProperties properties )
	throws IOException
	{
		final String bindAddress = TextTools.isEmpty( properties.getSourceAddress() ) ? "localhost" : properties.getSourceAddress();
		final int bindPort = ( properties.getSourcePort() <= 0 ) ? getFreeLocalPort() : properties.getSourcePort();
		final String targetAddress = TextTools.isEmpty( properties.getTargetAddress() ) ? "localhost" : properties.getTargetAddress();
		final int targetPort = properties.getTargetPort();

		LOG.trace( "createLocalPortForwarder( properties ) => Forward local " + bindAddress + ':' + bindPort + " to remote " + targetAddress + ':' + targetPort );
		connection.createLocalPortForwarder( new InetSocketAddress( bindAddress, bindPort ), targetAddress, targetPort );
		return bindPort;
	}

	/**
	 * Request remote port forwarding based on the specified {@link
	 * PortForwardingProperties}.
	 *
	 * @param connection Connection to request port forwardings on.
	 * @param properties Port forwarding properties.
	 *
	 * @return Local TCP port.
	 *
	 * @throws IOException port forwarding failed.
	 */
	public static int requestRemotePortForwarding( @NotNull final Connection connection, @NotNull final PortForwardingProperties properties )
	throws IOException
	{
		final String bindAddress = TextTools.isEmpty( properties.getSourceAddress() ) ? "localhost" : properties.getSourceAddress();
		final int bindPort = properties.getSourcePort();
		final String targetAddress = TextTools.isEmpty( properties.getTargetAddress() ) ? "localhost" : properties.getTargetAddress();
		final int targetPort = ( properties.getTargetPort() <= 0 ) ? getFreeLocalPort() : properties.getTargetPort();

		LOG.trace( "requestRemotePortForwarding => Forward remote " + bindAddress + ':' + bindPort + " to local " + targetAddress + ':' + targetPort );
		connection.requestRemotePortForwarding( bindAddress, bindPort, targetAddress, targetPort );
		return targetPort;
	}

	/**
	 * Get any free local TCP port (typically used for local port forwarding).
	 *
	 * @return Free local TCP port.
	 *
	 * @throws IOException if no free local TCP port is available.
	 */
	public static int getFreeLocalPort()
	throws IOException
	{
		final ServerSocket serverSocket = new ServerSocket( 0 );
		try
		{
			return serverSocket.getLocalPort();
		}
		finally
		{
			serverSocket.close();
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private SshTools()
	{
	}

	/**
	 * Defines local or remote port forwarding properties.
	 */
	public static class PortForwardingProperties
	{
		/**
		 * Source address.
		 */
		@Nullable
		private final String _sourceAddress;

		/**
		 * Source port.
		 */
		private final int _sourcePort;

		/**
		 * Target address.
		 */
		@Nullable
		private final String _targetAddress;

		/**
		 * Target port.
		 */
		private final int _targetPort;

		/**
		 * Create forwarding properties.
		 *
		 * @param sourceAddress Source address.
		 * @param sourcePort    Source port.
		 * @param targetAddress Target address.
		 * @param targetPort    Target port.
		 */
		public PortForwardingProperties( @Nullable final String sourceAddress, final int sourcePort, @Nullable final String targetAddress, final int targetPort )
		{
			_sourceAddress = sourceAddress;
			_sourcePort = sourcePort;
			_targetAddress = targetAddress;
			_targetPort = targetPort;
		}

		/**
		 * Get source address.
		 *
		 * @return Source address.
		 */
		@Nullable
		public String getSourceAddress()
		{
			return _sourceAddress;
		}

		/**
		 * Get source port.
		 *
		 * @return Source port.
		 */
		public int getSourcePort()
		{
			return _sourcePort;
		}

		/**
		 * Get target address.
		 *
		 * @return Target address.
		 */
		@Nullable
		public String getTargetAddress()
		{
			return _targetAddress;
		}

		/**
		 * Get target port.
		 *
		 * @return Target port.
		 */
		public int getTargetPort()
		{
			return _targetPort;
		}
	}
}
