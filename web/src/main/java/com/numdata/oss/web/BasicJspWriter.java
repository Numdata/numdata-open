/*
 * Copyright (c) 2009-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.web;

import java.io.*;
import javax.servlet.jsp.*;

/**
 * This class provides a basic {@link JspWriter} implementation on top of
 * another {@link Writer}.
 *
 * @author  Peter S. Heijnen
 */
@SuppressWarnings( "SynchronizeOnNonFinalField" )
public class BasicJspWriter
	extends JspWriter
{
	/**
	 * Encapsulated {@link JspWriter}.
	 */
	private final Writer _out;

	/**
	 * Constructor to create a {@link JspWriter}.
	 *
	 * @param   out     Output writer.
	 */
	public BasicJspWriter( final Writer out )
	{
		super( ( out instanceof JspWriter ) ? ((JspWriter)out).getBufferSize() : 0 , ( out instanceof JspWriter ) && ((JspWriter)out).isAutoFlush() );

		_out = out;
	}

	/**
	 * Constructor to create a {@link JspWriter}.
	 *
	 * @param   out             Output writer.
	 * @param   bufferSize      Size of the buffer to be used by the JspWriter.
	 * @param   autoFlush       Whether the JspWriter should be autoflushing.
	 */
	public BasicJspWriter( final Writer out, final int bufferSize, final boolean autoFlush )
	{
		super( bufferSize, autoFlush );
		_out = out;
	}

	@Override
	public void clear()
		throws IOException
	{
		if ( _out instanceof JspWriter )
		{
			((JspWriter)_out).clear();
		}
	}

	@Override
	public void clearBuffer()
		throws IOException
	{
		if ( _out instanceof JspWriter )
		{
			((JspWriter)_out).clearBuffer();
		}
	}

	@Override
	public void close()
		throws IOException
	{
		_out.close();
	}

	@Override
	public void flush()
		throws IOException
	{
		_out.flush();
	}

	@Override
	public int getRemaining()
	{
		return ( _out instanceof JspWriter ) ? ((JspWriter)_out).getRemaining() : bufferSize;
	}

	@Override
	public boolean isAutoFlush()
	{
		return ( _out instanceof JspWriter ) ? ((JspWriter)_out).isAutoFlush() : autoFlush;
	}

	@Override
	public void newLine()
		throws IOException
	{
		write( (int)'\n' );
	}

	@Override
	public void print( final boolean b )
		throws IOException
	{
		write( b ? "true" : "false" );
	}

	@Override
	public void print( final char[] s )
		throws IOException
	{
		write( s );
	}

	@Override
	public void print( final char c )
		throws IOException
	{
		write( (int)c );
	}

	@Override
	public void print( final double d )
		throws IOException
	{
		write( String.valueOf( d ) );
	}

	@Override
	public void print( final float f )
		throws IOException
	{
		write( String.valueOf( f ) );
	}

	@Override
	public void print( final int i )
		throws IOException
	{
		write( String.valueOf( i ) );
	}

	@Override
	public void print( final Object obj )
		throws IOException
	{
		write( String.valueOf( obj ) );
	}

	@Override
	public void print( final String s )
		throws IOException
	{
		write( ( s == null ) ? "null" : s );
	}

	@Override
	public void print( final long l )
		throws IOException
	{
		write( String.valueOf( l ) );
	}

	@Override
	public void println()
		throws IOException
	{
		newLine();
	}

	@Override
	public void println( final boolean b )
		throws IOException
	{
		synchronized ( lock )
		{
			print( b );
			println();
		}
	}

	@Override
	public void println( final char[] s )
		throws IOException
	{
		synchronized ( lock )
		{
			print( s );
			println();
		}
	}

	@Override
	public void println( final char c )
		throws IOException
	{
		synchronized ( lock )
		{
			write( (int)c );
			println();
		}
	}

	@Override
	public void println( final double d )
		throws IOException
	{
		synchronized ( lock )
		{
			print( d );
			println();
		}
	}

	@Override
	public void println( final float f )
		throws IOException
	{
		synchronized ( lock )
		{
			print( f );
			println();
		}
	}

	@Override
	public void println( final int i )
		throws IOException
	{
		synchronized ( lock )
		{
			print( i );
			println();
		}
	}

	@Override
	public void println( final Object obj )
		throws IOException
	{
		synchronized ( lock )
		{
			print( obj );
			println();
		}
	}

	@Override
	public void println( final String s )
		throws IOException
	{
		synchronized ( lock )
		{
			print( s );
			println();
		}
	}

	@Override
	public void println( final long l )
		throws IOException
	{
		synchronized ( lock )
		{
			print( l );
			println();
		}
	}

	@Override
	public void write( final char[] cbuf , final int off , final int len )
		throws IOException
	{
		_out.write( cbuf , off , len );

		if ( !( _out instanceof JspWriter ) && autoFlush )
		{
			_out.flush();
		}
	}

	@Override
	public void write( final int c )
		throws IOException
	{
		_out.write( c );

		if ( !( _out instanceof JspWriter ) && autoFlush )
		{
			_out.flush();
		}
	}
}
