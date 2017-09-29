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

import java.math.*;
import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class offers some utilities for {@link BigDecimal} objects.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "TypeMayBeWeakened", "UnusedDeclaration" } )
public class BigDecimalTools
{
	/**
	 * The number -1 (one).
	 */
	public static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();

	/**
	 * The number 1 (one).
	 */
	public static final BigDecimal ONE = BigDecimal.ONE;

	/**
	 * The number 10 (ten).
	 */
	public static final BigDecimal TEN = BigDecimal.TEN;

	/**
	 * The number 10<sup>2</sup> (hundred).
	 */
	public static final BigDecimal HUNDRED = new BigDecimal( BigInteger.ONE, -2 );

	/**
	 * The number 10<sup>3</sup> (thousand).
	 */
	public static final BigDecimal THOUSAND = new BigDecimal( BigInteger.ONE, -3 );

	/**
	 * The number 10<sup>6</sup> (million).
	 */
	public static final BigDecimal MILLION = new BigDecimal( BigInteger.ONE, -6 );

	/**
	 * The number 10<sup>-1</sup> (one tenth).
	 */
	public static final BigDecimal ONE_TENTH = new BigDecimal( BigInteger.ONE, 1 );

	/**
	 * The number 10<sup>-2</sup> (one hundredth).
	 */
	public static final BigDecimal ONE_HUNDREDTH = new BigDecimal( BigInteger.ONE, 2 );

	/**
	 * The number 10<sup>-3</sup> (one thousandth).
	 */
	public static final BigDecimal ONE_THOUSANDTH = new BigDecimal( BigInteger.ONE, 3 );

	/**
	 * The number 10<sup>-3</sup> (one millionth).
	 */
	public static final BigDecimal ONE_MILLIONTH = new BigDecimal( BigInteger.ONE, 6 );

	/**
	 * Default scale for {@link #toBigDecimal(double)}.
	 */
	public static final int DEFAULT_SCALE = 6;

	/**
	 * Default rounding mode.
	 */
	public static final int DEFAULT_ROUNDING = BigDecimal.ROUND_HALF_UP;

	/**
	 * Default rounding for currency values.
	 */
	public static final int CURRENCY_ROUNDING = BigDecimal.ROUND_HALF_EVEN;

	/**
	 * Must not be instantiated.
	 */
	private BigDecimalTools()
	{
		throw new AssertionError();
	}

	/**
	 * Convert integer value to {@link BigDecimal}.
	 *
	 * @param value Integer value to convert.
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( final int value )
	{
		return new BigDecimal( value );
	}

	/**
	 * Convert integer value to {@link BigDecimal}.
	 *
	 * @param value Integer value to convert.
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( final long value )
	{
		return new BigDecimal( value );
	}

	/**
	 * Convert double value to {@link BigDecimal} with default scale (6).
	 *
	 * @param value Double value to convert.
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( final double value )
	{
		return toBigDecimal( value, DEFAULT_SCALE );
	}

	/**
	 * Convert double value to {@link BigDecimal}.
	 *
	 * @param value Double value to convert.
	 * @param scale Scale to use for double value (number of fraction digits).
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( final double value, final int scale )
	{
		return ( value < 10.0 ) ? ( value == -1.0 ) ? MINUS_ONE :
		                          ( value == 0.0 ) ? BigDecimal.ZERO :
		                          ( value == 1.0 ) ? ONE :
		                          limitScale( BigDecimal.valueOf( value ), scale )
		                        : ( value == 10.0 ) ? TEN :
		                          ( value == 100.0 ) ? HUNDRED :
		                          ( value == 1000.0 ) ? THOUSAND :
		                          limitScale( BigDecimal.valueOf( value ), scale );
	}

	/**
	 * Convert double value to {@link BigDecimal}.
	 *
	 * @param value    Double value to convert.
	 * @param scale    Scale to use for double value (number of fraction
	 *                 digits).
	 * @param exponent Exponent to apply (result = value * 10^exponent).
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( final double value, final int scale, final int exponent )
	{
		BigDecimal bd = BigDecimal.valueOf( value );

		if ( exponent < 0 )
		{
			bd = bd.movePointLeft( -exponent );
		}
		else if ( exponent > 0 )
		{
			bd = bd.movePointRight( exponent );
		}

		return limitScale( bd, scale );
	}

	/**
	 * Convert {@link BigRational} value to {@link BigDecimal}.
	 *
	 * @param value {@link BigRational} value to convert.
	 *
	 * @return {@link BigDecimal} value.
	 */
	@NotNull
	public static BigDecimal toBigDecimal( @NotNull final BigRational value )
	{
		BigDecimal result = new BigDecimal( value.getNumerator() );
		if ( result.signum() != 0 )
		{
			final BigInteger denominator = value.getDenominator();
			if ( !BigInteger.ONE.equals( denominator ) )
			{
				result = result.divide( new BigDecimal( denominator ), DEFAULT_SCALE, DEFAULT_ROUNDING );
			}
		}
		return result;
	}

