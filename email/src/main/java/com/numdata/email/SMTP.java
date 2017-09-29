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
package com.numdata.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;

import com.numdata.oss.log.ClassLogger;

/**
 * This class can be used to send E-mail messages to an SMTP server.
 *
 * @author  Peter S. Heijnen
 */
public final class SMTP
{
	/** SMTP response code. */ public static final int SMTP_ERR_SYSTEM_REPLY          = 211;
	/** SMTP response code. */ public static final int SMTP_ERR_HELP                  = 214;
	/** SMTP response code. */ public static final int SMTP_ERR_READY                 = 220;
	/** SMTP response code. */ public static final int SMTP_ERR_CLOSING               = 221;
	/** SMTP response code. */ public static final int SMTP_ERR_COMPLETED             = 250;
	/** SMTP response code. */ public static final int SMTP_ERR_FORWARD               = 251;

	/** SMTP response code. */ public static final int SMTP_ERR_MAIL_START            = 354;

	/** SMTP response code. */ public static final int SMTP_ERR_UNAVAILABLE           = 421;
	/** SMTP response code. */ public static final int SMTP_ERR_MAIL_ACTION_NOT_TAKEN = 450;
	/** SMTP response code. */ public static final int SMTP_ERR_ACTION_ABORTED        = 451;
	/** SMTP response code. */ public static final int SMTP_ERR_SYSTEM_STORAGE        = 452;

