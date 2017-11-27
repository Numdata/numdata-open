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
package com.numdata.oss;

/**
 * Calculates various statistics (count, minimum, average, maximum, total) from
 * a sequence of integers.
 *
 * @author Gerrit Meinders
 */
public class IntStats
implements Stats
{
	/**
	 * Number of values.
	 */
	private int _count = 0;

	/**
	 * Sum of values.
	 */
	private int _sum = 0;

	/**
	 * Minimum value.
	 */
	private int _min = Integer.MAX_VALUE;

	/**
	 * Maximum value.
	 */
	private int _max = Integer.MIN_VALUE;

	/**
	 * Constructs a new instance.
	 */
	public IntStats()
	{
	}

	/**
	 * Constructs a new instance and adds the specified value.
	 *
	 * @param value Value to add.
	 */
	public IntStats( final int value )
	{
		add( value );
	}

	@Override
	public void add( final Stats other )
	{
		if ( !( other instanceof IntStats ) )
		{
			throw new ClassCastException( "Can't cast " + other.getClass() + " to " + IntStats.class );
		}
		add( (IntStats)other );
	}

	/**
	 * Adds the given statistics.
	 *
	 * @param other Statistics to add.
	 */
	public void add( final IntStats other )
	{
		add( other._count, other._sum, other._min, other._max );
	}

	/**
	 * Adds the given value.
	 *
	 * @param value Value to add.
	 */
	public void add( final int value )
	{
		add( 1, value, value, value );
	}

	/**
	 * Adds the given statistics.
	 *
	 * @param count Number of values.
	 * @param sum   Sum of values.
	 * @param min   Minimum value.
	 * @param max   Maximum value.
	 */
	private void add( final int count, final int sum, final int min, final int max )
	{
		_count += count;
		_sum += sum;
		if ( min < _min )
		{
			_min = min;
		}
		if ( max > _max )
		{
			_max = max;
		}
	}

	@Override
	public String toString()
	{
		return _count == 0 ? "no data" : _count == 1 ? String.valueOf( _sum ) : "min " + _min + ", avg " + Math.round( 1000.0 * getAverage() ) / 1000.0 + ", max " + _max + ", total " + _sum;
	}

	public int getCount()
	{
		return _count;
	}

	public int getSum()
	{
		return _sum;
	}

	public int getMinimum()
	{
		return _min;
	}

	public int getMaximum()
	{
		return _max;
	}

	/**
	 * Returns the average.
	 *
	 * @return Average of added values.
	 */
	public double getAverage()
	{
		return (double)_sum / _count;
	}
}
