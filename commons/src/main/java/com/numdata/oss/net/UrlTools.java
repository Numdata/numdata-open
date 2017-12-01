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
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class has some utility methods for building an URL or URI.
 *
 * @author Peter S. Heijnen
 */
public class UrlTools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( UrlTools.class );

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private UrlTools()
	{
	}

	/**
	 * Convert {@link URL} to {@link URI}.
	 *
	 * @param url URL to convert to URI.
	 *
	 * @return {@link URI}.
	 *
	 * @throws IllegalArgumentException if URL can not be converted.
	 */
	public static URI toURI( final URL url )
	{
		final URI uri;
		try
		{
			uri = url.toURI();
		}
		catch ( final URISyntaxException e )
		{
			throw new IllegalArgumentException( e.toString(), e );
		}
		return uri;
	}

	/**
	 * Convert {@link URI} to {@link URL}.
	 *
	 * @param uri URI to convert to URL.
	 *
	 * @return {@link URL}.
	 *
	 * @throws IllegalArgumentException if URL can not be converted.
	 */
	public static URL toURL( final URI uri )
	{
		try
		{
			return uri.toURL();
		}
		catch ( final MalformedURLException e )
		{
			throw new IllegalArgumentException( "Malformed: " + uri, e );
		}
	}

	/**
	 * Convert {@link String} to {@link URL}.
	 *
	 * @param str URL string to convert to URL.
	 *
	 * @return {@link URL}.
	 *
	 * @throws IllegalArgumentException if URL can not be converted.
	 */
	public static URL toURL( final String str )
	{
		try
		{
			return new URL( str );
		}
		catch ( final MalformedURLException e )
		{
			throw new IllegalArgumentException( "Malformed: " + str, e );
		}
	}

	/**
	 * Build URL from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 */
	public static URL buildUrl( final URL context, final String path, final Object... parameters )
	{
		return buildUrl( context, path, Arrays.asList( parameters ) );
	}

	/**
	 * Build URL from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 *
	 * @throws IllegalArgumentException if the path or parameters were
	 * malformed.
	 */
	public static URL buildUrl( final URL context, final String path, final List<?> parameters )
	{
		final String spec = buildSpec( path, parameters );
		try
		{
			return new URL( context, spec );
		}
		catch ( final MalformedURLException e )
		{
			throw new IllegalArgumentException( "Malformed: " + spec, e );
		}
	}

	/**
	 * Build URL from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 */
	public static URL buildUrl( final URI context, final String path, final Object... parameters )
	{
		return buildUrl( context, path, Arrays.asList( parameters ) );
	}

	/**
	 * Build URL from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 *
	 * @throws IllegalArgumentException if the path or parameters were
	 * malformed.
	 */
	public static URL buildUrl( final URI context, final String path, final List<?> parameters )
	{
		return toURL( buildUri( context, path, parameters ) );
	}

	/**
	 * Build URI from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 */
	public static URI buildUri( final URI context, final String path, final Object... parameters )
	{
		return buildUri( context, path, Arrays.asList( parameters ) );
	}

	/**
	 * Build URL from path and parameters.
	 *
	 * @param context    URI used to resolve the path.
	 * @param path       Path relative to context.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return URL that was built.
	 *
	 * @throws IllegalArgumentException if the path or parameters were
	 * malformed.
	 */
	public static URI buildUri( final URI context, final String path, final List<?> parameters )
	{
		return context.resolve( buildSpec( path, parameters ) );
	}

	/**
	 * Build string with path and query.
	 *
	 * @param path       Path to append query to.
	 * @param parameters Request parameters to include in URL.
	 *
	 * @return Combined string with path and query.
	 */
	public static String buildSpec( @NotNull final String path, @NotNull final List<?> parameters )
	{
		final String result;

		final int last = parameters.size() - 1;
		if ( last > 0 )
		{
			final StringBuilder sb = new StringBuilder();

			final int length = path.length();
			if ( length == 0 )
			{
				sb.append( '?' );
			}
			else
			{
				sb.append( path );
				final char lastChar = path.charAt( length - 1 );
				if ( ( lastChar != '?' ) && ( lastChar != '&' ) )
				{
					sb.append( ( path.indexOf( '?' ) < 0 ) ? '?' : '&' );
				}
			}

			for ( int cur = 0; cur < last; cur += 2 )
			{
				if ( cur > 0 )
				{
					sb.append( '&' );
				}

				sb.append( urlEncode( String.valueOf( parameters.get( cur ) ) ) );
				sb.append( '=' );
				sb.append( urlEncode( String.valueOf( parameters.get( cur + 1 ) ) ) );
			}

			result = sb.toString();
		}
		else
		{
			result = path;
		}

		return result;
	}

	/**
	 * Append parameter to a path string.
	 *
	 * @param path  Path string builder to append parameter to.
	 * @param name  Parameter name.
	 * @param value Parameter value.
	 *
	 * @return String path parameter appended.
	 */
	@NotNull
	public static String appendParameter( @NotNull final String path, @NotNull final String name, @NotNull final String value )
	{
		final String result;

		final int length = path.length();
		if ( length == 0 )
		{
			result = '?' + urlEncode( name ) + '=' + urlEncode( value );
		}
		else
		{
			final char lastChar = path.charAt( length - 1 );
			if ( ( lastChar == '?' ) || ( lastChar == '&' ) )
			{
				result = path + urlEncode( name ) + '=' + urlEncode( value );
			}
			else
			{
				result = path + ( ( path.indexOf( '?' ) < 0 ) ? '?' : '&' ) + name + '=' + urlEncode( value );
			}
		}

		return result;
	}

	/**
	 * Append parameter to a path string.
	 *
	 * @param builder Path string builder to append parameter to.
	 * @param name    Parameter name.
	 * @param value   Parameter value.
	 */
	public static void appendParameter( @NotNull final StringBuilder builder, @NotNull final String name, @NotNull final String value )
	{
		final int length = builder.length();
		if ( length == 0 )
		{
			builder.append( '?' );
		}
		else
		{
			final char lastChar = builder.charAt( length - 1 );
			if ( ( lastChar != '?' ) && ( lastChar != '&' ) )
			{
				builder.append( ( builder.indexOf( "?" ) < 0 ) ? '?' : '&' );
			}
		}

		builder.append( urlEncode( name ) );
		builder.append( '=' );
		builder.append( urlEncode( value ) );
	}

	/**
	 * Returns parameter value map from an URI string. If a parameter has multiple values, only the last value is returned.
	 *
	 * @param uri URI to get parameters from.
	 *
	 * @return Map with parameter values.
	 */
	@NotNull
	protected static Map<String, String> getParameterValueMapFromUri( @NotNull final String uri )
	{
		final Map<String, String> result;

		final int qmark = uri.indexOf( '?' );
		if ( qmark < 0 )
		{
			result = Collections.emptyMap();
		}
		else
		{
			final int hash = uri.indexOf( '#' );
			if ( ( hash < 0 ) || ( qmark < hash ) )
			{
				result = new LinkedHashMap<String, String>();

				final int start = qmark + 1;
				final int end = ( hash < 0 ) ? uri.length() : hash;

				int pos = start;
				while ( pos < end )
				{
					int endParam = uri.indexOf( '&', pos );
					if ( ( endParam < pos ) || ( endParam > end ) )
					{
						endParam = end;
					}

					final int equal = uri.indexOf( '=', pos );
					final boolean hasEqual = ( equal >= pos ) && ( equal < endParam );

					final int endName = hasEqual ? equal : endParam;
					if ( endName > pos ) // skip nameless parameters
					{
						final String name = urlDecode( uri.substring( pos, endName ) );
						final String value = hasEqual ? urlDecode( uri.substring( equal + 1, endParam ) ) : "";

						result.put( name, value );
					}

					pos = endParam + 1;
				}
			}
			else
			{
				result = Collections.emptyMap();
			}
		}

		return result;
	}

	/**
	 * Returns parameter value map from an URI string.
	 *
	 * @param uri URI to get parameters from.
	 *
	 * @return Map with parameter values.
	 */
	@NotNull
	protected static Map<String, List<String>> getParameterValuesMapFromUri( @NotNull final String uri )
	{
		final Map<String, List<String>> result;

		final int qmark = uri.indexOf( '?' );
		if ( qmark < 0 )
		{
			result = Collections.emptyMap();
		}
		else
		{
			final int hash = uri.indexOf( '#' );
			if ( ( hash < 0 ) || ( qmark < hash ) )
			{
				result = new LinkedHashMap<String, List<String>>();

				final int start = qmark + 1;
				final int end = ( hash < 0 ) ? uri.length() : hash;

				int pos = start;
				while ( pos < end )
				{
					int endParam = uri.indexOf( '&', pos );
					if ( ( endParam < pos ) || ( endParam > end ) )
					{
						endParam = end;
					}

					final int equal = uri.indexOf( '=', pos );
					final boolean hasEqual = ( equal >= pos ) && ( equal < endParam );

					final int endName = hasEqual ? equal : endParam;
					if ( endName > pos ) // skip nameless parameters
					{
						final String name = urlDecode( uri.substring( pos, endName ) );
						final String value = hasEqual ? urlDecode( uri.substring( equal + 1, endParam ) ) : "";

						List<String> values = result.get( name );
						if ( values == null )
						{
							result.put( name, Collections.singletonList( value ) );
						}
						else
						{
							if ( values.size() == 1 )
							{
								final String firstValue = values.get( 0 );
								values = new ArrayList<String>();
								values.add( firstValue );
								result.put( name, values );
							}

							values.add( value );
						}
					}

					pos = endParam + 1;
				}
			}
			else
			{
				result = Collections.emptyMap();
			}
		}

		return result;
	}

	/**
	 * URL-encode a string using UTF-8. Spaces are converted to '+', which is
	 * appropriate in HTML form data (including query string).
	 *
	 * @param string String to encode.
	 *
	 * @return URL-encoded string using UTF-8 character encoding.
	 */
	public static String urlEncode( final String string )
	{
		try
		{
			return URLEncoder.encode( string, "UTF-8" );
		}
		catch ( final UnsupportedEncodingException e )
		{
			throw new RuntimeException( e ); // never happens.
		}
	}

	/**
	 * URL-encode a string using UTF-8. Spaces are converted to '%20', which is
	 * appropriate in the path of a URL.
	 *
	 * @param string String to encode.
	 *
	 * @return URL-encoded string using UTF-8 character encoding.
	 */
	public static String urlEncodePath( final String string )
	{
		String result = urlEncode( string );
		result = result.replace( "+", "%20" );
		return result;
	}

	/**
	 * URL-decode a string using UTF-8.
	 *
	 * @param string String to decode.
	 *
	 * @return URL-decoded string using UTF-8 character encoding.
	 */
	public static String urlDecode( final String string )
	{
		try
		{
			return URLDecoder.decode( string, "UTF-8" );
		}
		catch ( final UnsupportedEncodingException e )
		{
			throw new RuntimeException( e ); // never happens.
		}
	}

	/**
	 * Get an {@link URL} that supports basic authentication without setting any
	 * global properties.
	 *
	 * @param url      Any {@link URL} (without authentication).
	 * @param username User name for authentication.
	 * @param password Password for authentication.
	 *
	 * @return {@link URL} with support for basic authentication.
	 */
	@NotNull
	public static URL getBasicAuthenticationURL( final URL url, final String username, final String password )
	{
		final URLStreamHandler urlStreamHandler = new URLStreamHandler()
		{
			@Override
			protected URLConnection openConnection( final URL ignored )
			throws IOException
			{
				final URLConnection result = url.openConnection();
				final String credentials = ( ( password != null ) ? username + ':' + password : username );
				//noinspection UnnecessaryFullyQualifiedName
				result.setRequestProperty( "Authorization", "Basic " + com.numdata.oss.Base64.encodeBase64( credentials.getBytes( "UTF-8" ) ) );
				return result;
			}

			@Override
			protected URLConnection openConnection( final URL ignored, final Proxy p )
			throws IOException
			{
				final URLConnection result = url.openConnection( p );
				final String credentials = ( ( password != null ) ? username + ':' + password : username );
				//noinspection UnnecessaryFullyQualifiedName
				result.setRequestProperty( "Authorization", "Basic " + com.numdata.oss.Base64.encodeBase64( credentials.getBytes( "UTF-8" ) ) );
				return result;
			}

			@Override
			protected int getDefaultPort()
			{
				return url.getDefaultPort();
			}

			@SuppressWarnings( "EqualsHashCodeCalledOnUrl" )
			@Override
			protected boolean equals( final URL ignored, final URL other )
			{
				return url.equals( other );
			}

			@SuppressWarnings( "EqualsHashCodeCalledOnUrl" )
			@Override
			protected int hashCode( final URL ignored )
			{
				return url.hashCode();
			}

			@Override
			protected String toExternalForm( final URL ignored )
			{
				return url.toExternalForm();
			}

			@Override
			protected boolean sameFile( final URL ignored, final URL other )
			{
				return url.sameFile( other );
			}

			@Override
			protected synchronized InetAddress getHostAddress( final URL u )
			{
				return super.getHostAddress( u );
			}
		};

		try
		{
			return new URL( url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), urlStreamHandler );
		}
		catch ( final MalformedURLException e )
		{
			throw new AssertionError( e ); // basically impossible....
		}
	}

	/**
	 * Download file with the given URL. If the download fails, it will be
	 * retried several times. If it still fails, an exception will be thrown
	 * including the server response (if available).
	 *
	 * @param url URL to download from.
	 *
	 * @return Downloaded content.
	 *
	 * @throws IOException if the download failed.
	 */
	public static byte[] downloadFromUrl( @NotNull final URL url )
	throws IOException
	{
		LOG.trace( "Download: " + url );
		for ( int tries = 1; ; tries++ )
		{
			final InputStream inputStream;
			final HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			try
			{
				urlConnection.setDoOutput( false );
				urlConnection.setDoInput( true );
				urlConnection.setUseCaches( false );
				inputStream = urlConnection.getInputStream();
			}
			catch ( final IOException e )
			{
				if ( tries == 3 )
				{
					LOG.warn( "Giving up download of '" + url + "' after 3 tries: " + e, e );
					String message = e.getMessage();
					final InputStream errorStream = urlConnection.getErrorStream();
					if ( errorStream != null )
					{
						try
						{
							message += "\nServer response:\n" + TextTools.loadText( errorStream );
						}
						catch ( final IOException ignored )
						{
						}
					}
					throw new IOException( message, e );
				}
				LOG.info( "Retry download '" + url + "' after exception: " + e, e );
				continue;
			}
			return DataStreamTools.readByteArray( inputStream );
		}
	}
}
