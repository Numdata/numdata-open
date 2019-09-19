/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * This class is a Writer implementation that is able to indent.
 *
 * @author  Peter S. Heijnen
 */
public class IndentingWriter
	extends FilterWriter
{
	/**
	 * Flag to indicate that output is a the beginning of a new line.
	 */
	private boolean _beginningOfLine = true;

	/**
	 * Current indent depth.
	 */
	private int _currentIndent = 0;

	/**
	 * Indenting string.
	 */
	private String _indentString;

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param   out     Writer object to provide the underlying stream.
	 */
	public IndentingWriter( final Writer out )
	{
		this( out , "    " );
	}

	/**
	 * Constructor Writer that is able to indent.
	 *
	 * @param   out     Writer object to provide the underlying stream.
	 */
	public IndentingWriter( final Writer out , final String indentString )
	{
		super( out );

		if ( indentString == null )
			throw new NullPointerException( "indentString" );

		_indentString = indentString;
	}

	/**
	 * Before every output is written, this method is called to check if
	 * indenting should be performed (_beginningOfLine). If so, sufficient
	 * spaces a prepended to match the indent size.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	protected void checkWrite()
		throws IOException
	{
		if ( _beginningOfLine)
		{
			_beginningOfLine = false;

			final String indentString = _indentString;

			for ( int i = _currentIndent ; i > 0 ; i-- )
				write( indentString );
		}
	}

	/**
	 * Increase indenting by one step.
	 */
	public void indentIn()
	{
		_currentIndent++;
	}

	/**
	 * Decrease indenting by one step.
	 */
	public void indentOut()
	{
		if ( _currentIndent > 0 )
			_currentIndent--;
	}

	/**
	 * Generates a newline.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void newLine()
		throws IOException
	{
		super.write( (int)'\n' );
		_beginningOfLine = true;
	}

	public void write( final char[] cbuf , final int off , final int len )
		throws IOException
	{
		if ( len > 0 )
			checkWrite();

		super.write( cbuf , off , len );
	}

	public void write( final int c )
		throws IOException
	{
		checkWrite();

		super.write( c );
	}

	public void write(final String s, final int off, final int len)
		throws IOException
	{
		if ( len > 0 )
			checkWrite();

		super.write( s , off , len );
	}

	/**
	 * Writes a newline.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void writeln()
		throws IOException
	{
		newLine();
	}

	/**
	 * Writes a String to the output followed by a newline.
	 *
	 * @param   s   String to write to the output.
	 *
	 * @throws  IOException if output cannot be written.
	 */
	public void writeln( final String s )
		throws IOException
	{
		write( s );
		newLine();
	}
}
