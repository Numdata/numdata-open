/*
 * Copyright (c) 2007-2020, Numdata BV, The Netherlands.
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

import org.jetbrains.annotations.*;

/**
 * This class provides tools for various math problems.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "FinalClass" )
public final class MathTools
{
	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	public static int mod( final int dividend, final int divisor )
	{
		final int tmp = dividend % divisor;
		return ( ( tmp != 0 ) && ( ( divisor < 0 ) != ( tmp < 0 ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	public static float mod( final float dividend, final float divisor )
	{
		final float tmp = dividend % divisor;
		return ( ( tmp != 0.0f ) && ( ( divisor < 0.0f ) != ( tmp < 0.0f ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Perform modulo operation whose result has the same sign as the divisor.
	 * This is different from the default Java implementation, which has the
	 * same sign as the dividend.
	 *
	 * @param dividend Dividend.
	 * @param divisor  Divisor.
	 *
	 * @return Result of modulo. Sign of result is same as the divisor.
	 */
	public static double mod( final double dividend, final double divisor )
	{
		final double tmp = dividend % divisor;
		return ( ( tmp != 0.0 ) && ( ( divisor < 0.0 ) != ( tmp < 0.0 ) ) ) ? tmp + divisor : tmp;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code 0} if the values are within a +/- 0.001 tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	public static int significantCompare( final float value1, final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta < -0.001f ) ? -1 : ( delta > 0.001f ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code 0} if the values are within the given tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	public static int significantCompare( final float value1, final float value2, final float epsilon )
	{
		final float delta = value1 - value2;
		return ( delta < -epsilon ) ? -1 : ( delta > epsilon ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code 0} if the values are within a +/- 0.001 tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	public static int significantCompare( final double value1, final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta < -0.001 ) ? -1 : ( delta > 0.001 ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are significantly different or 'almost'
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code 0} if the values are within the given tolerance of each
	 * other; {@code -1} if {@code value1} is significantly less than {@code
	 * value2}; {@code 1} if {@code value1} is significantly greater than
	 * {@code value2};
	 */
	public static int significantCompare( final double value1, final double value2, final double epsilon )
	{
		final double delta = value1 - value2;
		return ( delta < -epsilon ) ? -1 : ( delta > epsilon ) ? 1 : 0;
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} if the values are within a +/- 0.001 tolerance of
	 * each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final float value1, final float value2 )
	{
		final float delta = value1 - value2;
		return ( delta <= 0.001f ) && ( delta >= -0.001f );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} if the values are within a +/- 0.001 tolerance of
	 * each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final double value1, final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta <= 0.001 ) && ( delta >= -0.001 );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} if the values are within the specified tolerance of
	 * each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final float value1, final float value2, final float epsilon )
	{
		final float delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} if the values are within the specified tolerance of
	 * each other; {@code false} otherwise.
	 */
	public static boolean almostEqual( final double value1, final double value2, final double epsilon )
	{
		final double delta = value1 - value2;
		return ( delta <= epsilon ) && ( delta >= -epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost equal
	 * (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is less than or within a +/- 0.001
	 * tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final float value1, final float value2 )
	{
		return ( ( value1 - value2 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is less than the second operand or almost equal
	 * (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is less than or within a +/- 0.001
	 * tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final double value1, final double value2 )
	{
		return ( ( value1 - value2 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is less than the second operand or almost equal
	 * (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is less than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final float value1, final float value2, final float epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is less than the second operand or almost equal
	 * (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is less than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean lessOrAlmostEqual( final double value1, final double value2, final double epsilon )
	{
		return ( ( value1 - value2 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is greater than or within a +/-
	 * 0.001 tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final float value1, final float value2 )
	{
		return ( ( value2 - value1 ) <= 0.001f );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is greater than or within a +/-
	 * 0.001 tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final double value1, final double value2 )
	{
		return ( ( value2 - value1 ) <= 0.001 );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is greater than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final float value1, final float value2, final float epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is greater than or within the
	 * specified tolerance of {@code value2}; {@code false} otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final double value1, final double value2, final double epsilon )
	{
		return ( ( value2 - value1 ) <= epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least the {@code 0.001} less
	 * than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final float value1, final float value2 )
	{
		return ( ( value2 - value1 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least the {@code 0.001} less
	 * than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final double value1, final double value2 )
	{
		return ( ( value2 - value1 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance less than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final float value1, final float value2, final float epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance less than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyLessThan( final double value1, final double value2, final double epsilon )
	{
		return ( ( value2 - value1 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second
	 * operand (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least the {@code 0.001}
	 * greater than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final float value1, final float value2 )
	{
		return ( ( value1 - value2 ) > 0.001f );
	}

	/**
	 * Test if the first operand is significantly greater than the second
	 * operand (the difference between them exceeds a 0.001 tolerance).
	 *
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 *
	 * @return {@code true} is {@code value1} is at least the {@code 0.001}
	 * greater than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final double value1, final double value2 )
	{
		return ( ( value1 - value2 ) > 0.001 );
	}

	/**
	 * Test if the first operand is significantly greater than the second
	 * operand (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance greater than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final float value1, final float value2, final float epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
	}

	/**
	 * Test if the first operand is significantly greater than the second
	 * operand (the difference between them exceeds the specified tolerance).
	 *
	 * @param value1  First value to compare.
	 * @param value2  Second value to compare.
	 * @param epsilon Tolerance (always a positive number).
	 *
	 * @return {@code true} is {@code value1} is at least the specified
	 * tolerance greater than {@code value2}; {@code false} otherwise.
	 */
	public static boolean significantlyGreaterThan( final double value1, final double value2, final double epsilon )
	{
		return ( ( value1 - value2 ) > epsilon );
	}

	/**
	 * Returns the distance between the given ranges. The ranges, as well as
	 * their respective end-points may be specified in any order.
	 *
	 * If the ranges are disjoint, the result is positive. For ranges that
	 * share a single value, the result is zero. If the ranges overlap, the
	 * result is negative.
	 *
	 * If one range encloses the other or if both ranges are equal, the
	 * result is {@code NaN}. Also, if any parameter is {@code NaN}, the result
	 * is undefined.
	 *
	 * @param x1 First end-point of the first range.
	 * @param x2 Second end-point of the first range.
	 * @param y1 First end-point of the second range.
	 * @param y2 Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	public static float rangeDistance( final float x1, final float x2, final float y1, final float y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xMin <= xMax
		 *  - yMin <= yMax
		 *  - xMin <= yMin
		 */

		float xmin;
		float xmax;
		final float ymin;
		final float ymax;

		if ( x1 <= x2 )
		{
			xmin = x1;
			xmax = x2;
		}
		else
		{
			xmin = x2;
			xmax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xmin <= y1 )
			{
				ymin = y1;
				ymax = y2;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y1;
				xmax = y2;
			}
		}
		else
		{
			if ( xmin <= y2 )
			{
				ymin = y2;
				ymax = y1;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y2;
				xmax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final float result;
		if ( ( xmax < ymin ) || ( xmax < ymax ) && ( xmin < ymin ) )
		{
			result = ymin - xmax;
		}
		else
		{
			result = Float.NaN;
		}

		return result;
	}

	/**
	 * Returns the distance between the given ranges. The ranges, as well as
	 * their respective end-points may be specified in any order.
	 *
	 * If the ranges are disjoint, the result is positive. For ranges that
	 * share a single value, the result is zero. If the ranges overlap, the
	 * result is negative.
	 *
	 * If one range encloses the other or if both ranges are equal, the
	 * result is {@code NaN}. Also, if any parameter is {@code NaN}, the result
	 * is undefined.
	 *
	 * @param x1 First end-point of the first range.
	 * @param x2 Second end-point of the first range.
	 * @param y1 First end-point of the second range.
	 * @param y2 Second end-point of the second range.
	 *
	 * @return Distance between the ranges, or {@code NaN}.
	 */
	public static double rangeDistance( final double x1, final double x2, final double y1, final double y2 )
	{
		/*
		 * Sort input values, such that:
		 *  - xMin <= xMax
		 *  - yMin <= yMax
		 *  - xMin <= yMin
		 */

		double xmin;
		double xmax;
		final double ymin;
		final double ymax;

		if ( x1 <= x2 )
		{
			xmin = x1;
			xmax = x2;
		}
		else
		{
			xmin = x2;
			xmax = x1;
		}

		if ( y1 <= y2 )
		{
			if ( xmin <= y1 )
			{
				ymin = y1;
				ymax = y2;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y1;
				xmax = y2;
			}
		}
		else
		{
			if ( xmin <= y2 )
			{
				ymin = y2;
				ymax = y1;
			}
			else
			{
				ymin = xmin;
				ymax = xmax;
				xmin = y2;
				xmax = y1;
			}
		}

		/*
		 * Determine the distance, if possible; otherwise NaN.
		 */

		final double result;
		if ( ( xmax < ymin ) || ( xmax < ymax ) && ( xmin < ymin ) )
		{
			result = ymin - xmax;
		}
		else
		{
			result = Double.NaN;
		}

		return result;
	}

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private MathTools()
	{
	}

	/**
	 * Returns the power of two nearest to {@code value}, which must be
	 * positive. If the value is equidistant to two powers of two, the higher
	 * power of two is returned.
	 *
	 * For input values of 2<sup>30</sup>+2<sup>29</sup> or more, the correct
	 * result, 2<sup>31</sup>, can't be represented by a (signed) {@code int}.
	 * Therefore, these values are rounded downwards, resulting in
	 * 2<sup>30</sup>.
	 *
	 * @param value Value to find the nearest power of two for.
	 *
	 * @return Nearest power of two.
	 *
	 * @throws IllegalArgumentException if {@code value} isn't positive.
	 */
	public static int nearestPowerOfTwo( final int value )
	{
		if ( value <= 0 )
		{
			throw new IllegalArgumentException( "value: " + value );
		}

		final int highestOneBit = Integer.highestOneBit( value );
		final boolean roundUp = ( ( ( highestOneBit >> 1 ) & value ) != 0 );
		final int unsignedResult = roundUp ? ( highestOneBit << 1 ) : highestOneBit;

		return ( unsignedResult >= 0 ) ? unsignedResult : ( 1 << 30 );
	}

	/**
	 * Test if two objects are equals according to {@link Object#equals(Object)}
	 * in a null-safe manner.
	 *
	 * @param object1 First object to compare.
	 * @param object2 Second object to compare.
	 *
	 * @return {@code true} is the object are equal or both {@code null}; {@code
	 * false} otherwise.
	 */
	public static boolean equals( @Nullable final Object object1, @Nullable final Object object2 )
	{
		return ( object1 != null ) ? object1.equals( object2 ) : ( object2 == null );
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@code Double.valueOf( value ).hashCode()}.
	 *
	 * @param value Value to calculate hash code for.
	 *
	 * @return Hash code.
	 */
	public static int hashCode( final Double value )
	{
		final int result;

		if ( value != null )
		{
			final long bits = Double.doubleToLongBits( value );
			result = (int)( bits ^ ( bits >>> 32 ) );
		}
		else
		{
			result = 0;
		}

		return result;
	}

	/**
	 * Calculate hash code for floating-point value. This is the equivalent of
	 * calling {@code Double.valueOf( value ).hashCode()}.
	 *
	 * @param value Value to calculate hash code for.
	 *
	 * @return Hash code.
	 */
	public static int hashCode( final double value )
	{
		final long bits = Double.doubleToLongBits( value );
		return (int)( bits ^ ( bits >>> 32 ) );
	}

	/**
	 * Compares two null-able objects according to their natural ordering. Null
	 * is treated as greater than any non-null value.
	 *
	 * @param object1 First object.
	 * @param object2 Second object.
	 *
	 * @return Result of the comparison.
	 */
	public static <T extends Comparable<T>> int compare( @Nullable final T object1, @Nullable final T object2 )
	{
		return object1 == null ? object2 == null ? 0 : 1 : object2 == null ? -1 : object1.compareTo( object2 );
	}
}
