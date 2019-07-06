/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * Identifies a database by its name and, optionally, a server host name and
 * port number. Useful for command-line tools where a full JDBC URL would result
 * in excessive typing.
 *
 * Syntax: &lt;databaseName&gt;[@&lt;databaseServer&gt;[:&lt;databasePort&gt;][;user=&lt;databaseUser&gt;][;pass=&lt;databasePassword&gt;][@[&lt;user&gt;[:&lt;pass&gt;]@]&lt;host&gt;[:&lt;port&gt;]]]
 *
 * @author G. Meinders
 */
public class DatabaseName
{
	/**
	 * Regex pattern to match syntax (see class comment).
	 */
	private static final Pattern PATTERN = Pattern.compile( "(\\w+)(?:@([\\w.]+)(?::([0-9]{1,5}))?(?:;user=([^;@]+))?(?:;pass=([^;@]+))?(?:@(.+))?)?" );

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

	@Nullable
	public String getTunnel()
	{
		return _tunnel;
	}

	@Nullable
	public String getDatabase()
	{
		return _database;
	}

	@Nullable
	public String getHost()
	{
		return _host;
	}

	@Nullable
	public Integer getPort()
	{
		return _port;
	}

	@Nullable
	public String getUser()
	{
		return _user;
	}

	@Nullable
	public String getPassword()
	{
		return _password;
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
		final Matcher matcher = PATTERN.matcher( string );
		if ( !matcher.matches() )
		{
			throw new IllegalArgumentException( string );
		}

		final String database = matcher.group( 1 );
		final String host = matcher.group( 2 );
		final String port = matcher.group( 3 );
		final String user = matcher.group( 4 );
		final String pass = matcher.group( 5 );
		final String tunnel = matcher.group( 6 );

		return new DatabaseName( host, ( port == null ) ? null : Integer.valueOf( port ), database, user, pass, tunnel );
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
