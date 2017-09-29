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

package com.numdata.oss.net;

import java.io.*;
import java.nio.charset.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Provides utility methods for HTTP.
 *
 * @author Gerrit Meinders
 */
public class HttpTools
{
	/**
	 * Encodes a header parameter according to RFC 5987. This is used, for
	 * example, for the {@code filename} parameter of the {@code
	 * Content-Disposition} header.
	 *
	 * If the value contains only US-ASCII characters, the parameter is added
	 * as-is (according to RFC 2616: HTTP/1.1). Otherwise the parameter is
	 * included with non-US-ASCII characters replaced, and an additional
	 * parameter {@code <var>name;</var>*} is also added, with an escaped UTF-8
	 * representation of the value.
	 *
	 * The name of the parameter is not encoded and should only consist of
	 * characters in ISO-8859-1.
	 *
	 * @param name  Name of the parameter.
	 * @param value Value of the parameter.
	 *
	 * @return Encoded header parameter.
	 */
	@NotNull
	public static String encodeHeaderParameter( @NotNull final String name, @NotNull final String value )
	{
		try
		{
			return encodeHeaderParameter( new StringBuilder(), name, value ).toString();
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e ); // 'StringBuilder' should not throw 'IOException'.
		}
	}

	/**
	 * Appends a header parameter encodes according to RFC 5987 to the given
	 * character sequence. This is used, for example, for the {@code filename}
	 * parameter of the {@code Content-Disposition} header.
	 *
	 * If the value contains only US-ASCII characters, the parameter is added
	 * as-is (according to RFC 2616: HTTP/1.1). Otherwise the parameter is
	 * included with non-US-ASCII characters replaced, and an additional
	 * parameter {@code <var>name</var>*} is also added, with an escaped UTF-8
	 * representation of the value.
	 *
	 * The name of the parameter is not encoded and should only consist of
	 * characters in ISO-8859-1.
	 *
	 * @param result Character sequence to append to.
	 * @param name   Name of the parameter.
	 * @param value  Value of the parameter.
	 *
	 * @return The given character sequence.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	@NotNull
	public static Appendable encodeHeaderParameter( @NotNull final Appendable result, @NotNull final String name, @NotNull final String value )
	throws IOException
	{
		// RFC 2616 allows ISO-8859-1, but RFC 5987 recommends US-ASCII for compatibility.
		final Charset charset = Charset.forName( "US-ASCII" );

		final byte[] encoded = value.getBytes( charset );
		final String decoded = new String( encoded, charset );
		final String escaped = decoded.replace( "\"", "\\\"" );
		result.append( name );
		result.append( "=\"" );
		result.append( escaped );
		result.append( '"' );

		if ( !value.equals( decoded ) )
		{
			result.append( "; " );
			result.append( name );
			result.append( "*=UTF-8''" );
			final byte[] utfBytes = value.getBytes( Charset.forName( "UTF-8" ) );

			for ( final byte b : utfBytes )
			{
				final char c = (char)b;

				if ( ( ( c >= 'A' ) && ( c <= 'Z' ) ) || ( ( c >= 'a' ) && ( c <= 'z' ) ) ||
				     ( ( c >= '0' ) && ( c <= '9' ) ) ||
				     ( "!#$&+-.^_`|~".indexOf( c ) != -1 ) )
				{
					// Characters in the attr-char production are allowed (RFC 5987, section 3.2.1).
					result.append( c );
				}
				else
				{
					// Character is not allowed. Apply percent encoding with uppercase hexdigits (RFC 3986, section 2.1).
					result.append( '%' );
					TextTools.appendHexString( result, c, 2, true );
				}
			}
		}

		return result;
	}

	/**
	 * No instances should be created.
	 */
	private HttpTools()
	{
	}
}
