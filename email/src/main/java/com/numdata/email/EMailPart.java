/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import java.io.*;
import java.text.*;
import java.util.*;

import com.numdata.oss.Base64;
import com.numdata.oss.io.*;

/**
 * This class represents an E-mail message part. An E-mail message itself is
 * actually a derivative of this class.
 *
 * @author Peter S. Heijnen
 */
public class EMailPart
{
	/**
	 * Encoding: plain text.
	 */
	public static final int PLAIN_TEXT = 0;

	/**
	 * Encoding: BASE64-encoded binary.
	 */
	public static final int BINARY = 1;

	/**
	 * Encoding: quoted text.
	 */
	public static final int QUOTED = 2;

	/**
	 * Data format used for boundary dates.
	 */
	private static final SimpleDateFormat BOUNDARY_DATE_FORMAT = new SimpleDateFormat( "ss_SSS" );

	/**
	 * Content type.
	 */
	public String contentType = "Text";

	/**
	 * Content sub-type.
	 */
	public String contentSubType = "Plain";

	/**
	 * Part name.
	 */
	public String name = "";

	/**
	 * Part filename.
	 */
	public String filename = "";

	/**
	 * Content data.
	 */
	private final String _data;

	/**
	 * Content encoding.
	 */
	private final int _encoding;

	/**
	 * Parts within this part.
	 */
	private final List<EMailPart> _parts;

	/**
	 * Construct E-mail part that may be used as a container for other parts.
	 */
	public EMailPart()
	{
		_data = null;
		_encoding = 0;
		_parts = new ArrayList<EMailPart>();
	}

	/**
	 * Construct a binary E-mail part from a byte array.
	 *
	 * @param data Byte array with binary content.
	 */
	public EMailPart( final byte[] data )
	{
		contentType = "Application";
		contentSubType = "Octet-Stream";

		_data = Base64.encodeBase64( data );
		_encoding = 1;
		_parts = null;
	}

	/**
	 * Construct a binary E-mail part from a file.
	 *
	 * @param file File with binary content.
	 *
	 * @throws IOException if an I/O error occurred while reading the file.
	 */
	public EMailPart( final File file )
	throws IOException
	{
		final byte[] data;
		final FileInputStream is = new FileInputStream( file );
		try
		{
			data = DataStreamTools.readByteArray( is );
		}
		finally
		{
			is.close();
		}

		contentType = "Application";
		contentSubType = "Octet-Stream";
		filename = file.getName();

		_data = Base64.encodeBase64( data );
		_encoding = BINARY;
		_parts = null;
	}

	/**
	 * Construct a text E-mail part from a string. The text is assumed to be of
	 * mime type 'text/plain'.
	 *
	 * @param data Content string.
	 */
	public EMailPart( final String data )
	{
		this( data, PLAIN_TEXT );
	}

	/**
	 * Construct a text E-mail part from a string using the specified encoding.
	 * <dl>
	 *
	 * <dt>PLAIN_TEXT</dt><dd> The text content is broken into lines with a 80
	 * character maximum and DOS-style line termination (CR + LF) is replaced by
	 * Unix-style line termination (just LF).</dd>
	 *
	 * <dt>QUOTED</dt><dd></dd>
	 *
	 * <dt>BINARY</dt><dd> The content string is converted to a byte array by
	 * simply truncating character codes to 8 bits and encoding it using BASE64
	 * encoding.</dd>
	 *
	 * </dl>
	 *
	 * @param data     Content string.
	 * @param encoding Content encoding (PLAIN_TEXT, QUOTED, or BINARY).
	 */
	public EMailPart( String data, final int encoding )
	{
		String filteredData = data;

		switch ( encoding )
		{
			case QUOTED:
				filteredData = encodeQuoted( filteredData );
				//noinspection fallthrough

			case PLAIN_TEXT:
				final StringBuilder sb = new StringBuilder();

				int cur = 0;
				while ( cur < filteredData.length() )
				{
					int eol = filteredData.indexOf( "\r\n", cur );
					int skip = 2;

					final int eol_lf = filteredData.indexOf( '\n', cur );
					if ( eol_lf < eol && eol_lf >= 0 )
					{
						eol = eol_lf;
						skip = 1;
					}

					if ( eol == -1 )
					{
						eol = filteredData.length();
						skip = 0;
					}

					if ( eol - cur > 82 )
					{
						eol = cur + 80;
						skip = 0;
					}

					if ( ( eol > cur ) && filteredData.charAt( cur ) == '.' )
					{
						sb.append( '.' );
					}

					sb.append( filteredData.substring( cur, eol ) );
					cur = eol + skip;
				}

				filteredData = sb.toString();
				break;

			case BINARY:
				final byte[] bytes = new byte[ filteredData.length() ];
				for ( int i = 0; i < filteredData.length(); i++ )
				{
					bytes[ i ] = (byte)filteredData.charAt( i );
				}
				filteredData = Base64.encodeBase64( bytes );
				break;

			default:
				throw new IllegalArgumentException( "unknown encoding" );
		}

		_data = filteredData;
		_encoding = encoding;
		_parts = null;
	}

