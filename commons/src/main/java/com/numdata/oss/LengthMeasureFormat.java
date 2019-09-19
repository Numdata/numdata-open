/*
 * Copyright (c) 2015-2017, Numdata BV, The Netherlands.
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
import java.util.regex.*;

import com.numdata.oss.LengthMeasurePreferences.*;
import org.jetbrains.annotations.*;

/**
 * A {@link NumberFormat} implementation for formatting and parsing {@link
 * BigRational} values.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "ChainOfInstanceofChecks" )
public class LengthMeasureFormat
extends NumberFormat
{
	/**
	 * Regex pattern to parse numbers. Note that this pattern will accept all
	 * practical output formats that may be produced by this class.
	 *
	 * Acceptable example values:
	 * <pre>
	 *   1.5000
	 *   1500.0000
	 *   0'-1.5000\"
	 *   125'-0.0000\"
	 *   0'-1 1/2\"
	 *   125'-0\"
	 *   1 1/2
	 *   1500
	 *   5'
	 *   60\"
	 *   5'-9\"
	 *   5' 9\"
	 *   5'9\"
	 *   5'-1/2\"
	 *   5' 1/2\"
	 *   5'1/2\"
	 *   5'-9-1/2\"
	 *   5' 9-1/2\"
	 *   5'9-1/2\"
	 *   5'-9 1/2\"
	 *   5' 9 1/2\"
	 *   5'9 1/2\"
	 * </pre>
	 */
	private static final Pattern PARSE_PATTERN = Pattern.compile(
	//                                                    numerator
	//   negative?                                        |      denominator
	//   |      feet                        whole         |      |       decimal (inches)
	//   |      |                           |             |      |       |           fraction  inches?
	//   |      |                           |             |      |       |           |         |
	//   1      2                           3             4      5       6           7         8
	"\\s*(-)?(?:(\\d+)'[-\\s]?)?(?:(?:(?:(?:(\\d+)[-\\s])?(\\d+)/(\\d+))|(\\d+(?:[.,](\\d+))?))(\")?)?" );

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int NEGATIVE_GROUP = 1;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int FEET_GROUP = 2;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int WHOLE_GROUP = 3;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int NUMERATOR_GROUP = 4;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int DENOMINATOR_GROUP = 5;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int DECIMAL_GROUP = 6;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int FRACTION_GROUP = 7;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	public static final int INCHES_GROUP = 8;

	/**
	 * 12 as {@link BigInteger} instance.
	 */
	private static final BigInteger TWELVE = BigInteger.valueOf( 12 );

	/**
	 * Powers of 10.
	 */
	private static final long[] POWERS_OF_TEN = { 1,
	                                              10L,
	                                              100L,
	                                              1000L,
	                                              10000L,
	                                              100000L,
	                                              1000000L,
	                                              10000000L,
	                                              100000000L,
	                                              1000000000L,
	                                              10000000000L,
	                                              100000000000L,
	                                              1000000000000L,
	                                              10000000000000L,
	                                              100000000000000L,
	                                              1000000000000000L,
	                                              10000000000000000L,
	                                              100000000000000000L,
	                                              1000000000000000000L };

	/**
	 * Measure format preferences.
	 */
	@NotNull
	private final LengthMeasurePreferences _preferences;

	/**
	 * Symbols to use for formatting numbers.
	 */
	@NotNull
	private final DecimalFormatSymbols _decimalFormatSymbols;

	/**
	 * Whether to parse strings to {@link BigInteger}, {@link BigDecimal}, or
	 * {@link BigRational} objects (default: false).
	 */
	private boolean _usingBigValues = false;

	/**
	 * Create instance using the given preferences.
	 *
	 * @param preferences          Measure format preferences.
	 * @param decimalFormatSymbols Symbols to use for formatting numbers.
	 */
	public LengthMeasureFormat( @NotNull final LengthMeasurePreferences preferences, @NotNull final DecimalFormatSymbols decimalFormatSymbols )
	{
		_preferences = new LengthMeasurePreferences( preferences );
		_decimalFormatSymbols = decimalFormatSymbols;

	}

	/**
	 * Get symbols to use for formatting numbers.
	 *
	 * @return Symbols to use for formatting numbers.
	 */
	@NotNull
	public DecimalFormatSymbols getDecimalFormatSymbols()
	{
		return _decimalFormatSymbols;
	}

	/**
	 * Get whether to parse strings to {@link BigInteger}, {@link BigDecimal},
	 * or {@link BigRational} objects (default: false).
	 *
	 * @return {@code true} if strings are parsed to 'big' numbers.
	 */
	public boolean isUsingBigValues()
	{
		return _usingBigValues;
	}

	/**
	 * Set Whether to parse strings to {@link BigDecimal} or {@link BigRational}
	 * objects (default: false).
	 *
	 * @param usingBigValues {@code true} to parse strings to 'big' numbers.
	 */
	public void setUsingBigValues( final boolean usingBigValues )
	{
		_usingBigValues = usingBigValues;
	}

	/**
	 * Get measure format preferences.
	 *
	 * @return Measure format preferences.
	 */
	@NotNull
	public LengthMeasurePreferences getPreferences()
	{
		return _preferences;
	}

	@Override
	public StringBuffer format( final double number, @NotNull final StringBuffer toAppendTo, @NotNull final FieldPosition pos )
	{
		return format( Double.valueOf( number ), toAppendTo, pos );
	}

	@Override
	public StringBuffer format( final long number, @NotNull final StringBuffer toAppendTo, @NotNull final FieldPosition pos )
	{
		return format( Long.valueOf( number ), toAppendTo, pos );
	}

	@Override
	public StringBuffer format( @NotNull final Object object, @NotNull final StringBuffer toAppendTo, @NotNull final FieldPosition pos )
	{
		pos.setBeginIndex( 0 );
		pos.setEndIndex( 0 );

		final LengthMeasurePreferences preferences = getPreferences();
		final LengthUnit baseUnit = preferences.getBaseUnit();
		final LengthUnit displayUnit = preferences.getDisplayUnit();

		if ( preferences.isFraction() )
		{
			BigRational number = toBigRational( object );
			number = baseUnit.convert( number, displayUnit );
			number = limitBigRationalScale( number );

			formatRational( number, toAppendTo );
		}
		else
		{
			BigDecimal number = toBigDecimal( object );
			number = baseUnit.convert( number, displayUnit, preferences.getMaxScale() );
			number = limitBigDecimalScale( number );

			formatDecimal( number, toAppendTo );
		}

		return toAppendTo;
	}

	/**
	 * Convert a number object to a {@link BigDecimal}.
	 *
	 * @param number Number object to convert.
	 *
	 * @return {@link BigDecimal}.
	 *
	 * @throws IllegalArgumentException if number can't be converted.
	 */
	@NotNull
	protected BigDecimal toBigDecimal( @NotNull final Object number )
	{
		final BigDecimal result;

		if ( number instanceof BigDecimal )
		{
			result = (BigDecimal)number;
		}
		else if ( number instanceof BigRational )
		{
			final BigRational bigRational = (BigRational)number;
			final BigDecimal numerator = new BigDecimal( bigRational.getNumerator() );
			final BigDecimal denominator = new BigDecimal( bigRational.getDenominator() );

			result = numerator.divide( denominator, getPreferences().getMaxScale(), RoundingMode.HALF_UP );
		}
		else if ( number instanceof BigInteger )
		{
			result = new BigDecimal( (BigInteger)number );
		}
		else if ( number instanceof Byte ||
		          number instanceof Short ||
		          number instanceof Integer ||
		          number instanceof Long )
		{
			result = new BigDecimal( ( (Number)number ).longValue() );
		}
		else if ( number instanceof Number )
		{
			result = BigDecimal.valueOf( ( (Number)number ).doubleValue() );
		}
		else
		{
			throw new IllegalArgumentException( "Can't format " + number.getClass() + ": " + number );
		}

		return result;
	}

	/**
	 * Limit scale of {@link BigDecimal} according to {@link
	 * LengthMeasurePreferences}.
	 *
	 * @param bigDecimal Value whose scale to limit.
	 *
	 * @return {@link BigDecimal} with limited scale.
	 */
	@NotNull
	protected BigDecimal limitBigDecimalScale( @NotNull final BigDecimal bigDecimal )
	{
		BigDecimal result = bigDecimal;

		final LengthMeasurePreferences preferences = getPreferences();

		final int maxScale = preferences.getMaxScale();
		if ( result.scale() > maxScale )
		{
			result = result.setScale( maxScale, RoundingMode.HALF_UP );
		}

		result = result.stripTrailingZeros();

		final int minScale = preferences.getMinScale();
		if ( result.scale() < minScale )
		{
			result = result.setScale( minScale, RoundingMode.UNNECESSARY );
		}

		return result;
	}

	/**
	 * Formats a {@link BigDecimal}.
	 *
	 * @param number     Number to format.
	 * @param toAppendTo where the text is to be appended
	 */
	protected void formatDecimal( final BigDecimal number, @NotNull final StringBuffer toAppendTo )
	{
		final LengthMeasurePreferences preferences = getPreferences();

		BigDecimal whole = number;

		if ( whole.signum() < 0 )
		{
			toAppendTo.append( '-' );
			whole = whole.negate();
		}

		if ( preferences.isFeet() )
		{
			final BigInteger feet = whole.toBigInteger().divide( TWELVE );
			if ( preferences.isFeetZero() || ( feet.signum() != 0 ) )
			{
				toAppendTo.append( feet.toString() );
				toAppendTo.append( preferences.getFeetSymbol() );

				whole = whole.subtract( new BigDecimal( feet.multiply( TWELVE ) ) );

				if ( preferences.isInchesZero() || ( whole.signum() != 0 ) )
				{
					toAppendTo.append( preferences.getFeetInchesSeparator() );
				}
				else
				{
					whole = null;
				}
			}
		}

		if ( whole != null )
		{
			final String wholeString = whole.toPlainString();
			final char decimalSeparator = getDecimalFormatSymbols().getDecimalSeparator();
			toAppendTo.append( ( decimalSeparator == '.' ) ? wholeString : wholeString.replace( '.', decimalSeparator ) );

			if ( preferences.getDisplayUnit() == LengthUnit.INCH )
			{
				toAppendTo.append( preferences.getInchSymbol() );
			}
		}
	}

	/**
	 * Convert a number object to a {@link BigRational}.
	 *
	 * @param number Number object to convert.
	 *
	 * @return {@link BigRational}.
	 *
	 * @throws IllegalArgumentException if number can't be converted.
	 */
	@NotNull
	protected BigRational toBigRational( @NotNull final Object number )
	{
		final BigRational result;

		if ( number instanceof BigRational )
		{
			result = (BigRational)number;
		}
		else if ( number instanceof BigDecimal )
		{
			result = new BigRational( (BigDecimal)number );
		}
		else if ( number instanceof BigInteger )
		{
			result = new BigRational( (BigInteger)number );
		}
		else if ( number instanceof Byte ||
		          number instanceof Short ||
		          number instanceof Integer ||
		          number instanceof Long )
		{
			result = new BigRational( ( (Number)number ).longValue() );
		}
		else if ( number instanceof Number )
		{
			result = new BigRational( ( (Number)number ).doubleValue() );
		}
		else
		{
			throw new IllegalArgumentException( "Can't format " + number.getClass() + ": " + number );
		}

		return result;
	}

	/**
	 * Limit scale of {@link BigRational} according to {@link
	 * LengthMeasurePreferences}.
	 *
	 * @param bigRational Value whose scale to limit.
	 *
	 * @return {@link BigRational} with limited scale.
	 */
	@NotNull
	protected BigRational limitBigRationalScale( @NotNull final BigRational bigRational )
	{
		final BigRational result;

		final LengthMeasurePreferences preferences = getPreferences();
		final LengthUnit displayUnit = preferences.getDisplayUnit();

		if ( displayUnit.isImperial() )
		{
			final long precision = 1L << preferences.getMaxScale();
			result = new BigRational( bigRational.multiply( precision ).bigIntegerValue(), BigInteger.valueOf( precision ) );
		}
		else
		{
			final long precision = POWERS_OF_TEN[ preferences.getMaxScale() ];
			final BigInteger bigPrecision = BigInteger.valueOf( precision );

			if ( bigRational.getDenominator().compareTo( bigPrecision ) > 0 )
			{
				result = new BigRational( bigRational.multiply( precision ).bigIntegerValue(), bigPrecision );
			}
			else
			{
				result = bigRational;
			}
		}

		return result;
	}

	/**
	 * Formats a {@link BigRational}.
	 *
	 * @param number     Number to format.
	 * @param toAppendTo where the text is to be appended
	 */
	public void formatRational( @NotNull final BigRational number, @NotNull final StringBuffer toAppendTo )
	{
		final LengthMeasurePreferences preferences = getPreferences();

		BigInteger numerator = number.getNumerator();
		final BigInteger denominator = number.getDenominator();

		if ( numerator.signum() < 0 )
		{
			toAppendTo.append( '-' );
			numerator = numerator.negate();
		}

		if ( denominator.compareTo( BigInteger.ONE ) != 0 )
		{
			if ( preferences.isWhole() )
			{
				BigInteger whole = numerator.divide( denominator );
				if ( preferences.isWholeZero() || ( whole.signum() != 0 ) )
				{
					numerator = numerator.remainder( denominator );

					if ( preferences.isFeet() )
					{
						final BigInteger feet = whole.divide( TWELVE );
						if ( preferences.isFeetZero() || ( feet.signum() != 0 ) )
						{
							toAppendTo.append( feet.toString() );
							toAppendTo.append( preferences.getFeetSymbol() );
							whole = whole.remainder( TWELVE );
							toAppendTo.append( preferences.getFeetInchesSeparator() );
						}
					}

					if ( preferences.isWholeZero() || ( whole.signum() != 0 ) )
					{
						toAppendTo.append( whole.toString() );
						toAppendTo.append( preferences.getWholeFractionSeparator() );
					}
				}
			}

			toAppendTo.append( numerator.toString() );
			toAppendTo.append( preferences.getFractionSeparator() );
			toAppendTo.append( denominator.toString() );

			if ( preferences.getDisplayUnit() == LengthUnit.INCH )
			{
				toAppendTo.append( preferences.getInchSymbol() );
			}
		}
		else
		{
			if ( preferences.isFeet() )
			{
				final BigInteger feet = numerator.divide( TWELVE );
				if ( preferences.isFeetZero() || ( feet.signum() != 0 ) )
				{
					toAppendTo.append( feet.toString() );
					toAppendTo.append( preferences.getFeetSymbol() );

					numerator = numerator.remainder( TWELVE );

					if ( preferences.isInchesZero() || ( numerator.signum() != 0 ) )
					{
						toAppendTo.append( preferences.getFeetInchesSeparator() );
					}
					else
					{
						numerator = null;
					}
				}
			}

			if ( numerator != null )
			{
				toAppendTo.append( numerator.toString() );

				if ( preferences.getDisplayUnit() == LengthUnit.INCH )
				{
					toAppendTo.append( preferences.getInchSymbol() );
				}
			}
		}
	}

	@Nullable
	@Override
	public Number parse( final String source, final ParsePosition pos )
	{
		final Number result;

		final int initialIndex = pos.getIndex();
		final String substring = source.substring( initialIndex );
		final Matcher matcher = PARSE_PATTERN.matcher( substring );
		if ( !matcher.find() )
		{
			result = null;
		}
		else
		{
//			for ( int i = 1; i <= matcher.groupCount(); i++ ) System.out.print( "[" + i + "]=" + matcher.group( i ) + ( ( i < matcher.groupCount() ) ? ", " : "\n" ) );
			final boolean negative = ( matcher.group( NEGATIVE_GROUP ) != null );
			final String feetGroup = matcher.group( FEET_GROUP );
			final String decimalGroup = matcher.group( DECIMAL_GROUP );
			final String numeratorGroup = matcher.group( NUMERATOR_GROUP );

			final LengthMeasurePreferences preferences = getPreferences();
			final LengthUnit baseUnit = preferences.getBaseUnit();
			LengthUnit displayUnit = ( matcher.group( INCHES_GROUP ) != null ) ? LengthUnit.INCH : preferences.getDisplayUnit();


			BigRational value;

			if ( decimalGroup != null ) // ['-'] [<feet>] <decimal>
			{
				try
				{
					final char decimalSeparator = getDecimalFormatSymbols().getDecimalSeparator();
					value = new BigRational( ( decimalSeparator == '.' ) ? decimalGroup : decimalGroup.replace( decimalSeparator, '.' ) );

					if ( feetGroup != null )
					{
						value = value.add( Long.parseLong( feetGroup ) * 12L );
						displayUnit = LengthUnit.INCH;
					}

					if ( negative )
					{
						value = value.negate();
					}
				}
				catch ( final NumberFormatException ignored )
				{
					value = null;
				}
			}
			else if ( numeratorGroup != null ) // ['-'] [<feet>] [<whole>] <numerator> '/' <denominator>
			{
				@SuppressWarnings( "TooBroadScope" )
				long numerator = Long.parseLong( numeratorGroup );

				final int denominator = Integer.parseInt( matcher.group( DENOMINATOR_GROUP ) );
				if ( denominator == 0 )
				{
					value = null;
				}
				else
				{
					final String wholeGroup = matcher.group( WHOLE_GROUP );
					if ( wholeGroup != null )
					{
						numerator += Long.parseLong( wholeGroup ) * denominator;
					}

					if ( feetGroup != null )
					{
						numerator += Long.parseLong( feetGroup ) * 12L * denominator;
						displayUnit = LengthUnit.INCH;
					}

					if ( negative )
					{
						numerator = -numerator;
					}

					value = new BigRational( numerator, denominator );
				}
			}
			else if ( feetGroup != null ) // <feet>
			{
				value = BigRational.valueOf( Long.parseLong( feetGroup ) );
				displayUnit = LengthUnit.FOOT;
			}
			else
			{
				value = null;
			}

			if ( value == null )
			{
				result = null;
			}
			else
			{
				value = displayUnit.convert( value, baseUnit );

				final long numerator = value.getNumerator().longValue();
				final long denominator = value.getDenominator().longValue();

				if ( denominator == 1 )
				{
					if ( ( numerator >= Integer.MIN_VALUE ) && ( numerator <= Integer.MAX_VALUE ) )
					{
						result = (int)numerator;
					}
					else
					{
						result = numerator;
					}
				}
				else if ( !isUsingBigValues() || isPowerOfTwo( denominator ) )
				{
					result = value.doubleValue();
				}
				else
				{
					final int scale = ( denominator == 10 ) ? 1 :
					                  ( denominator == 100 ) ? 2 :
					                  ( denominator == 1000 ) ? 3 :
					                  ( denominator == 10000 ) ? 4 :
					                  ( denominator == 100000 ) ? 5 :
					                  ( denominator == 1000000 ) ? 6 :
					                  ( denominator == 10000000 ) ? 7 :
					                  ( denominator == 100000000 ) ? 8 :
					                  ( denominator == 1000000000 ) ? 9 : -1;
					if ( scale > 0 )
					{
						result = new BigDecimal( BigInteger.valueOf( numerator ), scale );
					}
					else
					{
						result = value;
					}
				}
			}
		}

		if ( result != null )
		{
			// set parse position after number
			pos.setIndex( initialIndex + matcher.end() );
		}
		else
		{
			pos.setErrorIndex( initialIndex );
		}

		return result;
	}

	/**
	 * Determine whether the given value is power of two.\
	 *
	 * @param value Value to test.
	 *
	 * @return {@code true} if value is a power of two.
	 */
	protected boolean isPowerOfTwo( final long value )
	{
		return ( value != 0 ) && ( ( value & ( value - 1 ) ) == 0 );
	}
}
