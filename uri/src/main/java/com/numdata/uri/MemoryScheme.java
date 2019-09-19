/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
package com.numdata.uri;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;

/**
 * This class defines a very simple scheme that can be used to store objects
 * referenced by URL's in memory. Basically, any URL can be stored as long as
 * it has the {@link #NAME} scheme.
 *
 * @author  Peter S. Heijnen
 */
public class MemoryScheme
{
	/**
	 * Content stored using this scheme. Content is stored in
	 * {@link ByteArrayOutputStream} instances, so they can be used as
	 * read/write buffers without paying much attention to input/ouput streams.
	 */
	private static final Map<Object,ByteArrayOutputStream> _content = new HashMap<Object,ByteArrayOutputStream>();

	/**
	 * Name of this scheme.
	 */
	public static final String NAME = "memory";

	/**
	 * Counter for {@link #getUniquePrefix} method.
	 */
	private static int prefixCount = 1;

//	static
//	{
//		try
//		{
//			URL.setURLStreamHandlerFactory( new HandlerFactory() );
//		}
//		catch ( Error e )
//		{
//			System.out.println( MemoryScheme.class + ": " + e );
//		}
//	}

	/**
	 * Test if URL is a valid {@link MemoryScheme} URL.
	 *
	 * @param   url     URL to be tested.
	 *
	 * @return  {@code true} if URL is for {@link MemoryScheme}.
	 *          {@code false} otherwise.
	 */
	public static boolean isValid( final URL url )
	{
		return ( ( url != null ) && NAME.equals( url.getProtocol() ) );
	}

	/**
	 * Test if URI is a valid {@link MemoryScheme} URI.
	 *
	 * @param   uri     URI to be tested.
	 *
	 * @return  {@code true} if URI is for {@link MemoryScheme}.
	 *          {@code false} otherwise.
	 */
	public static boolean isValid( final String uri )
	{
		final String name = NAME;
		return ( ( uri != null ) && ( uri.length() > name.length() ) && ( uri.charAt( name.length() ) == ':'  ) && uri.startsWith( name ) );
	}

	/**
	 * Test if URI is a valid {@link MemoryScheme} URI.
	 *
	 * @param   uri     URI to be tested.
	 *
	 * @return  {@code true} if URI is for {@link MemoryScheme}.
	 *          {@code false} otherwise.
	 */
	public static boolean isValid( final URI uri )
	{
		return ( ( uri != null ) && NAME.equals( uri.getScheme() ) );
	}

	/**
	 * Get input stream for the object referenced by the specified URL.
	 *
	 * @param   url     URL to object in memory.
	 *
	 * @return  Byte array with data for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URL is specified.
	 */
	public static byte[] getData( final URL url )
		throws FileNotFoundException
	{
		if ( url == null )
		{
			throw new NullPointerException( "url" );
		}

		return getData( URI.create( url.toString() ) );
	}

	/**
	 * Get input stream for the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @return  Byte array with data for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static byte[] getData( final String uri )
		throws FileNotFoundException
	{
		if ( uri == null )
		{
			throw new NullPointerException( "uri" );
		}

		return getData( URI.create( uri ) );
	}

	/**
	 * Get input stream for the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @return  Byte array with data for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static byte[] getData( final URI uri )
		throws FileNotFoundException
	{
		final Object key = getContentKey( uri );

		final ByteArrayOutputStream buffer;
		synchronized ( _content )
		{
			buffer = _content.get( key );
			if ( buffer == null )
			{
				throw new FileNotFoundException( uri.toString() );
			}
		}

		return buffer.toByteArray();
	}

	/**
	 * Get input stream for the object referenced by the specified URL.
	 *
	 * @param   url     URL to object in memory.
	 *
	 * @return  {@link InputStream} for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URL is specified.
	 */
	public static InputStream getInputStream( final URL url )
		throws FileNotFoundException
	{
		return new ByteArrayInputStream( getData( url ) );
	}

	/**
	 * Get input stream for the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @return  {@link InputStream} for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static InputStream getInputStream( final String uri )
		throws FileNotFoundException
	{
		return new ByteArrayInputStream( getData( uri ) );
	}

	/**
	 * Get input stream for the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @return  {@link InputStream} for object.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static InputStream getInputStream( final URI uri )
		throws FileNotFoundException
	{
		return new ByteArrayInputStream( getData( uri ) );
	}

	/**
	 * Get output stream for the object referenced by the specified URL. This
	 * method also allows appending data to an existing object.
	 *
	 * @param   url     URL to object in memory.
	 * @param   append  Whether to append to or replace existing object data.
	 *
	 * @return  {@link OutputStream} for object.
	 *
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URL is specified.
	 */
	public static OutputStream getOutputStream( final URL url , final boolean append )
	{
		if ( url == null )
		{
			throw new NullPointerException( "url" );
		}

		return getOutputStream( URI.create( url.toString() ) , append );
	}

