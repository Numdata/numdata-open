/*
 * Copyright (c) 2012-2017, Numdata BV, The Netherlands.
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
package com.numdata.email;

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Settings for sending email.
 *
 * @author G. Meinders
 */
public class EmailSettings
{
	/**
	 * Setting name.
	 */
	public static final String PROTOCOL = "protocol";

	/**
	 * Setting name.
	 */
	public static final String HOST = "host";

	/**
	 * Setting name.
	 */
	public static final String PORT = "port";

	/**
	 * Setting name.
	 */
	public static final String USER = "user";

	/**
	 * Setting name.
	 */
	public static final String PASSWORD = "password";

	/**
	 * Setting name.
	 */
	public static final String FROM_ADDRESS = "fromAddress";

	/**
	 * Protocol to be used, e.g. "smtp".
	 */
	private String _protocol = "smtp";

	/**
	 * Hostname or IP address of the mail server.
	 */
	private String _host = "";

	/**
	 * Port number of the mail server. By default the protocol default is used.
	 */
	private int _port = 0;

	/**
	 * Username used to authenticate with the mail server.
	 */
	private String _user = "";

	/**
	 * Password used to authenticate with the mail server.
	 */
	private String _password = "";

	/**
	 * Default email address specified in 'FROM' header.
	 */
	private String _fromAddress = "";

	/**
	 * Constructs default settings.
	 */
	public EmailSettings()
	{
	}

	/**
	 * Construct settings from the specified {@link Properties}.
	 *
	 * @param settings Default settings (optional).
	 */
	public EmailSettings( final Properties settings )
	{
		if ( settings != null )
		{
			importSettings( settings );
		}
	}

	/**
	 * Imports order settings from the given properties, replacing the settings
	 * currently represented by this object.
	 *
	 * @param settings Properties representing the settings to be imported.
	 */
	public void importSettings( final Properties settings )
	{
		_protocol = PropertyTools.getString( settings, PROTOCOL, _protocol );
		_host = PropertyTools.getString( settings, HOST, _host );
		_port = PropertyTools.getInt( settings, PORT, _port );
		_user = PropertyTools.getString( settings, USER, _user );
		_password = PropertyTools.getString( settings, PASSWORD, _password );
		_fromAddress = PropertyTools.getString( settings, FROM_ADDRESS, _fromAddress );
	}

	/**
	 * Exports the order settings represented by this object to properties.
	 *
	 * @return Properties representing these settings.
	 */
	public NestedProperties exportSettings()
	{
		final NestedProperties result = new NestedProperties();
		result.set( PROTOCOL, _protocol );
		result.set( HOST, _host );
		result.set( PORT, _port );
		result.set( USER, _user );
		result.set( PASSWORD, _password );
		result.set( FROM_ADDRESS, _fromAddress );
		return result;
	}

	/**
	 * Returns the protocol to be used, e.g. "smtp".
	 *
	 * @return Protocol name.
	 */
	@NotNull
	public String getProtocol()
	{
		return _protocol;
	}

	/**
	 * Sets the protocol to be used, e.g. "smtp".
	 *
	 * @param protocol Protocol name.
	 */
	public void setProtocol( @NotNull final String protocol )
	{
		_protocol = protocol;
	}

	/**
	 * Returns the hostname or IP address of the mail server.
	 *
	 * @return Hostname or IP address.
	 */
	@NotNull
	public String getHost()
	{
		return _host;
	}

	/**
	 * Sets the hostname or IP address of the mail server.
	 *
	 * @param host Hostname or IP address.
	 */
	public void setHost( @NotNull final String host )
	{
		_host = host;
	}

	/**
	 * Returns the port number of the mail server. By default the protocol
	 * default is used.
	 *
	 * @return Mail server port number; zero for the default port.
	 */
	public int getPort()
	{
		return _port;
	}

	/**
	 * Sets the port number of the mail server.
	 *
	 * @param port Mail server port number; zero or negative for default port.
	 */
	public void setPort( final int port )
	{
		_port = port < 0 ? 0 : port;
	}

	/**
	 * Returns the username used to authenticate with the mail server.
	 *
	 * @return Username used to authenticate with the mail server.
	 */
	@Nullable
	public String getUser()
	{
		return _user;
	}

	/**
	 * Sets the username used to authenticate with the mail server.
	 *
	 * @param user Username used to authenticate with the mail server.
	 */
	public void setUser( @Nullable final String user )
	{
		_user = user;
	}

	/**
	 * Returns the password used to authenticate with the mail server.
	 *
	 * @return Password used to authenticate with the mail server.
	 */
	@Nullable
	public String getPassword()
	{
		return _password;
	}

	/**
	 * Sets the password used to authenticate with the mail server.
	 *
	 * @param password Password used to authenticate with the mail server.
	 */
	public void setPassword( @Nullable final String password )
	{
		_password = password;
	}

	/**
	 * Get default email address specified in 'FROM' header.
	 *
	 * @return Default email address specified in 'FROM' header.
	 */
	public String getFromAddress()
	{
		return _fromAddress;
	}

	/**
	 * Set default email address specified in 'FROM' header.
	 *
	 * @param fromAddress Default email address specified in 'FROM' header.
	 */
	public void setFromAddress( final String fromAddress )
	{
		_fromAddress = fromAddress;
	}
}
