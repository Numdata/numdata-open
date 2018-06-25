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
 * This class represent a generic CNC machine.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public class FlexiblePrintWriter
extends PrintWriter
{
	/**
	 * If {@code true}, flush the output buffer if {@link #println}, {@link
	 * #printf}, or {@link #format} is called.
	 */
	private final boolean _autoFlush;

	/**
	 * Line separator to use; {@code null} to use system line separator.
	 */
	@Nullable
	private String _lineSeparator;

	/**
	 * Creates a new writer.
	 *
	 * @param out Character stream.
	 */
	public FlexiblePrintWriter( @NotNull final Appendable out )
	{
		this( out, false, null );
	}

	/**
	 * Creates a new writer using the system line separator.
	 *
	 * @param out       Character stream.
	 * @param autoFlush If {@code true}, flush the output buffer if {@link
	 *                  #println}, {@link * #printf}, or {@link #format} is
	 *                  called.
	 */
	public FlexiblePrintWriter( @NotNull final Appendable out, final boolean autoFlush )
	{
		this( out, autoFlush, null );
	}

	/**
	 * Creates a new writer.
	 *
	 * @param out           Character stream.
	 * @param autoFlush     If {@code true}, flush the output buffer if {@link
	 *                      #println}, {@link * #printf}, or {@link #format} is
	 *                      called.
	 * @param lineSeparator Line separator to use; {@code null} to use system
	 *                      line separator.
	 */
	public FlexiblePrintWriter( @NotNull final Appendable out, final boolean autoFlush, @Nullable final String lineSeparator )
	{
		super( ( out instanceof Writer ) ? (Writer)out : new AppendableWriter( out ), autoFlush );
		_autoFlush = autoFlush;
		_lineSeparator = lineSeparator;
	}

	public boolean isAutoFlush()
	{
		return _autoFlush;
	}

	@Nullable
	public String getLineSeparator()
	{
		return _lineSeparator;
	}

	public void setLineSeparator( @Nullable final String lineSeparator )
	{
		_lineSeparator = lineSeparator;
	}

	@Override
	public void println()
	{
		if ( _lineSeparator != null )
		{
			write( _lineSeparator );
			if ( isAutoFlush() )
			{
				flush();
			}
		}
		else
		{
			super.println();
		}
	}
}
