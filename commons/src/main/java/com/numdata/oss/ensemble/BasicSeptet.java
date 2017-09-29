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
 * Concrete implementation of {@link Septet} interface.
 *
 * @author  Peter S. Heijnen
 */
public class BasicSeptet<T1,T2,T3,T4,T5,T6,T7>
	implements Septet<T1,T2,T3,T4,T5,T6,T7>
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
	 * Sixth value.
	 */
	private T6 _value6;

	/**
	 * Seventh value.
	 */
	private T7 _value7;

	/**
	 * Default constructor.
	 */
	public BasicSeptet()
	{
		this( null , null , null , null , null , null , null );
	}

	/**
	 * Construct {@link Septet}.
	 *
	 * @param   value1  Initial first value.
	 * @param   value2  Initial second value.
	 * @param   value3  Initial third value.
	 * @param   value4  Initial fourth value.
	 * @param   value5  Initial fifth value.
	 * @param   value6  Initial sixth value.
	 * @param   value7  Initial seventh value.
	 */
	public BasicSeptet( final T1 value1 , final T2 value2 , final T3 value3 , final T4 value4 , final T5 value5 , final T6 value6 , final T7 value7 )
	{
		_value1 = value1;
		_value2 = value2;
		_value3 = value3;
		_value4 = value4;
		_value5 = value5;
		_value6 = value6;
		_value7 = value7;
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

	public T6 getValue6()
	{
		return _value6;
	}

	/**
	 * Set sixth value.
	 *
	 * @param   value   Sixth value.
	 */
	public void setValue6( final T6 value )
	{
		_value6 = value;
	}

	public T7 getValue7()
	{
		return _value7;
	}

	/**
	 * Set seventh value.
	 *
	 * @param   value   Seventh value.
	 */
	public void setValue7( final T7 value )
	{
		_value7 = value;
	}

	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj instanceof Septet<?,?,?,?,?,?,?> )
		{
			final Septet<?,?,?,?,?,?,?> other = (Septet<?,?,?,?,?,?,?>)obj;
			result = ( ( _value1 != null ) ? _value1.equals( other.getValue1() ) : ( other.getValue1() == null ) ) &&
			         ( ( _value2 != null ) ? _value2.equals( other.getValue2() ) : ( other.getValue2() == null ) ) &&
			         ( ( _value3 != null ) ? _value3.equals( other.getValue3() ) : ( other.getValue3() == null ) ) &&
			         ( ( _value4 != null ) ? _value4.equals( other.getValue4() ) : ( other.getValue4() == null ) ) &&
			         ( ( _value5 != null ) ? _value5.equals( other.getValue5() ) : ( other.getValue5() == null ) ) &&
			         ( ( _value6 != null ) ? _value6.equals( other.getValue6() ) : ( other.getValue6() == null ) ) &&
			         ( ( _value7 != null ) ? _value7.equals( other.getValue7() ) : ( other.getValue7() == null ) );
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
		       ( ( _value5 != null ) ? _value5.hashCode() : 0 ) ^
		       ( ( _value6 != null ) ? _value6.hashCode() : 0 ) ^
		       ( ( _value7 != null ) ? _value7.hashCode() : 0 );
	}

	public Iterator iterator()
	{
		return new Iterator( this );
	}

	@Override
	public String toString()
	{
		return "BasicNonet[value1=" + getValue1() + ", value2=" + getValue2() + ", value3=" + getValue3() + ", value4=" + getValue4() + ", value5=" + getValue5() + ", value6=" + getValue6() + ", value7=" + getValue7() + ']';
	}

	/**
	 * Iterator for {@link Septet}.
	 */
	public static class Iterator
		implements java.util.Iterator<Object>
	{
		/**
		 * Septet being iterated.
		 */
		private final Septet<?,?,?,?,?,?,?> _septet;

		/**
		 * Next element index (0=first).
		 */
		int _index = 0;

		/**
		 * Construct iterator.
		 *
		 * @param   septet   Septet to iterate.
		 */
		public Iterator( final Septet<?,?,?,?,?,?,?> septet )
		{
			_septet = septet;
		}

		public boolean hasNext()
		{
			return ( _index < 7 );
		}

		public Object next()
		{
			final Object result;

			final Septet<?,?,?,?,?,?,?> septet = _septet;
			final int                   index  = _index;

			switch ( index )
			{
				case 0 : result = septet.getValue1(); break;
				case 1 : result = septet.getValue2(); break;
				case 2 : result = septet.getValue3(); break;
				case 3 : result = septet.getValue4(); break;
				case 4 : result = septet.getValue5(); break;
				case 5 : result = septet.getValue6(); break;
				case 6 : result = septet.getValue7(); break;
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
