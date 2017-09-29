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
package com.numdata.oss.web;

import java.io.*;
import javax.servlet.jsp.*;

/**
 * This class is a special JspWriter implementation that is able to indent the
 * written text. It is used by various HTML generation methods.
 *
 * @author Peter S. Heijnen
 */
public class IndentingJspWriter
extends BasicJspWriter
{
	/**
	 * Flag to indicate that the writer is positioned at the beginning of a new
	 * line.
	 */
	private boolean _beginningOfLine;

	/**
	 * Current indent depth.
	 */
	private int _currentIndent;

	/**
	 * Indent step size.
	 */
	private final int _indentStep;

	/**
	 * Character used for indent.
	 */
	private final char _indentChar;

	/**
	 * Constructor to create a HtmlWriter.
	 *
	 * @param out           Output writer.
	 * @param indentChar    Character used for indent.
	 * @param indentStep    Indenting step size.
	 * @param initialIndent Initial indent depth.
	 * @param autoFlush     Whether the JspWriter should be auto-flushing.
	 */
	private IndentingJspWriter( final Writer out, final char indentChar, final int indentStep, final int initialIndent, final boolean autoFlush )
	{
		super( out, ( out instanceof JspWriter ) ? ( (JspWriter)out ).getBufferSize() : 0, autoFlush || ( ( out instanceof JspWriter ) && ( (JspWriter)out ).isAutoFlush() ) );

		if ( indentStep < 0 )
		{
			throw new IllegalArgumentException( "indentStep: " + indentStep );
		}

		_beginningOfLine = true;
		_indentStep = indentStep;
		_currentIndent = ( initialIndent > 0 ) ? initialIndent * indentStep : 0;
		_indentChar = indentChar;
	}

	/**
	 * Create an <c{@code dentingJspWriter} instance from the specified writer.
	 * If the writer was already an {@code IndentingJspWriter} instance, it is
	 * returned unchanged. Otherwise, construct a new wrapper with the specified
	 * indent properties.
	 *
	 * @param out           Output writer.
	 * @param indentStep    Indenting step size.
	 * @param initialIndent Initial indent depth.
	 *
	 * @return IndentingJspWriter instance (possibly wrapped).
	 */
	public static IndentingJspWriter create( final Writer out, final int indentStep, final int initialIndent )
	{
		return create( out, ' ', indentStep, initialIndent, false );
	}

	/**
	 * Create an <c{@code dentingJspWriter} instance from the specified writer.
	 * If the writer was already an {@code IndentingJspWriter} instance, it is
	 * returned unchanged. Otherwise, construct a new wrapper with the specified
	 * indent properties.
	 *
	 * @param out           Output writer.
	 * @param indentChar    Character used for indent.
	 * @param indentStep    Indenting step size.
	 * @param initialIndent Initial indent depth.
	 * @param autoFlush     Whether the JspWriter should be auto-flushing.
	 *
	 * @return IndentingJspWriter instance (possibly wrapped).
	 */
	public static IndentingJspWriter create( final Writer out, final char indentChar, final int indentStep, final int initialIndent, final boolean autoFlush )
	{
		//noinspection IOResourceOpenedButNotSafelyClosed
		return ( out instanceof IndentingJspWriter ) ? (IndentingJspWriter)out : new IndentingJspWriter( out, indentChar, indentStep, initialIndent, autoFlush );
	}

	/**
	 * Increase indentation depth for the specified Writer (it must be an
	 * IndentingJspWriter instance to make this work).
	 *
	 * @param out Writer used for output.
	 */
	public static void indentIn( final Writer out )
	{
		if ( out instanceof IndentingJspWriter )
		{
			( (IndentingJspWriter)out ).indentIn();
		}
	}

	/**
	 * Decrease indentation depth for the specified Writer (it must be an
	 * IndentingJspWriter instance to make this work).
	 *
	 * @param out Writer used for output.
	 */
	public static void indentOut( final Writer out )
	{
		if ( out instanceof IndentingJspWriter )
		{
			( (IndentingJspWriter)out ).indentOut();
		}
	}

	/**
	 * Get current indenting depth.
	 *
	 * @return Current indenting depth.
	 */
	public int getIndentDepth()
	{
		return _currentIndent / _indentStep;
	}

	/**
	 * Increase indenting by one step.
	 */
	public void indentIn()
	{
		_currentIndent += _indentStep;
	}

	/**
	 * Decrease indenting by one step.
	 */
	public void indentOut()
	{
		if ( ( _currentIndent -= _indentStep ) < 0 )
		{
			_currentIndent = 0;
		}
	}

	/**
	 * Set current indentation depth.
	 *
	 * @param depth Indentation depth to use.
	 */
	public void setIndentDepth( final int depth )
	{
		_currentIndent = ( depth >= 0 ? depth * _indentStep : 0 );
	}

	@Override
	public void write( final char[] cbuf, final int off, final int len )
	throws IOException
	{
		final int end = off + len;
		int start = off;

		for ( int pos = start; pos < end; pos++ )
		{
			if ( cbuf[ pos ] == '\n' )
			{
				if ( start < pos )
				{
					writeIndentIfNecessary();
					super.write( cbuf, start, pos - start );
				}
				newLine();
				start = pos + 1;
			}
		}

		if ( start < end )
		{
			writeIndentIfNecessary();
			super.write( cbuf, start, end - start );
		}
	}

	@Override
	public void write( final int i )
	throws IOException
	{
		if ( i == (int)'\n' )
		{
			_beginningOfLine = true;
		}
		else
		{
			writeIndentIfNecessary();
		}
		super.write( i );
	}

	/**
	 * This writes the indent at the beginning of each line.
	 *
	 * @throws IOException if output cannot be written.
	 */
	protected void writeIndentIfNecessary()
	throws IOException
	{
		if ( _beginningOfLine )
		{
			for ( int i = _currentIndent; i > 0; i-- )
			{
				super.write( (int)_indentChar );
			}

			_beginningOfLine = false;
		}
	}
}
