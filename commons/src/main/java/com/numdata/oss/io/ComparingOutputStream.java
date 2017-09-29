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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

/**
 * Output stream that compares the written to data read from an input stream.
 * Writing fails, if the data written does not match the data from the input
 * stream or if the input stream ends prematurely or provides more data when
 * the output stream is closed.
 *
 * @author  Peter S. Heijnen
 */
public class ComparingOutputStream
	extends OutputStream
{
	/**
	 * Position in stream.
	 */
	private int _position;

	/**
	 * Stream from which the expected data is read.
	 */
	@NotNull
	private final InputStream _reference;

	/**
	 * Constructs a new output stream.
	 *
	 * @param   reference   Stream to read expected data from.
	 */
	public ComparingOutputStream( @NotNull final InputStream reference )
	{
		_position = 0;
		_reference = reference;
	}

	@Override
	public void write( final int data )
		throws IOException
	{
		final int expected = _reference.read();
		if ( expected < 0 )
		{
			throw new EOFException( "Unexpected byte " + data + " / 0x" + Integer.toHexString( data ) + " written while at end of reference stream at position " + _position );
		}

		if ( ( data & 0xFF ) != expected )
		{
			throw new IOException( "Unexpected byte " + data + " / 0x" + Integer.toHexString( data ) + " written while expecting " + expected + " / 0x" + Integer.toHexString( expected ) + " at position " + _position + " in the stream" );
		}

		_position++;
	}

	@Override
	public void close()
		throws IOException
	{
		final int expected = _reference.read();
		if ( expected >= 0 )
		{
			throw new IOException( "Closed stream while reference stream still data (" + expected + " / 0x" + Integer.toHexString( expected ) + ") at position " + _position + " in the stream" );
		}

		_reference.close();
	}
}
