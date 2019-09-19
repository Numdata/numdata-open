/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
 * Concrete implementation of {@link Trio} interface.
 *
 * @author  Peter S. Heijnen
 */
public class BasicTrio<T1,T2,T3>
	implements Trio<T1,T2,T3>
{
	/**
	 * First value.
	 */
	private T1 _value1;

	/**
	 * Second value.
	 */
	private T2 _value2;

	/**
	 * Third value.
	 */
	private T3 _value3;

	/**
	 * Default constructor.
	 */
	public BasicTrio()
	{
		this( null , null , null );
	}

	/**
	 * Construct {@link Trio}.
	 *
	 * @param   value1  Initial first value.
	 * @param   value2  Initial second value.
	 * @param   value3  Initial third value.
	 */
	public BasicTrio( final T1 value1 , final T2 value2 , final T3 value3 )
	{
		_value1 = value1;
		_value2 = value2;
		_value3 = value3;
	}

	public T1 getValue1()
	{
		return _value1;
	}

	/**
	 * Set first value.
	 *
	 * @param   value   Value to set.
	 */
	public void setValue1( final T1 value )
	{
		_value1 = value;
	}

	public T2 getValue2()
	{
		return _value2;
	}

	/**
	 * Set second value.
	 *
	 * @param   value   Value to set.
	 */
	public void setValue2( final T2 value )
	{
		_value2 = value;
	}

	public T3 getValue3()
	{
		return _value3;
	}

	/**
	 * Set third value.
	 *
	 * @param   value   Value to set.
	 */
	public void setValue3( final T3 value )
	{
		_value3 = value;
	}

	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj instanceof Trio<?,?,?> )
		{
			final Trio<?,?,?> other = (Trio<?,?,?>)obj;
			result = ( ( _value1 != null ) ? _value1.equals( other.getValue1() ) : ( other.getValue1() == null ) ) &&
			         ( ( _value2 != null ) ? _value2.equals( other.getValue2() ) : ( other.getValue2() == null ) ) &&
			         ( ( _value3 != null ) ? _value3.equals( other.getValue3() ) : ( other.getValue3() == null ) );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return ( ( _value1 != null ) ? _value1.hashCode() : 0 ) ^
		       ( ( _value2 != null ) ? _value2.hashCode() : 0 ) ^
		       ( ( _value3 != null ) ? _value3.hashCode() : 0 );
	}

	public Iterator iterator()
	{
		return new Iterator( this );
	}

	@Override
	public String toString()
	{
		return "BasicTrio[value1=" + getValue1() + ", value2=" + getValue2() + ", value3=" + getValue3() + ']';
	}

	/**
	 * Iterator for {@link Trio}.
	 */
	public static class Iterator
		implements java.util.Iterator<Object>
	{
		/**
		 * Trio being iterated.
		 */
		private final Trio<?,?,?> _trio;

		/**
		 * Next element index (0=first).
		 */
		int _index = 0;

		/**
		 * Construct iterator.
		 *
		 * @param   trio   Trio to iterate.
		 */
		public Iterator( final Trio<?,?,?> trio )
		{
			_trio = trio;
		}

		public boolean hasNext()
		{
			return ( _index < 3 );
		}

		public Object next()
		{
			final Object result;

			final Trio<?,?,?> trio  = _trio;
			final int         index = _index;

			switch ( index )
			{
				case 0 : result = trio.getValue1(); break;
				case 1 : result = trio.getValue2(); break;
				case 2 : result = trio.getValue3(); break;
				default : throw new NoSuchElementException( String.valueOf( index ) );
			}

			_index = index + 1;
			return result;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
