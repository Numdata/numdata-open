/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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
 * A very basic {@link Writer} on top of an {@link Appendable}.
 *
 * @author Peter S. Heijnen
 */
public class AppendableWriter
extends Writer
{
	/**
	 * Target {@link Appendable} to write to.
	 */
	@NotNull
	private final Appendable _appendable;

	/**
	 * {@link StringBuilder} if {@link Appendable} is {@link StringBuilder}.
	 */
	@Nullable
	private final StringBuilder _stringBuilder;

	/**
	 * {@link Flushable} if {@link Appendable} is {@link Flushable}.
	 */
	@Nullable
	private final Flushable _flushable;

	/**
	 * {@link Closeable} if {@link Appendable} is {@link Closeable}.
	 */
	@Nullable
	private final Closeable _closeable;

	/**
	 * Construct writer.
	 *
	 * @param appendable Target {@link Appendable} to write to.
	 */
	public AppendableWriter( @NotNull final Appendable appendable )
	{
		_appendable = appendable;
		_stringBuilder = ( appendable instanceof StringBuilder ) ? (StringBuilder)appendable : null;
		_flushable = ( appendable instanceof Flushable ) ? (Flushable)appendable : null;
		_closeable = ( appendable instanceof Closeable ) ? (Closeable)appendable : null;
	}

	@NotNull
	public Appendable getAppendable()
	{
		return _appendable;
	}

	@Nullable
	public Flushable getFlushable()
	{
		return _flushable;
	}

	@Nullable
	public Closeable getCloseable()
	{
		return _closeable;
	}

	@Override
	public void write( final int i )
	throws IOException
	{
		getAppendable().append( (char)i );
	}

	@Override
	public void write( @NotNull final String str, final int off, final int len )
	throws IOException
	{
		_appendable.append( str, off, len );
	}

	@Override
	public void write( final char[] cbuf, final int off, final int len )
	throws IOException
	{
		final StringBuilder stringBuilder = _stringBuilder;
		if ( stringBuilder != null )
		{
			stringBuilder.append( cbuf, off, len );
		}
		else
		{
			final Appendable appendable = getAppendable();
			for ( int i = 0; i < len; i++ )
			{
				appendable.append( cbuf[ off + i ] );
			}
		}
	}

	@Override
	public Writer append( final char c )
	throws IOException
	{
		getAppendable().append( c );
		return this;
	}

	@Override
	public Writer append( final CharSequence csq )
	throws IOException
	{
		getAppendable().append( csq );
		return this;
	}

	@Override
	public Writer append( final CharSequence csq, final int start, final int end )
	throws IOException
	{
		getAppendable().append( csq, start, end );
		return this;
	}

	@Override
	public void flush()
	throws IOException
	{
		final Flushable flushable = getFlushable();
		if ( flushable != null )
		{
			flushable.flush();
		}
	}

	@Override
	public void close()
	throws IOException
	{
		final Closeable closeable = getCloseable();
		if ( closeable != null )
		{
			closeable.close();
		}
	}
}