	/**
	 * Convert {@link Number} value to {@link BigDecimal}.
	 *
	 * @param value Double value to convert.
	 *
	 * @return {@link BigDecimal} value.
	 */
	@SuppressWarnings( "ChainOfInstanceofChecks" )
	@NotNull
	public static BigDecimal toBigDecimal( @NotNull final Number value )
	{
		final BigDecimal result;

		if ( value instanceof BigDecimal )
		{
			result = (BigDecimal)value;
		}
		else if ( value instanceof BigInteger )
		{
			result = new BigDecimal( (BigInteger)value );
		}
		else if ( value instanceof BigRational )
		{
			result = toBigDecimal( (BigRational)value );
		}
		else if ( ( value instanceof Byte ) || ( value instanceof Short ) || ( value instanceof Integer ) || ( value instanceof Long ) )
		{
			result = BigDecimal.valueOf( value.longValue() );
		}
		else
		{
			result = toBigDecimal( value.doubleValue() );
		}

		return result;
	}

	/**
	 * Limit the scale of a big decimal value to the given number. If the
	 * current scale is greater than the given number, the scale is reduced
	 * (default rounding is applied). Any trailing fraction zeros are stripped
	 * from the result (even if the scale was not adjusted).
	 *
	 * @param value        Value whose scale to limit.
	 * @param maximumScale Maximum scale for number.
	 *
	 * @return Value whose scale will never exceed the given maximum scale.
	 */
	@NotNull
	public static BigDecimal limitScale( @NotNull final BigDecimal value, final int maximumScale )
	{
		return limitScale( value, 0, maximumScale );
	}

	/**
	 * Limit the scale of a big decimal value to the given range. If the current
	 * scale is greater than the given minimum, the scale is reduced (default
	 * rounding is applied); trailing zeros beyond the minimum scale are
	 * stripped from the result; if the current scale is less than the given
	 * minimum, the scale is increased.
	 *
	 * @param value        Value whose scale to limit.
	 * @param minimumScale Minimum scale for number.
	 * @param maximumScale Maximum scale for number.
	 *
	 * @return Value whose scale will never exceed the given maximum scale.
	 */
	@NotNull
	public static BigDecimal limitScale( @NotNull final BigDecimal value, final int minimumScale, final int maximumScale )
	{
		BigDecimal result = value;

		if ( result.scale() > maximumScale )
		{
			result = result.setScale( maximumScale, DEFAULT_ROUNDING );
		}

		result = result.stripTrailingZeros();

		if ( result.scale() < minimumScale )
		{
			//noinspection BigDecimalMethodWithoutRoundingCalled
			result = result.setScale( minimumScale );
		}

		return result;
	}

	/**
	 * Test whether the given value is zero.
	 *
	 * @param value Value to test.
	 *
	 * @return {@code true} if value is zero.
	 */
	public static boolean isNegative( @NotNull final BigDecimal value )
	{
		return ( value.signum() == -1 );
	}

	/**
	 * Test whether the given value is zero.
	 *
	 * @param value Value to test.
	 *
	 * @return {@code true} if value is zero.
	 */
	public static boolean isZero( @NotNull final BigDecimal value )
	{
		return ( value.signum() == 0 );
	}

