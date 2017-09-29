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
 * Writes data out as 7-bit data.
 *
 * @author Peter S. Heijnen
 * @see SevenBitInputStream
 */
public final class SevenBitOutputStream
extends OutputStream
{
	/**
	 * The output stream to be filtered.
	 */
	private final OutputStream _out;

	/**
	 * Buffer for bits left from previous write.
	 */
	private int _buffer;

	/**
	 * Number of bits left in the buffer.
	 */
	private int _bitsInBuffer;

	/**
	 * Creates a new SevenBitOutputStream.
	 *
	 * @param out Output stream.
	 */
	public SevenBitOutputStream( final OutputStream out )
	{
		_out = out;
		_buffer = 0;
		_bitsInBuffer = 0;
	}

	/**
	 * Closes this output stream and releases any system resources associated
	 * with the stream.
	 *
	 * The {@code close} method of {@code FilterOutputStream} calls its {@code
	 * flush} method, and then calls the {@code close} method of its underlying
	 * output stream.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public synchronized void close()
	throws IOException
	{
		/*
		 * If there are still bits left in the buffer, write them.
		 */
		if ( _bitsInBuffer > 0 )
		{
			_out.write( _buffer );
			_bitsInBuffer = -1;
		}

		super.close();
	}

	/**
	 * Writes a byte. Will block until the byte is actually written.
	 *
	 * @param b Byte to write out.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public synchronized void write( final int b )
	throws IOException
	{
		if ( _bitsInBuffer < 0 )
		{
			throw new IOException( "can't write data after flush" );
		}

		/*
		 * Add byte to the buffer
		 */
		_buffer |= ( b & 0xFF ) << _bitsInBuffer;

		/*
		 * Write out lower 7 bits and shift buffer.
		 */
		final int i = _buffer & 0x7F;
		_out.write( i );
		//System.err.println( "sent: " + i );
		_buffer = _buffer >> 7;

		/*
		 * As a result, we have actually one more bit in the buffer.
		 * When we have another 7 bits, write them out and reset the
		 * counter.
		 */
		if ( ++_bitsInBuffer == 7 )
		{
			_out.write( _buffer );
			//System.err.println( "sent: " + _buffer );
			_buffer = 0;
			_bitsInBuffer = 0;
		}
	}

}
