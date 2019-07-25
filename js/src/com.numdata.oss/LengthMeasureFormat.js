/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
import { BigDecimal, BigInteger, RoundingMode } from 'bigdecimal';
import bigRat from 'big-rational';
import LengthMeasurePreferences from './LengthMeasurePreferences';

const BigRational = bigRat.one.constructor;
const parseBigRational = bigRat;

/**
 * Class for formatting and parsing lengths.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ported to Javascript)
 */
export default class LengthMeasureFormat
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
	static PARSE_PATTERN =
		//                                               numerator
		//  negative?                                    |      denominator
		//  |      feet                      whole       |      |      decimal (inches)
		//  |      |                         |           |      |      |       fraction    inches?
		//  |      |                         |           |      |      |          |        |
		//  1      2                         3           4      5      6          7        8
		/\s*(-)?(?:(\d+)'[-\s]?)?(?:(?:(?:(?:(\d+)[-\s])?(\d+)\/(\d+))|(\d+(?:[.,](\d+))?))(")?)?/;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static NEGATIVE_GROUP = 1;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static FEET_GROUP = 2;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static WHOLE_GROUP = 3;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static NUMERATOR_GROUP = 4;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static DENOMINATOR_GROUP = 5;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static DECIMAL_GROUP = 6;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static FRACTION_GROUP = 7;

	/**
	 * Group index in {@link #PARSE_PATTERN}.
	 */
	static INCHES_GROUP = 8;

	/**
	 * 12 as {@link BigInteger} instance.
	 */
	static TWELVE = BigInteger.valueOf( 12 );

	/**
	 * Powers of 10.
	 */
	static POWERS_OF_TEN = [
		1,
		10,
		100,
		1000,
		10000,
		100000,
		1000000,
		10000000,
		100000000,
		1000000000,
		10000000000,
		100000000000,
		1000000000000,
		10000000000000,
		100000000000000,
		1000000000000000,
		10000000000000000,
		100000000000000000,
		1000000000000000000
	];

	/**
	 * Measure format preferences.
	 *
	 * @type {LengthMeasurePreferences}
	 */
	_preferences;

	/**
	 * Symbols to use for formatting numbers.
	 *
	 * @type {DecimalFormatSymbols}
	 */
	_decimalFormatSymbols;

	/**
	 * Whether to parse strings to {@link BigInteger}, {@link BigDecimal}, or
	 * {@link BigRational} objects (default: false).
	 *
	 * @type {boolean}
	 */
	_usingBigValues = false;

	/**
	 * Create instance using the given preferences.
	 *
	 * @param {LengthMeasurePreferences} preferences Measure format preferences.
	 * @param {DecimalFormatSymbols} decimalFormatSymbols Symbols to use for formatting numbers.
	 */
	constructor( preferences, decimalFormatSymbols )
	{
		this._preferences = new LengthMeasurePreferences( preferences );
		this._decimalFormatSymbols = decimalFormatSymbols;
	}

	/**
	 * Get symbols to use for formatting numbers.
	 *
	 * @return {DecimalFormatSymbols} Symbols to use for formatting numbers.
	 */
	getDecimalFormatSymbols()
	{
		return this._decimalFormatSymbols;
	}

	/**
	 * Get whether to parse strings to {@link BigInteger}, {@link BigDecimal},
	 * or {@link BigRational} objects (default: false).
	 *
	 * @return {boolean} {@code true} if strings are parsed to 'big' numbers.
	 */
	isUsingBigValues()
	{
		return this._usingBigValues;
	}

	/**
	 * Set Whether to parse strings to {@link BigDecimal} or {@link BigRational}
	 * objects (default: false).
	 *
	 * @param {boolean} usingBigValues {@code true} to parse strings to 'big' numbers.
	 */
	setUsingBigValues( usingBigValues )
	{
		this._usingBigValues = usingBigValues;
	}

	/**
	 * Get measure format preferences.
	 *
	 * @return {LengthMeasurePreferences} Measure format preferences.
	 */
	getPreferences()
	{
		return this._preferences;
	}

	/**
	 * Formats the given number as a length measure.
	 *
	 * @param {number|BigDecimal|BigRational} object Number to format.
	 * @param {string} [toAppendTo] String to append the result to.
	 *
	 * @returns {string} Resulting string.
	 */
	format( object, toAppendTo )
	{
		if ( toAppendTo === undefined )
		{
			toAppendTo = '';
		}

		let preferences = this.getPreferences();
		let baseUnit = preferences.getBaseUnit();
		let displayUnit = preferences.getDisplayUnit();

		if ( preferences.isFraction() )
		{
			let number = this.toBigRational( object );
			number = baseUnit.convert( number, displayUnit );
			number = this.limitBigRationalScale( number );

			toAppendTo += this.formatRational( number );
		}
		else
		{
			let number = this.toBigDecimal( object );
			number = baseUnit.convert( number, displayUnit, preferences.getMaxScale() );
			number = this.limitBigDecimalScale( number );

			toAppendTo += this.formatDecimal( number );
		}

		return toAppendTo;
	}

	/**
	 * Convert a number object to a {@link BigDecimal}.
	 *
	 * @param number Number object to convert.
	 *
	 * @return {BigDecimal}
	 *
	 * @throws TypeError if number can't be converted.
	 */
	toBigDecimal( number )
	{
		let result;

		if ( number instanceof BigDecimal )
		{
			result = number;
		}
		else if ( number instanceof BigRational )
		{
			let numerator = new BigDecimal( number.numerator.toString() );
			let denominator = new BigDecimal( number.denominator.toString() );

			result = numerator.divide( denominator, this.getPreferences().getMaxScale(), RoundingMode.HALF_UP() );
		}
		else if ( number instanceof BigInteger ) // TODO: May need support for 'big-integer' (used by 'big-rational').
		{
			result = new BigDecimal( number );
		}
		else if ( typeof number === 'number' )
		{
			result = new BigDecimal( number );
		}
		else
		{
			throw TypeError( "Can't format " + typeof number + ": " + number );
		}

		return result;
	}

	/**
	 * Limit scale of {@link BigDecimal} according to {@link LengthMeasurePreferences}.
	 *
	 * @param {BigDecimal} bigDecimal Value whose scale to limit.
	 *
	 * @return {BigDecimal} {@link BigDecimal} with limited scale.
	 */
	limitBigDecimalScale( bigDecimal )
	{
		let result = bigDecimal;

		let preferences = this.getPreferences();

		let maxScale = preferences.getMaxScale();
		if ( result.scale() > maxScale )
		{
			result = result.setScale( maxScale, RoundingMode.HALF_UP() );
		}

		result = result.stripTrailingZeros();

		let minScale = preferences.getMinScale();
		if ( result.scale() < minScale )
		{
			result = result.setScale( minScale, RoundingMode.UNNECESSARY() );
		}

		return result;
	}

	/**
	 * Formats a {@link BigDecimal}.
	 *
	 * @param {BigDecimal} number Number to format.
	 * @param {string} [toAppendTo] String to append the result to.
	 *
	 * @return {string} Formatted number.
	 */
	formatDecimal( number, toAppendTo )
	{
		if ( toAppendTo === undefined )
		{
			toAppendTo = '';
		}

		let preferences = this.getPreferences();

		let whole = number;

		if ( whole.signum() < 0 )
		{
			toAppendTo += '-';
			whole = whole.negate();
		}

		if ( preferences.isFeet() )
		{
			let feet = whole.toBigInteger().divide( LengthMeasureFormat.TWELVE );
			if ( preferences.isFeetZero() || ( feet.signum() !== 0 ) )
			{
				toAppendTo += feet.toString();
				toAppendTo += preferences.getFeetSymbol();

				whole = whole.subtract( new BigDecimal( feet.multiply( LengthMeasureFormat.TWELVE ) ) );

				if ( preferences.isInchesZero() || ( whole.signum() !== 0 ) )
				{
					toAppendTo += preferences.getFeetInchesSeparator();
				}
				else
				{
					whole = null;
				}
			}
		}

		if ( whole )
		{
			let wholeString = whole.toPlainString();
			let decimalSeparator = this.getDecimalFormatSymbols().decimalSeparator;
			toAppendTo += ( decimalSeparator === '.' ) ? wholeString : wholeString.replace( '.', decimalSeparator );

			if ( preferences.getDisplayUnit() === LengthMeasurePreferences.LengthUnit.INCH )
			{
				toAppendTo += preferences.getInchSymbol();
			}
		}

		return toAppendTo;
	}

	/**
	 * Convert a number object to a {@link BigRational}.
	 *
	 * @param number Number object to convert.
	 *
	 * @return {BigRational}
	 *
	 * @throws TypeError if number can't be converted.
	 */
	toBigRational( number )
	{
		let result;

		if ( number instanceof BigRational )
		{
			result = number;
		}
		else if ( number instanceof BigDecimal ||
		          number instanceof BigInteger )
		{
			result = parseBigRational( number.toString() );
		}
		else if ( typeof number === 'number' )
		{
			result = parseBigRational( number );
		}
		else
		{
			throw TypeError( "Can't format " + typeof number + ": " + number );
		}

		return result;
	}

	/**
	 * Limit scale of {@link BigRational} according to {@link LengthMeasurePreferences}.
	 *
	 * @param {BigRational} bigRational Value whose scale to limit.
	 *
	 * @return {BigRational} Rational with limited scale.
	 */
	limitBigRationalScale( bigRational )
	{
		let result;

		let preferences = this.getPreferences();
		let displayUnit = preferences.getDisplayUnit();

		if ( displayUnit.isImperial() )
		{
			let precision = 1 << preferences.getMaxScale();
			result = parseBigRational( bigRational.multiply( precision ).round().numerator, precision );
		}
		else
		{
			let precision = LengthMeasureFormat.POWERS_OF_TEN[ preferences.getMaxScale() ];

			if ( bigRational.denominator.compareTo( precision ) > 0 )
			{
				result = parseBigRational( bigRational.multiply( precision ).round().numerator, precision );
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
	 * @param {BigRational} number     Number to format.
	 * @param {string} [toAppendTo] String to append the result to.
	 *
	 * @return {string} Formatted number.
	 */
	formatRational( number, toAppendTo )
	{
		if ( toAppendTo === undefined )
		{
			toAppendTo = '';
		}

		let preferences = this.getPreferences();

		let numerator = number.numerator;
		let denominator = number.denominator;

		if ( numerator.isNegative() )
		{
			toAppendTo += '-';
			numerator = numerator.multiply( -1 );
		}

		if ( !denominator.equals( 1 ) )
		{
			if ( preferences.isWhole() )
			{
				let whole = numerator.divide( denominator );
				if ( preferences.isWholeZero() || !whole.isZero() )
				{
					numerator = numerator.remainder( denominator );

					if ( preferences.isFeet() )
					{
						let feet = whole.divide( 12 );
						if ( preferences.isFeetZero() || !feet.isZero() )
						{
							toAppendTo += feet.toString();
							toAppendTo += preferences.getFeetSymbol();
							whole = whole.remainder( 12 );
							toAppendTo += preferences.getFeetInchesSeparator();
						}
					}

					if ( preferences.isWholeZero() || !whole.isZero() )
					{
						toAppendTo += whole.toString();
						toAppendTo += preferences.getWholeFractionSeparator();
					}
				}
			}

			toAppendTo += numerator.toString();
			toAppendTo += preferences.getFractionSeparator();
			toAppendTo += denominator.toString();

			if ( preferences.getDisplayUnit() === LengthMeasurePreferences.LengthUnit.INCH )
			{
				toAppendTo += preferences.getInchSymbol();
			}
		}
		else
		{
			if ( preferences.isFeet() )
			{
				let feet = numerator.divide( 12 );
				if ( preferences.isFeetZero() || !feet.isZero() )
				{
					toAppendTo += feet.toString();
					toAppendTo += preferences.getFeetSymbol();

					numerator = numerator.remainder( 12 );

					if ( preferences.isInchesZero() || !numerator.isZero() )
					{
						toAppendTo += preferences.getFeetInchesSeparator();
					}
					else
					{
						numerator = null;
					}
				}
			}

			if ( numerator !== null )
			{
				toAppendTo += numerator.toString();

				if ( preferences.getDisplayUnit() === LengthMeasurePreferences.LengthUnit.INCH )
				{
					toAppendTo += preferences.getInchSymbol();
				}
			}
		}

		return toAppendTo;
	}

	/**
	 * Parses the given string as a length measure.
	 *
	 * @param {string} source String to parse.
	 * @param {number} [initialIndex] Index in the string to start parsing at.
	 *
	 * @return {number|BigDecimal|BigRational|null} Parsed value, or null if not parsable.
	 */
	parse( source, initialIndex )
	{
		let result;

		let substring = initialIndex ? source.substring( initialIndex ) : source;
		let matcher = LengthMeasureFormat.PARSE_PATTERN.exec( substring );
		if ( !matcher )
		{
			result = null;
		}
		else
		{
			//console.info( "RegExp.exec => " + matcher );
			let negative = ( matcher[ LengthMeasureFormat.NEGATIVE_GROUP ] !== undefined );
			let feetGroup = matcher[ LengthMeasureFormat.FEET_GROUP ];
			let decimalGroup = matcher[ LengthMeasureFormat.DECIMAL_GROUP ];
			let numeratorGroup = matcher[ LengthMeasureFormat.NUMERATOR_GROUP ];

			let preferences = this.getPreferences();
			let baseUnit = preferences.getBaseUnit();
			let displayUnit = ( matcher[ LengthMeasureFormat.INCHES_GROUP ] !== undefined ) ? LengthMeasurePreferences.LengthUnit.INCH : preferences.getDisplayUnit();

			let value;

			if ( decimalGroup !== undefined ) // ['-'] [<feet>] <decimal>
			{
				let decimalSeparator = this.getDecimalFormatSymbols().decimalSeparator;
				value = parseBigRational( ( decimalSeparator === '.' ) ? decimalGroup : decimalGroup.replace( decimalSeparator, '.' ) );

				if ( feetGroup !== undefined )
				{
					value = value.add( parseBigRational( feetGroup ).multiply( 12 ) );
					displayUnit = LengthMeasurePreferences.LengthUnit.INCH;
				}

				if ( negative )
				{
					value = value.multiply( -1 );
				}
			}
			else if ( numeratorGroup !== undefined ) // ['-'] [<feet>] [<whole>] <numerator> '/' <denominator>
			{
				let numerator = parseInt( numeratorGroup );
				let denominator = parseInt( matcher[ LengthMeasureFormat.DENOMINATOR_GROUP ] );
				if ( denominator === 0 )
				{
					value = null;
				}
				else
				{
					let wholeGroup = matcher[ LengthMeasureFormat.WHOLE_GROUP ];
					if ( wholeGroup !== undefined )
					{
						numerator += parseInt( wholeGroup ) * denominator;
					}

					if ( feetGroup !== undefined )
					{
						numerator += parseInt( feetGroup ) * 12 * denominator;
						displayUnit = LengthMeasurePreferences.LengthUnit.INCH;
					}

					if ( negative )
					{
						numerator = -numerator;
					}

					value = parseBigRational( numerator, denominator );
				}
			}
			else if ( feetGroup !== undefined ) // <feet>
			{
				value = parseBigRational( feetGroup );
				displayUnit = LengthMeasurePreferences.LengthUnit.FOOT;
			}
			else
			{
				value = null;
			}

			if ( value === null )
			{
				result = null;
			}
			else
			{
				value = displayUnit.convert( value, baseUnit );

				let numerator = value.numerator;
				let denominator = value.denominator;

				if ( denominator === 1 )
				{
					result = numerator.valueOf();
				}
				else if ( !this.isUsingBigValues() || LengthMeasureFormat.isPowerOfTwo( denominator ) )
				{
					result = value.valueOf();
				}
				else
				{
					let scale = ( denominator === 10 ) ? 1 :
					            ( denominator === 100 ) ? 2 :
					            ( denominator === 1000 ) ? 3 :
					            ( denominator === 10000 ) ? 4 :
					            ( denominator === 100000 ) ? 5 :
					            ( denominator === 1000000 ) ? 6 :
					            ( denominator === 10000000 ) ? 7 :
					            ( denominator === 100000000 ) ? 8 :
					            ( denominator === 1000000000 ) ? 9 : -1;
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

		return result;
	}

	/**
	 * Determine whether the given value is power of two.
	 *
	 * @param {number} value Value to test.
	 *
	 * @return {boolean} {@code true} if value is a power of two.
	 */
	static isPowerOfTwo( value )
	{
		return ( value !== 0 ) && ( ( value & ( value - 1 ) ) === 0 );
	}
}
