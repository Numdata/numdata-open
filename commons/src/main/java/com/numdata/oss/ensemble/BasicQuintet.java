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
 * Concrete implementation of {@link Quintet} interface.
 *
 * @author  Peter S. Heijnen
 */
public class BasicQuintet<T1,T2,T3,T4,T5>
	implements Quintet<T1,T2,T3,T4,T5>
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
	 * Fourth value.
	 */
	private T4 _value4;

	/**
	 * Fifth value.
	 */
	private T5 _value5;

	/**
	 * Default constructor.
	 */
	public BasicQuintet()
	{
		this( null , null , null , null , null );
	}

	/**
	 * Construct {@link Quintet}.
	 *
	 * @param   value1  Initial first value.
	 * @param   value2  Initial second value.
	 * @param   value3  Initial third value.
	 * @param   value4  Initial fourth value.
	 * @param   value5  Initial fifth value.
	 */
	public BasicQuintet( final T1 value1 , final T2 value2 , final T3 value3 , final T4 value4 , final T5 value5 )
	{
		_value1 = value1;
		_value2 = value2;
		_value3 = value3;
		_value4 = value4;
		_value5 = value5;
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

	public T4 getValue4()
	{
		return _value4;
	}

	/**
	 * Set fourth value.
	 *
	 * @param   value   Value to set.
	 */
	public void setValue4( final T4 value )
	{
		_value4 = value;
	}

	public T5 getValue5()
	{
		return _value5;
	}

	/**
	 * Set fifth value.
	 *
	 * @param   value   Fifth value.
	 */
	public void setValue5( final T5 value )
	{
		_value5 = value;
	}

	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj instanceof Quintet<?,?,?,?,?> )
		{
			final Quintet<?,?,?,?,?> other = (Quintet<?,?,?,?,?>)obj;
			result = ( ( _value1 != null ) ? _value1.equals( other.getValue1() ) : ( other.getValue1() == null ) ) &&
			         ( ( _value2 != null ) ? _value2.equals( other.getValue2() ) : ( other.getValue2() == null ) ) &&
			         ( ( _value3 != null ) ? _value3.equals( other.getValue3() ) : ( other.getValue3() == null ) ) &&
			         ( ( _value4 != null ) ? _value4.equals( other.getValue4() ) : ( other.getValue4() == null ) ) &&
			         ( ( _value5 != null ) ? _value5.equals( other.getValue5() ) : ( other.getValue5() == null ) );
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
		       ( ( _value3 != null ) ? _value3.hashCode() : 0 ) ^
		       ( ( _value4 != null ) ? _value4.hashCode() : 0 ) ^
		       ( ( _value5 != null ) ? _value5.hashCode() : 0 );
	}

	public Iterator iterator()
	{
		return new Iterator( this );
	}

	@Override
	public String toString()
	{
		return "BasicQuintet[value1=" + getValue1() + ", value2=" + getValue2() + ", value3=" + getValue3() + ", value4=" + getValue4() + ", value5=" + getValue5() + ']';
	}

	/**
	 * Iterator for {@link Quintet}.
	 */
	public static class Iterator
		implements java.util.Iterator<Object>
	{
		/**
		 * Quintet being iterated.
		 */
		private final Quintet<?,?,?,?,?> _quintet;

		/**
		 * Next element index (0=first).
		 */
		int _index = 0;

		/**
		 * Construct iterator.
		 *
		 * @param   quintet     Quintet to iterate.
		 */
		public Iterator( final Quintet<?,?,?,?,?> quintet )
		{
			_quintet = quintet;
		}

		public boolean hasNext()
		{
			return ( _index < 5 );
		}

		public Object next()
		{
			final Object result;

			final Quintet<?,?,?,?,?> quintet = _quintet;
			final int                index   = _index;

			switch ( index )
			{
				case 0 : result = quintet.getValue1(); break;
				case 1 : result = quintet.getValue2(); break;
				case 2 : result = quintet.getValue3(); break;
				case 3 : result = quintet.getValue4(); break;
				case 4 : result = quintet.getValue5(); break;
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
