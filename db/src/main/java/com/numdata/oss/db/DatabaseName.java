/*
 * Copyright (c) 2011-2021, Unicon Creation BV, The Netherlands.
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
package com.numdata.oss.db;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Identifies a database by its name and, optionally, a server host name and
 * port number. Useful for command-line tools where a full JDBC URL would result
 * in excessive typing.
 *
 * Syntax: &lt;databaseName&gt;[@&lt;databaseServer&gt;[:&lt;databasePort&gt;][;user=&lt;databaseUser&gt;][;pass=&lt;databasePassword&gt;][@[&lt;user&gt;[:&lt;pass&gt;]@]&lt;host&gt;[:&lt;port&gt;]]]
 *
 * @author Gerrit Meinders
 */
public class DatabaseName
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( DatabaseName.class );

	/**
	 * Regex pattern to match syntax (see class comment).
	 */
	private static final Pattern PATTERN = Pattern.compile( "(\\w+)(?:@([\\w.-]+)(?::([0-9]{1,5}))?(?:;user=([^;@]+))?(?:;pass=([^;@]+))?(?:@(.+))?)?" );

	/**
	 * SSH tunnel information (&lt;user&gt;:&lt;pass&gt;@&lt;host&gt;[':'&lt;port&gt;]).
	 */
	@Nullable
	private String _tunnel;

	/**
	 * Host name.
	 */
	@Nullable
	private String _host;

	/**
	 * Port number.
	 */
	@Nullable
	private Integer _port;

	/**
	 * Database name.
	 */
	@Nullable
	private String _database;

	/**
	 * Database user.
	 */
	@Nullable
	private String _user;

	/**
	 * Database password.
	 */
	@Nullable
	private String _password;

	/**
	 * JDBC driver.
	 */
	@Nullable
	private String _jdbcDriver;

	/**
	 * JDBC URL format ({0}=host, {1}=port, {2}=suffix).
	 */
	@Nullable
	private String _jdbcUrlFormat;

	/**
	 * Constructs a new instance.
	 *
	 * @param host     Host name.
	 * @param port     Port number.
	 * @param database Database name.
	 * @param user     Database user.
	 * @param password Database password.
	 * @param tunnel   SSH tunnel information (&lt;user&gt;:&lt;pass&gt;@&lt;host&gt;[':'&lt;port&gt;]).
	 */
	public DatabaseName( @Nullable final String host, @Nullable final Integer port, @Nullable final String database, @Nullable final String user, @Nullable final String password, @Nullable final String tunnel )
	{
		_tunnel = tunnel;
		_database = database;
		_user = user;
		_password = password;
		_host = host;
		_port = port;
	}

	/**
	 * Constructs a copy of the given instance.
	 *
	 * @param original Original to copy.
	 */
	public DatabaseName( final @NotNull DatabaseName original )
	{
		_tunnel = original._tunnel;
		_host = original._host;
		_port = original._port;
		_database = original._database;
		_user = original._user;
		_password = original._password;
		_jdbcDriver = original._jdbcDriver;
		_jdbcUrlFormat = original._jdbcUrlFormat;
	}

	@Nullable
	public String getTunnel()
	{
		return _tunnel;
	}

	public void setTunnel( final @Nullable String tunnel )
	{
		_tunnel = tunnel;
	}

	@Nullable
	public String getDatabase()
	{
		return _database;
	}

	public void setDatabase( final @Nullable String database )
	{
		_database = database;
	}

	@Nullable
	public String getHost()
	{
		return _host;
	}

	public void setHost( final @Nullable String host )
	{
		_host = host;
	}

	@Nullable
	public Integer getPort()
	{
		return _port;
	}

	public void setPort( final @Nullable Integer port )
	{
		_port = port;
	}

	@Nullable
	public String getUser()
	{
		return _user;
	}

	public void setUser( final @Nullable String user )
	{
		_user = user;
	}

	@Nullable
	public String getPassword()
	{
		return _password;
	}

	public void setPassword( final @Nullable String password )
	{
		_password = password;
	}

	public @Nullable String getJdbcDriver()
	{
		return _jdbcDriver;
	}

	public void setJdbcDriver( final @Nullable String jdbcDriver )
	{
		_jdbcDriver = jdbcDriver;
	}

	public @Nullable String getJdbcUrlFormat()
	{
		return _jdbcUrlFormat;
	}

	public void setJdbcUrlFormat( final @Nullable String jdbcUrlFormat )
	{
		_jdbcUrlFormat = jdbcUrlFormat;
	}

	@Override
	public String toString()
	{
		final String result;

		if ( _host == null )
		{
			result = String.valueOf( _database );
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( _database );

			sb.append( '@' ).append( _host );
			if ( _port != null )
			{
				sb.append( ':' ).append( _port );
			}
			if ( _user != null )
			{
				sb.append( ";user=" ).append( _user );
			}
			if ( _password != null )
			{
				sb.append( ";pass=*****" );
			}

			if ( _tunnel != null )
			{
				sb.append( '@' ).append( _tunnel );
			}
			result = sb.toString();
		}

		return result;
	}

	/**
	 * Returns a database name object with the value represented by the given
	 * string. The syntax is {@code database-name ("@" host-name (":"
	 * port-number)? )?}.
	 *
	 * @param string String to be parsed.
	 *
	 * @return Database name.
	 *
	 * @throws IllegalArgumentException if the given string can't be parsed.
	 */
	@NotNull
	public static DatabaseName valueOf( @NotNull final String string )
	{
		String database;
		String host;
		String port;
		String user;
		String password;
		String tunnel;

		if ( TextTools.startsWith( string, '@' ) )
		{
			final Properties properties = new Properties();
			final String path = string.substring( 1 );
			final boolean debug = LOG.isDebugEnabled();
			if ( debug )
			{
				LOG.debug( "valueOf() propertiesFile=" + path );
			}
			try
			{
				File file = new File( path );
				boolean found = file.exists();
				if ( !found )
				{
					// automatically add '.properties' extension
					if ( !path.endsWith( ".properties" ) )
					{
						file = new File( path + ".properties" );
						found = file.exists();
					}

					if ( !found )
					{
						throw new RuntimeException( "Missing database properties file '" + path + '\'' );
					}
				}

				try ( final InputStream in = new FileInputStream( file ) )
				{
					properties.load( in );
				}
			}
			catch ( final IOException e )
			{
				throw new RuntimeException( "Failed to read database properties from '" + path + "' file", e );
			}

			database = properties.getProperty( "databaseName" );
			if ( database == null )
			{
				database = properties.getProperty( "database" );
			}

			host = null;
			if ( database != null )
			{
				final int databaseAt = database.indexOf( '@' );
				if ( databaseAt > 0 )
				{
					host = database.substring( databaseAt + 1 );
					database = database.substring( 0, databaseAt );
				}
			}
			host = properties.getProperty( "databaseHost", host );
			if ( host == null )
			{
				host = properties.getProperty( "host" );
			}

			port = properties.getProperty( "databasePort" );
			if ( port == null )
			{
				port = properties.getProperty( "port" );
			}

			user = properties.getProperty( "databaseUser" );
			if ( user == null )
			{
				user = properties.getProperty( "user" );
			}

			password = properties.getProperty( "databasePassword" );
			if ( password == null )
			{
				password = properties.getProperty( "databasePass" );
				if ( password == null )
				{
					password = properties.getProperty( "password" );
					if ( password == null )
					{
						password = properties.getProperty( "pass" );
					}
				}
			}

			tunnel = properties.getProperty( "tunnel" );
			if ( tunnel == null )
			{
				final String sshUser = properties.getProperty( "sshUser" );
				final String sshPassword = properties.getProperty( "sshPassword", properties.getProperty( "sshPass" ) );
				final String sshHost = properties.getProperty( "sshHost" );
				final String sshPort = properties.getProperty( "sshPort" );
				if ( sshHost != null )
				{
					tunnel = ( ( sshUser != null ) ? sshUser + ( ( sshPassword != null ) ? ':' + sshPassword : "" ) + '@' : "" ) + sshHost + ( ( sshPort != null ) ? ':' + sshPort : "" );
				}
			}
		}
		else
		{
			final Matcher matcher = PATTERN.matcher( string );
			if ( !matcher.matches() )
			{
				throw new IllegalArgumentException( string );
			}

			database = matcher.group( 1 );
			host = matcher.group( 2 );
			port = matcher.group( 3 );
			user = matcher.group( 4 );
			password = matcher.group( 5 );
			tunnel = matcher.group( 6 );
		}

		return new DatabaseName( host, ( port == null ) ? null : Integer.valueOf( port ), database, user, password, tunnel );
	}

	/**
	 * Returns a database name object with the value represented by the given
	 * string. The given default values are used for any optional parts of the
	 * syntax that are not specified.
	 *
	 * @param string       String to be parsed.
	 * @param defaultValue Default values for optional fragments.
	 *
	 * @return Database name.
	 *
	 * @throws IllegalArgumentException if the given string can't be parsed.
	 */
	@NotNull
	public static DatabaseName valueOf( @NotNull final String string, @NotNull final DatabaseName defaultValue )
	{
		final DatabaseName result = valueOf( string );

		if ( result._database == null )
		{
			result._database = defaultValue.getDatabase();
		}

		if ( result._tunnel == null )
		{
			result._tunnel = defaultValue.getTunnel();
		}

		if ( result._host == null )
		{
			result._host = defaultValue.getHost();
		}

		if ( result._port == null )
		{
			result._port = defaultValue.getPort();
		}

		if ( result._user == null )
		{
			result._user = defaultValue.getUser();
		}

		if ( result._password == null )
		{
			result._password = defaultValue.getPassword();
		}

		return result;
	}
}