	/**
	 * Get output stream for the object referenced by the specified URI. This
	 * method also allows appending data to an existing object.
	 *
	 * @param   uri     URI to object in memory.
	 * @param   append  Whether to append to or replace existing object data.
	 *
	 * @return  {@link OutputStream} for object.
	 *
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static OutputStream getOutputStream( final String uri , final boolean append )
	{
		if ( uri == null )
		{
			throw new NullPointerException( "uri" );
		}

		return getOutputStream( URI.create( uri ) , append );
	}

	/**
	 * Get output stream for the object referenced by the specified URI. This
	 * method also allows appending data to an existing object.
	 *
	 * @param   uri     URI to object in memory.
	 * @param   append  Whether to append to or replace existing object data.
	 *
	 * @return  {@link OutputStream} for object.
	 *
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static OutputStream getOutputStream( final URI uri , final boolean append )
	{
		final Object key = getContentKey( uri );
		ByteArrayOutputStream result;

		final Map<Object,ByteArrayOutputStream> content = _content;
		synchronized ( content )
		{
			result = append ? content.get( key ) : null;
			if ( result == null )
			{
				result = new ByteArrayOutputStream();
				content.put( key , result );
			}
		}

		return result;
	}

	/**
	 * Remove the object referenced by the specified URL.
	 *
	 * @param   url     URL to object in memory.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URL is specified.
	 */
	public static void remove( final URL url )
		throws FileNotFoundException
	{
		if ( url == null )
		{
			throw new NullPointerException( "url" );
		}

		remove( URI.create( url.toString() ) );
	}

	/**
	 * Remove the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static void remove( final String uri )
		throws FileNotFoundException
	{
		if ( uri == null )
		{
			throw new NullPointerException( "uri" );
		}

		remove( URI.create( uri ) );
	}

	/**
	 * Remove the object referenced by the specified URI.
	 *
	 * @param   uri     URI to object in memory.
	 *
	 * @throws  FileNotFoundException if the specified object was not found.
	 * @throws  NullPointerException if any argument is {@code null}.
	 * @throws  IllegalArgumentException if an invalid URI is specified.
	 */
	public static void remove( final URI uri )
		throws FileNotFoundException
	{
		final Object key = getContentKey( uri );
		synchronized ( _content )
		{
			if ( _content.remove( key ) == null )
			{
				throw new FileNotFoundException( uri.toString() );
			}
		}
	}

	/**
	 * Get unique URI prefix. The prefix can optionally be suffixed for clarity.
	 *
	 * @param   suffix  Suffix to append to prefix (optional).
	 *
	 * @return  URI prefix (never {@code null}, always a folder prefix,
	 *          i.e. ending with a slash).
	 */
	public static String getUniquePrefix( final String suffix )
	{
		final StringBuilder result = new StringBuilder();
		result.append( NAME );
		result.append( ":/$" );
		result.append( prefixCount++ );
		result.append( "$/" );

		if ( TextTools.isNonEmpty( suffix ) )
		{
			final int l = suffix.length();
			result.append( suffix , ( suffix.charAt( 0 ) == '/' ) ? 1 : 0 , l );
			if ( suffix.charAt( l - 1 ) != '/' )
			{
				result.append( '/' );
			}
		}

		return result.toString();
	}

	/**
	 * This internal helper-method generates a key to use in the content map
	 * for the specified URI.
	 *
	 * @param   uri     URI to get content key for.
	 *
	 * @return  Content key for URI.
	 */
	private static Object getContentKey( final URI uri )
	{
		if ( uri == null )
		{
			throw new NullPointerException( "uri" );
		}

		if ( !NAME.equals( uri.getScheme() ) )
		{
			throw new IllegalArgumentException( "scheme != " + NAME + ": " + uri );
		}

		final URI normalizedURI = uri.normalize();
		return normalizedURI.toString();
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private MemoryScheme()
	{
	}

	/**
	 * {@link URLStreamHandlerFactory} that can create protocal handlers for
	 * the memory scheme.
	 */
	private static class HandlerFactory
		implements URLStreamHandlerFactory
	{
		public URLStreamHandler createURLStreamHandler( final String protocol )
		{
			final URLStreamHandler result;

			if ( NAME.equalsIgnoreCase( protocol ) )
			{
				result = new Handler();
			}
			else
			{
				result = null;
			}

			return result;
		}
	}

	/**
	 * {@link URLStreamHandler} that provides connections to memory URLs.
	 */
	private static class Handler
		extends URLStreamHandler
	{
		protected URLConnection openConnection( final URL url )
			throws IOException
		{
			return new Connection( url );
		}
	}

	/**
	 * URL connection for memory objects.
	 */
	private static class Connection
		extends URLConnection
	{
		/**
		 * Constructs a URL connection to the specified URL. A connection to
		 * the object referenced by the URL is not created.
		 *
		 * @param   url     URL to create connection for.
		 */
		private Connection( final URL url )
		{
			super( url );
		}

		public void connect()
			throws IOException
		{
		}

		public InputStream getInputStream()
			throws IOException
		{
			return MemoryScheme.getInputStream( url );
		}

		public OutputStream getOutputStream()
			throws IOException
		{
			return MemoryScheme.getOutputStream( url , false );
		}
	}
}
