/*
 * Copyright (c) 2004-2017, Numdata BV, The Netherlands.
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
 * Reads data from 7-bit input and translates it back to 8-bits.
 * <pre>
 * NOTE: This class does not extend FilterInputStream although it works
 *       like one. The reason is that the methods in FilterInputStream
 *       forward everything to the encapsulated input stream, returning
 *       incorrect results.
 * </pre>
 *
 * @author Peter S. Heijnen
 * @see SevenBitOutputStream
 */
public final class SevenBitInputStream
extends InputStream
{
	/**
	 * The input stream to be filtered.
	 */
	private final InputStream _in;

	/**
	 * Buffer for bits left from previous read.
	 */
	private int _buffer;

	/**
	 * Number of bits left in the buffer.
	 */
	private int _bitsInBuffer;

	/**
	 * Creates a {@code SevenBitIntputStream} that encapsulates the specified
	 * input stream.
	 *
	 * @param in Input stream.
	 */
	public SevenBitInputStream( final InputStream in )
	{
		_in = in;
		_buffer = 0;
		_bitsInBuffer = 0;
	}

	/**
	 * Returns the number of bytes that can be read from this input stream
	 * without blocking.
	 *
	 * This method simply performs {@code in.available(n)} and returns the
	 * result.
	 *
	 * @return Number of bytes that can be read from the input stream without
	 * blocking.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public synchronized int available()
	throws IOException
	{
		final int i = _in.available();
		return ( i <= 0 ) ? i : ( ( _bitsInBuffer + i * 7 ) >> 3 );
	}

	/**
	 * Reads the next byte of data from the input stream. The value byte is
	 * returned as an {@code int} in the range {@code 0} to {@code 255}. If no
	 * byte is available because the end of the stream has been reached, the
	 * value {@code -1} is returned. This method blocks until input data is
	 * available, the end of the stream is detected, or an exception is thrown.
	 *
	 * A subclass must provide an implementation of this method.
	 *
	 * @return Next byte of data, or {@code -1} if the end of the stream is
	 * reached.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public synchronized int read()
	throws IOException
	{
		/*
		 * Read until we have at least 8 bits in the buffer.
		 */
		while ( _bitsInBuffer < 8 )
		{
			final int i = _in.read();
			if ( i < 0 )
			{
				break;
			}

			_buffer |= ( i & 0x7F ) << _bitsInBuffer;
			_bitsInBuffer += 7;
		}

		/*
		 * Get 8 bits from the buffer and return it.
		 */
		final int result;
		if ( _bitsInBuffer < 8 )
		{
			result = -1;
		}
		else
		{
			result = ( _buffer & 0xFF );
			_buffer >>= 8;
			_bitsInBuffer -= 8;
		}
		return result;
	}

}
