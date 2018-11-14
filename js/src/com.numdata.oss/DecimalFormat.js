/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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

/**
 * Drop-in replacement for only the most basic usages of Java's DecimalFormat.
 *
 * This implementation only supports minimum integer digits, minimum/maximum
 * fraction digits, prefix and suffix.
 *
 * Grouping, exponent notation, percentages, currency, quotes and sub-patterns
 * are not supported.
 *
 * @author Gerrit Meinders
 */
export default class DecimalFormat
{
	/**
	 * Constructs a new instance.
	 *
	 * @param {string} pattern Decimal format pattern.
	 */
	constructor( pattern )
	{
		const match = /(#*)(0*)(?:\.(0*)(#*))?/.exec( pattern );

		if ( match )
		{
			this.minimumIntegerDigits = match[ 2 ] ? match[ 2 ].length : 0;
			this.maximumIntegerDigits = this.minimumIntegerDigits + ( match[ 1 ] ? match[ 1 ].length : 99 ); // Ignored; only applies to exponent notation.
			this.minimumFractionDigits = match[ 3 ] ? match[ 3 ].length : 0;
			this.maximumFractionDigits = this.minimumFractionDigits + ( match[ 4 ] ? match[ 4 ].length : 0 );
			this.prefix = pattern.substring( 0, match.index );
			this.suffix = pattern.substring( match.index + match[ 0 ].length );
		}
		else
		{
			console.warn( 'Invalid or unsupported decimal format pattern', pattern );
			this.minimumIntegerDigits = 1;
			this.maximumIntegerDigits = 99;
			this.minimumFractionDigits = 1;
			this.maximumFractionDigits = 1;
			this.prefix = '';
			this.suffix = '';
		}
	}

	/**
	 * Formats the given number.
	 *
	 * @param {number} value Number to format.
	 *
	 * @returns {string} Formatted number.
	 */
	format( value )
	{
		const sign = Math.sign( value ) < 0 ? '-' : '';
		const absValue = Math.abs( value );

		const formatted = String( absValue );
		let decimalSeparator = formatted.indexOf( '.' );

		const integerDigits = decimalSeparator === -1 ? formatted.length : decimalSeparator;
		const fractionDigits = decimalSeparator === -1 ? 0 : formatted.length - decimalSeparator - 1;

		let result = formatted;

		if ( fractionDigits > this.maximumFractionDigits )
		{
			result = absValue.toFixed( this.maximumFractionDigits );
			let lastNonZero = result.length - 1;
			while ( ( lastNonZero - decimalSeparator > this.minimumFractionDigits ) && ( result[ lastNonZero ] === '0' ) )
			{
				lastNonZero--;
			}
			result = result.substring( 0, ( lastNonZero > decimalSeparator ) ? lastNonZero + 1 : decimalSeparator );
		}
		else if ( this.minimumFractionDigits > 0 )
		{
			if ( fractionDigits === 0 )
			{
				decimalSeparator = result.length;
				result += '.';
			}

			for ( let i = fractionDigits; i < this.minimumFractionDigits; i++ )
			{
				result += '0';
			}
		}

		if ( ( this.minimumIntegerDigits === 0 ) && ( absValue < 1 ) )
		{
			result = result.substring( decimalSeparator );
		}
		else
		{
			for ( let i = integerDigits; i < this.minimumIntegerDigits; i++ )
			{
				result = '0' + result;
			}
		}

		return this.prefix + sign + result + this.suffix;
	}
}
