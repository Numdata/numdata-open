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
package com.numdata.oss.io;

import java.io.*;

import org.jetbrains.annotations.*;

/**
 * Input stream that counts the number of bytes read from it.
 *
 * @author G. Meinders
 */
public class CountingInputStream
extends FilterInputStream
{
	/**
	 * Number of bytes read from the stream.
	 */
	private long _bytesRead = 0L;

	/**
	 * Number of bytes read when the stream was last {@link #mark}ed.
	 */
	private long _mark = 0L;

	/**
	 * Constructs a new input stream.
	 *
	 * @param inputStream Underlying input stream, or {@code null}.
	 */
	public CountingInputStream( final InputStream inputStream )
	{
		super( inputStream );
	}

	/**
	 * Resets the byte counter to {@code 0}.
	 */
	public void resetCounter()
	{
		_bytesRead = 0L;
	}

	/**
	 * Returns the number of bytes read from the stream.
	 *
	 * @return Number of bytes read.
	 */
	public long getBytesRead()
	{
		return _bytesRead;
	}

	@Override
	public int read()
	throws IOException
	{
		final int result = super.read();
		if ( result != -1 )
		{
			_bytesRead++;
		}
		return result;
	}

	@Override
	public int read( @NotNull final byte[] b )
	throws IOException
	{
		final int result = in.read( b );
		if ( result != -1 )
		{
			_bytesRead += (long)result;
		}
		return result;
	}

	@Override
	public int read( @NotNull final byte[] b, final int off, final int len )
	throws IOException
	{
		final int result = in.read( b, off, len );
		if ( result != -1 )
		{
			_bytesRead += (long)result;
		}
		return result;
	}

	@Override
	public long skip( final long n )
	throws IOException
	{
		final long result = super.skip( n );
		_bytesRead += result;
		return result;
	}

	@Override
	public void mark( final int readlimit )
	{
		super.mark( readlimit );
		_mark = _bytesRead;
	}

	@Override
	public void reset()
	throws IOException
	{
		super.reset();
		_bytesRead = _mark;
	}
}
