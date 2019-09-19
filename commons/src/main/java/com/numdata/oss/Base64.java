/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.io.*;

/**
 * Encodes binary data using Base64 encoding.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders
 */
public class Base64
{
	/**
	 * Character set for Base64 encoding.
	 */
	private static final char[] BASE64_CHARS =
	{
	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
	'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
	'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
	'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};

	/**
	 * Encodes binary data using Base64 encoding.
	 *
	 * @param data Binary data to encode.
	 *
	 * @return Base64 encoded data.
	 */
	public static String encodeBase64( final byte[] data )
	{
		final StringBuilder sb = new StringBuilder();

		int i = 0;
		while ( i < data.length )
		{
			final int b1 = data[ i++ ];
			final boolean h2 = ( i < data.length );
			final int b2 = h2 ? (int)data[ i++ ] : 0;
			final boolean h3 = ( i < data.length );
			final int b3 = h3 ? (int)data[ i++ ] : 0;

			sb.append( BASE64_CHARS[ b1 >>> 2 & 0x3f ] );
			sb.append( BASE64_CHARS[ b1 << 4 & 0x3f | b2 >>> 4 & 0x0f ] );
			sb.append( h2 ? BASE64_CHARS[ b2 << 2 & 0x3f | b3 >>> 6 & 0x03 ] : '=' );
			sb.append( h3 ? BASE64_CHARS[ b3 & 0x3f ] : '=' );

			if ( ( i % 54 ) == 0 )
			{
				sb.append( "\r\n" );
			}
		}

		return sb.toString();
	}

	/**
	 * Decodes the given Base64 encoded data.
	 *
	 * @param data Encoded data.
	 *
	 * @return Decoded data.
	 */
	public static byte[] decodeBase64( final String data )
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream( ( data.length() / 4 ) * 3 );

		try
		{
			decodeBase64( out, new StringReader( data ) );
		}
		catch ( final IOException e )
		{
			// NOTE: The streams used here don't throw IOException.
			throw new RuntimeException( e );
		}

		return out.toByteArray();
	}

	/**
	 * Decodes Base64 encoded data.
	 *
	 * @param out Stream to write the decoded data to.
	 * @param in  Stream to read the encoded data from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void decodeBase64( final OutputStream out, final Reader in )
	throws IOException
	{
		final byte[] chunk = new byte[ 4 ];
		int matched = 0;
		for ( int read = in.read(); read != -1; read = in.read() )
		{
			final int token = decode( (char)read );
			if ( token != -1 )
			{
				chunk[ matched++ ] = (byte)token;
			}

			if ( matched == 4 )
			{
				if ( chunk[ 2 ] == 64 )
				{
					out.write( ( chunk[ 0 ] & 0x3f ) << 2 | ( chunk[ 1 ] >> 4 ) & 0x0f );
					break;
				}

				if ( chunk[ 3 ] == 64 )
				{
					out.write( ( chunk[ 0 ] & 0x3f ) << 2 | ( chunk[ 1 ] >> 4 ) & 0x0f );
					out.write( ( chunk[ 1 ] & 0x0f ) << 4 | ( chunk[ 2 ] & 0x3f ) >> 2 );
					break;
				}

				out.write( ( chunk[ 0 ] & 0x3f ) << 2 | ( chunk[ 1 ] >> 4 ) & 0x0f );
				out.write( ( chunk[ 1 ] & 0x0f ) << 4 | ( chunk[ 2 ] & 0x3f ) >> 2 );
				out.write( ( chunk[ 2 ] & 0x03 ) << 6 | ( chunk[ 3 ] & 0x3f ) );
				matched = 0;
			}
		}
	}

	/**
	 * Decodes the given character of Base64 encoded data. For a character in
	 * the Base64 alphabet, the associated value (between 0 and 63, inclusive)
	 * is returned. For the padding character ('='), 64 is returned. If any
	 * other character is given, -1 is returned.
	 *
	 * @param c Character to decode.
	 *
	 * @return Decoded character.
	 */
	private static int decode( final char c )
	{
		final int result;
		if ( c >= 'A' && c <= 'Z' )
		{
			result = c - 'A';
		}
		else if ( c >= 'a' && c <= 'z' )
		{
			result = 26 + ( c - 'a' );
		}
		else if ( c >= '0' && c <= '9' )
		{
			result = 52 + ( c - '0' );
		}
		else if ( c == '+' )
		{
			result = 62;
		}
		else if ( c == '/' )
		{
			result = 63;
		}
		else if ( c == '=' )
		{
			result = 64;
		}
		else
		{
			result = -1;
		}
		return result;
	}
}
