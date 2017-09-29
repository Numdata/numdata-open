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
package com.numdata.oss.io;

import java.io.*;

/**
 * This writer redirects its output to a string buffer. The implementation is
 * exactly the same as {@code java.io.StringWriter}, but does allow specifying
 * an external target buffer.
 *
 * @author Peter S. Heijnen
 * @see java.io.StringWriter
 */
public final class StringBufferWriter
extends Writer
{
	/**
	 * Buffer used for output.
	 */
	private final StringBuffer _buffer;

	/**
	 * Construct writer that wnew StringBufferWriter.
	 *
	 * @param buffer Buffer to use for output.
	 */
	public StringBufferWriter( final StringBuffer buffer )
	{
		if ( buffer == null )
		{
			throw new RuntimeException( "buffe" );
		}

		_buffer = buffer;
		lock = buffer;
	}

	/**
	 * Close writer. Has no effect.
	 */
	public void close()
	{
	}

	/**
	 * Flush stream. Has no effect.
	 */
	public void flush()
	{
	}

	/**
	 * Get buffer used for output.
	 *
	 * @return Buffer used for output.
	 */
	public StringBuffer getBuffer()
	{
		return _buffer;
	}

	/**
	 * Get buffer content as a string.
	 *
	 * @return Buffer content as a string.
	 */
	public String toString()
	{
		return _buffer.toString();
	}

	/**
	 * Write single character.
	 *
	 * @param ch Character to write.
	 */
	public void write( final int ch )
	{
		_buffer.append( (char)ch );
	}

	/**
	 * Write a portion of an array of characters.
	 *
	 * @param chars  Array of characters.
	 * @param offset Offset in array from which to start writing.
	 * @param length Number of characters to write.
	 */
	public void write( final char[] chars, final int offset, final int length )
	{
		_buffer.append( chars, offset, length );
	}

	/**
	 * Write a string.
	 *
	 * @param str String to write.
	 */
	public void write( final String str )
	{
		_buffer.append( str );
	}

	/**
	 * Write a portion of a string.
	 *
	 * @param str String to be written
	 * @param off Offset from which to start writing characters
	 * @param len Number of characters to write
	 */
	public void write( final String str, final int off, final int len )
	{
		_buffer.append( str.substring( off, off + len ) );
	}
}