	/** SMTP response code. */ public static final int SMTP_ERR_UNREC_COMMAND         = 500;
	/** SMTP response code. */ public static final int SMTP_ERR_PARAM_ERROR           = 501;
	/** SMTP response code. */ public static final int SMTP_ERR_IMPLEMENT_COMMAND     = 502;
	/** SMTP response code. */ public static final int SMTP_ERR_SEQUENCE_COMMAND      = 503;
	/** SMTP response code. */ public static final int SMTP_ERR_PARAM_COMMAND         = 504;
	/** SMTP response code. */ public static final int SMTP_ERR_ACTION_NOT_TAKEN      = 550;
	/** SMTP response code. */ public static final int SMTP_ERR_USER_NOT_LOCAL        = 551;
	/** SMTP response code. */ public static final int SMTP_ERR_MAIL_STORAGE          = 552;
	/** SMTP response code. */ public static final int SMTP_ERR_NAME_NOT_ALLOWED      = 553;
	/** SMTP response code. */ public static final int SMTP_ERR_TRANS_FAILED          = 554;

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SMTP.class );

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private SMTP()
	{
	}

	/**
	 * Get response string from SMTP server.
	 *
	 * @param   in      Stream through which data is received from the SMTP server.
	 *
	 * @return  Response code.
	 *
	 * @throws IOException if an I/O error occurred while receiving the response.
	 */
	static int getResponse( final InputStream in )
		throws IOException
	{
		int result = -1;

		for ( long startTime = System.currentTimeMillis() ; System.currentTimeMillis() - startTime < 50000L && result == -1; )
		{
			final StringBuilder sb = new StringBuilder();

			while ( in.available() > 0 && sb.length() < 255 )
			{
				sb.append( (char)in.read() );
			}

			if ( sb.length() > 0 )
			{
				try
				{
					result = Integer.valueOf( sb.substring( 0, 3 ) );
				}
				catch ( NumberFormatException e )
				{
					result = -1;
				}
			}
		}

		return result;
	}

	/**
	 * Send E-mail message.
	 *
	 * @param   eMail       E-mail message to send.
	 *
	 * @return  {@code true} if successful;
	 *          {@code false} otherwise.
	 */
	static boolean send( final EMail eMail )
	{
		Socket       sock   = null;
		InputStream  in     = null;
		OutputStream out    = null;
		boolean      result = false;

		try
		{
			sock = new Socket( eMail.smtpServer , eMail.smtpPort );
			in   = sock.getInputStream();
			out  = sock.getOutputStream();

			result = send( in , out , eMail );
		}
		catch ( Exception e )
		{
			LOG.error( "SMTP.send() caught exception" , e );
		}

		try { if ( out  != null )  out.close(); } catch ( Exception e ) { /* ignored */ }
		try { if ( in   != null )   in.close(); } catch ( Exception e ) { /* ignored */ }
		try { if ( sock != null ) sock.close(); } catch ( Exception e ) { /* ignored */ }

		return result;
	}

	/**
	 * Send E-mail message.
	 *
	 * @param   in          Stream through which data is received from the SMTP server.
	 * @param   out         Stream through which data is send to the SMTP server.
	 * @param   eMail       E-mail message to send.
	 *
	 * @return  {@code true} if successful;
	 *          {@code false} otherwise.
	 *
	 * @throws  IOException if an I/O error occurred while sending the message.
	 */
	static boolean send( final InputStream in , final OutputStream out , final EMail eMail )
		throws IOException
	{
		/*
		 * Check valid connection by getting 'READY' from server.
		 */
		if ( getResponse( in ) != SMTP_ERR_READY )
			return false;

		/*
		 * Reply 'HELO'
		 */
		write( out , "HELO " );
		write( out , eMail.smtpMyAddress );
		write( out , "\r\n" );

		if ( getResponse( in ) != SMTP_ERR_COMPLETED )
		{
			LOG.error( SMTP.class.getName() + ": Did not receive response to 'HELO'" );
			return false;
		}

		/*
		 * Send 'from'
		 */
		write( out , "MAIL FROM:" );
		writeEMailAddress( out , eMail.from );
		write( out , "\r\n" );

		if ( getResponse( in ) != SMTP_ERR_COMPLETED )
		{
			LOG.error( SMTP.class.getName() + ": Server did not accept 'FROM:" + eMail.from + '\'' );
			return false;
		}

		/*
		 * Send 'to'
		 */
		for ( Iterator<String> iterator = eMail.getRecipients() ; iterator.hasNext() ; )
		{
			final String to = (String)iterator.next();

			write( out , "RCPT TO:" );
			writeEMailAddress( out , to );
			write( out , "\r\n" );

			final int resp = getResponse( in );
			if( resp != SMTP_ERR_COMPLETED && resp != SMTP_ERR_FORWARD )
			{
				LOG.error( SMTP.class.getName() + ": Server did not accept 'RCPT TO: " + to + '\'' );
				return false;
			}
		}

		/*
		 * Send 'cc'
		 */
		for ( Iterator<String> iterator = eMail.getCarbonCopies() ; iterator.hasNext() ; )
		{
			final String cc = (String)iterator.next();

			write( out , "RCPT TO:" );
			writeEMailAddress( out , cc );
			write( out , "\r\n" );

			final int resp = getResponse( in );
			if( resp == -1 || resp == 0 )
			{
				LOG.error( SMTP.class.getName() + ": Server did not accept 'RCPT TO: " + cc + '\'' );
				return false;
			}
		}

		/*
		 * Send E-mail body
		 */
		write( out , "DATA\r\n" );

		if ( getResponse( in ) != SMTP_ERR_MAIL_START )
		{
			LOG.error( SMTP.class.getName() + ": Server does not accept 'DATA'" );
			return false;
		}

		/*
		 * Get lines from content.
		 */
		final String content = eMail.getContent();
		final int    l = content.length();

		for ( int pos = 0 ; pos >= 0 && pos < l ; )
		{
			final int i = content.indexOf( (int)'\r', pos );
			final int j = content.indexOf( (int)'\n', pos );
			final int eol;
			final int next;

			if ( i < 0 )
			{
				if ( j < 0 ) { eol = l; next = -1;    }
				        else { eol = j; next = j + 1; }
			}
			else
			{
				if ( j < 0 )
				{
					eol = i;
					next = i + 1;
				}
				else if ( i == j - 1 || i == j + 1 )
				{
					eol  = Math.min( i , j );
					next = Math.max( i , j ) + 1;
				}
				else
				{
					eol  = Math.min( i , j );
					next = eol + 1;
				}
			}

			final String line = content.substring( pos , eol );
			if ( eol > pos && line.charAt( 0 ) == '.' ) out.write( (int)'.' );
			write( out , line + "\r\n" );
			pos = next;
		}

		write( out , ".\r\n" );
		if ( getResponse( in ) != SMTP_ERR_COMPLETED )
		{
			LOG.error( SMTP.class.getName() + ": Server did not accept content" );
			return false;
		}

		write( out , "QUIT\r\n" );
		if ( getResponse( in ) != SMTP_ERR_CLOSING )
		{
			LOG.error( SMTP.class.getName() + ": Server failed to 'QUIT'" );
			return false;
		}

		return true;
	}

	/**
	 * Write string to the specified output stream.
	 *
	 * @param   out         Stream through which data is send to the SMTP server.
	 * @param   s           String to write.
	 *
	 * @throws IOException if an I/O problem occurred while writing the data.
	 */
	static void write( final OutputStream out , final String s )
		throws IOException
	{
		if ( s.length() > 0 )
			out.write( s.getBytes() );
	}

	/**
	 * Write E-mail address to the specified output stream. The E-mail address
	 * is written as '<{email address}>' to the stream. If the specified address
	 * contains a '<{email address}>' part, then that part will be send only.
	 *
	 * @param   out         Stream through which data is send to the SMTP server.
	 * @param   address     E-mail address.
	 *
	 * @throws IOException if an I/O problem occurred while writing the data.
	 */
	static void writeEMailAddress( final OutputStream out , final String address )
		throws IOException
	{
		String filteredAddress = address;

		int i = filteredAddress.indexOf( (int)'<' );
		if ( i >= 0 )
			filteredAddress = filteredAddress.substring( i + 1 );

		i = filteredAddress.indexOf( (int)'>' );
		if ( i >= 0 )
			filteredAddress = filteredAddress.substring( 0 , i );

		out.write( (int)'<' );
		write( out , filteredAddress );
		out.write( (int)'>' );
	}
}
