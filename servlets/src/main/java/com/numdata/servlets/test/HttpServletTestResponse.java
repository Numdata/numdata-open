/*
 * Copyright (c) 2008-2019, Numdata BV, The Netherlands.
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
package com.numdata.servlets.test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Dummy servlet response implementation for tests.
 *
 * @author G. Meinders
 */
public class HttpServletTestResponse
	implements HttpServletResponse
{
	private final List<String>        _headerFieldKeys;
	private final List<String>        _headerFieldValues;
	private final List<Cookie>        _cookies;
	private       ServletOutputStream _outputStream;

	/**
	 * Redirect set by {@link #sendRedirect(String)}.
	 */
	private String _redirect;

	/**
	 * Error set by {@link #sendError}.
	 */
	private String _error;

	/**
	 * Forward set by calling {@link RequestDispatcher#forward}.
	 */
	private String _forward;

	/**
	 * Include set by calling {@link RequestDispatcher#include}.
	 */
	private String _include;

	/**
	 * Constructs a new instance.
	 */
	public HttpServletTestResponse()
	{
		_headerFieldKeys   = new ArrayList<String>();
		_headerFieldValues = new ArrayList<String>();
		_cookies           = new ArrayList<Cookie>();
		_outputStream      = null;

		addHeader( null , "HTTP/1.1 200 OK" );
	}

	public String getCharacterEncoding()
	{
		throw new AssertionError( "not implemented" );
	}

	public String getContentType()
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletOutputStream getOutputStream()
		throws IOException
	{
		return _outputStream;
	}

	public PrintWriter getWriter()
		throws IOException
	{
		throw new AssertionError( "not implemented" );
	}

	public void setCharacterEncoding( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setContentLength( final int i )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setContentLengthLong( final long length )
	{
		throw new AssertionError( "not implemented" );

	}

	public void setContentType( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setBufferSize( final int i )
	{
		throw new AssertionError( "not implemented" );
	}

	public int getBufferSize()
	{
		throw new AssertionError( "not implemented" );
	}

	public void flushBuffer()
		throws IOException
	{
		throw new AssertionError( "not implemented" );
	}

	public void resetBuffer()
	{
		throw new AssertionError( "not implemented" );
	}

	public boolean isCommitted()
	{
		throw new AssertionError( "not implemented" );
	}

	public void reset()
	{
		throw new AssertionError( "not implemented" );
	}

	public void setLocale( final Locale locale )
	{
		throw new AssertionError( "not implemented" );
	}

	public Locale getLocale()
	{
		throw new AssertionError( "not implemented" );
	}

	public void addCookie( final Cookie cookie )
	{
		_cookies.add( cookie );
//		addHeader( "Set-Cookie" ,  );
	}

	public List<Cookie> getCookies()
	{
		return Collections.unmodifiableList( _cookies );
	}

	public boolean containsHeader( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	@SuppressWarnings( "MethodNamesDifferingOnlyByCase" )
	public String encodeURL( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	@SuppressWarnings( "MethodNamesDifferingOnlyByCase" )
	public String encodeRedirectURL( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	@SuppressWarnings( "MethodNamesDifferingOnlyByCase" )
	public String encodeUrl( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	@SuppressWarnings( "MethodNamesDifferingOnlyByCase" )
	public String encodeRedirectUrl( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	public void sendError( final int i , final String s )
		throws IOException
	{
		_error = "HTTP " + i + ": " + s;
	}

	public void sendError( final int i )
		throws IOException
	{
		_error = "HTTP " + i;
	}

	public String getError()
	{
		return _error;
	}

	public void sendRedirect( final String s )
		throws IOException
	{
		_redirect = s;
	}

	public String getRedirect()
	{
		return _redirect;
	}

	public void setDateHeader( final String s , final long l )
	{
		throw new AssertionError( "not implemented" );
	}

	public void addDateHeader( final String s , final long l )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setHeader( final String key , final String value )
	{
		removeHeader( key );
		addHeader( key , value );
	}

	public void removeHeader( final String key )
	{
		final List<String> keys   = _headerFieldKeys;
		final List<String> values = _headerFieldValues;

		/*
		 * Remove any matching key/value pairs, starting at the end of
		 * both lists for efficiency.
		 */
		for ( final ListIterator<String> i = keys  .listIterator( keys.size() ) ,
										 j = values.listIterator( keys.size() ) ; i.hasPrevious() ; )
		{
			j.previous();
			if ( key.equals( i.previous() ) )
			{
				i.remove();
				j.remove();
			}
		}
	}

	public void addHeader( final String key , final String value )
	{
		_headerFieldKeys.add( value );
		_headerFieldValues.add( value );
	}

	public void setIntHeader( final String s , final int i )
	{
		throw new AssertionError( "not implemented" );
	}

	public void addIntHeader( final String s , final int i )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setStatus( final int statusCode )
	{
		throw new AssertionError( "not implemented" );
	}

	public void setStatus( final int statusCode , final String statusMessage )
	{
		throw new AssertionError( "not implemented" );
	}

	public int getStatus()
	{
		throw new AssertionError( "not implemented" );
	}

	public String getHeader( final String name )
	{
		throw new AssertionError( "not implemented" );
	}

	public Collection<String> getHeaders( final String name )
	{
		throw new AssertionError( "not implemented" );
	}

	public Collection<String> getHeaderNames()
	{
		throw new AssertionError( "not implemented" );
	}

	public void setOutputStream( final OutputStream outputStream )
	{
		_outputStream = new ServletOutputStreamImpl( outputStream );
	}

	public String getHeaderFieldKey( final int header )
	{
		return ( _headerFieldKeys.size() > header ) ? _headerFieldKeys.get( header ) : null;
	}

	public String getHeaderField( final int header )
	{
		return ( _headerFieldValues.size() > header ) ? _headerFieldValues.get( header ) : null;
	}

	public String getForward()
	{
		return _forward;
	}

	void setForward( final String forward )
	{
		_forward = forward;
	}

	public String getInclude()
	{
		return _include;
	}

	void setInclude( final String include )
	{
		_include = include;
	}

	private static class ServletOutputStreamImpl
		extends ServletOutputStream
	{
		private final OutputStream _out;

		ServletOutputStreamImpl( final OutputStream out )
		{
			_out = out;
		}

		@Override
		public void write( final int b )
			throws IOException
		{
			_out.write( b );
		}

		@Override
		public void write( final byte[] b , final int off , final int len )
			throws IOException
		{
			super.write( b , off , len );
		}

		@Override
		public void write( final byte[] b )
			throws IOException
		{
			super.write( b );
		}

		public boolean isReady()
		{
			throw new AssertionError( "not implemented" );
		}

		@Override
		public void setWriteListener( final WriteListener listener )
		{
			throw new AssertionError( "not implemented" );
		}
	}
}