	/**
	 * Test whether the given value is zero.
	 *
	 * @param value Value to test.
	 *
	 * @return {@code true} if value is zero.
	 */
	public static boolean isPositive( @NotNull final BigDecimal value )
	{
		return ( value.signum() == 1 );
	}

	/**
	 * Test whether the first value is less than the second.
	 *
	 * @param value1 First value to test.
	 * @param value2 Second value to test.
	 *
	 * @return {@code true} if first is less than second.
	 */
	public static boolean isLess( @Nullable final BigDecimal value1, @Nullable final BigDecimal value2 )
	{
		return ( value1 != null ) ? ( ( value2 != null ) && ( value1.compareTo( value2 ) < 0 ) ) : ( value2 == null );
	}

	/**
	 * Test whether the first value is less than or equal to the second.
	 *
	 * @param value1 First value to test.
	 * @param value2 Second value to test.
	 *
	 * @return {@code true} if first is less than or equal to second.
	 */
	public static boolean isLessOrEqual( @Nullable final BigDecimal value1, @Nullable final BigDecimal value2 )
	{
		return ( value1 != null ) ? ( ( value2 != null ) && ( value1.compareTo( value2 ) <= 0 ) ) : ( value2 == null );
	}

	/**
	 * Test whether the two given values are equal.
	 *
	 * @param value1 First value to test.
	 * @param value2 Second value to test.
	 *
	 * @return {@code true} if the values are equals.
	 */
	public static boolean isEqual( @Nullable final BigDecimal value1, @Nullable final BigDecimal value2 )
	{
		return ( value1 != null ) ? ( ( value2 != null ) && ( value1.compareTo( value2 ) == 0 ) ) : ( value2 == null );
	}

	/**
	 * Test whether the first value is greater than or equal to the second.
	 *
	 * @param value1 First value to test.
	 * @param value2 Second value to test.
	 *
	 * @return {@code true} if first is greater than or equal to second.
	 */
	public static boolean isGreaterOrEqual( @Nullable final BigDecimal value1, @Nullable final BigDecimal value2 )
	{
		return ( value1 != null ) ? ( ( value2 != null ) && ( value1.compareTo( value2 ) >= 0 ) ) : ( value2 == null );
	}

	/**
	 * Test whether the first value is greater than the second.
	 *
	 * @param value1 First value to test.
	 * @param value2 Second value to test.
	 *
	 * @return {@code true} if first is greater than second.
	 */
	public static boolean isGreater( @Nullable final BigDecimal value1, @Nullable final BigDecimal value2 )
	{
		return ( value1 != null ) ? ( ( value2 != null ) && ( value1.compareTo( value2 ) > 0 ) ) : ( value2 == null );
	}

	/**
	 * Round value using the given rounding mode and precision.
	 *
	 * @param value        Value to round.
	 * @param precision    Precision for rounding.
	 * @param roundingMode Rounding mode.
	 *
	 * @return Value rounded with the requested precision.
	 */
	@NotNull
	public static BigDecimal round( @NotNull final BigDecimal value, @NotNull final RoundingMode roundingMode, @NotNull final BigDecimal precision )
	{
		return ( roundingMode == RoundingMode.UNNECESSARY ) || ( precision.signum() == 0 ) ? value : value.divide( precision, 0, roundingMode ).multiply( precision );
	}

	/**
	 * Round value using the given rounding mode and precision.
	 *
	 * @param value        Value to round.
	 * @param precision    Precision for rounding.
	 * @param roundingMode Rounding mode.
	 *
	 * @return Value rounded with the requested precision.
	 */
	@NotNull
	public static BigRational round( @NotNull final BigRational value, @NotNull final RoundingMode roundingMode, @NotNull final BigRational precision )
	{
		return ( ( roundingMode == RoundingMode.UNNECESSARY ) || precision.isZero() ) ? value : value.divide( precision ).round( getBigRationalRoundMode( roundingMode ) ).multiply( precision );
	}

	/**
	 * Round value using the given rounding mode and precision.
	 *
	 * @param value        Value to round.
	 * @param precision    Precision for rounding.
	 * @param roundingMode Rounding mode.
	 *
	 * @return Value rounded with the requested precision.
	 */
	@NotNull
	public static BigRational round( @NotNull final BigRational value, final int roundingMode, @NotNull final BigRational precision )
	{
		return ( ( roundingMode < 0 ) || precision.isZero() ) ? value : value.divide( precision ).round( roundingMode ).multiply( precision );
	}

