/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss;

/**
 * Implementation of {@link CharSequence} using a character array.
 *
 * @author Peter S. Heijnen
 */
public class CharArray
implements CharSequence
{
	/**
	 * Character array.
	 */
	private final char[] _chars;

	/**
	 * Start index in character array (inclusive).
	 */
	private final int _start;

	/**
	 * End index in character array (exclusive).
	 */
	private final int _end;

	/**
	 * Create new character array buffer.
	 *
	 * @param capacity Capacity of buffer.
	 */
	public CharArray( final int capacity )
	{
		this( new char[ capacity ] );
	}

	/**
	 * Create new character array buffer.
	 *
	 * @param chars Target array.
	 */
	public CharArray( final char[] chars )
	{
		this( chars, 0, chars.length );
	}

	/**
	 * Create new character array buffer.
	 *
	 * @param chars Target array.
	 * @param start Start index in character array (inclusive).
	 * @param end   End index in character array (exclusive).
	 */
	public CharArray( final char[] chars, final int start, final int end )
	{
		if ( ( start < 0 ) || ( start > end ) || ( end > chars.length ) )
		{
			throw new IndexOutOfBoundsException( "start=" + start + ", end=" + end + ", length=" + chars.length );
		}

		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_chars = chars;
		_start = start;
		_end = end;
	}

	/**
	 * Get character array.
	 *
	 * @return Character array.
	 */
	public char[] getChars()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _chars;
	}

	@Override
	public int length()
	{
		return _end - _start;
	}

	@Override
	public char charAt( final int index )
	{
		if ( ( index < 0 ) || ( index >= length() ) )
		{
			throw new IndexOutOfBoundsException( "index=" + index + ", length=" + length() );

		}
		return _chars[ _start + index ];
	}

	@Override
	public CharSequence subSequence( final int start, final int end )
	{
		if ( ( start < 0 ) || ( start > end ) || ( end > length() ) )
		{
			throw new IndexOutOfBoundsException( "start=" + start + ", end=" + end + ", length=" + length() );
		}

		return new CharArray( _chars, _start + start, _start + end );
	}
}