	/**
	 * Add EMail part.
	 *
	 * @param part EMail part.
	 */
	public final void addPart( final EMailPart part )
	{
		_parts.add( part );
	}

	/**
	 * Append content for this part to the specified target buffer.
	 *
	 * @param sb Target buffer for part content.
	 */
	public void appendContent( final StringBuffer sb )
	{
		final List<EMailPart> parts = _parts;
		if ( parts != null )
		{
			if ( parts.size() < 1 )
			{
				throw new IllegalArgumentException( "missing content" );
			}

			String boundary = null;

			if ( parts.size() > 1 )
			{
				final Calendar calendar = Calendar.getInstance();
				boundary = "--=SODA_" + BOUNDARY_DATE_FORMAT.format( calendar.getTime() );

				for ( long startTime = System.currentTimeMillis();
				      startTime == System.currentTimeMillis(); )
				{
					try
					{
						Thread.sleep( 1 );
					}
					catch ( InterruptedException ignored )
					{
					}
				}

				sb.append( "\r\nContent-Type: multipart/alternative;\r\n     boundary=\"" );
				sb.append( boundary );
				sb.append( '"' );
			}

			for ( int i = 0; i < parts.size(); i++ )
			{
				final EMailPart part = parts.get( i );

				if ( parts.size() > 1 )
				{
					sb.append( "\r\n--" );
					sb.append( boundary );
				}

				sb.append( "\r\n" );
				part.appendContent( sb );
				sb.append( "\r\n" );
			}

			if ( parts.size() > 1 )
			{
				sb.append( "\r\n--" );
				sb.append( boundary );
				sb.append( "--" );
			}
		}
		else
		{
			sb.append( "Content-Type: " );
			sb.append( contentType );
			sb.append( '/' );
			sb.append( contentSubType );

			final String partName = name;
			if ( ( partName != null ) && !partName.isEmpty() )
			{
				sb.append( ";\r\n     name=\"" );
				sb.append( partName );
				sb.append( '"' );
			}

			final String partFilename = filename;
			if ( ( partFilename != null ) && !partFilename.isEmpty() )
			{
				sb.append( "\r\nContent-Disposition: attachment;" );
				sb.append( "\r\n     filename=\"" );
				sb.append( partFilename );
				sb.append( '"' );
			}

			switch ( _encoding )
			{
				case BINARY:
					sb.append( "\r\nContent-Transfer-Encoding: base64" );
					break;

				case QUOTED:
					sb.append( "\r\nContent-Transfer-Encoding: quoted-printable" );
			}

			sb.append( "\r\n\r\n" );
			sb.append( _data );
		}
	}

	/**
	 * Encode the specified string as quoted text.
	 *
	 * @param text Source text.
	 *
	 * @return Text after encoding as quoted text.
	 */
	public static String encodeQuoted( final CharSequence text )
	{
		final StringBuilder sb = new StringBuilder();

		int len = 0;
		for ( int i = 0; i < text.length(); i++ )
		{
			final char c = text.charAt( i );
			boolean cok = false;

			if ( len > 72 )
			{
				sb.append( "=\r\n" );
				len = 0;
			}

			if ( c >= '!' && c <= '<' || c >= '>' && c <= '~' )
			{
				sb.append( c );
				len++;
				cok = true;
			}

			if ( ( c == '\t' || c == ' ' ) && sb.length() < 72 )
			{
				len++;
				sb.append( c );
				cok = true;
			}

			if ( !cok )
			{
				sb.append( '=' );
				final String hex = Integer.toHexString( (int)c );
				if ( hex.length() == 1 )
				{
					sb.append( '0' );
					sb.append( hex.charAt( 0 ) );
				}
				else
				{
					sb.append( hex.charAt( hex.length() - 2 ) );
					sb.append( hex.charAt( hex.length() - 1 ) );
				}
				len += 3;
			}
		}

		return sb.toString();
	}

	/**
	 * Get content of message.
	 *
	 * @return String with content.
	 */
	public final String getContent()
	{
		final StringBuffer sb = new StringBuffer();
		appendContent( sb );
		return sb.toString();
	}

}
