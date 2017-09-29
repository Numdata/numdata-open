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
package com.numdata.oss.io;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import com.numdata.oss.*;

/**
 * This output stream sends its output over the network to a SYSLOG server.
 *
 * Valid SYSLOG messages can only contain characters within the ASCII range 32
 * to 126.
 *
 * @author H.B.J. te Lintelo
 */
public final class SyslogOutputStream
extends OutputStream
{
	/** Standard SYSLOG priority: emergency. */
	public static final int FACILITY_EMERG = 0;

	/** Standard SYSLOG priority: alert. */
	public static final int FACILITY_ALERT = 1;

	/** Standard SYSLOG priority: critial error. */
	public static final int FACILITY_CRIT = 2;

	/** Standard SYSLOG priority: error. */
	public static final int FACILITY_ERR = 3;

	/** Standard SYSLOG priority: warning. */
	public static final int FACILITY_WARNING = 4;

	/** Standard SYSLOG priority: notice. */
	public static final int FACILITY_NOTICE = 5;

	/** Standard SYSLOG priority: information. */
	public static final int FACILITY_INFO = 6;

	/** Standard SYSLOG priority: debug message. */
	public static final int FACILITY_DEBUG = 7;

	/** Standard SYSLOG facility: kernel. */
	public static final int FACILITY_KERN = ( 0 << 3 );

	/** Standard SYSLOG facility: user-space. */
	public static final int FACILITY_USER = ( 1 << 3 );

	/** Standard SYSLOG facility: mail daemon. */
	public static final int FACILITY_MAIL = ( 2 << 3 );

	/** Standard SYSLOG facility: service daemon. */
	public static final int FACILITY_DAEMON = ( 3 << 3 );

	/** Standard SYSLOG facility: authentication. */
	public static final int FACILITY_AUTH = ( 4 << 3 );

	/** Standard SYSLOG facility: syslog service. */
	public static final int FACILITY_SYSLOG = ( 5 << 3 );

	/** Standard SYSLOG facility: printer service. */
	public static final int FACILITY_LPR = ( 6 << 3 );

	/** Standard SYSLOG facility: usenet service. */
	public static final int FACILITY_NEWS = ( 7 << 3 );

	/** Standard SYSLOG facility: UUCP service. */
	public static final int FACILITY_UUCP = ( 8 << 3 );

	/** Standard SYSLOG facility: cron daemon. */
	public static final int FACILITY_CRON = ( 9 << 3 );

	/** Standard SYSLOG facility: authentication. */
	public static final int FACILITY_AUTHPRIV = ( 10 << 3 );

	/** Standard SYSLOG facility: FTP service. */
	public static final int FACILITY_FTP = ( 11 << 3 );

	/* Facility 12 - 15 are reserved for system use. */

	/** User-assigned SYSLOG facility: local0. */
	public static final int FACILITY_LOCAL0 = ( 16 << 3 );

	/** User-assigned SYSLOG facility: local1. */
	public static final int FACILITY_LOCAL1 = ( 17 << 3 );

	/** User-assigned SYSLOG facility: local2. */
	public static final int FACILITY_LOCAL2 = ( 18 << 3 );

	/** User-assigned SYSLOG facility: local3. */
	public static final int FACILITY_LOCAL3 = ( 19 << 3 );

	/** User-assigned SYSLOG facility: local4. */
	public static final int FACILITY_LOCAL4 = ( 20 << 3 );

	/** User-assigned SYSLOG facility: local5. */
	public static final int FACILITY_LOCAL5 = ( 21 << 3 );

	/** User-assigned SYSLOG facility: local6. */
	public static final int FACILITY_LOCAL6 = ( 22 << 3 );

	/** User-assigned SYSLOG facility: local7. */
	public static final int FACILITY_LOCAL7 = ( 23 << 3 );

	/**
	 * Standard syslog port.
	 */
	public static final int LOG_PORT = 514;

	/**
	 * Server name where to send the log (default: 'localhost').
	 */
	private final InetAddress _server;

	/**
	 * Facility log level (default FACILITY_USER).
	 */
	private final int _facility;

	/**
	 * Application name (default: unknown).
	 */
	private final String _applicationName;

	/**
	 * Port number which the server receives messages.
	 */
	private final int _port;

	/**
	 * Datagram socket for syslog service.
	 */
	private final DatagramSocket _sysLogSocket;

	/**
	 * Message buffer.
	 */
	private final StringBuffer _message;

	/**
	 * Initial message length.
	 */
	private final int _messageResetLength;

	/**
	 * Create a SyslogOutputStream based on the specified options string.
	 *
	 * Syntax: [{serverName}][:[{port}][:[{facility}][:[{application}]]]]
	 *
	 * @param options String with syslog options (see above).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public SyslogOutputStream( final String options )
	throws IOException
	{
		final String[] parsed = TextTools.tokenize( options, ':' );

		/*
		 * Get server name from options string.
		 */
		String s = "localhost";
		if ( parsed.length >= 1 && parsed[ 0 ].length() > 0 )
		{
			s = parsed[ 0 ];
		}
		final String serverName = s;

		try
		{
			_server = InetAddress.getByName( serverName );
		}
		catch ( UnknownHostException ue )
		{
			throw new IOException( "Failed to lookup IP address for '" + serverName + "'" );
		}

		/*
		 * Get port from options string.
		 */
		int i = SyslogOutputStream.LOG_PORT;
		if ( parsed.length >= 2 && parsed[ 1 ].length() > 0 )
		{
			try
			{
				i = Integer.parseInt( parsed[ 1 ] );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		_port = i;

		/*
		 * Get facility from options string.
		 */
		i = SyslogOutputStream.FACILITY_USER;
		if ( parsed.length >= 3 && parsed[ 2 ].length() > 0 )
		{
			final String facilityName = parsed[ 2 ];
			try
			{
				final Field facilityField = SyslogOutputStream.class.getDeclaredField( facilityName );
				i = facilityField.getInt( null );
			}
			catch ( Exception e )
			{
				throw new IllegalArgumentException( "Unknown facility '" + facilityName + "'" );
			}
		}
		_facility = i;

		/*
		 * Get application name from options string.
		 */
		if ( parsed.length >= 4 && parsed[ 3 ].length() > 0 )
		{
			s = parsed[ 3 ];
		}
		else
		{
			s = getClass().getName();
			s = s.substring( s.lastIndexOf( ':' ) + 1 );
		}
		_applicationName = s;

		/*
		 * Create datagram socket.
		 */
		try
		{
			_sysLogSocket = new DatagramSocket();
		}
		catch ( SocketException se )
		{
			throw new IOException( "can't create datagram socket" );
		}

		_sysLogSocket.connect( _server, _port );

		/*
		 * Create message buffer.
		 */
		_message = new StringBuffer()
		.append( '<' )
		.append( _facility | FACILITY_INFO )
		.append( "> " )
		.append( _applicationName )
		.append( ": " );

		_messageResetLength = _message.length();
	}

	/**
	 * Writes the specified byte to this output stream. The general contract for
	 * {@code write} is that one byte is written to the output stream. The byte
	 * to be written is the eight low-order bits of the argument {@code b}.
	 *
	 * Only characters %d32 - %d126 are valid to write.
	 *
	 * @param b the {@code byte}.
	 *
	 * @throws IOException if an I/O error occurs. In particular, an {@code
	 * IOException} may be thrown if the output stream has been closed.
	 */
	@Override
	public void write( final int b )
	throws IOException
	{
		if ( b == '\n' )
		{
			/*
			 * Send syslog packet.
			 */
			final byte[] buf = _message.toString().getBytes();
			_sysLogSocket.send( new DatagramPacket( buf, buf.length ) );

			/*
			 * Clean buffer for next line.
			 */
			_message.setLength( _messageResetLength );
		}
		else
		{
			/*
			 * change /ignore non valid characters
			 */
			if ( b == '\t' ) // tab -> space
			{
				_message.append( ' ' );
			}
			else if ( b == '\b' ) // backspace -> delete last char in message
			{
				if ( _message.length() > 0 )
				{
					_message.deleteCharAt( _message.length() - 1 );
				}
			}
			else if ( b > 0 && b < 32 )
			{
				/*
				 * ignore
				 */
				return;
			}
			else if ( b > 126 && b < 255 ) // '&egrave' -> '?'
			{
				_message.append( '?' );
			}
			else
			{
				_message.append( (char)b );
			}
		}
	}

}
