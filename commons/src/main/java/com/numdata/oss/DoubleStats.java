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
 * Calculates various statistics (count, minimum, average, maximum, total,
 * standard deviation) from a sequence of doubles.
 *
 * @author Gerrit Meinders
 */
public class DoubleStats
	implements Stats
{
	/**
	 * Number of values.
	 */
	private int _count = 0;

	/**
	 * Sum of values.
	 */
	private double _sum = 0.0;

	/**
	 * Minimum value.
	 */
	private double _min = Double.POSITIVE_INFINITY;

	/**
	 * Maximum value.
	 */
	private double _max = Double.NEGATIVE_INFINITY;

	/**
	 * Sum of squares of differences from the current mean.
	 */
	private double _m2 = 0;

	/**
	 * Constructs a new instance.
	 */
	public DoubleStats()
	{
	}

	/**
	 * Constructs a new instance and adds the specified value.
	 *
	 * @param value Value to add.
	 */
	public DoubleStats( final double value )
	{
		add( value );
	}

	@Override
	public void add( final Stats other )
	{
		if ( !( other instanceof DoubleStats ) )
		{
			throw new ClassCastException( "Can't cast " + other.getClass() + " to " + DoubleStats.class );
		}
		add( (DoubleStats)other );
	}

	/**
	 * Adds the given statistics.
	 *
	 * @param other Statistics to add.
	 */
	public void add( final DoubleStats other )
	{
		final int countA = getCount();
		final double averageA = getAverage();

		add( other._count, other._sum, other._min, other._max );

		final int countB = other.getCount();
		final double averageB = other.getAverage();

		if ( countA == 0 )
		{
			_m2 = other._m2;
		}
		else if ( countB != 0 )
		{
			// Parallel algorithm for calculating variance.
			// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
			final double delta = averageB - averageA;
			_m2 += other._m2 + delta * delta * countA * countB / ( countA + countB );
		}
	}

	/**
	 * Adds the given value.
	 *
	 * @param value Value to add.
	 */
	public void add( final double value )
	{
		final int previousCount = getCount();
		final double previousAverage = getAverage();

		add( 1, value, value, value );

		// Online algorithm for calculating variance.
		// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
		if ( previousCount > 0 )
		{
			_m2 = _m2 + ( value - previousAverage ) * ( value - getAverage() );
		}
	}

	/**
	 * Adds the given statistics.
	 *
	 * @param count Number of values.
	 * @param sum   Sum of values.
	 * @param min   Minimum value.
	 * @param max   Maximum value.
	 */
	private void add( final int count, final double sum, final double min, final double max )
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
		final String result;
		if ( _count == 0 )
		{
			result = "no data";
		}
		else if ( _count == 1 )
		{
			result = String.valueOf( _sum );
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "min " );
			sb.append( _min );
			sb.append( ", max " );
			sb.append( _max );
			sb.append( ", total " );
			sb.append( _sum );
			if ( _count > 1 )
			{
				sb.append( ", avg " );
				sb.append( getAverage() );
				if ( _count > 2 )
				{
					sb.append( ", stdev " );
					sb.append( getStandardDeviation() );
				}
			}
			result = sb.toString();
		}
		return result;
	}

	public int getCount()
	{
		return _count;
	}

	public double getSum()
	{
		return _sum;
	}

	public double getMinimum()
	{
		return _min;
	}

	public double getMaximum()
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
		return _sum / _count;
	}

	/**
	 * Returns the variance.
	 *
	 * @return Variance of added values.
	 */
	public double getVariance()
	{
		return _count < 2 ? Double.NaN : _m2 / ( _count - 1 );
	}

	/**
	 * Returns the standard deviation.
	 *
	 * @return Standard deviation of added values.
	 */
	public double getStandardDeviation()
	{
		return Math.sqrt( getVariance() );
	}
}