	/**
	 * Convert {@link RoundingMode} to {@link BigRational} constant.
	 *
	 * @param roundingMode {@link RoundingMode} to convert.
	 *
	 * @return {@link BigRational} rounding mode constant; -1 if rounding mode
	 * is {@link RoundingMode#UNNECESSARY}.
	 */
	public static int getBigRationalRoundMode( @NotNull final RoundingMode roundingMode )
	{
		final int result;

		switch ( roundingMode )
		{
			case UP:
				result = BigRational.ROUND_UP;
				break;

			case DOWN:
				result = BigRational.ROUND_DOWN;
				break;

			case CEILING:
				result = BigRational.ROUND_CEILING;
				break;

			case FLOOR:
				result = BigRational.ROUND_FLOOR;
				break;

			case HALF_UP:
				result = BigRational.ROUND_HALF_UP;
				break;

			case HALF_DOWN:
				result = BigRational.ROUND_HALF_DOWN;
				break;

			case HALF_EVEN:
				result = BigRational.ROUND_HALF_EVEN;
				break;

			case UNNECESSARY:
				result = -1;
				break;

			default:
				throw new AssertionError( "unsupported: " + roundingMode );
		}

		return result;
	}

	/**
	 * Round currency value to cents.
	 *
	 * @param value Value to round.
	 *
	 * @return Value rounded to two decimals.
	 */
	@NotNull
	public static BigDecimal roundCents( @NotNull final BigDecimal value )
	{
		return ( value.scale() > 2 ) ? value.setScale( 2, CURRENCY_ROUNDING ) : value;
	}

	/**
	 * Get percentage of a value.
	 *
	 * @param value      Value to get percentage of.
	 * @param percentage Percentage to get.
	 *
	 * @return The requested percentage of the value.
	 */
	@NotNull
	public static BigDecimal getPercentage( @NotNull final BigDecimal value, @NotNull final BigDecimal percentage )
	{
		return value.multiply( getPercentFactor( percentage ) );
	}

	/**
	 * Get multiplication factor for percentage.
	 *
	 * @param percentage Percentage to get factor for.
	 *
	 * @return Multiplication factor for percentage.
	 */
	@NotNull
	public static BigDecimal getPercentFactor( @NotNull final BigDecimal percentage )
	{
		return percentage.movePointLeft( 2 );
	}

	/**
	 * Parse {@link BigDecimal} value from localized string.
	 *
	 * @param locale Locale to use for number format.
	 * @param value  Localized string value to parse.
	 *
	 * @return {@link BigDecimal}.
	 *
	 * @throws NumberFormatException if the string is not a valid number.
	 */
	@NotNull
	public static BigDecimal parse( @NotNull final Locale locale, @NotNull final String value )
	{
		return parse( DecimalFormat.getNumberInstance( locale ), value );
	}

	/**
	 * Parse {@link BigDecimal} value from localized string.
	 *
	 * @param format Number format to parse.
	 * @param value  Localized string value to parse.
	 *
	 * @return {@link BigDecimal}.
	 *
	 * @throws NumberFormatException if the string is not a valid number.
	 * @throws ClassCastException if the {@code format} is not a {@link
	 * DecimalFormat}.
	 */
	@NotNull
	public static BigDecimal parse( @NotNull final NumberFormat format, @NotNull final String value )
	{
		final String trimmed = value.trim();
		if ( trimmed.isEmpty() )
		{
			throw new NumberFormatException( "empty value" );
		}

		final DecimalFormat decimalFormat = (DecimalFormat)format.clone();
		decimalFormat.setParseBigDecimal( true );

		final ParsePosition parsePosition = new ParsePosition( 0 );
		final BigDecimal result = (BigDecimal)decimalFormat.parse( trimmed, parsePosition );
		if ( parsePosition.getIndex() != trimmed.length() )
		{
			throw new NumberFormatException( value );
		}

		return result;
	}

}
