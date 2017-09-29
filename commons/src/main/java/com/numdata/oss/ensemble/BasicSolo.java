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
package com.numdata.oss.ensemble;

import java.util.NoSuchElementException;

/**
 * Concrete implementation of {@link Solo} interface.
 *
 * @author  Peter S. Heijnen
 */
public class BasicSolo<T>
	implements Solo<T>
{
	/**
	 * Value.
	 */
	private T _value;

	/**
	 * Default constructor.
	 */
	public BasicSolo()
	{
		this( null );
	}

	/**
	 * Construct {@link Solo}.
	 *
	 * @param   value   Initial value.
	 */
	public BasicSolo( final T value )
	{
		_value = value;
	}

	public T getValue()
	{
		return _value;
	}

	/**
	 * Set value.
	 *
	 * @param   value   Value to set.
	 */
	public void setValue( final T value )
	{
		_value = value;
	}

	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj instanceof Solo<?> )
		{
			final Solo<?> other = (Solo<?>)obj;
			result = ( _value != null ) ? _value.equals( other.getValue() ) : ( other.getValue() == null );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return ( _value != null ) ? _value.hashCode() : 0;
	}

	public Iterator<T> iterator()
	{
		return new Iterator<T>( this );
	}

	@Override
	public String toString()
	{
		return "BasicSolo[value=" + getValue() + ']';
	}

	/**
	 * Iterator for {@link Solo}.
	 */
	public static class Iterator<T>
		implements java.util.Iterator<T>
	{
		/**
		 * Solo being iterated.
		 */
		private final Solo<T> _solo;

		/**
		 * Next element index (0=first).
		 */
		int _index = 0;

		/**
		 * Construct iterator.
		 *
		 * @param   solo   Solo to iterate.
		 */
		public Iterator( final Solo<T> solo )
		{
			_solo = solo;
		}

		public boolean hasNext()
		{
			return ( _index < 1 );
		}

		public T next()
		{
			final Solo<T> solo  = _solo;
			final int     index = _index;

			if ( index != 0 )
				throw new NoSuchElementException( String.valueOf( index ) );

			_index = index + 1;

			return solo.getValue();
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
